# Evidencias — POC-01 RLS Multitenancy

> Indice de archivos de evidencia que debe producir la ejecucion de la POC.
> Trazabilidad: `pocs/POC-01-rls-multitenancy/README.md §9` · `runbook.md` paso 6.
> Estado global: **Pendiente de ejecución**. Ningun archivo de la lista existe todavia.

---

## Evidencias obligatorias

| Archivo | Contenido esperado | Criterio de aceptacion | Estado |
|---------|--------------------|------------------------|--------|
| `metrics.csv` | Una fila por operacion: `operation,tenant_id,latency_ms,timestamp` | 1000 filas, ambas tenants representadas | Pendiente |
| `test-output.txt` | Stdout completo del `mvn test` de `MultitenantTest` | Incluye linea `Verdict: PASS` | Pendiente |
| `p95-latency.txt` | Un solo valor numerico en milisegundos | `< 505` | Pendiente |
| `cross-tenant-leak-count.txt` | Un solo numero entero | `= 0` | Pendiente |

## Evidencias opcionales (refuerzan defensa)

| Archivo | Contenido sugerido | Estado |
|---------|--------------------|--------|
| `pg-rls-check.sql.out` | Output de `SELECT relname, relrowsecurity FROM pg_class WHERE relname IN ('calificacion','centralizador');` mostrando `relrowsecurity = t` | Pendiente |
| `pg-policies-check.sql.out` | Output de `SELECT * FROM pg_policies WHERE tablename IN ('calificacion','centralizador');` | Pendiente |
| `cross-tenant-verify.sql.out` | Output del `SELECT COUNT(*)` cruzado por tenant del runbook paso 5 | Pendiente |
| `screenshots/` | Capturas del log de Testcontainers, IntelliJ ejecutando el test, terminal con `mvn test` | Pendiente |
| `histogram.png` | Histograma de latencias (HdrHistogram → PNG) | Pendiente |
| `run-N.log` (N=1,2,3) | Log completo de cada una de las 3 corridas para reproducibilidad | Pendiente |

---

## Reglas

- **No subir PII real**. Los `tenant_id` y `rude` aqui son sinteticos (UUIDs fijos del runbook).
- **No editar las metricas a mano**. El script de la POC genera `metrics.csv`; los resumenes (`p95-latency.txt`, `cross-tenant-leak-count.txt`) deben derivarse del CSV.
- Si una corrida falla, conservar su `run-N.log` con el modo de fallo en lugar de borrarlo.

---

## Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1 | 28/05/2026 | Rodrigo Aspeti | indice de evidencias creado (todas pendientes) |
