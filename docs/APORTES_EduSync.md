# Informe de Aportes Individuales — EduSync · Release 1.0.1

**Módulo 4 — Ingeniería de Software con IA Agéntica**
**Maestría en Ingeniería de Sistemas**

---

## 0. Metadatos del release

| Campo | Valor |
|-------|-------|
| **Producto** | EduSync — Plataforma SaaS de gestión académica multitenant |
| **Grupo** | G-EduSync |
| **Release evaluable** | `release/1.0.1` |
| **Sesiones cubiertas** | S5 (especificación), S6–S7 (arquitectura, skills, DTI), S8 (diseño hexagonal) |
| **Fecha de cierre** | 24/05/2026 |
| **Integrante(s)** | Rodrigo Aspeti — Dev Lead / PM (n = 1) |
| **Branch del release** | `release/1.0.1` |
| **Commit de cierre (HEAD)** | `pendiente de commit formal` |

---

## Introducción

El presente informe documenta los aportes realizados por el integrante del grupo G-EduSync durante el release `1.0.1`, que amplía el release anterior `1.0.0` (S5) incorporando la capa de arquitectura visual (diagramas C4), el ecosistema de skills agénticos y el Documento Técnico Inicial completo del producto.

Este release cubre íntegramente tres fases del proyecto:

