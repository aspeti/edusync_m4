"""Conexion LLM para el agente E2E — misma logica que tools/ai-test-generator-edusync.

Lee el .env de la raiz del repo (EDUSYNC_AI_PROVIDER=ollama|open-webui).
El codigo del agente no sabe cual proveedor hay detras.
"""
from __future__ import annotations

import os
from pathlib import Path

from dotenv import load_dotenv
from openai import OpenAI

RAIZ_E2E = Path(__file__).resolve().parent
RAIZ_REPO = RAIZ_E2E.parent.parent

# Prioridad: tests/e2e/.env.e2e → raiz .env → Lab vars
load_dotenv(RAIZ_E2E / ".env.e2e", override=False)
load_dotenv(RAIZ_REPO / ".env", override=False)

_PROVIDER = (os.getenv("EDUSYNC_AI_PROVIDER") or "ollama").strip().lower()


def _resolver() -> tuple[str, str, str, float]:
    """Devuelve (base_url, api_key, model, timeout)."""
    timeout = float(os.getenv("TIMEOUT_MODELO") or os.getenv("OLLAMA_TIMEOUT_SECONDS") or "180")

    if _PROVIDER == "open-webui":
        base = (os.getenv("OPEN_WEBUI_BASE_URL") or "http://localhost:3000").rstrip("/")
        return (
            f"{base}/api",
            os.getenv("OPEN_WEBUI_API_KEY") or "sk-local",
            os.getenv("OPEN_WEBUI_MODEL") or "llama3.1:latest",
            float(os.getenv("OPEN_WEBUI_TIMEOUT_SECONDS") or timeout),
        )

    # Compat Lab2: LOCALHOST_* / OPENAI_*
    if os.getenv("LOCALHOST_BASE_URL") or os.getenv("OPENAI_BASE_URL"):
        return (
            (os.getenv("LOCALHOST_BASE_URL") or os.getenv("OPENAI_BASE_URL") or "").rstrip("/"),
            os.getenv("LOCALHOST_API_KEY") or os.getenv("OPENAI_API_KEY") or "local",
            os.getenv("LOCALHOST_MODEL") or os.getenv("OPENAI_MODEL") or "llama3.1:latest",
            timeout,
        )

    # Default Ollama
    base = (os.getenv("OLLAMA_BASE_URL") or "http://localhost:11434").rstrip("/")
    return (
        f"{base}/v1",
        "ollama",
        os.getenv("OLLAMA_MODEL") or "llama3.1:latest",
        float(os.getenv("OLLAMA_TIMEOUT_SECONDS") or timeout),
    )


BASE_URL, API_KEY, MODEL, TIMEOUT = _resolver()


def crear_cliente() -> OpenAI:
    return OpenAI(base_url=BASE_URL, api_key=API_KEY, timeout=TIMEOUT)


def resumen() -> str:
    return f"provider={_PROVIDER} modelo={MODEL} via {BASE_URL}"
