"""agente_generador.py — espejo EduSync de LabX_M7_unit/agente_generador.py.

Lee PROMPT_AGENTE.md + clase + tests a mano, llama al modelo (temperature=0),
escribe un sidecar *AgenteTest.java (no pisa el test a mano) y reporta tokens.

    python agente_generador.py ejercicio_1_analizador
    python agente_generador.py ejercicio_2_guardrail
"""
import re
import sys
import time

import config

sys.stdout.reconfigure(line_buffering=True)

CONTEXTO = {
    "ejercicio_1_analizador": [
        "backend/src/main/java/com/edusync/shared/ai/application/service/AnalizadorIntencionConsultaAcademica.java",
        "backend/src/test/java/com/edusync/shared/ai/application/service/AnalizadorIntencionConsultaAcademicaTest.java",
    ],
    "ejercicio_2_guardrail": [
        "backend/src/main/java/com/edusync/shared/ai/application/service/GuardrailEntradaAgente.java",
        "backend/src/test/java/com/edusync/shared/ai/application/service/GuardrailEntradaAgenteTest.java",
    ],
}
SALIDA = {
    "ejercicio_1_analizador":
        "backend/src/test/java/com/edusync/shared/ai/application/service/AnalizadorIntencionConsultaAcademicaAgenteTest.java",
    "ejercicio_2_guardrail":
        "backend/src/test/java/com/edusync/shared/ai/application/service/GuardrailEntradaAgenteAgenteTest.java",
}


def leer_prompt(ejercicio: str) -> str:
    texto = (config.HERRAMIENTA / ejercicio / "PROMPT_AGENTE.md").read_text(encoding="utf-8")
    bloque = re.search(r"```\n(.*?)```", texto, re.S)
    return bloque.group(1).strip() if bloque else texto


def extraer_codigo(respuesta: str, nombre_clase: str) -> str:
    bloque = re.search(r"```(?:java)?\n(.*?)```", respuesta, re.S)
    codigo = (bloque.group(1) if bloque else respuesta).strip() + "\n"
    if "@Tag" in codigo and not re.search(r"^import org\.junit\.jupiter\.api\.Tag;", codigo, re.M):
        codigo = re.sub(
            r"(package [\w.]+;\s*)",
            r"\1\nimport org.junit.jupiter.api.Tag;\n",
            codigo,
            count=1,
        )
        print("aviso: el modelo uso @Tag sin importarlo; se agrego import (leer siempre lo que devuelve)", flush=True)
    codigo, n = re.subn(
        r"\b((?:public\s+)class\s+)\w+",
        rf"\1{nombre_clase}",
        codigo,
        count=1,
    )
    if n:
        print(f"aviso: clase renombrada a {nombre_clase} (debe coincidir con el sidecar)", flush=True)
    return codigo


def main(ejercicio: str):
    contexto = "\n\n".join(
        f"### {ruta}\n```java\n{(config.RAIZ / ruta).read_text(encoding='utf-8')}\n```"
        for ruta in CONTEXTO[ejercicio]
    )
    mensajes = [
        {"role": "system", "content": (
            "Eres un asistente que escribe tests unitarios JUnit 5 para EduSync. "
            "Devuelve SOLO un bloque de codigo Java completo, sin explicaciones.")},
        {"role": "user", "content": f"ARCHIVOS DEL PROYECTO:\n\n{contexto}\n\nTAREA:\n{leer_prompt(ejercicio)}"},
    ]
    print(f"agente -> {config.resumen()}", flush=True)
    print(f"contexto: {', '.join(CONTEXTO[ejercicio])}  "
          f"(~{sum(len(m['content']) for m in mensajes) // 4:,} tokens estimados)", flush=True)
    print("generando (llama 8B en CPU tarda 1-4 min; no es un cuelgue)...", flush=True)

    t0 = time.time()
    try:
        r = config.crear_cliente().chat.completions.create(
            model=config.MODEL, temperature=0, messages=mensajes)
    except Exception as e:
        sys.exit(
            f"el modelo no respondio ({type(e).__name__}: {e}). "
            "Si ves timeout: el .env de EduSync pone Open WebUI + 120s; este CLI ya usa Ollama /v1. "
            "Sube TIMEOUT_MODELO=600 si sigue cortando."
        )
    segundos = round(time.time() - t0, 1)

    destino = config.RAIZ / SALIDA[ejercicio]
    destino.parent.mkdir(parents=True, exist_ok=True)
    destino.write_text(extraer_codigo(r.choices[0].message.content, destino.stem), encoding="utf-8")
    u = r.usage
    print(f"escrito: {destino.relative_to(config.RAIZ)}", flush=True)
    print(f"tokens: entrada={u.prompt_tokens if u else '?'} "
          f"salida={u.completion_tokens if u else '?'}  tiempo={segundos}s", flush=True)
    clase = destino.stem
    print(f"ahora: cd backend && mvn -Dtest={clase} test     y despues, la auditoria (3 preguntas)", flush=True)


if __name__ == "__main__":
    if len(sys.argv) != 2 or sys.argv[1] not in CONTEXTO:
        sys.exit(f"uso: python agente_generador.py {' | '.join(CONTEXTO)}")
    main(sys.argv[1])
