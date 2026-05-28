# Architecture Decision Record (ADR)

## ADR-0005: Resiliencia en integración con el SIE mediante Resilience4j y estado por registro

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0005` |
| Título | Resiliencia en integración con el SIE mediante Resilience4j y estado por registro |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Adaptador de exportación al SIE — UC-04 (Exportación y sincronización masiva al SIE) |
| Stakeholders consultados | Secretaría (Wendy), Directores, Equipo de arquitectura G-EduSync |

### 1. Contexto

El SIE (Sistema de Información Educativa) del Ministerio de Educación de Bolivia es un servidor externo con disponibilidad limitada y capacidad de procesamiento que se satura durante los períodos de cierre trimestral, cuando todos los colegios del país intentan enviar sus calificaciones simultáneamente. Históricamente, un fallo del SIE a mitad de una exportación masiva (ej. al enviar el registro del estudiante 47 de 80) obliga al personal administrativo a reiniciar el proceso desde cero, con el riesgo de duplicar registros ya enviados y de tener que trabajar de madrugada.

El SIE no ofrece una API con garantías de idempotencia documentadas: si el mismo registro se envía dos veces, puede generar duplicados en el sistema ministerial. Tampoco confirma explícitamente qué registros recibió antes de fallar.

El KPI-01 del BRD define que EduSync debe eliminar el trabajo nocturno de cierre trimestral. Sin resiliencia adecuada en la integración con el SIE, este KPI es inalcanzable.

Las fuerzas en tensión son: **reanudación sin duplicados** vs. **complejidad del estado de exportación** vs. **confiabilidad ante un SIE no idempotente**.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. Exportación atómica completa (todo o nada, reintento desde cero) | Simple de implementar; sin estado adicional en BD | Un fallo en el registro 47/80 obliga a reenviar los 46 previos (riesgo de duplicado en SIE); el personal debe estar presente para reiniciar; no elimina el trabajo de madrugada | Cero — sin estado adicional |
| B. Estado de exportación persistido registro a registro con idempotencia por `rude + periodo_id` | Reanuda exactamente desde el último registro fallido; cero duplicados garantizados; Resilience4j gestiona circuit breaker y reintentos; el personal no necesita estar presente | Requiere tabla de estado de exportación en BD; complejidad en el scheduler de reintentos | Bajo — tabla adicional + Resilience4j (dependencia Maven) |
| C. Exportación en lotes con checkpoint en BD (ej. lotes de 10 estudiantes) | Mejor granularidad que A; menos registros a reenviar ante fallo | Mayor complejidad que B; el SIE puede aceptar el lote parcialmente sin confirmarlo; la lógica de checkpoint es más difícil de validar | Medio — más complejidad que B sin ventajas adicionales para el caso de uso |

### 3. Decisión

> **Elegimos la Alternativa B: estado de exportación persistido registro a registro, con clave de idempotencia compuesta por `rude + periodo_id`, circuit breaker y reintentos asíncronos mediante Resilience4j.**

La tabla `exportacion_sie_estado` registra el estado (`PENDIENTE`, `ENVIADO`, `FALLIDO`) de cada estudiante individual. Antes de enviar cualquier registro, EduSync verifica si ya está en estado `ENVIADO`; si lo está, lo omite sin reenviar. Un scheduler asíncrono reprocesa únicamente los registros en estado `FALLIDO` o `PENDIENTE` cada 5 minutos, hasta que todos los registros del periodo estén en `ENVIADO`.

Resilience4j gestiona el circuit breaker sobre el cliente HTTP del SIE: si el SIE devuelve 5xx consecutivos, el circuito se abre y EduSync deja de intentar hasta que el SIE se recupere, evitando saturar aún más al servidor ministerial.

### 4. Consecuencias

#### 4.1 Positivas

- Elimina el trabajo de madrugada ante fallos del SIE (KPI-01 del BRD): si el SIE falla al procesar el estudiante 47 de 80, EduSync reanuda desde el estudiante 48 en el siguiente ciclo, sin riesgo de duplicar los 46 ya enviados.
- La idempotencia por `rude + periodo_id` protege contra el reenvío accidental en un entorno donde el SIE no confirma duplicados.
- El circuit breaker de Resilience4j protege al SIE de ser saturado por reintentos agresivos de EduSync durante una caída ministerial.
- El estado de exportación es visible en tiempo real en el dashboard de secretaría (verde "Validado" / amarillo "Parcial" / rojo "Error").

#### 4.2 Negativas / costos

- La tabla `exportacion_sie_estado` añade complejidad al modelo de datos; requiere política RLS (ADR-0001) y migración Flyway.
- El scheduler de reintentos consume recursos de JVM incluso cuando no hay exportaciones en curso; requiere configuración de `cron` cuidadosa para evitar interferir con las ventanas de pico.
- Si el SIE acepta un registro pero responde con un error de red (no 2xx), EduSync lo marca como `FALLIDO` y lo reenvía; existe un margen de duplicado teórico en este edge case que debe documentarse y monitorizarse.
- La integración con un SIE sin API documentada requiere ingeniería inversa del protocolo de exportación actual (Excel → SIE).

#### 4.3 Neutras / observables

- El filtro pre-exportación obligatorio descarta automáticamente toda fila con RUDE nulo/inválido o nota nula, y las reporta como `EXCLUIDAS_SIN_RUDE` o `EXCLUIDAS_NOTA_INCOMPLETA` en el reporte de resultado.
- La configuración del circuit breaker de Resilience4j (umbral de apertura, tiempo de espera, tasa de fallos) se almacena en `parametro_academico` (ADR-0002) para ser ajustable sin redespliegue.
- Los logs de cada intento de exportación se escriben en `audit_log` (ADR-0003) con el estado del SIE y el código de error recibido.

### 5. Impacto en el sistema

- **Código**: puerto de salida `SIEExportPort` en `bo.edusync.domain.port.out`; adaptador `SIEHttpClientAdapter` en `bo.edusync.infrastructure.adapter.out.sie` con anotación `@CircuitBreaker(name = "sie")` de Resilience4j; entidad `ExportacionSIEEstado` en `bo.edusync.domain.model.exportacion`; scheduler `SIERetryScheduler` en `bo.edusync.infrastructure.adapter.in.scheduler`. Afecta directamente UC-04.
- **Operaciones**: migración Flyway V005 crea `exportacion_sie_estado` con políticas RLS; configuración `resilience4j.circuitbreaker.instances.sie.*` en `application.yml`; alarma CloudWatch si la tasa de fallos del SIE supera el 20 % en 5 minutos.
- **Seguridad**: el RUDE nunca aparece en logs ni en rutas URL del adaptador SIE (NFR-004 + NFR-007); solo en el payload cifrado del cuerpo de la petición al SIE.
- **Equipo**: el adaptador SIE requiere un stub WireMock en tests de integración para simular fallos parciales del SIE.
- **Costo**: Resilience4j es una dependencia Maven sin costo de licencia; sin impacto adicional en la factura AWS en v1.0.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si el scheduler de reintentos produce duplicados en el SIE en más del 0.1 % de los registros (tasa detectada por el Ministerio), o si la tabla `exportacion_sie_estado` supera 1 GB en un año de operación.
- **Costo estimado de revertir**: 1 semana para reemplazar el estado por registro con lotes con checkpoint (Alternativa C); la clave de idempotencia y el circuit breaker se mantienen.
- **Plan B**: si el SIE implementa una API REST con idempotencia nativa, eliminar la tabla `exportacion_sie_estado` y delegar la idempotencia al SIE; el adaptador `SIEHttpClientAdapter` absorbería el cambio sin afectar el dominio.

### 7. Validación

- **Golden test `SIEPayloadTest`**: `SIEPayloadTest.payload_uses_rude_only()` — verifica que el payload construido por `SIEHttpClientAdapter` para cada estudiante usa exclusivamente el RUDE como clave de identidad y no contiene nombre, apellido ni número de lista (NFR-004). Bloquea merge a `release/*` si falla.
- **Test de reanudación sin duplicados `SIERetryIT`**: simula con WireMock un SIE que acepta los primeros 10 registros, devuelve 503 en el 11.º y luego se recupera; verifica que EduSync reanuda desde el 11.º sin reenviar los 10 primeros.
- **Test de circuit breaker**: `SIECircuitBreakerTest` verifica que tras 5 errores consecutivos del SIE, el circuito se abre y EduSync deja de intentar durante el período de espera configurado.
- **Métrica**: tasa de éxito de exportación (registros `ENVIADO` / total registros `no EXCLUIDOS`) ≥ 99 % por periodo, medida en CloudWatch.

### 8. Referencias

- `FSD-UC-04` (Exportación y sincronización masiva al SIE — el flujo completo de exportación con idempotencia y reintentos).
- `BR-004` (La vinculación al SIE es exclusivamente por RUDE; nunca por nombre, apellido ni posición visual).
- `BR-006` (Los fallos parciales del SIE no reinician el proceso; el estado por registro permite reanudar).
- `NFR-004` (El RUDE es la única clave de identidad estudiantil en todas las operaciones de escritura).
- `NFR-007` (Cifrado PII: el RUDE viaja cifrado con KMS en reposo; en tránsito solo en el cuerpo HTTPS).
- `DA-05` en `docs/arquitectura_funcional_EduSync.md`.
- Referencia al escenario crítico en `02_parte_dificil.md` del repositorio.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado a partir de DA-05 en arquitectura_funcional_EduSync.md; estado Aceptada |
