# PR-IMPL-005 — Módulo identidad: CRUD administrativo de Usuarios y Roles

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-005` |
| Título | Generación del CRUD backend de Usuarios y Roles (alta, roles, estado, restablecimiento de contraseña) |
| Artefacto origen | `docs/design/DD-UC-005.md` |
| ID origen | `DD-UC-005` (`FSD-UC-021`, resto) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 04/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5 — el área `IMPL` es la única que se desvía de la convención plana `prompts/PR-<AREA>-NNN.md` usada por el resto de áreas.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Backend Engineer con experiencia en Java 25 LTS, Spring Boot 4.1.0
y arquitectura hexagonal (Ports & Adapters) sobre un monolito modular Spring
Modulith. Dominas el modelo de dominio inmutable (factory methods, sin setters)
y las invariantes de agregado.
```

### 1.2 Task

```text
Implementa el CRUD administrativo de Usuarios y Roles segun docs/design/DD-UC-005.md
§2 dentro del modulo identidad ya existente (creado por PR-IMPL-002): metodos
inmutables conRoles/activar/desactivar en Usuario; mini-agregado PasswordResetToken;
casos de uso ListarUsuarios/ActualizarRolesUsuario/CambiarEstadoUsuario/
IniciarRestablecimientoPassword/ConfirmarRestablecimientoPassword; UsuarioController
(POST/GET/PATCH roles/PATCH estado/POST iniciar-reset) y PasswordResetController
(POST confirmar, publico); NotificacionPort con adaptador log-only; migracion
V4__identidad_password_reset_token.sql.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-005.md (§1 alcance, §2 diseno, §3
  alternativas: notificacion log-only, ASESOR sin validacion de curso, filtro
  de tenant explicito, 404 en vez de 403).
- ADRs: ADR-0001 (RLS), ADR-0010 (invariante permanente SYSADMIN/multi-rol),
  ADR-0011 (limites de modulo, todo el codigo nuevo vive dentro de identidad),
  ADR-0012 (Lombok allowlist en domain/, springdoc-openapi, Bean Validation).
- Prerrequisito: PR-IMPL-001/002/003/004 ya ejecutados. Reutiliza Usuario,
  UsuarioRol, Rol, UsuarioRepositoryPort, PasswordHasherPort ya existentes
  (backend/src/main/java/com/edusync/identidad/**).
- Restricciones: Usuario.crear() es la unica fuente de verdad de la invariante
  ADR-0010 — conRoles/activar/desactivar DEBEN reutilizarla, nunca mutar campos
  directamente; ningun endpoint de este prompt puede asignar el rol SYSADMIN;
  el token de reset NUNCA se loguea en claro (AGENTS.md §7); no implementar
  E_ASESOR_SIN_CURSO (Curso no existe todavia); no implementar UI Angular
  (DD-UC-006 futuro); no implementar envio real de email (SES).
```

### 1.4 Reasoning

```text
1. Domain: Usuario.conRoles/activar/desactivar (reutilizan crear()); dominio
   PasswordResetToken + TokenResetInvalidoException.
2. application/port/out: UsuarioRepositoryPort.listarPorTenant(tenantId);
   PasswordResetTokenRepositoryPort; NotificacionPort.
3. application/service: ListarUsuariosService, ActualizarRolesUsuarioService,
   CambiarEstadoUsuarioService (ambos validan tenantId actor == tenantId
   objetivo, 404 si no coincide), RestablecerPasswordService (iniciar +
   confirmar, expiracion + un solo uso).
4. infrastructure: UsuarioController + PasswordResetController + DTOs con
   Bean Validation; LogNotificacionAdapter (placeholder); persistencia JPA
   de PasswordResetToken; V4__identidad_password_reset_token.sql.
5. Tests: unit (invariantes, filtro tenant, token expirado/usado) +
   integration Testcontainers (6 endpoints, casos A2/A3/A4, aislamiento
   cross-tenant) + verificar ModularityTests en verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST /usuarios crea con multi-rol y rechaza SYSADMIN/roles
vacio, (b) GET /usuarios devuelve solo usuarios del tenant del Admin, (c) PATCH
roles/estado revalidan la invariante ADR-0010 y devuelven 404 cross-tenant,
(d) el flujo de restablecimiento de password (iniciar + confirmar) funciona
con token de un solo uso y expiracion, sin loguear el token, (e) mvn test
(incluye ModularityTests) esta en verde. No implementes E_ASESOR_SIN_CURSO,
UI Angular ni envio real de email.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/identidad/domain/Usuario.java (delta)
backend/src/main/java/com/edusync/identidad/domain/PasswordResetToken.java
backend/src/main/java/com/edusync/identidad/infrastructure/adapter/in/rest/UsuarioController.java
backend/src/main/resources/db/migration/V4__identidad_password_reset_token.sql
```

