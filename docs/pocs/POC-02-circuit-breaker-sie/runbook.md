# Runbook — POC-02 Circuit Breaker SIE con Resilience4j

> Pasos repetibles para ejecutar la POC declarada en `README.md`.
> Trazabilidad: `docs/DTI.md §12.2` · `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md`.
> Todos los comandos son **placeholders**: el proyecto Java/Maven todavia no existe.

---

## Pre-requisitos

- Docker Desktop instalado y corriendo.
- JDK 21 disponible (`java -version`).
- Maven 3.9+ disponible (`mvn -v`).
- 4 CPU + 6 GB RAM libres.

---

## Paso 1 — Levantar servicio mock SIE con WireMock

Opcion A — contenedor independiente:

```bash
docker run --name edusync-poc02-wiremock \
  -p 8089:8080 \
  -v "$(pwd)/wiremock":/home/wiremock \
  -d wiremock/wiremock:3.5.0
```

Stubs minimos esperados en `wiremock/mappings/`:

- `sie-503.json` — `POST /registro/{rude}` → 503.
- `sie-timeout.json` — `POST /registro/{rude}` → 200 con `withFixedDelay(35000)`.
- `sie-ok.json` — `POST /registro/{rude}` → 200 OK.

Configurar `scenarios` de WireMock para rotar las respuestas con frecuencia 60 % falla / 40 % exito.

Opcion B — embedded en el test (preferida para CI).

Verificacion:

```bash
curl -X POST "http://localhost:8089/registro/RUDE-001" -H "Content-Type: application/json" -d '{}'
```

---

## Paso 2 — Configurar Resilience4j

Snippet `application.yml` (placeholder; ajustar contra `ADR-0005 §5`):

```yaml
resilience4j:
  circuitbreaker:
    instances:
      sie:
        failureRateThreshold: 50
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 20
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 30s
        registerHealthIndicator: true
  retry:
    instances:
      sie:
        maxAttempts: 3
        waitDuration: 2s
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException

spring:
  cloud:
    openfeign:
      client:
        config:
          sie:
            connectTimeout: 5000
            readTimeout: 30000
```

Comprobar:

```bash
curl http://localhost:8080/actuator/circuitbreakers | jq
```

---

## Paso 3 — Simular 100 llamadas con 60 % timeout/falla

Codigo Java de la POC todavia no existe (placeholder de comando):

```bash
mvn -pl pocs/POC-02-circuit-breaker-sie -am clean test \
  -Dtest=CircuitBreakerTest \
  -Dpoc.calls=100 \
  -Dpoc.failureRate=0.60 \
  -Dpoc.tenant=11111111-1111-1111-1111-111111111111 \
  -Dpoc.wiremock=http://localhost:8089
```

Notas:

- Las 100 llamadas son secuenciales (1 estudiante por iteracion).
- WireMock retorna 60 % de respuestas no-OK; el test debe registrar en `exportacion_sie_estado` el resultado.
- El test debe loggear el estado del CB en cada llamada para `circuit-breaker-state.txt`.

---

## Paso 4 — Verificar estado `OPEN` del circuit breaker

```bash
curl http://localhost:8080/actuator/circuitbreakers \
  | jq '.circuitBreakers.sie'
```

Salida esperada (algun snapshot durante el experimento):

```json
{
  "state": "OPEN",
  "failureRate": ">50.0%",
  "bufferedCalls": 20,
  "failedCalls": ">10"
}
```

Si nunca aparece `OPEN`, la POC falla por configuracion incorrecta del `slidingWindowSize` o del umbral.

---

## Paso 5 — Ejecutar recuperación de exportaciones `PENDIENTE`

Cambiar WireMock a modo 100 % OK:

```bash
curl -X POST "http://localhost:8089/__admin/mappings/reset"
curl -X POST "http://localhost:8089/__admin/mappings/import" \
  -H "Content-Type: application/json" \
  -d @wiremock/mappings/sie-ok-only.json
```

Esperar a que `SIERetryScheduler` (`@Scheduled(fixedDelay = 60000)` en la POC) procese todas las filas `PENDIENTE`:

```bash
psql "postgresql://edusync:edusync@localhost:5432/poc02" -c \
  "SELECT estado, count(*) FROM exportacion_sie_estado GROUP BY estado;"
```

Criterio de exito: `ENVIADO = 100`, `PENDIENTE = 0`, `FALLIDO = 0` en < 15 min reloj de pared.

---

## Paso 6 — Capturar métricas y evidencias

Archivos minimos a producir (ver `evidencia/README.md`):

- `metrics.csv`
- `circuit-breaker-state.txt`
- `wiremock-requests.log`
- `recovery-time.txt`
- (opcional) `actuator-snapshot-OPEN.json`
- (opcional) `actuator-snapshot-RECOVERED.json`
- (opcional) capturas del log del scheduler.

Verificacion final de idempotencia:

```sql
SELECT rude, periodo_id, COUNT(*) AS envios
FROM exportacion_sie_estado
GROUP BY rude, periodo_id
HAVING COUNT(*) > 1;
-- Debe devolver 0 filas.
```

---

## Plan de reversión

Si la POC falla (CB no abre, duplicados, recuperacion > 20 min):

1. Documentar modo de fallo en `README.md §10` y `§11`.
2. Abrir issue en el repo para revisar `ADR-0005 §6 Plan de reversion`.
3. Bloquear merge a `release/2.0.0` hasta resolver.

---

## Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1 | 28/05/2026 | Rodrigo Aspeti | runbook inicial con 6 pasos placeholder |
