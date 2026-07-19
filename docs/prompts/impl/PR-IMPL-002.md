# PR-IMPL-002 — Módulo `identidad`: modelo Usuario/UsuarioRol y autenticación (login, JWT, seed SysAdmin)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-002` |
| Título | Generación del módulo `identidad` (dominio, aplicación, infraestructura), login JWT, `TenantContextProvider` real y seed del primer `SYSADMIN` |
| Artefacto origen | `docs/design/DD-UC-002.md` |
| ID origen | `DD-UC-002` (`FSD-UC-021`, parcial) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5 — el área `IMPL` es la única que se desvía de la convención plana `prompts/PR-<AREA>-NNN.md` usada por el resto de áreas (ver `PR-IMPL-001.md` §0 para la nota completa de la excepción).

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Software Engineer con experiencia en arquitectura hexagonal,
monolitos modulares con Spring Modulith, Spring Security 7 sobre Spring Boot 4.1.0 /
Java 25 LTS, autenticación JWT stateless y PostgreSQL 15 con Row-Level Security.
```

### 1.2 Task

```text
Implementa el módulo com.edusync.identidad sobre el esqueleto de DD-UC-001: el
dominio Usuario/UsuarioRol con su invariante de exclusión mutua SYSADMIN/tenant,
el flujo de login con JWT, el seed del primer SYSADMIN, la implementación real
de TenantContextProvider (shared/tenant) y el puerto público UsuarioCreacionPort,
exactamente como se describe en docs/design/DD-UC-002.md §2.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-002.md (§1 objetivo, §2 diseño, §3 alternativas
  elegidas: JWT HS256, política RLS "OR tenant_id IS NULL" con filtro explícito
  adicional en UsuarioRepositoryPort).
- ADRs aplicables: ADR-0001 (RLS multitenancy — TenantContextProvider implementa
  aquí por primera vez el placeholder), ADR-0008 (stack), ADR-0010 (invariante
  tenant_id IS NULL ⟺ roles = {SYSADMIN}, modelo multi-rol UsuarioRol N:M),
  ADR-0011 (módulo identidad, puerto público UsuarioCreacionPort como Open Host
  Service — ningún otro módulo debe importar clases internas de identidad).
- Prerequisito: DD-UC-001 / PR-IMPL-001 ya ejecutado (esqueleto backend/frontend/
  infra existente, módulo identidad vacío con package-info.java).
- Restricciones de dominio: NO implementar en este prompt el CRUD administrativo
  completo de usuarios (alta desde Admin, PATCH roles, activar/desactivar, reset
  password) — eso es DD-UC-004. NO implementar alta de Tenant — eso es DD-UC-003.
- Restricciones técnicas: JWT firmado HS256 (secreto en variable de entorno
  JWT_SECRET, nunca hardcodeado), expiración 8h (PRD-NFR-007), BCrypt para hash
  de contraseñas, Flyway V2__identidad_usuario.sql con política RLS
  "CREATE POLICY tenant_isolation ON usuario USING (tenant_id = current_setting
  ('app.current_tenant', true)::uuid OR tenant_id IS NULL)".
```

### 1.4 Reasoning

```text
1. Crear com.edusync.identidad.domain: Usuario (Aggregate Root con factory que
   valida tenant_id IS NULL ⟺ roles == {SYSADMIN}), UsuarioRol, enum Rol.
2. Crear com.edusync.identidad.application: puertos in (AutenticarUsuarioUseCase,
   CrearUsuarioUseCase), puerto out (UsuarioRepositoryPort), servicios de aplicación.
3. Crear com.edusync.identidad.infrastructure.adapter.in.rest: AuthController
   (POST /api/v1/auth/login) y UsuarioCreacionPortImpl (Open Host Service).
4. Crear com.edusync.identidad.infrastructure.adapter.out.persistence: entidades
   JPA UsuarioJpaEntity/UsuarioRolJpaEntity y su repositorio, implementando
   UsuarioRepositoryPort con el filtro explícito tenant_id descrito en DD-UC-002 §2.
5. Crear com.edusync.identidad.infrastructure.security: JwtTokenProvider
   (emisión/validación HS256), JwtAuthenticationFilter, SecurityConfig
   (SecurityFilterChain, PasswordEncoder BCrypt).
6. Implementar com.edusync.shared.tenant.TenantContextProvider real: tras validar
   el JWT, ejecuta SET app.current_tenant en la conexión JDBC activa.
7. Crear backend/src/main/resources/db/migration/V2__identidad_usuario.sql:
   tablas usuario/usuario_rol + política RLS "OR tenant_id IS NULL".
8. Crear el seed del primer SYSADMIN (ApplicationRunner condicional o migración
   Flyway separada), leyendo la contraseña inicial desde variable de entorno.
9. Verificar que ModularityTests sigue en verde y que ningún otro módulo importa
   clases internas de identidad.domain/identidad.application.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST /api/v1/auth/login devuelve un JWT válido para el
