# PR-IMPL-017 — Académico: Evaluaciones (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-017` |
| Título | Módulo `academico`: `Evaluacion` (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-017.md` |
| ID origen | `DD-UC-017` (`FSD-UC-015`) |
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
Eres un Senior Full-Stack Engineer con experiencia en Java 25 / Spring Boot
4.1.0 (arquitectura hexagonal, Spring Data JPA, Spring Modulith) y Angular 21
(standalone, signals) en el proyecto EduSync.
```

### 1.2 Task

```text
Implementa FSD-UC-015 segun docs/design/DD-UC-017.md §2, backend Y frontend
en el mismo prompt:

Backend (modulo academico): dominio Evaluacion (Aggregate independiente;
puntajeMaximo copiado de seccion.nota, NUNCA del cliente; estado ACTIVA/
ANULADA); POST /api/v1/evaluaciones; GET /materias/{id}/evaluaciones;
GET/PATCH /evaluaciones/{id}; PATCH .../estado ANULADA; GET /materias/mias
(antes de GET /{id}). A1 409 E_MATERIA_SIN_PROFESOR; 422 E_PERIODO_NO_ABIERTO
y E_SECCION_NO_PERTENECE_A_GESTION. actorId = JWT principal, nunca del body.
Migracion Flyway V11 con tenant_id + RLS FORCE. Deltas GET PROFESOR en
catalogos (gestiones/periodos/secciones/GET materia).

Frontend: /academico/mis-materias (PROFESOR) y
/academico/materias/:id/evaluaciones (ADMIN+PROFESOR); enlace en materia
detalle; shell "Mis materias"; login PROFESOR -> mis-materias.
No tocar role.guard.ts (usar data.roles).
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-017.md (Aggregate independiente, puntajeMaximo
  derivado, primera UI PROFESOR; ver §2/§3).
- FSD: docs/product/FSD.md §4.6.5 (FSD-UC-015).
- ADRs: ADR-0001 (RLS), ADR-0008 (stack vivo), ADR-0009, ADR-0010 (multi-rol),
  ADR-0011, ADR-0012 (Lombok allowlist), ADR-0013 (modelo generico; este slice
  materializa Evaluacion, NO Calificacion ni el motor de calculo).
- Precedentes backend: AsignacionMateriaProfesorRepositoryPort,
  PeriodoEvaluacion, SeccionEvaluacion, MateriaController, JwtAuthenticationFilter
  (principal = userId).
- Precedentes frontend: materia-detalle.page.ts, role.guard data.roles,
  gestion-secciones.page.ts.
- Prerrequisito: PR-IMPL-001..016 ejecutados (Secciones API+UI).
- Restricciones: tenantId SIEMPRE desde TenantContextProvider; academico
  NO importa identidad; no Calificacion/notas de estudiante; no motor
  FSD-UC-016 (round/floor); no TipoEvaluacion; no audit_log (ADR-0009 §3
  punto 5); no loguear PII; no modificar docs/baseline/**.
```

### 1.4 Reasoning

```text
BACKEND
1. domain: EvaluacionId, EstadoEvaluacion, Evaluacion (factory crear con
   puntajeMaximo inyectado desde el servicio = seccion.nota; actualizarDatos;
   anular; reconstruir). Excepciones listadas en DD-UC-017 §2. Lombok solo
   @Getter.
2. V11: tabla evaluacion (id, tenant_id, materia_id, periodo_evaluacion_id,
   seccion_evaluacion_id, nombre, fecha, puntaje_maximo, descripcion, estado)
   + RLS FORCE + FKs. Sin backfill.
3. EvaluacionRepositoryPort: guardar, buscarPorIdYTenant,
   listarPorMateriaYTenant (filtro periodo opcional).
4. CrearEvaluacionService: cargar materia/periodo/seccion del tenant;
   exigir periodo ABIERTO; seccion.gestion == periodo.gestion; al menos un
   profesor asignado; si actor es PROFESOR-only, debe estar asignado;
   puntajeMaximo = seccion.nota; estado ACTIVA.
5. actorId desde SecurityContext en el adapter, pasado al command.
6. EvaluacionController + delta MateriaController (GET /mias ANTES de
   /{id}; GET /{id}/evaluaciones; GET /{id} + PROFESOR). Deltas GET PROFESOR
   en GestionEscolarController, PeriodoEvaluacionController,
   SeccionEvaluacionController (solo lecturas ya existentes).
7. ExceptionHandler: 409 E_MATERIA_SIN_PROFESOR; 422 UNPROCESSABLE_CONTENT
   para periodo/seccion-gestion; 404 resto.
8. Tests: dominio, Mockito, EvaluacionIntegrationTest (dos evals Saber
   puntajeMaximo=45, A1 409, periodo PENDIENTE 422, PROFESOR no asignado
   404, cross-tenant 404).

FRONTEND
9. evaluacion.model.ts, mis-materias.page.ts, materia-evaluaciones.page.ts.
10. Rutas: mis-materias; materias/:id/evaluaciones (despues de /nuevo y
    detalle). data.roles. No reescribir role.guard.ts.
11. Shell enlace Mis materias; login PROFESOR -> mis-materias; enlace
    Evaluaciones en materia-detalle.
12. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando:
- POST /evaluaciones sin puntajeMaximo crea eval con puntajeMaximo = seccion.nota.
- Materia sin profesor -> 409 E_MATERIA_SIN_PROFESOR.
- Periodo no ABIERTO -> 422 E_PERIODO_NO_ABIERTO.
- PROFESOR no asignado -> 404 E_MATERIA_NO_ENCONTRADA (no 403).
- Cross-tenant 404 (no 403).
- GET /materias/mias lista las asignadas al JWT.
- mvn test (incl. ModularityTests) y ng build en verde.
NO implementes FSD-UC-016, Calificacion, round/floor, TipoEvaluacion,
audit_log. NO edites docs/baseline/**. NO reescribas role.guard.ts.
```

### 1.6 Output

```text
backend/src/main/java/com/edusync/academico/domain/Evaluacion*.java
backend/src/main/java/com/edusync/academico/application/** (use cases evaluacion)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/** (delta)
backend/src/main/resources/db/migration/V11__academico_evaluacion.sql
backend/src/test/java/com/edusync/academico/**Evaluacion*
frontend/src/app/features/academico/evaluacion.model.ts
frontend/src/app/features/academico/mis-materias.page.ts
frontend/src/app/features/academico/materia-evaluaciones.page.ts
frontend/src/app/features/academico/materia-detalle.page.ts (delta)
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
frontend/src/app/features/auth/login/login.page.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene del cliente — siempre de `TenantContextProvider`.
- `actorId` **nunca** proviene del cliente — siempre del JWT principal.
- `academico` **no** importa `identidad` ni `plataforma`.
- `puntajeMaximo` **solo** se copia de `seccion.nota`; el command/request no lo trae.
- Ninguna escritura de evaluación si el periodo no está `ABIERTO`.
- Acceso cross-tenant → `404`, nunca `403`.
- `mvn test` (incluye `ModularityTests`) y `ng build` en verde.
- Logs **MUST NOT** registrar PII.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar; siempre del contexto |
| `E_ACTOR_DESDE_CLIENTE` | El body trae `profesorId`/`actorId` | Rechazar; siempre del JWT |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar; `ApplicationModules.verify()` |
| `E_PUNTAJE_DEL_CLIENTE` | Se persiste un `puntajeMaximo` enviado por el cliente | Revertir; viola `ADR-0013` §3.3 |
| `E_CALIFICACION_COLADA` | Se implementó nota de estudiante o el motor `round`/`floor` | Revertir; es `FSD-UC-016` |
| `E_TIPO_EVALUACION` | Se implementó el catálogo `TipoEvaluacion` | Revertir; diferido `ADR-0013` §3.5 |
| `E_AGREGADO_EMBEBIDO` | Evals como colección JPA de `Materia` | Revertir; Aggregate independiente |
| `E_AUDIT_LOG_INVENTADO` | `audit_log` sin resolver `ADR-0009` §3 punto 5 | Revertir |
| `E_PII_EN_LOG` | Se loguea PII | Rechazar (`AGENTS.md` §7) |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` | Rechazar; solo usarlo |
| `E_BASELINE_TOCCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: `actorId` siempre desde el JWT principal.
- MUST: `puntajeMaximo = seccion.nota` en el alta.
- MUST: `409 E_MATERIA_SIN_PROFESOR`, `422 E_PERIODO_NO_ABIERTO`.
- MUST: `404` cross-tenant y Profesor no asignado (no 403).
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST NOT: `FSD-UC-016`, `Calificacion`, `round`/`floor`, `TipoEvaluacion`.
- MUST NOT: `audit_log`; `docs/baseline/**`; PII en logs.
- MUST NOT: reescribir `role.guard.ts`.
- MUST NOT: embebido de evaluaciones en el AR `Materia`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-017` | PR-IMPL-017 | `dev-agent` | `Evaluacion`, `V11`, consola Angular de Evaluaciones |
| FSD | `FSD-UC-015` | PR-IMPL-017 | `dev-agent` | Octavo feature de negocio real de `academico` (sexto fullstack) |
| ADR | `ADR-0013` | PR-IMPL-017 | `dev-agent` | Evals en `[0, seccion.nota]` vía `puntajeMaximo` derivado |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-017` completo; materia con profesor; T1 `ABIERTO`; Saber `nota=45`.
- **Output esperado**: `POST` dos evals → `201` con `puntajeMaximo=45.00`; `GET /materias/mias` lista la materia; UI crea y lista; `mvn test` y `ng build` en verde.

### 6.2 Caso borde

- **Input**: materia sin `AsignacionMateriaProfesor`; o periodo `PENDIENTE`.
- **Output esperado**: `409 E_MATERIA_SIN_PROFESOR` / `422 E_PERIODO_NO_ABIERTO`.

### 6.3 Caso adversarial

- **Input**: solicitud de persistir nota de estudiante, `round`/`floor`, o `puntajeMaximo` en el body.
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-017` §3; `FSD-UC-016` tiene Design Doc propio.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-017.md` v1.0. Sexto prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | Ejecución: Aggregate `Evaluacion`, `V11`, consola Mis materias / evaluaciones. `mvn test` 228/228; `ng build` verde. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | aprobado (prompt) | Diseño fullstack; ejecución de código pendiente |
| Rodrigo Aspeti | 21/08/2026 | ejecutado | Código + tests verdes (`mvn test` 228/228, `ng build`) |
