# PR-POC-001 — Estructura documental para POC-01 RLS multitenancy

## 1. Role

Actua como `docs-agent` de EduSync, responsable de documentacion tecnica y trazabilidad de POCs criticas para la defensa final.

## 2. Task

Crear la estructura documental inicial para ejecutar la POC-01 de multitenancy con PostgreSQL 15 Row-Level Security, sin inventar metricas ni marcar la POC como ejecutada.

## 3. Context

- Documentos fuente: `plantillas/POC_TEMPLATE.md`, `docs/DTI.md §12.1`, `docs/adr/0001-multitenancy-rls-postgresql.md`, `AGENTS.md`.
- Artefactos a producir:
  - `docs/pocs/POC-01-rls-multitenancy/README.md`
  - `docs/pocs/POC-01-rls-multitenancy/runbook.md`
  - `docs/pocs/POC-01-rls-multitenancy/evidencia/README.md`
- Restricciones de dominio: DA-01, ADR-0001, NFR-010, IG-05.
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 15, Testcontainers.

## 4. Reasoning

1. Leer `plantillas/POC_TEMPLATE.md` y conservar sus secciones 0-14.
2. Extraer de `docs/DTI.md §12.1` el riesgo, hipotesis, criterio de exito y alcance.
3. Cruzar la decision con ADR-0001 y AGENTS para no violar RLS ni PII.
4. Crear README, runbook y evidencia/README con resultados en estado `Pendiente de ejecucion`.

## 5. Stop Condition

Detente cuando existan los 3 archivos de POC-01, todos referencien `docs/DTI.md §12.1` y ADR-0001, y ninguna seccion contenga metricas ejecutadas.

## 6. Output

Markdown en `docs/pocs/POC-01-rls-multitenancy/` listo para ejecutar la POC:

- `README.md`
- `runbook.md`
- `evidencia/README.md`

## 7. Invariants

- No inventar resultados numericos ni veredictos.
- Mantener `Resultado: Pendiente de ejecucion`.
- No exponer PII ni RUDE real; solo datos sinteticos.
- Criterio de exito: 0 leaks cross-tenant y p95 INSERT/SELECT < 505 ms.

## 8. Failure Modes

- `E_MISSING_TEMPLATE`: falta `plantillas/POC_TEMPLATE.md` — STOP, solicitar.
- `E_MISSING_DTI_SECTION`: falta `docs/DTI.md §12.1` — STOP, no crear POC.
- `E_METRICS_INVENTED`: aparece una metrica real sin evidencia — remover y dejar pendiente.

## 9. Metrics

- Tokens estimados: ~2 000 entrada / ~5 500 salida.
- Antes: POC-01 solo definida en `docs/DTI.md §12.1`.
- Despues: carpeta `docs/pocs/POC-01-rls-multitenancy/` lista para ejecucion documental.
