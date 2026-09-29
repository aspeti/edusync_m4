"""Conexion con el modelo — misma idea que LabX_M7_unit/config.py.

Este CLI habla con Ollama /v1 por defecto (como el lab).
NO usa EDUSYNC_AI_PROVIDER=open-webui ni los timeout 120s del .env de EduSync:
esos cortan la generacion de una clase Java en CPU.

Override: LOCALHOST_* / OPENAI_* (lab) o M7_PROVIDER=open-webui.
TIMEOUT_MODELO (default 300).
"""
import os
from pathlib import Path

from dotenv import load_dotenv
from openai import OpenAI

HERRAMIENTA = Path(__file__).resolve().parent
RAIZ = HERRAMIENTA.parent.parent
load_dotenv(RAIZ / ".env")
load_dotenv(HERRAMIENTA / ".env", override=True)

_ollama = (os.getenv("OLLAMA_BASE_URL") or "http://localhost:11434").rstrip("/")
if not _ollama.endswith("/v1"):
    _ollama = _ollama + "/v1"

_webui = (os.getenv("OPEN_WEBUI_BASE_URL") or "").rstrip("/")
_m7 = (os.getenv("M7_PROVIDER") or "ollama").lower()
TIMEOUT = float(os.getenv("TIMEOUT_MODELO") or "300")

if os.getenv("LOCALHOST_BASE_URL") or os.getenv("OPENAI_BASE_URL"):
    BASE_URL = os.getenv("LOCALHOST_BASE_URL") or os.getenv("OPENAI_BASE_URL")
    API_KEY = os.getenv("LOCALHOST_API_KEY") or os.getenv("OPENAI_API_KEY") or "local"
    MODEL = os.getenv("LOCALHOST_MODEL") or os.getenv("OPENAI_MODEL") or os.getenv("OLLAMA_MODEL") or "llama3.1:8b"
elif _m7 == "open-webui" and _webui:
    if _webui.endswith("/api") or _webui.endswith("/v1"):
        BASE_URL = _webui
    else:
        BASE_URL = _webui + "/api"
    API_KEY = os.getenv("OPEN_WEBUI_API_KEY") or "sk-local"
    MODEL = os.getenv("OPEN_WEBUI_MODEL") or "llama3.1:8b"
else:
    BASE_URL = _ollama
    API_KEY = "local"
    MODEL = os.getenv("OLLAMA_MODEL") or os.getenv("LOCALHOST_MODEL") or "llama3.1:8b"


def crear_cliente() -> OpenAI:
    return OpenAI(base_url=BASE_URL, api_key=API_KEY, timeout=TIMEOUT, max_retries=0)


def resumen() -> str:
    return f"modelo={MODEL} via {BASE_URL} timeout={int(TIMEOUT)}s"
