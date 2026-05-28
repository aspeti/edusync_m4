# Architecture Decision Record (ADR)

## ADR-0004: Consolidación asíncrona de centralizadores mediante Spring Events

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0004` |
| Título | Consolidación asíncrona de centralizadores mediante Spring Events |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Motor de consolidación — UC-02 (cierre de materia) y UC-03 (consolidación) |
| Stakeholders consultados | Docentes (Marcela), Secretaría (Wendy), Equipo de arquitectura G-EduSync |

### 1. Contexto

Al cerrar la última materia de un curso (UC-02), EduSync debe calcular el centralizador trimestral (UC-03): promediar todas las calificaciones de todos los estudiantes de ese curso, aplicar la regla de truncado `floor()`, determinar si el resultado es `PROVISIONAL` u `OFICIAL`, y —si los tres trimestres están cerrados— calcular el promedio anual.

Este cálculo puede ser computacionalmente costoso en colegios grandes (hasta 1.000 estudiantes × múltiples dimensiones × múltiples materias). Ejecutarlo de forma síncrona dentro de la misma transacción del cierre de materia impone una espera al Docente (Marcela) que puede durar varios segundos, degradando la experiencia de usuario en el momento de mayor carga (los cierres trimestrales de Bolivia ocurren todos los colegios en el mismo período, saturando el servidor).

Las fuerzas en tensión son: **experiencia de usuario del Docente** (respuesta inmediata al cerrar) vs. **consistencia** (el centralizador provisional debe estar disponible sin demora visible) vs. **complejidad operativa** del equipo de uno.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. Consolidación síncrona (misma transacción del cierre) | El docente ve el centralizador inmediatamente; consistencia fuerte; sin infraestructura adicional | El tiempo de respuesta de UC-02 depende del tiempo de cálculo de UC-03; en colegios grandes puede superar el umbral NFR-001 (500 ms p95); riesgo de timeout en picos de cierre masivo trimestral | Cero — sin cambios de infraestructura |
| B. Asíncrona mediante Spring Events internos (`@TransactionalEventListener`) | Respuesta inmediata al Docente; desacoplamiento entre cierre y cálculo; sin infraestructura externa; diseño migrable a SQS sin cambiar el dominio | Consistencia eventual (el centralizador tarda 1–5 segundos en actualizarse tras el cierre); el thread del cálculo comparte JVM con el de la aplicación web | Bajo — solo configuración de Spring Events en la misma JVM |
| C. Asíncrona mediante AWS SQS FIFO + consumer dedicado (worker service) | Máxima desacoplamiento; el worker puede escalar independientemente; reintentos nativos de SQS | Alta complejidad operativa para un equipo de uno; requiere un servicio adicional en ECS Fargate; overhead de latencia de SQS (10–30 ms por mensaje) en la fase actual | Alto — segundo contenedor ECS + cola SQS FIFO + Dead Letter Queue |

### 3. Decisión

> **Elegimos la Alternativa B: consolidación asíncrona mediante Spring Events internos con diseño orientado a eventos que permite migrar a AWS SQS sin cambiar la interfaz del dominio.**

Al cerrar una materia (UC-02), la capa de aplicación publica un evento de dominio `MateriaCerradaEvent` mediante `DomainEventPublisher`. Un `@TransactionalEventListener(phase = AFTER_COMMIT)` en el adaptador de consolidación recibe el evento e inicia el cálculo en un thread separado del pool de Spring. El Docente recibe respuesta HTTP 200 inmediatamente al cerrar su materia, sin esperar el cálculo.

El diseño es migrable: `DomainEventPublisher` es un puerto de salida del dominio; su implementación actual usa Spring Events internos, pero puede reemplazarse por una implementación SQS sin cambiar ninguna línea de código del dominio ni de la aplicación.

### 4. Consecuencias

#### 4.1 Positivas

- El Docente recibe respuesta inmediata al cerrar su materia (UC-02 dentro de NFR-001: p95 < 500 ms) sin esperar el cálculo del centralizador.
- El dominio es agnóstico al mecanismo de transporte: `ConsolidacionUseCase` recibe un `ConsolidarCentralizadorCommand` sin saber si vino de Spring Events o de SQS.
- Para el volumen del mercado boliviano (hasta 1.000 estudiantes por colegio), Spring Events son suficientes y eliminan la complejidad operativa de SQS en la versión 1.0.
- La migración a SQS en v1.1 es un cambio de infraestructura + adaptador, sin tocar el dominio.

#### 4.2 Negativas / costos

- Consistencia eventual: tras el cierre de materia, el centralizador tarda 1–5 segundos en actualizarse. El dashboard de secretaría muestra el estado `CALCULANDO` durante ese intervalo.
- El thread de consolidación comparte JVM con el de la aplicación web; un cálculo muy pesado puede impactar la latencia de otras peticiones si el pool de threads está saturado.
- Si la JVM falla durante el cálculo (ej. OOM), el evento se pierde y el centralizador queda en estado `PROVISIONAL` indefinidamente; se requiere un scheduler de recuperación que detecte centralizadores huérfanos.
- Los tests de integración de UC-03 deben incluir `@DirtiesContext` o esperas explícitas (`Awaitility`) para verificar el resultado asíncrono.

#### 4.3 Neutras / observables

- El evento `MateriaCerradaEvent` incluye: `tenantId`, `materiaId`, `cursoId`, `periodoId`, `occurredAt`.
- El scheduler `CentralizadorHuerfanoScheduler` ejecuta cada 15 minutos en producción para detectar y relanzar consolidaciones que fallaron silenciosamente.
- En v1.1, la migración a SQS FIFO requiere añadir el `periodoId` como `MessageGroupId` para garantizar orden de procesamiento por periodo.

### 5. Impacto en el sistema

- **Código**: evento `MateriaCerradaEvent` en `bo.edusync.domain.model.consolidacion.event`; puerto `DomainEventPublisher` en `bo.edusync.domain.port.out`; adaptador `SpringEventPublisher` en `bo.edusync.infrastructure.adapter.out.events`; listener `MateriaCerradaEventListener` anotado con `@TransactionalEventListener(phase = AFTER_COMMIT)` en `bo.edusync.infrastructure.adapter.in.events`. Afecta directamente UC-02 y UC-03.
- **Operaciones**: configuración del pool de threads asíncronos en `AsyncConfig` de Spring Boot; alertas CloudWatch si el scheduler de recuperación detecta centralizadores en estado `CALCULANDO` por más de 5 minutos.
- **Seguridad**: el `tenantId` se propaga en el `MateriaCerradaEvent` y se inyecta en el contexto del thread asíncrono mediante `TenantContextHolder` (coherente con ADR-0001).
- **Equipo**: los tests de UC-03 requieren `Awaitility` para verificar el resultado asíncrono; se prohíbe usar `Thread.sleep()` en tests (regla de `qa-agent`).
- **Costo**: sin costo adicional en v1.0; en v1.1 la migración a SQS FIFO añade ~$2–5/mes por cola.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si el thread pool de consolidación satura el 80 % de los threads disponibles de la JVM durante cierres trimestrales, o si los centralizadores huérfanos superan el 1 % de los cierres en producción.
- **Costo estimado de revertir**: 1 semana para implementar la consolidación síncrona (Alternativa A) como modo de compatibilidad activable por feature flag; el dominio no cambia.
- **Plan B**: activar el feature flag `edusync.consolidacion.modo=sincrono` en `parametro_academico` para forzar la consolidación síncrona en colegios pequeños que prefieren consistencia fuerte sobre velocidad de respuesta.

### 7. Validación

- **Test de consistencia eventual `ConsolidacionAsincronaIT`**: `ConsolidacionAsincronaIT.centralizador_disponible_dentro_de_5_segundos_post_cierre()` — usa `Awaitility` para verificar que tras el cierre de la última materia del curso, el centralizador en BD tiene estado `PROVISIONAL` u `OFICIAL` dentro de 5 segundos. Ejecuta en CI con Testcontainers PostgreSQL.
- **Test de respuesta inmediata**: verifica que el endpoint `POST /materias/{id}/cierre` responde en menos de 500 ms (NFR-001) independientemente del tiempo que tarde el cálculo de consolidación.
- **Test de scheduler de recuperación**: `CentralizadorHuerfanoSchedulerTest` verifica que un centralizador en estado `CALCULANDO` por más de 5 minutos es detectado y relanzado en el siguiente ciclo del scheduler.

### 8. Referencias

- `FSD-UC-02` (Cierre operativo de materia — dispara el evento `MateriaCerradaEvent`).
- `FSD-UC-03` (Consolidación algorítmica — el motor consume el evento y produce el centralizador).
- `BR-003` (El criterio de truncado es `floor()`; el motor de consolidación es el único lugar donde se aplica).
- `BR-008` (El cálculo de promedio es exclusivo del motor de dominio; no en SQL, no en el frontend).
- `BR-011` (El promedio anual solo se calcula cuando los 3 trimestres están `CERRADO`).
- `NFR-001` (p95 de respuesta de UC-02 < 500 ms — la asincronía garantiza este umbral).
- `DA-04` en `docs/arquitectura_funcional_EduSync.md`.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado a partir de DA-04 en arquitectura_funcional_EduSync.md; estado Aceptada |
