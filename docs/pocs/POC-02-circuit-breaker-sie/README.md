# Prueba de Concepto (POC) — POC-02

> **Estado**: Propuesta · **Resultado**: Pendiente de ejecución
> Documento base: `plantillas/POC_TEMPLATE.md`
> Trazabilidad: `docs/DTI.md §12.2` · `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` · `AGENTS.md §1`

---

## POC-02: Circuit Breaker SIE con Resilience4j

### 0. Metadatos

| Campo | Valor |
|-------|-------|
| ID | `POC-02` |
| Título | Circuit Breaker SIE con Resilience4j |
| Grupo | `G-EduSync` |
| Responsable(s) | Rodrigo Aspeti |
| Fecha de inicio | Pendiente |
| Fecha objetivo de cierre | Pendiente (cronograma de 2 dias segun `docs/DTI.md §12.2`) |
| Estado | Propuesta |
| ADR relacionado | `ADR-0005` (Resiliencia en integracion con el SIE mediante Resilience4j) |

### 1. Riesgo que mitiga

Caida o latencia inaceptable del SIE (Sistema de Informacion Educativa del Ministerio) durante el cierre trimestral, cuando todos los colegios del pais intentan exportar simultaneamente. Ver `ADR-0005 §1`. El riesgo concreto:

- Una exportacion masiva (ej. 80 estudiantes) falla en el registro 47/80.
- Sin idempotencia ni reanudacion, el personal debe reenviar los 80 con riesgo de duplicado.
- El SIE no devuelve garantia de idempotencia documentada.
- El equipo administrativo termina trabajando de madrugada (rompe KPI-01 del BRD).

### 2. Hipótesis

> *Creemos que `Resilience4j` con `failureRateThreshold = 50%`, `timeout = 30s`, retry con backoff exponencial, combinado con `SIERetryScheduler` cada 5 minutos y clave de idempotencia `(rude, periodo_id)`, garantiza que ninguna exportacion quede en estado inconsistente mas de 15 minutos y que no se generen duplicados en el SIE.*

### 3. Criterio de éxito medible (SMART)

- **Circuit breaker (obligatorio)**: en 100 llamadas simuladas con 60 % timeout/5xx, el `CircuitBreaker` transita a estado `OPEN` correctamente segun la ventana deslizante.
- **Recuperacion (obligatorio)**: 100 % de exportaciones marcadas `PENDIENTE` se reprocesan exitosamente en < 15 minutos despues de que el SIE vuelve a estar disponible.
- **Idempotencia (obligatorio)**: 0 registros duplicados en el SIE simulado por par `(rude, periodo_id)` despues del flujo completo.
- **Test golden (obligatorio)**: `CircuitBreakerTest` y `SIEPayloadTest.payload_uses_rude_only` pasan al 100 %.
- **Umbral de fracaso (obligatorio)**: si el circuit breaker no abre en 100 llamadas con 60 % de falla, o si se detecta >= 1 duplicado en SIE, o si la recuperacion supera 20 minutos → POC fallida y `ADR-0005` debe revisarse.

### 4. Alcance reducido (time-boxed)

- **Funcionalidades incluidas**:
  - `SIEHttpClient` con anotacion `@CircuitBreaker(name="sie")` y `@Retry(name="sie")`.
  - `SIERetryScheduler` con `@Scheduled` cada 60 s (acelerado para la POC; produccion = 5 min).
  - Tabla `exportacion_sie_estado` con clave `(rude, periodo_id)` y estados `PENDIENTE / ENVIADO / FALLIDO`.
  - WireMock para simular SIE con escenarios: 60 % timeout / 5xx, 40 % 200 OK.
  - 100 llamadas controladas.
- **Funcionalidades excluidas**:
  - Integracion real con el SIE ministerial.
  - Encriptacion del payload con KMS (NFR-007).
  - Frontend Angular ni dashboard de secretaria.
  - Multi-tenant real (la POC corre con 1 tenant sintetico).
- **Duración máxima**: 2 dias. Si se excede, se cierra y se documenta lo aprendido en §11.

### 5. Diseño de la prueba

#### 5.1 Stack usado

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 3.3.x |
| Resiliencia | Resilience4j (Spring Boot Starter) | 2.x |
| Mock HTTP | WireMock | 3.x |
| Cliente HTTP | Spring `RestClient` | 6.x |
| Base de datos | PostgreSQL | 15 (Testcontainers) |
| Test runner | JUnit | 5 |

#### 5.2 Arquitectura de la POC

```mermaid
flowchart LR
  T1[Test Runner JUnit 5] --> APP[Spring Boot mini-app]
  APP --> CLIENT[SIEHttpClient\n@CircuitBreaker @Retry]
  CLIENT --> WM[WireMock SIE Mock\n60 percent fail / 40 percent ok]
  APP --> SCHED[SIERetryScheduler\n@Scheduled fixedDelay 60s]
  SCHED --> PG[(PostgreSQL 15\nexportacion_sie_estado)]
  CLIENT --> PG
  APP --> ACT[Spring Boot Actuator\n/actuator/circuitbreakers]
  ACT --> METRICS[evidencia/metrics.csv\nevidencia/circuit-breaker-state.txt]
```

