# Aportes Individuales — `release/2.0.0` EduSync

> Generado por el contrato `prompts/PR-APORTES-001.md` v0.1.
> Estructura literal de `plantillas/APORTES_TEMPLATE.md` (6 secciones).
> **Caso degenerado n = 1**: el grupo G-EduSync es unipersonal, por lo que el factor de aporte individual colapsa matemáticamente a `1.00`. Este archivo funciona como **inventario auditable** del trabajo individual, no como ajuste de nota relativo.

---

## 0. Metadatos

| Campo | Valor |
|-------|-------|
| Producto | EduSync |
| Grupo | G-EduSync |
| Release evaluable | `release/2.0.0` |
| Sesión asociada | S12 |
| Fecha de cierre | 28/05/2026 |
| Integrantes del grupo (n) | Rodrigo Aspeti (n = 1) |
| Branch del release | `release/2.0.0` |
| Commit de cierre (HEAD) | pendiente (a poblar al hacer push del release) |

---

## 1. Tabla de tareas atribuidas

> Una fila por tarea concreta producida; cada referencia es verificable contra archivo+sección o ruta del repo. Orden cronológico ascendente. Total esperado al cierre: ≥ 50 filas.

| # | Integrante | Tarea concreta | Categoría | Referencia | Fecha |
|---|------------|----------------|-----------|------------|-------|
| 1 | Rodrigo Aspeti | Arquitectura funcional EduSync — 10 UCs críticos + 5 decisiones arquitectónicas (DA-01..DA-05) | FSD | `docs/arquitectura_funcional_EduSync.md` | 14/05 |
| 2 | Rodrigo Aspeti | BRD EduSync v1 — Business Requirements iniciales | BRD | `docs/brd/BRD_EduSync_v1.md` | 14/05 |
| 3 | Rodrigo Aspeti | Diagrama AI-SDLC vs. SDLC tradicional | Diagrama | `docs/diagrams/ai-sdlc.mmd` | 14/05 |
| 4 | Rodrigo Aspeti | Diagrama de estados del Docente — 18 estados de "Cargar Notas" | Diagrama | `docs/diagrams/estados_cargar_notas.mmd` | 14/05 |
| 5 | Rodrigo Aspeti | Diagrama de estados del Director — 23 estados de Administración | Diagrama | `docs/diagrams/estados_administracion.mmd` | 14/05 |
| 6 | Rodrigo Aspeti | Diagrama de estados duplicado normalizado (compatibilidad de parsers) | Diagrama | `docs/diagrams/estados.cargarnotas.mmd` | 14/05 |
| 7 | Rodrigo Aspeti | PR-ARCH-001 — Prompt-contrato de generación de `AGENTS.md` v0.1 | Prompt | `prompts/PR-ARCH-001.md` | 14/05 |
| 8 | Rodrigo Aspeti | PR-BRD-001 — Prompt-contrato de generación del BRD v1 | Prompt | `prompts/PR-BRD-001.md` | 14/05 |
| 9 | Rodrigo Aspeti | PR-UC-001 — Prompt-contrato de FSD-UC-001 (Registro de calificación) | Prompt | `prompts/PR-UC-001.md` | 14/05 |
| 10 | Rodrigo Aspeti | PR-UC-002 — Prompt-contrato de FSD-UC-002 (Cierre de materia) | Prompt | `prompts/PR-UC-002.md` | 14/05 |
| 11 | Rodrigo Aspeti | PR-UC-003 — Prompt-contrato de FSD-UC-003 (Consolidación centralizador) | Prompt | `prompts/PR-UC-003.md` | 14/05 |
| 12 | Rodrigo Aspeti | PR-UC-004 — Prompt-contrato de FSD-UC-004 (Exportación SIE) | Prompt | `prompts/PR-UC-004.md` | 14/05 |
| 13 | Rodrigo Aspeti | PR-UC-005 — Prompt-contrato de FSD-UC-005 (Autorización corrección retroactiva) | Prompt | `prompts/PR-UC-005.md` | 14/05 |
| 14 | Rodrigo Aspeti | PR-UC-009 — Prompt-contrato de FSD-UC-009 (Gestión de períodos académicos) | Prompt | `prompts/PR-UC-009.md` | 14/05 |
| 15 | Rodrigo Aspeti | PR-ADR-001 — Prompt-contrato de ADR multitenancy RLS PostgreSQL | Prompt | `prompts/PR-ADR-001.md` | 14/05 |
| 16 | Rodrigo Aspeti | PR-ADR-002 — Prompt-contrato de ADR parametrización de reglas normativas | Prompt | `prompts/PR-ADR-002.md` | 14/05 |
| 17 | Rodrigo Aspeti | PR-ADR-003 — Prompt-contrato de ADR persistencia inmutable `audit_log` | Prompt | `prompts/PR-ADR-003.md` | 14/05 |
| 18 | Rodrigo Aspeti | PR-ADR-004 — Prompt-contrato de ADR async consolidación Spring Events | Prompt | `prompts/PR-ADR-004.md` | 14/05 |
| 19 | Rodrigo Aspeti | PR-ADR-005 — Prompt-contrato de ADR resiliencia SIE Resilience4j | Prompt | `prompts/PR-ADR-005.md` | 14/05 |
| 20 | Rodrigo Aspeti | PR-AUD-001 — Prompt-contrato de auditoría IA del sistema | Prompt | `prompts/PR-AUD-001.md` | 14/05 |
| 21 | Rodrigo Aspeti | PR-INF-001 — Prompt-contrato de infraestructura | Prompt | `prompts/PR-INF-001.md` | 14/05 |
| 22 | Rodrigo Aspeti | PR-DIAG-001 — Prompt-contrato de diagrama de estados Docente | Prompt | `prompts/PR-DIAG-001.md` | 14/05 |
| 23 | Rodrigo Aspeti | PR-DIAG-002 — Prompt-contrato de diagrama de estados Director | Prompt | `prompts/PR-DIAG-002.md` | 14/05 |
| 24 | Rodrigo Aspeti | BRD EduSync v2 — consolidación BR-001..BR-012 + RB-01..RB-11 + KPI-01..05 | BRD | `docs/brd/BRD_EduSync_v2.md` | 14/05 |
| 25 | Rodrigo Aspeti | PR-BRD-002 — Prompt-contrato de consolidación del BRD v2 | Prompt | `prompts/PR-BRD-002.md` | 14/05 |
| 26 | Rodrigo Aspeti | PR-MRD-001 — Prompt-contrato del MRD EduSync | Prompt | `prompts/PR-MRD-001.md` | 15/05 |
| 27 | Rodrigo Aspeti | MRD EduSync v1.0 — Market Requirements Document | MRD | `docs/mrd/MRD_EduSync.md` | 15/05 |
| 28 | Rodrigo Aspeti | PR-PRD-001 — Prompt-contrato del PRD EduSync | Prompt | `prompts/PR-PRD-001.md` | 15/05 |
| 29 | Rodrigo Aspeti | PRD EduSync v1.0 — Product Requirements Document (17 US, 6 épicas) | PRD | `docs/prd/PRD_EduSync.md` | 15/05 |
| 30 | Rodrigo Aspeti | PR-FSD-001 — Prompt-contrato del FSD Clásico EduSync | Prompt | `prompts/PR-FSD-001.md` | 15/05 |
| 31 | Rodrigo Aspeti | FSD EduSync v1.0 — FSD Clásico (5 FSD-UC, ER 16 entidades, 14 tasks) | FSD | `docs/fsd/FSD_EduSync.md` | 15/05 |
| 32 | Rodrigo Aspeti | PR-LFSD-001 — Prompt-contrato del Low-Level Functional Specification | Prompt | `prompts/PR-LFSD-001.md` | 15/05 |
| 33 | Rodrigo Aspeti | LFSD EduSync v1.0.1 — arquitectura hexagonal, DDL, 15+ APIs, 4 diagramas de secuencia, 16 tasks | FSD | `docs/LFSD-EduSync.md` | 15/05 |
| 34 | Rodrigo Aspeti | PR-ARCH-002 — Prompt-contrato de consolidación de `AGENTS.md` v0.2 | Prompt | `prompts/PR-ARCH-002.md` | 17/05 |
| 35 | Rodrigo Aspeti | AGENTS.md v0.2 — 6 rutas corregidas + 15 artefactos nuevos + 6 agentes + 4 golden tests | AGENTS | `AGENTS.md §15 (historial v0.2)` | 17/05 |
| 36 | Rodrigo Aspeti | PR-SKILL-001 — Prompt-contrato del skill `update-prompt-mapping` | Prompt | `prompts/PR-SKILL-001.md` | 17/05 |
| 37 | Rodrigo Aspeti | Skill `update-prompt-mapping` (Cursor + Claude) — actualización del catálogo de prompts | Skill | `.cursor/skills/update-prompt-mapping/SKILL.md` | 17/05 |
| 38 | Rodrigo Aspeti | PR-SKILL-002 — Prompt-contrato del skill `c4-edusync` | Prompt | `prompts/PR-SKILL-002.md` | 17/05 |
| 39 | Rodrigo Aspeti | Skill `c4-edusync` — generación de diagramas C4 de EduSync | Skill | `.cursor/skills/c4-edusync/SKILL.md` | 17/05 |
| 40 | Rodrigo Aspeti | PR-C4-001 — Prompt-contrato del C4 Level 1 (Contexto del sistema) | Prompt | `prompts/PR-C4-001.md` | 17/05 |
| 41 | Rodrigo Aspeti | C4 Level 1 — Diagrama de Contexto del Sistema EduSync | Diagrama | `docs/diagrams/c4_level1.mmd` | 17/05 |
| 42 | Rodrigo Aspeti | PR-C4-002 — Prompt-contrato del C4 Level 2 (Contenedores) | Prompt | `prompts/PR-C4-002.md` | 17/05 |
| 43 | Rodrigo Aspeti | C4 Level 2 — Diagrama de Contenedores (7 contenedores EduSync) | Diagrama | `docs/diagrams/c4_level2.mmd` | 17/05 |
| 44 | Rodrigo Aspeti | PR-SKILL-003 — Prompt-contrato del skill `dti-edusync` | Prompt | `prompts/PR-SKILL-003.md` | 17/05 |
| 45 | Rodrigo Aspeti | Skill `dti-edusync` — autor y validador del Documento Técnico Inicial | Skill | `.cursor/skills/dti-edusync/SKILL.md` | 17/05 |
| 46 | Rodrigo Aspeti | PR-DTI-001 — Prompt-contrato de generación del DTI EduSync | Prompt | `prompts/PR-DTI-001.md` | 17/05 |
| 47 | Rodrigo Aspeti | DTI EduSync v0.1 — 23 secciones, 883 líneas, C4 L1/L2/L3 embebidos, 5 ADRs provisionales, 16 NFRs, 4 golden tests | AGENTS | `docs/DTI.md §0..§23` | 17/05 |
| 48 | Rodrigo Aspeti | PR-HEX-001 — Prompt-contrato de arquitectura hexagonal del core | Prompt | `prompts/PR-HEX-001.md` | 24/05 |
| 49 | Rodrigo Aspeti | Arquitectura hexagonal EduSync v0.1 — 20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 Aggregate Roots | FSD | `docs/arquitectura_hexagonal_EduSync.md` | 24/05 |
| 50 | Rodrigo Aspeti | PR-DTO-001 — Prompt-contrato de DTOs por capa hexagonal | Prompt | `prompts/PR-DTO-001.md` | 24/05 |
| 51 | Rodrigo Aspeti | DTOs EduSync v0.1 — 4 Request, 4 Commands, 3 Response, 5 Domain Events, 5 enums, 3 tablas DTO↔Entidad | FSD | `docs/dtos_EduSync.md` | 24/05 |
| 52 | Rodrigo Aspeti | Rule de seguridad — OWASP ASVS L2 para Java/Spring (secretos, PII en logs) | Rule | `.cursor/rules/seguridad.mdc` | 24/05 |
| 53 | Rodrigo Aspeti | APORTES_EduSync.md — informe de aportes de releases previos (1.0.0/1.0.1) | Bitácora | `docs/APORTES_EduSync.md` | 24/05 |
| 54 | Rodrigo Aspeti | Skill `sync-doc-chain` — sincronización atómica DTI↔AGENTS↔PROMPT_MAPPING | Skill | `.cursor/skills/sync-doc-chain/SKILL.md` | 28/05 |
| 55 | Rodrigo Aspeti | Skill `adr-edusync` — autor y validador de ADRs formales | Skill | `.cursor/skills/adr-edusync/SKILL.md` | 28/05 |
| 56 | Rodrigo Aspeti | Skill `poc-runner-edusync` — ejecutor y capturador de evidencia para POCs | Skill | `.cursor/skills/poc-runner-edusync/SKILL.md` | 28/05 |
| 57 | Rodrigo Aspeti | Skill `edusync-skill-creator` — generador de nuevos skills EduSync | Skill | `.cursor/skills/edusync-skill-creator/SKILL.md` | 28/05 |
| 58 | Rodrigo Aspeti | Skill `materialize-prompt-files` — materialización de archivos individuales `prompts/PR-*.md` | Skill | `.cursor/skills/materialize-prompt-files/SKILL.md` | 28/05 |
| 59 | Rodrigo Aspeti | Skill `distributed-architecture-reviewer` (derivado EduSync) | Skill | `.cursor/skills/distributed-architecture-reviewer/SKILL.md` | 28/05 |
| 60 | Rodrigo Aspeti | ADR-0001 — Multitenancy con RLS PostgreSQL (formal, Aceptada) | ADR | `docs/adr/0001-multitenancy-rls-postgresql.md` | 28/05 |
| 61 | Rodrigo Aspeti | ADR-0002 — Parametrización de reglas normativas (formal, Aceptada) | ADR | `docs/adr/0002-parametrizacion-reglas-normativas.md` | 28/05 |
| 62 | Rodrigo Aspeti | ADR-0003 — Persistencia inmutable `audit_log` append-only (formal, Aceptada) | ADR | `docs/adr/0003-persistencia-inmutable-audit-log.md` | 28/05 |
| 63 | Rodrigo Aspeti | ADR-0004 — Consolidación asíncrona con Spring Events (formal, Aceptada) | ADR | `docs/adr/0004-async-consolidacion-spring-events.md` | 28/05 |
| 64 | Rodrigo Aspeti | ADR-0005 — Resiliencia integración SIE con Resilience4j (formal, Aceptada) | ADR | `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` | 28/05 |
| 65 | Rodrigo Aspeti | ADR-0006 — Cloud provider AWS y estilo de despliegue ECS Fargate (formal, Aceptada) | ADR | `docs/adr/0006-cloud-provider-y-estilo-de-despliegue.md` | 28/05 |
| 66 | Rodrigo Aspeti | PR-POC-001 — Prompt-contrato de POC-01 (RLS multitenancy) | Prompt | `prompts/PR-POC-001.md` | 28/05 |
| 67 | Rodrigo Aspeti | POC-01 README — definición y criterios de éxito de la POC de aislamiento RLS | POC | `docs/pocs/POC-01-rls-multitenancy/README.md` | 28/05 |
| 68 | Rodrigo Aspeti | POC-01 runbook — procedimiento ejecutable de la POC-01 | POC | `docs/pocs/POC-01-rls-multitenancy/runbook.md` | 28/05 |
| 69 | Rodrigo Aspeti | PR-POC-002 — Prompt-contrato de POC-02 (Circuit Breaker SIE) | Prompt | `prompts/PR-POC-002.md` | 28/05 |
| 70 | Rodrigo Aspeti | POC-02 README — definición y criterios de éxito del Circuit Breaker para SIE | POC | `docs/pocs/POC-02-circuit-breaker-sie/README.md` | 28/05 |
| 71 | Rodrigo Aspeti | POC-02 runbook — procedimiento ejecutable de la POC-02 | POC | `docs/pocs/POC-02-circuit-breaker-sie/runbook.md` | 28/05 |
| 72 | Rodrigo Aspeti | PR-DTI-SEAMS-001 — Prompt-contrato de seams de descomposición §6.2 del DTI | Prompt | `prompts/PR-DTI-SEAMS-001.md` | 28/05 |
| 73 | Rodrigo Aspeti | DTI v0.2 — §6.2 Seams de descomposición (Tarea 1 Módulo 4) | Bitácora | `docs/DTI.md §6.2` + changelog v0.2 | 28/05 |
| 74 | Rodrigo Aspeti | DTI v0.3 — sincronización con estado real del repo (ADRs reales, skills v9, secciones POC) | Bitácora | `docs/DTI.md` changelog v0.3 | 28/05 |
| 75 | Rodrigo Aspeti | PROMPT_MAPPING.md v1.5 — bump tras incorporación C4 Level 3 + Deployment | Bitácora | `docs/PROMPT_MAPPING.md` historial v1.5 | 28/05 |
| 76 | Rodrigo Aspeti | PR-C4-003 — Prompt-contrato de C4 Level 3 (`api-gateway`) | Prompt | `prompts/PR-C4-003.md` | 28/05 |
| 77 | Rodrigo Aspeti | C4 Level 3 — Componentes del contenedor `api-gateway` | Diagrama | `docs/diagrams/c4_level3_api_gateway.mmd` | 28/05 |
| 78 | Rodrigo Aspeti | DTI v0.4 — incorporación de C4 L3 `api-gateway` como fuente canónica de §3.3 | Bitácora | `docs/DTI.md` changelog v0.4 | 28/05 |
| 79 | Rodrigo Aspeti | PR-C4-004 — Prompt-contrato de C4 Level 3 (`domain-layer`) | Prompt | `prompts/PR-C4-004.md` | 28/05 |
| 80 | Rodrigo Aspeti | C4 Level 3 — Componentes de la capa de dominio hexagonal | Diagrama | `docs/diagrams/c4_level3_domain_layer.mmd` | 28/05 |
| 81 | Rodrigo Aspeti | PR-C4-005 — Prompt-contrato de C4 Level 3 (`sie-adapter`) | Prompt | `prompts/PR-C4-005.md` | 28/05 |
| 82 | Rodrigo Aspeti | C4 Level 3 — Componentes del adaptador SIE (Circuit Breaker, Retry, Bulkhead) | Diagrama | `docs/diagrams/c4_level3_sie_adapter.mmd` | 28/05 |
| 83 | Rodrigo Aspeti | PR-C4-006 — Prompt-contrato del C4 Deployment AWS | Prompt | `prompts/PR-C4-006.md` | 28/05 |
| 84 | Rodrigo Aspeti | C4 Deployment AWS — CloudFront/S3, ALB/WAF, ECS Fargate, RDS Multi-AZ, SQS, KMS, CloudWatch | Diagrama | `docs/diagrams/deployment_aws.mmd` | 28/05 |
| 85 | Rodrigo Aspeti | DTI v0.5 — incorporación en tanda de 3 diagramas L3 + Deployment como fuentes canónicas | Bitácora | `docs/DTI.md` changelog v0.5 | 28/05 |
| 86 | Rodrigo Aspeti | AGENTS.md v0.7 — sincronización con DTI v0.5 + PROMPT_MAPPING v1.5 (34 contratos) | Bitácora | `AGENTS.md` historial v0.7 | 28/05 |
| 87 | Rodrigo Aspeti | PROMPT_MAPPING.md v1.6 — incorporación de PR-ROADMAP-001 + área `ROADMAP` | Bitácora | `docs/PROMPT_MAPPING.md` historial v1.6 | 28/05 |
| 88 | Rodrigo Aspeti | AGENTS.md v0.8 — sincronización atómica con DTI v0.6 + roadmap v0.1 | Bitácora | `AGENTS.md` historial v0.8 | 28/05 |
| 89 | Rodrigo Aspeti | PR-ROADMAP-001 — Prompt-contrato del roadmap técnico y de negocio | Prompt | `prompts/PR-ROADMAP-001.md` | 28/05 |
| 90 | Rodrigo Aspeti | Roadmap v0.1 — hoja de ruta canónica con 4 horizontes, Gantt, 9 lecciones, métricas, riesgos y compromisos | AGENTS | `docs/roadmap.md` | 28/05 |
| 91 | Rodrigo Aspeti | DTI v0.6 — §19 reescrita como espejo resumen de `docs/roadmap.md` | Bitácora | `docs/DTI.md` changelog v0.6 | 28/05 |
| 92 | Rodrigo Aspeti | AGENTS.md v0.9 — **move físico de `docs/AGENTS.md` → `AGENTS.md` raíz** (110 menciones propagadas en 34 archivos) | Bitácora | `AGENTS.md` historial v0.9 | 28/05 |
| 93 | Rodrigo Aspeti | PROMPT_MAPPING.md v1.7 — sync del move de AGENTS.md + ajustes quirúrgicos en 3 contratos inline | Bitácora | `docs/PROMPT_MAPPING.md` historial v1.7 | 28/05 |
| 94 | Rodrigo Aspeti | DTI v0.7 — sincronización tras move de AGENTS.md a la raíz | Bitácora | `docs/DTI.md` changelog v0.7 | 28/05 |
| 95 | Rodrigo Aspeti | PR-APORTES-001 — Prompt-contrato de generación de este informe de aportes (caso n = 1) | Prompt | `prompts/PR-APORTES-001.md` | 28/05 |

