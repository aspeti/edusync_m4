---
name: c4-edusync
description: >-
  Autor y validador de diagramas C4 (Context, Container, Component) en Mermaid
  para el proyecto EduSync. Activar cuando el usuario pide "diagrama C4 nivel N
  de EduSync", edita un archivo docs/diagrams/c4_*.mmd, o quiere visualizar la
  arquitectura hexagonal (Java 21, Spring Boot 3.3, PostgreSQL 15 RLS, Angular 17,
  AWS ECS). Consume FSD-EduSync.md y LFSD-EduSync.md; produce el bloque Mermaid
  con tabla de trazabilidad FSD-UC ↔ contenedor/componente.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: c4-edusync — Diagramas C4 para EduSync

> Skill vive en `.cursor/skills/c4-edusync/SKILL.md` y `.claude/skills/c4-edusync/SKILL.md`.
> Activar con: `@c4-edusync nivel <N> [contenedor <nombre>]`

## 1. Cuándo activarlo

- Usuario pide "diagrama C4 nivel 1/2/3 de EduSync" o "visualiza la arquitectura".
- Usuario abre o edita `docs/diagrams/c4_*.mmd`.
- **NO activar** en fase de descubrimiento (BRD/MRD); requiere FSD v1.0 mínimo.

**Niveles soportados:**

| Nivel | Nombre | Archivo de salida |
|-------|--------|-------------------|
| 1 | System Context | `docs/diagrams/c4_level1.mmd` |
| 2 | Containers | `docs/diagrams/c4_level2.mmd` |
| 3 | Components | `docs/diagrams/c4_level3_<contenedor>.mmd` |
| 4 | Code | **Fuera de alcance** — ver §9 |

---

## 2. Entradas obligatorias

Antes de generar, confirmar:

- Nivel C4 deseado (1, 2 o 3).
- Para nivel 3: nombre del contenedor (`api-gateway`, `domain-layer`, `sie-adapter`).
- Fuente FSD: `docs/fsd/FSD_EduSync.md` (se asume disponible en el repo).

Si falta el nivel: responder `"Necesito el nivel C4 (1, 2 o 3) y, para nivel 3, el contenedor objetivo."`

---

## 3. Fuentes de verdad (orden de precedencia)

1. `docs/fsd/FSD_EduSync.md` — FSD-UC-001, 003, 004, 005, 009 + BR-001..BR-012 + NFRs.
2. `docs/LFSD-EduSync.md` — paquetes Java, contratos API, DDL, secuencias, seguridad.
3. `docs/AGENTS.md` — stack autoritativo, restricciones, agentes y guardrails.
4. `docs/arquitectura_funcional_EduSync.md` — DA-01..DA-05 (multitenancy, inmutabilidad, consolidación async, reglas paramétricas, resiliencia SIE).
5. `docs/adr/` — decisiones arquitectónicas formales (cuando existan).

---

## 4. Arquitectura de referencia EduSync (no modificar sin ADR)

### Actores (Level 1)

| Actor | Tipo | Descripción |
|-------|------|-------------|
| DIRECTOR (Jeanneth) | `Person` | Gestiona periodos, autoriza correcciones retroactivas |
| DOCENTE (Marcela) | `Person` | Carga calificaciones por dimensión (Ser/Saber/Hacer/Decidir) |
| SECRETARIA (Wendy) | `Person` | Exporta datos al SIE ministerial |
| SIE — Ministerio de Educación Bolivia | `System_Ext` | Recibe exportación de calificaciones por RUDE |
| AWS KMS | `System_Ext` | Cifrado en reposo de PII (RUDE, nombre, fecha de nacimiento) |

### Contenedores (Level 2)

| Contenedor | Tecnología | Protocolo | Justificación (DA/UC) |
|------------|-----------|-----------|----------------------|
| `angular-spa` | Angular 17 | HTTPS/REST | Frontend reactivo — DA-01 |
| `api-gateway` | Spring Boot 3.3, Java 21 | HTTPS/REST | Punto de entrada único — DA-01 |
| `domain-layer` | Java 21, arquitectura hexagonal | In-process | Lógica de negocio aislada — DA-02 |
| `postgres-rls` | PostgreSQL 15 (RDS Multi-AZ) | JDBC/TLS | Aislamiento multitenant por RLS — DA-01 |
| `event-bus` | Spring Events → AWS SQS | AMQP/HTTPS | Consolidación asíncrona — DA-04 |
| `sie-adapter` | Java 21, Resilience4j | HTTPS/REST | Resiliencia SIE con circuit breaker — DA-05 |
| `scheduler` | Spring Scheduler | In-process | Ventanas de corrección + reintentos SIE |

