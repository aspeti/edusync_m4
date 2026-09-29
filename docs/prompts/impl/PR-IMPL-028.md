# PR-IMPL-028 — RAG de procesos EduSync (camino PROCESO)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-028` |
| Título | Corpus de procesos EduSync + camino PROCESO (RAG léxico) |
| Artefacto origen | `docs/design/DD-UC-028.md`, `docs/adr/0021-*.md` |
| ID origen | `DD-UC-028`, `ADR-0021`, `NFR-007` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 27/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> Corpus solo de procesos EduSync. No LangGraph. No Chroma.

## 1. Task

Corpus `docs/ai/base_conocimiento/` + classpath `ai/base_conocimiento/`. `RecuperadorProcesosEdusync`. Nodo PROCESO en el grafo. Tests: cálculo de notas cita `round`; RUDE cita el corpus. KEYWORD/CONSULTA intactos.

## 2. Stop condition

`mvn test` y `ng build` verdes.
