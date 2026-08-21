# PR-IMPL-010 — Académico: Cursos y Paralelos

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-010` |
| Título | Módulo `academico`: `Curso` y `Paralelo` (alta y listado) |
| Artefacto origen | `docs/design/DD-UC-010.md` |
| ID origen | `DD-UC-010` (`FSD-UC-017`) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 20/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Backend Engineer con experiencia en Java 25 / Spring Boot 4.1.0
(arquitectura hexagonal, Spring Data JPA, Spring Modulith) en el proyecto EduSync.
```

### 1.2 Task

```text
Implementa Curso y Paralelo segun docs/design/DD-UC-010.md §2 en el modulo
academico (ya contiene GestionEscolar de DD-UC-008/PR-IMPL-008): dominio
Curso + Paralelo (dos Aggregates independientes, sin estado); POST/GET
/api/v1/cursos (alta, listado con filtro q y paginacion reutilizando
shared.PageQuery/PageResult/PageResponse de DD-UC-007); POST/GET
/api/v1/cursos/{id}/paralelos (alta, listado simple sin paginacion).
Migracion Flyway V6 con tenant_id + RLS en ambas tablas.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-010.md (Curso y Paralelo como Aggregates
  independientes con repositorios propios, no un Curso con List<Paralelo>
  embebido; ver justificacion en DD-UC-010 §2/§3).
- FSD: docs/product/FSD.md §4.6.7 (FSD-UC-017). Solo declara dos POST; el
  GET de listado se añade por inferencia practica (mismo criterio que
  DD-UC-008 con GestionEscolar), sin contradecir el FSD.
- ADRs: ADR-0001 (RLS por tenant_id), ADR-0008 (stack vivo), ADR-0009
  (Curso/Paralelo son entidades de la generalizacion SaaS), ADR-0011
  (academico es modulo propio, shared es OPEN), ADR-0012 (Lombok allowlist
  en domain/, sin restriccion en infrastructure/application).
- Precedentes de codigo a replicar: academico/domain/GestionEscolar.java
  (Aggregate Root inmutable, factory crear(), DD-UC-008), academico/
  infrastructure/adapter/out/persistence/GestionEscolarRepositoryAdapter.java
  (filtro explicito por tenantId), academico/infrastructure/adapter/in/rest/
  ErrorResponse.java (reutilizar, no duplicar).
- Prerrequisito: PR-IMPL-001..009 ya ejecutados.
- Restricciones: tenantId SIEMPRE desde TenantContextProvider, nunca del
  body/query; POST /cursos/{id}/paralelos valida que el Curso exista y sea
  del tenant actual ANTES de crear el Paralelo (404 E_CURSO_NO_ENCONTRADO
  si no); no implementar PATCH/DELETE de Curso ni Paralelo; no implementar
  validacion de unicidad de nombre de Paralelo dentro de un Curso; no
  implementar audit_log (gobernanza pendiente, ADR-0009 §3 punto 5); no
  tocar identidad ni plataforma salvo lectura de shared (no resolver
  E_ASESOR_SIN_CURSO de FSD-UC-021 en este prompt); no implementar
  FSD-UC-018/020 (Materias, Estudiantes/Inscripciones); no implementar UI
  Angular.
```

### 1.4 Reasoning

