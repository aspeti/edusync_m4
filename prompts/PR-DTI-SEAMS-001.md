# PR-DTI-SEAMS-001 — Seams de descomposición de EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-DTI-SEAMS-001` |
| Título | Seams de descomposición de EduSync |
| Artefacto origen | DTI + BRD v2 + FSD |
| ID origen | `FSD-UC-001`, `FSD-UC-003`, `FSD-UC-004`, `FSD-UC-005`, `FSD-UC-009`, `BR-002`, `BR-004`, `BR-005`, `BR-008`, `BR-011`, `DA-04`, `DA-05`, `NFR-001` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres el arquitecto de software del equipo G-EduSync, experto en descomposicion
de monolitos y patrones de microservicios (Strangler Fig, seams de
descomposicion). Conoces a fondo EduSync: monolito modular Java 21 / Spring
Boot 3.3 / PostgreSQL 15 RLS desplegado en AWS ECS Fargate, con bounded
contexts definidos en docs/DTI.md §4.1 y arquitectura hexagonal en §5.
```

### 1.2 Task
```text
Agregar la subseccion ### 6.2 Seams de descomposición al archivo docs/DTI.md,
inmediatamente despues de la subseccion ### 6.1 Patrones de resiliencia
aplicados, con el analisis de los 2 seams de descomposicion identificados
para EduSync.
```

### 1.3 Context
```text
- Documentos fuente: docs/DTI.md, docs/brd/BRD_EduSync_v2.md,
  docs/fsd/FSD_EduSync.md.
- Bounded contexts disponibles en DTI §4.1: calificaciones, periodos,
  consolidacion, exportacion, auditoria.
- Evidencia de desacoplamiento: calificaciones -> consolidacion por
  MateriaCerradaEvent AFTER_COMMIT (DA-04), preparado para AWS SQS v1.1.
- Evidencia de resiliencia: SIEHttpClient vive en adaptador de salida propio
  con Resilience4j, circuit breaker, timeout 30 s y scheduler de reintentos
  cada 5 min (DA-05).
- Roles y trafico: Docente usa calificaciones de forma continua; Director usa
  periodos esporadicamente; Secretaria usa exportacion de forma puntual y
  masiva al final de trimestre; consolidacion corre batch post-cierre.
- Criterios T1.8: equipos independientes, escala diferenciada, aislamiento de
  fallos, costo de separacion vs beneficio operacional en año 1 (< 50 unidades
  educativas).
```

### 1.4 Reasoning
```text
1. Leer docs/DTI.md completo para entender la estructura actual del §6 y
   confirmar que ### 6.1 Patrones de resiliencia aplicados existe antes de
   insertar.
2. Leer docs/brd/BRD_EduSync_v2.md para identificar BR-NNN que tocan multiples
   bounded contexts simultaneamente como evidencia de acoplamiento.
3. Leer docs/fsd/FSD_EduSync.md para confirmar que FSD-UC pertenecen a cada
   bounded context y cuales cruzan fronteras.
4. Construir el Seam 1 calificaciones ↔ consolidacion con evidencia FSD-UC,
   BR/DA, patrones de trafico, tabla T1.8 y recomendacion.
5. Construir el Seam 2 exportacion ↔ nucleo con evidencia FSD-UC, BR/DA,
   patrones de trafico, tabla T1.8 y recomendacion.
6. Insertar el bloque Markdown despues del cierre de ### 6.1 y antes de ## 7.
7. Agregar la fila del registro de cambios del DTI para la Tarea 1 Modulo 4.
```

### 1.5 Stop condition
```text
Detente cuando:
- docs/DTI.md contenga ### 6.2 Seams de descomposición con los 2 seams completos.
- Cada seam tenga nombre, tabla de evidencia, tabla T1.8 con 4 criterios y
  recomendacion.
