# AGENTS.md — EduSync

## 1. Identidad del producto

- **Nombre**: EduSync
- **Grupo**: G-EduSync
- **Dominio**: EdTech / GovTech Académico (Unidades Educativas Privadas y de Convenio — Mercado Boliviano)
- **Resumen de 1 frase**: Plataforma SaaS B2B multitenant que descentraliza el registro de calificaciones por rol, consolida centralizadores automáticamente aplicando la regla `floor` y sincroniza con el sistema estatal SIE a través del código RUDE, eliminando la triple digitación manual.

### Documentación del proyecto

| Documento | Ruta | Descripción |
|-----------|------|-------------|
| **Visión de negocio** | `01_vision_negocio.md` | Contexto del producto y problema de negocio |
| **Arquitectura funcional** | `docs/arquitectura_funcional_EduSync.md` | 10 UCs críticos + 5 DAs — fuente de verdad funcional |
| **BRD v1** | `docs/brd/BRD_EduSync_v1.md` | Business Requirements Document inicial |
| **BRD v2** | `docs/brd/BRD_EduSync_v2.md` | BRD consolidado — BR-001..BR-012, RB-01..RB-11 |
| **BRD vFinal** | `docs/brd/BRD_EduSync_vFinal.md` | Snapshot congelado de BRD v2 para `release/2.0.0` (generado por `PR-VFINAL-001`) |
| **MRD** | `docs/mrd/MRD_EduSync.md` | Market Requirements Document v1.0 |
| **MRD vFinal** | `docs/mrd/MRD_EduSync_vFinal.md` | Snapshot congelado de MRD v1.0 para `release/2.0.0` (generado por `PR-VFINAL-001`) |
| **PRD** | `docs/prd/PRD_EduSync.md` | Product Requirements Document v1.0 |
| **PRD vFinal** | `docs/prd/PRD_EduSync_vFinal.md` | Snapshot congelado de PRD v1.0 para `release/2.0.0` (generado por `PR-VFINAL-001`) |
| **FSD** | `docs/fsd/FSD_EduSync.md` | Functional Specification Document — 5 FSD-UC, ER, 3 prompt-contratos |
| **FSD vFinal** | `docs/fsd/FSD_EduSync_vFinal.md` | Snapshot congelado de FSD v1.0 para `release/2.0.0` (generado por `PR-VFINAL-001`) |
| **LFSD** | `docs/LFSD-EduSync.md` | Low-Level Functional Specification v1.0.1 — arquitectura hexagonal, DDL, APIs, diagramas de secuencia |
| **PROMPT_MAPPING** | `docs/PROMPT_MAPPING.md` | Catálogo de prompt-contratos `PR-<AREA>-NNN` — v2.34 (54 contratos activos: PR-ARCH..PR-POC-002, PR-C4-003..006, PR-ROADMAP-001, PR-APORTES-001, PR-VFINAL-001, PR-ADR-006, PR-IMPL-001..016; `PR-IMPL-001`..`016` **ejecutados**; `PR-ADR-006` = `ADR-0013`) |
| **APORTES** | `docs/APORTES_EduSync.md` | Informe de aportes individuales — release 1.0.0 |
| **APORTES release/2.0.0** | `docs/aportes/release-2.0.0.md` | Informe de aportes individuales del release de defensa — grupo unipersonal (n = 1), 95 tareas auditables, factor 1.00, generado por `PR-APORTES-001` |
| **Roadmap** | `docs/roadmap.md` | Hoja de ruta técnica y de negocio v0.3 — 4 horizontes (`release/1.0.1` → `release/1.1.0`/`release/3.0.0` → `release/1.2.0` → `release/2.0.0`), Gantt, 9 lecciones, métricas BRD/NFR, riesgos y compromisos; fuente canónica detallada (espejo historico en `docs/baseline/DTI.md` §19, espejo vivo hacia adelante en `docs/product/DTP.md` §B) |
| **Regla de seguridad** | `.cursor/rules/seguridad.mdc` | OWASP ASVS L2 — Java/Spring (secretos, PII en logs) |
| **Regla de baseline congelado** | `.cursor/rules/baseline-congelado.mdc` | Prohíbe a cualquier agente editar `docs/baseline/**`; espejo de la regla de este documento (§8.2) |
| **Hooks de Cursor** | `.cursor/hooks.json` + `.cursor/hooks/*.js` | Automatización, no solo convención: `protect-baseline.js` (`preToolUse`) bloquea con `permission: deny` cualquier `Write`/`StrReplace`/`EditNotebook`/`Delete` sobre `docs/baseline/**`; `warn-shell-baseline.js` (`beforeShellExecution`) pide confirmación ante comandos de shell que escriban/muevan/borren rutas de `docs/baseline/`; `dtp-sync-reminder.js` (`stop`) revisa `git diff`/`git ls-files --others` al final de cada turno y recuerda ejecutar `@dtp-sync` si hay cambios sin commitear en `backend/`, `frontend/`, `docs/design/` o `docs/prompts/impl/PR-IMPL-*.md` sin reflejo en `docs/product/DTP.md` |
| **Baseline congelado M4** | `docs/baseline/{BRD_EduSync_vFinal,MRD_EduSync_vFinal,PRD_EduSync_vFinal,FSD_EduSync_vFinal,DTI}.md` | Registro histórico **inmutable** evaluado en M4, tag `release/2.0.0`, `status: congelado`. Protegido por `CODEOWNERS` y `.cursor/rules/baseline-congelado.mdc`. El antiguo `docs/DTI.md` (v0.8, §0–§23) es hoy `docs/baseline/DTI.md` — su continuación viva es `docs/product/DTP.md` |
| **DTP (capa viva)** | `docs/product/DTP.md` | Documento Técnico del Producto v1.34 (21/08/2026) — continuación viva del DTI congelado; §A.3 marca `FSD-UC-011`, `FSD-UC-012`, `FSD-UC-013`, `FSD-UC-014`, `FSD-UC-017`, `FSD-UC-018`, `FSD-UC-019`, `FSD-UC-020` y `FSD-UC-021` como **completos** (backend + UI); `FSD-UC-015`/`016` **desbloqueados para diseño** (`ADR-0013`); `PR-IMPL-007`..`PR-IMPL-016` **ejecutados** (215/215 tests backend, `ng build` verde) |
| **PRD/FSD/BRD vivos** | `docs/product/{BRD,PRD,FSD}.md` | Copias editables de la capa viva (banner "COPIA VIVA"), abiertas para `release/3.0.0`; el FSD vivo opera en modo LFSD ⚡ (`docs/product/FSD.md` v2.12, BRD v3.2, PRD v2.3). Desde `ADR-0009`/`ADR-0013`: plataforma SaaS multi-tenant configurable; periodos/secciones/cálculo genérico resueltos (puntos 1–4 de `ADR-0009` §3). Desde `ADR-0010`: `BR-024`/`FSD-UC-021` multi-rol. |
| **Diagramas C4** | `docs/diagrams/c4_level1.mmd`, `c4_level2.mmd`, `c4_level3_api_gateway.mmd`, `c4_level3_domain_layer.mmd`, `c4_level3_sie_adapter.mmd`, `deployment_aws.mmd` (+ `.md` espejos para Level 3/Deployment) | C4 Level 1, 2, 3 y Deployment AWS; cumple la base para la rúbrica de diagramas versionados |
| **ADRs** | `docs/adr/0001..0006-*.md`, `0008-*.md`, `0009-*.md`, `0010-*.md`, `0011-*.md`, `0012-*.md`, `0013-*.md` | 12 ADRs aprobados: 0001..0006, 0008 stack vivo, 0009 generalización SaaS, 0010 multi-rol, 0011 Modulith, 0012 Lombok/OpenAPI/Bean Validation, **0013 modelo genérico de periodos/secciones/cálculo** (resuelve `ADR-0009` §3 puntos 1–4; ninguno supersede a los anteriores). `ADR-0007` (Strangler Fig) queda *gated*. |
| **Arq. hexagonal** | `docs/arquitectura_hexagonal_EduSync.md` | Arquitectura hexagonal v0.1 — 20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 Aggregate Roots (Perfil Bolivia SIE, paquete `bo.edusync`, baseline M4). **En actualización** (`ADR-0011`): paquete `com.edusync` + organización monolito modular *module-first* (Spring Modulith) para `release/3.0.0` |
| **Design Docs (capa viva)** | `docs/design/DD-UC-NNN.md` | Documentos de diseño por feature/*vertical slice*, trazados a `FSD-UC` + `ADR` + `PR-IMPL-NNN`; alimentan el DTP vía `@dtp-sync`. `DD-UC-001` (bootstrap); `DD-UC-002` (módulo `identidad`); `DD-UC-003` (módulo `plataforma`); `DD-UC-004` (frontend login + consola SysAdmin); `DD-UC-005` (CRUD backend de Usuarios y Roles); `DD-UC-006` (consola Angular de Usuarios y Roles); `DD-UC-007` (filtros y paginación reutilizables en Usuarios y Tenants — **ejecutado**, DoD 100%; mejora no funcional sobre `FSD-UC-011`/`FSD-UC-021`, ambos ya completos); `DD-UC-008` (módulo `academico`: `GestionEscolar` — alta, listado con filtros/paginación, ciclo de estado — **ejecutado**, DoD 100%; primer feature de negocio real de `academico`, `FSD-UC-012` completo en backend); `DD-UC-009` (consola Angular de Gestión Escolar — **ejecutado**, DoD 100%; primer *vertical slice* de UI de `academico`, `FSD-UC-012` completo backend+UI); `DD-UC-010` (módulo `academico`: `Curso`/`Paralelo` — alta y listado, sin ciclo de vida — **ejecutado**, DoD 100%; segundo feature de negocio real de `academico`, `FSD-UC-017` completo en backend); `DD-UC-011` (consola Angular de Cursos y Paralelos — **ejecutado**, DoD 100%; segundo *vertical slice* de UI de `academico`, después de `DD-UC-009`, `FSD-UC-017` completo backend+UI); `DD-UC-012` (módulo `academico`: `Materia` + asignaciones Curso/Profesor, backend + UI fullstack — **ejecutado**, DoD 100%; tercer feature de negocio real de `academico`, `FSD-UC-018` completo backend+UI); `DD-UC-013` (módulo `academico`: `Estudiante` + `Inscripcion`, backend + UI fullstack — **ejecutado**, DoD 100%; cuarto feature de negocio real de `academico`, `FSD-UC-020` completo backend+UI); `DD-UC-014` (módulo `academico`: Profesores, consulta inversa de asignaciones, backend + UI fullstack — **ejecutado**, DoD 100%; quinto feature de negocio real de `academico`, `FSD-UC-019` completo backend+UI); `DD-UC-015` (módulo `academico`: Periodos de Evaluación, backend + UI fullstack — **ejecutado**, DoD 100%; sexto feature de negocio real de `academico`, `FSD-UC-013` completo backend+UI); `DD-UC-016` (módulo `academico`: Secciones de Evaluación, backend + UI fullstack — **ejecutado**, DoD 100%; séptimo feature de negocio real de `academico`, `FSD-UC-014` completo backend+UI) |
| **DTOs por capa** | `docs/dtos_EduSync.md` | DTOs hexagonales v0.1 — 4 Request, 4 Commands, 3 Response, 5 Domain Events, 5 enums |
| **Skills de Cursor** | `.cursor/skills/<slug>/SKILL.md` | **14 skills** en paridad con Claude: 11 EduSync (`feature-design-doc`, `dtp-sync`, `adr-edusync`, `c4-edusync`, `dti-edusync`, `update-prompt-mapping`, `edusync-skill-creator`, `sync-doc-chain`, `poc-runner-edusync`, `materialize-prompt-files`, `ollama-edusync`) + 3 de arquitectura (`async-architecture-reviewer`, `distributed-architecture-reviewer-edusync`, `monolith-decomposition-architect`). Los ~16 canónicos restantes de `plantillas/plantillas2/` siguen como plantillas fuente, no materializados como skills activos |
| **Skills de Claude Code** | `.claude/skills/<slug>/SKILL.md` | **14 skills** — paridad completa con `.cursor/skills/` (mismos slugs y contenido). Entrada Claude: `CLAUDE.md` (importa `AGENTS.md`) + `.claude/rules/` + `.claude/agents/` |
| **Contratos materializados** | `prompts/PR-<AREA>-NNN.md` | Archivos individuales por prompt-contrato — generados por skill `materialize-prompt-files`. **Excepción**: los prompts del área de implementación (`PR-IMPL-NNN.md`) NO viven aquí — viven en `docs/prompts/impl/PR-IMPL-NNN.md`, siguiendo `FEATURE_DESIGN_DOC_TEMPLATE.md`/`MODELO_DOCUMENTAL_IMPLEMENTACION.md`; es la única área que se desvía de la convención plana de M4 |

---

## 2. Contexto que el agente MUST leer antes de actuar

Al comenzar cualquier tarea, el agente **MUST** leer en orden:

1. `docs/arquitectura_funcional_EduSync.md` — los 10 casos de uso críticos, sus invariantes y las 5 decisiones arquitectónicas (DA-01..DA-05).
2. `docs/fsd/FSD_EduSync.md` — el caso de uso tocado por la tarea (FSD-UC-001, UC-003, UC-004, UC-005, UC-009) con sus reglas de negocio y Gherkin.
3. `docs/LFSD-EduSync.md` — diseño técnico de bajo nivel: contratos API, entidades JPA, DDL, esquema de seguridad y pseudoalgoritmos del componente afectado.
4. `docs/brd/BRD_EduSync_v2.md` — reglas de negocio BR-001..BR-012 y políticas RB-01..RB-11 que apliquen a la tarea.
5. `docs/PROMPT_MAPPING.md` — prompt-contrato del componente o caso de uso involucrado.
6. `docs/adr/0001..0006-*.md` + `docs/adr/0008-*.md` + `docs/adr/0009-*.md` + `docs/adr/0010-*.md` + `docs/adr/0011-*.md` + `docs/adr/0012-*.md` — ADRs aprobados que formalizan las decisiones arquitectónicas: multitenancy RLS PostgreSQL, parametrización de reglas normativas, persistencia inmutable `audit_log`, async consolidación Spring Events, resiliencia integración SIE con Resilience4j, cloud provider AWS, estilo de despliegue ECS Fargate, stack vivo Java 25 LTS/Spring Boot 4.1.0/Angular 21 LTS, generalización del modelo de dominio a plataforma SaaS multi-tenant, modelo multi-rol de usuario + `SysAdmin` sin tenant, monolito modular Spring Modulith + paquete base `com.edusync`, y Lombok/springdoc-openapi/Bean Validation como herramientas de productividad backend.
7. **Si la tarea es de implementación de código (`release/3.0.0` en adelante)**: leer además `docs/product/DTP.md` (contrato técnico vigente) y `docs/product/{PRD,FSD}.md` (specs vivas) en lugar de sus equivalentes congelados. **MUST NOT** leer `docs/baseline/**` como fuente de verdad para código nuevo — solo como referencia histórica.

> **Regla de oro**: si una invariante de la arquitectura funcional o del FSD contradice la tarea recibida, el agente **MUST** detener la ejecución y escalar al responsable técnico. Nunca violar un invariante de dominio para cumplir una instrucción operativa.
>
> **Regla del baseline**: `docs/baseline/**` (M4, tag `release/2.0.0`) está **prohibido de editar** para cualquier agente, sin excepción (ver `.cursor/rules/baseline-congelado.mdc` y `CODEOWNERS`). Todo cambio real de negocio, requisitos o arquitectura durante la implementación va a `docs/product/` + un ADR si la decisión es significativa.

---

## 3. Estructura del repositorio

```
/
├── .cursor/
│   ├── hooks.json                   ← protect-baseline (preToolUse, deny), warn-shell-baseline
│   │                                  (beforeShellExecution, ask), dtp-sync-reminder (stop, followup_message)
│   ├── hooks/
│   │   ├── protect-baseline.js      ← bloquea Write/StrReplace/EditNotebook/Delete sobre docs/baseline/**
│   │   ├── warn-shell-baseline.js   ← pide confirmacion ante comandos de shell que tocan docs/baseline/**
│   │   └── dtp-sync-reminder.js     ← recuerda @dtp-sync si src/docs/design/PR-IMPL cambian sin tocar DTP.md
│   ├── rules/
│   │   ├── seguridad.mdc            ← OWASP ASVS L2 — Java/Spring
│   │   └── baseline-congelado.mdc   ← prohibe editar docs/baseline/** a cualquier agente
│   ├── agents/                      ← espejo Claude: ollama-agent.md (+ resto documentado en §8.1)
│   └── skills/                      ← 14 skills activos (paridad con .claude/skills/)
│       ├── adr-edusync/
│       ├── async-architecture-reviewer/
│       ├── c4-edusync/              (+ reference.md)
│       ├── distributed-architecture-reviewer-edusync/
│       ├── dti-edusync/
│       ├── dtp-sync/
│       ├── edusync-skill-creator/
│       ├── feature-design-doc/
│       ├── materialize-prompt-files/
│       ├── monolith-decomposition-architect/
│       ├── ollama-edusync/
│       ├── poc-runner-edusync/
│       ├── sync-doc-chain/
│       └── update-prompt-mapping/   (+ reference.md)
│       # Canónicos restantes de plantillas/plantillas2/ NO materializados como skills activos
├── .claude/
│   ├── skills/                      ← 14 skills (paridad completa con .cursor/skills/, mismos slugs)
│   ├── agents/                      ← 7 subagentes: dev/docs/arch/qa/process/compliance/ollama-agent.md
│   └── rules/                       ← espejo de .cursor/rules: baseline-congelado.md, seguridad.md
├── README.md
├── AGENTS.md                        ← este archivo (v0.45) — convención multi-agente (Cursor + Claude)
├── CLAUDE.md                        ← entrada Claude Code (importa AGENTS.md; no duplica reglas)
├── CODEOWNERS                       ← revisión humana obligatoria sobre docs/baseline/**
├── 01_vision_negocio.md             ← visión y contexto del producto
├── 02_parte_dificil.md              ← análisis de riesgos técnicos
├── S01_03_Prompt.md                 ← prompt de sistema mejorado
├── docs/
│   ├── APORTES_EduSync.md           ← informe de aportes individuales
│   ├── roadmap.md                   ← Hoja de ruta canónica v0.3 (4 horizontes, Gantt, lecciones, compromisos, apertura de capa viva)
│   ├── arquitectura_funcional_EduSync.md  ← 10 UCs + 5 DAs (fuente de verdad)
│   ├── arquitectura_hexagonal_EduSync.md  ← puertos, adaptadores, Aggregate Roots (v0.1)
│   ├── dtos_EduSync.md              ← DTOs por capa hexagonal (v0.1)
│   ├── LFSD-EduSync.md              ← Low-Level Functional Spec v1.0.1 (hex. arch, DDL, APIs)
│   ├── PROMPT_MAPPING.md            ← catálogo de 41 prompt-contratos v2.7 (área IMPL: 4 filas, PR-IMPL-001..004)
│   ├── baseline/                    ← ⚠ CONGELADO (M4, tag release/2.0.0). Prohibido editar (CODEOWNERS + baseline-congelado.mdc)
│   │   ├── BRD_EduSync_vFinal.md
│   │   ├── MRD_EduSync_vFinal.md
│   │   ├── PRD_EduSync_vFinal.md
│   │   ├── FSD_EduSync_vFinal.md
│   │   └── DTI.md                   ← Documento Técnico Inicial v0.8 congelado (antes docs/DTI.md); continuación viva: docs/product/DTP.md
│   ├── product/                     ← VIVO desde release/3.0.0 (editable)
│   │   ├── BRD.md, PRD.md, FSD.md   ← copias vivas (banner "COPIA VIVA"); FSD en modo LFSD ⚡
│   │   └── DTP.md                   ← Documento Técnico del Producto v1.14 (ADR-0008/0009/0010/0011/0012 registrados en §A.2)
│   ├── design/                      ← DD-UC-NNN (design docs por feature, skill feature-design-doc)
│   │   ├── DD-UC-001.md             ← Bootstrap del proyecto (monolito modular Spring Modulith, paquete com.edusync; crea ADR-0011)
│   │   ├── DD-UC-002.md             ← Módulo identidad: Usuario/UsuarioRol, login JWT, seed SYSADMIN (FSD-UC-021 parcial)
│   │   ├── DD-UC-003.md             ← Módulo plataforma: alta y gestión de Tenants, scheduler vencimiento (FSD-UC-011 completo)
│   │   ├── DD-UC-004.md             ← Frontend: login + consola SysAdmin (FSD-UC-021/011 UI; ejecutado, 19/07/2026)
│   │   ├── DD-UC-005.md             ← CRUD backend de Usuarios y Roles (FSD-UC-021 backend completo; ejecutado, 04/08/2026)
│   │   ├── DD-UC-006.md             ← Consola Angular de Usuarios y Roles (FSD-UC-021 completo backend+UI; ejecutado, 04/08/2026)
│   │   ├── DD-UC-007.md             ← Filtros y paginación reutilizables en Usuarios y Tenants (ejecutado, 20/08/2026)
│   │   ├── DD-UC-008.md             ← Módulo academico: GestionEscolar, alta/listado/ciclo de estado (ejecutado, 20/08/2026; FSD-UC-012 completo backend)
│   │   ├── DD-UC-009.md             ← Consola Angular de Gestión Escolar (ejecutado, 20/08/2026; FSD-UC-012 completo backend+UI)
│   │   ├── DD-UC-010.md             ← Módulo academico: Curso y Paralelo, alta y listado (ejecutado, 20/08/2026; FSD-UC-017 completo backend)
│   │   ├── DD-UC-011.md             ← Consola Angular de Cursos y Paralelos (ejecutado, 21/08/2026; FSD-UC-017 completo backend+UI)
│   │   ├── DD-UC-012.md             ← Académico: Materias backend+UI fullstack (ejecutado, 21/08/2026; FSD-UC-018 completo backend+UI)
│   │   ├── DD-UC-013.md             ← Académico: Estudiantes e Inscripciones backend+UI fullstack (ejecutado, 21/08/2026; FSD-UC-020 completo backend+UI)
│   │   ├── DD-UC-014.md             ← Académico: Profesores backend+UI fullstack (ejecutado, 21/08/2026; FSD-UC-019 completo backend+UI)
│   │   ├── DD-UC-015.md             ← Académico: Periodos de Evaluación backend+UI fullstack (ejecutado, 21/08/2026; FSD-UC-013 completo)
│   │   └── DD-UC-016.md             ← Académico: Secciones de Evaluación backend+UI fullstack (ejecutado, 21/08/2026; FSD-UC-014 completo)
│   ├── prompts/impl/                ← ÚNICA excepción a la convención plana de prompts/ (FEATURE_DESIGN_DOC_TEMPLATE.md §5)
│   │   ├── PR-IMPL-001.md           ← bootstrap del esqueleto backend/frontend/infra (ejecutado, 18/07/2026)
│   │   ├── PR-IMPL-002.md           ← módulo identidad: login/JWT + seed SysAdmin (ejecutado, 18-19/07/2026; ADR-0012 aplicado)
│   │   ├── PR-IMPL-003.md           ← módulo plataforma: alta de Tenants + TenantConsultaPort (ejecutado, 19/07/2026)
│   │   ├── PR-IMPL-004.md           ← frontend login + consola SysAdmin + GET /tenants (ejecutado, 19/07/2026)
│   │   ├── PR-IMPL-005.md           ← CRUD backend de Usuarios y Roles (roles/estado/reset password) (ejecutado, 04/08/2026)
│   │   ├── PR-IMPL-006.md           ← consola Angular de Usuarios y Roles + confirmación pública (ejecutado, 04/08/2026)
│   │   ├── PR-IMPL-007.md           ← filtros y paginación reutilizables en GET /usuarios y GET /tenants (ejecutado, 20/08/2026)
│   │   ├── PR-IMPL-008.md           ← módulo academico: GestionEscolar (alta, listado, ciclo de estado) (ejecutado, 20/08/2026)
│   │   ├── PR-IMPL-009.md           ← consola Angular de Gestión Escolar (ejecutado, 20/08/2026)
│   │   ├── PR-IMPL-010.md           ← módulo academico: Curso y Paralelo (alta, listado) (ejecutado, 20/08/2026)
│   │   ├── PR-IMPL-011.md           ← consola Angular de Cursos y Paralelos (ejecutado, 21/08/2026)
│   │   ├── PR-IMPL-012.md           ← academico: Materia + asignaciones (backend+UI fullstack, ejecutado 21/08/2026)
│   │   ├── PR-IMPL-013.md           ← academico: Estudiante + Inscripcion (backend+UI fullstack, ejecutado 21/08/2026)
│   │   ├── PR-IMPL-014.md           ← academico: Profesores (consulta inversa, backend+UI fullstack, ejecutado 21/08/2026)
│   │   ├── PR-IMPL-015.md           ← academico: Periodos de Evaluación (backend+UI fullstack, ejecutado 21/08/2026)
│   │   └── PR-IMPL-016.md           ← academico: Secciones de Evaluación (backend+UI fullstack, ejecutado 21/08/2026)
│   ├── brd/
│   │   ├── BRD_EduSync_v1.md        ← BRD inicial
│   │   └── BRD_EduSync_v2.md        ← BRD consolidado (BR-001..BR-012)
│   ├── mrd/
│   │   └── MRD_EduSync.md           ← Market Requirements v1.0
│   ├── prd/
│   │   └── PRD_EduSync.md           ← Product Requirements v1.0 (17 US, 6 épicas)
│   ├── fsd/
│   │   └── FSD_EduSync.md           ← FSD Clásico v1.0 (5 FSD-UC, ER 16 entidades)
│   ├── adr/                         ← 12 ADRs aprobados (0007 Strangler Fig queda gated, sin crear)
│   │   ├── 0001-multitenancy-rls-postgresql.md
│   │   ├── 0002-parametrizacion-reglas-normativas.md
│   │   ├── 0003-persistencia-inmutable-audit-log.md
│   │   ├── 0004-async-consolidacion-spring-events.md
│   │   ├── 0005-resiliencia-integracion-sie-resilience4j.md
│   │   ├── 0006-cloud-provider-y-estilo-de-despliegue.md
│   │   ├── 0008-actualizacion-stack-java25-springboot4-angular21.md
│   │   ├── 0009-generalizacion-modelo-dominio-multitenant-configurable.md
│   │   ├── 0010-modelo-multirol-usuario-y-sysadmin-sin-tenant.md
│   │   ├── 0011-monolito-modular-spring-modulith-package-base.md
│   │   ├── 0012-lombok-openapi-validation-productividad-backend.md
│   │   └── 0013-modelo-generico-periodos-secciones-calculo.md
│   └── diagrams/                    ← diagramas Mermaid (fuente de verdad visual)
│       ├── ai-sdlc.mmd              ← comparativa AI-SDLC vs. SDLC tradicional
│       ├── c4_level1.mmd            ← C4 Level 1 — Contexto del sistema
│       ├── c4_level2.mmd            ← C4 Level 2 — Contenedores
│       ├── estados.cargarnotas.mmd  ← 18 estados del Docente (.mmd canónico)
│       ├── estados_cargar_notas.mmd ← duplicado normalizado (mismo origen)
│       ├── estados_cargar_notas.md  ← spec formal estados Docente
│       ├── estados_administracion.mmd ← 23 estados del Director
│       └── estados_administracion.md  ← spec formal estados Director
├── prompts/                         ← archivos individuales por prompt-contrato, convención plana — todas las áreas
│                                      EXCEPTO `IMPL` (ver `docs/prompts/impl/` arriba)
│   └── PR-<AREA>-NNN.md             ← 37 contratos materializados (PR-ADR-001..005, PR-ARCH-001/002,
│                                      PR-APORTES-001, PR-AUD-001, PR-BRD-001/002, PR-C4-001..006, PR-DIAG-001/002,
│                                      PR-DTI-001, PR-DTI-SEAMS-001, PR-DTO-001, PR-FSD-001,
│                                      PR-HEX-001, PR-INF-001, PR-LFSD-001, PR-MRD-001, PR-POC-001/002, PR-PRD-001,
│                                      PR-ROADMAP-001, PR-SKILL-001/002/003, PR-UC-001..005, PR-UC-009, PR-VFINAL-001)
│                                      — 53 contratos en total contando PR-IMPL-001..015 en docs/prompts/impl/
├── backend/                         ← ✅ esqueleto (PR-IMPL-001) + identidad (PR-IMPL-002/005 + ADR-0012) + plataforma
│                                      (PR-IMPL-003) + delta GET /tenants (PR-IMPL-004) + filtros/paginación
│                                      reutilizables (PR-IMPL-007) + academico/GestionEscolar (PR-IMPL-008) +
│                                      academico/Curso-Paralelo (PR-IMPL-010) + academico/Materia (PR-IMPL-012) +
│                                      academico/Estudiante-Inscripcion (PR-IMPL-013) +
│                                      academico/Profesores (PR-IMPL-014) +
│                                      academico/PeriodoEvaluacion (PR-IMPL-015) +
│                                      academico/SeccionEvaluacion (PR-IMPL-016) —
│                                      monolito modular Spring Boot 4.1.0 (Java 25 LTS) spring-modulith-bom 2.1.0,
│                                      com.edusync; 215/215 tests verde (incluye ModularityTests 7/7). PR-IMPL-001..016
│                                      ejecutados
│                                      (reemplaza al antiguo
│                                      src/domain|application|infrastructure)
│   ├── pom.xml
│   └── src/main/java/com/edusync/
│       ├── EduSyncApplication.java
│       ├── plataforma/              ← ✅ real (PR-IMPL-003 + GET lista PR-IMPL-004 + filtros/paginación PR-IMPL-007) —
│       │                              Tenant/EstadoTenant, ciclo de suscripcion, scheduler, TenantConsultaPort,
│       │                              ListarTenantsUseCase, TenantFiltro/TenantSpecifications
│       │                              (FSD-UC-011 completo API+UI, DD-UC-003/004/007)
│       ├── identidad/               ← ✅ real (PR-IMPL-002 + PR-IMPL-005 + filtros/paginación PR-IMPL-007) —
│       │                              Usuario/UsuarioRol, login JWT, TenantContextProvider, seed SYSADMIN,
│       │                              UsuarioCreacionPort, CRUD de Usuarios/Roles (UsuarioController,
│       │                              PasswordResetToken/Controller), UsuarioFiltro/UsuarioSpecifications
│       │                              (FSD-UC-021 completo backend, DD-UC-002/005/007);
│       │                              ProfesorConsultaPortImpl (Open Host Service de academico, PR-IMPL-012)
│       ├── academico/               ← ✅ real (PR-IMPL-008 + PR-IMPL-010 + PR-IMPL-012 + PR-IMPL-013 + PR-IMPL-014 + PR-IMPL-015 + PR-IMPL-016) — GestionEscolar (Aggregate Root, alta/listado
│       │                              con filtros-paginación/ciclo de estado PLANIFICACION-ACTIVA-CERRADA),
│       │                              GestionEscolarSpecifications (FSD-UC-012 completo backend+UI, DD-UC-008/009);
│       │                              Curso/Paralelo (Aggregates independientes, sin estado, alta/listado),
│       │                              CursoSpecifications (FSD-UC-017 completo backend+UI, DD-UC-010/011);
│       │                              Materia + AsignacionMateriaCurso/Profesor (FSD-UC-018 completo backend+UI,
│       │                              DD-UC-012); ProfesorConsultaPort en raíz (Open Host Service, impl en identidad);
│       │                              Estudiante + Inscripcion (FSD-UC-020 completo backend+UI, DD-UC-013);
│       │                              Profesores (FSD-UC-019 completo backend+UI, DD-UC-014);
│       │                              PeriodoEvaluacion (FSD-UC-013 completo backend+UI, DD-UC-015); SeccionEvaluacion (FSD-UC-014 completo backend+UI, DD-UC-016); Evaluacion (FSD-UC-015..016 **diseño desbloqueado** `ADR-0013`, código pendiente)
│       ├── notassie/                ← vacio (package-info.java) — Perfil Bolivia SIE: Calificacion/Consolidacion/ExportacionSIE (FSD-UC-001/003/004/005/009)
│       └── shared/                  ← tenant/ real (TenantContext/TenantContextProvider/TenantAwareDataSource),
│                                      web/ (GlobalExceptionHandler, OpenApiConfig, ErrorResponse — ADR-0012,
│                                      PageResponse — PR-IMPL-007), PageQuery/PageResult (PR-IMPL-007),
│                                      exception/DomainException; modulo OPEN de Spring Modulith
├── frontend/                        ← ✅ SPA Angular 21.2.19 LTS (PR-IMPL-001 esqueleto + PR-IMPL-004/006/009/011/012/013/014/015/016 UI +
│                                      PR-IMPL-007 filtros/paginación, 21/08/2026) (standalone, sin Nx); ng build sin errores
│   └── src/app/
│       ├── core/auth/               ← ✅ AuthService (JWT sessionStorage), interceptor, guards (DD-UC-004)
│       ├── core/api/                ← ✅ page-response.model.ts (PageResponse<T> genérico, PR-IMPL-007)
│       ├── features/auth/           ← ✅ login page + reset-password-confirm (pública, DD-UC-006)
│       ├── features/plataforma/     ← ✅ consola SysAdmin tenants (lista/alta/admin/estado, filtros/paginación DD-UC-007)
│       ├── features/usuarios/       ← ✅ consola Admin: lista/alta/roles/estado/reset (DD-UC-006, filtros/paginación DD-UC-007)
│       ├── features/academico/      ← ✅ consola Admin: Gestión Escolar (lista con filtros/paginación, alta,
│       │                              cambio de estado con transiciones válidas, DD-UC-009); Cursos y Paralelos
│       │                              (lista con filtro/paginación, alta, detalle con Paralelos y alta inline, DD-UC-011);
│       │                              Materias (lista/alta/detalle con asignaciones inline, DD-UC-012; ADMIN+SECRETARIA);
│       │                              Estudiantes (lista/alta/detalle con inscripciones inline, DD-UC-013; ADMIN+SECRETARIA);
│       │                              Profesores (lista/detalle de asignaciones de solo lectura, DD-UC-014; ADMIN+SECRETARIA);
│       │                              Periodos de Evaluación (detalle anidado en Gestión Escolar, DD-UC-015; ADMIN);
│       │                              Secciones de Evaluación (detalle anidado en Gestión Escolar, DD-UC-016; ADMIN)
│       ├── features/home/           ← placeholder post-login (roles sin consola propia)
│       └── shared/layout/           ← shell mínimo (nav condicional por rol, DD-UC-006/009/011/012/013/014)
├── infra/                           ← ✅ docker-compose.yml generado (PR-IMPL-001, PostgreSQL 15 local, DD-UC-001);
│                                      IaC Terraform/AWS más adelante
│   ├── docker-compose.yml
│   ├── rds/
│   ├── sqs/
│   └── ecs/
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
├── plantillas/                      ← templates de documentación del módulo
│   ├── ADR_TEMPLATE.md
│   ├── AGENTS_TEMPLATE.md
│   ├── APORTES_TEMPLATE.md
│   ├── BRD_TEMPLATE.md
│   ├── DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md
│   ├── FSD_TEMPLATE.md
│   ├── MRD_TEMPLATE.md
│   ├── POC_TEMPLATE.md
│   ├── PRD_TEMPLATE.md
│   ├── PROMPT_TEMPLATE.md
│   ├── SKILL_TEMPLATE.md
│   ├── c4.md
│   ├── dti-author.md
│   └── poc-runner.md
└── plantillas2/                     ← material canónico Módulo 4 — UMSS
    ├── SKILL_TEMPLATE (2).md        ← template referencia 10 secciones
    ├── <19 SKILL.md genéricos>      ← importados a .cursor/skills/ (28/05/2026)
    ├── cursor_prompt_FSD.md
    ├── cursor_prompt_skill_prompts_mejorados.md
    ├── PRD.md, README.md
    └── <rubrica del modulo>.pdf     ← rubrica de evaluación final del módulo
```

---

## 4. Stack tecnológico autoritativo

> ⚠️ **Dualidad de stack**: esta tabla describe el stack **vivo**, vigente desde `release/3.0.0` (`ADR-0008`). El **baseline congelado de M4** (`docs/baseline/DTI.md`, tag `release/2.0.0`) documenta Java 21 (LTS) / Spring Boot 3.3 / Angular 17 como hecho histórico y **no se actualiza**. Cualquier dependencia de terceros nueva debe verificarse contra Jakarta EE 11 / Spring Framework 7.0.8 antes de fijarse en `pom.xml`.

| Capa | Tecnología | Versión | Justificación |
|------|------------|---------|---------------|
| Lenguaje principal | Java | **25 (LTS)** — baseline M4: 21 (LTS) | Adopción del LTS más reciente al abrir `release/3.0.0` sobre `src/` vacío (greenfield, sin costo de migración); records y virtual threads para alto throughput en exportación masiva SIE (`ADR-0008`) |
| Framework backend | Spring Boot | **4.1.0 (Spring Framework 7.0.8)** — baseline M4: 3.3 | Jakarta EE 11; AOT Cache sobre Java 25; Spring Security 7 para RBAC + JWT; Spring Data JPA; Spring Events para consolidación asíncrona (DA-04) (`ADR-0008`) |
| Persistencia | PostgreSQL | 15 (RDS) — sin cambio | ACID estricto; Row-Level Security (RLS) nativo para aislamiento multitenant (DA-01); append-only para modificaciones retroactivas |
| Migraciones DB | Flyway | 10.x | Versionado de esquema reproducible; **MUST NOT** modificar migraciones ya aplicadas en `main` |
| Mensajería | Spring Events → AWS SQS | Managed (v1.1+) | Consolidación asíncrona post-cierre (DA-04); reintentos idempotentes en exportación SIE (DA-05) |
| Frontend | Angular | **21 (LTS)** — baseline M4: 17+ | Ventana LTS larga (hasta ~mayo 2027) priorizada sobre la última minor (Angular 22) para un equipo de uno; reactive forms/Signal Forms para validación antierrores en tiempo real (`ADR-0008`) |
| IaC | Terraform | 1.8 | Infraestructura reproducible sobre AWS (región `us-east-1` por defecto) |
| Contenedores | AWS ECS Fargate | Managed | Despliegue sin gestión de servidores; escalado automático en picos de cierre trimestral; imagen base a actualizar a OpenJDK 25 al crear el primer `Dockerfile` |
| Testing | JUnit 5 + Testcontainers | Latest stable | Pruebas de integración con PostgreSQL 15 real; sin mocks de BD en tests de dominio |
| Auditoría | `audit_log` append-only + Hibernate Envers | — | `AuditLogAspect` (AOP) en la misma TX que la escritura; `@Immutable` en entidad JPA; sin UPDATE/DELETE (DA-03) |
| Generación PDF | Apache PDFBox | Latest stable (verificar compat. Jakarta EE 11) | Boletines académicos con plantilla ministerial parametrizable |
| Seguridad | OWASP ASVS L2 | — | `.cursor/rules/seguridad.mdc`; sin PII en logs, sin secretos en código |
| Reducción de boilerplate | Lombok | 1.18.46 (`scope=provided`) | Sin restricción en `infrastructure`/`application`; en `domain/` restringido a un *allowlist* (`@Getter`/`@EqualsAndHashCode`/`@ToString`, nomenclatura JavaBean estándar; nunca `@Data`/`@Setter`/`@Builder` público, que evadirían la validación de invariantes de los Aggregate Roots) (`ADR-0012`) |
| Documentación de API | springdoc-openapi | `springdoc-openapi-starter-webmvc-ui` 3.0.3 | Rama `3.x.x` compatible con Spring Boot 4.x; genera `/v3/api-docs` + `/swagger-ui.html` desde las anotaciones de los controladores/DTOs existentes (`ADR-0012`) |
| Validación de entrada | Bean Validation (Jakarta) | `spring-boot-starter-validation` (BOM `spring-boot-starter-parent`) | `@NotBlank`/`@Email`/`@Size` declarativos en DTOs de `infrastructure/adapter/in/rest/`, con `@ExceptionHandler(MethodArgumentNotValidException.class)` común normalizando al formato `ErrorResponse` (`ADR-0012`) |

> El agente **MUST NOT** introducir dependencias fuera de esta tabla sin crear un ADR en `docs/adr/` y obtener aprobación humana explícita en el PR.

---

## 5. Convenciones de código

- **Idioma del código**: inglés (clases, métodos, variables, comentarios inline).
- **Idioma de la documentación**: español (docs, ADR, comentarios Javadoc de dominio).
- **Estilo**: Google Java Style Guide.
- **Naming**: clases `PascalCase`, métodos `camelCase`, constantes `UPPER_SNAKE_CASE`, paquetes `lower.case`.
- **Arquitectura**: hexagonal estricta (Ports & Adapters). El paquete `domain/` **MUST NOT** importar de `infrastructure/` ni depender de frameworks que impongan comportamiento o ciclo de vida en runtime (Spring, JPA, AWS). Solo interfaces puras. Procesadores de anotaciones sin huella en runtime están permitidos bajo el *allowlist* de `ADR-0012`: Lombok `@Getter`/`@EqualsAndHashCode`/`@ToString`, siempre con nomenclatura JavaBean estándar (`getId()`, `isActivo()` — nunca el estilo fluido sin prefijo); **MUST NOT** usarse `@Data`/`@Setter`/`@Builder` con acceso público en `domain/`, porque evadirían la validación de invariantes de los Aggregate Roots (constructor privado + factory method, ej. `Usuario.crear()`/`ADR-0010`). En `infrastructure/`/`application/` Lombok se permite sin esta restricción.
- **Commits**: Conventional Commits — `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
- **Tamaño máximo de PR**: 400 líneas netas. PRs más grandes deben dividirse por caso de uso.
- **DTOs**: toda respuesta de API **MUST** usar clases en `infrastructure/web/dto/`. **MUST NOT** exponer entidades JPA ni clases de dominio directamente.
- **Estados de periodo**: usar el enum `EstadoPeriodo { PENDIENTE, CONFIGURADO, ABIERTO, CERRADO }` definido en dominio. **MUST NOT** usar strings literales para representar estados.
- **Estados de centralizador**: usar el enum `EstadoCentralizador { PROVISIONAL, OFICIAL }`. El estado `PROVISIONAL` puede sobreescribirse; el `OFICIAL` es inmutable.

---

## 6. Reglas de dominio invariantes

> Ningún cambio puede violar estas reglas sin revisión humana explícita y creación de un ADR en `docs/adr/`.

### Reglas generales

- **MUST**: persistir toda escritura dentro de una transacción `@Transactional`. Sin escrituras fuera de transacción.
- **MUST NOT**: exponer entidades JPA directamente por API. Usar DTOs en `infrastructure/web/dto/`.
- **MUST NOT**: acoplar adaptadores entre sí. La comunicación entre `infrastructure/persistence/` y `infrastructure/integration/sie/` pasa exclusivamente por el dominio o la capa de aplicación.
- **MUST**: toda llamada al sistema externo SIE tiene `circuit breaker` (Resilience4j), `timeout` configurable (30 s) y política de reintentos con backoff; scheduler de reintentos cada 5 min (ver `SIERetryScheduler`).
- **MUST**: todo endpoint público tiene autenticación JWT y control RBAC por rol (`DIRECTOR` / `SECRETARIA` / `DOCENTE`).

### Reglas específicas del dominio EduSync

- **MUST**: vincular toda calificación al estudiante exclusivamente por su código `RUDE`. **MUST NOT** usar nombre, apellido, número de lista ni posición visual como clave de asociación en ninguna operación de escritura o exportación (BR-004 / Constitución §0.2).
- **MUST**: validar que el valor de cada dimensión (Ser, Saber, Hacer, Decidir, Autoevaluación) esté dentro del rango paramétrico vigente **antes de persistir**. Si está fuera de rango → error de negocio `E_RANGO_INVALIDO`. La validación ocurre en `CalificacionDomainService`, no en el controlador (BR-002).
- **MUST**: el cálculo de promedios ocurre **exclusivamente** en el motor de dominio (nunca en adaptadores, SQL ad-hoc ni frontend). **Perfil Bolivia SIE / `FSD-UC-003`:** `floor()` solo en `ConsolidacionDomainService` (`BR-008`, golden `FloorTest`). **Modelo genérico / `FSD-UC-016` (`ADR-0013`):** promedio simple + `round` HALF_UP (2 decimales en sección, entero en periodo y gestión); **MUST NOT** usar `floor()` en ese motor.
- **MUST**: verificar el estado del periodo académico antes de cualquier escritura. Si el periodo está en `CERRADO`, la operación **MUST** ser rechazada con `E_PERIODO_NO_MODIFICABLE`, salvo que exista una `AutorizacionCorreccion` activa y vigente autorizada por el Director (BR-005 / FSD-UC-005).
- **MUST**: registrar una entrada en `audit_log` en la **misma transacción** que cada escritura exitosa. La entrada **MUST** incluir: `tenant_id`, `actor_id`, `accion`, `entidad_afectada`, `entidad_id`, `valor_anterior`, `valor_nuevo`, `timestamp_utc` (BR-010 / DA-03).
- **MUST**: el `audit_log` es inalterable. **MUST NOT** ejecutar `UPDATE` ni `DELETE` sobre ninguna fila de esta tabla. Protegido por `RULE` de PostgreSQL + `@Immutable` en Hibernate.
- **MUST NOT**: permitir que un docente altere, agregue o elimine registros de la nómina de estudiantes. La nómina es de solo lectura para el rol `DOCENTE` (BR-001).
- **MUST**: aplicar multitenancy en cada consulta y escritura mediante `SET LOCAL app.tenant_id` antes de cada transacción JPA. Ninguna query **MUST NOT** acceder a datos de un tenant distinto al autenticado en el contexto de seguridad actual (DA-01 / Constitución §0.5).
- **MUST NOT**: modificar un registro de calificación ya persistido. Toda corrección retroactiva genera un nuevo registro con `registro_padre_id` referenciando el original (append-only). El registro original es inmutable (BR-005 / RB-10).
- **MUST**: toda `AutorizacionCorreccion` tiene `ventana_fin` definido (1–72 h). No existe autorización indefinida. La revocación es automática vía `VentanaExpiracionScheduler` (BR-009).

---

## 7. Seguridad y privacidad

- **PII en el sistema**: `rude` (código único del estudiante), `nombre_completo`, `fecha_nacimiento`. Cifrado en reposo mediante AWS KMS (`alias/edusync-pii-key`).
- **Datos sensibles institucionales**: calificaciones, promedios, centralizadores. Acceso restringido por RBAC + RLS de PostgreSQL.
- **Secretos**: provienen exclusivamente de AWS Secrets Manager o variables de entorno inyectadas por ECS Task Definition. **MUST NOT** aparecer en código fuente, logs, prompts de agentes ni en archivos de configuración commiteados.
- **Logs**: **MUST NOT** registrar `rude`, `password`, `token`, `jwt`, ni ningún campo de calificación individual en logs nivel INFO o superior. Solo referencias por `id` interno (Constitución §0.4 / NFR-007 del FSD).
- **Regla de seguridad activa**: `.cursor/rules/seguridad.mdc` y espejo `.claude/rules/seguridad.md` — OWASP ASVS L2 aplicado en Java/Spring.
- **Cumplimiento aplicable**:
  - **Ley 164 (Bolivia)**: protección de datos personales aplicable a datos de menores de edad (estudiantes).
  - **Regulación ministerial SIE**: formato y protocolo de exportación son obligatorios e inquebrantables; toda desviación constituye incumplimiento sancionable.
- **Autenticación**: JWT con expiración máxima de 8 horas (NFR-008 del FSD). **MUST NOT** aceptar tokens sin firma válida o expirados.
- **TLS**: HTTPS/TLS 1.3 en tránsito (NFR-009 del FSD). Configurado a nivel de Load Balancer AWS.

---

## 8. Capacidades y guardrails de agentes

### 8.1 Agentes activos en este repositorio

> En Claude Code los mismos roles viven como subagentes en `.claude/agents/<nombre>.md`. En Cursor se invocan por instrucción / skill según la tarea.

| Agente | Propósito | Modelo | Herramientas | Límites estrictos |
|--------|-----------|--------|--------------|-------------------|
| `dev-agent` | Implementar casos de uso backend (FSD-UC-001..009) | Sonnet | `read`, `edit`, `run-tests`, `run-linter` | **MUST NOT** tocar `infra/`; **MUST NOT** modificar migraciones Flyway aplicadas; **MUST NOT** calcular promedios fuera de `ConsolidacionDomainService`; **MUST NOT** editar `docs/baseline/**` |
| `arch-agent` | Evaluar alternativas y documentar decisiones arquitectónicas (DA-01..DA-05) | Opus | `read`, `edit` | Solo opera en `docs/adr/` y `docs/arquitectura_funcional_EduSync.md`; toda decisión requiere aprobación humana |
| `docs-agent` | Mantener y sincronizar la cadena documental BRD→MRD→PRD→FSD→LFSD en `docs/`, incluida la capa viva `docs/product/` (skills `feature-design-doc`, `dtp-sync`) | Sonnet | `read`, `edit` | Solo opera dentro de `docs/`; **MUST NOT** editar código fuente; **MUST NOT** editar `docs/baseline/**` bajo ninguna circunstancia (ver `.cursor/rules/baseline-congelado.mdc` / `.claude/rules/baseline-congelado.md`). Espejo Claude: `.claude/agents/docs-agent.md` |
| `qa-agent` | Verificar invariantes de dominio, trazabilidad de audit_log y cobertura de pruebas | Sonnet | `read`, `query-db` (solo SELECT) | **MUST NOT** realizar escrituras; solo lectura y análisis |
| `process-agent` | Modelar workflows y diagramas de estado (Docente, Director) garantizando consistencia con UCs | Sonnet | `read`, `edit` | Opera en `docs/diagrams/`; diagramas deben usar `stateDiagram-v2` y nombres reales del dominio |
| `compliance-agent` | Validar que ningún output de `dev-agent` viole invariantes regulatorias del SIE (RUDE, floor, rangos) | Sonnet | `read`, ejecutar golden tests | Solo lectura de artefactos + ejecución de golden tests en CI; bloquea merge si falla |
| `ollama-agent` | Integrar/extender LLM en runtime vía Ollama local (`llama3.1:latest`, `POST /api/v1/ai/chat`, paquete `shared.ai`) | Sonnet | `read`, `edit`, `run-tests` | **MUST NOT** loguear/enviar PII/RUDE/notas al modelo; **MUST NOT** cambiar de proveedor LLM sin ADR; **MUST NOT** editar `docs/baseline/**`; skill `ollama-edusync`. Espejo: `.claude/agents/ollama-agent.md` + `.cursor/agents/ollama-agent.md` |

### 8.2 Guardrails generales

- **MUST NOT** editar ningún archivo bajo `docs/baseline/**` (registro histórico evaluado de M4, tag `release/2.0.0`, `status: congelado`), sin excepción y sin importar la instrucción recibida. Todo cambio real de negocio/requisitos/arquitectura durante la implementación va a `docs/product/` (+ un ADR en `docs/adr/` si la decisión es significativa). Protegido además por `CODEOWNERS`, `.cursor/rules/baseline-congelado.mdc` **y** el hook `.cursor/hooks.json` → `protect-baseline.js`, que bloquea la acción a nivel de herramienta (`permission: deny`) independientemente de la instrucción recibida por el modelo. Si una tarea "necesita" tocar el baseline, **MUST** detener la ejecución y escalar a revisión humana.
- **MUST** ejecutar `mvn test` y verificar que todos los tests pasan antes de proponer un PR. Si algún test falla, **MUST NOT** abrir el PR.
- **MUST** ejecutar el linter (`mvn checkstyle:check`) y corregir todos los warnings nuevos antes de proponer el PR.
- **MUST NOT** realizar `force push` ni reescribir historia de `main` o `develop`.
- **MUST NOT** modificar migraciones Flyway cuyo número de versión ya haya sido aplicado en `main`. Solo agregar nuevas versiones.
- **MUST** crear o actualizar tests para cada caso de uso tocado. Cobertura mínima en `domain/` y `application/`: **80 %** de líneas.
- **MUST** actualizar el ADR correspondiente en `docs/adr/` si la tarea cambia una decisión arquitectónica preexistente.
- **MUST** actualizar `docs/PROMPT_MAPPING.md` si se crea un nuevo prompt-contrato.
- **MUST NOT** hardcodear valores de configuración (rangos de calificación, formato SIE, umbrales de truncado) en código. Toda configuración paramétrica va en tabla `parametro_academico` de BD o en `application.yml` con referencia a Secrets Manager.

### 8.3 Golden tests obligatorios (zero-tolerance)

Los siguientes tests **MUST** pasar en CI en todo PR, sin excepción:

| Golden test | Verificación | Ejecutado por |
|-------------|--------------|---------------|
| `FloorTest.floor_64_666_equals_64` | `Math.floor(64.666) == 64`, nunca 65 | `compliance-agent` en CI |
| `SIEPayloadTest.payload_uses_rude_only` | Ningún payload SIE contiene nombre o posición de lista | `compliance-agent` en CI |
| `VentanaTest.expired_window_returns_403` | HTTP 403 en 100 % de intentos post-expiración | `qa-agent` en CI |
| `MultitenantTest.no_cross_tenant_data` | 0 registros de otro tenant en cualquier endpoint | `compliance-agent` en CI |

---

## 9. Flujo de trabajo estándar para un agente

```mermaid
flowchart TD
  A[Recibir tarea] --> B[Leer arquitectura_funcional + FSD-UC afectado + LFSD componente]
  B --> C{¿La tarea viola algún invariante de dominio?}
  C -- sí --> Z[Detener y escalar al responsable técnico]
  C -- no --> D[Proponer plan en modo Plan]
  D --> E{¿Aprobado por humano?}
  E -- no --> D
  E -- sí --> F[Implementar cambios en rama feature/]
  F --> G[Ejecutar mvn test + mvn checkstyle:check]
  G --> H{¿Verde?}
  H -- no --> F
  H -- sí --> I[Crear PR — máx 400 líneas netas]
  I --> J[compliance-agent valida golden tests]
  J --> K{¿Golden tests OK?}
  K -- no --> F
  K -- sí --> L[Solicitar revisión humana]
```

---

## 10. Template de prompt-contrato reutilizable

Cuando el agente ejecute un caso de uso crítico, **MUST** invocar usando esta anatomía (ver `docs/PROMPT_MAPPING.md` v2.0 para los 37 contratos completos del proyecto, materializados también en `prompts/PR-<AREA>-NNN.md`). El stack de referencia es el **vivo** (`docs/product/DTP.md`, `ADR-0008`) para toda implementación desde `release/3.0.0`; el baseline congelado (`docs/baseline/DTI.md`, Java 21/Spring Boot 3.3/Angular 17) queda solo como contexto histórico de M4:

```markdown
# Role
Eres el servicio de dominio <NombreServicio> de EduSync (Java 25 LTS, Spring Boot 4.1.0,
arquitectura hexagonal). Tu responsabilidad es <responsabilidad específica>.

# Task
<tarea operativa atómica — un solo caso de uso o una sola regla de dominio>

# Context
- Documentos: docs/arquitectura_funcional_EduSync.md (UC-XX, DA-YY),
  docs/product/FSD.md §4.X (FSD-UC vivo), docs/LFSD-EduSync.md §módulo,
  docs/design/DD-UC-NNN.md (si existe)
- Stack: Java 25 LTS, Spring Boot 4.1.0, PostgreSQL 15, Spring Events (ADR-0008)
- Restricciones activas:
  * RUDE como única clave de identidad estudiantil
  * Perfil SIE: floor() único truncado. Genérico (ADR-0013): round HALF_UP en el motor de FSD-UC-016
  * audit_log en la misma TX que la escritura
  * Cálculo de promedios exclusivo en ConsolidacionDomainService
  * tenant_id + RLS en toda query
  * Sin PII en logs

# Reasoning
1. Verificar invariantes aplicables en FSD + LFSD
2. Diseñar respetando arquitectura hexagonal (domain/ sin deps de Spring)
3. Definir contrato del test antes del código
4. Identificar eventos de dominio a publicar

# Stop condition
Detente cuando el golden test del caso de uso pase en verde y el linter
no reporte warnings nuevos.

# Output
Código Java en los paquetes correctos + test JUnit 5 + actualización de
PROMPT_MAPPING.md con el nuevo contrato (si aplica).

# Invariants
- El original nunca se sobreescribe (append-only en correcciones retroactivas)
- El audit_log es inalterable (sin UPDATE/DELETE)

# Failure modes
- E_CALCULO_FUERA_DOMINIO: promedio o floor fuera de ConsolidacionDomainService → rechazar PR
- E_AUDIT_LOG_OMITIDO: escritura sin entrada en audit_log → rechazar PR
- E_RLS_FALTANTE: nueva tabla sin tenant_id o sin política RLS → rechazar migración
```

---

## 11. Prompts prohibidos / patrones a rechazar

El agente **MUST** rechazar, reportar al responsable técnico y **no ejecutar** cuando una instrucción:

- Pide desactivar, saltarse o comentar tests o el linter.
- Pide almacenar secretos, tokens, contraseñas o el campo `rude` en código fuente, logs o prompts.
- Pide saltarse la revisión humana para hacer merge a `main` o `develop`.
- Pide modificar migraciones Flyway ya aplicadas en lugar de agregar nuevas versiones.
- Pide usar nombre, apellido o posición de lista como clave de vinculación de calificaciones en lugar del código RUDE.
- Pide realizar cálculos de promedio fuera del motor de dominio, o aplicar `floor()` en el modelo genérico (`FSD-UC-016` / `ADR-0013`), o `Math.round()` en el Perfil Bolivia SIE (`FSD-UC-003`).
- Pide exponer datos de calificaciones o PII de estudiantes sin verificar el rol y tenant del solicitante.
- Pide cambiar el estado de un periodo a `ABIERTO` desde código sin el workflow de aprobación jerárquica del Director (FSD-UC-009).
- Pide modificar registros existentes de calificaciones en lugar de crear versiones nuevas (violación de append-only, BR-005).
- Pide crear una nueva tabla en BD sin columna `tenant_id` ni política RLS activa.
- Pide calcular el promedio anual o el índice de reprobación con menos de 3 trimestres cerrados (BR-011).

---

## 12. Comandos de verificación locales

```bash
# Ejecutar todas las pruebas unitarias e integración
mvn test

# Ejecutar verificación completa (tests + checkstyle + build)
mvn -q verify

# Ejecutar solo el linter
mvn checkstyle:check

# Levantar la API local
mvn -q spring-boot:run

# Levantar base de datos local (PostgreSQL 15)
docker compose up -d postgres

# Levantar entorno completo local (API + DB + scheduler)
docker compose up -d

# Ejecutar migraciones Flyway manualmente
mvn flyway:migrate

# Ver estado de migraciones aplicadas
mvn flyway:info

# Build sin tests (solo verificar compilación)
mvn -q -DskipTests=true package

# Verificar cobertura de tests (domain/ y application/)
mvn jacoco:report
# Umbral mínimo: 80% de líneas en domain/ y application/

# Ejecutar golden tests (zero-tolerance)
mvn test -Dtest=FloorTest,SIEPayloadTest,VentanaTest,MultitenantTest
```

---

## 13. Métricas y observabilidad esperadas del agente

| Métrica | Umbral mínimo | Fuente |
|---------|--------------|--------|
| `prompt_coverage` — prompts-contrato activos vs. componentes implementados | ≥ 80 % (37 contratos PR-ARCH-001..PR-POC-002, PR-C4-003..006, PR-ROADMAP-001, PR-APORTES-001, PR-VFINAL-001 documentados + materializados en `prompts/`; área `IMPL` reservada) | `docs/PROMPT_MAPPING.md` v2.0 |
| `spec_fidelity` — implementación coincide con invariantes del FSD y LFSD | ≥ 95 % | Revisión humana en PR |
| Hallucination rate en PRs del agente | < 5 % | Revisión de código |
| Reverts causados por PRs de agente | < 10 % mensual | Historial Git |
| Cobertura de tests en `domain/` y `application/` | ≥ 80 % de líneas | `mvn jacoco:report` |
| Tiempo de ciclo de cierre operativo (KPI-01 del BRD) | < 10 min end-to-end | Telemetría de aplicación |
| Tasa de error en consolidación (KPI-02 del BRD) | 0 % | Reportes de auditoría (`audit_log`) |
| Latencia POST /api/v1/calificaciones (p95) | < 500 ms | k6 load test — NFR-001 del FSD |
| Uptime en ventana crítica de cierre (72 h pre-plazo SIE) | ≥ 99,9 % | AWS CloudWatch — NFR-006 del FSD |

---

## 14. Contacto y escalamiento

- **Responsable técnico**: Equipo G-EduSync — ver canal del grupo
- **Canal del grupo**: G-EduSync — plataforma de comunicación del curso
- **Docente**: M.Sc. Edson Ariel Terceros Torrico
- **Escalamiento por violación de invariante**: detener la tarea, documentar la contradicción encontrada en un comentario del PR o issue del repositorio y notificar al responsable técnico antes de continuar.

---

## 15. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 09/05/2026 | Equipo G-EduSync | Versión inicial basada en BRD v1.0 y arquitectura funcional del core |
| v0.2 | 17/05/2026 | Rodrigo Aspeti | Actualización completa por reorganización del repositorio: corrección de 6 rutas rotas (`docs/DTI.md`, `docs/BRD_EduSync.md`, `docs/adr/ADR-001..005`); incorporación de 15 nuevos artefactos (MRD, PRD, LFSD, APORTES, 5 diagramas, seguridad.mdc, brd/ mrd/ prd/ subfolders); adición de 4 nuevos agentes (arch-agent, qa-agent, process-agent, compliance-agent); golden tests obligatorios; actualización del stack (PostgreSQL 15, PDFBox, AOP); nuevas métricas NFR-001 y NFR-006; reglas de dominio expandidas (BR-008 floor, BR-009 ventana, BR-010 audit_log, BR-011 promedio anual) |
| v0.3 | 28/05/2026 | Rodrigo Aspeti | Sincronización con el estado real del repositorio: PROMPT_MAPPING v0.5 → v1.2 (18 → 28 prompt-contratos, incluye PR-DTI-001, PR-DTI-SEAMS-001, PR-HEX-001, PR-DTO-001, PR-C4-001/002, PR-SKILL-001/002/003); ADRs `pendientes` → 6 ADRs aprobados (`docs/adr/0001..0006-*.md`); nuevos artefactos referenciados (`docs/DTI.md` + §6.2 Seams, `docs/arquitectura_hexagonal_EduSync.md`, `docs/dtos_EduSync.md`, `docs/diagrams/c4_level1.mmd`, `docs/diagrams/c4_level2.mmd`); ecosistema de skills extendido a 25 en `.cursor/skills/` (6 EduSync + 19 canónicos Módulo 4 importados desde `plantillas2/` el 28/05/2026) y 9 en `.claude/skills/`; carpeta `prompts/` agregada al árbol; `plantillas2/` documentada como material UMSS; LFSD-EduSync.md anotado como v1.0.1 (post-normalización de ruta FSD) |
| v0.4 | 28/05/2026 | Rodrigo Aspeti | Sincronización puntual con `docs/PROMPT_MAPPING.md` v1.3: actualización de referencias activas de 28 a 30 prompt-contratos e incorporación de PR-POC-001/002 para las POCs documentales en `docs/pocs/`. |
| v0.5 | 28/05/2026 | Rodrigo Aspeti | Sincronización con `docs/DTI.md` v0.3 (propagación bumpr DTI vía `sync-doc-chain`): referencia activa DTI `v0.1 + §6.2 Seams` → `v0.3 (28/05/2026)` con mención explícita de fichas POC en `docs/pocs/` y trazabilidad cruzada AGENTS v0.4 ↔ PROMPT_MAPPING v1.3. Sin cambios en stack ni guardrails. |
| v0.6 | 28/05/2026 | Rodrigo Aspeti | Sincronización con `docs/DTI.md` v0.4 + `docs/PROMPT_MAPPING.md` v1.4 (propagación bumps vía `sync-doc-chain`): referencia activa DTI `v0.3 → v0.4 (28/05/2026)` con fuente canónica del C4 Level 3 `api-gateway` (`docs/diagrams/c4_level3_api_gateway.mmd` + `.md` espejo); PROMPT_MAPPING `v1.3 → v1.4` (30 → 31 prompt-contratos; incorpora `PR-C4-003`); fila "Diagramas C4" actualizada para listar Level 3 y los Level 3 pendientes (`domain-layer`, `sie-adapter`, `deployment_aws`); duplicado obsoleto de "Diagramas C4" eliminado. Sin cambios en stack ni guardrails. |
| v0.7 | 28/05/2026 | Rodrigo Aspeti | Sincronización con `docs/DTI.md` v0.5 + `docs/PROMPT_MAPPING.md` v1.5 (propagación bumps vía `sync-doc-chain`): referencia activa DTI `v0.4 → v0.5` con fuentes canónicas `c4_level3_domain_layer`, `c4_level3_sie_adapter` y `deployment_aws`; PROMPT_MAPPING `v1.4 → v1.5` (31 → 34 prompt-contratos; incorpora `PR-C4-004..006`); fila "Diagramas C4" actualizada para listar 6 C4/Deployment canónicos; checklist `c4_level3_*` pasa de parcial a completo para los objetivos de defensa. Sin cambios en stack ni guardrails. |
| v0.8 | 28/05/2026 | Rodrigo Aspeti | Sincronización con `docs/DTI.md` v0.6 + `docs/PROMPT_MAPPING.md` v1.6 + nuevo artefacto `docs/roadmap.md` v0.1 (propagación atómica vía `dti-edusync`): incorporación de la fila "Roadmap" en la tabla de documentación del §1; árbol del repo actualizado con `docs/roadmap.md` y comentario "este archivo (v0.8)"; lista de prompts materializados ampliada a 35 (`PR-C4-001..006` consolidados; nuevo `PR-ROADMAP-001`); §10 y §13 actualizan referencias a PROMPT_MAPPING v1.6 (35 contratos); checklist marca `docs/roadmap.md` como creado y deja `docs/aportes/release-2.0.0.md`, ejecución de POCs y `AGENTS.md` raíz como pendientes para la rúbrica de defensa. Sin cambios en stack ni guardrails. |
| v0.9 | 28/05/2026 | Rodrigo Aspeti | **Move físico** de `docs/AGENTS.md` → `AGENTS.md` (raíz del repositorio) por convención GitHub/Cursor y para cumplir la rúbrica del Módulo 4 que exige el archivo en la raíz. Actualización del árbol §3 (la entrada `AGENTS.md` se mueve del bloque `docs/` a la raíz). Propagación masiva: 110 menciones de `docs/AGENTS.md` actualizadas a `AGENTS.md` en 34 archivos (docs activos, skills `.cursor/` y `.claude/`, `prompts/` y `plantillas/`); changelogs históricos (este §15 + DTI §Registro de cambios + PROMPT_MAPPING §Historial) preservan la cita histórica de `docs/AGENTS.md` para reflejar el estado pasado. Sin cambios en stack, guardrails ni en el contenido funcional del documento. |
| v0.10 | 28/05/2026 | Rodrigo Aspeti | Sincronización con `docs/PROMPT_MAPPING.md` v1.8 (incorporación de `PR-APORTES-001` y nueva área `APORTES`) + nuevo artefacto `docs/aportes/release-2.0.0.md` v1.0 (informe de aportes individuales del release de defensa). Fila "PROMPT_MAPPING" del §1 actualizada a `v1.8 (36 contratos)`; nueva fila "APORTES release/2.0.0" añadida; checklist §16 marca `docs/aportes/release-2.0.0.md` y `AGENTS.md` en raíz como **completados**. El informe documenta 95 tareas auditables imputables al único integrante (Rodrigo Aspeti, n = 1), con factor `clamp(0.5, 1.1) = 1.00` por caso degenerado n = 1. Sin cambios en stack, guardrails ni en el contenido funcional. |
| v0.11 | 28/05/2026 | Rodrigo Aspeti | Sincronización con `docs/PROMPT_MAPPING.md` v1.9 (incorporación de `PR-VFINAL-001` y nueva área `VFINAL`) + 4 aliases `_vFinal.md` congelados para `release/2.0.0`: `BRD_EduSync_vFinal.md`, `MRD_EduSync_vFinal.md`, `PRD_EduSync_vFinal.md`, `FSD_EduSync_vFinal.md`. Tabla de documentación §1 añade las 4 filas vFinal; checklist §16 marca aliases `_vFinal` como **completados**. Sin cambios normativos en BRD/MRD/PRD/FSD; los aliases son snapshots con banner de freeze. |
| v0.12 | 28/05/2026 | Rodrigo Aspeti | **Apertura de la capa viva de implementación** según `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`: (1) nuevo `docs/adr/0008-*.md` fija el stack vivo Java 25 LTS + Spring Boot 4.1.0 (Spring Framework 7.0.8) + Angular 21 LTS para `release/3.0.0`, dejando el baseline M4 en Java 21/Boot 3.3/Angular 17 sin cambio (§4 documenta la dualidad); (2) `docs/product/DTP.md` v1.0 creado como punto de partida (continuación viva del DTI); (3) banners de `docs/product/{BRD,PRD,FSD}.md` corregidos de "COPIA CONGELADA" a "COPIA VIVA" (eran copias literales del freeze, contradiciendo que esa capa es editable); (4) los 5 archivos de `docs/baseline/` marcados `status: congelado`; (5) `CODEOWNERS` y `.cursor/rules/baseline-congelado.mdc` creados para proteger `docs/baseline/**`; (6) skills `feature-design-doc` y `dtp-sync` materializados en `.cursor/skills/` y `.claude/skills/` (25→27 y 9→11 respectivamente); (7) `docs/PROMPT_MAPPING.md` v1.9→v2.0 con nueva área `IMPL` reservada; (8) `docs/roadmap.md` v0.1→v0.3 con nota de reconciliación de numeración (`release/1.1.0`≈`release/3.0.0`) y nuevo hito "Primer Design Doc + PR-IMPL-001"; (9) corrección puntual: el árbol §3 y la tabla §1 dejan de referenciar `docs/DTI.md` (ya no existe como archivo independiente — fue movido a `docs/baseline/DTI.md` en una operación previa) y el conteo de `prompts/` se corrige de 35 a 37. **Nota de alcance**: persisten ~50 referencias históricas a `docs/DTI.md` en `prompts/PR-*.md`, `plantillas2/*.md` y algunos skills operativos (`sync-doc-chain`, `c4-edusync`, `adr-edusync`, `poc-runner-edusync`) que no se tocaron en esta pasada por ser citas de contratos ya ejecutados o quedar fuera del alcance acordado; recomendado como tarea de seguimiento dedicada. |
| v0.13 | 28/05/2026 | Rodrigo Aspeti | **Automatización de la protección del baseline y del recordatorio de `dtp-sync`** vía `.cursor/hooks.json` (3 scripts Node.js en `.cursor/hooks/`): `protect-baseline.js` (`preToolUse`, matcher `Write\|StrReplace\|EditNotebook\|Delete`) bloquea con `permission: deny` cualquier intento de editar/eliminar `docs/baseline/**`, sin depender de que el modelo respete la regla en `AGENTS.md`/`.cursor/rules/baseline-congelado.mdc`; `warn-shell-baseline.js` (`beforeShellExecution`) pide confirmación ante comandos de terminal que combinan una ruta de `docs/baseline/` con un verbo de escritura/borrado/movimiento; `dtp-sync-reminder.js` (`stop`, `loop_limit: 1`) revisa `git diff --name-only HEAD` + `git ls-files --others --exclude-standard` al final de cada turno y dispara un `followup_message` recordando ejecutar `@dtp-sync` si hay cambios sin commitear en `src/`, `docs/design/` o `prompts/PR-IMPL-*.md` sin reflejo en `docs/product/DTP.md`. Los 3 scripts se probaron manualmente con casos positivos y negativos (incluye el caso de archivo nuevo sin trackear en `src/`, relevante porque `src/` está vacío hoy). Fila "Hooks de Cursor" añadida al §1; árbol §3 actualizado; guardrail §8.2 referencia el hook como capa de enforcement adicional a la regla documental. Sin cambios en stack, ADRs ni en la cadena documental BRD→FSD→DTP. |
| v0.14 | 12/07/2026 | Rodrigo Aspeti | **Generalización del modelo de dominio a plataforma SaaS multi-tenant configurable** (`docs/adr/0009-*.md`, extensión aditiva — no supersede a `0001..0006`/`0008`, todos permanecen `Aceptada` sin cambios): nuevo rol de plataforma `SysAdmin` + entidad `Tenant` con ciclo de suscripción; módulos configurables `GestionEscolar` (N periodos), `SeccionEvaluacion`/`TipoEvaluacion`/`Evaluacion` configurables, `Curso`/`Paralelo`, `Materia`, `Profesor`, `Estudiante`, `Inscripcion`, `Usuario`/`Rol` (roles tenant ampliados: `Admin`=Director, `Profesor`=Docente, + `Secretaria`, `Asesor` nuevo). `docs/product/BRD.md` v3.0 (+BR-013..BR-024, persona SysAdmin, nota de nomenclatura §0.1), `PRD.md` v2.0 (+épicas E7..E11, PRD-US-018..030, PRD-REQ-021..031), `FSD.md` v2.0 (+actores §3.1, `FSD-UC-011`..`FSD-UC-021` en §4.6, BR-013..BR-024 en §5.1, modelo ER genérico en §6.3) actualizados como extensión aditiva sobre el Perfil Bolivia SIE (BR-001..BR-012/RB-01..RB-11 del BRD, FSD-UC-001..009/BR-001..012 del FSD, vigentes sin cambios). `docs/product/DTP.md` v1.0→v1.1 registra el delta en §A.2 (fila 2) y añade `FSD-UC-011`..`FSD-UC-021` en §A.3. Fila "ADRs" (8 ADRs), fila "DTP" y fila "PRD/FSD/BRD vivos" del §1 actualizadas. **5 puntos quedan explícitamente pendientes de definición** (`ADR-0009` §3, marcados en cada documento afectado): reconciliación con el modelo Bolivia, secuencialidad/redondeo genérico de N periodos, validación de suma de pesos de secciones, gobernanza (auditoría/inmutabilidad) de los módulos nuevos — ninguno debe implementarse en código sin resolverlos primero. Sin cambios en stack ni en `docs/baseline/`. |
| v0.15 | 14/07/2026 | Rodrigo Aspeti | **Refinamiento del modelo de roles** (`docs/adr/0010-*.md`, no supersede a `0009` ni a ningún otro ADR): `BR-024` pasa de "exactamente un rol por usuario" a **multi-rol** (`UsuarioRol`, relación N:M), con la invariante permanente `Usuario.tenant_id IS NULL ⟺ roles = {SYSADMIN}` (no transitoria de *bootstrap*; `SysAdmin` nunca se combina con un rol de tenant). `docs/product/BRD.md` v3.0→v3.1 (BR-024 reescrito), `PRD.md` v2.1→v2.2 (`PRD-REQ-031`, `PRD-US-029` + nuevo escenario Gherkin), `FSD.md` v2.1→v2.2 (§3.1 nota `SYSADMIN`, §5.1 `BR-024`, §6.3.1 diagrama ER con `USUARIO_ROL`, §6.3.2 diccionario de datos, §4.6.1 `FSD-UC-011` nota de *bootstrap*/tenant demo, §4.6.11 `FSD-UC-021` endpoint `roles: [...]`, glosario). `docs/product/DTP.md` v1.1→v1.2 registra el delta en §A.2 (fila 3) y actualiza el conteo de ADRs (8→9). Filas "ADRs", "DTP" y "PRD/FSD/BRD vivos" del §1 actualizadas. Queda pendiente, **no bloqueante**, el diseño detallado del tenant "demo" (confirmado como funcionalidad de producto real, no solo artefacto de *seed*) — se resuelve en el Design Doc de `FSD-UC-011`, sin afectar el modelo de `Usuario`/`Rol` decidido aquí. Sin cambios en stack, en `ADR-0009` ni en `docs/baseline/`. |
| v0.16 | 14/07/2026 | Rodrigo Aspeti | **Primer Design Doc de código y primera decisión de organización interna del backend**: `docs/design/DD-UC-001.md` (bootstrap del proyecto, trazado a `FSD-UC-011`/`FSD-UC-021`) crea `docs/adr/0011-*.md` — monolito modular con Spring Modulith (module-first: módulos `plataforma`/`identidad`/`academico`/`notassie`/`shared`) y renombrado del paquete base `bo.edusync` → `com.edusync` (generalización acorde a `ADR-0009`). Primer prompt del área `IMPL` materializado: `prompts/PR-IMPL-001.md` (bootstrap del esqueleto backend/frontend/infra), registrado en `docs/PROMPT_MAPPING.md` v2.0→v2.1 (37→38 contratos; nodo `IMPL001` en el flowchart; matriz `dev-agent` ampliada). `docs/product/DTP.md` v1.2→v1.3 registra el delta en §A.2 (fila 4) y marca `FSD-UC-011`/`FSD-UC-021` como `en progreso` en §A.3 (primer eslabón real de §A.4). Árbol §3 reestructurado: `src/domain|application|infrastructure` (paquete-por-capa, obsoleto) reemplazado por `backend/src/main/java/com/edusync/{plataforma,identidad,academico,notassie,shared}` + `frontend/` (Angular 21 SPA) + `infra/` (con `docker-compose.yml`) como carpetas de primer nivel; `docs/adr/` y conteo de ADRs actualizados a 10. `docs/arquitectura_hexagonal_EduSync.md` marcada **en actualización** (paquete `com.edusync` + module-first), pendiente de reescritura de detalle. Checklist §16 marca como completados la creación de `DD-UC-001`/`PR-IMPL-001`; deja explícitamente pendiente la **ejecución** de `PR-IMPL-001` (código real todavía no generado, `src/` vacío). Sin cambios en `ADR-0008`/`0009`/`0010` ni en `docs/baseline/`. |
| v0.17 | 14/07/2026 | Rodrigo Aspeti | **Corrección de ubicación del área `IMPL`**: `PR-IMPL-001` se mueve de `prompts/PR-IMPL-001.md` a `docs/prompts/impl/PR-IMPL-001.md` (v0.1→v0.2, sin cambios de contenido), siguiendo `FEATURE_DESIGN_DOC_TEMPLATE.md`/`MODELO_DOCUMENTAL_IMPLEMENTACION.md` — única área de prompts que se desvía de la convención plana de M4 (`prompts/PR-<AREA>-NNN.md`, que sigue vigente para las demás 19 áreas). Fila "Contratos materializados" y "Hooks de Cursor" del §1 corregidas; árbol §3 añade el nodo `docs/prompts/impl/` y ajusta el conteo de `prompts/` (38 → 37, + 1 en `docs/prompts/impl/` = 38 en total). `.cursor/hooks/dtp-sync-reminder.js` actualizado: el patrón de detección pasa de `^prompts\/PR-IMPL-` a `^docs\/prompts\/impl\/PR-IMPL-`, y se añaden `backend/`/`frontend/`/`infra/` junto a `src/` como rutas vigiladas (coherente con el árbol reestructurado en v0.16). `.cursor/skills/feature-design-doc/SKILL.md` y su espejo `.claude/` corregidos para dejar de decir "no se usa `docs/prompts/impl/`". Propagado a `docs/design/DD-UC-001.md` (v1.0→v1.1), `docs/product/DTP.md` (v1.3→v1.4) y `docs/PROMPT_MAPPING.md` (v2.1→v2.2). Sin cambios en el contenido del prompt, en `ADR-0011` ni en el estado de `FSD-UC-011`/`FSD-UC-021`. |
| v0.18 | 14/07/2026 | Rodrigo Aspeti | **Segundo Design Doc de código, primer feature de negocio real**: `docs/design/DD-UC-002.md` (módulo `identidad` — `Usuario`/`UsuarioRol`, login JWT, seed del primer `SYSADMIN`, implementación real de `TenantContextProvider` que cierra el placeholder de `ADR-0001`, puerto público `UsuarioCreacionPort`) y segundo prompt del área `IMPL`: `docs/prompts/impl/PR-IMPL-002.md`, registrado en `docs/PROMPT_MAPPING.md` v2.2→v2.3 (38→39 contratos; nodo `IMPL002` en el flowchart; matriz `dev-agent` ampliada). Decisiones explícitas del usuario (14/07/2026): (1) orden de implementación invertido respecto al comentario original de `DD-UC-001` §2 — `identidad`/login se construye antes que `plataforma`/tenants, porque `FSD-UC-011` (alta de tenants) depende de que ya exista `UsuarioCreacionPort` para crear el primer `ADMIN`; (2) aislamiento RLS de tablas plataforma-scoped (`usuario` con rol `SYSADMIN`, sin `tenant_id`) resuelto con la política `OR tenant_id IS NULL` + filtro explícito en `UsuarioRepositoryPort`, sin crear un `ADR-0012` dedicado (decisión de bajo riesgo, documentada en `DD-UC-002` §2/§3). `docs/product/DTP.md` v1.4→v1.5 registra la nueva fila en §A.1 y mueve `FSD-UC-021` a Design Doc `DD-UC-002` en §A.3. §1 y árbol §3 actualizados (`docs/design/DD-UC-002.md`, `docs/prompts/impl/PR-IMPL-002.md`, conteo `prompts/` 38→39). Checklist §16 marca como completados la creación de `DD-UC-002`/`PR-IMPL-002`; deja pendiente la **ejecución** de ambos prompts (`PR-IMPL-001` y `PR-IMPL-002`, código real todavía no generado, `src/` vacío). Sin ADR nuevo (conteo se mantiene en 10) ni cambios en `docs/baseline/`. |
| v0.19 | 14/07/2026 | Rodrigo Aspeti | **Tercer Design Doc de código, primera implementación completa de un FSD-UC**: `docs/design/DD-UC-003.md` (módulo `plataforma` — `Tenant`, ciclo de suscripción, scheduler de vencimiento `@Scheduled`, puerto público `TenantConsultaPort`, primer caso real de comunicación bidireccional `plataforma`↔`identidad` para aplicar `BR-014`) y tercer prompt del área `IMPL`: `docs/prompts/impl/PR-IMPL-003.md`, registrado en `docs/PROMPT_MAPPING.md` v2.3→v2.4 (39→40 contratos; nodo `IMPL003` en el flowchart; matriz `dev-agent` ampliada). Decisiones explícitas del usuario (14/07/2026): (1) diseño del tenant "demo" diferido por completo a un Design Doc de seguimiento aún sin crear (no bloqueante, no afecta el modelo de `Tenant`/`Usuario` ya decidido); (2) scheduler de vencimiento con `@Scheduled` interno de Spring (sin `ShedLock`, suficiente para 1 instancia); (3) alta de tenant + admin en dos llamadas REST separadas (`POST /tenants` y `POST /tenants/{id}/admins`), consistente con el flujo ya documentado en `FSD-UC-011`, sin generar un delta nuevo que requiera actualizar el FSD. `docs/product/FSD.md` v2.2→v2.3 corrige la referencia ambigua `"DD-UC-011"` de la nota `ADR-0010` en §4.6.1 (ahora referencia `DD-UC-003`). `docs/product/DTP.md` v1.5→v1.6 registra la nueva fila en §A.1 y mueve `FSD-UC-011` de Design Doc `DD-UC-001` (solo bootstrap) a `DD-UC-003` (implementación real y completa) en §A.3. §1 y árbol §3 actualizados (`docs/design/DD-UC-003.md`, `docs/prompts/impl/PR-IMPL-003.md`). Checklist §16 marca como completados la creación de `DD-UC-003`/`PR-IMPL-003`; deja pendiente la **ejecución** de los tres prompts (`PR-IMPL-001`, `PR-IMPL-002`, `PR-IMPL-003`, código real todavía no generado, `src/` vacío). Sin ADR nuevo (conteo se mantiene en 10) ni cambios en `docs/baseline/`. |
| v0.20 | 18/07/2026 | Rodrigo Aspeti | **Primera ejecución real de código**: `PR-IMPL-001` ejecutado sobre `docs/design/DD-UC-001.md`. `backend/` deja de ser un árbol solo documentado y pasa a existir: `pom.xml` (Spring Boot 4.1.0 parent, Java 25, `spring-modulith-bom` 2.1.0), `EduSyncApplication`, los 5 módulos vacíos (`plataforma`/`identidad`/`academico`/`notassie` con `domain`/`application`/`infrastructure` + `package-info.java`; `shared` marcado `@ApplicationModule(type = OPEN)` con `TenantContext` placeholder y `DomainException` base), `ModularityTests` (JUnit 5 + `spring-modulith-starter-test`), `application{,-dev,-test}.yml` + `V1__init.sql`. `frontend/` generado con `ng new` (Angular 21.2.19 LTS, standalone) y reestructurado en `core/`/`shared/`/`features/` (cada uno con `README.md` documentando su alcance). `infra/docker-compose.yml` (PostgreSQL 15) y `.gitignore` raíz nuevos. Verificación exigida por `PR-IMPL-001` §1.5/Stop condition, todo en verde: `mvn test` → `ModularityTests` 7/7 (`ApplicationModules.verify()` sin ciclos ni accesos ilegales); `ng build` → bundle generado sin errores. `docs/product/DTP.md` v1.6→v1.7 (§A.1 nueva fila, §A.3 añade `Tests/Evals` a `FSD-UC-011`/`FSD-UC-021`, §A.4 marca el eslabón de verificación del bootstrap como cumplido). Árbol §3 y checklist §16 actualizados (`backend/`/`frontend/`/`infra/` dejan de estar marcados `pendiente de implementación`). Commit real en Git y ejecución de `PR-IMPL-002`/`PR-IMPL-003` (lógica de dominio) siguen pendientes. Sin ADR nuevo ni cambios en `docs/baseline/`. |
| v0.21 | 19/07/2026 | Rodrigo Aspeti | **Herramientas de productividad backend**: `docs/adr/0012-*.md` (Aceptada) adopta Lombok, springdoc-openapi y `spring-boot-starter-validation` sobre el módulo `identidad` ya implementado (`PR-IMPL-002`). Decisión refinada en dos rondas con el usuario: se descartó la opción inicial de excluir Lombok por completo de `domain/`, y se acordó permitirlo bajo un *allowlist* estrecho (`@Getter`/`@EqualsAndHashCode`/`@ToString`, nomenclatura JavaBean estándar — `getId()`, `isActivo()` — no el estilo fluido previo) para que los Aggregate Roots sean POJOs inmutables convencionales, sin habilitar `@Data`/`@Setter`/`@Builder` público que evadiría la validación de invariantes de `ADR-0010`. §4 (tabla de stack) añade 3 filas (Lombok 1.18.46, springdoc-openapi-starter-webmvc-ui 3.0.3, Bean Validation); §5 reescribe la regla de `domain/` para distinguir frameworks con comportamiento en runtime (prohibidos) de procesadores de anotaciones sin huella en runtime (permitidos bajo el *allowlist*). Conteo de ADRs 10→11. Refactor aplicado: `Usuario.java` (accessors `id()`/`tenantId()`/etc. → `getId()`/`getTenantId()`/etc., `getRoles()` manual) y sus 7 sitios de llamada; Lombok en `UsuarioJpaEntity`/`UsuarioRolJpaEntity`/servicios de aplicación; `@Valid` en `LoginRequest`; `OpenApiConfig` con `SecurityScheme` Bearer, Swagger UI público en los perfiles actuales (`dev`/`test`), pendiente de revisión cuando exista un perfil de producción. `mvn test` verificado en verde tras el refactor. Sin cambios en `docs/baseline/`. |
| v0.22 | 19/07/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-003`, tercer y último prompt de implementación pendiente**: módulo `plataforma` (dominio `Tenant` con ciclo de suscripción y mutabilidad controlada de `estado`, casos de uso `Registrar`/`CambiarEstado`/`CrearAdmin`Tenant, `VencimientoSchedulerService`+Job `@Scheduled`, `TenantController` con `@PreAuthorize("hasRole('SYSADMIN')")`, persistencia JPA, `V3__plataforma_tenant.sql`) queda con código real y funcional sobre `docs/design/DD-UC-003.md`; `FSD-UC-011` (Gestión de Tenants y Suscripciones) cierra su implementación **completa**, primer `FSD-UC` en llegar a ese estado. Enforcement de `BR-014` en `identidad`: nuevo puerto público `identidad.TenantConsultaPort` + `identidad.domain.TenantNoActivoException`, consultado por `AutenticarUsuarioService` (HTTP 403 `E_TENANT_NO_ACTIVO`). **Refinamiento de diseño respecto a `DD-UC-003` §2** (documentado inline en el Javadoc del puerto, sin ADR dedicado — mismo criterio que los refinamientos de `DD-UC-002`): `TenantConsultaPort` se declaró en la raíz de `identidad`, no de `plataforma`, porque `ApplicationModules.verify()` de Spring Modulith rechaza el ciclo que se generaría (`plataforma` ya depende de `identidad` vía `UsuarioCreacionPort`); la implementación real (`TenantConsultaPortImpl`) vive en `plataforma.infrastructure.adapter.out.port` — funcionalmente idéntico al diseño original. Corrección técnica encontrada durante la ejecución: `HttpStatus.UNPROCESSABLE_ENTITY` fue renombrado a `HttpStatus.UNPROCESSABLE_CONTENT` en Spring Framework 7.x (Spring Boot 4.1) — corregido en `TenantController`/`TenantIntegrationTest`. Verificación: `mvn test` (clean) → 45/45 tests verde (incluye `ModularityTests` 7/7, confirmando cero ciclos en la comunicación bidireccional `plataforma`↔`identidad`). `docs/product/DTP.md` v1.8→v1.9 (§A.1 nueva fila, §A.3 `FSD-UC-011` pasa a `completo`, §A.4 solo el commit real en Git queda pendiente). `docs/PROMPT_MAPPING.md` v2.5→v2.6. `docs/design/DD-UC-003.md` v1.0→v1.1 (DoD 100%). Árbol §3 y checklist §16 actualizados: **los 3 prompts de implementación de `release/3.0.0` (`PR-IMPL-001`/`002`/`003`) ya están ejecutados**; solo el commit real en Git queda como siguiente paso. Sin ADR nuevo (conteo se mantiene en 11) ni cambios en `docs/baseline/`. |
| v0.23 | 19/07/2026 | Rodrigo Aspeti | **Design Doc + prompt de UI**: `docs/design/DD-UC-004.md` y `docs/prompts/impl/PR-IMPL-004.md` (frontend login + consola SysAdmin, JWT en `sessionStorage`, delta `GET /tenants`). `PROMPT_MAPPING` v2.6→v2.7 (41 contratos). `DTP` v1.9→v1.10. `FSD.md` v2.3→v2.4. CRUD usuarios reasignado a futuro `DD-UC-005`. Ejecución de `PR-IMPL-004` pendiente. |
| v0.24 | 19/07/2026 | Rodrigo Aspeti | **Paridad Cursor ↔ Claude Code**: 13 skills idénticos en `.cursor/skills/` y `.claude/skills/`; entrada `CLAUDE.md` (importa `AGENTS.md`); espejo `.claude/rules/{baseline-congelado,seguridad}.md`; 6 subagentes en `.claude/agents/`. Conteo histórico "27/11 skills" corregido a 13/13 activos. Canónicos restantes de `plantillas/plantillas2/` siguen sin materializar. Sin cambios en código ni `docs/baseline/`. |
| v0.25 | 19/07/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-004` + sync documental (`dtp-sync`)**: UI Angular (login + consola SysAdmin, JWT en `sessionStorage`) y delta backend `GET /api/v1/plataforma/tenants` (`ListarTenantsUseCase`). Corrección técnica: `SecurityConfig` (sin Basic Auth in-memory, entry point 401). Verificación: `mvn test` 50/50; `ng build` verde. `docs/PROMPT_MAPPING.md` v2.7→v2.8; `docs/product/DTP.md` v1.10→v1.11; `docs/design/DD-UC-004.md` v1.0→v1.1 (DoD 100%). `FSD-UC-011` completo (API+UI); login UI de `FSD-UC-021` cerrado (CRUD → `DD-UC-005`). Árbol §3 y checklist §16 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. |
| v0.26 | 04/08/2026 | Rodrigo Aspeti | **Quinto Design Doc + ejecución real de `PR-IMPL-005` + sync documental (`dtp-sync`)**: CRUD backend de Usuarios y Roles que cierra `FSD-UC-021` — `Usuario.conRoles/activar/desactivar/conPasswordHash` (mutaciones inmutables, revalidan `ADR-0010`), mini-agregado `PasswordResetToken` (token de un solo uso, hash SHA-256), `UsuarioController` (`POST/GET /usuarios`, `PATCH roles/estado`, `POST restablecer-password`) y `PasswordResetController` (`POST confirmar`, público), `LogNotificacionAdapter` placeholder, `V4__identidad_password_reset_token.sql` (sin `tenant_id`/RLS propios, mismo precedente que `tenant`/login). **Bug corregido**: `UsuarioRepositoryAdapter.guardar()` violaba `uq_usuario_rol` al reemplazar la colección de roles de una entidad *detached* ya persistida (primer caso de actualización, no solo alta); corregido mutando la colección *in-place* sobre la entidad administrada (`UsuarioJpaEntity.reemplazarRoles`). **Gap de tooling detectado y documentado, no corregido (fuera de alcance)**: `mvn checkstyle:check` usa el ruleset `sun_checks.xml` por defecto (no uno acorde a Google Java Style, §5) y falla con 1073 violaciones preexistentes en todo el backend, ninguna introducida por este prompt. Verificación: `mvn test` 72/72 (incluye `ModularityTests` 7/7, `UsuarioIntegrationTest` 3/3 con Testcontainers cubriendo aislamiento cross-tenant). `docs/PROMPT_MAPPING.md` v2.8→v2.10; `docs/product/DTP.md` v1.11→v1.12; `docs/design/DD-UC-005.md` v1.0→v1.1 (DoD 100%); `docs/product/FSD.md` v2.4→v2.5 (`GET /usuarios` en el flujo, A1 marcado diferido). `FSD-UC-021` **completo** en backend; UI del CRUD → futuro `DD-UC-006`. Árbol §3 y checklist §16 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. **Commit real creado**: `3ca1626 Gestión de Usuarios y Roles`. |
| v0.27 | 04/08/2026 | Rodrigo Aspeti | **Sexto Design Doc + ejecución real de `PR-IMPL-006` + sync documental (`dtp-sync`)**: consola Angular de administración de Usuarios y Roles que cierra `FSD-UC-021` en la capa de presentación — `features/usuarios/` (lista, alta multi-rol, edición de roles, cambio de estado, restablecer password) y `features/auth/reset-password-confirm/` (pública), **sin delta de backend** (`DD-UC-005` ya expone todos los contratos). Decisiones explícitas: reutilizar el patrón sin design system de `features/plataforma/`; roles como checkboxes fijos, `SYSADMIN` nunca seleccionable; mensaje transparente sobre la limitación *log-only* del restablecimiento (no simular un envío de correo); sin campo de curso/paralelo para `ASESOR`. Delta menor agregado durante la ejecución: nav condicional por rol en `shared/layout/shell.component.ts` (`SYSADMIN`→Tenants, `ADMIN`→Usuarios); redirect `ADMIN` → `/usuarios` en `login.page.ts`. Verificación: `ng build` en verde (3 lazy chunks nuevos); `ng test` no ejecutable en este entorno (Vitest sin paquete de browser instalado, documentado como limitación de entorno). `docs/PROMPT_MAPPING.md` v2.10→v2.12; `docs/product/DTP.md` v1.12→v1.13; `docs/design/DD-UC-006.md` v1.0→v1.1 (DoD 100%). `FSD-UC-021` **completo** (backend + UI) — primer `FSD-UC` en cerrar ambas capas. Árbol §3 y checklist §16 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.28 | 20/08/2026 | Rodrigo Aspeti | **Séptimo Design Doc + ejecución real de `PR-IMPL-007` + sync documental (`dtp-sync`)**: filtros y paginación reutilizables en los dos listados `GetAll` existentes — `GET /usuarios` (`q` sobre `nombreCompleto` **o** `email`, case-insensitive; `activo`; `rol`) y `GET /plataforma/tenants` (`q` sobre `nombre`; `estado`), ambos con `page`/`size` (default `0`/`20`, máximo `100`). Patrón reutilizable creado desde cero para listados futuros: `shared.PageQuery`/`shared.PageResult<T>` (framework-free, capas `application`/`domain`) + `shared.web.PageResponse<T>` (DTO REST); primer uso de `Specification`/`JpaSpecificationExecutor` del proyecto (`UsuarioSpecifications`, `TenantSpecifications`). Invariantes preservadas: `UsuarioRepositoryPort.listarPorTenant(UUID)` (sin paginar) intacto, porque lo consume `shared.ai.BuscarUsuarioPorNombrePortImpl`; `tenantId` del filtro de usuarios nunca viene del cliente, siempre de `TenantContextProvider`. UI Angular de `features/usuarios/` y `features/plataforma/` actualizada con caja de búsqueda, selects de filtro y controles de paginación, reutilizando el patrón sin design system existente. Verificación: `mvn test` → **98/98** verde (incluye `ModularityTests` 7/7 y nuevos tests de filtro/paginación); `ng build` verde. `docs/PROMPT_MAPPING.md` v2.12→v2.13 (44 contratos); `docs/product/DTP.md` v1.13→v1.14; `docs/design/DD-UC-007.md` v1.0. `FSD-UC-011`/`FSD-UC-021` permanecen **completos** (mejora no funcional sobre ambos, no cambia su estado). Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.29 | 20/08/2026 | Rodrigo Aspeti | **Octavo Design Doc + ejecución real de `PR-IMPL-008` + sync documental (`dtp-sync`)**: primer feature de negocio real del módulo `academico` (hasta ahora vacío, solo `package-info.java` desde `PR-IMPL-001`) — `GestionEscolar` (Aggregate Root inmutable con factory `crear()` y mutador controlado `cambiarEstado()`, mismo patrón que `Tenant`), `POST/GET /gestiones-escolares` (alta, listado con filtros `q`/`estado` y paginación reutilizando `shared.{PageQuery,PageResult,web.PageResponse}` de `DD-UC-007` sin modificarlos) y `PATCH /gestiones-escolares/{id}/estado` (ciclo `PLANIFICACION`→`ACTIVA`→`CERRADA`, con reapertura `ACTIVA`→`PLANIFICACION` permitida en este slice). Decisiones explícitas del usuario (confirmadas vía preguntas estructuradas, 20/08/2026): (1) backend completo con `PATCH estado`, sin exigir periodos/secciones configurados para activar (el FSD §4.6.2 paso 3 no lo declara como excepción bloqueante, solo como secuencia deseable); (2) backend-only, UI Angular diferida a un Design Doc de seguimiento (mismo patrón `DD-UC-005`→`DD-UC-006`); (3) sin `audit_log` todavía — misma postura mínima de aislamiento que `Tenant`/`Usuario` (RLS `FORCE` + filtro explícito por `tenant_id` + RBAC `ADMIN`), gobernanza formal de los módulos nuevos sigue **pendiente de definición** (`ADR-0009` §3 punto 5). Migración `V5__academico_gestion_escolar.sql` (tabla con `tenant_id NOT NULL`, sin la excepción `OR tenant_id IS NULL` de `usuario`). 21 tests nuevos (8 dominio, 7 servicios con Mockito, 6 integración con Testcontainers, incluido aislamiento cross-tenant `404`). Verificación: `mvn test` → **119/119** verde (incluye `ModularityTests` 7/7, confirmando que `academico` no depende de `identidad`/`plataforma` fuera de `shared`). `docs/PROMPT_MAPPING.md` v2.13→v2.15 (45 contratos); `docs/product/DTP.md` v1.14→v1.16; `docs/design/DD-UC-008.md` v1.0→v1.1 (DoD 100%). `FSD-UC-012` queda **completo (backend)**; UI Angular → futuro Design Doc. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.30 | 20/08/2026 | Rodrigo Aspeti | **Noveno Design Doc + ejecución real de `PR-IMPL-009` + sync documental (`dtp-sync`)**, diseñados y ejecutados en el mismo turno: primer *vertical slice* de UI del módulo `academico` — consola Angular de Gestión Escolar (`features/academico/gestion-escolar.model.ts`, `gestiones-escolares-list.page.ts` con filtros `q`/`estado`, paginación y diálogo de cambio de estado, `gestion-escolar-create.page.ts`), cerrando `FSD-UC-012` en la capa de presentación sin tocar backend (`DD-UC-008` ya expone todos los contratos). Decisión de diseño explícita: el diálogo de cambio de estado calcula client-side las transiciones válidas del estado actual (`transicionesValidas(estadoActual)`) — oculta el botón "Cambiar estado" sobre `CERRADA` — a diferencia del diálogo genérico de `Tenant` (`DD-UC-004`), que ofrece los 3 estados siempre porque cualquier transición es válida allí; ruta con prefijo de módulo `/academico/gestiones-escolares`. Delta menor agregado durante la ejecución, no listado explícitamente en el diseño original: `shell.component.ts` gana el enlace "Gestión Escolar" junto a "Usuarios" (mismo condicional `hasRole('ADMIN')`). Verificación: `ng build` → verde, 2 lazy chunks nuevos (`gestiones-escolares-list-page`, `gestion-escolar-create-page`); `ng test` no ejecutable en este entorno (Vitest sin paquete de browser instalado, misma limitación documentada en `DD-UC-004`/`006`/`007`). `docs/PROMPT_MAPPING.md` v2.16→v2.17 (46 contratos, sin filas nuevas); `docs/product/DTP.md` v1.17→v1.18; `docs/design/DD-UC-009.md` v1.0→v1.1 (DoD 100%). `FSD-UC-012` cierra su implementación **completa** (backend + UI) — segundo `FSD-UC` en cerrar ambas capas, después de `FSD-UC-021`. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.31 | 20/08/2026 | Rodrigo Aspeti | **Décimo Design Doc + ejecución real de `PR-IMPL-010` + sync documental (`dtp-sync`)**, diseñados y ejecutados en turnos consecutivos del mismo día: segundo feature de negocio real del módulo `academico`, después de `GestionEscolar` — `Curso` y `Paralelo` (Aggregates independientes sin estado ni ciclo de vida, cada uno con su propio repositorio; decisión explícita para que `Materia`/`Inscripcion`/`Usuario.curso_asignado_id`, en Design Docs futuros, referencien un `Paralelo` por id sin cargar el `Curso` padre completo), `POST/GET /api/v1/cursos` (alta, listado con filtro `q` y paginación reutilizando `shared.{PageQuery,PageResult,web.PageResponse}` de `DD-UC-007`) y `POST/GET /api/v1/cursos/{id}/paralelos` (alta, listado simple sin paginar — cardinalidad acotada; valida que el `Curso` padre exista y sea del tenant actual antes de operar, `404 E_CURSO_NO_ENCONTRADO` si no). Refinamiento respecto al plan original: los DTOs REST se crearon directamente en `academico.infrastructure.adapter.in.rest`, sin el subpaquete `dto/` esbozado en el diseño, replicando el precedente real de `DD-UC-008`; dominio con Lombok solo `@Getter` (sin `@EqualsAndHashCode`/`@ToString`), más estricto que el *allowlist* completo de `ADR-0012`, mismo criterio que `GestionEscolar`. Migración `V6__academico_curso_paralelo.sql` (dos tablas con `tenant_id NOT NULL` + RLS `FORCE` cada una; `paralelo.tenant_id` redundante por diseño, derivable por join contra `curso`, para mantener RLS directa por tabla). 15 tests nuevos (3 dominio, 7 servicios Mockito, 5 integración Testcontainers con `CursoIntegrationTest`, incluida validación del curso padre y aislamiento cross-tenant 404 en ambos recursos). Verificación: `mvn test` → **134/134** verde (incluye `ModularityTests` 7/7). `docs/PROMPT_MAPPING.md` v2.17→v2.19 (47 contratos); `docs/product/DTP.md` v1.18→v1.20; `docs/design/DD-UC-010.md` v1.0→v1.1 (DoD 100%); `docs/prompts/impl/PR-IMPL-010.md` v0.1→v0.2 (estado "Ejecutado"). `FSD-UC-017` queda **completo (backend)**; UI Angular → futuro Design Doc (mismo patrón backend-primero de `DD-UC-008`→`DD-UC-009`). Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.32 | 21/08/2026 | Rodrigo Aspeti | **Undécimo Design Doc + sync documental (`dtp-sync`)** — diseño aprobado, ejecución pendiente: segundo *vertical slice* de UI del módulo `academico`, después de Gestión Escolar (`DD-UC-009`) — consola Angular de Cursos y Paralelos (`docs/design/DD-UC-011.md`): lista de Cursos con filtro `q`/paginación reutilizando `DD-UC-007`, alta de Curso, y una vista de detalle nueva (primera pantalla de "detalle" del proyecto, hasta ahora solo lista+alta) con los Paralelos de un Curso y alta inline de Paralelo. Decisiones explícitas de diseño: la vista de Paralelos es una pantalla/ruta propia (`/academico/cursos/:id/paralelos`), no un acordeón en la lista de Cursos, para no cargar los Paralelos de todos los Cursos de la página a la vez; el alta de Paralelo es un formulario inline en esa misma pantalla, sin ruta `/nuevo` separada, porque un `Paralelo` siempre se crea en el contexto de un `Curso` ya visible; sin `<select>` de filtro por estado en la lista de Cursos (`Curso` no tiene estado, `DD-UC-010` §2); sin edición/eliminación (el backend de `DD-UC-010` no expone `PATCH`/`DELETE`). `docs/prompts/impl/PR-IMPL-011.md` v0.1 materializado (estado "Aprobado (prompt)"). `docs/PROMPT_MAPPING.md` v2.19→v2.20 (48 contratos); `docs/product/DTP.md` v1.20→v1.21. `FSD-UC-017` gana `DD-UC-011` en su Design Doc de UI; permanece **completo (backend)**, UI diseñada con ejecución pendiente. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Ejecución de `PR-IMPL-011` (código real) pendiente. |
| v0.33 | 21/08/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-011` + sync documental (`dtp-sync`)**, en turnos consecutivos del mismo día que su diseño: código Angular real generado — `curso.model.ts`, `cursos-list.page.ts` (lista con filtro `q`/paginación), `curso-create.page.ts` (alta), `curso-paralelos.page.ts` (detalle de un Curso con sus Paralelos y alta inline); delta en `app.routes.ts` (`/academico/cursos[, /nuevo, /:id/paralelos]`) y `shell.component.ts` (enlace "Cursos"). Refinamiento encontrado durante la ejecución, documentado en `DD-UC-011` §2/§8: el backend (`DD-UC-010`) no expone `GET /cursos/{id}`, por lo que el nombre del Curso se propaga como *query param* desde `CursosListPage` hacia `CursoParalelosPage`, sin delta de backend — el `404 E_CURSO_NO_ENCONTRADO` de `GET /cursos/{id}/paralelos` sigue siendo la única validación real de existencia/pertenencia al tenant. `ng build` → verde (3 lazy chunks nuevos: `cursos-list-page`, `curso-create-page`, `curso-paralelos-page`). `docs/PROMPT_MAPPING.md` v2.20→v2.21; `docs/product/DTP.md` v1.21→v1.22; `docs/design/DD-UC-011.md` v1.0→v1.1 (DoD 100%); `docs/prompts/impl/PR-IMPL-011.md` v0.1→v0.2 (estado "Ejecutado"). `FSD-UC-017` cierra su implementación **completa** (backend + UI) — tercer `FSD-UC` en cerrar ambas capas, después de `FSD-UC-021` y `FSD-UC-012`. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.34 | 21/08/2026 | Rodrigo Aspeti | **Sincronización de cadena documental (`sync-doc-chain`)** tras el cierre de `FSD-UC-017`: `docs/product/FSD.md` v2.5→v2.6 documenta `GET /cursos`, `GET /cursos/{id}/paralelos`, A1 `E_CURSO_NO_ENCONTRADO` y trazabilidad `DD-UC-010`/`DD-UC-011`. `docs/product/DTP.md` v1.22→v1.23; `docs/PROMPT_MAPPING.md` v2.22→v2.23. BRD/PRD/ADR/diagramas/baseline revisados sin cambio (sin nuevo BR, US, NFR ni DA). Sin ADR nuevo. |
| v0.35 | 21/08/2026 | Rodrigo Aspeti | **Duodécimo Design Doc + `PR-IMPL-012`** — diseño aprobado, ejecución pendiente: primer *vertical slice* fullstack del módulo `academico` (`docs/design/DD-UC-012.md`) — `Materia` + asignaciones a `Curso`/`Paralelo` y a `Profesor` (`FSD-UC-018`). Decisiones explícitas: tres Aggregates independientes (no FKs embebidas en `Materia`); `ProfesorConsultaPort` en la raíz de `academico` implementado por `identidad` (Open Host Service, espejo de `TenantConsultaPort`); A1 `409 E_MATERIA_SIN_CURSO`; `GET /materias/{id}` desde el día 1; RBAC `ADMIN`+`SECRETARIA`; consola Angular lista/alta/detalle con asignaciones inline. `docs/prompts/impl/PR-IMPL-012.md` v0.1 (estado "Aprobado (prompt)"). `docs/PROMPT_MAPPING.md` v2.23→v2.24 (49 contratos); `docs/product/DTP.md` v1.23→v1.24. Sin ADR nuevo ni cambios en `docs/baseline/`. Ejecución de `PR-IMPL-012` (código real) pendiente. |
| v0.36 | 21/08/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-012` + sync documental (`dtp-sync`)**: código fullstack de Materias — backend `Materia`/`AsignacionMateriaCurso`/`AsignacionMateriaProfesor`, `ProfesorConsultaPort` (raíz de `academico`, impl en `identidad`, arista `identidad → academico`), `MateriaController` (`POST/GET /materias`, `GET /materias/{id}`, asignaciones curso/profesor, catálogo `GET /materias/profesores-disponibles`), `V7__academico_materia.sql` (RLS `FORCE`); delta `CursoController` GET también `SECRETARIA`. UI Angular `features/academico/{materia.model,materias-list,materia-create,materia-detalle}.page.ts`; `role.guard.ts` acepta `data.roles` aditivo; shell enlace "Materias" para ADMIN o SECRETARIA. A1 `409 E_MATERIA_SIN_CURSO`. Verificación: `mvn test` → **154/154** verde (incluye `ModularityTests` 7/7); `ng build` verde (3 lazy chunks: `materias-list-page`, `materia-create-page`, `materia-detalle-page`). `docs/product/FSD.md` v2.6→v2.7; `docs/PROMPT_MAPPING.md` v2.24→v2.25; `docs/product/DTP.md` v1.24→v1.25; `docs/design/DD-UC-012.md` v1.0→v1.1 (DoD 100%); `docs/prompts/impl/PR-IMPL-012.md` v0.1→v0.2 (estado "Ejecutado"). `FSD-UC-018` cierra su implementación **completa** (backend + UI) — cuarto `FSD-UC` en cerrar ambas capas, después de `FSD-UC-021`, `FSD-UC-012` y `FSD-UC-017`. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.37 | 21/08/2026 | Rodrigo Aspeti | **Decimotercer Design Doc + `PR-IMPL-013`** — diseño aprobado, ejecución pendiente: segundo *vertical slice* fullstack del módulo `academico` (`docs/design/DD-UC-013.md`) — `Estudiante` + `Inscripcion` (`FSD-UC-020`). Decisiones explícitas: dos Aggregates independientes (`BR-023`); `rude` obligatorio único por tenant (`BR-004`/`RB-01`, no espera `ADR-0009` §3 punto 1); A1 `409 E_INSCRIPCION_DUPLICADA`; `GET /estudiantes/{id}` desde el día 1; RBAC `ADMIN`+`SECRETARIA`; delta GET de Gestiones Escolares para `SECRETARIA`; consola Angular lista/alta/detalle con inscripciones inline. `docs/prompts/impl/PR-IMPL-013.md` v0.1 (estado "Aprobado (prompt)"). `docs/PROMPT_MAPPING.md` v2.25→v2.26 (50 contratos); `docs/product/DTP.md` v1.25→v1.26. Sin ADR nuevo ni cambios en `docs/baseline/`. Ejecución de `PR-IMPL-013` (código real) pendiente. |
| v0.38 | 21/08/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-013` + sync documental (`dtp-sync`)**: código fullstack de Estudiantes e Inscripciones — backend `Estudiante`/`Inscripcion`, `EstudianteController`/`InscripcionController` (`POST/GET /estudiantes`, `GET /{id}`, historial, `POST /inscripciones`), `V8__academico_estudiante_inscripcion.sql` (RLS `FORCE`); delta GET de `GestionEscolarController` también `SECRETARIA`. UI Angular `features/academico/{estudiante.model,estudiantes-list,estudiante-create,estudiante-detalle}.page.ts`; shell enlace "Estudiantes" para ADMIN o SECRETARIA. A1 `409 E_INSCRIPCION_DUPLICADA`; `409 E_RUDE_DUPLICADO` sin interpolar el código. Verificación: `mvn test` → **173/173** verde (incluye `ModularityTests` 7/7); `ng build` verde (3 lazy chunks: `estudiantes-list-page`, `estudiante-create-page`, `estudiante-detalle-page`). `docs/product/FSD.md` v2.7→v2.8; `docs/PROMPT_MAPPING.md` v2.26→v2.27; `docs/product/DTP.md` v1.26→v1.27; `docs/design/DD-UC-013.md` v1.0→v1.1 (DoD 100%); `docs/prompts/impl/PR-IMPL-013.md` v0.1→v0.2 (estado "Ejecutado"). `FSD-UC-020` cierra su implementación **completa** (backend + UI) — quinto `FSD-UC` en cerrar ambas capas, después de `FSD-UC-021`, `FSD-UC-012`, `FSD-UC-017` y `FSD-UC-018`. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.39 | 21/08/2026 | Rodrigo Aspeti | **Decimocuarto Design Doc + `PR-IMPL-014`** — diseño aprobado, ejecución pendiente: tercer *vertical slice* fullstack del módulo `academico` (`docs/design/DD-UC-014.md`) — consola de Profesores, consulta inversa de asignaciones (`FSD-UC-019`). Decisiones explícitas: sin entidad/tabla `Profesor` (perfil de `Usuario`); extensión de `ProfesorConsultaPort`; `GET /profesores` + `GET /{id}` + `GET /{id}/asignaciones`; alta permanece en `FSD-UC-021`; escrituras de asignación en `FSD-UC-018`; RBAC `ADMIN`+`SECRETARIA`; consola Angular lista/detalle de solo lectura. `docs/prompts/impl/PR-IMPL-014.md` v0.1 (estado "Aprobado (prompt)"). `docs/PROMPT_MAPPING.md` v2.27→v2.28 (51 contratos); `docs/product/DTP.md` v1.27→v1.28. Sin ADR nuevo ni cambios en `docs/baseline/`. Ejecución de `PR-IMPL-014` (código real) pendiente. |
| v0.40 | 21/08/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-014` + sync documental (`dtp-sync`)**: código fullstack de Profesores — extensión de `ProfesorConsultaPort` (`buscarPorIdYTenant`, `listarDelTenant`), `ProfesorController` (`GET /profesores`, `GET /{id}`, `GET /{id}/asignaciones` enriquecido), sin tabla ni Flyway. UI Angular `features/academico/{profesor.model,profesores-list,profesor-detalle}.page.ts`; shell enlace "Profesores" para ADMIN o SECRETARIA. A1 `404 E_PROFESOR_NO_ENCONTRADO`. Verificación: `mvn test` → **184/184** verde (incluye `ModularityTests` 7/7); `ng build` verde (2 lazy chunks: `profesores-list-page`, `profesor-detalle-page`). `docs/product/FSD.md` v2.8→v2.9; `docs/PROMPT_MAPPING.md` v2.28→v2.29; `docs/product/DTP.md` v1.28→v1.29; `docs/design/DD-UC-014.md` v1.0→v1.1 (DoD 100%); `docs/prompts/impl/PR-IMPL-014.md` v0.1→v0.2 (estado "Ejecutado"). `FSD-UC-019` cierra su implementación **completa** (backend + UI) — sexto `FSD-UC` en cerrar ambas capas. Árbol §3 y tabla §1 actualizados. Sin ADR nuevo ni cambios en `docs/baseline/`. Commit formal pendiente. |
| v0.41 | 21/08/2026 | Rodrigo Aspeti | **`ADR-0013`** (modelo genérico de periodos, secciones y cálculo de notas): resuelve `ADR-0009` §3 puntos 1–4. `docs/product/BRD.md` v3.1→v3.2, `PRD.md` v2.2→v2.3, `FSD.md` v2.9→v2.10, `DTP.md` v1.29→v1.30, `PROMPT_MAPPING.md` v2.29→v2.30 (`PR-ADR-006`). Dualidad de cálculo: genérico = `round` HALF_UP; SIE = `floor()` (`§6`). `FSD-UC-013`..`016` desbloqueados para diseño. Punto 5 (gobernanza) sigue pendiente. Sin código. Baseline intacto. |
| v0.42 | 21/08/2026 | Rodrigo Aspeti | **Decimoquinto Design Doc + `PR-IMPL-015`** — diseño aprobado, ejecución pendiente: cuarto *vertical slice* fullstack de `academico` (`docs/design/DD-UC-015.md`) — Periodos de Evaluación (`FSD-UC-013`). Decisiones explícitas: Aggregate `PeriodoEvaluacion` independiente con `orden`; seed 3 trimestres al crear gestión; freeze de N/datos si hay un `ABIERTO`; `GET /gestiones-escolares/{id}`; consola detalle anidada. `docs/prompts/impl/PR-IMPL-015.md` v0.1. `docs/PROMPT_MAPPING.md` v2.30→v2.31 (53 contratos); `docs/product/DTP.md` v1.30→v1.31. Sin ADR nuevo ni código. Baseline intacto. |
| v0.43 | 21/08/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-015` + sync documental (`dtp-sync`)**: código fullstack de Periodos de Evaluación — Aggregate `PeriodoEvaluacion`, seed 3 trimestres en `CrearGestionEscolarService`, `GET /gestiones-escolares/{id}`, `POST/GET .../periodos`, `PATCH`/`DELETE` + `PATCH .../estado`, `V9__academico_periodo_evaluacion.sql`. UI `gestion-periodos.page.ts` + enlace Periodos en la lista de gestiones. Verificación: `mvn test` → **200/200** verde (incluye `ModularityTests` 7/7); `ng build` verde. `docs/product/FSD.md` v2.10→v2.11; `docs/PROMPT_MAPPING.md` v2.31→v2.32; `docs/product/DTP.md` v1.31→v1.32. `FSD-UC-013` cierra implementación **completa** (backend + UI) — séptimo `FSD-UC` en cerrar ambas capas. Sin ADR nuevo. Baseline intacto. |
| v0.44 | 21/08/2026 | Rodrigo Aspeti | **Decimosexto Design Doc + `PR-IMPL-016`** — diseño aprobado, ejecución pendiente: quinto *vertical slice* fullstack de `academico` (`docs/design/DD-UC-016.md`) — Secciones de Evaluación (`FSD-UC-014`). Decisiones explícitas: Aggregate `SeccionEvaluacion` independiente a nivel de gestión; seed 4 secciones al crear gestión; PUT atómico (Σ `nota` = 100); freeze sticky (ABIERTO o CERRADO); consola detalle anidada. `docs/prompts/impl/PR-IMPL-016.md` v0.1. `docs/PROMPT_MAPPING.md` v2.32→v2.33 (54 contratos); `docs/product/DTP.md` v1.32→v1.33. Sin ADR nuevo ni código. Baseline intacto. |
| v0.45 | 21/08/2026 | Rodrigo Aspeti | **Ejecución real de `PR-IMPL-016` + sync documental (`dtp-sync`)**: código fullstack de Secciones de Evaluación — Aggregate `SeccionEvaluacion`, seed 4 secciones en `CrearGestionEscolarService`, `GET/PUT/POST .../secciones`, `PATCH /secciones-evaluacion/{id}`, freeze sticky, `V10__academico_seccion_evaluacion.sql`. UI `gestion-secciones.page.ts` + enlace Secciones en la lista de gestiones. Verificación: `mvn test` → **215/215** verde (incluye `ModularityTests` 7/7); `ng build` verde. `docs/product/FSD.md` v2.11→v2.12; `docs/PROMPT_MAPPING.md` v2.33→v2.34; `docs/product/DTP.md` v1.33→v1.34. `FSD-UC-014` cierra implementación **completa** (backend + UI) — octavo `FSD-UC` en cerrar ambas capas. Sin ADR nuevo. Baseline intacto. |

---

## Checklist de validez

- [x] Sincronizado con `docs/arquitectura_funcional_EduSync.md` (10 UCs + 5 DAs).
- [x] Sincronizado con `docs/fsd/FSD_EduSync.md` v1.0 (FSD-UC-001, UC-003, UC-004, UC-005, UC-009).
- [x] Sincronizado con `docs/LFSD-EduSync.md` v1.0.1 (arquitectura hexagonal, DDL, APIs; ruta FSD normalizada).
- [x] Sincronizado con `docs/PROMPT_MAPPING.md` v2.12 (43 prompt-contratos + carpeta `prompts/`; área `IMPL` con 6 filas `PR-IMPL-001..006`, las seis en `docs/prompts/impl/` y las seis **ejecutadas**).
- [x] Sincronizado con `docs/baseline/DTI.md` v0.8 (congelado, tag `release/2.0.0`) — referencia atómica AGENTS ↔ DTI; C4 L1/L2/L3 + Deployment AWS canónicos en `docs/diagrams/`; `docs/roadmap.md` v0.3 declarado fuente canónica del horizonte (espejo histórico en DTI §19, espejo vivo en `docs/product/DTP.md` §B).
- [x] Todos los paths de archivos verificados contra la estructura real del repositorio.
- [x] Sin secretos en texto plano.
- [x] Stack y versiones actualizados — baseline M4: PostgreSQL 15, Spring Boot 3.3, Java 21, Angular 17; vivo desde `release/3.0.0`: PostgreSQL 15, Spring Boot 4.1.0, Java 25 LTS, Angular 21 LTS (`ADR-0008`).
- [x] 6 agentes documentados con sus límites estrictos.
- [x] 4 golden tests obligatorios de zero-tolerance definidos.
- [x] Guardrails probados con lista de prompts prohibidos.
- [x] `docs/adr/0001..0006-*.md` + `0008-*.md` + `0009-*.md` + `0010-*.md` + `0011-*.md` + `0012-*.md` + `0013-*.md` creados — 12 ADRs aprobados. `ADR-0007` (Strangler Fig) queda *gated*, sin crear.
- [x] `docs/diagrams/c4_level1.mmd` y `c4_level2.mmd` creados (Contexto + Contenedores).
- [x] Skills activos: 13 en `.cursor/skills/` y 13 en `.claude/skills/` (paridad completa, 19/07/2026). Los canónicos restantes de `plantillas/plantillas2/` siguen como plantillas fuente.
- [x] `docs/diagrams/c4_level3_*.mmd` + `deployment_aws.mmd` creados — `api-gateway` (PR-C4-003), `domain-layer` (PR-C4-004), `sie-adapter` (PR-C4-005) y Deployment AWS (PR-C4-006).
- [x] `docs/roadmap.md` v0.3 creado y actualizado (PR-ROADMAP-001) — hoja de ruta canónica con 4 horizontes + apertura de capa viva; espejo histórico en `docs/baseline/DTI.md` §19, espejo vivo en `docs/product/DTP.md`.
- [x] `docs/baseline/` protegido — 5 archivos marcados `status: congelado`, `CODEOWNERS`, `.cursor/rules/baseline-congelado.mdc` y el hook `.cursor/hooks.json` → `protect-baseline.js` (enforcement real, no solo advisory) creados y probados manualmente.
- [x] `.cursor/hooks.json` creado con 3 scripts Node.js probados: `protect-baseline.js` (bloqueo), `warn-shell-baseline.js` (confirmación en shell) y `dtp-sync-reminder.js` (recordatorio `@dtp-sync` al cierre de turno).
- [x] `docs/product/DTP.md` v1.0 creado como punto de partida de la capa viva (`release/3.0.0`), con el delta de stack `ADR-0008` registrado en §A.2.
- [x] `docs/adr/0009-*.md` creado y `docs/product/{BRD,PRD,FSD}.md` + `DTP.md` v1.1 actualizados con la generalización del modelo de dominio a plataforma SaaS multi-tenant configurable, como extensión aditiva sobre el Perfil Bolivia SIE (BR-001..BR-012/RB-01..RB-11 y FSD-UC-001..009 vigentes sin cambios).
- [x] `docs/adr/0010-*.md` creado y `docs/product/{BRD,PRD,FSD}.md` + `DTP.md` v1.2 actualizados con el modelo multi-rol de usuario (`UsuarioRol` N:M) y la invariante permanente `tenant_id IS NULL ⟺ roles = {SYSADMIN}`, refinando `BR-024`/`FSD-UC-021` sin contradecir `ADR-0009`.
- [x] Resolución de los puntos 1–4 de `ADR-0009` §3 vía `ADR-0013` (21/08/2026): modelo genérico único, secuencialidad, `round` genérico, suma de secciones = 100. **Sigue pendiente** el punto 5 (gobernanza de módulos nuevos). `FSD-UC-013` y `FSD-UC-014` **completos** (backend+UI); `FSD-UC-015`/`016` desbloqueados para diseño (código aún no).
- [ ] `pocs/POC-NN/` pendiente — evidencia ejecutiva de POC-01 (RLS) y POC-02 (Circuit Breaker SIE) bloqueando promoción a `release/1.1.0`.
- [x] Primer `docs/design/DD-UC-001.md` (bootstrap del proyecto, `FSD-UC-011`/`FSD-UC-021`, crea `ADR-0011`) y primer `docs/prompts/impl/PR-IMPL-001.md` creados y aprobados.
- [x] Segundo `docs/design/DD-UC-002.md` (módulo `identidad`: login JWT, seed `SYSADMIN`, `TenantContextProvider` real, `FSD-UC-021` parcial) y segundo `docs/prompts/impl/PR-IMPL-002.md` creados y aprobados.
- [x] Tercer `docs/design/DD-UC-003.md` (módulo `plataforma`: alta y gestión de Tenants, scheduler de vencimiento, `TenantConsultaPort`, `FSD-UC-011` completo) y tercer `docs/prompts/impl/PR-IMPL-003.md` creados y aprobados.
- [x] Ejecución de `PR-IMPL-001` (18/07/2026): esqueleto real generado en `backend/`, `frontend/`, `infra/docker-compose.yml`; `mvn test` → `ModularityTests` 7/7 verde; `ng build` sin errores. `src/` ya no está vacío.
- [x] Ejecución de `PR-IMPL-002` (18-19/07/2026): módulo `identidad` con lógica de dominio real (login JWT, `Usuario`/`UsuarioRol`, `TenantContextProvider`); `ADR-0012` (Lombok/springdoc-openapi/Bean Validation) aplicado sobre el mismo módulo; `mvn test` → 27/27 verde (incluye `ModularityTests` 7/7); smoke test manual de Swagger UI/OpenAPI verde.
- [x] Ejecución de `PR-IMPL-003` (19/07/2026): módulo `plataforma` con lógica de dominio real (`Tenant`, ciclo de suscripción, scheduler de vencimiento, `TenantConsultaPort`) y enforcement de `BR-014` en `identidad`; `FSD-UC-011` API **completa**; `mvn test` (clean) → 45/45 verde en ese momento (incluye `ModularityTests` 7/7).
- [x] Cuarto Design Doc `DD-UC-004` + `PR-IMPL-004` (19/07/2026): UI login + consola SysAdmin (`sessionStorage`, `GET /tenants`) — **aprobados**.
- [x] Ejecución de `PR-IMPL-004` (19/07/2026): UI Angular real + delta `GET /api/v1/plataforma/tenants`; ajuste `SecurityConfig` (401/403); `mvn test` → **50/50** verde; `ng build` verde. `FSD-UC-011` queda **completo** (API+UI); login UI de `FSD-UC-021` cerrado. Docs sincronizados (`DTP` v1.11, `PROMPT_MAPPING` v2.8). Commit formal pendiente.
- [x] `docs/adr/0012-*.md` creado — Lombok (allowlist `domain/`), springdoc-openapi y Bean Validation aplicados retroactivamente sobre `identidad` (`PR-IMPL-002`).
- [x] Quinto Design Doc `DD-UC-005` + `PR-IMPL-005` (04/08/2026): CRUD backend de Usuarios y Roles — **aprobados y ejecutados en el mismo turno**.
- [x] Ejecución de `PR-IMPL-005` (04/08/2026): CRUD real (`POST/GET/PATCH /usuarios`, restablecimiento de contraseña); corregido un bug de merge JPA en `UsuarioRepositoryAdapter.guardar()` expuesto por el primer caso de actualización sobre un usuario ya persistido; `mvn test` → **72/72** verde (incluye `ModularityTests` 7/7). `FSD-UC-021` queda **completo** en backend; UI del CRUD → futuro `DD-UC-006`. Docs sincronizados (`DTP` v1.12, `PROMPT_MAPPING` v2.10). **Commit real creado**: `3ca1626 Gestión de Usuarios y Roles`.
- [x] Sexto Design Doc `DD-UC-006` + `PR-IMPL-006` (04/08/2026): consola Angular de Usuarios y Roles — **aprobados y ejecutados en el mismo turno**.
- [x] Ejecución de `PR-IMPL-006` (04/08/2026): consola real (`features/usuarios/**`, `features/auth/reset-password-confirm/**`), sin delta de backend; nav condicional por rol en `shell.component.ts`; redirect `ADMIN` → `/usuarios`; `ng build` en verde. `FSD-UC-021` queda **completo** en backend + UI — primer `FSD-UC` en cerrar ambas capas. Docs sincronizados (`DTP` v1.13, `PROMPT_MAPPING` v2.12). Commit formal pendiente.
- [x] Séptimo Design Doc `DD-UC-007` + `PR-IMPL-007` (20/08/2026): filtros y paginación reutilizables en Usuarios y Tenants — **aprobados y ejecutados en el mismo turno**.
- [x] Ejecución de `PR-IMPL-007` (20/08/2026): patrón `shared.{PageQuery,PageResult,web.PageResponse}` real; `GET /usuarios` (`q` nombre-o-email, `activo`, `rol`) y `GET /plataforma/tenants` (`q` nombre, `estado`), ambos con `page`/`size`; primer uso de `Specification`/`JpaSpecificationExecutor` del proyecto; `UsuarioRepositoryPort.listarPorTenant(UUID)` conservado intacto (lo consume `shared.ai`); UI Angular de ambas listas con filtros y paginador. `mvn test` → **98/98** verde (incluye `ModularityTests` 7/7); `ng build` en verde. `FSD-UC-011`/`FSD-UC-021` permanecen **completos** (mejora no funcional). Docs sincronizados (`DTP` v1.14, `PROMPT_MAPPING` v2.13). Commit formal pendiente.
- [x] Octavo Design Doc `DD-UC-008` + `PR-IMPL-008` (20/08/2026): módulo `academico` — `GestionEscolar` (alta, listado con filtros/paginación, ciclo de estado) — **aprobados y ejecutados en el mismo turno**.
- [x] Ejecución de `PR-IMPL-008` (20/08/2026): primer feature de negocio real del módulo `academico` (hasta ahora solo `package-info.java`): `GestionEscolar` (Aggregate Root con factory `crear()` y mutador controlado `cambiarEstado()`), `application`/`infrastructure` completos (`GestionEscolarController` con `POST/GET/PATCH .../estado`, `@PreAuthorize("hasRole('ADMIN')")`), `V5__academico_gestion_escolar.sql` (RLS `FORCE`, `tenant_id` obligatorio sin excepción `SYSADMIN`). 21 tests nuevos (dominio, servicios Mockito, integración Testcontainers con aislamiento cross-tenant 404). `mvn test` → **119/119** verde (incluye `ModularityTests` 7/7). `FSD-UC-012` queda **completo (backend)**; UI Angular → futuro Design Doc. Docs sincronizados (`DTP` v1.16, `PROMPT_MAPPING` v2.15). Commit formal pendiente.
- [x] Noveno Design Doc `DD-UC-009` + `PR-IMPL-009` (20/08/2026): consola Angular de Gestión Escolar (lista con filtros/paginación, alta, cambio de estado) — **aprobados y ejecutados en el mismo turno**.
- [x] Ejecución de `PR-IMPL-009` (20/08/2026): primer *vertical slice* de UI del módulo `academico`: `features/academico/{gestion-escolar.model,gestiones-escolares-list.page,gestion-escolar-create.page}.ts`; diálogo de cambio de estado calcula client-side `transicionesValidas(estadoActual)` (oculta el botón sobre `CERRADA`), a diferencia del diálogo genérico de `Tenant`; delta `app.routes.ts` (`/academico/gestiones-escolares[, /nuevo]`) y `shell.component.ts` (enlace "Gestión Escolar"). Sin delta de backend. `ng build` → verde (2 lazy chunks nuevos). `FSD-UC-012` queda **completo** (backend + UI) — segundo `FSD-UC` en cerrar ambas capas, después de `FSD-UC-021`. Docs sincronizados (`DTP` v1.18, `PROMPT_MAPPING` v2.17). Commit formal pendiente.
- [x] Décimo Design Doc `DD-UC-010` + `PR-IMPL-010` (20/08/2026): módulo `academico` — `Curso` y `Paralelo` (alta y listado, sin ciclo de vida) — **aprobados y ejecutados en turnos consecutivos del mismo día**.
- [x] Ejecución de `PR-IMPL-010` (20/08/2026): segundo feature de negocio real del módulo `academico`, después de `GestionEscolar`: `Curso`/`Paralelo` como Aggregates independientes (sin estado), `CursoController` (`POST/GET /cursos`, `POST/GET /cursos/{id}/paralelos`), `CursoSpecifications` (filtro `q`), `V6__academico_curso_paralelo.sql` (RLS `FORCE` en ambas tablas, `tenant_id` obligatorio). `CrearParaleloService`/`ListarParalelosService` validan el `Curso` padre antes de operar (`404 E_CURSO_NO_ENCONTRADO` si no existe o es de otro tenant). 15 tests nuevos (dominio, servicios Mockito, integración Testcontainers con aislamiento cross-tenant 404). `mvn test` → **134/134** verde (incluye `ModularityTests` 7/7). `FSD-UC-017` queda **completo (backend)**; UI Angular → futuro Design Doc. Docs sincronizados (`DTP` v1.20, `PROMPT_MAPPING` v2.19). Commit formal pendiente.
- [x] Undécimo Design Doc `DD-UC-011` + `PR-IMPL-011` (21/08/2026): consola Angular de Cursos y Paralelos (lista de Cursos con filtro `q`/paginación, alta, vista de detalle con Paralelos y alta inline) — **aprobados y ejecutados en turnos consecutivos del mismo día**. Segundo *vertical slice* de UI del módulo `academico`, después de `DD-UC-009`. Decisiones explícitas: vista de Paralelos como pantalla/ruta propia (`/academico/cursos/:id/paralelos`), no un acordeón en la lista de Cursos; alta de Paralelo inline en esa misma pantalla, sin ruta `/nuevo` separada; sin `<select>` de filtro por estado en la lista de Cursos (`Curso` no tiene estado).
- [x] Ejecución de `PR-IMPL-011` (21/08/2026): tercer *vertical slice* de UI del módulo `academico` en cerrar backend+UI: `curso.model.ts`, `cursos-list.page.ts`, `curso-create.page.ts`, `curso-paralelos.page.ts` generados; delta en `app.routes.ts`/`shell.component.ts`. Refinamiento encontrado durante la ejecución: el backend no expone `GET /cursos/{id}`, por lo que el nombre del Curso se propaga como *query param* desde la lista hacia el detalle, sin delta de backend. `ng build` → verde (3 lazy chunks nuevos). `FSD-UC-017` queda **completo (backend + UI)** — tercer `FSD-UC` en cerrar ambas capas, después de `FSD-UC-021` y `FSD-UC-012`. Docs sincronizados (`DTP` v1.23, `PROMPT_MAPPING` v2.23, `FSD` v2.6). Commit formal pendiente.
- [x] Duodécimo Design Doc `DD-UC-012` + `PR-IMPL-012` (21/08/2026): módulo `academico` — `Materia` y asignaciones a Curso/Profesor (backend + UI fullstack en un solo DD) — **aprobados y ejecutados en el mismo día**. Primer *vertical slice* fullstack de `academico`, después del patrón backend-primero de `DD-UC-010`→`011`. Decisiones explícitas: tres Aggregates independientes; `ProfesorConsultaPort` en `academico` implementado por `identidad`; A1 `409 E_MATERIA_SIN_CURSO`; `GET /materias/{id}`; RBAC `ADMIN`+`SECRETARIA`.
- [x] Ejecución de `PR-IMPL-012` (21/08/2026): cuarto `FSD-UC` en cerrar backend+UI (`FSD-UC-018`): `Materia` + asignaciones, `ProfesorConsultaPortImpl` en `identidad`, `V7__academico_materia.sql`; UI `materias-list`/`materia-create`/`materia-detalle`; `role.guard` aditivo `data.roles`. `mvn test` → **154/154** verde (incluye `ModularityTests` 7/7); `ng build` verde (3 lazy chunks nuevos). Docs sincronizados (`DTP` v1.25, `PROMPT_MAPPING` v2.25, `FSD` v2.7). Commit formal pendiente.
- [x] Decimotercer Design Doc `DD-UC-013` + `PR-IMPL-013` (21/08/2026): módulo `academico` — `Estudiante` e `Inscripcion` (backend + UI fullstack en un solo DD) — **aprobados y ejecutados en el mismo día**. Segundo *vertical slice* fullstack de `academico`. Decisiones explícitas: dos Aggregates independientes; `rude` obligatorio único por tenant (`BR-004`); A1 `409 E_INSCRIPCION_DUPLICADA`; `GET /estudiantes/{id}`; RBAC `ADMIN`+`SECRETARIA`.
- [x] Ejecución de `PR-IMPL-013` (21/08/2026): quinto `FSD-UC` en cerrar backend+UI (`FSD-UC-020`): `Estudiante` + `Inscripcion`, `V8__academico_estudiante_inscripcion.sql`; UI `estudiantes-list`/`estudiante-create`/`estudiante-detalle`; delta GET Gestiones para `SECRETARIA`. `mvn test` → **173/173** verde (incluye `ModularityTests` 7/7); `ng build` verde (3 lazy chunks nuevos). Docs sincronizados (`DTP` v1.27, `PROMPT_MAPPING` v2.27, `FSD` v2.8). Commit formal pendiente.
- [x] Decimocuarto Design Doc `DD-UC-014` + `PR-IMPL-014` (21/08/2026): módulo `academico` — Profesores (consulta inversa de asignaciones, backend + UI fullstack en un solo DD) — **aprobados y ejecutados**. Tercer *vertical slice* fullstack de `academico`. Decisiones explícitas: sin entidad/tabla `Profesor`; extensión de `ProfesorConsultaPort`; `GET /profesores/{id}/asignaciones`; alta en `FSD-UC-021`; escrituras en `FSD-UC-018`.
- [x] Ejecución de `PR-IMPL-014` (21/08/2026): sexto `FSD-UC` en cerrar backend+UI (`FSD-UC-019`): extensión `ProfesorConsultaPort`, `ProfesorController`, UI `profesores-list`/`profesor-detalle`. `mvn test` → **184/184** verde (incluye `ModularityTests` 7/7); `ng build` verde (2 lazy chunks nuevos). Docs sincronizados (`DTP` v1.29, `PROMPT_MAPPING` v2.29, `FSD` v2.9). Commit formal pendiente.
- [x] Decimoquinto Design Doc `DD-UC-015` + `PR-IMPL-015` (21/08/2026): módulo `academico` — Periodos de Evaluación (backend + UI fullstack en un solo DD) — **aprobados y ejecutados**. Cuarto *vertical slice* fullstack de `academico`. Decisiones explícitas: Aggregate independiente con `orden`; seed 3 trimestres; freeze si hay un `ABIERTO`; `GET /gestiones-escolares/{id}`.
- [x] Ejecución de `PR-IMPL-015` (21/08/2026): séptimo `FSD-UC` en cerrar backend+UI (`FSD-UC-013`): `PeriodoEvaluacion`, seed, `V9`, UI `gestion-periodos.page`. `mvn test` → **200/200** verde (incluye `ModularityTests` 7/7); `ng build` verde. Docs sincronizados (`DTP` v1.32, `PROMPT_MAPPING` v2.32, `FSD` v2.11). Commit formal pendiente.
- [x] Decimosexto Design Doc `DD-UC-016` + `PR-IMPL-016` (21/08/2026): módulo `academico` — Secciones de Evaluación (backend + UI fullstack en un solo DD) — **aprobados y ejecutados**. Quinto *vertical slice* fullstack de `academico`. Decisiones explícitas: Aggregate independiente a nivel de gestión; seed 4 secciones; PUT atómico Σ=100; freeze sticky.
- [x] Ejecución de `PR-IMPL-016` (21/08/2026): octavo `FSD-UC` en cerrar backend+UI (`FSD-UC-014`): `SeccionEvaluacion`, seed 4, `V10`, UI `gestion-secciones.page`. `mvn test` → **215/215** verde (incluye `ModularityTests` 7/7); `ng build` verde. Docs sincronizados (`DTP` v1.34, `PROMPT_MAPPING` v2.34, `FSD` v2.12). Commit formal pendiente.
- [ ] `mvn checkstyle:check` en verde — **gap detectado durante `PR-IMPL-005`**: el `pom.xml` usa el ruleset `sun_checks.xml` por defecto (no uno acorde a Google Java Style, `AGENTS.md` §5) y falla con 1073 violaciones preexistentes en todo el backend (ninguna introducida por `PR-IMPL-005`); el linter nunca estuvo realmente en verde en este proyecto. Recomendado como tarea de seguimiento dedicada (reconfigurar el ruleset, no reformatear todo el código a ciegas).
- [ ] Diseño del tenant "demo" como funcionalidad de producto (`ADR-0010` §3, no bloqueante) — pendiente para un Design Doc de seguimiento, distinto de `DD-UC-003` (que resolvió el resto de `FSD-UC-011`).
- [ ] Limpieza de ~50 referencias históricas a `docs/DTI.md` (ya movido a `docs/baseline/DTI.md`) dispersas en `prompts/PR-*.md`, `plantillas2/`, y skills operativos (`sync-doc-chain`, `c4-edusync`, `adr-edusync`, `poc-runner-edusync`) — fuera de alcance de esta pasada, recomendado como tarea de seguimiento.
- [x] `docs/aportes/release-2.0.0.md` creado (PR-APORTES-001 v0.1 — 95 tareas auditables, factor 1.00 por caso degenerado n = 1; commit final pendiente del push de `release/2.0.0`).
- [x] `AGENTS.md` en la **raíz** del repo (rúbrica del Módulo 4 pide ubicación raíz) — completado en v0.9.
- [x] `.cursor/skills/` y `.claude/skills/` en **paridad completa** (13 skills, mismos slugs); entrada Claude Code vía `CLAUDE.md` + `.claude/rules/` + `.claude/agents/` (6 agentes).
- [x] Alias `_vFinal` creados para BRD/MRD/PRD/FSD (`PR-VFINAL-001`): `docs/brd/BRD_EduSync_vFinal.md`, `docs/mrd/MRD_EduSync_vFinal.md`, `docs/prd/PRD_EduSync_vFinal.md`, `docs/fsd/FSD_EduSync_vFinal.md`.
- [x] Bump del DTI a v0.6 + cierre del drift de `adrs_vigentes` (nombres reales `0001..0006-*.md` en frontmatter).
- [ ] Stack y versiones a verificar contra `pom.xml` cuando el proyecto de código sea inicializado.
- [ ] Revisado por al menos un humano del grupo antes de cada release.