```text
1. academico/domain/CursoId.java, ParaleloId.java (records UUID, mismo
   patron que GestionEscolarId).
2. academico/domain/Curso.java: constructor privado + factory
   Curso.crear(tenantId, nombre) -- valida nombre no en blanco; sin estado,
   sin metodo de transicion. Lombok @Getter/@EqualsAndHashCode/@ToString
   (allowlist domain/, ADR-0012).
3. academico/domain/Paralelo.java: constructor privado + factory
   Paralelo.crear(tenantId, cursoId, nombre) -- valida nombre no en blanco;
   sin estado.
4. academico/domain/CursoNoEncontradoException.java (extends DomainException,
   codigo E_CURSO_NO_ENCONTRADO, mismo patron que
   GestionEscolarNoEncontradaException).
5. application/port/in/{CrearCursoUseCase, ListarCursosUseCase,
   CrearParaleloUseCase, ListarParalelosUseCase, CursoFiltro}.java --
   CursoFiltro solo tiene campo q (Curso no tiene estado).
6. application/port/out/{CursoRepositoryPort, ParaleloRepositoryPort}.java --
   CursoRepositoryPort: guardar, buscarPorIdYTenant, listar(tenantId, filtro,
   pageQuery) -> PageResult<Curso>. ParaleloRepositoryPort: guardar,
   listarPorCursoYTenant(cursoId, tenantId) -> List<Paralelo> (sin paginar).
7. application/service/{CrearCurso, ListarCursos, CrearParalelo,
   ListarParalelos}Service.java -- CrearParaleloService busca el Curso por
   id+tenant (404 CursoNoEncontradoException si no existe o es de otro
   tenant) ANTES de invocar Paralelo.crear(...).
8. infrastructure/adapter/out/persistence/: CursoJpaEntity,
   CursoJpaRepository (+ JpaSpecificationExecutor), CursoSpecifications
   (package-private, Criteria API, mismo patron que
   GestionEscolarSpecifications), CursoRepositoryAdapter;
   ParaleloJpaEntity, ParaleloJpaRepository, ParaleloRepositoryAdapter
   (filtra explicitamente por tenantId, igual que el resto de adaptadores
   de academico).
9. infrastructure/adapter/in/rest/CursoController.java + DTOs
   (CrearCursoRequest, CrearParaleloRequest, CursoResponse,
   ParaleloResponse) -- @PreAuthorize("hasRole('ADMIN')") en los 4
   endpoints; reutiliza academico.infrastructure.adapter.in.rest.
   ErrorResponse (ya existe desde DD-UC-008, no duplicar).
10. backend/src/main/resources/db/migration/V6__academico_curso_paralelo.sql --
    tabla curso (id, tenant_id NOT NULL, nombre, creado_en) + RLS FORCE;
    tabla paralelo (id, tenant_id NOT NULL, curso_id FK, nombre, creado_en)
    + RLS FORCE; mismo patron de politica tenant_isolation que
    V5__academico_gestion_escolar.sql.
11. Tests: CursoTest/ParaleloTest (unit, dominio); CursoIntegrationTest
    (Testcontainers PostgreSQL 15) -- POST/GET cursos con filtro q y
    paginacion, POST/GET paralelos caso feliz, POST paralelo sobre curso
    inexistente o de otro tenant (404), aislamiento cross-tenant en ambos
    listados; ModularityTests debe seguir en verde.
12. mvn test en verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST/GET de /cursos funcionan segun los contratos de
DD-UC-010 §2, incluyendo filtro q y paginacion, (b) POST/GET de
/cursos/{id}/paralelos funcionan, validando la existencia del Curso padre
en el tenant actual, (c) el aislamiento cross-tenant devuelve 404 en ambos
recursos, (d) todos los tests pasan incluyendo ModularityTests. No
implementes PATCH/DELETE, unicidad de nombre de paralelo, audit_log,
E_ASESOR_SIN_CURSO en identidad, Materias, Estudiantes/Inscripciones ni UI
Angular.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/academico/domain/{Curso,CursoId,Paralelo,
  ParaleloId,CursoNoEncontradoException}.java
backend/src/main/java/com/edusync/academico/application/**
backend/src/main/java/com/edusync/academico/infrastructure/**
backend/src/main/resources/db/migration/V6__academico_curso_paralelo.sql
backend/src/test/java/com/edusync/academico/**
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene de un query param/body del cliente — siempre de `TenantContextProvider` (mismo invariante que `DD-UC-002`/`DD-UC-005`/`DD-UC-007`/`DD-UC-008`).
- `POST /cursos/{id}/paralelos` **siempre** valida que el `Curso` padre exista y pertenezca al tenant actual antes de crear el `Paralelo`.
- `Curso` y `Paralelo` son Aggregates independientes con repositorios propios — **no** un `Curso` con `List<Paralelo>` embebido (ver `DD-UC-010` §2/§3).
- `mvn test` **debe** quedar en verde, incluyendo `ModularityTests`.
- Acceso cross-tenant a un `Curso` o sus `Paralelo` responde `404`, nunca `403` ni datos parciales.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` como parámetro del cliente | Rechazar; `tenantId` siempre viene del contexto de seguridad |
| `E_PARALELO_SIN_VALIDAR_PADRE` | Se creó un `Paralelo` sin verificar que el `Curso` exista y sea del tenant actual | Rechazar; viola el invariante de integridad padre-hijo de `DD-UC-010` §2 |
| `E_AGREGADO_EMBEBIDO_INVENTADO` | Se modeló `Paralelo` como colección embebida dentro de `Curso` en vez de Aggregate independiente | Revertir; contradice la decisión explícita de `DD-UC-010` §2/§3 |
| `E_VALIDACION_UNICIDAD_INVENTADA` | Se implementó una validación de nombre único de `Paralelo` dentro de un `Curso` | Revertir; el FSD no la declara, está explícitamente fuera de alcance |
| `E_CICLO_MODULO` | `academico` importa directamente de `identidad`/`plataforma` (no vía `shared`) | Rechazar; `ApplicationModules.verify()` debe fallar y bloquear el build |
| `E_AUDIT_LOG_INVENTADO` | Se implementó `audit_log` para `Curso`/`Paralelo` sin resolver `ADR-0009` §3 punto 5 | Revertir; requiere una decisión de gobernanza previa |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: validar el `Curso` padre (existencia + tenant) antes de crear un `Paralelo`.
- MUST: `Curso` y `Paralelo` como Aggregates independientes, cada uno con su propio repositorio.
- MUST: `mvn test` en verde, incluyendo `ModularityTests` (sin ciclos nuevos).
- MUST: acceso cross-tenant → `404`.
- MUST NOT: implementar `PATCH`/`DELETE` de `Curso` ni `Paralelo`.
- MUST NOT: implementar validación de unicidad de nombre de `Paralelo`.
- MUST NOT: implementar `audit_log`, la validación `E_ASESOR_SIN_CURSO` en `identidad`, `FSD-UC-018`/`020`, ni UI Angular.
- MUST NOT: modificar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-010` | PR-IMPL-010 | `dev-agent` | `academico.{domain,application,infrastructure}` (`Curso`, `Paralelo`), `V6__academico_curso_paralelo.sql` |
| FSD | `FSD-UC-017` | PR-IMPL-010 | `dev-agent` | Segundo feature de negocio real del módulo `academico` |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-010` completo; backend de `PR-IMPL-001..009` disponible.
- **Output esperado**: `POST /cursos {nombre:"Primero de Primaria"}` → `201`; `POST /cursos/{id}/paralelos {nombre:"A"}` → `201`; `GET /cursos/{id}/paralelos` → `200` con el paralelo creado; `mvn test` en verde.

