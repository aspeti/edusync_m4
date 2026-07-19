# Prompt — Actualizar DTI §6: Seams de descomposición del monolito EduSync

> **ID**: `PR-DTI-SEAMS-001`
> **Cómo usarlo**: Abre Claude Code o Cursor con el repositorio EduSync montado y pega
> este prompt desde `## Role` hasta el final. El agente actualizará `docs/DTI.md` directamente.

---

## Metadatos del prompt

| Campo            | Valor                                                              |
|------------------|--------------------------------------------------------------------|
| ID del prompt    | `PR-DTI-SEAMS-001`                                                 |
| Título           | Seams de descomposición del monolito EduSync — Tarea 1             |
| Artefacto origen | `docs/DTI.md` §4.1 (bounded contexts) + `docs/brd/BRD_EduSync_v2.md` |
| Artefacto destino| `docs/DTI.md` §6 — nueva subsección §6.2                          |
| Tipo de prompt   | transformación / generación                                        |
| Modelo sugerido  | Claude Sonnet                                                      |
| Temperatura      | 0.2                                                                |
| Versión          | v1.0                                                               |
| Fecha            | 28/05/2026                                                         |
| Autor            | Rodrigo Aspeti                                                     |
| Estado           | Aprobado                                                           |

---

## Role

Eres el arquitecto de software del equipo G-EduSync, experto en descomposición de monolitos
y patrones de microservicios (Strangler Fig, seams de descomposición). Conoces a fondo el
sistema EduSync: monolito modular en Java 21 / Spring Boot 3.3 / PostgreSQL 15 RLS desplegado
en AWS ECS Fargate, con 5 bounded contexts definidos en `docs/DTI.md §4.1` y arquitectura
hexagonal documentada en `docs/DTI.md §5`.

Tu tarea es producir un análisis técnico riguroso de los 2 mejores seams de descomposición
del sistema, con evidencia extraída exclusivamente de los documentos del repositorio.

---

## Task

Agregar la subsección `### 6.2 Seams de descomposición` al archivo `docs/DTI.md`,
inmediatamente después de la subsección `### 6.1 Patrones de resiliencia aplicados`,
con el análisis de los 2 seams de descomposición identificados para EduSync usando:

- Los bounded contexts del `DTI §4.1`.
- El heat map de cambios implícito en los FSD-UC y sus BR (frecuencia de cambio por contexto).
- Los volúmenes de tráfico diferenciados por rol de usuario (Director / Docente / Secretaria).
- El árbol de decisión T1.8 del módulo (criterios: equipos independientes, escala diferenciada,
  costo de separación vs. beneficio operacional, aislamiento de fallos).

No modificar ninguna otra sección del DTI. No borrar ni renumerar `### 6.1`.

---

## Context

### Bounded contexts disponibles en DTI §4.1

| Contexto        | Responsabilidad principal                              | Integración actual     |
|-----------------|--------------------------------------------------------|------------------------|
| `calificaciones`| Registrar, validar y corregir notas por dimensión      | Síncrona (HTTP)        |
| `periodos`      | Ciclo de vida del periodo académico y materias         | Síncrona (HTTP)        |
| `consolidacion` | Calcular promedios con `floor()` y emitir centralizadores | Asíncrona (Spring Event post-commit) |
| `exportacion`   | Sincronizar con SIE por RUDE con idempotencia          | Asíncrona resiliente (Resilience4j) |
| `auditoria`     | Registro inmutable de todas las escrituras             | Síncrona (misma TX)    |

### Evidencia de desacoplamiento existente (extraída del DTI)

- `calificaciones` → `consolidacion`: ya desacoplados por `MateriaCerradaEvent` (AFTER_COMMIT).
  El evento viaja por Spring Events hoy y está preparado para AWS SQS en v1.1 (DA-04).
- `exportacion`: `SIEHttpClient` ya vive en un adaptador de salida propio con Resilience4j,
  circuit breaker, timeout 30 s y scheduler de reintentos cada 5 min (DA-05).
  Puede fallar sin afectar el registro de calificaciones.

### Roles y patrones de tráfico diferenciados (BRD v2 + FSD)

| Rol       | Contexto que más usa | Patrón de carga                                     |
|-----------|----------------------|-----------------------------------------------------|
| Docente   | `calificaciones`     | Continuo durante el periodo; picos en deadlines     |
| Director  | `periodos`           | Esporádico; crítico solo en apertura/cierre         |
| Secretaria| `exportacion`        | Puntual y masivo al final de cada trimestre (SIE)   |
| Sistema   | `consolidacion`      | Batch post-cierre; puede ser diferido sin impacto UX|
| Sistema   | `auditoria`          | Co-ubicado con toda escritura; no puede separarse   |

### Árbol de decisión T1.8 — criterios a evaluar por seam

Para cada seam responder SÍ/NO a:

1. ¿Equipos distintos necesitan desplegarlo de forma independiente?
2. ¿Los volúmenes de tráfico son tan diferentes que requieren escala independiente?
3. ¿El contexto puede fallar sin afectar la disponibilidad del núcleo?
4. ¿El costo de separación (contratos API, consistencia eventual, TX distribuidas) se justifica HOY con el volumen proyectado (año 1: < 50 unidades educativas)?

