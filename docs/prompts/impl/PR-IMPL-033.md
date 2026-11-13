# PR-IMPL-033 — Planilla Profesor por Curso (`FSD-UC-022`)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-033` |
| Título | Landing Mis cursos + planilla estudiantes×evaluaciones con edición inline |
| Artefacto origen | `docs/design/DD-UC-033.md`, `docs/product/FSD.md` §4.6.12 |
| ID origen | `DD-UC-033`, `FSD-UC-022`, `PRD-US-031`, `ADR-0013`, `ADR-0026` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Ejecutado** |
| Issue | https://github.com/aspeti/edusync_m4/issues/21 |

## 1. Task

Implementar `docs/design/DD-UC-033.md` §2: endpoints `GET /profesores/me/asignaciones` y `GET /materias/{id}/planilla`, delta `PUT .../calificaciones` con `valor: null` (clear), UI Angular landing + planilla (`p-table`, autosave blur/Enter), redirect/nav del Profesor a `/academico/mis-cursos`.

## 2. Stop condition

`mvn test` verde (incluye `ModularityTests`). `ng build` verde. DoD de `DD-UC-033` §7 con tests/build marcados. Sin cálculo de notas en frontend. Sin `floor()`. Sin editar `docs/baseline/**`. Tras cerrar: `@dtp-sync` y estado del prompt → Ejecutado.

## 3. Anatomía

### Role

Eres un Senior Full-Stack Engineer de EduSync (Java 25 / Spring Boot 4.1.0 / Spring Modulith / Angular 21 + PrimeNG 21) que implementa vertical slices hexagonales sin violar `CalculoNotas` ni RLS.

### Task

Implementa en este orden:

1. **Backend — mis asignaciones**: `ListarMisAsignacionesUseCase` + service + `GET /api/v1/profesores/me/asignaciones` (ruta `me` antes de `{id}`). Reutilizar enriquecimiento de nombres como `AsignacionProfesorVista` / `cursoEtiqueta`.
2. **Backend — planilla**: `ObtenerPlanillaMateriaUseCase` + `GET /api/v1/materias/{materiaId}/planilla?cursoId&paraleloId&periodoId`. Columnas solo `ACTIVA`; filas nómina del curso/paralelo; `notaPeriodo` vía `CalculoNotas` only. RBAC y 404 según `DD-UC-033` §2.2.
3. **Backend — clear**: en upsert de calificaciones, `valor: null` elimina la calificación; tests de integración.
4. **Frontend**: `mis-cursos-list.page.ts`, `planilla-curso.page.ts`, modelos, rutas, shell «Mis cursos», login redirect `PROFESOR` → `/academico/mis-cursos`. Autosave celda sin botones Agregar/Editar/Guardar.
5. Conservar `/academico/mis-materias` y matriz por evaluación como camino secundario.
6. Cerrar con `dtp-sync`, DoD y este prompt a «Ejecutado».

### Context

- Documentos: `DD-UC-033`, `FSD-UC-022`, `DD-UC-018`, `DD-UC-032`, `ADR-0013`, `ADR-0026`, issue #21.
- Stack: Java 25, Spring Boot 4.1.0, Angular 21, PrimeNG 21.1.x, PostgreSQL 15 RLS.
- Restricciones:
  - Promedios **solo** en `CalculoNotas`; sin `floor()`.
  - Sin PII/`rude`/notas en logs.
  - Identidad de escritura por `estudianteId`; `rude` solo display.
  - No tocar `docs/baseline/**`.
  - No inventar alta de evaluaciones en esta planilla.

### Reasoning

1. Verificar que `GET .../me/asignaciones` no colisione con `GET .../{id}`.
2. Tests de planilla (orden columnas, ACTIVA-only, 404 cross-tenant) antes de UI.
3. Confirmar null-clear no rompe tests existentes de upsert (añadir casos).
4. UI: blur/Enter → PUT 1 ítem; actualizar `notaPeriodo` de la fila sin recalcular en TS.

### Output

Código en `backend/.../academico/**`, `frontend/.../mis-cursos*`, `planilla-curso*`, deltas rutas/shell/login, tests. Docs: DoD/`PROMPT_MAPPING`/DTP vía sync.

### Invariants

- Un solo motor de notas (`CalculoNotas`, `round` HALF_UP).
- Multitenancy + 404 cross-tenant.
- Sin botones Agregar/Editar/Guardar por celda en la planilla.
- Sin baseline.

### Failure modes

- `E_CALCULO_EN_FRONTEND`: la UI calcula el promedio → rechazar.
- `E_FLOOR`: aparece `floor`/`Math.floor` en notas → rechazar.
- `E_MIAS_REUSADO_MAL`: se rompe `GET /materias/mias` o el asistente → rechazar.
- `E_RUTA_ME_CAPTURADA`: `me` tratado como UUID → rechazar.
- `E_BASELINE_TOCADO`: revertir.
- `E_PII_EN_LOGS`: rude/nota en logs → rechazar.

## 4. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 10/11/2026 | G-EduSync | Prompt aprobado (`DD-UC-033`). Ejecución pendiente |
| v0.2 | 10/11/2026 | G-EduSync | Ejecución completa: me/asignaciones, planilla, null-clear, UI Mis cursos. Stop condition verde. Estado: **Ejecutado**. |
