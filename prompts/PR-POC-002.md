# PR-POC-002 — Estructura documental para POC-02 Circuit Breaker SIE

## 1. Role

Actua como `docs-agent` de EduSync, responsable de documentacion tecnica y trazabilidad de POCs criticas para la defensa final.

## 2. Task

Crear la estructura documental inicial para ejecutar la POC-02 de Circuit Breaker SIE con Resilience4j y WireMock, sin inventar metricas ni marcar la POC como ejecutada.

## 3. Context

- Documentos fuente: `plantillas/POC_TEMPLATE.md`, `docs/DTI.md §12.2`, `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md`, `AGENTS.md`.
- Artefactos a producir:
  - `docs/pocs/POC-02-circuit-breaker-sie/README.md`
  - `docs/pocs/POC-02-circuit-breaker-sie/runbook.md`
  - `docs/pocs/POC-02-circuit-breaker-sie/evidencia/README.md`
- Restricciones de dominio: DA-05, ADR-0005, NFR-011, NFR-012, IG-01.
- Stack: Java 21, Spring Boot 3.3, Resilience4j, WireMock, PostgreSQL 15.

## 4. Reasoning

1. Leer `plantillas/POC_TEMPLATE.md` y conservar sus secciones 0-14.
2. Extraer de `docs/DTI.md §12.2` el riesgo, hipotesis, criterio de exito y alcance.
3. Cruzar la decision con ADR-0005 para mantener idempotencia y retry seguro.
4. Crear README, runbook y evidencia/README con resultados en estado `Pendiente de ejecucion`.

## 5. Stop Condition

Detente cuando existan los 3 archivos de POC-02, todos referencien `docs/DTI.md §12.2` y ADR-0005, y ninguna seccion contenga metricas ejecutadas.

## 6. Output

Markdown en `docs/pocs/POC-02-circuit-breaker-sie/` listo para ejecutar la POC:

- `README.md`
- `runbook.md`
- `evidencia/README.md`

## 7. Invariants

- No inventar resultados numericos ni veredictos.
- Mantener `Resultado: Pendiente de ejecucion`.
- No exponer RUDE real ni payloads SIE con PII.
- Criterio de exito: CB abre con 60 % de timeout/falla, recovery < 15 min y 0 duplicados.

## 8. Failure Modes

- `E_MISSING_TEMPLATE`: falta `plantillas/POC_TEMPLATE.md` — STOP, solicitar.
- `E_MISSING_DTI_SECTION`: falta `docs/DTI.md §12.2` — STOP, no crear POC.
- `E_METRICS_INVENTED`: aparece una metrica real sin evidencia — remover y dejar pendiente.

## 9. Metrics

- Tokens estimados: ~2 000 entrada / ~5 500 salida.
- Antes: POC-02 solo definida en `docs/DTI.md §12.2`.
- Despues: carpeta `docs/pocs/POC-02-circuit-breaker-sie/` lista para ejecucion documental.