### 6.2 Caso borde

- **Input**: `POST /cursos/{id}/paralelos` con un `{id}` de `Curso` que pertenece a otro tenant.
- **Output esperado**: `404 E_CURSO_NO_ENCONTRADO` (no `403` ni filtración de datos del otro tenant).

### 6.3 Caso adversarial

- **Input**: solicitud de modelar `Paralelo` como una lista embebida dentro de `Curso`, o de agregar validación de nombre único de paralelo "para evitar duplicados".
- **Comportamiento esperado**: rechazo — ambas decisiones están explícitamente descartadas en `DD-UC-010` §3 (alternativas B y D); no implementar sin un Design Doc de seguimiento que lo justifique.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 20/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-010.md` v1.0. Segundo prompt de implementación del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 20/08/2026 | Rodrigo Aspeti | **Ejecución real**: `Curso`/`Paralelo` (dominio, aplicación, infraestructura), `CursoController`, `V6__academico_curso_paralelo.sql`, 15 tests nuevos. `mvn test` → **134/134** verde (incluye `ModularityTests` 7/7). Ningun failure mode de §3 se materializó. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 20/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
| Rodrigo Aspeti | 20/08/2026 | **aprobado (ejecución)** | `mvn test` 134/134 verde; `FSD-UC-017` completo en backend |
