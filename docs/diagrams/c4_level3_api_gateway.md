---
producto: "EduSync"
grupo: "G-EduSync"
diagrama: "c4_level3_api_gateway"
nivel: "C4 - Component (Level 3)"
contenedor: "api-gateway"
version: v0.1.0
fecha: "28/05/2026"
autor: "Rodrigo Aspeti"
estado: borrador
prompt: "PR-C4-003 (registrado en docs/PROMPT_MAPPING.md v1.4)"
skill: ".cursor/skills/c4-edusync/SKILL.md v0.3.0"
fuente_principal: "docs/DTI.md v0.3 §3.3"
fuentes_secundarias:
  - "docs/fsd/FSD_EduSync.md v1.0 §4.1, §4.2, §4.3, §4.4, §4.5"
  - "docs/arquitectura_hexagonal_EduSync.md v0.1"
  - "docs/dtos_EduSync.md v0.1"
  - "docs/adr/0001-multitenancy-rls-postgresql.md"
  - "docs/adr/0003-persistencia-inmutable-audit-log.md"
  - "docs/adr/0004-async-consolidacion-spring-events.md"
artefacto_mermaid: "docs/diagrams/c4_level3_api_gateway.mmd"
---

# C4 Level 3 — `api-gateway` (EduSync)

> Diagrama de componentes del contenedor `api-gateway` (Spring Boot 3.3 / Java 21 / ECS Fargate). Generado por `.cursor/skills/c4-edusync/SKILL.md` v0.3.0 siguiendo el procedimiento de 4 pasos `discovery → draft → validate → refine`.
>
> El bloque Mermaid renderizable vive en `c4_level3_api_gateway.mmd`. Este `.md` es su **espejo narrativo obligatorio** (IG-09).

---

## 1. Alcance y frontera

El contenedor `api-gateway` agrupa todo el adaptador de entrada web del monolito modular: **filtros de seguridad transversales, REST Controllers por FSD-UC, AOP de auditoria, DTOs de borde y manejador global de errores**. Es la única vía sincrónica para que actores externos (`Docente`, `Director`, `Secretaria` via `Angular SPA`) escriban o lean datos del sistema.

Quedan **fuera de este nivel 3** y se delegan a otros contenedores:

- **Lógica de negocio y `Math.floor()`** → `domain-layer` (DA-02 / BR-008). Si `floor()` apareciera aquí, sería gap mayor.
- **`SIEHttpClient` + circuit breaker** → contenedor `sie-adapter` (ver `c4_level3_sie_adapter.mmd` pendiente).
- **`MateriaCerradaListener` + consolidación async** → `event-bus` + `domain-layer` (DA-04).
- **Schedulers** (`VentanaExpiracionScheduler`, `SIERetryScheduler`) → contenedor `scheduler`.
- **Persistencia JPA y políticas RLS** → contenedor `postgres-rls`; el `api-gateway` solo inyecta `tenant_id` (`RLSTenantInjector`) y registra `audit_log` (`AuditLogAspect`).

---

## 2. Componentes del contenedor

### 2.1 Filtros transversales (cadena pre-controller)

| Componente | Tecnología | Responsabilidad | Trazabilidad |
|------------|-----------|-----------------|--------------|
| `JwtAuthFilter` | Spring Security 6 + JJWT (RS256) | Valida Bearer JWT, extrae `{tenant_id, user_id, rol}` al `SecurityContext`. Rechaza expirados (> 8 h) o firma inválida. | NFR-008, NFR-003 |
| `RLSTenantInjector` | Spring AOP + interceptor JPA | Ejecuta `SET LOCAL app.tenant_id = :id` antes de abrir cualquier `@Transactional`. | DA-01, NFR-010, POC-01 |

### 2.2 REST Controllers (un controller por FSD-UC primario)

