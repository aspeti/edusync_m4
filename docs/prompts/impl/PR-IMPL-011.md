# PR-IMPL-011 — Frontend: consola de Cursos y Paralelos

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-011` |
| Título | Generación de la consola Angular de Cursos y Paralelos |
| Artefacto origen | `docs/design/DD-UC-011.md` |
| ID origen | `DD-UC-011` (`FSD-UC-017`, cierre de UI) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 21/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Frontend Engineer con experiencia en Angular 21 (standalone,
signals) consumiendo un backend Java 25 / Spring Boot 4.1.0 hexagonal.
```

### 1.2 Task

```text
Implementa la consola Angular de Cursos y Paralelos segun docs/design/
DD-UC-011.md §2: features/academico/ (lista de Cursos con filtro q +
paginacion, alta de Curso, vista de detalle de un Curso con sus Paralelos
y alta inline de Paralelo); rutas /academico/cursos[, /nuevo,
/:id/paralelos] protegidas por roleGuard(ADMIN); enlace de nav "Cursos" en
shell.component.ts. Sin delta de backend.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-011.md (patron sin design system de
  features/academico/, DD-UC-009; vista de Paralelos como pantalla/ruta
  propia, no un acordeon en la lista de Cursos; alta de Paralelo inline en
  esa misma pantalla, sin ruta /nuevo separada).
- Contratos ya existentes (DD-UC-010, sin cambios): GET/POST /cursos (q,
  page, size), GET/POST /cursos/{id}/paralelos (sin paginar).
- Precedentes de codigo a replicar: features/academico/
  gestiones-escolares-list.page.ts (lista con filtro q + paginacion
  PageResponse<T>, sin el select de estado porque Curso no tiene estado),
  features/academico/gestion-escolar-create.page.ts (formulario de alta),
  core/api/page-response.model.ts (DD-UC-007).
- ADRs: ADR-0008 (Angular 21), ADR-0009 (Curso/Paralelo son entidades de la
  generalizacion SaaS).
- Prerrequisito: PR-IMPL-001..010 ya ejecutados (backend de Curso/Paralelo
  completo).
- Restricciones: NO delta de backend; NO implementar pantallas de
  Materia/Profesor/Estudiante/Inscripcion (FSD-UC-018..020); NO implementar
  edicion ni eliminacion de Curso/Paralelo (el backend no expone
  PATCH/DELETE); NO implementar seleccion de Curso/Paralelo en el alta de
  Usuario ASESOR (E_ASESOR_SIN_CURSO diferido, fuera de alcance de DD-UC-011).
```

### 1.4 Reasoning

```text
1. curso.model.ts (CursoResponse, CursoFiltro, ParaleloResponse).
2. cursos-list.page.ts: GET /cursos (q, page, size) -- sin select de estado;
   cada fila con link "Ver paralelos" hacia /academico/cursos/:id/paralelos.
3. curso-create.page.ts: POST /cursos (nombre); validar nombre no vacio.
4. curso-paralelos.page.ts: GET /cursos/{id}/paralelos (sin paginar) +
   formulario inline (nombre) que hace POST /cursos/{id}/paralelos y
   refresca la lista tras crear.
5. app.routes.ts: /academico/cursos[, /nuevo, /:id/paralelos] (roleGuard
   ADMIN).
6. shell.component.ts: enlace "Cursos" condicional a auth.hasRole('ADMIN'),
   junto a "Usuarios" y "Gestion Escolar".
7. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) Admin autenticado ve /academico/cursos y puede
crear/listar/filtrar/paginar Cursos, (b) desde una fila puede navegar a
/academico/cursos/:id/paralelos y ver/crear Paralelos de ese Curso, (c) ng
build en verde. No implementes delta de backend, edicion/eliminacion de
Curso/Paralelo, ni pantallas de FSD-UC-018..020.
```

### 1.6 Output

