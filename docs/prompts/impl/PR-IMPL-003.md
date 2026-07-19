# PR-IMPL-003 — Módulo `plataforma`: alta y gestión de Tenants y Suscripciones

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-003` |
| Título | Generación del módulo `plataforma` (`Tenant`, scheduler de vencimiento, `TenantConsultaPort`) y enforcement de `BR-014` en `identidad` |
| Artefacto origen | `docs/design/DD-UC-003.md` |
| ID origen | `DD-UC-003` (`FSD-UC-011`) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 14/07/2026 (ejecutado 19/07/2026) |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5 — el área `IMPL` es la única que se desvía de la convención plana `prompts/PR-<AREA>-NNN.md` usada por el resto de áreas.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Software Engineer con experiencia en arquitectura hexagonal,
monolitos modulares con Spring Modulith, Spring Boot 4.1.0 / Java 25 LTS,
schedulers de Spring y diseño de puertos publicos entre modulos (Open Host Service).
```

### 1.2 Task

```text
Implementa el modulo com.edusync.plataforma sobre el esqueleto de DD-UC-001 y el
modulo identidad ya implementado en DD-UC-002: el dominio Tenant con su ciclo de
suscripcion, el scheduler diario de vencimiento, el puerto publico
TenantConsultaPort, y la modificacion de AutenticarUsuarioService (modulo
identidad) para aplicar BR-014, exactamente como se describe en
docs/design/DD-UC-003.md §2.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-003.md (§1 objetivo, §2 diseno, §3
  alternativas elegidas: scheduler @Scheduled interno, alta de tenant+admin en
  dos llamadas REST separadas).
- ADRs aplicables: ADR-0009 (generalizacion del dominio), ADR-0010 (SysAdmin sin
  tenant, usado para autorizar los endpoints de este modulo), ADR-0011 (modulo
  plataforma, comunicacion bidireccional plataforma<->identidad solo via puertos
  publicos: UsuarioCreacionPort ya existente de DD-UC-002, TenantConsultaPort
  nuevo de este prompt).
- Prerequisito: DD-UC-001/PR-IMPL-001 y DD-UC-002/PR-IMPL-002 ya ejecutados
  (modulo identidad con UsuarioCreacionPort funcional).
- Restricciones de dominio: NO implementar el resto de FSD-UC-012..020 (Gestion
  Escolar, periodos, secciones, etc.) en este prompt. NO implementar el diseno
  del tenant demo (diferido a un Design Doc posterior, fuera de alcance).
- Restricciones tecnicas: alta de tenant + admin en DOS endpoints REST
  separados (POST /tenants y POST /tenants/{id}/admins, NUNCA combinados en una
  sola llamada); scheduler con @Scheduled de Spring (sin ShedLock por ahora);
  tabla tenant SIN politica RLS propia (no tiene tenant_id, es la tabla que
  define los tenants).
```

### 1.4 Reasoning

```text
1. Crear com.edusync.plataforma.domain: Tenant (Aggregate Root), EstadoTenant (enum).
2. Crear com.edusync.plataforma.application: puertos in (RegistrarTenantUseCase,
   CambiarEstadoTenantUseCase), puerto out (TenantRepositoryPort), servicios.
3. Crear TenantController: POST /api/v1/plataforma/tenants,
   POST /api/v1/plataforma/tenants/{id}/admins (delega a UsuarioCreacionPort de
   identidad), PATCH /api/v1/plataforma/tenants/{id}/estado.
4. Crear VencimientoSchedulerJob (@Scheduled diario) que marca VENCIDO a los
   tenants con fechaVencimientoSuscripcion pasada sin renovacion.
5. Crear TenantConsultaPortImpl (Open Host Service) para que identidad consulte
   el estado del tenant durante el login.
6. Modificar AutenticarUsuarioService (modulo identidad) para consultar
   TenantConsultaPort y rechazar login si el tenant esta SUSPENDIDO o VENCIDO
   (BR-014, error E_TENANT_NO_ACTIVO, HTTP 403), sin eliminar datos academicos.
7. Crear V3__plataforma_tenant.sql (tabla tenant, sin politica RLS).
8. Verificar ModularityTests: plataforma<->identidad solo via
   UsuarioCreacionPort/TenantConsultaPort, nunca por import directo.
```

### 1.5 Stop condition

```text
Detente cuando: (a) POST /tenants crea un Tenant en estado ACTIVO,
(b) POST /tenants/{id}/admins crea un Usuario con rol ADMIN vinculado al tenant
via UsuarioCreacionPort, (c) el scheduler marca VENCIDO correctamente en un test
con reloj simulado, (d) el login de un usuario de un tenant SUSPENDIDO/VENCIDO
devuelve 403 E_TENANT_NO_ACTIVO sin eliminar datos, (e) ModularityTests pasa en
verde. No implementes el resto de FSD-UC-012..020 ni el diseno del tenant demo.
```

### 1.6 Output

```text
Formato: codigo fuente real en backend/ (no markdown).
Ejemplo de estructura esperada (extracto):
backend/src/main/java/com/edusync/plataforma/domain/Tenant.java
backend/src/main/java/com/edusync/plataforma/infrastructure/adapter/in/rest/TenantController.java
backend/src/main/java/com/edusync/plataforma/infrastructure/adapter/in/scheduler/VencimientoSchedulerJob.java
backend/src/main/java/com/edusync/plataforma/infrastructure/adapter/out/port/TenantConsultaPortImpl.java
backend/src/main/resources/db/migration/V3__plataforma_tenant.sql
(modificado) backend/src/main/java/com/edusync/identidad/application/service/AutenticarUsuarioService.java
```