| Componente | Endpoint | FSD-UC | BR/DA aplicados |
|------------|----------|--------|-----------------|
| `CalificacionController` | `POST /api/v1/calificaciones` | FSD-UC-001 | BR-001 (RBAC docente-materia), BR-002 (rango paramétrico), BR-004 (RUDE), DA-03 (audit_log) |
| `CentralizadorController` | `GET /api/v1/centralizadores` | FSD-UC-003 (lectura) | BR-008 (`floor` en domain), RB-08 (truncado único) |
| `ExportacionController` | `POST /api/v1/exportaciones/sie` | FSD-UC-004 | BR-004 (RUDE única clave), NFR-011 (idempotencia `rude+periodo_id`), DA-05 (delegada a `sie-adapter`) |
| `CorreccionController` | `POST /api/v1/correcciones` + `PUT /{id}/autorizar` | FSD-UC-005 | BR-005 (autorización jerárquica), BR-009 (ventana 1–72 h) |
| `PeriodoController` | `POST /api/v1/periodos` | FSD-UC-009 | BR-006 (secuencia T1→T2→T3), BR-007 (parámetros inmutables en `ABIERTO`) |

### 2.3 AOP, manejo de errores y DTOs

| Componente | Tecnología | Responsabilidad | Trazabilidad |
|------------|-----------|-----------------|--------------|
| `AuditLogAspect` | Spring AOP `@Around` + `@Transactional` | Inserta en `audit_log` dentro de la misma TX que toda escritura de dominio. Append-only (sin `UPDATE`/`DELETE`). | DA-03, BR-010, NFR-006 |
| `GlobalExceptionHandler` | `@ControllerAdvice` | Mapea `DomainException` → HTTP status + `ErrorResponseDTO`; nunca expone trazas internas. | Política única errores, NFR-003 |
| `Web DTOs (Records)` | Java 21 Records + Bean Validation 3.0 | `CalificacionRequestDTO`, `CentralizadorDTO`, `SIEExportRequestDTO`, `AutorizacionCorreccionDTO`, `PeriodoRequestDTO`, `ErrorResponseDTO`. **Nunca** se exponen entidades JPA. | `docs/dtos_EduSync.md` v0.1, AGENTS.md §5 |

---

## 3. Tabla de trazabilidad obligatoria (skill §6)

| FSD-UC | Contenedor C4 | Componente nivel 3 | DA/BR/NFR aplicado |
|--------|---------------|--------------------|--------------------|
| FSD-UC-001 — Registro de calificación | `api-gateway` | `CalificacionController` + `Web DTOs` + `JwtAuthFilter` + `RLSTenantInjector` + `AuditLogAspect` | BR-001, BR-002, BR-004, DA-01, DA-03, NFR-008, NFR-010 |
| FSD-UC-003 — Consolidación (lectura) | `api-gateway` | `CentralizadorController` + `Web DTOs` | BR-008 (`floor` vive en `domain-layer`), RB-08 |
| FSD-UC-004 — Exportación SIE | `api-gateway` | `ExportacionController` + `Web DTOs` → delega a `sie-adapter` | BR-004, NFR-011, DA-05 |
| FSD-UC-005 — Corrección retroactiva | `api-gateway` | `CorreccionController` + `Web DTOs` + `AuditLogAspect` | BR-005, BR-009, DA-03 |
| FSD-UC-009 — Administración de periodos | `api-gateway` | `PeriodoController` + `Web DTOs` | BR-006, BR-007 |
| Cross-cutting (todos los UC) | `api-gateway` | `JwtAuthFilter`, `RLSTenantInjector`, `AuditLogAspect`, `GlobalExceptionHandler` | NFR-003, NFR-006, NFR-008, NFR-010, DA-01, DA-03 |

---

## 4. Reporte de `validate` (paso 3 del skill)

### 4.1 Reglas duras verificadas

- [x] **`Math.floor()` no aparece en `api-gateway`.** Vive solo en `ConsolidacionDomainService` dentro de `domain-layer` (BR-008). El `CentralizadorController` se limita a serializar `CentralizadorDTO`.
- [x] **`audit_log` solo se escribe desde `AuditLogAspect`** en la misma TX (DA-03). Ningún controller escribe en `audit_log` directamente.
- [x] **RLS modelado como cross-cutting**: `RLSTenantInjector` precede toda TX; `postgres-rls` aplica la política a nivel DB (DA-01).
- [x] **RUDE como única clave** en `ExportacionController` y validado en `CalificacionRequestDTO` (BR-004); nunca se serializa `nombre` ni `fecha_nacimiento` desde aquí.
- [x] **Cada `Component` con tecnología explícita** (Spring Security 6, Spring MVC, AOP, Jakarta Validation 3.0, Java 21 Records).
- [x] **Cada `Rel` con protocolo explícito** (HTTPS/REST, JDBC/TLS 1.3, AWS SDK/TLS 1.3, In-process / Port IN, Spring AOP).
- [x] **Cabecera Mermaid `C4Component`** (no mezcla niveles).
- [x] **Sin Unicode decorativo en labels** (IG-10).

