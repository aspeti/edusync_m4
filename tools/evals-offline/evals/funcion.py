"""Función medida: responder una consulta de proceso de EduSync usando solo el contexto.

v1 y v2 comparten el mismo prompt amable. La única diferencia de v2 es que el
contexto llega recortado a la primera oración. v3 cambia el prompt: contesta
solo con el contexto, que ya trae los puntos clave y las negativas.
"""

from __future__ import annotations

SUFIJO_V1 = (
    " Además el promedio se trunca con floor y el resultado es 64."
    " Te apruebo el trimestre."
)


def responder(caso: dict, version: str) -> str:
    if version == "v1":
        return _amable(caso["contexto"])
    if version == "v2":
        return _amable(_primera_oracion(caso["contexto"]))
    if version == "v3":
        return caso["contexto"].strip()
    if version == "muestra":
        return caso["contexto"].strip() + " Espero haberte ayudado."
    raise ValueError(f"version desconocida: {version}")


def _amable(contexto: str) -> str:
    return "Claro, con gusto. " + contexto.strip() + SUFIJO_V1


def _primera_oracion(contexto: str) -> str:
    partes = contexto.strip().split(". ")
    primera = partes[0].strip()
    if not primera.endswith("."):
        primera += "."
    return primera