```text
Formato: codigo fuente real en frontend/ (no markdown).
Extracto esperado:
frontend/src/app/features/academico/curso.model.ts
frontend/src/app/features/academico/cursos-list.page.ts
frontend/src/app/features/academico/curso-create.page.ts
frontend/src/app/features/academico/curso-paralelos.page.ts
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
```

## 2. Invariantes del prompt

- Ninguna pantalla de este prompt implementa `FSD-UC-018`..`FSD-UC-020` ni una selección de `Curso`/`Paralelo` para `Usuario` `ASESOR`.
- Ninguna pantalla ofrece editar o eliminar un `Curso`/`Paralelo` (el backend no lo expone).
- `ng build` **debe** quedar en verde.
- Este prompt **no debe** modificar código backend.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_OPERACION_NO_EXPUESTA` | La UI ofrece editar/eliminar un `Curso`/`Paralelo` | Revertir; el backend (`DD-UC-010`) no expone `PATCH`/`DELETE` |
| `E_ALCANCE_EXCEDIDO` | Se implementó una pantalla de `FSD-UC-018`..`020` o de asignación `ASESOR` | Revertir; corresponde a un Design Doc de seguimiento |
| `E_DELTA_BACKEND` | El prompt modificó código bajo `backend/` | Rechazar; este prompt es frontend-only |
| `E_SELECT_ESTADO_INVENTADO` | Se agregó un filtro de estado en la lista de Cursos | Revertir; `Curso` no tiene estado (`DD-UC-010` §2) |

## 4. Guardrails

- MUST: `ng build` en verde antes de considerar el prompt completo.
- MUST: la vista de Paralelos valida que el `Curso` exista (reutiliza el `404 E_CURSO_NO_ENCONTRADO` ya expuesto por el backend, sin ocultarlo).
- MUST NOT: modificar ningún archivo bajo `backend/` ni `docs/baseline/**`.
- MUST NOT: implementar pantallas de `FSD-UC-018`..`FSD-UC-020`.
- MUST NOT: implementar edición/eliminación de `Curso`/`Paralelo`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-011` | PR-IMPL-011 | `dev-agent` | `frontend/src/app/features/academico/**` (delta) |
| FSD | `FSD-UC-017` (cierre UI) | PR-IMPL-011 | `dev-agent` | Consola de Cursos y Paralelos |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-011` completo; backend de `DD-UC-010` disponible.
- **Output esperado**: Admin crea un Curso, lo lista con filtro `q`/paginación, entra a su detalle y crea los Paralelos "A" y "B"; `ng build` en verde.

### 6.2 Caso borde

- **Input**: Curso recién creado, sin Paralelos todavía.
- **Output esperado**: la vista de detalle muestra la lista vacía sin error, con el formulario de alta de Paralelo disponible.

### 6.3 Caso adversarial

- **Input**: solicitud de agregar botones de editar/eliminar Curso o Paralelo "para completar el CRUD".
- **Comportamiento esperado**: rechazo — el backend (`DD-UC-010`) no expone esas operaciones; no implementarlas sin un Design Doc de seguimiento que las diseñe primero.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-011.md` v1.0. Segundo prompt de UI del módulo `academico`, después de `PR-IMPL-009`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | Ejecución real: `curso.model.ts`, `cursos-list.page.ts`, `curso-create.page.ts`, `curso-paralelos.page.ts` generados; delta en `app.routes.ts`/`shell.component.ts`. Refinamiento respecto al plan (documentado en `DD-UC-011` §2/§8): el nombre del Curso se propaga como *query param* hacia `CursoParalelosPage` porque el backend no expone `GET /cursos/{id}`. `ng build` → verde (3 lazy chunks nuevos). Sin delta de backend, sin pantallas de `FSD-UC-018`..`020`, sin edición/eliminación — ninguno de los `failure modes` de §3 se activó. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
| Rodrigo Aspeti | 21/08/2026 | **aprobado (ejecución)** | `ng build` verde; consola de Cursos y Paralelos funcional; `FSD-UC-017` completo (backend + UI) |
