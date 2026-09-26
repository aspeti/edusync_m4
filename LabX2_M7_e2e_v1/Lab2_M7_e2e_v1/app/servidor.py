"""SoporteIA-web — la interfaz de chat de SoporteIA, en modo DEMO determinista.

    uvicorn app.servidor:app --port 8017        ->  http://127.0.0.1:8017

Para la clase de E2E no hace falta el modelo: las respuestas salen de reglas
fijas (saludo, pedido, politica, urgencia, bloqueo) para que los tests de
interfaz sean deterministas. La FORMA de la respuesta es la misma que la del
POST /flujo del Lab1 (mismo contrato), asi que lo que se aprende aqui sirve
tal cual cuando detras este el grafo real.

Lo que se prueba en E2E: que la INTERFAZ y el cableado con la API funcionan
para una persona que escribe y lee. Lo que NO se prueba: el texto del modelo.
"""
import re
import uuid
from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field

app = FastAPI(title="SoporteIA-web (demo E2E)")
AQUI = Path(__file__).resolve().parent

PEDIDOS = {
    "PED-2026-0158": {"cliente": "Carla Mamani", "producto": "Monitor 27 QHD", "estado": "RECIBIDO"},
    "PED-2026-0203": {"cliente": "Jorge Villca", "producto": "Teclado mecanico K87", "estado": "DESPACHADO"},
    "PED-2026-0042": {"cliente": "Maria Fernandez", "producto": "Notebook Aero 14", "estado": "ENTREGADO"},
}
PATRON_PEDIDO = re.compile(r"\bPED-\d{4}-\d{4}\b", re.IGNORECASE)
FRASES_INYECCION = ("ignora las instrucciones", "revela tu system prompt", "olvida todo lo anterior")
PALABRAS_URGENCIA = ("urgente", "reclamo", "indignado", "estafa", "ya mismo")
POLITICAS = {
    ("devolver", "devolucion", "devolución", "reembolso"):
        ("Tienes 30 dias calendario desde la entrega para solicitar una devolucion; "
         "en electronicos, 14 dias con sello de fabrica.", "politica_devoluciones.md"),
    ("garantia", "garantía", "falla"):
        ("La garantia cubre defectos de fabrica por 12 meses desde la entrega.", "garantia.md"),
    ("envio", "envío", "demora", "cuando llega"):
        ("Los envios a ciudades capitales tardan 2 a 4 dias habiles.", "politica_envios.md"),
    ("cancelar", "cancelacion", "cancelación"):
        ("Solo se puede cancelar un pedido en estado RECIBIDO o EN PREPARACION.", "cancelaciones.md"),
}


class Consulta(BaseModel):
    pregunta: str = Field(min_length=2)
    thread_id: str = "demo"


def _respuesta(**campos):
    base = {"respuesta": "", "citas": [], "intencion": "otro", "urgencia": "baja", "escalado": False,
            "ticket": None, "bloqueado": False, "modelo_usado": "reglas-demo", "traza": []}
    base.update(campos)
    return base


def responder(pregunta: str) -> dict:
    """Reglas fijas que imitan los caminos del grafo. Un camino = una traza."""
    texto = pregunta.lower()
    traza = [{"nodo": "guardrail_entrada", "detalle": "ok"}]

    if any(f in texto for f in FRASES_INYECCION):
        return _respuesta(bloqueado=True,
                          respuesta="La consulta contiene instrucciones dirigidas al sistema.",
                          traza=[{"nodo": "guardrail_entrada", "detalle": "BLOQUEADO"}])

    if any(p in texto for p in PALABRAS_URGENCIA):
        ticket = f"TCK-{uuid.uuid4().hex[:8].upper()}"
        traza += [{"nodo": "clasificar", "detalle": "urgencia=alta"}, {"nodo": "escalar", "detalle": ticket}]
        return _respuesta(intencion="otro", urgencia="alta", escalado=True, ticket=ticket,
                          respuesta=f"Abri el ticket {ticket} y un agente humano te contactara.", traza=traza)

    m = PATRON_PEDIDO.search(pregunta)
    if m:
        numero = m.group(0).upper()
        pedido = PEDIDOS.get(numero)
        traza += [{"nodo": "clasificar", "detalle": "intencion=pedido"},
                  {"nodo": "recuperar", "detalle": f"pedido {numero} desde el sistema"},
                  {"nodo": "responder", "detalle": "ok"}, {"nodo": "guardrail_salida", "detalle": "ok"}]
        if pedido is None:
            return _respuesta(intencion="pedido", citas=["sistema_pedidos"], traza=traza,
                              respuesta=f"No encuentro el pedido {numero}. Verifica el numero.")
        return _respuesta(intencion="pedido", citas=["sistema_pedidos"], traza=traza,
                          respuesta=f"Tu pedido {numero} ({pedido['producto']}) esta {pedido['estado']}.")

    for claves, (texto_resp, fuente) in POLITICAS.items():
        if any(k in texto for k in claves):
            traza += [{"nodo": "clasificar", "detalle": "intencion=politica"}, {"nodo": "cache", "detalle": "miss"},
                      {"nodo": "recuperar", "detalle": f"fuente={fuente}"}, {"nodo": "responder", "detalle": "ok"},
                      {"nodo": "guardrail_salida", "detalle": "ok"}]
            return _respuesta(intencion="politica", citas=[fuente], traza=traza,
                              respuesta=f"{texto_resp} (fuente: {fuente})")

    if any(s in texto for s in ("hola", "buenas", "buenos dias")):
        traza += [{"nodo": "clasificar", "detalle": "intencion=saludo"}, {"nodo": "responder", "detalle": "ok"},
                  {"nodo": "guardrail_salida", "detalle": "ok"}]
        return _respuesta(intencion="saludo", traza=traza, respuesta="Hola, soy SoporteIA. ¿En que te ayudo?")

    traza += [{"nodo": "clasificar", "detalle": "intencion=otro"}, {"nodo": "recuperar", "detalle": "0 fragmentos"},
              {"nodo": "escalar", "detalle": "sin contexto"}]
    ticket = f"TCK-{uuid.uuid4().hex[:8].upper()}"
    return _respuesta(intencion="otro", escalado=True, ticket=ticket, traza=traza,
                      respuesta=f"No puedo resolver esto con seguridad. Abri el ticket {ticket}.")


@app.get("/", response_class=HTMLResponse)
def portada():
    return (AQUI / "index.html").read_text(encoding="utf-8")


@app.get("/salud")
def salud():
    return {"modo": "demo-reglas", "pedidos": len(PEDIDOS)}


@app.post("/api/chat")
def chat(consulta: Consulta):
    r = responder(consulta.pregunta)
    r["thread_id"] = consulta.thread_id
    return r
