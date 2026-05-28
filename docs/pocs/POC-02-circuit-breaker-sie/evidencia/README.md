# Evidencias — POC-02 Circuit Breaker SIE con Resilience4j

> Indice de archivos de evidencia que debe producir la ejecucion de la POC.
> Trazabilidad: `pocs/POC-02-circuit-breaker-sie/README.md §9` · `runbook.md` paso 6.
> Estado global: **Pendiente de ejecución**. Ningun archivo de la lista existe todavia.

---

## Evidencias obligatorias

| Archivo | Contenido esperado | Criterio de aceptacion | Estado |
|---------|--------------------|------------------------|--------|
| `metrics.csv` | Una fila por llamada: `call_n,status_code,latency_ms,cb_state,retry_count,timestamp` | 100 filas; suma de no-2xx >= 60 | Pendiente |
| `circuit-breaker-state.txt` | Secuencia de transiciones del CB capturadas durante las 100 llamadas (`CLOSED → OPEN → HALF_OPEN → CLOSED`) | Aparece al menos un `OPEN` y termina en `CLOSED` | Pendiente |
| `wiremock-requests.log` | Log de WireMock con las requests recibidas | 100+ entradas correlacionables con `metrics.csv` | Pendiente |
| `recovery-time.txt` | Numero de minutos desde el ultimo fallo hasta `PENDIENTE = 0` | `< 15` | Pendiente |

## Evidencias opcionales (refuerzan defensa)

| Archivo | Contenido sugerido | Estado |
|---------|--------------------|--------|
| `actuator-snapshot-OPEN.json` | Salida de `/actuator/circuitbreakers` en el momento en que el CB esta `OPEN` | Pendiente |
| `actuator-snapshot-RECOVERED.json` | Salida del mismo endpoint cuando el CB vuelve a `CLOSED` | Pendiente |
| `idempotency-check.sql.out` | Resultado de la query de duplicados del runbook paso 6 (debe devolver 0 filas) | Pendiente |
| `screenshots/` | Capturas de `/actuator/circuitbreakers`, log de WireMock, log del `SIERetryScheduler` | Pendiente |
| `run-N.log` (N=1,2,3) | Log completo de cada corrida para reproducibilidad | Pendiente |
| `cb-state-timeline.png` | Grafico de transiciones del CB en el tiempo derivado del CSV | Pendiente |

---

## Reglas

- **No subir PII real**. El RUDE en los payloads de WireMock es sintetico (`RUDE-001` ... `RUDE-100`).
- **No editar las metricas a mano**. El test genera `metrics.csv` y `circuit-breaker-state.txt`; los resumenes derivados deben recalcularse desde el CSV.
- **Conservar logs de fallo**. Si una corrida termina sin que el CB abra, guardar su `run-N.log` con el detalle.
- **Verificar idempotencia siempre**. La query final del paso 6 debe correrse despues de cada corrida.

---

## Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1 | 28/05/2026 | Rodrigo Aspeti | indice de evidencias creado (todas pendientes) |
