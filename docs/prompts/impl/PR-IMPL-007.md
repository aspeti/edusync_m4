# PR-IMPL-007 — Filtros y paginación reutilizables en los listados GetAll

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-007` |
| Título | Filtros y paginación en `GET /usuarios` y `GET /plataforma/tenants` (backend + UI), patrón reutilizable |
| Artefacto origen | `docs/design/DD-UC-007.md` |
| ID origen | `DD-UC-007` (`FSD-UC-011`, `FSD-UC-021`, mejora no funcional) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 20/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Backend/Frontend Engineer full-stack con experiencia en Java 25 /
Spring Boot 4.1.0 (arquitectura hexagonal, Spring Data JPA) y Angular 21
(standalone, signals).
```

### 1.2 Task

```text
Implementa filtros y paginacion segun docs/design/DD-UC-007.md §2 en los dos
listados GetAll existentes: GET /api/v1/usuarios (q sobre nombreCompleto o
email, activo, rol) y GET /api/v1/plataforma/tenants (q sobre nombre, estado);
ambos con page/size (default 0/20, maximo 100). Crea el patron reutilizable
shared.{PageQuery,PageResult,web.PageResponse} para que listados futuros lo
adopten sin rediseñarlo. Actualiza la UI Angular de ambas listas para exponer
los nuevos filtros y la paginacion.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-007.md (Specification/JpaSpecificationExecutor
  para combinar filtros opcionales; PageResult framework-free vs. PageResponse
  REST, mismo precedente que RespuestaLlm/ChatResponse en shared.ai).
- Contratos existentes a modificar (DD-UC-004/005/006): GET /usuarios,
  GET /plataforma/tenants (ambos devolvian T[] sin paginar).
- ADRs: ADR-0008 (stack vivo), ADR-0011 (shared es modulo OPEN, visible a
  identidad y plataforma).
- Prerrequisito: PR-IMPL-001..006 ya ejecutados.
- Restricciones: no modificar reglas de negocio ni invariantes de dominio
  (RUDE, floor, audit_log, tenant_id/RLS); mantener el metodo no paginado
  UsuarioRepositoryPort.listarPorTenant(UUID) intacto (lo consume
  shared.ai.BuscarUsuarioPorNombrePortImpl); tenantId nunca viene del cliente
  en el filtro de usuarios (viene de TenantContextProvider).
```

### 1.4 Reasoning

```text
1. shared.PageQuery (normaliza/clampa page/size) y shared.PageResult<T>
   (framework-free, calcula totalPages).
2. shared.web.PageResponse<T> (DTO REST) + PageResponse.from(PageResult,mapper).
3. identidad: UsuarioFiltro, firma nueva de ListarUsuariosUseCase/Service,
   overload paginado en UsuarioRepositoryPort/Adapter, UsuarioSpecifications
   (Criteria API, join a roles con distinct para el filtro rol),
   JpaSpecificationExecutor en UsuarioJpaRepository.
4. plataforma: TenantFiltro, firma nueva de ListarTenantsUseCase/Service,
   TenantRepositoryPort.listarTodos(filtro,pageQuery), TenantSpecifications,
   JpaSpecificationExecutor en TenantJpaRepository.
5. Controllers: @RequestParam opcionales (q/activo/rol/page/size en
   UsuarioController; q/estado/page/size en TenantController), responden
   PageResponse<*Response>.
6. Actualizar UsuarioIntegrationTest/TenantIntegrationTest al nuevo contrato
   paginado + agregar tests de filtro/paginacion. Actualizar
   ListarUsuariosServiceTest/ListarTenantsServiceTest a las nuevas firmas.
   Agregar unit tests de PageQuery/PageResult/PageResponse.
7. Frontend: core/api/page-response.model.ts (generico); usuario.model.ts +
   usuarios-list.page.ts (caja de busqueda, selects rol/activo, paginador);
   tenant.model.ts + tenants-list.page.ts (caja de busqueda, select estado,
   paginador). HttpParams para construir la query string.
8. mvn test + ng build en verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) GET /usuarios y GET /plataforma/tenants aceptan
q/filtros/page/size y devuelven PageResponse<*>, (b) el filtro q de usuarios
coincide por nombre O email (case-insensitive), (c) todos los tests backend
pasan (incluye ModularityTests y los nuevos de filtros/paginacion), (d) ambas
listas de la UI permiten buscar/filtrar/paginar sin romper crear/editar/
cambiar estado ya existentes, (e) ng build en verde. No implementes
ordenamiento (sort) ni toques ningun modulo de academico.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ y frontend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/shared/{PageQuery,PageResult}.java
backend/src/main/java/com/edusync/shared/web/PageResponse.java
backend/src/main/java/com/edusync/identidad/**  (delta: filtro/paginacion)
backend/src/main/java/com/edusync/plataforma/** (delta: filtro/paginacion)
frontend/src/app/core/api/page-response.model.ts
frontend/src/app/features/usuarios/**  (delta)
frontend/src/app/features/plataforma/** (delta)
```

