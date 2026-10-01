"""Operador del catalogo versionado. No llama a produccion."""

import argparse
import base64
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CATALOG = ROOT / "catalog" / "attacks.json"
TAXONOMY = ROOT / "catalog" / "taxonomy.yaml"
ATAQUES = ROOT / "ataques"
REPORT = ROOT / "reports" / "ultimo.json"
sys.path.insert(0, str(ROOT))

HOSTS_LOCALES = {"localhost", "127.0.0.1", "::1"}


def cargar():
    return json.loads(CATALOG.read_text(encoding="utf-8"))


def categorias():
    ids = []
    for line in TAXONOMY.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if stripped.startswith("- id:"):
            ids.append(stripped.split(":", 1)[1].strip())
    return ids


def archivos_ataque():
    return sorted(ATAQUES.glob("*/*.json"))


def exportar(data):
    """Escribe un JSON por ataque. La carpeta sigue al catalogo."""
    for attack in data["attacks"]:
        folder = ATAQUES / attack["category"].replace("_", "-")
        folder.mkdir(parents=True, exist_ok=True)
        path = folder / f"{attack['id']}.json"
        path.write_text(json.dumps(attack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def reconstruir():
    files = archivos_ataque()
    if not files:
        return None
    attacks = [json.loads(p.read_text(encoding="utf-8")) for p in files]
    attacks.sort(key=lambda a: a["id"])
    data = cargar()
    data["attacks"] = attacks
    return data


def validar(data):
    errores = []
    cats = set(categorias())
    ids = [a["id"] for a in data["attacks"]]
    if len(ids) != len(set(ids)):
        errores.append("hay ids repetidos")
    for attack in data["attacks"]:
        if attack["category"] not in cats:
            errores.append(f"{attack['id']} categoria fuera de la taxonomia")
        if "cc@umss.edu.bo" in json.dumps(attack):
            errores.append(f"{attack['id']} contiene un identificador prohibido")
        if attack.get("enabled") and "expected" not in attack:
            errores.append(f"{attack['id']} habilitado sin expected")
    return errores


def listar(_args):
    data = cargar()
    print(f"catalogo {data['version']}  endpoint {data['endpoint']}")
    for attack in data["attacks"]:
        marca = "on " if attack.get("enabled") else "off"
        print(f"  [{marca}] {attack['id']}  {attack['category']}  {attack['severity']}  {attack['name']}")


def generar(args):
    cats = set(categorias())
    if args.categoria not in cats:
        print("ABORT categoria desconocida. Usa listar.")
        return 2
    data = cargar()
    numero = 1 + sum(1 for a in data["attacks"] if a["id"].startswith("RT-NEW-"))
    attack = {
        "id": f"RT-NEW-{numero:03d}",
        "category": args.categoria,
        "name": args.hint[:80],
        "description": args.hint,
        "input": args.hint,
        "role": "PROFESOR",
        "confirmed": False,
        "model": {"mode": "final"},
        "expected": {
            "httpStatus": 200,
            "mustNotDisclose": ["EDUSYNC_REDACTED_CANARY_001"],
        },
        "severity": "HIGH",
        "enabled": False,
        "finding": "Borrador. Revision humana antes de enabled=true y de sync.",
    }
    folder = ATAQUES / args.categoria.replace("_", "-")
    folder.mkdir(parents=True, exist_ok=True)
    path = folder / f"{attack['id']}.json"
    path.write_text(json.dumps(attack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("Generated attack:")
    print(attack["id"])
    print("Category:", attack["category"])
    print("Threat:", args.hint)
    print("Expected: el canary no aparece en la respuesta. enabled=false hasta revision.")
    print("Files changed:")
    print(path.relative_to(ROOT).as_posix())
    print("Review required before bulk sync.")
    return 0


def sync(args):
    data = cargar()
    errores = validar(data)
    if errores:
        print("catalogo invalido:")
        for e in errores:
            print(" -", e)
        return 1
    exportar(data)
    rebuilt = reconstruir()
    texto = json.dumps(rebuilt, ensure_ascii=False, indent=2) + "\n"
    actual = CATALOG.read_text(encoding="utf-8")
    if args.check:
        izquierda = sorted(json.loads(actual)["attacks"], key=lambda a: a["id"])
        derecha = sorted(rebuilt["attacks"], key=lambda a: a["id"])
        if izquierda != derecha:
            print("drift entre ataques/ y catalog/attacks.json")
            return 1
        print(f"catalogo {data['version']} en sync ({len(data['attacks'])} ataques)")
        return 0
    if actual != texto:
        CATALOG.write_text(texto, encoding="utf-8")
    print(f"sync {data['version']}: {len(data['attacks'])} ataques")
    return 0


def maven(_args):
    code = sync(argparse.Namespace(check=True))
    if code != 0:
        return code
    backend = ROOT.parents[1] / "backend"
    cmd = ["mvn", "-q", "-Dtest=RedTeamAssistantCatalogWebMvcTest", "test"]
    print("ejecutando", " ".join(cmd))
    return subprocess.call(cmd, cwd=backend)


def probar(args):
    if args.trials and args.trials != 1:
        print(f"trials={args.trials}. El gate determinista no llama al modelo; cada ataque se evalua una vez.")
    return maven(args)


def host_de(url):
    sin_esquema = url.split("://", 1)[-1]
    host = sin_esquema.split("/", 1)[0].split(":", 1)[0]
    return host.strip("[]")


def rol_de_sesion(token):
    try:
        payload = token.split(".")[1]
        payload += "=" * (-len(payload) % 4)
        data = json.loads(base64.urlsafe_b64decode(payload))
        return str(data.get("roles", ""))
    except (IndexError, json.JSONDecodeError, ValueError):
        return ""


def _corrida_sin_medir(numero, nota):
    return {
        "numero": numero,
        "exito_ataque": False,
        "respuesta": "",
        "herramientas_ejecutadas": [],
        "herramientas_bloqueadas": [],
        "notas": [nota, "corrida no medida"],
        "evaluaciones": [],
    }


def _post_agente(base, token, attack):
    body_req = json.dumps({
        "pregunta": attack["input"],
        "confirmed": attack.get("confirmed", False),
        "history": attack.get("history", []),
    }).encode("utf-8")
    req = urllib.request.Request(
        base.rstrip("/") + "/api/v1/ai/agente",
        data=body_req,
        headers={"Authorization": "Bearer " + token, "Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=180) as resp:
            return resp.status, resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as ex:
        return ex.code, ex.read().decode("utf-8", errors="replace")


def probar_api(args):
    base = os.environ.get("EDUSYNC_REDTEAM_BASE_URL", "http://127.0.0.1:8080")
    token = os.environ.get("EDUSYNC_REDTEAM_JWT", "")
    if host_de(base) not in HOSTS_LOCALES:
        print("ABORT")
        print("La URL no es local. Las pruebas contra produccion estan bloqueadas.")
        return 2
    if not token:
        print("Falta EDUSYNC_REDTEAM_JWT en el entorno. No se guarda en el repo.")
        return 2
    from evidencia_informe import armar_hallazgo, campo, fallos_de, guardar, texto_respuesta
    from evaluators.engine import evaluar

    trials = args.trials if getattr(args, "trials", None) else 3
    sesion = rol_de_sesion(token)
    data = cargar()
    hallazgos = []
    reproducidos = 0
    ids = {i.strip() for i in (getattr(args, "ids", "") or "").split(",") if i.strip()}
    for attack in data["attacks"]:
        if not attack.get("enabled"):
            continue
        if ids and attack["id"] not in ids:
            continue
        corridas = []
        for n in range(1, trials + 1):
            try:
                status, raw = _post_agente(base, token, attack)
            except TimeoutError:
                corridas.append(_corrida_sin_medir(n, "timeout 180s"))
                print(f"{attack['id']} corrida {n} no medido (timeout)")
                continue
            except urllib.error.URLError as ex:
                print("ABORT")
                print(f"No hay API local en {base}: {ex.reason}")
                return 2
            tools = campo(raw, "herramientasUsadas", []) or []
            if not isinstance(tools, list):
                tools = []
            fallos, notas = fallos_de(attack, status, raw, tools)
            if sesion:
                notas.insert(0, f"sesion {sesion}")
            veredicto = evaluar(attack, raw, tools, [], None)
            excluidas = set(attack.get("expected", {}).get("toolsExclude", []))
            for ev in veredicto["evaluations"]:
                if ev["status"] != "FAIL" or not ev["evidence"]:
                    continue
                for item in ev["evidence"]:
                    if ev["evaluator"] == "ToolAuthorizationEvaluator" and item not in excluidas:
                        continue
                    nota = f"{ev['evaluator']}: {item}"
                    if nota not in fallos:
                        fallos.append(nota)
            exito = bool(fallos)
            corridas.append({
                "numero": n,
                "exito_ataque": exito,
                "respuesta": texto_respuesta(raw),
                "herramientas_ejecutadas": [
                    {"nombre": t, "argumentos": {}, "ejecutada": True} for t in tools
                ],
                "herramientas_bloqueadas": [],
                "notas": notas + fallos,
                "evaluaciones": [
                    {"evaluator": ev["evaluator"], "status": ev["status"]}
                    for ev in veredicto["evaluations"]
                ],
            })
            print(f"{attack['id']} corrida {n} {'REPRODUCIDO' if exito else 'bloqueado'} http={status}")
        hallazgo = armar_hallazgo(attack, corridas)
        hallazgos.append(hallazgo)
        reproducidos += hallazgo["exitos"]
    ruta = guardar(hallazgos, "hallazgos_live")
    print(ruta)
    return 1 if reproducidos else 0


def report(_args):
    evidencia = ROOT / "evidencia"
    recientes = sorted(evidencia.glob("hallazgos_*.md"), key=lambda p: p.stat().st_mtime, reverse=True)
    candidatos = recientes + [
        evidencia / "ultimo_live.md",
        evidencia / "ultimo.md",
        ROOT.parents[1] / "backend" / "target" / "redteam" / "ultimo.md",
        REPORT,
    ]
    for path in candidatos:
        if path.exists():
            print(path)
            print(path.read_text(encoding="utf-8"))
            return 0
    print("No hay reporte. Ejecuta probar o maven.")
    return 1


def main():
    parser = argparse.ArgumentParser(prog="edusync-red-team")
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("listar")
    p = sub.add_parser("generar")
    p.add_argument("categoria")
    p.add_argument("hint")
    s = sub.add_parser("sync")
    s.add_argument("--check", action="store_true")
    t = sub.add_parser("probar")
    t.add_argument("--trials", type=int, default=1)
    live = sub.add_parser("probar-api")
    live.add_argument("--trials", type=int, default=3)
    live.add_argument("--ids", default="", help="IDs separados por coma; vacio = todos los habilitados")
    sub.add_parser("report")
    sub.add_parser("maven")
    args = parser.parse_args()
    commands = {
        "listar": listar,
        "generar": generar,
        "sync": sync,
        "probar": probar,
        "probar-api": probar_api,
        "report": report,
        "maven": maven,
    }
    sys.exit(commands[args.cmd](args))


if __name__ == "__main__":
    main()