#### 5.3 Datos de prueba

- **Origen**: payloads sinteticos generados en el setup del test.
- **Volumen**: 100 estudiantes sinteticos con RUDE valido y `promedio_final` entero.
- **Tenants**: 1 tenant sintetico (`11111111-1111-1111-1111-111111111111`).
- **Sesgos conocidos**: WireMock no reproduce la API real del SIE; los timeouts son deterministas (`fixedDelay`), no aleatorios.

#### 5.4 Procedimiento experimental

1. Levantar WireMock con 5 stubs: 3 con `withStatus(503)`, 2 con `withFixedDelay(35000)`, 1 con `withStatus(200)`.
2. Configurar la rotacion de stubs (60 % fallidos / 40 % exitosos) via scenarios de WireMock.
3. Configurar `application.yml` de la POC con los parametros declarados en `ADR-0005 §3` y en `docs/DTI.md §6.1`.
4. Ejecutar 100 llamadas secuenciales desde el test.
5. Capturar el estado del `CircuitBreaker` via `/actuator/circuitbreakers` despues de cada llamada.
6. Despues de la llamada 100, cambiar WireMock para responder siempre 200 OK.
7. Esperar a que `SIERetryScheduler` reprocese todas las `PENDIENTE` y registre el tiempo total.
8. Verificar que la tabla `exportacion_sie_estado` no tiene duplicados por `(rude, periodo_id)`.

### 6. Entorno

- **Ejecución**: local con Docker Desktop (WireMock + PostgreSQL en contenedores).
- **Recursos minimos**: 4 CPU, 6 GB RAM, 3 GB disco.
- **Instancia AWS**: N/A (POC local).
- **Costo estimado**: 0 USD.

### 7. Herramientas de medición

- JUnit 5 para orquestar las 100 llamadas y la verificacion final.
- WireMock standalone (contenedor o embedded) para simular SIE.
- Spring Boot Actuator (`/actuator/circuitbreakers`, `/actuator/retries`, `/actuator/metrics`).
- Micrometer + simple meter registry para exportar metricas a CSV.
- `psql` para verificar duplicados e idempotencia.

### 8. Plan de ejecución

| Día | Actividad | Responsable |
|-----|-----------|-------------|
| 1 | Setup proyecto Maven, configurar Resilience4j y WireMock, schema `exportacion_sie_estado` | Rodrigo Aspeti |
| 2 | Implementar 100 llamadas + scheduler, ejecutar 3 corridas, llenar `§9`/`§10` | Rodrigo Aspeti |

### 9. Resultados

> Completar **al finalizar** la POC. Hoy: Pendiente de ejecución.

#### 9.1 Tabla de métricas

| Métrica | Valor obtenido | Umbral éxito | Veredicto |
|---------|----------------|--------------|-----------|
| Circuit breaker abre con 60 % failure | Pendiente | si | Pendiente |
| Tiempo total hasta recuperacion completa | Pendiente | < 15 min | Pendiente |
| Duplicados por `(rude, periodo_id)` | Pendiente | = 0 | Pendiente |
| `CircuitBreakerTest` pasa | Pendiente | 100 % | Pendiente |
| `SIEPayloadTest.payload_uses_rude_only` pasa | Pendiente | 100 % | Pendiente |

#### 9.2 Gráficos / capturas

- Pendiente. Ver `evidencia/` cuando este disponible.

### 10. Conclusiones y veredicto

> Pendiente de ejecución. No completar antes de tener métricas reales.

### 11. Aprendizajes (lessons learned)

> Pendiente de ejecución.

### 12. Riesgos remanentes

- WireMock no reproduce el comportamiento real del SIE ante latencias > 30 s, payloads malformados o renegociacion de TLS.
- La POC no prueba el escenario en el que el SIE acepta el payload pero la respuesta HTTP se pierde (edge case de duplicado teorico, ver `ADR-0005 §4.2`).
- No prueba la interaccion con KMS para encriptacion del payload en transito.
- No prueba degradacion del scheduler bajo carga concurrente de varios tenants.

### 13. Referencias

- `docs/DTI.md §12.2` — definicion original de la POC.
- `docs/DTI.md §6.1` — tabla de patrones de resiliencia.
- `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` — decision aceptada.
- `docs/arquitectura_funcional_EduSync.md` DA-05.
- `AGENTS.md §6` — golden tests obligatorios (`SIEPayloadTest`).
- Resilience4j Spring Boot 3 reference: https://resilience4j.readme.io/docs/getting-started-3
- WireMock standalone: https://wiremock.org/docs/standalone/

### 14. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1 | 28/05/2026 | Rodrigo Aspeti | creacion del documento base (secciones 0-8) |

---

## Checklist de cierre de POC

- [x] Hipótesis y criterio de éxito declarados antes de ejecutar.
- [x] Alcance time-boxed respetado (2 dias).
- [ ] Resultados numéricos con evidencia en `evidencia/`.
- [ ] Veredicto explícito (✅ / ⚠️ / ❌).
- [ ] Aprendizajes capturados.
- [ ] ADR-0005 actualizado si la POC cambia la decision.