## 2. Invariantes del prompt

- `tenantId` del filtro de usuarios **nunca** proviene de un query param del cliente — siempre de `TenantContextProvider` (mismo invariante que `DD-UC-002`/`DD-UC-005`).
- El método no paginado `UsuarioRepositoryPort.listarPorTenant(UUID)` **debe** conservarse intacto (lo usa `shared.ai`).
- Ningún cálculo de negocio (promedios, `floor`, RLS) se toca en este prompt.
- `mvn test` y `ng build` **deben** quedar en verde.
- El filtro `q` **debe** ser case-insensitive y por `contains` (no exact match).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El filtro de usuarios acepta `tenantId` como query param | Rechazar; `tenantId` siempre viene del contexto de seguridad |
| `E_METODO_NO_PAGINADO_ELIMINADO` | Se eliminó `listarPorTenant(UUID)` en vez de añadir un overload | Revertir; rompería `shared.ai.BuscarUsuarioPorNombrePortImpl` |
| `E_CALCULO_EN_ADAPTADOR` | Se movió lógica de negocio a `UsuarioSpecifications`/`TenantSpecifications` | Rechazar; estos archivos solo traducen filtros a predicados JPA |
| `E_CONTRATO_SIN_DOCUMENTAR` | El cambio de `T[]` a `PageResponse<T>` no se registra en `DD-UC-007` §4 | Completar la sección antes de cerrar el prompt |

## 4. Guardrails

- MUST: `tenantId` del filtro de usuarios viene siempre de `TenantContextProvider`.
- MUST: conservar `UsuarioRepositoryPort.listarPorTenant(UUID)` sin cambios (consumidor: `shared.ai`).
- MUST: `mvn test` y `ng build` en verde antes de considerar el prompt completo.
- MUST NOT: mover cálculos de negocio a las clases `*Specifications`.
- MUST NOT: modificar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-007` | PR-IMPL-007 | `dev-agent` | `shared.{PageQuery,PageResult,web.PageResponse}`, filtros/paginación en `identidad`/`plataforma`, UI Angular |
| FSD | `FSD-UC-011`, `FSD-UC-021` | PR-IMPL-007 | `dev-agent` | Mejora no funcional sobre los listados `GetAll` ya completos de ambos casos de uso |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-007` completo; backend de `PR-IMPL-001..006` disponible.
- **Output esperado**: `GET /usuarios?q=roberto&rol=PROFESOR&page=0&size=10` devuelve `PageResponse<UsuarioResponse>` filtrado; `mvn test`/`ng build` en verde.

### 6.2 Caso borde

- **Input**: `size=1000` (fuera de rango).
- **Output esperado**: `PageQuery.of` clampa a 100, sin error 500.

### 6.3 Caso adversarial

- **Input**: solicitud de agregar `tenantId` como query param filtrable en `GET /usuarios`.
- **Comportamiento esperado**: rechazo `E_TENANT_DESDE_CLIENTE`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 20/08/2026 | Rodrigo Aspeti | Creación y **ejecución en el mismo turno** a partir de `docs/design/DD-UC-007.md` v1.0: patrón `shared.{PageQuery,PageResult,web.PageResponse}`; filtros/paginación en `GET /usuarios` (q, activo, rol) y `GET /plataforma/tenants` (q, estado); `Specification`/`JpaSpecificationExecutor` (primer uso en el proyecto); UI Angular de ambas listas actualizada. `mvn test` 98/98 verde (incluye `ModularityTests` 7/7); `ng build` verde | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 20/08/2026 | **ejecutado** | Código en working tree; docs sincronizados vía `dtp-sync`; commit formal pendiente |