## 2. Invariantes del prompt

- Toda mutación de `Usuario` (roles, estado) **debe** pasar por `Usuario.crear()` para revalidar la invariante permanente de `ADR-0010`.
- Ningún endpoint de este prompt **puede** asignar el rol `SYSADMIN`.
- El token de restablecimiento de contraseña **no debe** aparecer en logs, respuestas de error ni el cuerpo de `POST /usuarios/{id}/restablecer-password`.
- Toda consulta/mutación scoped a tenant **debe** filtrar explícitamente por `tenantId` en la capa de aplicación (no confiar solo en RLS, mismo patrón `DD-UC-002`).
- `ModularityTests` **debe** quedar en verde; ningún código nuevo sale del módulo `identidad`.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_INVARIANTE_ROL_VIOLADA` | Se intentó combinar `SYSADMIN` con un rol de tenant o dejar `roles` vacío sin pasar por `Usuario.crear()` | Rechazar; usar los factory methods existentes |
| `E_FILTRO_TENANT_AUSENTE` | Un endpoint lista o muta usuarios sin filtrar por `tenantId` del actor | Añadir el filtro explícito antes de aceptar el prompt |
| `E_TOKEN_EN_LOG` | El token de reset aparece en logs o en una respuesta HTTP | Corregir `LogNotificacionAdapter`/controlador; nunca exponer el token |
| `E_ALCANCE_EXCEDIDO` | Se implementó `E_ASESOR_SIN_CURSO`, UI Angular o envío real de email | Revertir; corresponde a `academico`/`DD-UC-006`/decisión de infraestructura futura |

## 4. Guardrails

- MUST: revalidar la invariante `ADR-0010` en cada mutación de `Usuario` vía `crear()`.
- MUST: filtrar `tenantId` explícitamente en toda consulta/mutación de tenant.
- MUST: `404` (no `403`) cuando el usuario objetivo pertenece a otro tenant.
- MUST: `mvn test` (incluye `ModularityTests`) en verde antes de considerar el prompt completo.
- MUST NOT: loguear el token de restablecimiento de contraseña ni ninguna contraseña en claro.
- MUST NOT: modificar ningún archivo bajo `docs/baseline/**`.
- MUST NOT: implementar `E_ASESOR_SIN_CURSO`, UI Angular (`DD-UC-006`) o envío real de email (SES).

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-005` | PR-IMPL-005 | `dev-agent` | `identidad/**` (delta) + `V4__identidad_password_reset_token.sql` |
| FSD | `FSD-UC-021` (resto CRUD) | PR-IMPL-005 | `dev-agent` | CRUD Usuarios/Roles backend |
| ADR | `ADR-0010` / `ADR-0001` / `ADR-0011` / `ADR-0012` | PR-IMPL-005 | `dev-agent` | Invariante multi-rol + aislamiento tenant + límites de módulo + convenciones backend |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-005` completo; módulo `identidad` de `PR-IMPL-002` disponible.
- **Output esperado**: alta multi-rol, listado scoped, PATCH roles/estado, reset password (iniciar+confirmar) funcionando; `mvn test` en verde.

### 6.2 Caso borde

- **Input**: Admin de tenant A intenta `PATCH /usuarios/{id}/roles` sobre un usuario de tenant B.
- **Output esperado**: `404` (no `403`, no confirma existencia).

### 6.3 Caso adversarial

- **Input**: solicitud de asignar `SYSADMIN` vía `POST /usuarios` o de loguear el token de reset "para debug".
- **Comportamiento esperado**: rechazo `E_INVARIANTE_ROL_VIOLADA` / `E_TOKEN_EN_LOG`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 04/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-005.md` v1.0 | Sonnet |
| v0.2 | 04/08/2026 | Rodrigo Aspeti | Estado → **Ejecutado**: CRUD backend real (alta multi-rol, `PATCH` roles/estado, restablecimiento de contraseña de un solo uso); `mvn test` 72/72 verde (incluye `ModularityTests` 7/7). Corrección de un bug de merge JPA en `UsuarioRepositoryAdapter.guardar()` expuesto por este prompt (ver `docs/design/DD-UC-005.md` changelog v1.1). Sincronizado con `DTP` v1.12 / `PROMPT_MAPPING` v2.10 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 04/08/2026 | aprobado (prompt) | Prompt aprobado; ejecución de código real en el mismo ciclo |
| Rodrigo Aspeti | 04/08/2026 | **ejecutado** | Código en working tree; docs sincronizados vía `dtp-sync`; commit formal pendiente |
