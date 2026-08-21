# PR-IMPL-013 — Académico: Estudiantes e Inscripciones (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-013` |
| Título | Módulo `academico`: `Estudiante` e `Inscripcion` (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-013.md` |
| ID origen | `DD-UC-013` (`FSD-UC-020`) |
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
Implementa FSD-UC-020 segun docs/design/DD-UC-013.md §2, backend Y frontend
en el mismo prompt:

Backend (modulo academico): dominio Estudiante + Inscripcion (dos Aggregates
independientes). POST/GET /api/v1/estudiantes (alta con rude obligatorio
unico por tenant, listado q+estado+paginacion, GET por id);
GET /api/v1/estudiantes/{id}/inscripciones (historial, lista simple);
POST /api/v1/inscripciones (estado ACTIVA; 409 E_INSCRIPCION_DUPLICADA si
ya existe el mismo estudiante en la misma GestionEscolar).
Migracion Flyway V8 con tenant_id + RLS en ambas tablas.
Delta RBAC: GET /gestiones-escolares tambien SECRETARIA (POST/PATCH siguen
ADMIN). GET de cursos/paralelos ya admite SECRETARIA (no tocarlo).

Frontend: consola Angular lista/alta/detalle de Estudiantes con
inscripciones inline; rutas /academico/estudiantes[, /nuevo, /:id] con
data.roles ['ADMIN','SECRETARIA']; enlace "Estudiantes" en el shell.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-013.md (dos Aggregates independientes; rude
  obligatorio unico por tenant — BR-004; ver §2/§3).
- FSD: docs/product/FSD.md §4.6.10 (FSD-UC-020). Declara dos POST + A1 409
  E_INSCRIPCION_DUPLICADA. Los GET de listado/detalle/historial, rude y
  E_RUDE_DUPLICADO se anaden por inferencia practica / invariante BR-004
  (mismo criterio que DD-UC-008/010/012), sin contradecir el FSD.
- ADRs: ADR-0001 (RLS), ADR-0008 (stack vivo), ADR-0009 (Estudiante/
  Inscripcion son entidades de la generalizacion SaaS), ADR-0011
  (academico modulo propio), ADR-0012 (Lombok allowlist en domain/).
- Precedentes backend: academico/domain/Materia.java (Aggregate inmutable,
  factory crear(), solo @Getter), MateriaController (DTOs en adapter/in/rest
  sin subpaquete dto/, @ExceptionHandler 404 vs 409), Estudiante NO es
  Usuario — tabla propia. Reutilizar GestionEscolarNoEncontradaException,
  CursoNoEncontradoException, ParaleloNoEncontradoException.
- Precedentes frontend: features/academico/materia-detalle.page.ts
  (detalle + alta inline); core/api/page-response.model.ts; role.guard.ts
  ya acepta data.roles (DD-UC-012) — NO reescribirlo, solo usarlo.
- Prerrequisito: PR-IMPL-001..012 ya ejecutados (GestionEscolar, Curso/
  Paralelo y roleGuard aditivo disponibles).
- Restricciones: tenantId SIEMPRE desde TenantContextProvider; academico
  NO importa identidad ni plataforma; no PATCH/DELETE de Estudiante ni
  Inscripcion; no FSD-UC-019/001/006; no audit_log (ADR-0009 §3 punto 5);
  no loguear rude, nombreCompleto ni datosPersonales; no interpolar rude
  en mensajes de excepcion; no implementar puntos 1-5 de ADR-0009 §3
  (incluir rude NO es esa reconciliacion).
```

### 1.4 Reasoning

```text
BACKEND
1. domain: EstudianteId, EstadoEstudiante (ACTIVO, INACTIVO),
   Estudiante.crear(tenantId, rude, nombreCompleto, estado, datosPersonales);
   InscripcionId, EstadoInscripcion (ACTIVA, RETIRADA, TRANSFERIDA),
   Inscripcion.crear(...) siempre ACTIVA. Excepciones
   EstudianteNoEncontradoException (E_ESTUDIANTE_NO_ENCONTRADO),
   RudeDuplicadoException (E_RUDE_DUPLICADO, mensaje SIN el valor),
   InscripcionDuplicadaException (E_INSCRIPCION_DUPLICADA). Lombok solo
   @Getter. Mapear en el handler del controlador (404 vs 409).
2. application/port/in y out + servicios transaccionales.
   CrearEstudianteService rechaza rude duplicado del tenant (409).
   CrearInscripcionService valida Estudiante + GestionEscolar + Curso +
   Paralelo (paralelo.cursoId == cursoId) ANTES de persistir; rechaza
   duplicado (estudiante, gestion) del tenant (409 A1).
3. persistence: JpaEntity/Repository/Adapter + EstudianteSpecifications
   (q sobre nombreCompleto contains OR rude exacto case-insensitive;
   filtro estado). Filtro explicito por tenantId.
4. EstudianteController (POST/GET /estudiantes, GET /{id}, GET
   /{id}/inscripciones) + InscripcionController (POST /inscripciones).
   DTOs en adapter/in/rest. @PreAuthorize hasAnyRole('ADMIN','SECRETARIA').
5. Delta GestionEscolarController: GET listado ->
   hasAnyRole('ADMIN','SECRETARIA'); POST y PATCH siguen hasRole('ADMIN').
6. V8__academico_estudiante_inscripcion.sql: dos tablas tenant_id NOT NULL
   + RLS FORCE (patron V6/V7). UNIQUE (tenant_id, rude);
   UNIQUE (tenant_id, estudiante_id, gestion_escolar_id). FKs a
   estudiante, gestion_escolar, curso, paralelo.
7. Tests: dominio; servicios Mockito (409 A1, 409 rude, 404 padres,
   cross-tenant); EstudianteIntegrationTest Testcontainers;
   ModularityTests sigue 7/7 (sin arista nueva de modulos).

FRONTEND
8. estudiante.model.ts; estudiantes-list.page.ts (q + estado + paginacion,
   link al detalle); estudiante-create.page.ts (POST rude + nombreCompleto
   + estado default ACTIVO; SIN editor JSON de datosPersonales);
   estudiante-detalle.page.ts (GET /estudiantes/{id} para titulo — NO query
   param; GET inscripciones; form inline POST /inscripciones con selects
   de gestiones, cursos y paralelos + fecha).
9. app.routes.ts: /academico/estudiantes[, /nuevo, /:id] con
   data.roles ['ADMIN','SECRETARIA']. NO modificar role.guard.ts.
10. shell.component.ts: enlace "Estudiantes" si ADMIN o SECRETARIA.
11. mvn test verde + ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST/GET de /estudiantes (incl. GET por id, q/estado/
paginacion y 409 E_RUDE_DUPLICADO) cumplen DD-UC-013 §2, (b) POST
/inscripciones valida padres del tenant y persiste estado ACTIVA,
(c) segunda inscripcion del mismo estudiante en la misma GestionEscolar
devuelve 409 E_INSCRIPCION_DUPLICADA, (d) aislamiento cross-tenant es 404,
(e) ModularityTests en verde (academico sigue sin importar identidad/
plataforma), (f) Admin o Secretaria autenticado ve /academico/estudiantes,
crea un Estudiante con RUDE, entra al detalle e inscribe en una gestion/
curso/paralelo, y una segunda gestion queda consultable en el historial,
(g) ng build en verde. No implementes PATCH/DELETE, FSD-UC-019/001/006,
audit_log, editor JSON de datosPersonales, ni puntos 1-5 de ADR-0009 §3.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ y frontend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/academico/domain/**
backend/src/main/java/com/edusync/academico/application/**
backend/src/main/java/com/edusync/academico/infrastructure/**
backend/src/main/resources/db/migration/V8__academico_estudiante_inscripcion.sql
backend/src/test/java/com/edusync/academico/**
frontend/src/app/features/academico/estudiante.model.ts
frontend/src/app/features/academico/estudiantes-list.page.ts
frontend/src/app/features/academico/estudiante-create.page.ts
frontend/src/app/features/academico/estudiante-detalle.page.ts
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene de un query param/body del cliente — siempre de `TenantContextProvider`.
- `academico` **no** importa `identidad` ni `plataforma`.
- `rude` es obligatorio y único por tenant. Conflicto → `409 E_RUDE_DUPLICADO` **sin** interpolar el valor en mensaje ni logs.
- `POST /inscripciones` **siempre** verifica unicidad `(estudianteId, gestionEscolarId)` del tenant — si ya existe, `409 E_INSCRIPCION_DUPLICADA`.
- `Estudiante` e `Inscripcion` son Aggregates independientes — **no** una colección embebida.
- Acceso cross-tenant responde `404`, nunca `403` ni datos parciales.
- `mvn test` (incluye `ModularityTests`) y `ng build` **deben** quedar en verde.
- Logs **MUST NOT** registrar `rude`, `nombreCompleto`, `datosPersonales`, email ni tokens.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar; siempre del contexto de seguridad |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar; `ApplicationModules.verify()` debe fallar y bloquear el build |
| `E_INSCRIPCION_DUPLICADA_OMITIDA` | Se permite una segunda inscripción del mismo estudiante en la misma Gestión Escolar | Rechazar; viola A1 de `FSD-UC-020` |
| `E_RUDE_AUSENTE` | Se persiste `Estudiante` sin `rude` o con `rude` nullable | Rechazar; viola `BR-004`/`RB-01` y `DD-UC-013` §2 |
| `E_PII_EN_LOG` | Se loguea `rude`, `nombreCompleto` u otro PII; o el mensaje de `E_RUDE_DUPLICADO` interpola el código | Rechazar (`AGENTS.md` §7) |
| `E_AGREGADO_EMBEBIDO_INVENTADO` | Se modeló `Inscripcion` como colección dentro de `Estudiante` | Revertir; contradice `DD-UC-013` §2/§3 |
| `E_FSD_UC_019_COLADO` | Se implementó `GET /profesores/{id}/asignaciones` o consola de Profesores | Revertir; es `FSD-UC-019` |
| `E_AUDIT_LOG_INVENTADO` | Se implementó `audit_log` sin resolver `ADR-0009` §3 punto 5 | Revertir |
| `E_PATCH_INVENTADO` | Se implementó `PATCH`/`DELETE` de Estudiante o Inscripcion | Revertir; recorte explícito de `DD-UC-013` |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` aunque ya acepta `data.roles` | Rechazar; solo usarlo |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: `rude` NOT NULL, unique `(tenant_id, rude)`, `409 E_RUDE_DUPLICADO` sin interpolar el valor.
- MUST: validar Estudiante + GestionEscolar + Curso + Paralelo (pertenencia paralelo→curso) antes de `Inscripcion`.
- MUST: `409 E_INSCRIPCION_DUPLICADA` cuando ya exista `(estudiante, gestionEscolar)` del tenant.
- MUST: `Inscripcion` nace `ACTIVA`; el body de POST no acepta `estado`.
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST: acceso cross-tenant → `404`.
- MUST NOT: `PATCH`/`DELETE` de Estudiante o Inscripcion.
- MUST NOT: implementar `FSD-UC-019`/`001`/`006`, `audit_log`, editor JSON de `datosPersonales`, ni `ADR-0009` §3 puntos 1–5.
- MUST NOT: modificar `docs/baseline/**`.
- MUST NOT: loguear PII (`rude`, nombre, datos personales).
- MUST NOT: reescribir `role.guard.ts`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-013` | PR-IMPL-013 | `dev-agent` | `academico` (`Estudiante`, `Inscripcion`), `V8__academico_estudiante_inscripcion.sql`, consola Angular de Estudiantes |
| FSD | `FSD-UC-020` | PR-IMPL-013 | `dev-agent` | Cuarto feature de negocio real del módulo `academico` (segundo fullstack en un solo prompt) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-013` completo; backend de `PR-IMPL-001..012` disponible; una `GestionEscolar` y un `Curso` con Paralelo "A" en el mismo tenant.
- **Output esperado**: `POST /estudiantes {rude, nombreCompleto}` → `201` (sin inscripción); `POST /inscripciones` → `201` con `estado=ACTIVA`; `GET /estudiantes/{id}/inscripciones` lista esa fila; UI lista/crea/inscribe; `mvn test` y `ng build` en verde.

### 6.2 Caso borde

- **Input**: segundo `POST /inscripciones` del mismo `estudianteId` y `gestionEscolarId`.
- **Output esperado**: `409 E_INSCRIPCION_DUPLICADA`.

### 6.3 Caso adversarial

- **Input**: solicitud de omitir `rude`, de embeber inscripciones en `Estudiante`, o de agregar `PATCH` estado / `GET /profesores/{id}/asignaciones` "ya que estamos".
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-013` §3; no implementar sin un Design Doc de seguimiento.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-013.md` v1.0. Segundo prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | **Ejecutado**: código real de Estudiantes e Inscripciones (backend hexagonal + consola Angular). `mvn test` 173/173 (incluye `ModularityTests` 7/7); `ng build` verde. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
| Rodrigo Aspeti | 21/08/2026 | **ejecutado** | Código real generado; `mvn test` 173/173; `ng build` verde |
