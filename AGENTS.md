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
| **PROMPT_MAPPING** | `docs/PROMPT_MAPPING.md` | Catálogo de prompt-contratos `PR-<AREA>-NNN` — v2.0 (37 contratos activos: PR-ARCH..PR-POC-002, PR-C4-003..006, PR-ROADMAP-001, PR-APORTES-001, PR-VFINAL-001; v2.0 = apertura del área `IMPL` para prompts de implementación de `release/3.0.0`, todavía sin filas) |
| **APORTES** | `docs/APORTES_EduSync.md` | Informe de aportes individuales — release 1.0.0 |
| **APORTES release/2.0.0** | `docs/aportes/release-2.0.0.md` | Informe de aportes individuales del release de defensa — grupo unipersonal (n = 1), 95 tareas auditables, factor 1.00, generado por `PR-APORTES-001` |
| **Roadmap** | `docs/roadmap.md` | Hoja de ruta técnica y de negocio v0.3 — 4 horizontes (`release/1.0.1` → `release/1.1.0`/`release/3.0.0` → `release/1.2.0` → `release/2.0.0`), Gantt, 9 lecciones, métricas BRD/NFR, riesgos y compromisos; fuente canónica detallada (espejo historico en `docs/baseline/DTI.md` §19, espejo vivo hacia adelante en `docs/product/DTP.md` §B) |
| **Regla de seguridad** | `.cursor/rules/seguridad.mdc` | OWASP ASVS L2 — Java/Spring (secretos, PII en logs) |
| **Regla de baseline congelado** | `.cursor/rules/baseline-congelado.mdc` | Prohíbe a cualquier agente editar `docs/baseline/**`; espejo de la regla de este documento (§8.2) |
| **Hooks de Cursor** | `.cursor/hooks.json` + `.cursor/hooks/*.js` | Automatización, no solo convención: `protect-baseline.js` (`preToolUse`) bloquea con `permission: deny` cualquier `Write`/`StrReplace`/`EditNotebook`/`Delete` sobre `docs/baseline/**`; `warn-shell-baseline.js` (`beforeShellExecution`) pide confirmación ante comandos de shell que escriban/muevan/borren rutas de `docs/baseline/`; `dtp-sync-reminder.js` (`stop`) revisa `git diff`/`git ls-files --others` al final de cada turno y recuerda ejecutar `@dtp-sync` si hay cambios sin commitear en `src/`, `docs/design/` o `prompts/PR-IMPL-*.md` sin reflejo en `docs/product/DTP.md` |
| **Baseline congelado M4** | `docs/baseline/{BRD_EduSync_vFinal,MRD_EduSync_vFinal,PRD_EduSync_vFinal,FSD_EduSync_vFinal,DTI}.md` | Registro histórico **inmutable** evaluado en M4, tag `release/2.0.0`, `status: congelado`. Protegido por `CODEOWNERS` y `.cursor/rules/baseline-congelado.mdc`. El antiguo `docs/DTI.md` (v0.8, §0–§23) es hoy `docs/baseline/DTI.md` — su continuación viva es `docs/product/DTP.md` |
| **DTP (capa viva)** | `docs/product/DTP.md` | Documento Técnico del Producto v1.1 (12/07/2026) — continuación viva del DTI congelado, punto de partida para `release/3.0.0`; registra el delta de stack `ADR-0008` y el delta de generalización del modelo de dominio `ADR-0009` en §A.2; §A.3 lista los 5 FSD-UC del baseline + `FSD-UC-011`..`FSD-UC-021` (módulos generalizados) como `pendiente` (sin `DD-UC-NNN` ni `PR-IMPL-NNN` todavía) |
| **PRD/FSD/BRD vivos** | `docs/product/{BRD,PRD,FSD}.md` | Copias editables de la capa viva (banner "COPIA VIVA"), abiertas para `release/3.0.0`; el FSD vivo opera en modo LFSD ⚡. Desde `ADR-0009` (v3.0/v2.0/v2.0 resp.): plataforma SaaS multi-tenant configurable (SysAdmin, Tenant con suscripción, Gestión Escolar, periodos/secciones/tipos de evaluación configurables, Cursos/Paralelos, Materias, Profesores, Estudiantes, Inscripciones, Usuarios y Roles) añadida como extensión aditiva sobre el Perfil Bolivia SIE (BR-001..BR-012/RB-01..RB-11, vigentes sin cambios) |
| **Diagramas C4** | `docs/diagrams/c4_level1.mmd`, `c4_level2.mmd`, `c4_level3_api_gateway.mmd`, `c4_level3_domain_layer.mmd`, `c4_level3_sie_adapter.mmd`, `deployment_aws.mmd` (+ `.md` espejos para Level 3/Deployment) | C4 Level 1, 2, 3 y Deployment AWS; cumple la base para la rúbrica de diagramas versionados |
| **ADRs** | `docs/adr/0001..0006-*.md`, `0008-*.md`, `0009-*.md` | 8 ADRs aprobados: 0001 multitenancy RLS, 0002 parametrización reglas normativas, 0003 audit_log append-only, 0004 async consolidación (Spring Events), 0005 resiliencia SIE (Resilience4j), 0006 cloud provider AWS, 0008 stack vivo Java 25 LTS/Spring Boot 4.1.0/Angular 21 LTS, 0009 generalización del modelo de dominio a plataforma SaaS multi-tenant configurable (extensión aditiva, no supersede a ninguno de los anteriores). `ADR-0007` (Strangler Fig) queda *gated*, sin crear todavía (ver `docs/roadmap.md` §4) |
| **Arq. hexagonal** | `docs/arquitectura_hexagonal_EduSync.md` | Arquitectura hexagonal v0.1 — 20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 Aggregate Roots |
| **DTOs por capa** | `docs/dtos_EduSync.md` | DTOs hexagonales v0.1 — 4 Request, 4 Commands, 3 Response, 5 Domain Events, 5 enums |
| **Skills de Cursor** | `.cursor/skills/<slug>/SKILL.md` | 27 skills (8 EduSync nativos incl. `feature-design-doc` y `dtp-sync` + 19 canónicos Módulo 4 importados desde `plantillas2/`) |
| **Skills de Claude Code** | `.claude/skills/<slug>/SKILL.md` | 11 skills (paridad parcial con `.cursor/skills/`, incluye `feature-design-doc` y `dtp-sync`) |
| **Contratos materializados** | `prompts/PR-<AREA>-NNN.md` | Archivos individuales por prompt-contrato — generados por skill `materialize-prompt-files`. Los prompts de implementación (`PR-IMPL-NNN.md`) siguen la misma convención de archivo plano en `prompts/` (no `docs/prompts/impl/`) |

