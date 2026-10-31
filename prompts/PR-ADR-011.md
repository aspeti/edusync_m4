# PR-ADR-011 — RAG vectorial del camino PROCESO

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-011` |
| Título | ADR-0024, embeddings en memoria para PROCESO |
| Artefacto origen | `docs/design/DD-UC-028.md`, `docs/adr/0021-grafo-produccion-guardrails-asistente.md` |
| ID origen | `DD-UC-030`, `ADR-0024` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 02/10/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aprobado** |

## 1. Anatomía

### Role

Eres el arquitecto de EduSync. Registras una decisión costosa de revertir sin tocar el baseline.

### Task

Redactar `ADR-0024`: el camino PROCESO recupera por embeddings del proveedor ya configurado, cita los párrafos y no añade Chroma ni pgvector.

### Context

- `ADR-0021` difirió embeddings.
- `ADR-0017` deja Spring AI detrás de un puerto.
- `ADR-0023` ya eligió Gemini como proveedor por el endpoint compatible con OpenAI, con Ollama disponible.
- El corpus son seis markdown. No hay datos de estudiantes ahí.

### Reasoning

1. Medir el fallo del overlap y del regex con las 12 preguntas offline corridas contra el asistente.
2. Descartar redactar con el LLM, Chroma y pgvector.
3. Aceptar el índice en memoria y el léxico como respaldo técnico.

### Stop condition

El ADR tiene contexto, alternativas, decisión, consecuencias, impacto, reversión, validación y referencias. Estado Aceptada. No edita `docs/baseline/**`.

### Output

`docs/adr/0024-rag-vectorial-camino-proceso.md`.

### Invariants

- No supersede a `ADR-0021` ni a `ADR-0023`.
- `domain/` y `application/` no importan Spring AI.
- Al menos tres alternativas además de la elegida.

### Failure modes

- `E_CHROMA_O_PGVECTOR`: la decisión introduce un store nuevo. Reescribir.
- `E_LLM_REDACTA_CORPUS`: la decisión deja que el chat reescriba la cita. Reescribir.
- `E_BASELINE_TOCADO`: revertir.
