---
producto: "EduSync"
grupo: "G-EduSync"
documento: DTP                 # Documento Técnico del Producto (continuación VIVA del DTI)
version: v1.2                  # versiona la implementación, no el diseño de M4
fecha: "28/05/2026"
status: vivo                   # vivo | en_revision | publicado-release   (NUNCA "congelado")
audiencia: dual                # humanos + agentes IA
baseline_ref:                  # el baseline CONGELADO del que parte este DTP (M4)
  dti: "docs/baseline/DTI.md"
  tag: "release/2.0.0"
  commit: "<sha pendiente — se completa al taggear release/2.0.0>"
release: "release/3.0.0"       # release vivo que este DTP describe
stack:
  - "Java 25 (LTS)"
  - "Spring Boot 4.1.0 (Spring Framework 7.0.8)"
  - "Angular 21 (LTS)"
  - "PostgreSQL 15 RLS"
  - "AWS ECS Fargate"
repo: "https://github.com/rodrigo-aspeti/edusync"
agents_md: "/AGENTS.md"
artefactos_vivos:
  prd: "docs/product/PRD.md"          # copia VIVA (no el PRD congelado de M4)
  fsd: "docs/product/FSD.md"          # copia VIVA en modo LFSD ⚡
  prompt_mapping: "docs/PROMPT_MAPPING.md"
  design_docs_dir: "docs/design/"     # DD-UC-NNN
  adr_dir: "docs/adr/"
---

# Documento Técnico del Producto (DTP) — EduSync

