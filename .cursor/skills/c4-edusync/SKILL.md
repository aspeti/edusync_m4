---
name: c4-edusync
description: >-
  Autor y validador de diagramas C4 (Context, Container, Component) en Mermaid
  para el proyecto EduSync. Activar cuando el usuario pide "diagrama C4 nivel N
  de EduSync", edita un archivo docs/diagrams/c4_*.mmd, o quiere visualizar la
  arquitectura hexagonal (Java 21, Spring Boot 3.3, PostgreSQL 15 RLS, Angular 17,
  AWS ECS). Consume FSD_EduSync.md y LFSD-EduSync.md; produce el bloque Mermaid
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
- Para nivel 3: nombre del contenedor (`api-gateway`, `domain-layer`, `sie-adapter`, `event-bus`, `postgres-rls`, `scheduler`).
- **Artefactos de entrada canónicos (rutas verificadas a 28/05/2026)** — leer en este orden:

| # | Ruta | Versión | Aporta |
|---|------|---------|--------|
| 1 | `docs/fsd/FSD_EduSync.md` | v1.0 | FSD-UC-001 / 003 / 004 / 005 / 009 + BR-001..BR-012 + NFRs |
| 2 | `docs/LFSD-EduSync.md` | v1.0.1 | DDL, contratos API, secuencias, paquetes Java, seguridad hexagonal |
| 3 | `docs/arquitectura_funcional_EduSync.md` | v1.0 | DA-01..DA-05 (multitenancy, hexagonal, audit_log, async, resiliencia) |
| 4 | `docs/arquitectura_hexagonal_EduSync.md` | v0.1 | 20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 Aggregate Roots |
| 5 | `docs/dtos_EduSync.md` | v0.1 | 4 Request, 4 Commands, 3 Response, 5 Domain Events, 5 enums |
| 6 | `docs/DTI.md` | v0.3 | §2 contexto, §3 contenedores, §5 hexagonal, §6.2 seams, §7 eventos, §8 AWS |
| 7 | `docs/adr/0001..0006-*.md` | Aceptada | justificación arquitectónica por contenedor (ver §3) |
| 8 | `docs/pocs/POC-01-rls-multitenancy/README.md` | v0.1 | evidencia experimental para `postgres-rls` (DA-01) |
| 9 | `docs/pocs/POC-02-circuit-breaker-sie/README.md` | v0.1 | evidencia experimental para `sie-adapter` (DA-05) |
| 10 | `AGENTS.md` | v0.5 | stack autoritativo, guardrails, golden tests |
| 11 | `docs/PROMPT_MAPPING.md` | v1.3 | trazabilidad PR-C4-001 / PR-C4-002 + PR-DIAG-* |

Si falta el nivel: responder `"Necesito el nivel C4 (1, 2 o 3) y, para nivel 3, el contenedor objetivo."`
Si falta el FSD canónico (`docs/fsd/FSD_EduSync.md`): responder `"E_MISSING_FSD: no encuentro docs/fsd/FSD_EduSync.md — el skill c4-edusync requiere el FSD v1.0 mínimo."`

### Estado de diagramas en `docs/diagrams/` (a 28/05/2026)

#### 2.1 Diagramas C4 (alcance directo del skill)

| Archivo | Tipo C4 | Estado | Origen |
|---------|---------|--------|--------|
| `docs/diagrams/c4_level1.mmd` | C4Context | Generado | PR-C4-001 |
| `docs/diagrams/c4_level2.mmd` | C4Container | Generado | PR-C4-002 |
| `docs/diagrams/c4_level3_api_gateway.mmd` | C4Component | **Pendiente** — extraer de `docs/DTI.md §3.3` y completar con FSD-UC-001/004/005/009 |
| `docs/diagrams/c4_level3_domain_layer.mmd` | C4Component | **Pendiente** — fuente `docs/arquitectura_hexagonal_EduSync.md` + DTI §5 |
| `docs/diagrams/c4_level3_sie_adapter.mmd` | C4Component | **Pendiente** — fuente ADR-0005 + DTI §6.1 |
| `docs/diagrams/deployment_aws.mmd` | C4Deployment | **Pendiente** — fuente DTI §8 + ADR-0006 (mapeo AWS por capa para criterio 2 de la rúbrica de defensa) |

#### 2.2 Diagramas no-C4 que viven en la misma carpeta (fuera del alcance directo del skill)

| Archivo | Tipo Mermaid | Skill responsable | Estado |
|---------|--------------|-------------------|--------|
| `docs/diagrams/estados_cargar_notas.mmd` (+ `.md` espejo) | stateDiagram | `process-agent` (PR-DIAG-001) | Generado |
| `docs/diagrams/estados_administracion.mmd` (+ `.md` espejo) | stateDiagram | `process-agent` (PR-DIAG-002) | Generado |
| `docs/diagrams/ai-sdlc.mmd` | flowchart LR | `docs-agent` | Generado (sin `.md` espejo — IG-09 pendiente) |
| `docs/diagrams/estados.cargarnotas.mmd` | stateDiagram | — | **Drift** — duplicado de `estados_cargar_notas.mmd`; consolidar con `sync-doc-chain` |