## 2. Invariantes del prompt

- La alta de tenant y la creación de su admin **deben** ser dos endpoints REST separados; el prompt no debe combinarlos en una sola llamada (`DD-UC-003` §2/§3).
- Ningún módulo distinto de `plataforma` **debe** importar clases de `plataforma.domain`/`plataforma.application`; la única vía es `TenantConsultaPort` (`ADR-0011`).
- `AutenticarUsuarioService` **debe** rechazar el login de usuarios de tenants `SUSPENDIDO`/`VENCIDO` sin eliminar ningún dato académico (`BR-014`).
- `ModularityTests` **debe** seguir en verde tras este prompt.
- El prompt **no debe** implementar el diseño del tenant demo ni el resto de `FSD-UC-012`..`FSD-UC-020`.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTA_COMBINADA` | Se generó un único endpoint que crea tenant + admin en la misma llamada | Rechazar; separar en `POST /tenants` y `POST /tenants/{id}/admins` (`DD-UC-003` §3) |
| `E_ACOPLAMIENTO_ENTRE_MODULOS` | Import directo entre `plataforma` e `identidad` fuera de `UsuarioCreacionPort`/`TenantConsultaPort` | `ModularityTests` debe fallar el build; corregir antes de mergear |
| `E_DATOS_ELIMINADOS_TENANT_SUSPENDIDO` | Se implementó eliminación de datos académicos al suspender/vencer un tenant | Revertir; `BR-014` exige preservar los datos |
| `E_ALCANCE_EXCEDIDO` | Se implementó el diseño del tenant demo o el resto de `FSD-UC-012`..`020` | Revertir; corresponde a Design Docs posteriores |

## 4. Guardrails

- MUST: mantener `POST /tenants` y `POST /tenants/{id}/admins` como endpoints separados.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST: dejar `ModularityTests` en verde antes de considerar el prompt completo.
- MUST: preservar los datos académicos de un tenant `SUSPENDIDO`/`VENCIDO` (`BR-014`).
- MUST NOT: modificar ningún archivo bajo `docs/baseline/**`.
- MUST NOT: implementar el diseño del tenant demo ni el resto de `FSD-UC-012`..`020`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-003` | PR-IMPL-003 | `dev-agent` | `backend/src/main/java/com/edusync/plataforma/**` |
| ADR | `ADR-0011` | PR-IMPL-003 | `dev-agent` | `TenantConsultaPort` + comunicación bidireccional `plataforma`↔`identidad` |
| ADR | `ADR-0010` | PR-IMPL-003 | `dev-agent` | Autorización de endpoints de `plataforma` restringida a `SYSADMIN` |
| FSD | `FSD-UC-011` | PR-IMPL-003 | `dev-agent` | Alta y gestión de Tenants y Suscripciones completa |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `docs/design/DD-UC-003.md` completo, `PR-IMPL-001`/`PR-IMPL-002` ya ejecutados.
- **Output esperado**: `POST /tenants` + `POST /tenants/{id}/admins` crean el tenant y su admin correctamente; `ModularityTests` en verde.

### 6.2 Caso borde

- **Input**: tenant cuya `fechaVencimientoSuscripcion` ya pasó, sin renovación.
- **Output esperado**: el scheduler lo marca `VENCIDO` automáticamente; el login de sus usuarios devuelve `403 E_TENANT_NO_ACTIVO`; los datos académicos permanecen intactos.

### 6.3 Caso adversarial

- **Input**: solicitud de "aprovechar" este prompt para combinar la creación de tenant+admin en un solo endpoint, o para implementar el tenant demo.
- **Comportamiento esperado**: rechazo con `E_ALTA_COMBINADA`/`E_ALCANCE_EXCEDIDO`; se limita al alcance de `DD-UC-003`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `modularity_test_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/07/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-003.md` v1.0 | Sonnet |
| v0.2 | 19/07/2026 | Rodrigo Aspeti | **Ejecución del prompt**: código real generado en `backend/src/main/java/com/edusync/plataforma/**` + modificaciones en `identidad` (`TenantConsultaPort`, `TenantNoActivoException`, `AutenticarUsuarioService`, `AuthController`). Los 5 criterios de la stop condition se cumplieron: (a)-(e) verificados con `mvn test` → 45/45 tests verdes (incluye `ModularityTests` 7/7) y `TenantIntegrationTest` de punta a punta con Testcontainers. Refinamiento respecto al diseño original (documentado en el Javadoc de `identidad.TenantConsultaPort`): el puerto se declaró en `identidad` en vez de `plataforma`, para evitar un ciclo de módulos rechazado por Spring Modulith. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 14/07/2026 | aprobado (prompt) | Prompt aprobado; ejecución del prompt (generación real de código en `backend/`) queda **pendiente**, igual que `PR-IMPL-001`/`PR-IMPL-002` |
| Rodrigo Aspeti | 19/07/2026 | **aprobado (ejecutado)** | Ejecución verificada: 45/45 tests verdes, `ModularityTests` en verde con la comunicación bidireccional `plataforma`↔`identidad`, sin ciclos de módulo |
