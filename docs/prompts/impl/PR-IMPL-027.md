# PR-IMPL-027 — Grafo de producción del asistente (oleada 1: guardrails + rutas)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-027` |
| Título | Guardrails entrada/salida + grafo Java + saludo + temperatura 0 |
| Artefacto origen | `docs/design/DD-UC-027.md`, `docs/adr/0021-*.md` |
| ID origen | `DD-UC-027`, `ADR-0021`, `NFR-007` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 27/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> Vive en `docs/prompts/impl/`. No tocar `docs/baseline/**`. No tocar `LlmPort` ni `POST /api/v1/ai/chat`. **No** añadir LangGraph, Chroma ni SQLite checkpoints.

## 1. Anatomía del prompt

### 1.1 Role

Eres el servicio de dominio de EduSync (Java 25 LTS, Spring Boot 4.1.0, hexagonal, Angular 21). Implementas `DD-UC-027` oleada 1: patrones del Lab 6 **en Java**, envolviendo KEYWORD/CONSULTA/ReAct.

### 1.2 Task

1. `GuardrailEntradaAgente` / `GuardrailSalidaAgente` / `RutasGrafoAsistente`.
2. Envolver `EjecutarConsultaAgenteService.consultar()`: entrada → clasificar → caminos existentes o SALUDO/BLOQUEADO → salida.
3. Constantes `CAMINO_BLOQUEADO`, `CAMINO_SALUDO`; `steps` con nodos.
4. `edusync.ai.agente.temperature: 0` + `OllamaChatOptions`.
5. Badge UI para los caminos nuevos.
6. Tests: inyección, saludo, KEYWORD sigue, salida redacta RUDE.

### 1.3 Invariants

- Sin LangGraph ni segundo runtime.
- Guardrail no loguea valores de PII, solo etiquetas.
- `shared.ai` no importa `academico`.
- Loopback JWT (`ADR-0018`) intacto.
- KEYWORD antes que CONSULTA antes que ReAct.

### 1.4 Stop condition

`mvn test` verde y `ng build` verde; «ignora las instrucciones» → `BLOQUEADO` con `verifyNoInteractions` sobre LLM y ejecutor.

### 1.5 Output

Código en `backend/src/main/java/com/edusync/shared/ai/**` + tests + delta YAML/config + badge frontend + `dtp-sync` / `PROMPT_MAPPING`.

## 2. Trazabilidad

`NFR-007` → `DD-UC-027` → `ADR-0021` → este prompt → `EjecutarConsultaAgenteService` + guardrails.