- **Fase de especificación** (S5 — tareas #1–#32): cadena documental BRD → MRD → PRD → FSD → LFSD, decisiones arquitectónicas DA-01..DA-05 y ecosistema de 18 prompt-contratos.
- **Fase de arquitectura e ingeniería de agentes** (S6–S7 — tareas #33–#41): actualización de `AGENTS.md` v0.2, creación de 3 skills de agente (update-prompt-mapping, c4-edusync, dti-edusync), generación de los diagramas C4 Level 1 y Level 2, y producción del Documento Técnico Inicial (DTI) completo con 23 secciones.
- **Fase de diseño técnico hexagonal** (S8 — tareas #42–#45): diseño formal de la arquitectura hexagonal del core con identificación de 20 puertos IN, 16 puertos OUT, 32 adaptadores y 8 Aggregate Roots con invariantes verificables; diseño de los DTOs por capa hexagonal (Request/Command/Response/Event) para los 3 UCs críticos de EduSync con tablas de mapeo DTO ↔ Entidad y BR validadora; más el registro de los prompt-contratos `PR-HEX-001` y `PR-DTO-001` en el ecosistema (PROMPT_MAPPING v0.8 y v0.9).

EduSync es una plataforma SaaS B2B multitenant construida en Java 21, Spring Boot 3.3 y PostgreSQL 15, orientada a eliminar la "triple digitación manual" en las unidades educativas de Bolivia durante los cierres trimestrales del SIE del Ministerio de Educación. El trabajo documentado en este informe abarca **45 tareas** distribuidas en 15 categorías, todas verificables en el repositorio del proyecto.

---

## 1. Registro de tareas atribuidas

Las tareas se presentan en orden cronológico y lógico de ejecución. Cada entrada es trazable a un artefacto concreto del repositorio.

| # | Integrante | Tarea concreta | Categoría | Referencia | Fecha |
|---|------------|----------------|-----------|------------|-------|
| 1 | Rodrigo Aspeti | Definición de la arquitectura funcional del core EduSync: 10 casos de uso críticos (UC-01..UC-10) con actores, entradas, invariantes de negocio y salidas; análisis de restricciones del mercado boliviano y stack tecnológico | FSD | `docs/arquitectura_funcional_EduSync.md` §§ UC-01..UC-10 | 14/05 |
| 2 | Rodrigo Aspeti | DA-01: Decisión arquitectónica sobre estrategia de aislamiento multitenant en PostgreSQL — análisis de 3 alternativas (schema separado, discriminador RLS, BD separada) y selección de RLS con `tenant_id` | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-01 | 14/05 |
| 3 | Rodrigo Aspeti | DA-02: Decisión sobre parametrización de reglas normativas sin redespliegue — tabla `parametro_academico` en BD con alcance `tenant + periodo`, evitando hotfixes ante cambios ministeriales | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-02 | 14/05 |
| 4 | Rodrigo Aspeti | DA-03: Decisión sobre modelo de persistencia inmutable — `audit_log` explícito + Hibernate Envers + modelo append-only en UC-05 para cumplir normativa boliviana de trazabilidad | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-03 | 14/05 |
| 5 | Rodrigo Aspeti | DA-04: Decisión sobre estrategia de consolidación post-cierre — Spring Events internos (asíncronos) con diseño migrable a SQS, equilibrando experiencia de usuario y complejidad operativa | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-04 | 14/05 |
| 6 | Rodrigo Aspeti | DA-05: Decisión sobre resiliencia ante fallos del SIE — estado de exportación registro a registro con clave de idempotencia compuesta `rude + periodo_id` y reintentos asíncronos | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-05 | 14/05 |
| 7 | Rodrigo Aspeti | Cursor Rule de seguridad `.cursor/rules/seguridad.mdc` — estándar OWASP ASVS L2 para Java/Spring: prevención de secrets en código, PII en logs y credenciales hardcodeadas | Rule | `.cursor/rules/seguridad.mdc` | 14/05 |
| 8 | Rodrigo Aspeti | BRD EduSync V1: documento inicial de requerimientos de negocio con visión de producto, análisis de las tres personas UX primarias (Marcela, Wendy, Jeanneth) y requerimientos base del sistema | BRD | `docs/brd/BRD_EduSync_v1.md` §§ 1..8 | 14/05 |
| 9 | Rodrigo Aspeti | BRD EduSync V2: consolidado v2.0 que integra la arquitectura funcional y los diagramas de estado; incluye BR-001..BR-012, RB-01..RB-11, RACI, KPIs SMART, Business Model Canvas y Amazon PR-FAQ | BRD | `docs/brd/BRD_EduSync_v2.md` §§ 0..15 | 14/05 |
| 10 | Rodrigo Aspeti | Diagrama de estados del Docente en Mermaid `stateDiagram-v2` — 18 estados que cubren el ciclo completo de carga de notas: desde habilitación inicial hasta expiración de ventana y publicación de notas | Diagrama | `docs/diagrams/estados.cargarnotas.mmd` | 14/05 |
| 11 | Rodrigo Aspeti | Especificación formal del flujo de estados del Docente — descripción narrativa de cada estado, condiciones de transición, actores involucrados e invariantes de negocio aplicables | FSD | `docs/diagrams/estados_cargar_notas.md` | 14/05 |
| 12 | Rodrigo Aspeti | Diagrama de estados del Director en Mermaid `stateDiagram-v2` — 23 estados que cubren la gestión académica anual: creación de gestión, configuración de parámetros, apertura y cierre secuencial de trimestres | Diagrama | `docs/diagrams/estados_administracion.mmd` | 14/05 |
| 13 | Rodrigo Aspeti | Especificación formal del flujo de estados del Director — descripción de estados, validaciones de apertura secuencial, reglas de inmutabilidad de parámetros y condiciones de cierre institucional | FSD | `docs/diagrams/estados_administracion.md` | 14/05 |
| 14 | Rodrigo Aspeti | MRD EduSync v1.0: Market Requirements Document con TAM/SAM/SOM del mercado boliviano, 3 personas de mercado, 8 JTBD, análisis competitivo (5 alternativas), pricing por tiers, estrategia go-to-market y 10 MRD-N-* priorizados | MRD | `docs/mrd/MRD_EduSync.md` §§ 0..16 | 15/05 |
| 15 | Rodrigo Aspeti | PRD EduSync v1.0: Product Requirements Document con Constitution de 5 principios no negociables, 17 user stories INVEST con Gherkin, 6 épicas, priorización RICE top-10, 3 user journeys Mermaid, 20 PRD-REQ-* y 15 PRD-NFR-* | PRD | `docs/prd/PRD_EduSync.md` §§ 0..16 | 15/05 |
| 16 | Rodrigo Aspeti | FSD-UC-001: especificación completa del registro descentralizado de calificaciones por dimensión — flujo principal, 5 flujos alternativos, precondiciones, datos entrada/salida y criterios Gherkin | UC | `docs/fsd/FSD_EduSync.md` §4.1 | 15/05 |
| 17 | Rodrigo Aspeti | FSD-UC-003: especificación del motor de consolidación algorítmica — regla `floor`, estados PROVISIONAL y OFICIAL, promedio anual condicionado a 3 trimestres cerrados, con Gherkin de truncado | UC | `docs/fsd/FSD_EduSync.md` §4.2 | 15/05 |
| 18 | Rodrigo Aspeti | FSD-UC-004: especificación de exportación masiva al SIE por RUDE — idempotencia `rude + periodo_id`, filtro pre-exportación, reintentos asíncronos cada 5 min, con Gherkin de reanudación sin duplicados | UC | `docs/fsd/FSD_EduSync.md` §4.3 | 15/05 |
| 19 | Rodrigo Aspeti | FSD-UC-005: especificación de autorización jerárquica de modificación retroactiva — ventana temporal 1–72 h, modelo append-only, alerta 30 min antes del vencimiento y revocación automática, con Gherkin | UC | `docs/fsd/FSD_EduSync.md` §4.4 | 15/05 |
| 20 | Rodrigo Aspeti | FSD-UC-009: especificación de administración de periodos académicos — apertura secuencial (T2 bloqueado sin T1 cerrado), parámetros inmutables post-apertura, cierre con verificación al 100 %, con Gherkin | UC | `docs/fsd/FSD_EduSync.md` §4.5 | 15/05 |
| 21 | Rodrigo Aspeti | 16 requerimientos no funcionales (NFR-001..NFR-016) con métrica cuantificable, umbral de aceptación y método de verificación; categorías: rendimiento, disponibilidad, seguridad, usabilidad, accesibilidad, mantenibilidad, escalabilidad y auditabilidad | NFR | `docs/fsd/FSD_EduSync.md` §10 | 15/05 |
| 22 | Rodrigo Aspeti | 12 reglas de negocio (BR-001..BR-012) con tipo (validación / cálculo / política / arquitectura), origen normativo y lista de casos de uso afectados; incluye BR-003 (criterio `floor`), BR-008 (dominio exclusivo de cálculo) y BR-010 (inmutabilidad del audit_log) | FSD | `docs/fsd/FSD_EduSync.md` §5 | 15/05 |
| 23 | Rodrigo Aspeti | FSD EduSync v1.0 completo en modo FSD Clásico: modelo ER con 16 entidades y relaciones, diccionario de datos con 60+ atributos, 3 prompt-contratos internos (UC-001, UC-003, UC-005), 14 tasks del Spec Kit, plan de pruebas funcionales y glosario de 15 términos | FSD | `docs/fsd/FSD_EduSync.md` §§ 0..15 | 15/05 |
| 24 | Rodrigo Aspeti | LFSD EduSync v1.0: Low-Level Functional Specification Document con arquitectura hexagonal (Domain/Application/Infrastructure), estructura de paquetes Java, 5 diagramas de clases Mermaid, 15 contratos API REST con request/response completos, DDL lógico de 14 tablas con políticas RLS, 4 diagramas de secuencia, configuración Spring Security 6, AOP de auditoría, 2 schedulers y 16 tasks técnicas con estimaciones | FSD | `docs/LFSD-EduSync.md` §§ 0..20 | 15/05 |
| 25 | Rodrigo Aspeti | PROMPT_MAPPING.md v0.5: catálogo completo de 18 prompt-contratos del proyecto (PR-ARCH-001..PR-LFSD-001) con índice, flowchart de flujo de información entre prompts, matriz de responsabilidades por agente, invariantes globales del ecosistema, trazabilidad completa y 5 versiones documentadas | Prompt | `docs/PROMPT_MAPPING.md` v0.5 | 15/05 |
| 26 | Rodrigo Aspeti | Prompt-contrato PR-ARCH-001: especificación del prompt para la arquitectura funcional, con los 6 elementos canónicos (Role/Task/Context/Reasoning/Stop condition/Output) más Invariants y Failure modes | Prompt | `docs/PROMPT_MAPPING.md` §Prompts/PR-ARCH-001 | 14/05 |
| 27 | Rodrigo Aspeti | Prompt-contrato PR-BRD-002: especificación del prompt para el BRD V2 consolidado, cubriendo Business Model Canvas, Amazon PR-FAQ y 12 requerimientos de negocio | Prompt | `docs/PROMPT_MAPPING.md` §Prompts/PR-BRD-002 | 14/05 |
| 28 | Rodrigo Aspeti | Prompt-contrato PR-MRD-001: especificación del prompt para el MRD con análisis TAM/SAM/SOM, posicionamiento competitivo y 4 failure modes específicos de mercado | Prompt | `docs/PROMPT_MAPPING.md` §Prompts/PR-MRD-001 | 15/05 |
| 29 | Rodrigo Aspeti | Prompt-contrato PR-PRD-001: especificación del prompt para el PRD con criterios INVEST, Gherkin y cálculo RICE, incluyendo trazabilidad PRD-REQ → BR → MRD-N | Prompt | `docs/PROMPT_MAPPING.md` §Prompts/PR-PRD-001 | 15/05 |
| 30 | Rodrigo Aspeti | Prompt-contrato PR-FSD-001: especificación del prompt para el FSD Clásico con 5 FSD-UC, modelo ER de 16 entidades y prompt-contratos internos de calificación, consolidación y corrección retroactiva | Prompt | `docs/PROMPT_MAPPING.md` §Prompts/PR-FSD-001 | 15/05 |
| 31 | Rodrigo Aspeti | Prompt-contrato PR-LFSD-001: especificación del prompt para el LFSD con pseudoalgoritmos, DDL completo, contratos API, Spring Security 6 y 5 failure modes técnicos (E_DOMINIO_SIN_PSEUDOCODIGO, E_API_SIN_ERRORES, E_DDL_SIN_RLS, etc.) | Prompt | `docs/PROMPT_MAPPING.md` §Prompts/PR-LFSD-001 | 15/05 |
| 32 | Rodrigo Aspeti | Análisis comparativo AI-SDLC Agéntico vs. SDLC tradicional (Cascada/Ágil) para EduSync: tabla comparativa de 7 fases (descubrimiento, análisis, diseño, implementación, prueba, despliegue, operación) con columnas humano vs. agente, más 3 riesgos críticos identificados | Bitácora | `docs/diagrams/ai-sdlc.mmd` | 14/05 |
| 33 | Rodrigo Aspeti | `AGENTS.md` v0.2: actualización integral del documento de agentes — corrección de 6 rutas de archivos rotas, incorporación de 15 nuevos artefactos (BRD v2, MRD, PRD, FSD, LFSD, diagramas, PROMPT_MAPPING, APORTES), expansión de 4 a 6 agentes activos (`arch-agent`, `qa-agent`, `process-agent`, `compliance-agent`), 4 golden tests de zero-tolerance y sincronización de estructura del repositorio | AGENTS | `AGENTS.md` v0.2 (418 líneas) | 17/05 |
| 34 | Rodrigo Aspeti | Skill `update-prompt-mapping` para Cursor y Claude Code: guía de 7 pasos con procedimiento completo para registrar prompt-contratos en `PROMPT_MAPPING.md`; incluye plantillas copy-paste exactas del proyecto, tabla de IDs válidos, checklist de validación, reference.md con ejemplos reales y modos de fallo conocidos | Skill | `.cursor/skills/update-prompt-mapping/SKILL.md` + `.claude/skills/update-prompt-mapping/SKILL.md` | 17/05 |
| 35 | Rodrigo Aspeti | PROMPT_MAPPING.md v0.6: registro de PR-ARCH-002 (AGENTS.md v0.2) y PR-SKILL-001 (skill update-prompt-mapping); área SKILL añadida al header; nodos AGENTS y SKILL en el flowchart Mermaid; matriz docs-agent actualizada; trazabilidad ampliada a 20 prompt-contratos | Prompt | `docs/PROMPT_MAPPING.md` v0.6 | 17/05 |
| 36 | Rodrigo Aspeti | Skill `c4-edusync` para Cursor y Claude Code: guía especializada para generar diagramas C4 (Level 1, 2 y 3) de EduSync; incluye mapa de trazabilidad FSD-UC ↔ contenedor C4, anti-patrones EduSync, procedimiento de 4 pasos y reference.md con bloques Mermaid `C4Context`, `C4Container` y `flowchart` listos para usar | Skill | `.cursor/skills/c4-edusync/SKILL.md` + `.claude/skills/c4-edusync/SKILL.md` | 17/05 |
| 37 | Rodrigo Aspeti | C4 Level 1 — Diagrama de Contexto del Sistema: actores Director/Docente/Secretaria con sus personas reales (Jeanneth, Marcela, Wendy), sistema EduSync con stack completo, y sistemas externos SIE (Ley 070) y AWS KMS (NFR-007); validado sin caracteres Unicode en labels (IG-10) | Diagrama | `docs/diagrams/c4_level1.mmd` | 17/05 |
| 38 | Rodrigo Aspeti | C4 Level 2 — Diagrama de Contenedores: 7 contenedores internos (Angular SPA, API Gateway, Domain Layer, PostgreSQL 15, Event Bus, SIE Adapter, Scheduler) con tecnologías, descripciones y DA/BR citados; tabla de trazabilidad FSD-UC ↔ contenedor ↔ DA para los 5 UCs críticos | Diagrama | `docs/diagrams/c4_level2.mmd` | 17/05 |
| 39 | Rodrigo Aspeti | Skill `dti-edusync` para Cursor y Claude Code: guía de 5 pasos adaptada a EduSync para poblar y mantener el DTI; incluye tabla de mapeo de 25 secciones DTI con datos reales del proyecto (stack, agentes, golden tests, bounded contexts, eventos, ADRs), checklist de 12 ítems y anti-patrones EduSync específicos | Skill | `.cursor/skills/dti-edusync/SKILL.md` + `.claude/skills/dti-edusync/SKILL.md` | 17/05 |
| 40 | Rodrigo Aspeti | DTI EduSync v0.1: Documento Técnico Inicial completo con 23 secciones (§0–§23, 883 líneas): frontmatter YAML con audiencia dual, tabla de 6 agentes SDLC, visión con North Star, C4 L1/L2 embebidos, C4 L3 flowchart del API Gateway, secuencia FSD-UC-001, 5 bounded contexts, 9 entidades, 6 DTOs, 11 puertos, 7 adaptadores, catálogo de eventos, mapa AWS, 16 NFRs, 2 POCs con criterio medible, STRIDE resumido, 5 ADRs provisionales (DA-01..DA-05), 4 golden tests CI y checklist 24/27 completado | FSD | `docs/DTI.md` v0.1 (883 líneas) | 17/05 |
| 41 | Rodrigo Aspeti | PROMPT_MAPPING.md v0.7: registro de 5 nuevos prompt-contratos (PR-SKILL-002, PR-C4-001, PR-C4-002, PR-SKILL-003, PR-DTI-001); áreas C4 y DTI añadidas; subgraph ARQUITECTURA en flowchart; matrices docs-agent y arch-agent actualizadas; trazabilidad ampliada a 25 prompt-contratos | Prompt | `docs/PROMPT_MAPPING.md` v0.7 | 17/05 |
| 42 | Rodrigo Aspeti | Arquitectura Hexagonal del core EduSync v0.1: diseño técnico de bajo nivel con 20 puertos IN (uno por FSD-UC), 16 puertos OUT (persistencia, mensajería, terceros, seguridad, tiempo), 32 adaptadores (15 IN + 17 OUT) con tecnología y ubicación, y 8 Aggregate Roots (GestionAcademica, PeriodoAcademico, Estudiante, Calificacion, Centralizador, ExportacionSIE, CorreccionRetroactiva, AuditLogEntry) con invariantes verificables BR-001..BR-012; estructura de paquetes Java completa, materialización DA-01..DA-05, catálogo de 4 eventos de dominio y checklist de implementación para dev-agent | Arquitectura | `docs/arquitectura_hexagonal_EduSync.md` v0.1 (283 líneas) | 24/05 |
| 43 | Rodrigo Aspeti | PROMPT_MAPPING.md v0.8: registro del prompt-contrato PR-HEX-001 (arquitectura hexagonal del core EduSync); área `HEX` añadida al header; nodo HEX en flowchart Mermaid con aristas desde FSD, LFSD, ARCH y PRD; matriz `arch-agent` actualizada (responsabilidad ampliada a diseño hexagonal); trazabilidad ampliada a 26 prompt-contratos | Prompt | `docs/PROMPT_MAPPING.md` v0.8 | 24/05 |
| 44 | Rodrigo Aspeti | DTOs por capa hexagonal EduSync v0.1: diseño técnico vinculante para los 3 UCs críticos (FSD-UC-001, UC-003, UC-005) con 4 Request DTOs (Java Records + Bean Validation), 4 Commands puros (sin Spring/Jakarta — DA-02), 3 Response DTOs (sin exponer `tenant_id`/`actor_id`), 5 Domain Events inmutables (`CalificacionRegistradaEvent`, `MateriaCerradaEvent`, `CentralizadorOficialEvent`, `AutorizacionEmitidaEvent`, `VentanaExpiradaEvent`), 5 enums de dominio (`Dimension`, `TipoCalificacion`, `EstadoCentralizador`, `AlcanceCorreccion`, `EstadoAutorizacion`) y 3 tablas DTO ↔ Entidad mapeando cada campo a su BR (BR-001..BR-011) y capa de validación (Jakarta vs. Domain Service); checklist de implementación para `dev-agent` y verificación contra invariantes hexagonales | Arquitectura | `docs/dtos_EduSync.md` v0.1 (445 líneas) | 24/05 |
| 45 | Rodrigo Aspeti | PROMPT_MAPPING.md v0.9: registro del prompt-contrato PR-DTO-001 (DTOs por capa hexagonal para FSD-UC-001/003/005); área `DTO` añadida al header; nodo DTO en flowchart Mermaid con aristas desde FSD, HEX y AGENTS; matriz `dev-agent` actualizada (responsabilidad ampliada a generación de DTOs hexagonales con mapeo DTO ↔ Entidad); trazabilidad ampliada a 27 prompt-contratos | Prompt | `docs/PROMPT_MAPPING.md` v0.9 | 24/05 |

**Categorías presentes en este release:** `ADR` · `AGENTS` · `Arquitectura` · `BRD` · `Bitácora` · `Diagrama` · `FSD` · `MRD` · `NFR` · `PRD` · `Prompt` · `Rule` · `Skill` · `UC` · `Otro`

---

## 2. Resumen de aportes por integrante

El siguiente cuadro consolida el total de tareas registradas en §1, agrupadas por integrante, como base para el cálculo del factor de aporte.

| Integrante | Total de tareas | Categorías cubiertas | Observación |
|------------|-----------------|----------------------|-------------|
| Rodrigo Aspeti | 45 | 15 | Dev Lead / PM del proyecto. Responsable de la totalidad de la cadena documental de especificación (BRD→MRD→PRD→FSD→LFSD→DTI→Arquitectura Hexagonal→DTOs), las 5 decisiones arquitectónicas, los 5 UCs críticos con Gherkin, el ecosistema de 27 prompt-contratos, 3 skills agénticos, diagramas C4 Level 1 y Level 2, AGENTS.md v0.2 y el diseño técnico de bajo nivel hexagonal con 8 Aggregate Roots, invariantes verificables y los 4 Request DTOs + 4 Commands + 3 Response DTOs + 5 Domain Events de los 3 UCs críticos. |
| **Total grupo** | **45** | — | — |

### Distribución de tareas por categoría

| Categoría | Cantidad | Artefactos principales |
|-----------|----------|----------------------|
| Prompt | 10 | PROMPT_MAPPING.md (v0.5→v0.9) + PR-ARCH-001, PR-BRD-002, PR-MRD-001, PR-PRD-001, PR-FSD-001, PR-LFSD-001, v0.6, v0.7, v0.8 (PR-HEX-001), **v0.9 (PR-DTO-001)** |
| FSD | 8 | Arquitectura funcional, specs estados Docente/Director, reglas de negocio, FSD completo, LFSD completo, DTI completo |
| ADR | 5 | DA-01..DA-05 (multitenancy, parametrización, inmutabilidad, consolidación, SIE) |
| UC | 5 | FSD-UC-001, UC-003, UC-004, UC-005, UC-009 |
| Diagrama | 4 | Estados Docente (18 estados), estados Director (23 estados), C4 Level 1, C4 Level 2 |
| Skill | 3 | update-prompt-mapping, c4-edusync, dti-edusync |
| Arquitectura | 2 | Arquitectura Hexagonal del core v0.1 (20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 Aggregate Roots) · **DTOs por capa hexagonal v0.1** (4 Request DTOs, 4 Commands, 3 Response DTOs, 5 Domain Events, 5 enums, 3 tablas DTO ↔ Entidad) |
| BRD | 2 | BRD EduSync V1 y V2 |
| NFR | 1 | NFR-001..NFR-016 (16 requerimientos no funcionales) |
| MRD | 1 | MRD EduSync v1.0 |
| PRD | 1 | PRD EduSync v1.0 |
| AGENTS | 1 | AGENTS.md v0.2 (6 agentes, 4 golden tests) |
| Rule | 1 | `.cursor/rules/seguridad.mdc` (OWASP ASVS L2) |
| Bitácora | 1 | Análisis comparativo AI-SDLC |

---

## 3. Cálculo del factor de aporte individual

### Fórmula aplicada

El cálculo sigue la fórmula estandarizada del módulo, sin modificación:

```
aporte_promedio_grupo = total_tareas_grupo / n_integrantes
factor_i              = clamp(tareas_i / aporte_promedio_grupo, 0.5, 1.1)
Nota_individual_i     = Nota_grupal × factor_i
```

- **Piso** `0.5`: protege al integrante que aporta muy poco, garantizándole al menos el 50 % de la nota grupal.
- **Techo** `1.1`: permite al integrante que sobrepasa el promedio obtener hasta un 10 % adicional sobre la nota grupal.
- **Grupo con un único integrante**: el aporte promedio es igual al total de tareas del integrante, resultando siempre en `factor = 1.0` antes del clamp. El clamp lo deja en `1.0`.

### Datos del cálculo

> Aporte promedio del grupo: **45 tareas / 1 integrante = 45.00 tareas/persona**

| Integrante | Tareas (§2) | Factor sin clamp | Factor (clamp 0.5–1.1) | Nota individual |
|------------|-------------|------------------|------------------------|-----------------|
| Rodrigo Aspeti | 45 | 45 / 45.00 = **1.00** | **1.00** | Nota_grupal × 1.00 |

### Interpretación

Dado que el grupo está compuesto por un único integrante que realizó la totalidad de las 45 tareas documentadas, el factor de aporte resulta en `1.00`. La nota individual es igual a la nota grupal, sin ajuste al alza ni a la baja. El cálculo es matemáticamente consistente: no existe diferencia entre el aporte individual y el promedio del grupo cuando n = 1.

---

## 4. Criterios de granularidad aplicados

El grupo adoptó íntegramente los criterios estándar del módulo, con las siguientes aclaraciones y adaptaciones documentadas:

**Criterios del módulo aplicados sin modificación:**

- **Un caso de uso** con flujo principal + alterno + Gherkin = 1 tarea. Aplicado en tareas #16–#20 (FSD-UC-001, UC-003, UC-004, UC-005, UC-009).
- **Un NFR ISO 25010** cuantificable con métrica + umbral + verificación = 1 tarea. Los 16 NFRs se contabilizaron como 1 tarea porque corresponden a una única sección `##` de un mismo documento con contenido homogéneo (tarea #21).
- **Un diagrama Mermaid** versionado y coherente con el FSD = 1 tarea. Aplicado en tareas #10, #12 (estados) y #37, #38 (C4 Level 1 y Level 2).
- **Una sección de nivel `##`** en BRD/MRD/PRD/FSD/DTI con contenido sustantivo = 1 tarea. Los documentos completos (BRD v1, BRD v2, MRD, PRD, FSD, LFSD, DTI) se contabilizaron cada uno como 1 tarea.
- **Un ADR aceptado** = 1 tarea. Aplicado en tareas #2–#6 (DA-01..DA-05).
- **Una cursor rule** específica del dominio = 1 tarea. Aplicado en tarea #7.
- **Un prompt-contrato** con los 6 elementos + Invariants + Failure modes = 1 tarea. Aplicado en tareas #26–#31 (6 contratos individuales), tarea #25 (PROMPT_MAPPING v0.5 como catálogo), tarea #35 (v0.6), tarea #41 (v0.7), tarea #43 (v0.8 — PR-HEX-001) y tarea #45 (v0.9 — PR-DTO-001).
- **Un skill propio** accionable (SKILL.md con procedimiento, checklist y reference.md) = 1 tarea. Aplicado en tareas #34 (update-prompt-mapping), #36 (c4-edusync) y #39 (dti-edusync).
- **Una sección de bitácora** = 1 tarea. Aplicado en tarea #32 (análisis AI-SDLC).

**Aclaraciones específicas del grupo:**

1. **FSD vs. LFSD vs. DTI**: se contabilizan como tareas separadas (#23, #24 y #40) porque son documentos con propósitos completamente diferenciados: el FSD especifica *qué* hace el sistema; el LFSD especifica *cómo* está diseñado internamente; el DTI es el contrato técnico inicial para humanos y agentes IA con diagramas C4, POCs, ADRs, NFRs y evaluación de guardrails.

2. **PROMPT_MAPPING.md**: se contabiliza como 1 tarea por versión significativa (#25 v0.5, #35 v0.6, #41 v0.7, #43 v0.8, #45 v0.9) porque cada actualización implica 7 secciones modificadas simultáneamente (cabecera, índice, Mermaid, matriz de agentes, contrato, trazabilidad, historial) y no es un simple cambio de una sección. Los contratos individuales (#26–#31) son verificables de forma independiente por sección.

6. **Diseño técnico hexagonal y DTOs**: las tareas #42 (Arquitectura Hexagonal) y #44 (DTOs por capa) se contabilizan como categoría `Arquitectura` separada porque cada una produce un artefacto técnico vinculante distinto: la #42 define la topología (puertos, adaptadores, Aggregate Roots e invariantes); la #44 define los **contratos de datos** (Java Records con Bean Validation y mapeo explícito DTO ↔ Entidad ↔ BR) que serán consumidos por `dev-agent` y `qa-agent` para implementación y golden tests. Son artefactos con propósitos no superpuestos y entregables independientes.

3. **Skills de agente** (#34, #36, #39): se contabilizan como tareas de categoría `Skill` independientes porque cada skill produce 4 artefactos distintos (SKILL.md Cursor, reference.md Cursor, SKILL.md Claude, reference.md Claude), define un protocolo de procedimiento específico al proyecto y es accionable de forma autónoma por un agente.

4. **AGENTS.md v0.2** (#33): se contabiliza como tarea de categoría `AGENTS` porque implica la revisión completa del contrato de agentes del proyecto (418 líneas): actualización de paths, incorporación de 15 artefactos nuevos, expansión a 6 agentes, definición de 4 golden tests de zero-tolerance y actualización del workflow estándar.

5. **Especificaciones Markdown de diagramas** (#11, #13): se contabilizan como tareas de categoría `FSD` independientes de los archivos `.mmd` (#10, #12) porque producen un artefacto de texto diferente (descripción narrativa de estados, transiciones y actores) que no se genera automáticamente desde el diagrama.

**No se contabilizaron:** correcciones tipográficas y de formato menores sobre documentos ya creados, ajustes de indentación en archivos Mermaid para compatibilidad de parsers, correcciones de encoding en scripts PowerShell de actualización del PROMPT_MAPPING.

---

## 5. Espacio para auditoría del docente

> Esta sección queda reservada para que el docente registre ajustes manuales al factor calculado en §3, si los hubiere. En ausencia de observaciones del docente, se aplica directamente el factor calculado automáticamente.

| Integrante | Factor calculado (§3) | Factor final aplicado | Justificación del ajuste |
|------------|-----------------------|------------------------|---------------------------|
| Rodrigo Aspeti | 1.00 | *(sin ajuste — a criterio del docente)* | *(ninguno; factor calculado se aplica directamente sobre las 45 tareas verificables)* |

---

## 6. Checklist de cierre del release

Los siguientes ítems verifican la completitud y validez del presente informe antes de su commit en el repositorio:

- [x] **§0** — Metadatos completos: producto, grupo, release `1.0.1`, sesiones S5+S6–S8, fecha 24/05/2026, integrante y branch declarados.
- [x] **§1** — Las 45 tareas registradas incluyen integrante, categoría admitida y referencia verificable (archivo + sección).
- [x] **§2** — La suma de tareas por integrante (45) coincide con el total del grupo (45). Tabla de distribución por 15 categorías incluida.
- [x] **§3** — Aporte promedio (45.00 tareas/persona) y factor (1.00) calculados y documentados con su interpretación.
- [x] **§4** — Los criterios estándar del módulo fueron aplicados sin relajación; las 6 adaptaciones específicas del grupo están documentadas explícitamente con justificación.
- [x] **Tareas S6–S7** — Las 9 tareas (#33–#41) registradas con categoría `AGENTS`, `Skill`, `Diagrama`, `FSD` y `Prompt`.
- [x] **Tareas S8** — Las 4 tareas (#42–#45) de diseño hexagonal, DTOs por capa hexagonal y registro de PR-HEX-001 + PR-DTO-001 incluidas en categorías `Arquitectura` y `Prompt`.
- [ ] **Commit pendiente** — El archivo debe ser commiteado en el branch `release/1.0.1` con el hash de HEAD actualizado en §0 antes del cierre formal del release.