- La recomendacion cite explicitamente el criterio T1.8 que la determino.
- ### 6.1 Patrones de resiliencia aplicados este intacta y no renumerada.
- El registro de cambios del DTI refleje la actualizacion.
- No se haya modificado ninguna otra seccion del DTI.
```

### 1.6 Output
```text
Bloque Markdown en docs/DTI.md con:
- ### 6.2 Seams de descomposición `[humano]`
- Contexto academico de EduSync v1.0 como monolito modular.
- #### Seam 1: `calificaciones` ↔ `consolidacion`
- Tabla de evidencia de desacoplamiento para Seam 1.
- Tabla T1.8 con 4 criterios SÍ/NO para Seam 1.
- Recomendacion de Seam 1 citando "3 de 4 criterios T1.8 son SÍ".
- #### Seam 2: `exportacion` ↔ nucleo (`calificaciones` + `consolidacion` + `periodos`)
- Tabla de evidencia de desacoplamiento para Seam 2.
- Tabla T1.8 con 4 criterios SÍ/NO para Seam 2.
- Recomendacion de Seam 2 citando "3 de 4 criterios T1.8 son SÍ".
- Fila de registro de cambios con version, fecha, Rodrigo Aspeti y
  "§6.2 Seams de descomposición — Tarea 1 Módulo 4".
```

## 2. Invariantes del prompt

- El bloque insertado referencia al menos 2 FSD-UC, 2 BR/DA y 1 NFR por seam.
- Las recomendaciones citan el numero de criterios T1.8 que las fundamentan.
- No se modifica ninguna seccion del DTI fuera de §6 y el registro de cambios.
- No se borra ni renumera `### 6.1 Patrones de resiliencia aplicados`.
- Los nombres de bounded contexts coinciden exactamente con DTI §4.1:
  `calificaciones`, `periodos`, `consolidacion`, `exportacion`, `auditoria`.
- El contexto `auditoria` no se propone como seam separable porque esta
  co-ubicado con toda escritura y comparte transaccion.
- El output no contiene secretos ni PII de estudiantes.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_SECTION_NOT_FOUND` | No existe `### 6.1` en el DTI | STOP; verificar la version del DTI antes de insertar |
| `E_BC_MISMATCH` | Nombre de bounded context no coincide con §4.1 | Corregir el nombre antes de guardar |
| `E_MISSING_TRACEABILITY` | Un seam no referencia FSD-UC, BR o DA del proyecto | Completar la tabla de evidencia con datos del FSD/BRD |
| `E_RECOMMENDATION_UNSUPPORTED` | La recomendacion no cita criterios T1.8 | Agregar el conteo de criterios SÍ/NO y la justificacion |

## 4. Guardrails

- MUST: validar que el output cumple la estructura exacta de §1.6 antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetria.
- MUST: preservar la trazabilidad hacia `docs/DTI.md`, `docs/brd/BRD_EduSync_v2.md` y `docs/fsd/FSD_EduSync.md`.
- MUST NOT: exponer secretos ni credenciales en el context.
- MUST NOT: almacenar PII ni RUDE reales en logs del prompt.
- MUST NOT: editar secciones ajenas al alcance declarado.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| DTI §4.1, §5 y §6 | `calificaciones`, `periodos`, `consolidacion`, `exportacion`, `auditoria`, `DA-04`, `DA-05` | PR-DTI-SEAMS-001 | `docs-agent` | `docs/DTI.md §6.2` |
| BRD v2 + FSD | `BR-002`, `BR-004`, `BR-005`, `BR-008`, `BR-011`, `FSD-UC-001`, `FSD-UC-003`, `FSD-UC-004`, `FSD-UC-005`, `FSD-UC-009` | PR-DTI-SEAMS-001 | `docs-agent` | Analisis T1.8 de seams y registro de cambios del DTI |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: DTI con §6.1 existente, BRD v2 y FSD disponibles, bounded contexts definidos en §4.1.
- **Output esperado**: `docs/DTI.md` contiene §6.2 con 2 seams, ambas tablas de evidencia, ambas tablas T1.8 y registro de cambios actualizado.

### 6.2 Caso borde
- **Input**: DTI ya contiene un §6.2 previo.
- **Output esperado**: el agente detecta duplicidad, evita crear una segunda subseccion y propone actualizar solo si el contenido existente no cumple los invariantes.

### 6.3 Caso adversarial
- **Input**: solicitud de separar `auditoria` como microservicio independiente en v1.0 aunque comparte transaccion con toda escritura.
- **Comportamiento esperado**: rechazo con `E_RECOMMENDATION_UNSUPPORTED`; la recomendacion debe respetar T1.8 y el costo transaccional documentado.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`, `hallucination_rate`.
- Eventos mínimos: `prompt.started`, `source_docs.read`, `dti.updated`, `prompt.completed`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 28/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v1.1 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