> Si el usuario pide modificar un archivo de §2.2, **redirigir** al skill correspondiente: `process-agent` para sequence/state, `fsd-modelo-datos-a-jpa-flyway` para erDiagram, `strangler-fig-migrator` para diagramas de migración v1.x → v2.0.

Antes de generar cualquier archivo, leer el archivo existente con `Read` para determinar si hay drift con el FSD actual.

---

## 3. Fuentes de verdad (orden de precedencia)

> Regla: usar el archivo con mayor `_vN`; si no hay sufijo, equivale a v1.0. Resolver rutas con `Glob`/`Read` antes de citarlas; nunca asumir paths legacy (`docs/FSD_EduSync.md`, `docs/lfsd/`, `docs/diagramas/` están **prohibidos** por IG-08).

1. **FSD vigente** — `docs/fsd/FSD_EduSync.md` v1.0. Cubre **FSD-UC-001** (registro), **FSD-UC-003** (consolidación), **FSD-UC-004** (exportación SIE), **FSD-UC-005** (corrección retroactiva), **FSD-UC-009** (periodos) + BR-001..BR-012 + tabla de NFRs. Cualquier `Container` o `Component` SIN FSD-UC que lo justifique se reporta como gap en el paso `validate` (§5).
2. **LFSD vigente** — `docs/LFSD-EduSync.md` v1.0.1. Cubre paquetes Java (`domain/`, `application/`, `adapter/in/web`, `adapter/out/persistence`, `adapter/out/integration/sie`), contratos REST, DDL Flyway, secuencias y seguridad hexagonal.
3. **Arquitectura hexagonal detallada** — `docs/arquitectura_hexagonal_EduSync.md` v0.1. Catálogo canónico de **20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 Aggregate Roots**. Fuente preferente para C4 Level 3 del `domain-layer`.
4. **Catálogo de DTOs por capa** — `docs/dtos_EduSync.md` v0.1. **4 Request DTO, 4 Command, 3 Response, 5 Domain Events, 5 enums**. Útil para Level 3 del `api-gateway` (saber qué cruza el límite hexagonal).
5. **Arquitectura funcional** — `docs/arquitectura_funcional_EduSync.md` v1.0. DA-01..DA-05 (multitenancy RLS, hexagonal, audit_log inmutable, consolidación async, resiliencia SIE).
6. **DTI** — `docs/DTI.md` v0.3:
   - §2.1 / §3.2 / §3.3 — bloques Mermaid C4 ya embebidos (verificar antes de regenerar).
   - §3.4 — sequence diagram FSD-UC-001 (referencia para Level 3 del `api-gateway`).
   - §5 — hexagonal del core (puertos y adaptadores).
   - §6.1 — patrones de resiliencia aplicados (input para `sie-adapter`).
   - §6.2 — seams de descomposición monolito modular → v2.0 microservicios (Strangler Fig).
   - §7 — catálogo de eventos de dominio (input para `event-bus`).
   - §8 — mapeo a AWS (input para `deployment_aws.mmd`).
7. **ADRs formales** — `docs/adr/NNNN-*.md` (estado **Aceptada**, fechados 17/05–28/05/2026):
   - `docs/adr/0001-multitenancy-rls-postgresql.md` → contenedor `postgres-rls`.
   - `docs/adr/0002-parametrizacion-reglas-normativas.md` → contenedor `domain-layer` (motor de parámetros académicos).
   - `docs/adr/0003-persistencia-inmutable-audit-log.md` → componente `AuditLogAspect` dentro de `api-gateway`.
   - `docs/adr/0004-async-consolidacion-spring-events.md` → contenedor `event-bus`.
   - `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` → contenedor `sie-adapter`.
   - `docs/adr/0006-cloud-provider-y-estilo-de-despliegue.md` → diagrama `deployment_aws.mmd` (ECS Fargate, RDS Multi-AZ, ALB, KMS).
8. **POCs experimentales** — `docs/pocs/POC-NN-<slug>/README.md`:
   - POC-01 (RLS multitenancy) → validación empírica de `postgres-rls` (Level 2) y `TenantContextInjector` (Level 3).
   - POC-02 (Circuit breaker SIE) → validación empírica del `sie-adapter` (Level 3).
   - Si la POC está cerrada con veredicto `fail`, **NO** dibujar el contenedor/componente como si funcionara; reportar gap y proponer rediseño antes de continuar.
9. **AGENTS y mapeo de prompts** — `AGENTS.md` v0.5 (stack, guardrails, golden tests `FloorTest` / `SIEPayloadTest` / `VentanaTest` / `MultitenantTest`) y `docs/PROMPT_MAPPING.md` v1.3 (trazabilidad `PR-C4-001`, `PR-C4-002`, `PR-DIAG-001`, `PR-DIAG-002` y los nuevos `PR-C4-003..PR-C4-006` cuando se generen los pendientes de §2.1).

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
- Nivel 2: incluir todos los contenedores de §4, tecnología y protocolo en cada `Rel`. Añadir nota de seam en `domain-layer` ↔ `event-bus` (Seam 1) y en `sie-adapter` (Seam 2, candidato a microservicio `edusync-sie-exporter` en v2.0). Ver DTI §6.2.
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

