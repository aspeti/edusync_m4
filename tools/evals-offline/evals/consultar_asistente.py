"""Manda las 12 preguntas al asistente real y guarda la respuesta.

No pisa respuestas/v3.json. No escribe el JWT.
Uso, desde tools/evals-offline:
  python evals/consultar_asistente.py
"""

from __future__ import annotations

import json
import os
import urllib.error
import urllib.request
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
DATASET = RAIZ / "dataset" / "dataset_dorado.json"
SALIDA = Path(os.environ.get(
    "EDUSYNC_EVAL_SALIDA", str(RAIZ / "respuestas" / "asistente_real.json")))
BASE = os.environ.get("EDUSYNC_REDTEAM_BASE_URL", "http://127.0.0.1:8080")
TIMEOUT = 120


def _post(ruta: str, payload: dict, token: str | None = None) -> tuple[int, dict]:
    datos = json.dumps(payload).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(BASE + ruta, data=datos, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            cuerpo = resp.read().decode("utf-8")
            return resp.status, json.loads(cuerpo) if cuerpo else {}
    except urllib.error.HTTPError as ex:
        cuerpo = ex.read().decode("utf-8", errors="replace")
        try:
            parsed = json.loads(cuerpo)
        except json.JSONDecodeError:
            parsed = {"error": cuerpo[:300]}
        return ex.code, parsed


def login() -> str:
    email = os.environ.get("EDUSYNC_EVAL_EMAIL", "sysadmin@edusync.local")
    password = os.environ.get("EDUSYNC_EVAL_PASSWORD", "changeme_local_dev")
    status, data = _post("/api/v1/auth/login", {"email": email, "password": password})
    token = data.get("accessToken") or ""
    if status != 200 or not token:
        raise SystemExit(f"login HTTP {status}")
    return token


def main() -> None:
    casos = json.loads(DATASET.read_text(encoding="utf-8"))["casos"]
    token = login()
    filas = []
    for caso in casos:
        status, data = _post(
            "/api/v1/ai/agente",
            {"pregunta": caso["pregunta"], "confirmed": False},
            token,
        )
        if status == 200:
            respuesta = data.get("respuesta") or ""
            camino = data.get("camino") or ""
        else:
            respuesta = data.get("message") or data.get("error") or f"HTTP {status}"
            camino = f"HTTP_{status}"
        filas.append({
            "id": caso["id"],
            "version": "asistente_real",
            "pregunta": caso["pregunta"],
            "respuesta": respuesta,
            "camino": camino,
            "http": status,
        })
        print(f"{caso['id']} HTTP {status} camino {camino} chars {len(respuesta)}")
        SALIDA.write_text(json.dumps(filas, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"guardado {SALIDA.relative_to(RAIZ)}")


if __name__ == "__main__":
    main()