---

## 2. Resumen por integrante

| Integrante | Total de tareas | Categorías cubiertas (#) | Observación |
|------------|-----------------|--------------------------|--------------|
| Rodrigo Aspeti | 95 | 11 (`BRD`, `MRD`, `PRD`, `FSD`, `ADR`, `AGENTS`, `Skill`, `Rule`, `POC`, `Bitácora`, `Diagrama`, `Prompt`) | Único integrante del grupo G-EduSync; cubrió ciclo SDLC completo (negocio → producto → funcional → técnico → arquitectura → diagramas → POCs → catálogo de prompts → roadmap) |
| **Total grupo** | **95** | — | — |

> Distribución por categoría:
>
> | Categoría | Filas |
> |-----------|-------|
> | Prompt | 39 |
> | Diagrama | 10 |
> | Bitácora | 12 |
> | Skill | 9 |
> | ADR | 6 |
> | FSD | 5 |
> | POC | 4 |
> | BRD | 2 |
> | AGENTS | 3 |
> | MRD | 1 |
> | PRD | 1 |
> | Rule | 1 |
> | **Total** | **95** |
>
> (La fila "AGENTS" agrupa: DTI v0.1, roadmap v0.1 y AGENTS.md v0.2 — los tres archivos estructurales no clasificables como `Prompt`/`ADR`/`Diagrama` y producidos por el agente `docs-agent`.)

---

## 3. Cálculo del factor de aporte individual

> **Caso degenerado n = 1**: el factor es trivialmente `1.00`; este archivo funciona como **inventario auditable** del trabajo individual, no como ajuste de nota relativo entre integrantes.

Fórmula del módulo (idéntica en los 3 releases evaluables):

```
aporte_promedio_grupo = total_tareas_grupo / n_integrantes
factor_i              = clamp(tareas_i / aporte_promedio_grupo, 0.5, 1.1)
Nota_individual_i     = Nota_grupal × factor_i
```

Aplicación con `n = 1`:

- `aporte_promedio = 95 / 1 = 95.00`
- `factor_raw      = 95 / 95 = 1.00`
- `factor          = clamp(1.00, 0.5, 1.1) = 1.00`

### Aplicación

| Integrante | Tareas (de §2) | factor sin clamp = tareas / promedio | factor (clamp 0.5–1.1) |
|------------|----------------|--------------------------------------|------------------------|
| Rodrigo Aspeti | 95 | 95 / 95 = 1.00 | **1.00** |

> **Aporte promedio del grupo**: `95 / 1 = 95` tareas/persona.
> **Nota individual**: la columna se omite porque `nota_grupal = null` (el docente la asignará tras la auditoría).

### Ejemplo numérico (referencia)

Conservado de la plantilla para no perder el sentido pedagógico del instrumento:

Grupo de 4 integrantes, total 20 tareas, nota grupal = 80/100.

| Integrante | Tareas | Sin clamp | Con clamp | Nota individual |
|------------|--------|-----------|-----------|-----------------|
| Ana | 10 | 10/5 = 2.0 | 1.10 | 88 |
| Beto | 6 | 6/5 = 1.2 | 1.10 | 88 |
| Carla | 3 | 3/5 = 0.6 | 0.60 | 48 |
| Dani | 1 | 1/5 = 0.2 | 0.50 | 40 |

(Aporte promedio = 20/4 = 5 tareas.)

---

## 4. Reglas del grupo sobre qué cuenta como tarea

> Granularidad estándar recomendada por el módulo. El grupo puede afinar pero no relajar.

- **Un UC** (con flujo principal + alterno + Gherkin) = 1 tarea.
- **Un NFR ISO 25010** cuantificable con métrica + umbral + verificación = 1 tarea.
- **Un diagrama Mermaid** (`.mmd`) versionado y coherente con FSD = 1 tarea.
- **Una sección de un documento** del nivel `##` (BRD/MRD/PRD/FSD/DTI) con contenido sustantivo = 1 tarea.
- **Un ADR aceptado** = 1 tarea.
- **Una POC ejecutada con evidencia** = 1 tarea.
- **Un skill propio** (`docs/skills/<skill>.md`) accionable = 1 tarea.
- **Una cursor rule** (`.cursor/rules/<dominio>.mdc`) específica del dominio = 1 tarea.
- **Un prompt-contrato** con los 6 elementos + Invariants + Failure modes = 1 tarea.
- **Una user story** INVEST con criterios de aceptación = 1 tarea.
- **Una sección de bitácora** o **una sesión de demo** preparada y entregada = 1 tarea.
- **Una función o módulo no trivial de código** (con su prueba) = 1 tarea.
- **Co-autoría**: si dos personas hicieron la misma tarea de forma sustantiva, registrarla **dos veces** (una por autor) con la observación `co-autoría con <otro>`.

No cuentan: cambios cosméticos, correcciones tipográficas aisladas, commits de configuración sin contenido sustantivo, copiar/pegar de otra fuente sin adaptación.

---

## 5. Auditoría del docente (opcional)

> Espacio para que el docente registre observaciones, ajustes manuales o justificaciones aprobadas. Si está vacío, se aplica el cálculo automático de §3.

| Integrante | Factor calculado (§3) | Factor final aplicado | Justificación del ajuste |
|------------|-----------------------|------------------------|---------------------------|
| Rodrigo Aspeti | 1.00 | — | (a poblar por el docente) |

---

## 6. Checklist de cierre del release

- [x] §0 Metadatos completos con `n_integrantes` y branch del release.
- [x] §1 Cada tarea tiene Integrante, Categoría y Referencia verificable contra archivo+sección del repo.
- [x] §2 Suma de tareas por integrante = total del grupo (95 = 95).
- [x] §3 Aporte promedio y factor calculado para el único integrante (factor = 1.00 por caso degenerado n = 1).
- [x] §4 El grupo confirma que respetó la granularidad estándar (texto literal de `plantillas/APORTES_TEMPLATE.md §4`).
- [ ] Archivo commiteado en el branch del release `release/2.0.0` antes del cierre (pendiente del push final).

---

*Generado por el contrato `prompts/PR-APORTES-001.md` v0.1 | Agente: `docs-agent` | Modelo: claude-opus-4.7 | Fecha: 28/05/2026 | Trazabilidad: PROMPT_MAPPING v1.8, AGENTS.md v0.10, roadmap §5 L-09.*
