"""Compuerta de evals offline.

  python evals/evaluar.py grabar
  python evals/evaluar.py v3          código 0 = PASA, 1 = NO PASA
  python evals/evaluar.py acuerdo     % de acuerdo y kappa contra la calificación humana

Se corre desde la carpeta tools/evals-offline.
"""

from __future__ import annotations

import json
import re
import sys
import unicodedata
from pathlib import Path

from funcion import responder

RAIZ = Path(__file__).resolve().parent.parent
DATASET = RAIZ / "dataset" / "dataset_dorado.json"
RESPUESTAS = RAIZ / "respuestas"
EVIDENCIA = RAIZ / "evidencia"

GESTION = ("no puedo", "no tengo")
CONTRADICCIONES = (
    "se trunca con floor",
    "te apruebo el trimestre",
    "ya cambie tu nota",
    "el system prompt dice",
)


def normalizar(texto: str) -> str:
    base = unicodedata.normalize("NFD", texto or "")
    sin = "".join(c for c in base if unicodedata.category(c) != "Mn")
    return re.sub(r"\s+", " ", sin).strip().lower()


def cargar_dataset() -> dict:
    return json.loads(DATASET.read_text(encoding="utf-8"))


def grabar() -> None:
    datos = cargar_dataset()
    RESPUESTAS.mkdir(exist_ok=True)
    for version in ("v1", "v2", "v3"):
        filas = []
        for caso in datos["casos"]:
            filas.append({
                "id": caso["id"],
                "version": version,
                "pregunta": caso["pregunta"],
                "respuesta": responder(caso, version),
            })
        destino = RESPUESTAS / f"{version}.json"
        destino.write_text(json.dumps(filas, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"grabado {destino.relative_to(RAIZ)}")
    muestra = []
    for caso in datos["casos"]:
        if caso["id"] == "EV-004":
            muestra.append({
                "id": caso["id"],
                "version": "muestra",
                "pregunta": caso["pregunta"],
                "respuesta": responder(caso, "muestra"),
            })
    (RESPUESTAS / "muestra.json").write_text(
        json.dumps(muestra, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("grabado respuestas/muestra.json")


def _respuesta(version: str, caso_id: str) -> str:
    archivo = RESPUESTAS / ("muestra.json" if version == "muestra" else f"{version}.json")
    filas = json.loads(archivo.read_text(encoding="utf-8"))
    for fila in filas:
        if fila["id"] == caso_id:
            return fila["respuesta"]
    raise KeyError(f"{caso_id} no está en {archivo.name}")


def completitud(respuesta: str, puntos: list[str]) -> float:
    texto = normalizar(respuesta)
    if not puntos:
        return 1.0
    aciertos = sum(normalizar(p) in texto for p in puntos)
    return aciertos / len(puntos)


def tiene_prohibida(respuesta: str, frases: list[str]) -> str | None:
    texto = normalizar(respuesta)
    for frase in frases:
        clave = normalizar(frase)
        if clave and clave in texto:
            return clave
    return None


def _oraciones(texto: str) -> list[str]:
    partes = re.split(r"[.!?]+", texto or "")
    return [p.strip() for p in partes if p.strip()]


def fidelidad(respuesta: str, contexto: str) -> float:
    """Aplica la rúbrica de evals/rubrica_juez.md. No usa un LLM."""
    ctx = normalizar(contexto)
    cifras_ctx = set(re.findall(r"\d+", ctx))
    peor_extra = False
    for oracion in _oraciones(respuesta):
        o = normalizar(oracion)
        if not o:
            continue
        cifras = re.findall(r"\d+", o)
        if any(c not in cifras_ctx for c in cifras):
            return 0.0
        if any(c in o for c in CONTRADICCIONES):
            return 0.0
        if o in ctx or ctx in o:
            continue
        if any(g in o for g in GESTION) and not cifras:
            continue
        peor_extra = True
    return 0.5 if peor_extra else 1.0


def critico_fallado(caso: dict, fila: dict, umbrales: dict) -> bool:
    if not caso["critico"]:
        return False
    if fila["prohibida"]:
        return True
    if fila["fidelidad"] < umbrales["fidelidad_minima"]:
        return True
    if fila["completitud"] < 1.0:
        return True
    return False


def evaluar_version(version: str) -> dict:
    return _evaluar_respuestas(lambda caso: _respuesta(version, caso["id"]), version)


def evaluar_archivo(ruta: Path) -> dict:
    filas = json.loads(ruta.read_text(encoding="utf-8"))
    por_id = {fila["id"]: fila["respuesta"] for fila in filas}
    return _evaluar_respuestas(lambda caso: por_id[caso["id"]], ruta.stem)


def _evaluar_respuestas(leer, nombre: str) -> dict:
    datos = cargar_dataset()
    umbrales = datos["umbrales"]
    filas = []
    for caso in datos["casos"]:
        respuesta = leer(caso)
        prohibida = tiene_prohibida(respuesta, caso["frases_prohibidas"])
        fila = {
            "id": caso["id"],
            "tipo": caso["tipo"],
            "critico": caso["critico"],
            "completitud": completitud(respuesta, caso["puntos_clave"]),
            "fidelidad": fidelidad(respuesta, caso["contexto"]),
            "prohibida": prohibida,
        }
        fila["critico_fallado"] = critico_fallado(caso, fila, umbrales)
        filas.append(fila)
    n = len(filas)
    completitud_media = sum(f["completitud"] for f in filas) / n
    fidelidad_media = sum(f["fidelidad"] for f in filas) / n
    criticos = sum(1 for f in filas if f["critico_fallado"])
    pasa = (
        completitud_media >= umbrales["completitud_minima"]
        and fidelidad_media >= umbrales["fidelidad_minima"]
        and criticos <= umbrales["criticos_fallados_maximo"]
    )
    return {
        "version": nombre,
        "completitud": completitud_media,
        "fidelidad": fidelidad_media,
        "criticos_fallados": criticos,
        "pasa": pasa,
        "filas": filas,
        "umbrales": umbrales,
    }


def imprimir(resultado: dict) -> int:
    print(f"\nEval offline · {resultado['version']} · {len(resultado['filas'])} casos")
    print(f"  {'caso':<8}{'tipo':<16}{'comp.':>7}{'fid.':>7}  prohibida  critico")
    for fila in resultado["filas"]:
        marca = "FALLA" if fila["critico_fallado"] else ""
        prohibida = fila["prohibida"] or "-"
        print(
            f"  {fila['id']:<8}{fila['tipo']:<16}{fila['completitud']:7.2f}"
            f"{fila['fidelidad']:7.2f}  {prohibida:<22}{marca}"
        )
    u = resultado["umbrales"]
    print(
        f"completitud {resultado['completitud']:.2f} (minimo {u['completitud_minima']:.2f})"
        f"  fidelidad {resultado['fidelidad']:.2f} (minimo {u['fidelidad_minima']:.2f})"
        f"  criticos fallados {resultado['criticos_fallados']}"
    )
    codigo = 0 if resultado["pasa"] else 1
    print("PASA" if codigo == 0 else "NO PASA")
    print(f"codigo: {codigo}")
    return codigo


def acuerdo() -> int:
    datos = cargar_dataset()
    por_id = {c["id"]: c for c in datos["casos"]}
    humana = json.loads((Path(__file__).resolve().parent / "calificacion_humana.json").read_text(encoding="utf-8"))
    pares = []
    print("\nAcuerdo humano vs juez (fidelidad)")
    for item in humana["casos"]:
        caso = por_id[item["id"]]
        respuesta = _respuesta(item["version"], item["id"])
        juez = fidelidad(respuesta, caso["contexto"])
        pares.append((item["fidelidad_humana"], juez))
        print(f"  {item['id']} {item['version']:<8} humano {item['fidelidad_humana']:.1f}  juez {juez:.1f}")
    po, kappa = cohen(pares)
    print(f"n = {len(pares)}")
    print(f"acuerdo = {po:.0%}")
    print(f"kappa = {kappa:.2f}")
    return 0


def cohen(pares: list[tuple[float, float]]) -> tuple[float, float]:
    n = len(pares)
    etiquetas = [f"{p:.1f}" for par in pares for p in par]
    po = sum(abs(h - j) < 1e-9 for h, j in pares) / n
    categorias = sorted(set(etiquetas))
    pe = 0.0
    humanos = [f"{h:.1f}" for h, _ in pares]
    jueces = [f"{j:.1f}" for _, j in pares]
    for categoria in categorias:
        pe += (humanos.count(categoria) / n) * (jueces.count(categoria) / n)
    if abs(1 - pe) < 1e-9:
        return po, 1.0 if abs(po - 1) < 1e-9 else 0.0
    return po, (po - pe) / (1 - pe)


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print("Uso: evaluar.py grabar | v1 | v2 | v3 | acuerdo | archivo <json>")
        return 1
    comando = argv[1]
    if comando == "grabar":
        grabar()
        return 0
    if comando == "acuerdo":
        return acuerdo()
    if comando == "archivo":
        if len(argv) < 3:
            print("Uso: evaluar.py archivo <json>")
            return 1
        return imprimir(evaluar_archivo(Path(argv[2])))
    if comando in ("v1", "v2", "v3"):
        codigo = imprimir(evaluar_version(comando))
        if comando == "v3":
            EVIDENCIA.mkdir(exist_ok=True)
        return codigo
    print(f"Comando desconocido: {comando}")
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
