---
id: DD-UC-028
titulo: "Asistente — RAG de procesos EduSync"
producto: "EduSync"
grupo: "G-EduSync"
fsd_uc:
  - "NFR-007"
  - "FSD-UC-016"
prd_refs:
  - "PRD-NFR-007"
adrs:
  - "ADR-0021"
prompts:
  - "PR-IMPL-028"
release: "release/3.0.0"
status: aprobado
fecha: "27/09/2026"
autores:
  - "Rodrigo Aspeti"
---

# Design Doc `DD-UC-028` — Camino PROCESO (RAG léxico sobre procesos EduSync)

> El corpus es exclusivamente institucional: gestiones, calificaciones, periodos, RUDE, roles y SIE.

## 1. Objetivo

Responder «cómo funciona el proceso» (cálculo de notas, periodos, RUDE, roles, SIE) con texto **citado** del corpus, 0 turnos LLM si hay match, sin Chroma ni embeddings.

## 2. Diseño

Orden del grafo: KEYWORD → CONSULTA → **PROCESO** → SALUDO → ReAct.

Clasificador Java (`esPreguntaDeProceso`): cómo / qué es / quién puede / RUDE / SIE / periodo cerrado / floor / cálculo de notas.

Recuperación: overlap de tokens sobre `ai/base_conocimiento/*.md` en classpath (espejo en `docs/ai/base_conocimiento/`). Umbral mínimo de puntuación; top-2 fragmentos. Respuesta con `Fuente: archivo.md`.

Sin LangGraph, sin Chroma, sin embeddings de terceros.

## 3. Fuera de alcance

Embeddings Ollama, caché semántico, nodo ESCALAR, checkpoint SQLite.
