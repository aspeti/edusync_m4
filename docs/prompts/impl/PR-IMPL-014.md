# PR-IMPL-014 — Académico: Profesores (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-014` |
| Título | Módulo `academico`: consola de Profesores (consulta inversa de asignaciones) |
| Artefacto origen | `docs/design/DD-UC-014.md` |
| ID origen | `DD-UC-014` (`FSD-UC-019`) |
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
Implementa FSD-UC-019 segun docs/design/DD-UC-014.md §2, backend Y frontend
en el mismo prompt:

Backend: NO crear entidad/tabla Profesor. Extender academico.ProfesorConsultaPort
(buscarPorIdYTenant + listarDelTenant paginado) e implementarlo en
identidad.ProfesorConsultaPortImpl. GET /api/v1/profesores (q+activo+paginacion),
GET /profesores/{id}, GET /profesores/{id}/asignaciones (lista simple enriquecida
con nombres de Materia/Curso/Paralelo). 404 E_PROFESOR_NO_ENCONTRADO si no existe,
otro tenant, o el usuario no tiene rol PROFESOR. Profesor inactivo CON rol:
GET asignaciones responde 200. Sin Flyway (no hay tabla nueva).

Frontend: consola Angular lista + detalle de solo lectura; rutas
/academico/profesores[, /:id] con data.roles ['ADMIN','SECRETARIA']; enlace
"Profesores" en el shell. ADMIN ve enlace a /usuarios/nuevo; SECRETARIA no.
NO hay POST de asignaciones en esta consola.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-014.md (perfil de Usuario, no Aggregate nuevo;
  consulta inversa de AsignacionMateriaProfesor; ver §2/§3).
- FSD: docs/product/FSD.md §4.6.9 (FSD-UC-019). Declara POST /usuarios con
  PROFESOR (ya cubierto por FSD-UC-021) y GET /profesores/{id}/asignaciones.
  GET listado/detalle se anaden por inferencia practica (mismo criterio que
  DD-UC-013), sin contradecir el FSD.
- ADRs: ADR-0001 (RLS), ADR-0008 (stack vivo), ADR-0009 (Profesor es rol de
  la generalizacion SaaS), ADR-0010 (multi-rol), ADR-0011 (academico modulo
  propio), ADR-0012 (Lombok allowlist).
- Precedentes backend: ProfesorConsultaPort / ProfesorConsultaPortImpl
  (DD-UC-012), AsignacionMateriaProfesorRepositoryPort.listarPorMateriaYTenant,
  ProfesorNoEncontradoException (404 E_PROFESOR_NO_ENCONTRADO, ya existe),
  shared.PageQuery/PageResult/web.PageResponse (DD-UC-007), UsuarioFiltro
  (q/activo/rol). GET /materias/profesores-disponibles NO se rompe: su DTO
  REST sigue {id, nombreCompleto}.
- Precedentes frontend: features/academico/estudiantes-list.page.ts (filtros
  + paginacion); estudiante-detalle.page.ts (GET /{id} para titulo).
  role.guard.ts ya acepta data.roles (DD-UC-012) — NO reescribirlo.
- Prerrequisito: PR-IMPL-001..013 ya ejecutados (asignaciones profesor y
  ProfesorConsultaPort disponibles).
- Restricciones: tenantId SIEMPRE desde TenantContextProvider; academico
  NO importa identidad ni plataforma; no POST /profesores; no PATCH/DELETE;
  no alta de asignaciones aqui (FSD-UC-018); no tabla Profesor; no Flyway;
  no FSD-UC-015/001; no audit_log (ADR-0009 §3 punto 5); no loguear
  nombreCompleto ni email; no implementar puntos 1-5 de ADR-0009 §3.
```

### 1.4 Reasoning

```text
BACKEND
1. ProfesorResumen: anadir boolean activo. Actualizar listarActivosDelTenant
   para pasar activo=true. NO cambiar el DTO REST de
   GET /materias/profesores-disponibles (sigue {id, nombreCompleto}).