Criterio de recomendación:
- 4 SÍ → **Romper ahora**.
- 3 SÍ → **Romper en v2.0** (cuando el volumen lo justifique).
- ≤ 2 SÍ → **No romper** (el costo supera el beneficio).

---

## Reasoning

Sigue estos pasos en orden:

1. Leer `docs/DTI.md` completo para entender la estructura actual del §6 y confirmar
   que `### 6.1 Patrones de resiliencia aplicados` existe antes de insertar.

2. Leer `docs/brd/BRD_EduSync_v2.md` para identificar qué BR-NNN tocan múltiples
   bounded contexts simultáneamente (esas BR son evidencia de acoplamiento).

3. Leer `docs/fsd/FSD_EduSync.md` para confirmar qué FSD-UC pertenecen a cada
   bounded context y cuáles cruzan fronteras (FSD-UC que tocan 2 contextos = seam candidato).

4. Construir el análisis del **Seam 1: `calificaciones` ↔ `consolidacion`**:
   - Nombre del seam.
   - Evidencia: cuántos FSD-UC tocan ambos lados; qué evento ya los desacopla (DA-04);
     qué BR pertenece a cada lado (BR-002, BR-008); patrones de tráfico distintos.
   - Árbol T1.8: evaluar los 4 criterios con SÍ/NO y justificación de una línea cada uno.
   - Recomendación: romper ahora / romper en v2.0 / no romper — con criterio explícito.

5. Construir el análisis del **Seam 2: `exportacion` ↔ núcleo (`calificaciones` + `consolidacion`)**:
   - Nombre del seam.
   - Evidencia: `SIEHttpClient` ya aislado (DA-05); fallo del SIE no bloquea registro de notas;
     tráfico puntual masivo vs. tráfico continuo; cumplimiento Ley 070 como dominio separado.
   - Árbol T1.8: evaluar los 4 criterios con SÍ/NO y justificación de una línea cada uno.
   - Recomendación: con criterio explícito.

6. Redactar el bloque Markdown `### 6.2 Seams de descomposición` con la estructura
   exacta definida en §Output de este prompt.

7. Insertar el bloque en `docs/DTI.md` inmediatamente después del cierre (`---`) de
   `### 6.1 Patrones de resiliencia aplicados`, antes de `## 7.`.

8. Agregar una fila al registro de cambios del DTI (§0 Metadatos o al final del archivo)
   indicando: versión, fecha, autor `Rodrigo Aspeti`, cambio `"§6.2 Seams de descomposición — Tarea 1 Módulo 4"`.

---

## Stop Condition

Detente cuando:

- `docs/DTI.md` contenga `### 6.2 Seams de descomposición` con los 2 seams completos.
- Cada seam tenga: nombre, tabla de evidencia, tabla T1.8 con 4 criterios, y recomendación.
- La recomendación cite explícitamente el criterio T1.8 que la determinó.
- `### 6.1 Patrones de resiliencia aplicados` esté intacta (no modificada, no renumerada).
- El registro de cambios del DTI refleje la actualización.
- No se haya modificado ninguna otra sección del DTI.

---

## Output

El bloque a insertar en `docs/DTI.md` debe tener exactamente esta estructura:

```markdown
### 6.2 Seams de descomposición `[humano]`

> **Contexto**: EduSync v1.0 es un monolito modular. Este análisis identifica los 2 seams
> de descomposición con mayor potencial para una futura migración a servicios independientes,
> usando los bounded contexts del §4.1 y el árbol de decisión T1.8.
> **Entrega académica**: Tarea 1 — Módulo 4 UMSS / M.Sc. Edson Terceros.

---

#### Seam 1: `calificaciones` ↔ `consolidacion`

**Evidencia de desacoplamiento**

| Dimensión | Calificaciones | Consolidación |
|-----------|---------------|---------------|
| FSD-UC principales | FSD-UC-001 (registrar), FSD-UC-005 (corregir) | FSD-UC-003 (consolidar centralizador) |
| BR dominantes | BR-002 (rango), BR-004 (RUDE), BR-005 (append-only) | BR-008 (floor()), BR-011 (3 trimestres) |
| Actor principal | Docente — carga continua durante el periodo | Sistema — batch post-cierre de materia |
| Patrón de tráfico | Continuo, frecuente, latencia < 500 ms (NFR-001) | Diferido, batch, puede tolerar segundos |
| Desacoplamiento existente | — | `MateriaCerradaEvent` AFTER_COMMIT (DA-04); preparado para AWS SQS v1.1 |

**Árbol de decisión T1.8**

| Criterio | Resultado | Justificación |
|----------|-----------|---------------|
| 1. ¿Equipos distintos necesitan desplegarlo independientemente? | SÍ | Docentes generan carga constante; la consolidación solo corre al cierre — cadencias de release distintas |
| 2. ¿Volúmenes de tráfico tan distintos que requieren escala independiente? | SÍ | Registro de notas: picos en deadline (todos los docentes a la vez); consolidación: 1 ejecución por materia cerrada |
| 3. ¿Puede fallar sin afectar disponibilidad del núcleo? | SÍ | Si la consolidación falla, el Docente sigue registrando; el Centralizador queda en PROVISIONAL hasta reintentar |
| 4. ¿El costo de separación se justifica HOY (año 1, < 50 unidades)? | NO | Con volumen < 50 unidades el overhead de consistencia eventual y TX distribuidas supera el beneficio |

**Recomendación: Romper en v2.0**
> 3 de 4 criterios T1.8 son SÍ. El desacoplamiento técnico ya existe (DA-04 / Spring Events).
> La separación formal se justifica cuando el volumen supere 50 unidades educativas o cuando
> el equipo de consolidación necesite ciclos de release independientes del equipo de calificaciones.
> Candidato a primer servicio extraído con patrón Strangler Fig desde el Event Bus.

---

#### Seam 2: `exportacion` ↔ núcleo (`calificaciones` + `consolidacion` + `periodos`)

**Evidencia de desacoplamiento**

| Dimensión | Exportación SIE | Núcleo (calificaciones + consolidación + periodos) |
|-----------|----------------|---------------------------------------------------|
| FSD-UC principal | FSD-UC-004 (exportar al SIE) | FSD-UC-001, 003, 005, 009 |
| BR/DA dominantes | DA-05 (circuit breaker), NFR-005 (idempotencia SIE) | BR-001..BR-009, DA-01..DA-04 |
| Actor principal | Secretaria — puntual, fin de trimestre | Docente + Director — continuo durante el periodo |
| Patrón de tráfico | Masivo y puntual (exportación trimestral); puede diferirse | Constante durante el periodo académico |
| Dominio regulatorio | Ley 070 Avelino Sinani — contrato externo no negociable | Reglas internas del negocio |
| Aislamiento existente | `SIEHttpClient` en adaptador propio con Resilience4j (DA-05); fallo del SIE no bloquea escritura de notas | — |

**Árbol de decisión T1.8**

| Criterio | Resultado | Justificación |
|----------|-----------|---------------|
| 1. ¿Equipos distintos necesitan desplegarlo independientemente? | SÍ | El protocolo SIE cambia por regulación ministerial independientemente del negocio interno; updates del adaptador SIE no deben afectar el registro de notas |
| 2. ¿Volúmenes de tráfico tan distintos que requieren escala independiente? | SÍ | Exportación: pico masivo puntual al fin de trimestre; núcleo: carga distribuida continua — perfiles de escala opuestos |
| 3. ¿Puede fallar sin afectar disponibilidad del núcleo? | SÍ | Circuit breaker activo (DA-05): SIE caído → exportaciones en PENDIENTE; Docentes siguen registrando notas sin interrupción |
| 4. ¿El costo de separación se justifica HOY (año 1, < 50 unidades)? | NO | El volumen actual no genera suficiente presión operacional; el adaptador ya está suficientemente aislado como módulo interno |

**Recomendación: Romper en v2.0 — primer candidato a microservicio**
> 3 de 4 criterios T1.8 son SÍ. Es el seam más maduro: el `SIEHttpClient` ya es un adaptador
> de salida independiente (DA-05), el dominio regulatorio es externo y cambia por ley, y el
> aislamiento de fallos ya está validado en producción. Cuando el volumen supere 30 unidades
> simultáneas exportando al cierre trimestral, separarlo elimina el riesgo de que un pico de
> exportación degrade la latencia de registro de notas (NFR-001 < 500 ms p95).
> Patrón de migración recomendado: Strangler Fig extrayendo `ExportarSIEUseCase` +
> `SIEHttpClient` + `SIERetryScheduler` como servicio `edusync-sie-exporter`.

---
```

---

## Invariants

- El bloque insertado MUST referenciar al menos 2 FSD-UC, 2 BR/DA y 1 NFR por seam.
- Las recomendaciones MUST citar el número de criterios T1.8 que las fundamentan.
- MUST NOT modificar ninguna sección del DTI fuera de §6 y el registro de cambios.
- MUST NOT renumerar `### 6.1 Patrones de resiliencia aplicados`.
- Los nombres de bounded contexts MUST coincidir exactamente con los de `DTI §4.1`.

---

## Failure Modes

| Código | Descripción | Acción |
|--------|-------------|--------|
| `E_SECTION_NOT_FOUND` | No existe `### 6.1` en el DTI | STOP — verificar la versión del DTI antes de insertar |
| `E_BC_MISMATCH` | Nombre de bounded context no coincide con §4.1 | Corregir el nombre antes de guardar |
| `E_MISSING_TRACEABILITY` | Un seam no referencia FSD-UC, BR o DA del proyecto | Completar la tabla de evidencia con datos del FSD/BRD |
| `E_RECOMMENDATION_UNSUPPORTED` | La recomendación no cita criterios T1.8 | Agregar el conteo de criterios SÍ/NO y la justificación |

---

## Versionado

| Versión | Fecha      | Autor          | Cambio          |
|---------|------------|----------------|-----------------|
| v1.0    | 28/05/2026 | Rodrigo Aspeti | Creación inicial |
