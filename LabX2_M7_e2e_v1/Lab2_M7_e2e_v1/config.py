"""Conexion con el modelo — la misma del Lab6 del Modulo 6.

El SDK cliente es compatible con cualquier servidor que exponga la API de chat
(el servidor local del M6 o un proveedor remoto): solo cambian las tres
variables del .env. El codigo del agente (agente_generador.py) no sabe cual hay
detras. Ese es el punto.
"""
import os
from pathlib import Path

from dotenv import load_dotenv
from openai import OpenAI

RAIZ = Path(__file__).resolve().parent
load_dotenv(RAIZ / ".env")

BASE_URL = os.getenv("LOCALHOST_BASE_URL") or os.getenv("OPENAI_BASE_URL") or "http://localhost:11434/v1"
API_KEY = os.getenv("LOCALHOST_API_KEY") or os.getenv("OPENAI_API_KEY") or "local"
MODEL = os.getenv("LOCALHOST_MODEL") or os.getenv("OPENAI_MODEL") or "llama3.2:3b"
TIMEOUT = float(os.getenv("TIMEOUT_MODELO", "120"))


def crear_cliente() -> OpenAI:
    return OpenAI(base_url=BASE_URL, api_key=API_KEY, timeout=TIMEOUT)


def resumen() -> str:
    return f"modelo={MODEL} via {BASE_URL}"