---

## 2. Contexto que el agente MUST leer antes de actuar

Al comenzar cualquier tarea, el agente **MUST** leer en orden:

1. `docs/arquitectura_funcional_EduSync.md` — los 10 casos de uso críticos, sus invariantes y las 5 decisiones arquitectónicas (DA-01..DA-05).
2. `docs/fsd/FSD_EduSync.md` — el caso de uso tocado por la tarea (FSD-UC-001, UC-003, UC-004, UC-005, UC-009) con sus reglas de negocio y Gherkin.
3. `docs/LFSD-EduSync.md` — diseño técnico de bajo nivel: contratos API, entidades JPA, DDL, esquema de seguridad y pseudoalgoritmos del componente afectado.
4. `docs/brd/BRD_EduSync_v2.md` — reglas de negocio BR-001..BR-012 y políticas RB-01..RB-11 que apliquen a la tarea.
5. `docs/PROMPT_MAPPING.md` — prompt-contrato del componente o caso de uso involucrado.
6. `docs/adr/0001..0006-*.md` + `docs/adr/0008-*.md` — ADRs aprobados que formalizan las decisiones arquitectónicas: multitenancy RLS PostgreSQL, parametrización de reglas normativas, persistencia inmutable `audit_log`, async consolidación Spring Events, resiliencia integración SIE con Resilience4j, cloud provider AWS, estilo de despliegue ECS Fargate y stack vivo Java 25 LTS/Spring Boot 4.1.0/Angular 21 LTS.
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
│   └── skills/                      ← 27 skills activos en Cursor
│       ├── c4-edusync/SKILL.md              ← C4 Level 1/2/3 de EduSync (PR-SKILL-002)
│       ├── dti-edusync/SKILL.md             ← poblar y mantener el DTI (histórico; el DTI vigente ya está congelado en docs/baseline/DTI.md) (PR-SKILL-003)
│       ├── feature-design-doc/SKILL.md      ← generar DD-UC-NNN en docs/design/ a partir de un FSD-UC vivo
│       ├── dtp-sync/SKILL.md                ← sincronizar docs/product/DTP.md tras cada PR de implementación; nunca toca docs/baseline/
│       ├── update-prompt-mapping/SKILL.md   ← actualizar PROMPT_MAPPING (PR-SKILL-001)
│       ├── edusync-skill-creator/SKILL.md   ← crear nuevos skills EduSync
│       ├── materialize-prompt-files/SKILL.md← backfill de prompts/PR-*.md
│       ├── sync-doc-chain/SKILL.md          ← propagar cambios BRD→FSD→ADR→DTI ↔ diagrams
│       └── <19 canónicos Módulo 4>/SKILL.md ← async-architecture-reviewer, broker-selector,
│                                            cdc-pipeline-designer, ddd-aggregate-designer,
│                                            distributed-architecture-reviewer, dr-strategy-designer,
│                                            event-catalog-author, event-schema-designer,
│                                            external-api-designer, fsd-gherkin-a-tests-aceptacion,
│                                            fsd-modelo-datos-a-jpa-flyway, fsd-uc-a-vertical-slice,
│                                            ipc-style-selector, monolith-decomposition-architect,
│                                            quantum-opportunity-scout, resilience-strategy-designer,
│                                            saga-designer, serverless-architect,
│                                            strangler-fig-migrator
├── .claude/
│   └── skills/                      ← 11 skills (paridad parcial con .cursor/skills/)
│       ├── c4-edusync/SKILL.md
│       ├── dti-edusync/SKILL.md
│       ├── feature-design-doc/SKILL.md
│       ├── dtp-sync/SKILL.md
│       ├── update-prompt-mapping/SKILL.md
│       ├── edusync-skill-creator/SKILL.md
│       ├── sync-doc-chain/SKILL.md
│       └── adr-edusync/SKILL.md
├── README.md
├── AGENTS.md                        ← este archivo (v0.12) — convención GitHub/Cursor, raíz requerida por la rúbrica del Módulo 4
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
│   ├── PROMPT_MAPPING.md            ← catálogo de 37 prompt-contratos v2.0 (área IMPL reservada)
│   ├── baseline/                    ← ⚠ CONGELADO (M4, tag release/2.0.0). Prohibido editar (CODEOWNERS + baseline-congelado.mdc)
│   │   ├── BRD_EduSync_vFinal.md
│   │   ├── MRD_EduSync_vFinal.md
│   │   ├── PRD_EduSync_vFinal.md
│   │   ├── FSD_EduSync_vFinal.md
│   │   └── DTI.md                   ← Documento Técnico Inicial v0.8 congelado (antes docs/DTI.md); continuación viva: docs/product/DTP.md
│   ├── product/                     ← VIVO desde release/3.0.0 (editable)
│   │   ├── BRD.md, PRD.md, FSD.md   ← copias vivas (banner "COPIA VIVA"); FSD en modo LFSD ⚡
│   │   └── DTP.md                   ← Documento Técnico del Producto v1.0 (punto de partida; ADR-0008 registrado en §A.2)
│   ├── design/                      ← ⚠ pendiente de creación — DD-UC-NNN (design docs por feature, skill feature-design-doc)
│   ├── brd/
│   │   ├── BRD_EduSync_v1.md        ← BRD inicial
│   │   └── BRD_EduSync_v2.md        ← BRD consolidado (BR-001..BR-012)
│   ├── mrd/
│   │   └── MRD_EduSync.md           ← Market Requirements v1.0
│   ├── prd/
│   │   └── PRD_EduSync.md           ← Product Requirements v1.0 (17 US, 6 épicas)
│   ├── fsd/
│   │   └── FSD_EduSync.md           ← FSD Clásico v1.0 (5 FSD-UC, ER 16 entidades)
│   ├── adr/                         ← 7 ADRs aprobados (0007 Strangler Fig queda gated, sin crear)
│   │   ├── 0001-multitenancy-rls-postgresql.md
│   │   ├── 0002-parametrizacion-reglas-normativas.md
│   │   ├── 0003-persistencia-inmutable-audit-log.md
│   │   ├── 0004-async-consolidacion-spring-events.md
│   │   ├── 0005-resiliencia-integracion-sie-resilience4j.md
│   │   ├── 0006-cloud-provider-y-estilo-de-despliegue.md
│   │   └── 0008-actualizacion-stack-java25-springboot4-angular21.md
│   └── diagrams/                    ← diagramas Mermaid (fuente de verdad visual)
│       ├── ai-sdlc.mmd              ← comparativa AI-SDLC vs. SDLC tradicional
│       ├── c4_level1.mmd            ← C4 Level 1 — Contexto del sistema
│       ├── c4_level2.mmd            ← C4 Level 2 — Contenedores
│       ├── estados.cargarnotas.mmd  ← 18 estados del Docente (.mmd canónico)
│       ├── estados_cargar_notas.mmd ← duplicado normalizado (mismo origen)
│       ├── estados_cargar_notas.md  ← spec formal estados Docente
│       ├── estados_administracion.mmd ← 23 estados del Director
│       └── estados_administracion.md  ← spec formal estados Director
├── prompts/                         ← archivos individuales por prompt-contrato
│   └── PR-<AREA>-NNN.md             ← 37 contratos materializados (PR-ADR-001..005, PR-ARCH-001/002,
│                                      PR-APORTES-001, PR-AUD-001, PR-BRD-001/002, PR-C4-001..006, PR-DIAG-001/002,
│                                      PR-DTI-001, PR-DTI-SEAMS-001, PR-DTO-001, PR-FSD-001,
│                                      PR-HEX-001, PR-INF-001, PR-LFSD-001, PR-MRD-001, PR-POC-001/002, PR-PRD-001,
│                                      PR-ROADMAP-001, PR-SKILL-001/002/003, PR-UC-001..005, PR-UC-009, PR-VFINAL-001)
│                                      — área IMPL (PR-IMPL-NNN) reservada, sin contratos todavía
├── src/                             ← ⚠ pendiente de implementación
│   ├── domain/                      ← entidades, VO, aggregates, puertos (sin deps Spring)
│   │   ├── calificacion/
│   │   ├── periodo/
│   │   ├── estudiante/
│   │   ├── exportacion/
│   │   └── auditoria/
│   ├── application/                 ← casos de uso (ports-in impl)
│   │   ├── RegistrarCalificacionUseCase.java
│   │   ├── CerrarMateriaUseCase.java
│   │   ├── ConsolidarCentralizadorUseCase.java
│   │   ├── ExportarSIEUseCase.java
│   │   └── GestionarCorreccionUseCase.java
│   └── infrastructure/
│       ├── web/                     ← REST controllers + DTOs
│       ├── persistence/             ← JPA adapters + Flyway
│       ├── security/                ← JwtAuthFilter, SecurityConfig, RLS injection
│       ├── integration/sie/         ← SIEHttpClient
│       ├── scheduler/               ← VentanaExpiracionScheduler, SIERetryScheduler
│       └── aop/                     ← AuditLogAspect
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
├── infra/                           ← ⚠ pendiente de creación (IaC Terraform/AWS)
│   ├── rds/
│   ├── sqs/
│   └── ecs/
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

