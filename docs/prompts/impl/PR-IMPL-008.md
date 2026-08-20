# PR-IMPL-008 — Académico: Gestión Escolar (alta, listado y ciclo de estado)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-008` |
| Título | Módulo `academico`: `GestionEscolar` (alta, listado con filtros/paginación, ciclo de estado) |
| Artefacto origen | `docs/design/DD-UC-008.md` |
| ID origen | `DD-UC-008` (`FSD-UC-012`) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 20/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** — `mvn test` 119/119 verde (incluye `ModularityTests` 7/7) |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Backend Engineer con experiencia en Java 25 / Spring Boot 4.1.0
(arquitectura hexagonal, Spring Data JPA, Spring Modulith) en el proyecto EduSync.
```

### 1.2 Task

```text
Implementa GestionEscolar segun docs/design/DD-UC-008.md §2 en el modulo
academico (hoy vacio, solo package-info.java): dominio GestionEscolar +
EstadoGestionEscolar; POST /api/v1/gestiones-escolares; GET
/api/v1/gestiones-escolares con filtros (q sobre nombre, estado) y
paginacion (reutilizando shared.PageQuery/PageResult/PageResponse de
DD-UC-007); PATCH /api/v1/gestiones-escolares/{id}/estado con las
transiciones PLANIFICACION->ACTIVA, ACTIVA->CERRADA, ACTIVA->PLANIFICACION.
Migracion Flyway V5 con tenant_id + RLS.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-008.md (GestionEscolar Aggregate Root inmutable,
  mismo patron que Usuario/Tenant; filtros/paginacion reutilizan el patron
  de DD-UC-007 sin modificarlo).
- FSD: docs/product/FSD.md §4.6.2 (FSD-UC-012). El paso 3 del flujo principal
  ("activar una vez configurados periodos y secciones") NO es una validacion
  bloqueante en este prompt (no hay excepcion A2 en el FSD para esto) --
  no la implementes; se revisa cuando exista FSD-UC-013/014.
- ADRs: ADR-0001 (RLS por tenant_id), ADR-0008 (stack vivo), ADR-0009
  (GestionEscolar es una entidad de la generalizacion SaaS), ADR-0011
  (academico es modulo propio, shared es OPEN), ADR-0012 (Lombok allowlist
  en domain/, sin restriccion en infrastructure/application).
- Precedentes de codigo a replicar: identidad/domain/Usuario.java (Aggregate
  Root inmutable), plataforma/domain/Tenant.java (entidad con transiciones
  de estado controladas), shared.{PageQuery,PageResult,web.PageResponse}
  (DD-UC-007), identidad/infrastructure/adapter/out/persistence/
  UsuarioSpecifications.java (patron de Specification/JpaSpecificationExecutor).
- Prerrequisito: PR-IMPL-001..007 ya ejecutados.
- Restricciones: tenantId SIEMPRE desde TenantContextProvider, nunca del
  body/query; no implementar audit_log (gobernanza pendiente, ADR-0009 §3
  punto 5); no tocar identidad ni plataforma salvo lectura de shared; no
  implementar FSD-UC-013..020 (periodos, secciones, cursos, materias,
  estudiantes); no implementar UI Angular.
```

### 1.4 Reasoning

```text
1. academico/domain/EstadoGestionEscolar.java (enum PLANIFICACION/ACTIVA/CERRADA).
2. academico/domain/GestionEscolar.java: constructor privado + factory
   GestionEscolar.crear(tenantId, nombre, fechaInicio, fechaFin) -- valida
   fechaFin > fechaInicio (FechasInvalidasException); metodo de dominio
   cambiarEstado(EstadoGestionEscolar) -- valida transiciones permitidas
   (EstadoGestionEscolarInvalidoException en caso contrario); Lombok
   @Getter/@EqualsAndHashCode/@ToString (allowlist domain/, ADR-0012).
3. application/port/in/{CrearGestionEscolarUseCase, ListarGestionesEscolaresUseCase,
   CambiarEstadoGestionEscolarUseCase, GestionEscolarFiltro}.java.
4. application/port/out/GestionEscolarRepositoryPort.java (metodos: guardar,
   buscarPorIdYTenant, listar(tenantId, filtro, pageQuery) -> PageResult<GestionEscolar>).
5. application/service/{Crear,Listar,CambiarEstado}GestionEscolarService.java --
   CambiarEstadoGestionEscolarService busca por id+tenant (404 si no existe o
   es de otro tenant) antes de invocar GestionEscolar.cambiarEstado(...).
6. infrastructure/adapter/out/persistence/: GestionEscolarJpaEntity,
   GestionEscolarJpaRepository (+ JpaSpecificationExecutor),
   GestionEscolarSpecifications (package-private, Criteria API, mismo patron
   que UsuarioSpecifications/TenantSpecifications), GestionEscolarRepositoryAdapter.
7. infrastructure/adapter/in/rest/GestionEscolarController.java + DTOs
   (CrearGestionEscolarRequest, CambiarEstadoGestionEscolarRequest,
   GestionEscolarResponse) -- @PreAuthorize("hasRole('ADMIN')") en los 3 endpoints.
8. backend/src/main/resources/db/migration/V5__academico_gestion_escolar.sql --
   tabla gestion_escolar (id, tenant_id NOT NULL, nombre, fecha_inicio,
   fecha_fin, estado, creado_en) + RLS FORCE + politica tenant_isolation
   (sin el caso especial "OR tenant_id IS NULL" de usuario, porque toda
   GestionEscolar pertenece a un tenant).
