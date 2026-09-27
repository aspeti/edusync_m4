# PR-IMPL-026 — Consultas académicas dinámicas del asistente

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-026` |
| Título | Resolver entidades + `consultar_academico` + camino CONSULTA + contexto |
| Artefacto origen | `docs/ai/conversational-assistant-design.md`, `docs/design/DD-UC-026.md`, `docs/adr/0020-*.md` |
| ID origen | `DD-UC-026`, `ADR-0020`, `NFR-007`, `FSD-UC-016` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 27/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> Vive en `docs/prompts/impl/`. No tocar `docs/baseline/**`. No tocar `LlmPort` ni `POST /api/v1/ai/chat`.

## 1. Anatomía del prompt

### 1.1 Role

Eres el servicio de dominio de EduSync (Java 25 LTS, Spring Boot 4.1.0, hexagonal, Angular 21). Implementas `DD-UC-026` sin violar Modulith, `ADR-0018` (loopback JWT) ni NFR-007 (sin RUDE en JSON de tools ni logs).

### 1.2 Task

Implementar resolución de entidades, consulta parametrizable, camino CONSULTA, catálogo tipado al ReAct, history/contexto y tests de los casos 1–7.

### 1.3 Invariants

- El LLM no inventa UUIDs.
- Agregados solo en `CalculoNotas` / nota provisional (sin `floor()`).
- `shared.ai` no importa `academico`.
- Sin SQL generado.
- Sin Playbook por combinación.

### 1.4 Stop condition

`mvn test` verde y `ng build` verde; camino CONSULTA cubre las frases de ejemplo del diseño.