> El agente **MUST NOT** introducir dependencias fuera de esta tabla sin crear un ADR en `docs/adr/` y obtener aprobación humana explícita en el PR.

---

## 5. Convenciones de código

- **Idioma del código**: inglés (clases, métodos, variables, comentarios inline).
- **Idioma de la documentación**: español (docs, ADR, comentarios Javadoc de dominio).
- **Estilo**: Google Java Style Guide.
- **Naming**: clases `PascalCase`, métodos `camelCase`, constantes `UPPER_SNAKE_CASE`, paquetes `lower.case`.
- **Arquitectura**: hexagonal estricta (Ports & Adapters). El paquete `domain/` **MUST NOT** importar de `infrastructure/` ni de frameworks externos (Spring, JPA, AWS). Solo interfaces puras.
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
- **MUST**: el cálculo de promedios y la aplicación de `floor()` ocurren **exclusivamente** en `ConsolidacionDomainService`. **MUST NOT** realizar cálculos de promedio en adaptadores, consultas SQL ad-hoc ni en el frontend (BR-008). La función de truncado es `Math.floor()`, nunca `Math.round()`, `HALF_UP` ni `CEILING`.
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
- **Regla de seguridad activa**: `.cursor/rules/seguridad.mdc` — OWASP ASVS L2 aplicado en Java/Spring.
- **Cumplimiento aplicable**:
  - **Ley 164 (Bolivia)**: protección de datos personales aplicable a datos de menores de edad (estudiantes).
  - **Regulación ministerial SIE**: formato y protocolo de exportación son obligatorios e inquebrantables; toda desviación constituye incumplimiento sancionable.
