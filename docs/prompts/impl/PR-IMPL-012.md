# PR-IMPL-012 — Académico: Materias (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-012` |
| Título | Módulo `academico`: `Materia` y asignaciones a Curso/Profesor (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-012.md` |
| ID origen | `DD-UC-012` (`FSD-UC-018`) |
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
Implementa FSD-UC-018 segun docs/design/DD-UC-012.md §2, backend Y frontend
en el mismo prompt:

Backend (modulo academico): dominio Materia + AsignacionMateriaCurso +
AsignacionMateriaProfesor (tres Aggregates independientes, sin estado);
POST/GET /api/v1/materias (alta, listado q+paginacion, GET por id);
POST/GET /api/v1/materias/{id}/asignaciones-curso;
POST/GET /api/v1/materias/{id}/asignaciones-profesor (409 E_MATERIA_SIN_CURSO
si no hay asignacion curso previa para el mismo curso/paralelo);
GET /api/v1/materias/profesores-disponibles.
Puerto academico.ProfesorConsultaPort (Open Host Service) implementado en
identidad.infrastructure. Migracion Flyway V7 con tenant_id + RLS en las
tres tablas. Delta RBAC: GET /cursos y GET /cursos/{id}/paralelos tambien
SECRETARIA (POST de Curso/Paralelo siguen ADMIN).

Frontend: consola Angular lista/alta/detalle de Materias con asignaciones
inline; rutas /academico/materias[, /nuevo, /:id]; roleGuard acepta data.roles
(['ADMIN','SECRETARIA']) sin romper data.role existente; enlace "Materias"
en el shell.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-012.md (tres Aggregates independientes, no FKs
  embebidas en Materia; Profesor = Usuario con rol PROFESOR; ver §2/§3).
- FSD: docs/product/FSD.md §4.6.8 (FSD-UC-018). Declara tres POST + A1 409
  E_MATERIA_SIN_CURSO. Los GET de listado/detalle/asignaciones y el catalogo
  de profesores se anaden por inferencia practica (mismo criterio que
  DD-UC-008/010), sin contradecir el FSD.
- ADRs: ADR-0001 (RLS), ADR-0008 (stack vivo), ADR-0009 (Materia es entidad
  de la generalizacion SaaS), ADR-0011 (academico modulo propio, shared
  OPEN), ADR-0012 (Lombok allowlist en domain/).
- Precedentes backend: academico/domain/Curso.java (Aggregate inmutable,
  factory crear(), solo @Getter), CursoController (DTOs en adapter/in/rest
  sin subpaquete dto/), CursoSpecifications, ErrorResponse de academico
  (reutilizar, no duplicar), identidad.TenantConsultaPort (puerto en el
  CONSUMIDOR, implementacion en el proveedor — replicar para
  ProfesorConsultaPort en academico / impl en identidad).
- Precedentes frontend: features/academico/cursos-list.page.ts,
  curso-create.page.ts, curso-paralelos.page.ts (detalle + alta inline);
  core/api/page-response.model.ts; role.guard.ts (hoy solo data.role).
- Prerrequisito: PR-IMPL-001..011 ya ejecutados (Curso/Paralelo y UI de
  Cursos disponibles).
- Restricciones: tenantId SIEMPRE desde TenantContextProvider; academico
  NO importa identidad (solo declara ProfesorConsultaPort); no
  PATCH/DELETE de Materia ni de asignaciones; no unicidad de nombre; no
  audit_log (ADR-0009 §3 punto 5); no FSD-UC-019 (GET
  /profesores/{id}/asignaciones) ni FSD-UC-015/020; no loguear
  nombreCompleto ni otros PII; no implementar puntos 2-5 de ADR-0009 §3.
```

### 1.4 Reasoning

```text
BACKEND
1. academico/ProfesorConsultaPort.java + ProfesorResumen.java (record
   {id, nombreCompleto}) en la RAIZ del modulo academico.
2. identidad/infrastructure/adapter/out/port/ProfesorConsultaPortImpl.java
   — consulta Usuario del tenant con rol PROFESOR y activo; implementa el
   puerto de academico (arista identidad -> academico, sin ciclo).
3. domain: MateriaId, Materia.crear(tenantId, nombre); AsignacionMateriaCurso
   / AsignacionMateriaProfesor con factories; excepciones
   MateriaNoEncontradaException (E_MATERIA_NO_ENCONTRADA),
   MateriaSinCursoException (E_MATERIA_SIN_CURSO),
   ParaleloNoEncontradoException (E_PARALELO_NO_ENCONTRADO),
   ProfesorNoEncontradoException (E_PROFESOR_NO_ENCONTRADO). Lombok solo
   @Getter. Mapear las nuevas DomainException en el handler existente
   (mismo patron que CursoNoEncontradoException / GestionEscolar*).
4. application/port/in y out + servicios transaccionales. CrearAsignacion
   CursoService valida Materia + Curso + Paralelo (paralelo.cursoId ==
   cursoId) ANTES de persistir. CrearAsignacionProfesorService exige
   asignacion curso previa (409 si no) y ProfesorConsultaPort
   (404 si no es profesor activo del tenant).
5. persistence: JpaEntity/Repository/Adapter + MateriaSpecifications (q
   sobre nombre). Filtro explicito por tenantId.
6. MateriaController + DTOs en adapter/in/rest. @PreAuthorize
   hasAnyRole('ADMIN','SECRETARIA'). GET /materias/profesores-disponibles
   como path literal (el {id} es UUID).
7. Delta CursoController: GET /cursos y GET /cursos/{id}/paralelos ->
   hasAnyRole('ADMIN','SECRETARIA'); POST siguen hasRole('ADMIN').
8. V7__academico_materia.sql: tres tablas tenant_id NOT NULL + RLS FORCE
   (patron V6). FKs a curso/paralelo/materia; profesor_id UUID (sin FK
   cruzada a usuario para no acoplar esquemas; integridad en aplicacion).
9. Tests: dominio; servicios Mockito (409 A1, 404 padres, cross-tenant);
   MateriaIntegrationTest Testcontainers; ModularityTests sigue 7/7.

FRONTEND
10. Ampliar role.guard.ts: si data.roles (string[]) existe, basta UN rol
    coincidente; si no, data.role (string) como hoy. Actualizar
    guards.spec.ts (SECRETARIA entra a roles:[ADMIN,SECRETARIA]; PROFESOR
    no). Rutas existentes con data.role NO se tocan.
11. materia.model.ts; materias-list.page.ts (q + paginacion, link al
    detalle); materia-create.page.ts (POST nombre); materia-detalle.page.ts
    (GET /materias/{id} para el titulo — NO query param; GET/POST
    asignaciones-curso con selects de cursos/paralelos; GET/POST
    asignaciones-profesor con select de profesores-disponibles y
    curso/paralelo restringido a los ya asignados).
12. app.routes.ts: /academico/materias[, /nuevo, /:id] con
    data.roles ['ADMIN','SECRETARIA'].
13. shell.component.ts: enlace "Materias" si ADMIN o SECRETARIA.
14. mvn test verde + ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST/GET de /materias (incl. GET por id y q/paginacion)
cumplen DD-UC-012 §2, (b) POST/GET asignaciones-curso validan Curso/Paralelo
del tenant, (c) POST asignaciones-profesor caso feliz y 409 E_MATERIA_SIN_CURSO
cuando falta la asignacion curso, (d) aislamiento cross-tenant es 404,
(e) ModularityTests en verde (identidad depende de academico; academico NO
importa identidad), (f) Admin o Secretaria autenticado ve /academico/materias,
crea una Materia, entra al detalle, asigna curso/paralelo y profesor,
(g) ng build en verde. No implementes PATCH/DELETE, FSD-UC-019/015/020,
audit_log, unicidad de nombre ni puntos 2-5 de ADR-0009 §3.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ y frontend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/academico/ProfesorConsultaPort.java
backend/src/main/java/com/edusync/academico/domain/**
backend/src/main/java/com/edusync/academico/application/**
backend/src/main/java/com/edusync/academico/infrastructure/**
backend/src/main/java/com/edusync/identidad/infrastructure/adapter/out/port/
  ProfesorConsultaPortImpl.java
backend/src/main/resources/db/migration/V7__academico_materia.sql
backend/src/test/java/com/edusync/academico/**
frontend/src/app/features/academico/materia.model.ts
frontend/src/app/features/academico/materias-list.page.ts
frontend/src/app/features/academico/materia-create.page.ts
frontend/src/app/features/academico/materia-detalle.page.ts
frontend/src/app/core/auth/role.guard.ts (delta)
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene de un query param/body del cliente — siempre de `TenantContextProvider`.
- `academico` **no** importa `identidad` ni `plataforma`. `ProfesorConsultaPort` vive en `academico`; la implementación vive en `identidad`.
- `POST .../asignaciones-profesor` **siempre** verifica una `AsignacionMateriaCurso` previa para el mismo `(cursoId, paraleloId)` — si no, `409 E_MATERIA_SIN_CURSO`.
- `Materia`, `AsignacionMateriaCurso` y `AsignacionMateriaProfesor` son Aggregates independientes — **no** FKs embebidas en `Materia`.
- Acceso cross-tenant responde `404`, nunca `403` ni datos parciales.
- `mvn test` (incluye `ModularityTests`) y `ng build` **deben** quedar en verde.
- Logs **MUST NOT** registrar `nombreCompleto`, email, ni tokens.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar; siempre del contexto de seguridad |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar; `ApplicationModules.verify()` debe fallar y bloquear el build |
| `E_MATERIA_SIN_CURSO_OMITIDO` | Se crea asignación de profesor sin exigir asignación curso previa | Rechazar; viola A1 de `FSD-UC-018` |
| `E_AGREGADO_EMBEBIDO_INVENTADO` | Se modeló curso/profesor como campos de `Materia` en vez de Aggregates de asignación | Revertir; contradice `DD-UC-012` §2/§3 |
| `E_FSD_UC_019_COLADO` | Se implementó `GET /profesores/{id}/asignaciones` o consola de Profesores | Revertir; es `FSD-UC-019` |
| `E_AUDIT_LOG_INVENTADO` | Se implementó `audit_log` sin resolver `ADR-0009` §3 punto 5 | Revertir |
| `E_PII_EN_LOG` | Se loguea `nombreCompleto` u otro PII | Rechazar (`AGENTS.md` §7) |
| `E_ROLE_GUARD_ROTO` | Las rutas existentes con `data.role` (string) dejan de funcionar | Rechazar; la ampliación a `data.roles` es aditiva |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: validar Materia + Curso + Paralelo (pertenencia paralelo→curso) antes de `AsignacionMateriaCurso`.
- MUST: `409 E_MATERIA_SIN_CURSO` cuando falte la asignación curso al asignar profesor.
- MUST: `ProfesorConsultaPort` en `academico`, implementación en `identidad`.
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST: acceso cross-tenant → `404`.
- MUST NOT: `PATCH`/`DELETE` de Materia o asignaciones.
- MUST NOT: implementar `FSD-UC-019`/`015`/`020`, `audit_log`, unicidad de nombre, ni `ADR-0009` §3 puntos 2–5.
- MUST NOT: modificar `docs/baseline/**`.
- MUST NOT: loguear PII.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-012` | PR-IMPL-012 | `dev-agent` | `academico` (`Materia`, asignaciones), `V7__academico_materia.sql`, `ProfesorConsultaPortImpl`, consola Angular de Materias |
| FSD | `FSD-UC-018` | PR-IMPL-012 | `dev-agent` | Tercer feature de negocio real del módulo `academico` (primer fullstack en un solo prompt) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-012` completo; backend de `PR-IMPL-001..011` disponible; un Curso con Paralelo "A" y un Usuario `PROFESOR` en el mismo tenant.
- **Output esperado**: `POST /materias {nombre:"Matemáticas"}` → `201`; `POST .../asignaciones-curso` → `201`; `POST .../asignaciones-profesor` → `201`; UI lista/crea/asigna; `mvn test` y `ng build` en verde.

### 6.2 Caso borde

- **Input**: `POST .../asignaciones-profesor` sin `POST .../asignaciones-curso` previo para ese `(cursoId, paraleloId)`.
- **Output esperado**: `409 E_MATERIA_SIN_CURSO`.

### 6.3 Caso adversarial

- **Input**: solicitud de modelar `curso_id`/`profesor_id` como columnas de `materia`, o de agregar `GET /profesores/{id}/asignaciones` "ya que estamos".
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-012` §3; no implementar sin un Design Doc de seguimiento.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-012.md` v1.0. Primer prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | Ejecución real: código backend + UI + tests + `V7`. `mvn test` 154/154; `ng build` verde. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
| Rodrigo Aspeti | 21/08/2026 | **ejecutado** | Código real generado y verificado (`mvn test` 154/154, `ng build` verde) |