SYSADMIN seed y 401 E_CREDENCIALES_INVALIDAS para credenciales incorrectas,
(b) la invariante tenant_id/roles está validada a nivel de dominio (no solo REST),
(c) TenantContextProvider fija app.current_tenant correctamente en un test de
integración con Testcontainers, (d) ModularityTests pasa en verde,
(e) UsuarioCreacionPort está expuesto y listo para ser consumido por plataforma
en DD-UC-003. No implementes el CRUD administrativo completo (DD-UC-004) ni la
gestión de Tenant (DD-UC-003) — eso corresponde a Design Docs posteriores.
```

### 1.6 Output

```text
Formato: código fuente real en backend/ (no markdown).
Ejemplo de estructura esperada (extracto):
backend/src/main/java/com/edusync/identidad/domain/Usuario.java
backend/src/main/java/com/edusync/identidad/application/port/in/AutenticarUsuarioUseCase.java
backend/src/main/java/com/edusync/identidad/infrastructure/adapter/in/rest/AuthController.java
backend/src/main/java/com/edusync/identidad/infrastructure/security/JwtAuthenticationFilter.java
backend/src/main/java/com/edusync/shared/tenant/TenantContextProvider.java
backend/src/main/resources/db/migration/V2__identidad_usuario.sql
```

## 2. Invariantes del prompt

- `Usuario.tenant_id IS NULL` **debe** ser equivalente exactamente a que el conjunto de roles del usuario sea `{SYSADMIN}` — validado en el dominio (factory/constructor), no solo en la capa REST (`ADR-0010`).
- Ningún módulo distinto de `identidad` **debe** importar clases de `identidad.domain`/`identidad.application` directamente; la única vía de interacción es `UsuarioCreacionPort` (`ADR-0011`).
- El secreto JWT (`JWT_SECRET`) **no debe** estar hardcodeado en ningún archivo versionado.
- `UsuarioRepositoryPort` **debe** filtrar explícitamente por `tenant_id` en toda consulta de un Admin de tenant, sin depender solo de la política RLS `OR tenant_id IS NULL` (mitigación decidida en `DD-UC-002` §2/§3).
- `ModularityTests` **debe** seguir en verde después de este prompt.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_INVARIANTE_ROL_VIOLADA` | Se genera código que permite persistir un `Usuario` con `SYSADMIN` combinado con un rol de tenant, o con `tenant_id` no nulo y rol `SYSADMIN` | Rechazar y corregir la factory/constructor de `Usuario` |
| `E_ACOPLAMIENTO_ENTRE_MODULOS` | Import directo de `identidad.domain`/`identidad.application` desde otro módulo (fuera de `UsuarioCreacionPort`) | `ModularityTests` debe fallar el build; corregir antes de mergear |
| `E_ALCANCE_EXCEDIDO` | Se implementó el CRUD administrativo completo (`DD-UC-004`) o la gestión de `Tenant` (`DD-UC-003`) en este prompt | Revertir el alcance excedido; corresponde a Design Docs posteriores |
| `E_FILTRO_TENANT_AUSENTE` | `UsuarioRepositoryPort` no aplica el filtro explícito de `tenant_id` para consultas de Admin de tenant, dependiendo solo de la política RLS `OR tenant_id IS NULL` | Rechazar; corregir el adaptador de persistencia según la mitigación de `DD-UC-002` §2 |

## 4. Guardrails

- MUST: validar la invariante `tenant_id`/roles en el dominio, con tests unitarios que cubran el caso límite de intento de combinación `SYSADMIN` + rol de tenant.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST: dejar `ModularityTests` en verde antes de considerar el prompt completo.
- MUST: aplicar el filtro explícito de `tenant_id` en `UsuarioRepositoryPort` (mitigación de la política RLS `OR tenant_id IS NULL`).
- MUST NOT: modificar ningún archivo bajo `docs/baseline/**`.
- MUST NOT: implementar el CRUD administrativo completo de usuarios ni la gestión de `Tenant` (fuera de alcance, `DD-UC-004`/`DD-UC-003`).
- MUST NOT: hardcodear el secreto JWT ni credenciales del seed `SYSADMIN`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-002` | PR-IMPL-002 | `dev-agent` | `backend/src/main/java/com/edusync/identidad/**`, `shared/tenant/TenantContextProvider.java` |
| ADR | `ADR-0001` | PR-IMPL-002 | `dev-agent` | Implementación real de `TenantContextProvider` + política RLS `V2__identidad_usuario.sql` |
| ADR | `ADR-0010` | PR-IMPL-002 | `dev-agent` | `Usuario`/`UsuarioRol` con invariante multi-rol + `SYSADMIN` sin tenant |
| ADR | `ADR-0011` | PR-IMPL-002 | `dev-agent` | Módulo `identidad` + puerto público `UsuarioCreacionPort` |
| FSD | `FSD-UC-021` (parcial) | PR-IMPL-002 | `dev-agent` | Flujo de autenticación (login JWT) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `docs/design/DD-UC-002.md` completo, `PR-IMPL-001` ya ejecutado (esqueleto existente).
- **Output esperado**: `POST /api/v1/auth/login` con las credenciales del `SYSADMIN` seed devuelve `200` con un JWT válido; `ModularityTests` en verde.

### 6.2 Caso borde

- **Input**: intento de construir un `Usuario` con `roles = {SYSADMIN, ADMIN}` o con `tenant_id` no nulo y rol `SYSADMIN`.
- **Output esperado**: la factory de dominio lanza una excepción (`E_INVARIANTE_ROL_VIOLADA`); no se persiste el registro.

### 6.3 Caso adversarial

- **Input**: solicitud de "aprovechar" este prompt para ya implementar el endpoint de alta de `Tenant` o el CRUD completo de usuarios.
- **Comportamiento esperado**: rechazo con `E_ALCANCE_EXCEDIDO`; se limita al login/seed/`UsuarioCreacionPort` y remite a `DD-UC-003`/`DD-UC-004`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `modularity_test_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/07/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-002.md` v1.0 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 14/07/2026 | aprobado (prompt) | Prompt aprobado; ejecución del prompt (generación real de código en `backend/`) queda **pendiente** como siguiente paso, igual que `PR-IMPL-001` |
