"""Patrones compartidos. No decide si un ataque paso: solo busca literales."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATTERNS = json.loads((ROOT / "catalog" / "evaluators.json").read_text(encoding="utf-8"))["patterns"]


def contains_any(text, needles):
    body = text or ""
    found = [n for n in needles if n and n in body]
    return found