2. ProfesorConsultaPort: buscarPorIdYTenant (rol PROFESOR, mismo tenant,
   activo o no); listarDelTenant(tenantId, ProfesorFiltro, PageQuery).
   ProfesorConsultaPortImpl delega a UsuarioRepositoryPort.listarPorTenant
   con UsuarioFiltro(q, activo, Rol.PROFESOR). academico NO importa
   identidad: ProfesorFiltro vive en academico.application.port.in.
3. AsignacionMateriaProfesorRepositoryPort + JpaRepository:
   listarPorProfesorYTenant / findByProfesorIdAndTenantId.
4. Servicios: ListarProfesoresService, ObtenerProfesorService,
   ListarAsignacionesPorProfesorService (verifica profesor ANTES de listar;
   enriquece nombres via Materia/Curso/Paralelo ports). Reusar
   ProfesorNoEncontradoException.
5. ProfesorController GET /profesores, GET /{id}, GET /{id}/asignaciones.
   DTOs en adapter/in/rest. @PreAuthorize hasAnyRole('ADMIN','SECRETARIA').
6. Sin V9. Sin cambios a UsuarioController.
7. Tests: servicios Mockito (404, inactivo 200, cross-tenant);
   ProfesorIntegrationTest Testcontainers (lista, detalle, asignaciones
   enriquecidas, no romper profesores-disponibles);
   ModularityTests sigue 7/7 (sin arista nueva).

FRONTEND
8. profesor.model.ts; profesores-list.page.ts (q + activo + paginacion,
   link al detalle; ADMIN: enlace a /usuarios/nuevo);
   profesor-detalle.page.ts (GET /profesores/{id} para titulo — NO query
   param; tabla de asignaciones de solo lectura).
9. app.routes.ts: /academico/profesores[, /:id] con
   data.roles ['ADMIN','SECRETARIA']. NO modificar role.guard.ts.
