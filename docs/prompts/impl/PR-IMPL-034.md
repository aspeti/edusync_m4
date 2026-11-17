# PR-IMPL-034 — Rangos min/max en `SeccionEvaluacion` (`FSD-UC-014`)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-034` |
| Título | Mover rangoMin/rangoMax a sección; limpiar UI Periodos; retirar ParametroPeriodo |
| Artefacto origen | `docs/design/DD-UC-034.md`, `docs/product/FSD.md` v2.20 |
| ID origen | `DD-UC-034`, `FSD-UC-014`, `FSD-UC-013`, `FSD-UC-009`, `ADR-0027` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 16/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Ejecutado** |

## 1. Task

Implementar `docs/design/DD-UC-034.md` §2: campos `rangoMin`/`rangoMax` en `SeccionEvaluacion`, migración Flyway, retirar `ParametroPeriodo` y endpoints `/parametros`, UI Secciones con mín/máx, UI Periodos solo nombre/fechas/estado/acciones, gates al abrir y validación de calificaciones según `ADR-0027`.

## 2. Stop condition

`mvn test` verde (incluye `ModularityTests`). `ng build` verde. DoD de `DD-UC-034` §7 con tests/build marcados. Sin panel de parámetros en Periodos. Sin `docs/baseline/**`. Tras cerrar: `@dtp-sync` y estado del prompt → Ejecutado.

## 3. Anatomía

### Role

Eres un Senior Full-Stack Engineer de EduSync (Java 25 / Spring Boot 4.1.0 / Spring Modulith / Angular 21 + PrimeNG) que implementa vertical slices hexagonales respetando RLS, sin `floor()` y con un solo motor `CalculoNotas`.

### Task

Implementa en este orden:

1. **Dominio:** ampliar `SeccionEvaluacion` con `rangoMin`/`rangoMax` e invariantes; seed 0/`nota`.
2. **Flyway:** columnas + backfill; drop `parametro_periodo`.
3. **API secciones:** DTOs/controllers con los nuevos campos; `E_RANGO_INVALIDO` en escritura.
4. **Retirar** `ParametroPeriodo` (domain/application/infrastructure/tests) y `GET`/`PUT .../parametros`.
5. **Abrir periodo:** validar rangos de secciones + Σ=100 + `E_MATERIA_SIN_DOCENTE`; eliminar gates `E_PARAMETROS_*`.
6. **Calificaciones:** validar valor en `[rangoMin, rangoMax]`.
7. **UI:** `gestion-secciones.page.ts` (+ modelo); `gestion-periodos.page.ts` sin bloque de parámetros.
8. Cerrar con `dtp-sync`, DoD y este prompt → Ejecutado.

### Context

- Documentos: `DD-UC-034`, `ADR-0027`, `FSD.md` v2.20 §4.5/§4.6.3/§4.6.4, `DD-UC-029` (histórico).
- Stack: Java 25, Spring Boot 4.1.0, Angular 21, PrimeNG 21.x, PostgreSQL 15 RLS.
- Restricciones:
  - `puntajeMaximo` sigue = `seccion.nota`.
  - Sin PII/`rude`/notas en logs.
  - No tocar `docs/baseline/**`.
  - No restaurar freeze/secuencialidad (`ADR-0014`).

### Reasoning

1. Migración primero (columnas NOT NULL con default) antes de cambiar el aggregate.
2. Actualizar/eliminar tests de `ParametroPeriodo` en el mismo PR.
3. Confirmar que abrir periodo ya no depende de `/parametros`.
4. UI Periodos: verificar que no queden llamadas HTTP a parámetros.

### Output

Código en `backend/.../academico/**`, migración Flyway, `frontend/.../gestion-secciones*`, `gestion-periodos*`, tests. Docs: DoD / `PROMPT_MAPPING` / DTP vía sync.

### Invariants

- Plantilla de secciones única por gestión (`ADR-0013`).
- Multitenancy + 404 cross-tenant.
- Un solo motor de notas (`CalculoNotas`, `round` HALF_UP).
- Sin baseline.

### Failure modes

- `E_PARAMETROS_VIVOS`: quedan endpoints o UI de `ParametroPeriodo` → rechazar.
- `E_RANGO_SIN_VALIDAR`: se acepta calificación fuera de `[rangoMin, rangoMax]` → rechazar.
- `E_FLOOR`: aparece `floor`/`Math.floor` en notas → rechazar.
- `E_BASELINE_TOCADO`: revertir.

## 4. Notas de ejecución

**Ejecutado 16/11/2026.** Dominio `SeccionEvaluacion` + `rangoMin`/`rangoMax`; Flyway `V14__academico_seccion_rangos.sql` (backfill + drop `parametro_periodo`); retirada completa de `ParametroPeriodo` y endpoints `/parametros`; gates de apertura vía `AperturaPeriodoPolitica` (rangos + Σ=100 + docente); calificaciones validan `[rangoMin, rangoMax]`; UI Secciones con mín/máx; UI Periodos sin panel de parámetros. `mvn test` verde; `ng build` verde.