### Componentes clave por contenedor (Level 3)

**`api-gateway`** (Spring Boot / Infraestructura web):
- `JwtAuthFilter` — valida JWT, extrae rol y tenant_id (NFR-008)
- `TenantContextInjector` — ejecuta `SET LOCAL app.tenant_id` antes de cada TX (DA-01)
- `CalificacionController` — POST /api/v1/calificaciones (FSD-UC-001)
- `CentralizadorController` — GET /api/v1/centralizadores (FSD-UC-003)
- `ExportacionController` — POST /api/v1/exportaciones/sie (FSD-UC-004)
- `CorreccionController` — POST/PUT /api/v1/correcciones (FSD-UC-005)
- `PeriodoController` — POST /api/v1/periodos (FSD-UC-009)
- `AuditLogAspect` — registra toda escritura en `audit_log` en la misma TX (DA-03)
- `GlobalExceptionHandler` — mapea `DomainException` → HTTP status + `ErrorResponseDTO`

**`domain-layer`** (Hexagonal — sin dependencias de Spring):
- `CalificacionDomainService` — valida rango paramétrico, emite `CalificacionRegistradaEvent`
- `ConsolidacionDomainService` — único responsable de `Math.floor()` y cálculo de promedios (BR-008)
- `ExportacionDomainService` — mapeo RUDE, idempotencia, estado `EN_PROGRESO`/`COMPLETADO`
- `CorreccionDomainService` — ventana temporal `ventana_fin`, append-only (BR-005, BR-009)
- Entidades inmutables: `@Immutable CalificacionEntity`, `@Immutable AuditLogEntity`

**`sie-adapter`** (Integración externa):
- `SIEHttpClient` — circuit breaker Resilience4j, timeout 30 s, backoff exponencial
- `SIERetryScheduler` — reintenta registros `PENDIENTE` cada 5 min (DA-05)
- `VentanaExpiracionScheduler` — revoca `AutorizacionCorreccion` expiradas (BR-009)

---

## 5. Procedimiento (4 pasos)

### Paso 1 — discovery
Identificar desde el FSD:
- ≥ 3 UC críticos que justifican contenedores.
- Actores externos (DIRECTOR, DOCENTE, SECRETARIA, SIE, KMS).
- Restricciones cross-cutting: multitenancy (DA-01), inmutabilidad (DA-03), `floor()` (BR-008).

### Paso 2 — draft
Emitir el bloque Mermaid del nivel solicitado usando la arquitectura de §4.
- Nivel 1: incluir `System_Ext` para SIE y KMS.
- Nivel 2: incluir todos los contenedores de §4, tecnología y protocolo en cada `Rel`.
- Nivel 3: descomponer el contenedor indicado en los componentes de §4; citar UC/BR en cada componente.

### Paso 3 — validate
Cruzar contra el FSD:
- Cada FSD-UC-001, 003, 004, 005, 009 tiene su contenedor/componente.
- Ningún contenedor sin UC que lo justifique.
- `floor()` aparece **únicamente** en `ConsolidacionDomainService` — nunca en controllers, SQL ni frontend (BR-008).
- El `audit_log` se escribe en el `AuditLogAspect`, **no** en los controllers (DA-03).
- Reportar gaps explícitamente.

### Paso 4 — refine
- Cerrar todos los gaps detectados.
- Si nivel 2 y el contenedor crítico es `domain-layer` o `api-gateway`, bajar a nivel 3 automáticamente.
- Emitir tabla de trazabilidad obligatoria (ver §6).

---

## 6. Salida esperada

### Archivo Mermaid
Guardar en `docs/diagrams/c4_level<N>[_<contenedor>].mmd`.
- Una sentencia por línea, indentación consistente, sin caracteres Unicode decorativos en labels (IG-10).

### Tabla de trazabilidad obligatoria

