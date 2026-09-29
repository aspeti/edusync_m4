# PR-IMPL-025 — Fullstack: asistente v1.1 (catálogo, KEYWORD, formatter, writes, Open WebUI, historial, traza)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-025` |
| Título | Evolución del asistente según `DD-UC-025` / `ADR-0019` |
| Artefacto origen | `docs/design/DD-UC-025.md`, `docs/design/assistant/TOOL-CATALOG.md`, `docs/adr/0019-*.md` |
| ID origen | `DD-UC-025`, `ADR-0019`, `NFR-007` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 26/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado (parcial)** — oleada KEYWORD (`§8` 1–4). Writes, Open WebUI del agente e historial pendientes |

> Vive en `docs/prompts/impl/`. No tocar `docs/baseline/**`. No tocar `LlmPort` ni `POST /api/v1/ai/chat`. Oleada KEYWORD ejecutada 26/09/2026.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres el servicio de dominio shared.ai de EduSync (Java 25 LTS, Spring Boot 4.1.0,
arquitectura hexagonal, Angular 21). Implementas DD-UC-025 sin violar Modulith
ni NFR-007.
```

### 1.2 Task

```text
Implementa el delta del asistente en este orden (DD-UC-025 §8):
1) Extiende AgenteRequest/Response y RespuestaAgente (history, confirmed,
   camino KEYWORD|LLM|NINGUNO, agente=general, steps, confirmacionRequerida).
2) CatalogoHerramientasAgente 1:1 con TOOL-CATALOG.md; linter vs OpenAPI en tests.
   El LLM ya NO recibe el dump del DescubridorHerramientasOpenApiAdapter.
3) EnrutadorPalabrasClaveAgente + FormateadorRespuestaAgente.
   El LLM nunca es autor de `respuesta`.
4) Orquestador: KEYWORD → loop ReAct → formatter; flag llm-habilitado.
5) AgenteAiConfig: Open WebUI (OpenAiChatModel), no OllamaApi.
6) Tools write con preview y confirmed=true; loopback JWT existente.
7) UI /asistente: sessionStorage, history en request, badge camino, steps/tablas,
   botones Confirmar/Cancelar.
MUST NOT: RAG, MCP, copilotos, calificaciones, editar baseline, tocar /ai/chat.
```

### 1.3 Context

- `docs/design/DD-UC-025.md`, `docs/design/assistant/TOOL-CATALOG.md`
- `docs/adr/0018-*.md` (loopback + JWT se conservan), `docs/adr/0019-*.md`
- Stack: Java 25, Boot 4.1.0, Spring AI 2.0.0, Angular 21
- NFR-007: sin PII/RUDE/notas/JWT en logs ni en el texto de chat de estudiantes (`rude` enmascarado)

### 1.4 Invariants

- `shared.ai` no importa paquetes internos de `academico`/`identidad`.
- Writes sin `confirmed=true` no llaman POST/PATCH.
- `camino=KEYWORD` ⇒ `turnos=0`.
- `ModularityTests` 7/7.

### 1.5 Stop condition

Tests de `shared.ai` (router, formatter, confirmed, catálogo prohibido) verdes y `ng build` verde.

### 1.6 Output

Código Java/TS + tests. Tras ejecutar: `@dtp-sync`. No commitear salvo pedido humano.

### 1.7 Failure modes

- `E_LLM_REDACTA_PRODUCTO`: `respuesta` copiada del modelo → rechazar PR
- `E_OPENAPI_COMO_CATALOGO`: el bucle vuelve a enviar todos los GET → rechazar PR
- `E_WRITE_SIN_CONFIRMAR`: mutación con `confirmed=false` → rechazar PR
- `E_OLLAMA_DIRECTO_AGENTE`: `AgenteAiConfig` sigue en `OllamaApi` → rechazar PR
- `E_RAG_COLADO`: embeddings/FTS en este slice → rechazar PR

## 8. Versionado

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 26/09/2026 | Rodrigo Aspeti | Prompt aprobado; ejecución pendiente. |
| v0.2 | 26/09/2026 | Rodrigo Aspeti | Oleada KEYWORD ejecutada (catálogo + router + formatter + traza UI). Writes/Open WebUI/historial pendientes. |
| v0.3 | 26/09/2026 | Rodrigo Aspeti | Oleada C: `create_curso`/`create_materia` con `confirmed` + UI Confirmar. |