- **Autenticación**: JWT con expiración máxima de 8 horas (NFR-008 del FSD). **MUST NOT** aceptar tokens sin firma válida o expirados.
- **TLS**: HTTPS/TLS 1.3 en tránsito (NFR-009 del FSD). Configurado a nivel de Load Balancer AWS.

---

## 8. Capacidades y guardrails de agentes

### 8.1 Agentes activos en este repositorio

| Agente | Propósito | Modelo | Herramientas | Límites estrictos |
|--------|-----------|--------|--------------|-------------------|
| `dev-agent` | Implementar casos de uso backend (FSD-UC-001..009) | Sonnet | `read`, `edit`, `run-tests`, `run-linter` | **MUST NOT** tocar `infra/`; **MUST NOT** modificar migraciones Flyway aplicadas; **MUST NOT** calcular promedios fuera de `ConsolidacionDomainService`; **MUST NOT** editar `docs/baseline/**` |
| `arch-agent` | Evaluar alternativas y documentar decisiones arquitectónicas (DA-01..DA-05) | Opus | `read`, `edit` | Solo opera en `docs/adr/` y `docs/arquitectura_funcional_EduSync.md`; toda decisión requiere aprobación humana |
| `docs-agent` | Mantener y sincronizar la cadena documental BRD→MRD→PRD→FSD→LFSD en `docs/`, incluida la capa viva `docs/product/` (skills `feature-design-doc`, `dtp-sync`) | Sonnet | `read`, `edit` | Solo opera dentro de `docs/`; **MUST NOT** editar código fuente; **MUST NOT** editar `docs/baseline/**` bajo ninguna circunstancia (ver `.cursor/rules/baseline-congelado.mdc`) |
| `qa-agent` | Verificar invariantes de dominio, trazabilidad de audit_log y cobertura de pruebas | Sonnet | `read`, `query-db` (solo SELECT) | **MUST NOT** realizar escrituras; solo lectura y análisis |
| `process-agent` | Modelar workflows y diagramas de estado (Docente, Director) garantizando consistencia con UCs | Sonnet | `read`, `edit` | Opera en `docs/diagrams/`; diagramas deben usar `stateDiagram-v2` y nombres reales del dominio |
| `compliance-agent` | Validar que ningún output de `dev-agent` viole invariantes regulatorias del SIE (RUDE, floor, rangos) | Sonnet | `read`, ejecutar golden tests | Solo lectura de artefactos + ejecución de golden tests en CI; bloquea merge si falla |

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
  A[Recibir tarea] --> B[Leer arquitectura_funcional + FSD-UC afectado + LFSD §componente]
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
  * floor() como único truncado permitido
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
- Pide realizar cálculos de promedio o aplicar `Math.round()` / `HALF_UP` fuera de `ConsolidacionDomainService`.
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