| FSD-UC | Contenedor C4 | Componente (nivel 3) | DA/BR aplicado |
|--------|---------------|---------------------|----------------|
| FSD-UC-001 | `api-gateway` | `CalificacionController` + `CalificacionDomainService` | BR-002 (rango), DA-03 (audit_log) |
| FSD-UC-003 | `domain-layer` | `ConsolidacionDomainService` | BR-008 (`floor()`), DA-04 (async) |
| FSD-UC-004 | `sie-adapter` | `SIEHttpClient` + `SIERetryScheduler` | DA-05 (circuit breaker) |
| FSD-UC-005 | `api-gateway` + `domain-layer` | `CorreccionController` + `CorreccionDomainService` + `VentanaExpiracionScheduler` | BR-005, BR-009 (ventana) |
| FSD-UC-009 | `api-gateway` | `PeriodoController` | BR-006 (secuencia periodos) |

---

## 7. Verificación ("bien hecho")

- [ ] Cabecera Mermaid coincide con el nivel (`C4Context` / `C4Container` / `C4Component`).
- [ ] Cada `Container`/`Component` tiene tecnología explícita (ej. "PostgreSQL 15", no "DB").
- [ ] Cada `Rel` tiene protocolo explícito ("HTTPS/REST", "JDBC/TLS", "Spring Event").
- [ ] `Math.floor()` solo aparece en `ConsolidacionDomainService` (BR-008).
- [ ] `audit_log` solo se escribe desde `AuditLogAspect` (DA-03).
- [ ] `tenant_id` y RLS están modelados como cross-cutting concern en el contenedor `postgres-rls` (DA-01).
- [ ] El bloque Mermaid renderiza sin errores en [mermaid.live](https://mermaid.live).
- [ ] Tabla de trazabilidad cubre los 5 FSD-UC críticos.
- [ ] Ningún contenedor sin FSD-UC que lo justifique.

---

## 8. Anti-patrones EduSync

| Anti-patrón | Mitigación |
|-------------|-----------|
| `floor()` en SQL, controller o frontend | Solo en `ConsolidacionDomainService` (BR-008) |
| Escritura en `audit_log` fuera de la TX | `AuditLogAspect` en la misma `@Transactional` (DA-03) |
| `tenant_id` sin política RLS | Toda tabla nueva requiere `CREATE POLICY` + `TenantContextInjector` (DA-01) |
| Exponer entidad JPA directamente en API | Usar DTOs en `infrastructure/web/dto/` (AGENTS.md §5) |
| Nivel 4 (Code) sin FSD completo | Responder con advertencia y pedir justificación explícita |
| Contenedor sin `Rel` a PostgreSQL | Todo servicio con persistencia **MUST** pasar por `postgres-rls` |
| RUDE en texto visible en diagrama | Modelar como `studentId` (referencia interna), nunca como dato PII expuesto |

---

## 9. Mini ejemplo de invocación

```
@c4-edusync nivel 2

Fuente: docs/fsd/FSD_EduSync.md
UC críticos: FSD-UC-001 (calificaciones), FSD-UC-003 (consolidación),
             FSD-UC-004 (exportación SIE), FSD-UC-009 (periodos)
Stack: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS ECS Fargate, SQS
Luego baja a nivel 3 del contenedor api-gateway.
```

---

## 10. Modos de fallo conocidos

- FSD menciona BR-004 (RUDE) pero el diagrama usa nombre/apellido → STOP, corregir.
- Dos FSD-UC mapeados al mismo componente sin justificación → reportar gap, proponer split.
- `validate` detecta > 3 gaps → recomendar revisar el FSD antes de continuar.
- Usuario pide **nivel 4 (Code)** → responder: "El nivel 4 está fuera del alcance del skill c4-edusync. El LFSD (`docs/LFSD-EduSync.md`) ya documenta la lógica de clase con pseudocódigo. Solicita justificación explícita antes de continuar."
- Usuario pide **Deployment diagram** → redirigir a `infra/` (Terraform/ECS) — fuera de alcance.

---

## 11. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1.0 | 17/05/2026 | Rodrigo Aspeti | Versión inicial — adaptación de plantillas/c4.md al proyecto EduSync; stack Java 21/Spring Boot 3.3/PostgreSQL 15/Angular 17/AWS; 5 FSD-UC críticos, 7 contenedores, 16 componentes |
