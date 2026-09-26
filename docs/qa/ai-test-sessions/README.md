# Sesiones HITL — AI Test Generation Agent (Fase 1)

Registro de sesiones del CLI `tools/ai-test-generator-edusync/GenerarTest.java`.

La IA **nunca** escribe el estado `APPROVED`. Solo el desarrollador lo hace con `--decidir`.

## Estados

| Estado | Quién lo pone | Significado |
|--------|---------------|-------------|
| `PROPOSED` | CLI (`--proponer`) | Propuesta de escenarios; aún no hay código generado |
| `GENERATED` | CLI (`--generar --aprobar-alcance`) | Test escrito con `@Tag("agente")` |
| `PASSED` / `FAILED` | CLI (`--ejecutar-tests`) | Resultado de `mvn -Dtest=...` (verde ≠ aprobado) |
| `APPROVED` | Humano (`--decidir --veredicto APPROVE`) | Auditoría de 3 preguntas OK |
| `REJECTED` | Humano (`--veredicto REJECT`) | Descartado |
| `MODIFIED` | Humano (`--veredicto MODIFIED`) | El humano editó el test antes de aceptarlo |

## Archivos

Cada sesión es un JSON: `<timestampUTC>-<ClaseObjetivo>.json`.

No contener secretos ni PII real (RUDE, nombres reales, tokens).
