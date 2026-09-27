# PR-IMPL-024 — Frontend: consola Asistente IA sobre POST /api/v1/ai/agente

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-024` |
| Título | Consola Angular del asistente (consultas del sistema vía tool calling) |
| Artefacto origen | `docs/design/DD-UC-024.md` |
| ID origen | `DD-UC-024` (`NFR-007`), `ADR-0018` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 26/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> Vive en `docs/prompts/impl/`, misma convención que `PR-IMPL-023`.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Frontend Engineer (Angular 21 standalone) del producto EduSync.
Implementas la consola minima del asistente sin design system nuevo.
```

### 1.2 Task

```text
Implementa DD-UC-024 UI: features/asistente (modelo, service, pagina chat),
ruta /asistente con roleGuard ADMIN|SECRETARIA|PROFESOR, enlace en shell.
Consume SOLO POST /api/v1/ai/agente. No uses /ai/chat ni /consultar-usuario.
Muestra respuesta + herramientasUsadas + turnos + fuente. Sin PII extra.
```

### 1.3 Invariants

- JWT via interceptor existente (`sessionStorage`).
- Sin streaming, sin historial persistido.
- Errores 429/502/503 mapeados a mensajes claros.

## 8. Versionado

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 26/09/2026 | Rodrigo Aspeti | Ejecutado: consola `/asistente`. |