9. Tests: GestionEscolarTest (unit, dominio); GestionEscolarIntegrationTest
   (Testcontainers PostgreSQL 15) -- POST feliz, A1 fechas invalidas, GET con
   filtros/paginacion, PATCH estado feliz + transicion invalida, aislamiento
   cross-tenant (404); ModularityTests debe seguir en verde (academico sin
   depender de identidad/plataforma mas alla de shared).
10. mvn test en verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST/GET/PATCH de /gestiones-escolares funcionan segun
los contratos de DD-UC-008 §2, (b) GET acepta q/estado/page/size y responde
PageResponse<GestionEscolarResponse>, (c) las transiciones de estado validas
e invalidas se comportan segun §2, (d) el aislamiento cross-tenant devuelve
404, (e) todos los tests pasan incluyendo ModularityTests 7/7. No
implementes periodos, secciones, cursos, materias, estudiantes, audit_log
ni UI Angular.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/academico/domain/{GestionEscolar,EstadoGestionEscolar,
  FechasInvalidasException,EstadoGestionEscolarInvalidoException}.java
backend/src/main/java/com/edusync/academico/application/**
backend/src/main/java/com/edusync/academico/infrastructure/**
backend/src/main/resources/db/migration/V5__academico_gestion_escolar.sql
backend/src/test/java/com/edusync/academico/**
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene de un query param/body del cliente — siempre de `TenantContextProvider` (mismo invariante que `DD-UC-002`/`DD-UC-005`/`DD-UC-007`).
- Ninguna transición de estado se ejecuta fuera de `GestionEscolar.cambiarEstado(...)` (dominio), nunca vía un setter directo o SQL ad-hoc.
- La precondición "periodos/secciones configurados antes de `ACTIVA`" **no** se implementa en este prompt (diferida explícitamente, ver `DD-UC-008` §2).
- `mvn test` **debe** quedar en verde, incluyendo `ModularityTests`.
- Acceso cross-tenant a una `GestionEscolar` ajena responde `404`, nunca `403` ni datos parciales.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` como parámetro del cliente | Rechazar; `tenantId` siempre viene del contexto de seguridad |
| `E_VALIDACION_PERIODOS_ANTICIPADA` | Se implementó una validación bloqueante de periodos/secciones para `ACTIVA` | Revertir; está explícitamente diferida a `FSD-UC-013`/`014` |
| `E_SETTER_DIRECTO` | Se añadió un setter público de `estado` en `GestionEscolar` en vez de `cambiarEstado(...)` | Rechazar; viola el patrón de Aggregate Root inmutable |
| `E_CICLO_MODULO` | `academico` importa directamente de `identidad`/`plataforma` (no vía `shared`) | Rechazar; `ApplicationModules.verify()` debe fallar y bloquear el build |
| `E_AUDIT_LOG_INVENTADO` | Se implementó `audit_log` para `GestionEscolar` sin resolver `ADR-0009` §3 punto 5 | Revertir; requiere una decisión de gobernanza previa |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: transiciones de estado solo vía `GestionEscolar.cambiarEstado(...)`.
- MUST: `mvn test` en verde, incluyendo `ModularityTests` (sin ciclos nuevos).
- MUST: acceso cross-tenant → `404`.
- MUST NOT: implementar la validación de periodos/secciones para `ACTIVA`.
- MUST NOT: implementar `audit_log`, UI Angular, ni `FSD-UC-013`..`020`.
- MUST NOT: modificar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-008` | PR-IMPL-008 | `dev-agent` | `academico.{domain,application,infrastructure}` (`GestionEscolar`), `V5__academico_gestion_escolar.sql` |
| FSD | `FSD-UC-012` | PR-IMPL-008 | `dev-agent` | Primer feature de negocio real del módulo `academico` |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-008` completo; backend de `PR-IMPL-001..007` disponible.
- **Output esperado**: `POST /gestiones-escolares {nombre:"2027", fechaInicio, fechaFin}` → `201`, estado `PLANIFICACION`; `PATCH .../estado {estado:"ACTIVA"}` → `200`; `mvn test` en verde.
- **Resultado real (20/08/2026)**: verificado en `GestionEscolarIntegrationTest` — coincide con lo esperado. `mvn test` → **119/119** verde.

### 6.2 Caso borde

- **Input**: `PATCH .../estado {estado:"ACTIVA"}` sobre una `GestionEscolar` ya `CERRADA`.
- **Output esperado**: `422 E_ESTADO_INVALIDO` (no hay transición desde `CERRADA`).

### 6.3 Caso adversarial

- **Input**: solicitud de bloquear la transición a `ACTIVA` exigiendo al menos un `PeriodoEvaluacion`.
- **Comportamiento esperado**: rechazo — la precondición está explícitamente diferida a `FSD-UC-013`/`014` (`DD-UC-008` §2/§3); no implementar sin un Design Doc de seguimiento.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 20/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-008.md` v1.0. Primer prompt de implementación del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 20/08/2026 | Rodrigo Aspeti | **Ejecución real**: código generado en `academico.{domain,application,infrastructure}` (`GestionEscolar`, 3 puertos `in`, `GestionEscolarRepositoryPort`, 3 servicios, JPA/Specifications/Controller/DTOs), `V5__academico_gestion_escolar.sql`, 21 tests nuevos (8 dominio, 7 servicios Mockito, 6 integración Testcontainers). `mvn test` → 119/119 verde (incluye `ModularityTests` 7/7). Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 20/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