10. shell.component.ts: enlace "Profesores" si ADMIN o SECRETARIA.
11. mvn test verde + ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) GET /profesores (q/activo/paginacion) y GET /{id}
cumplen DD-UC-014 §2, (b) GET /profesores/{id}/asignaciones valida al
profesor del tenant y devuelve Materia/Curso/Paralelo con nombres,
(c) usuario sin rol PROFESOR u otro tenant → 404 E_PROFESOR_NO_ENCONTRADO,
(d) profesor inactivo con rol → 200 en asignaciones, (e) GET
/materias/profesores-disponibles sigue {id, nombreCompleto},
(f) ModularityTests en verde (academico sigue sin importar identidad/
plataforma), (g) Admin o Secretaria autenticado ve /academico/profesores
y el detalle con asignaciones de solo lectura; Admin ve el enlace de
alta a Usuarios y Secretaria no, (h) ng build en verde. No implementes
POST /profesores, PATCH/DELETE, alta de asignaciones en esta consola,
tabla Profesor, Flyway, FSD-UC-015/001, ni audit_log.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ y frontend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/academico/ProfesorConsultaPort.java (delta)
backend/src/main/java/com/edusync/academico/ProfesorResumen.java (delta)
backend/src/main/java/com/edusync/academico/application/**
backend/src/main/java/com/edusync/academico/infrastructure/adapter/in/rest/ProfesorController.java
backend/src/main/java/com/edusync/identidad/infrastructure/adapter/out/port/ProfesorConsultaPortImpl.java (delta)
backend/src/test/java/com/edusync/academico/**
frontend/src/app/features/academico/profesor.model.ts
frontend/src/app/features/academico/profesores-list.page.ts
frontend/src/app/features/academico/profesor-detalle.page.ts
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene de un query param/body del cliente — siempre de `TenantContextProvider`.
- `academico` **no** importa `identidad` ni `plataforma`.
- No existe tabla ni Aggregate `Profesor`; `profesorId` es `Usuario.id`.
- Acceso cross-tenant o usuario sin rol `PROFESOR` responde `404`, nunca `403` ni datos parciales.
- `GET /materias/profesores-disponibles` conserva el contrato `{id, nombreCompleto}`.
- `mvn test` (incluye `ModularityTests`) y `ng build` **deben** quedar en verde.
- Logs **MUST NOT** registrar `nombreCompleto`, email ni tokens.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar; siempre del contexto de seguridad |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar; `ApplicationModules.verify()` debe fallar y bloquear el build |
| `E_TABLA_PROFESOR_INVENTADA` | Se creó entidad/migración `profesor` | Revertir; contradice `DD-UC-014` §2/§3 |
| `E_POST_PROFESOR_INVENTADO` | Se implementó `POST /profesores` | Revertir; el alta es `FSD-UC-021` |
| `E_ALTA_ASIGNACION_COLADA` | Se POST-ea asignación desde `/profesores` | Revertir; escritura permanece en `FSD-UC-018` |
| `E_CATALOGO_ROTO` | `GET /materias/profesores-disponibles` cambió de shape | Revertir; viola `DD-UC-012` |
| `E_PII_EN_LOG` | Se loguea `nombreCompleto` o email | Rechazar (`AGENTS.md` §7) |
| `E_AUDIT_LOG_INVENTADO` | Se implementó `audit_log` sin resolver `ADR-0009` §3 punto 5 | Revertir |
| `E_PATCH_INVENTADO` | Se implementó `PATCH`/`DELETE` de profesor | Revertir |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` aunque ya acepta `data.roles` | Rechazar; solo usarlo |
| `E_FLYWAY_INVENTADO` | Se añadió `V9` sin tabla nueva justificada | Revertir |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: verificar profesor del tenant (rol `PROFESOR`) **antes** de listar asignaciones.
- MUST: `404 E_PROFESOR_NO_ENCONTRADO` (no 403) para otro tenant o usuario sin el rol.
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST NOT: crear tabla/Aggregate `Profesor` ni `POST /profesores`.
- MUST NOT: alta de asignaciones en esta consola.
- MUST NOT: implementar `FSD-UC-015`/`001`, `audit_log`, ni `ADR-0009` §3 puntos 1–5.
- MUST NOT: modificar `docs/baseline/**`.
- MUST NOT: loguear PII (`nombreCompleto`, email).
- MUST NOT: reescribir `role.guard.ts`.
- MUST NOT: romper `GET /materias/profesores-disponibles`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-014` | PR-IMPL-014 | `dev-agent` | `ProfesorController`, extensión `ProfesorConsultaPort`, consola Angular de Profesores |
| FSD | `FSD-UC-019` | PR-IMPL-014 | `dev-agent` | Quinto feature de negocio real del módulo `academico` (tercer fullstack en un solo prompt) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-014` completo; backend de `PR-IMPL-001..013` disponible; un `Usuario` `PROFESOR` y una `AsignacionMateriaProfesor` del mismo tenant.
- **Output esperado**: `GET /profesores` lista al profesor; `GET /profesores/{id}/asignaciones` → `200` con materia/curso/paralelo y nombres; UI lista/abre detalle de solo lectura; `mvn test` y `ng build` en verde.

### 6.2 Caso borde

- **Input**: `GET /profesores/{id}/asignaciones` de un `PROFESOR` con `activo = false`.
- **Output esperado**: `200` (historial visible). `POST` de una asignación nueva sobre ese id sigue siendo 404 vía `FSD-UC-018` (`esProfesorActivoDelTenant`).

### 6.3 Caso adversarial

- **Input**: solicitud de crear tabla `profesor`, `POST /profesores`, o formulario de alta de asignaciones "ya que estamos".
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-014` §3; no implementar sin un Design Doc de seguimiento.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-014.md` v1.0. Tercer prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | **Ejecutado**: código real de Profesores (extensión `ProfesorConsultaPort`, `ProfesorController`, consola Angular lista/detalle). `mvn test` 184/184 (incluye `ModularityTests` 7/7); `ng build` verde. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
| Rodrigo Aspeti | 21/08/2026 | **ejecutado** | Código real generado; `mvn test` 184/184; `ng build` verde |