- **`E_MISSING_FSD`** — no existe `docs/fsd/FSD_EduSync.md` → STOP, pedir aclaración; no inventar UCs.
- **`E_FSD_DRIFT`** — el FSD menciona BR-004 (RUDE) pero el diagrama usa nombre/apellido → STOP, corregir antes de guardar.
- **`E_COMPONENT_OVERLOAD`** — dos FSD-UC distintos mapeados al mismo componente sin justificación → reportar gap, proponer split.
- **`E_GAPS_EXCEEDED`** — `validate` detecta > 3 gaps → recomendar revisar el FSD antes de continuar al paso `refine`.
- **`E_LEVEL_4_REQUEST`** — usuario pide **nivel 4 (Code)** → responder: "El nivel 4 está fuera del alcance del skill c4-edusync. El LFSD (`docs/LFSD-EduSync.md`) ya documenta la lógica de clase con pseudocódigo. Solicita justificación explícita antes de continuar."
- **`E_WRONG_DIAGRAM_TYPE`** — usuario pide tipo de diagrama que NO es C4 → redirigir al skill correcto:
  - `sequenceDiagram` para FSD-UC-001 / 003 / 004 / 005 / 009 → `process-agent` (extraer de `docs/DTI.md §3.4` y §7.2).
  - `erDiagram` para el modelo de datos → `fsd-modelo-datos-a-jpa-flyway` (fuente: DDL del LFSD).
  - `stateDiagram` para flujos por rol → `process-agent` (ver `estados_cargar_notas.mmd` y `estados_administracion.mmd`).
  - **Deployment AWS** (`deployment_aws.mmd`) — **SÍ está en alcance** del skill `c4-edusync` usando cabecera `C4Deployment`; fuente DTI §8 + ADR-0006. Es un caso especial dentro de los *supporting diagrams* C4 (espejado en §1 del template `plantillas/c4.md`).
  - Diagramas de migración monolito → microservicios → `strangler-fig-migrator` (input: DTI §6.2).
- **`E_ADR_ACCEPTED_CONFLICT`** — el diagrama propuesto contradice un ADR `Aceptada` (p. ej. dibujar `domain-layer` con dependencia a Spring rompe ADR-0002) → STOP, escalar al `arch-agent`; no actualizar el ADR sin curación humana.
- **`E_POC_FAIL_CONFLICT`** — un componente C4 cuya POC asociada cerró con veredicto `fail` no debe dibujarse como si funcionara → reportar gap y bloquear hasta nuevo veredicto (`docs/pocs/POC-NN-*/evidencia/log.md`).

---

## 11. Registro de cambios

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|

| 0.1.0 | 17/05/2026 | Rodrigo Aspeti | Versión inicial — adaptación de plantillas/c4.md al proyecto EduSync; stack Java 21/Spring Boot 3.3/PostgreSQL 15/Angular 17/AWS; 5 FSD-UC críticos, 7 contenedores, 16 componentes | FSD v1.0, LFSD v1.0, AGENTS.md v0.2 |
| 0.2.0 | 28/05/2026 | Rodrigo Aspeti | §3 regla de versión más reciente + integración ADRs 0001-0006 + DTI §6.2 seams; §2 tabla estado diagramas; §5 Paso 2 nota seam level 2 | FSD v1.0, LFSD v1.0, DTI v0.2, ADRs 0001-0006 |
| 0.3.0 | 28/05/2026 | Rodrigo Aspeti | Cierre de drift documental — §2 amplía Entradas obligatorias a 11 artefactos con versión verificada (FSD v1.0, LFSD v1.0.1, arquitectura_funcional v1.0, **arquitectura_hexagonal v0.1**, **dtos_EduSync v0.1**, DTI v0.3, 6 ADRs, **fichas POC-01/02**, AGENTS v0.5, **PROMPT_MAPPING v1.3**); §2 tabla de estado de diagramas se divide en §2.1 C4 (incluye `deployment_aws.mmd` como pendiente para criterio 2 de la defensa final) y §2.2 no-C4 (`stateDiagram`, `flowchart`, drift de `estados.cargarnotas.mmd`); §3 reescrita con rutas exactas y mapeo ADR ↔ contenedor/componente; §10 reorganizada como modos de fallo nombrados (`E_MISSING_FSD`, `E_FSD_DRIFT`, `E_COMPONENT_OVERLOAD`, `E_LEVEL_4_REQUEST`, `E_WRONG_DIAGRAM_TYPE`, `E_ADR_ACCEPTED_CONFLICT`, `E_POC_FAIL_CONFLICT`); `deployment_aws.mmd` movido al alcance del skill via `C4Deployment` (espeja `plantillas/c4.md §1`). Sin cambios en §4 (arquitectura de referencia) ni §5 (procedimiento). | FSD v1.0, LFSD v1.0.1, DTI v0.3, AGENTS v0.5, PROMPT_MAPPING v1.3, ADRs 0001-0006, POC-01/02 docs, arquitectura_hexagonal v0.1, dtos_EduSync v0.1 |