> **Qué es**: el DTP es la **continuación viva del DTI**. Donde el DTI fue *"el plano"* (foto técnica congelada al cierre de M4, `release/2.0.0`, ver `docs/baseline/DTI.md`), el DTP es *"el DTI que compila"*: el **contrato técnico vigente** del producto mientras se implementa. Consolida la entrega final (documentación + software).
>
> **Regla de oro** (heredada del modelo documental de M4, ver [`plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../../plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md)): **cero divergencia silenciosa**. Si el código necesita contradecir una decisión del DTI vFinal, primero se actualiza el ADR + este DTP + la spec viva; **nunca al revés**.
>
> **Qué NO es**: este DTP **no reescribe** el baseline congelado (`BRD/MRD/PRD/FSD clásico + DTI` en `docs/baseline/`, recuperable por el tag `release/2.0.0`). El baseline es el registro histórico evaluado de M4 y permanece intacto (ver `.cursor/rules/baseline-congelado.mdc` y `CODEOWNERS`).
>
> ⚠️ **Punto de partida (`v1.0`)**: esta primera versión del DTP es un **placeholder inicial**. `release/3.0.0` todavía no ha arrancado (`src/` está vacío en `AGENTS.md` §3): no existe ningún `DD-UC-NNN` ni `PR-IMPL-NNN` todavía. §A.1–A.4 no tienen filas reales más allá del delta de stack de `ADR-0008`, y §B marca `no` (sin cambios) en casi todas las secciones porque nada se ha implementado aún. Este documento se puebla incrementalmente con cada design doc y cada PR de implementación, vía el skill `@dtp-sync`.

## Cómo se origina

1. Se copia el **DTI vFinal / congelado** (`docs/baseline/DTI.md`, tag `release/2.0.0`) como punto de partida conceptual del DTP (`status: vivo`).
2. A partir de aquí, **todo cambio técnico** entra por el flujo de control de cambios (ver §A).
3. El baseline de M4 queda inmutable en `docs/baseline/` + tag `release/2.0.0`.

```mermaid
flowchart LR
  DTI["DTI congelado (docs/baseline/DTI.md, release/2.0.0)"] -->|copia inicial| DTP["DTP v1.0 (vivo, este documento)"]
  FSDv["FSD vivo (docs/product/FSD.md, LFSD, FSD-UC-NNN)"] --> DD["Design Doc (DD-UC-NNN, docs/design/)"]
  DD --> Code["PR / codigo (src/)"]
  Code --> DTP
  ADR["ADR (docs/adr/, si hay decision)"] --> DTP
  ADR8["ADR-0008 ya cerrado (stack Java 25 / Boot 4.1.0 / Angular 21)"] --> DTP
  ADR9["ADR-0009 ya cerrado (generalizacion modelo dominio SaaS multi-tenant)"] --> DTP
  ADR10["ADR-0010 ya cerrado (multi-rol + SysAdmin sin tenant permanente)"] --> DTP
```

---

## A. Control de cambios (núcleo del DTP) `[humano+máquina]`

> Esta sección es lo que distingue al DTP del DTI. Todo cambio técnico durante la implementación se registra aquí.

### A.1 Changelog de implementación

| Fecha | Cambio | Disparador (FSD-UC / DD / hallazgo) | ADR | PR / commit | Autor |
|-------|--------|-------------------------------------|-----|-------------|-------|
| 28/05/2026 | Apertura de la capa viva (`docs/product/`) y creación de este DTP v1.0 como punto de partida | Cierre de M4 → transición a `release/3.0.0` (`plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`) | — | `pendiente de commit formal` | Rodrigo Aspeti |
| 28/05/2026 | Fijación del stack vivo: Java 21/Boot 3.3/Angular 17 (baseline) → Java 25 LTS/Spring Boot 4.1.0/Angular 21 LTS (vivo) | Apertura de `release/3.0.0` sobre `src/` vacío (greenfield, sin costo de migración) | `ADR-0008` | `pendiente de commit formal` | Rodrigo Aspeti |
| 12/07/2026 | Generalización del modelo de dominio a plataforma SaaS multi-tenant configurable: nuevo rol `SysAdmin` + entidad `Tenant` con suscripción; módulos configurables `GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion`/`TipoEvaluacion`/`Evaluacion`/`Curso`/`Paralelo`/`Materia`/`Estudiante`/`Inscripcion`/`Usuario` añadidos como extensión aditiva sobre el Perfil Bolivia SIE (sin modificarlo) | Nuevo diseño funcional recibido para `release/3.0.0` (roles SaaS, periodos y secciones de evaluación configurables, estructura académica genérica) | `ADR-0009` | `pendiente de commit formal` | Rodrigo Aspeti |
| 14/07/2026 | Refinamiento del modelo de roles: `Usuario.rol` (valor único, `ADR-0009`) reemplazado por relación N:M `UsuarioRol` (multi-rol); invariante permanente `tenant_id IS NULL ⟺ roles = {SYSADMIN}` (no transitoria de *bootstrap*) | Clarificación de negocio recibida durante el diseño del login y del alta de tenants (multi-rol operativo + alcance del rol SysAdmin) | `ADR-0010` | `pendiente de commit formal` | Rodrigo Aspeti |

### A.2 Deltas respecto al DTI vFinal

> Diferencias **deliberadas** entre lo diseñado en M4 y lo construido. Cada delta significativo exige un ADR.

| # | Sección del DTI afectada | Qué decía el DTI vFinal | Qué dice ahora el DTP | Motivo | ADR |
|---|--------------------------|-------------------------|-----------------------|--------|-----|
| 1 | `§4 Stack tecnológico` (`docs/baseline/DTI.md`, también reflejado en `AGENTS.md` §4) | Java 21 (LTS) / Spring Boot 3.3 / Angular 17 | Java 25 (LTS) / Spring Boot 4.1.0 (Spring Framework 7.0.8) / Angular 21 (LTS) | `src/` está vacío al cerrar M4 (greenfield): se adopta el stack más reciente estable disponible sin costo de migración, priorizando AOT Cache de Boot 4 sobre Java 25 y la ventana LTS de Angular 21 | `ADR-0008` |
| 2 | `§4 Modelo de dominio` (`docs/baseline/DTI.md`) | Modelo de dominio específico de Bolivia: tenant = colegio individual (sin capa SaaS), 3 roles (`DIRECTOR`/`SECRETARÍA`/`DOCENTE`), 3 periodos trimestrales fijos, dimensiones pedagógicas fijas (Ser/Saber/Hacer/Decidir/Autoevaluación), truncado `floor()` único, identidad exclusiva por RUDE | Se añade, como extensión aditiva (sin reemplazar lo anterior): capa de plataforma SaaS (`SysAdmin`, `Tenant` con suscripción), roles ampliados (`ADMIN`=`DIRECTOR`, `PROFESOR`=`DOCENTE`, + `SECRETARIA`, `ASESOR` nuevo), `GestionEscolar` con N periodos configurables, `SeccionEvaluacion`/`TipoEvaluacion` configurables, `Curso`/`Paralelo`/`Materia`/`Estudiante`/`Inscripcion`/`Usuario` genéricos | Nuevo diseño funcional recibido para `release/3.0.0`: EduSync se generaliza a plataforma SaaS multi-tenant configurable, sin perder el Perfil Bolivia SIE (que sigue soportado sin cambios) | `ADR-0009` |
| 3 | `BR-024` (`docs/product/BRD.md`, introducida por `ADR-0009`) | `Usuario.rol`: atributo ENUM de valor único ("exactamente un rol vigente") | `UsuarioRol`: relación N:M ("uno o más roles simultáneos"), con invariante permanente `Usuario.tenant_id IS NULL ⟺ roles = {SYSADMIN}` | Clarificación de negocio: una misma persona puede cubrir más de una función en su institución (multi-rol); el alcance de `tenant_id` nulo para `SYSADMIN` se confirma permanente, no transitorio de *bootstrap* | `ADR-0010` |

> **Pendiente de definición (`ADR-0009` §3, ver también `docs/product/FSD.md` §4.6/§5.1/§6.3):** (1) reconciliación entre `GestionAcademica`/`ParametroAcademico` (Perfil Bolivia) y `GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion` (genérico); (2) generalización de secuencialidad de apertura y de promedio final a N periodos; (3) criterio de redondeo del cálculo de notas genérico; (4) validación de suma de pesos porcentuales de secciones; (5) gobernanza (auditoría/inmutabilidad) de los módulos nuevos. Ninguno debe implementarse en código sin un Design Doc o ADR de seguimiento que lo resuelva.
>
> **Pendiente de definición (`ADR-0010` §3, no bloqueante):** (1) el diseño detallado del tenant "demo" (primer tenant del sistema, funcionalidad de producto real para ventas) — no afecta el modelo de `Usuario`/`Rol`/`tenant_id`, se resuelve en el Design Doc de `FSD-UC-011`; (2) posible combinación futura `SYSADMIN` + rol de tenant (requeriría separar identidad de membresía en `usuario_tenant_rol`) — sin necesidad de negocio confirmada, no se construye ahora.

### A.3 Estado de implementación por FSD-UC

| FSD-UC | Design Doc | Estado | Release | Tests/Evals | Notas |
|--------|------------|--------|---------|-------------|-------|
| `FSD-UC-001` (Registro de calificaciones) | — | pendiente | `release/3.0.0` | — | Sin `DD-UC-NNN` creado todavía; usar `@feature-design-doc` para iniciar |
| `FSD-UC-003` (Consolidación de centralizadores) | — | pendiente | `release/3.0.0` | — | Ídem |
| `FSD-UC-004` (Exportación SIE) | — | pendiente | `release/3.0.0` | — | Ídem |
| `FSD-UC-005` (Modificación retroactiva) | — | pendiente | `release/3.0.0` | — | Ídem |
| `FSD-UC-009` (Administración de periodos) | — | pendiente | `release/3.0.0` | — | Ídem |
| `FSD-UC-011`..`FSD-UC-021` (Módulos generalizados: Tenants, Gestión Escolar, Periodos/Secciones/Evaluaciones configurables, Cálculo de Notas, Cursos/Paralelos, Materias, Profesores, Estudiantes/Inscripciones, Usuarios) | — | pendiente | `release/3.0.0` | — | Nuevos desde `ADR-0009`; sin `DD-UC-NNN` todavía. 5 puntos pendientes de definición antes de iniciar diseño (ver `ADR-0009` §3). `FSD-UC-021` (Usuarios y Roles) y el `SYSADMIN` de `FSD-UC-011` refinados por `ADR-0010` (multi-rol + `tenant_id` nulo permanente); diseño del tenant demo pendiente y no bloqueante (ver `ADR-0010` §3) |

### A.4 Trazabilidad código ↔ DTP

> Cadena completa por feature. Debe poder reconstruirse para cualquier línea del producto.

`BRD/MRD (baseline)` → `PRD/FSD vivo (FSD-UC-NNN)` → `Design Doc (DD-UC-NNN)` → `Prompt (PR-IMPL-NNN)` → `PR/commit` → `Tests/Evals` → `ADR (si aplica)` → **DTP**.

Estado actual: la cadena existe completa hasta `PRD/FSD vivo` (`docs/product/PRD.md`, `docs/product/FSD.md`). No hay todavía ningún `DD-UC-NNN`, `PR-IMPL-NNN` ni código en `src/`. El único eslabón poblado fuera de specs es el delta de stack (`ADR-0008`).

---

## B. Contenido técnico vigente `[humano+máquina]`

> El DTP mantiene **al día** las mismas secciones que el DTI (no se duplica la plantilla: se reusa la estructura de [`DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md`](../../plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md)). Para cada sección, si **no cambió** respecto al DTI vFinal, basta referenciarla; si **cambió**, se reescribe aquí y se registra el delta en §A.2.

| Sección (espejo del DTI) | ¿Cambió vs DTI vFinal? | Dónde está la versión vigente |
|--------------------------|------------------------|-------------------------------|
| §1 Visión del producto | no | `docs/baseline/DTI.md` §1 |
| §2 Contexto del sistema (C4 N1) | no | `docs/baseline/DTI.md` §2 / `docs/diagrams/c4_level1.mmd` |
| §3 Arquitectura de alto nivel (C4 N2/N3) | no | `docs/baseline/DTI.md` §3 / `docs/diagrams/c4_level2.mmd`, `c4_level3_*.mmd` |
| §3.5 Contenedores agénticos | no | `docs/baseline/DTI.md` §3.5 |
| §4 Modelo de dominio | **sí** (extensión aditiva) | Este DTP §A.2 (filas 2 y 3) + `ADR-0009`/`ADR-0010` — capa SaaS, módulos configurables y modelo multi-rol (`UsuarioRol`) añadidos sobre `docs/baseline/DTI.md` §4, que sigue vigente para el Perfil Bolivia SIE |
| §4 Stack tecnológico | **sí** | Este DTP §A.2 (fila 1) + `ADR-0008` — Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS |
| §5 Arquitectura hexagonal del core | no | `docs/arquitectura_hexagonal_EduSync.md` |
| §6 Distribuida (si aplica) | no | `docs/baseline/DTI.md` §6 (Seams, sin activar — `ADR-0007` Strangler Fig sigue *gated*) |
| §7 Asíncrona / event-driven | no | `docs/baseline/DTI.md` §7 (Spring Events; migración a SQS FIFO prevista en `ADR-0004`, no ejecutada aún) |
| §8 Despliegue cloud | no | `docs/baseline/DTI.md` §8 / `docs/diagrams/deployment_aws.mmd` (imagen Docker deberá basarse en OpenJDK 25 al implementarse, ver `ADR-0008` §5) |
| §9 Capa de IA / agentes | no | `docs/baseline/DTI.md` §9 |
| §10 Prompt mapping | sí (crece con `PR-IMPL-*`) | `docs/PROMPT_MAPPING.md` (área `IMPL`, aún sin filas) |
| §11 NFRs | no | `docs/baseline/DTI.md` §11 |
| §12 POCs | no | `docs/baseline/DTI.md` §12 / `docs/pocs/POC-01-rls-multitenancy/`, `docs/pocs/POC-02-circuit-breaker-sie/` (ejecución con evidencia real sigue pendiente) |
| §13–§16 Seguridad / Observabilidad / DevOps / Antipatrones | no | `docs/baseline/DTI.md` §13–§16 |
| §21 ADRs | sí (crece) | `docs/adr/` — 9 ADRs vigentes (`0001`–`0006`, `0008`, `0009`, `0010`) |
| §22–§23 Auditoría IA / Evals | no todavía | `docs/baseline/DTI.md` §22–§23 |

> **Solo escribir aquí las secciones que cambiaron.** Las que no cambiaron se mantienen por referencia al DTI vFinal, preservando un único punto de verdad por release.

---

## Checklist del DTP (entrega de implementación)

- [x] Frontmatter con `baseline_ref` (`docs/baseline/DTI.md` + tag `release/2.0.0`) y `status: vivo`.
- [x] §A.1 Changelog de implementación poblado y al día (2 filas: apertura de capa viva + delta de stack).
- [x] §A.2 Deltas vs DTI vFinal, **cada uno con ADR** (3 deltas → `ADR-0008` stack, `ADR-0009` generalización del modelo de dominio, `ADR-0010` multi-rol + `SysAdmin` sin tenant permanente).
- [x] §A.3 Estado por FSD-UC con su Design Doc (todos `pendiente`, sin `DD-UC-NNN` aún — esperado en un DTP punto de partida; incluye `FSD-UC-011`..`FSD-UC-021`).
- [ ] §A.4 Trazabilidad código ↔ DTP reconstruible para cada feature — **pendiente**: no hay código todavía.
- [x] §B: solo secciones cambiadas reescritas (stack + prompt mapping + ADRs); el resto referencia al DTI vFinal.
- [ ] `docs/PROMPT_MAPPING.md` ampliado con prompts de implementación (`PR-IMPL-*`) — **pendiente**: área `IMPL` reservada, sin filas todavía.
- [x] `AGENTS.md` sincronizado (pendiente de ejecutar en este mismo plan).
- [x] Baseline congelado (`docs/baseline/`) **intacto** (sin commits que lo modifiquen; protegido por `CODEOWNERS` y `.cursor/rules/baseline-congelado.mdc`).

## Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v1.0 | 28/05/2026 | Rodrigo Aspeti | Creación del DTP como punto de partida de la capa viva (`release/3.0.0`), a partir de `plantillas/plantillas3/DTP_TEMPLATE.md`; registra el delta de stack `ADR-0008` (Java 25 LTS + Spring Boot 4.1.0 + Angular 21 LTS); §A.3 lista los 5 FSD-UC del baseline como `pendiente`; sin `DD-UC-NNN` ni `PR-IMPL-NNN` todavía. |
| v1.1 | 12/07/2026 | Rodrigo Aspeti | Registra el delta de generalización del modelo de dominio `ADR-0009` (§A.2 fila 2): plataforma SaaS multi-tenant configurable añadida como extensión aditiva sobre el Perfil Bolivia SIE, alineada con `docs/product/BRD.md` v3.0, `PRD.md` v2.0 y `FSD.md` v2.0. §A.3 añade `FSD-UC-011`..`FSD-UC-021` como `pendiente`. §B actualiza §4 Modelo de dominio (extensión aditiva) y el conteo de ADRs vigentes (9). Deja registrados 5 puntos pendientes de definición (ver `ADR-0009` §3) que deben resolverse antes de implementar código sobre los módulos nuevos. |
| v1.2 | 14/07/2026 | Rodrigo Aspeti | Registra el delta de refinamiento del modelo de roles `ADR-0010` (§A.2 fila 3): `BR-024`/`FSD-UC-021` pasan de "un rol por usuario" a multi-rol (`UsuarioRol` N:M), con la invariante permanente `tenant_id IS NULL ⟺ roles = {SYSADMIN}`, alineado con `docs/product/BRD.md` v3.1, `PRD.md` v2.2 y `FSD.md` v2.2. §A.3 anota `FSD-UC-011`/`FSD-UC-021` como refinados por `ADR-0010`. §B actualiza el conteo de ADRs vigentes (8 → 9, incluye `0010`). Deja registrado como pendiente no bloqueante el diseño del tenant demo (ver `ADR-0010` §3), sin afectar el modelo de `Usuario`/`Rol` decidido. |