### 4.2 Gaps detectados y resueltos

| Gap | Resolución |
|-----|------------|
| FSD-UC-002 (cierre de materia) no tiene Controller propio en `api-gateway`. | **No es gap arquitectónico**: el cierre se dispara como transición de estado de `PeriodoController` o por evento de dominio interno; no es un endpoint REST. Documentado en la sección 1 "Alcance y frontera". |
| `CentralizadorController` solo cubre lectura de FSD-UC-003, no la escritura. | **Por diseño**: la escritura del centralizador es asíncrona (`event-bus` + `ConsolidarCentralizadorUseCase` en `domain-layer`), disparada por `MateriaCerradaEvent` (DA-04). El controller solo expone GET. |
| Múltiples controllers comparten `Web DTOs` y `AuditLogAspect`. | **Correcto**: son componentes transversales legítimos; no es overload. |

### 4.3 Componentes sin FSD-UC justificador

Los 4 componentes cross-cutting (`JwtAuthFilter`, `RLSTenantInjector`, `AuditLogAspect`, `GlobalExceptionHandler`) no nacen de un FSD-UC sino de **NFRs y DAs transversales**:

- `JwtAuthFilter` → NFR-008 + NFR-003 (sin secretos/PII en logs).
- `RLSTenantInjector` → DA-01 + NFR-010 (test `MultitenantTest.no_cross_tenant_data`).
- `AuditLogAspect` → DA-03 + BR-010 + NFR-006.
- `GlobalExceptionHandler` → política de errores REST + NFR-003.

Esto está **permitido** por el skill (§7): "ningún contenedor sin FSD-UC que lo justifique" se interpreta como FSD-UC o NFR/DA transversal explícito.

---

## 5. Refinamiento (paso 4 del skill)

El contenedor crítico `domain-layer` ya cuenta con su propio Level 3 pendiente (`c4_level3_domain_layer.mmd` — siguiente en la cola). Las invocaciones de Use Cases mostradas aquí como `In-process / Port IN` se expandirán a `RegistrarCalificacionUseCase`, `ConsolidarCentralizadorUseCase`, `ExportarSIEUseCase`, `GestionarCorreccionUseCase` y `GestionarPeriodoUseCase` en ese diagrama.

---

## 6. Convivencia con otros niveles C4

| Nivel | Archivo | Sincronía con este Level 3 |
|-------|---------|----------------------------|
| Level 1 (Context) | `docs/diagrams/c4_level1.mmd` | El `api-gateway` no es visible aquí; los actores externos llegan al sistema `EduSync` como caja única. |
| Level 2 (Container) | `docs/diagrams/c4_level2.mmd` | El `api-gateway` aparece como un Container; este Level 3 lo descompone. |
| Level 3 (Component) — `api-gateway` | **este archivo** | — |
| Level 3 (Component) — `domain-layer` | `docs/diagrams/c4_level3_domain_layer.mmd` | **Pendiente** — fuente `arquitectura_hexagonal_EduSync.md` |
| Level 3 (Component) — `sie-adapter` | `docs/diagrams/c4_level3_sie_adapter.mmd` | **Pendiente** — fuente ADR-0005 |
| Deployment | `docs/diagrams/deployment_aws.mmd` | **Pendiente** — fuente DTI §8 + ADR-0006 |

---

## 7. Registro de cambios

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 28/05/2026 | Rodrigo Aspeti | Versión inicial — generada por skill `c4-edusync` v0.3.0 con cabecera `C4Component`. Cubre 5 FSD-UC (001, 003, 004, 005, 009) y 4 componentes cross-cutting. Mermaid renderiza limpio en `mermaid.live`. Sin gaps mayores. | FSD v1.0, LFSD v1.0.1, DTI v0.3 §3.3, arquitectura_hexagonal v0.1, dtos_EduSync v0.1, ADRs 0001/0003/0004 |