---

## Checklist de validez

- [x] Sincronizado con `docs/arquitectura_funcional_EduSync.md` (10 UCs + 5 DAs).
- [x] Sincronizado con `docs/fsd/FSD_EduSync.md` v1.0 (FSD-UC-001, UC-003, UC-004, UC-005, UC-009).
- [x] Sincronizado con `docs/LFSD-EduSync.md` v1.0.1 (arquitectura hexagonal, DDL, APIs; ruta FSD normalizada).
- [x] Sincronizado con `docs/PROMPT_MAPPING.md` v2.0 (37 prompt-contratos + carpeta `prompts/`; área `IMPL` reservada).
- [x] Sincronizado con `docs/baseline/DTI.md` v0.8 (congelado, tag `release/2.0.0`) — referencia atómica AGENTS ↔ DTI; C4 L1/L2/L3 + Deployment AWS canónicos en `docs/diagrams/`; `docs/roadmap.md` v0.3 declarado fuente canónica del horizonte (espejo histórico en DTI §19, espejo vivo en `docs/product/DTP.md` §B).
- [x] Todos los paths de archivos verificados contra la estructura real del repositorio.
- [x] Sin secretos en texto plano.
- [x] Stack y versiones actualizados — baseline M4: PostgreSQL 15, Spring Boot 3.3, Java 21, Angular 17; vivo desde `release/3.0.0`: PostgreSQL 15, Spring Boot 4.1.0, Java 25 LTS, Angular 21 LTS (`ADR-0008`).
- [x] 6 agentes documentados con sus límites estrictos.
- [x] 4 golden tests obligatorios de zero-tolerance definidos.
- [x] Guardrails probados con lista de prompts prohibidos.
- [x] `docs/adr/0001..0006-*.md` + `0008-*.md` creados — 7 ADRs aprobados (multitenancy RLS, parametrización, audit_log, async, resiliencia SIE, cloud provider, stack vivo Java 25/Boot 4.1.0/Angular 21). `ADR-0007` (Strangler Fig) queda *gated*, sin crear.
- [x] `docs/diagrams/c4_level1.mmd` y `c4_level2.mmd` creados (Contexto + Contenedores).
- [x] `.cursor/skills/` extendida a 27 skills (8 EduSync incl. `feature-design-doc`/`dtp-sync` + 19 canónicos Módulo 4); `.claude/skills/` a 11.
- [x] `docs/diagrams/c4_level3_*.mmd` + `deployment_aws.mmd` creados — `api-gateway` (PR-C4-003), `domain-layer` (PR-C4-004), `sie-adapter` (PR-C4-005) y Deployment AWS (PR-C4-006).
- [x] `docs/roadmap.md` v0.3 creado y actualizado (PR-ROADMAP-001) — hoja de ruta canónica con 4 horizontes + apertura de capa viva; espejo histórico en `docs/baseline/DTI.md` §19, espejo vivo en `docs/product/DTP.md`.
- [x] `docs/baseline/` protegido — 5 archivos marcados `status: congelado`, `CODEOWNERS`, `.cursor/rules/baseline-congelado.mdc` y el hook `.cursor/hooks.json` → `protect-baseline.js` (enforcement real, no solo advisory) creados y probados manualmente.
- [x] `.cursor/hooks.json` creado con 3 scripts Node.js probados: `protect-baseline.js` (bloqueo), `warn-shell-baseline.js` (confirmación en shell) y `dtp-sync-reminder.js` (recordatorio `@dtp-sync` al cierre de turno).
- [x] `docs/product/DTP.md` v1.0 creado como punto de partida de la capa viva (`release/3.0.0`), con el delta de stack `ADR-0008` registrado en §A.2.
- [x] `docs/adr/0009-*.md` creado y `docs/product/{BRD,PRD,FSD}.md` + `DTP.md` v1.1 actualizados con la generalización del modelo de dominio a plataforma SaaS multi-tenant configurable, como extensión aditiva sobre el Perfil Bolivia SIE (BR-001..BR-012/RB-01..RB-11 y FSD-UC-001..009 vigentes sin cambios).
- [ ] Resolución de los 5 puntos pendientes de definición de `ADR-0009` §3 (reconciliación con el modelo Bolivia, secuencialidad/redondeo genérico, validación de suma de pesos, gobernanza de módulos nuevos) — bloqueante antes de implementar código sobre `FSD-UC-011`..`FSD-UC-021`.
- [ ] `pocs/POC-NN/` pendiente — evidencia ejecutiva de POC-01 (RLS) y POC-02 (Circuit Breaker SIE) bloqueando promoción a `release/1.1.0`.
- [ ] Primer `docs/design/DD-UC-NNN.md` + primer `prompts/PR-IMPL-001.md` — pendiente, `src/` sigue vacío.
- [ ] Limpieza de ~50 referencias históricas a `docs/DTI.md` (ya movido a `docs/baseline/DTI.md`) dispersas en `prompts/PR-*.md`, `plantillas2/`, y skills operativos (`sync-doc-chain`, `c4-edusync`, `adr-edusync`, `poc-runner-edusync`) — fuera de alcance de esta pasada, recomendado como tarea de seguimiento.
- [x] `docs/aportes/release-2.0.0.md` creado (PR-APORTES-001 v0.1 — 95 tareas auditables, factor 1.00 por caso degenerado n = 1; commit final pendiente del push de `release/2.0.0`).
- [x] `AGENTS.md` en la **raíz** del repo (rúbrica del Módulo 4 pide ubicación raíz) — completado en v0.9.
- [x] Alias `_vFinal` creados para BRD/MRD/PRD/FSD (`PR-VFINAL-001`): `docs/brd/BRD_EduSync_vFinal.md`, `docs/mrd/MRD_EduSync_vFinal.md`, `docs/prd/PRD_EduSync_vFinal.md`, `docs/fsd/FSD_EduSync_vFinal.md`.
- [x] Bump del DTI a v0.6 + cierre del drift de `adrs_vigentes` (nombres reales `0001..0006-*.md` en frontmatter).
- [ ] `.claude/skills/` alcanzar paridad con `.cursor/skills/` (faltan los 19 canónicos Módulo 4 + `materialize-prompt-files`).
- [ ] Stack y versiones a verificar contra `pom.xml` cuando el proyecto de código sea inicializado.
- [ ] Revisado por al menos un humano del grupo antes de cada release.
