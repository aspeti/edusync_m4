# Informe de Aportes Individuales — EduSync · Release 1.0.0

**Módulo 4 — Ingeniería de Software con IA Agéntica**
**Maestría en Ingeniería de Sistemas**

---

## 0. Metadatos del release

| Campo | Valor |
|-------|-------|
| **Producto** | EduSync — Plataforma SaaS de gestión académica multitenant |
| **Grupo** | G-EduSync |
| **Release evaluable** | `release/1.0.0` |
| **Sesión asociada** | S5 |
| **Fecha de cierre** | 15/05/2026 |
| **Integrante(s)** | Rodrigo Aspeti — Dev Lead / PM (n = 1) |
| **Branch del release** | `release/1.0.0` |
| **Commit de cierre (HEAD)** | `pendiente de commit formal` |

---

## Introducción

El presente informe documenta los aportes realizados por el integrante del grupo G-EduSync durante el release `1.0.0`, correspondiente a la Sesión S5 del Módulo 4. El release cubre íntegramente la **fase de especificación** del producto EduSync: desde la visión de negocio hasta el diseño técnico de bajo nivel, incluyendo la cadena documental completa (BRD → MRD → PRD → FSD → LFSD) y el ecosistema de prompts-contrato que gobierna el trabajo de los agentes de IA en el proyecto.

EduSync es una plataforma SaaS B2B multitenant construida en Java 21, Spring Boot 3.3 y PostgreSQL 15, orientada a eliminar la "triple digitación manual" que afecta a las unidades educativas de Bolivia durante los cierres trimestrales del Sistema de Información Educativa (SIE) del Ministerio de Educación. El equipo de desarrollo está compuesto por un desarrollador principal (Rodrigo Aspeti) asistido por agentes de IA especializados, siguiendo una metodología de AI-SDLC Agéntico.

El trabajo documentado en este informe abarca **32 tareas** distribuidas en 11 categorías, todas verificables en el repositorio del proyecto mediante referencias directas a archivos y secciones específicas.

---

## 1. Registro de tareas atribuidas

Las tareas se presentan en orden cronológico y lógico de ejecución, reflejando la secuencia real del proceso de especificación. Cada entrada es trazable a un artefacto concreto del repositorio.

| # | Integrante | Tarea concreta | Categoría | Referencia | Fecha |
|---|------------|----------------|-----------|------------|-------|
| 1 | Rodrigo Aspeti | Definición de la arquitectura funcional del core EduSync: 10 casos de uso críticos (UC-01..UC-10) con actores, entradas, invariantes de negocio y salidas; análisis de restricciones del mercado boliviano y stack tecnológico | FSD | `docs/arquitectura_funcional_EduSync.md` §§ UC-01..UC-10 | 14/05 |
| 2 | Rodrigo Aspeti | DA-01: Decisión arquitectónica sobre estrategia de aislamiento multitenant en PostgreSQL — análisis de 3 alternativas (schema separado, discriminador RLS, BD separada) y selección de RLS con `tenant_id` | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-01 | 14/05 |
| 3 | Rodrigo Aspeti | DA-02: Decisión sobre parametrización de reglas normativas sin redespliegue — tabla `parametro_academico` en BD con alcance `tenant + periodo`, evitando hotfixes ante cambios ministeriales | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-02 | 14/05 |
| 4 | Rodrigo Aspeti | DA-03: Decisión sobre modelo de persistencia inmutable — `audit_log` explícito + Hibernate Envers + modelo append-only en UC-05 para cumplir normativa boliviana de trazabilidad | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-03 | 14/05 |
| 5 | Rodrigo Aspeti | DA-04: Decisión sobre estrategia de consolidación post-cierre — Spring Events internos (asíncronos) con diseño migrable a SQS, equilibrando experiencia de usuario y complejidad operativa | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-04 | 14/05 |
| 6 | Rodrigo Aspeti | DA-05: Decisión sobre resiliencia ante fallos del SIE — estado de exportación registro a registro con clave de idempotencia compuesta `rude + periodo_id` y reintentos asíncronos | ADR | `docs/arquitectura_funcional_EduSync.md` §DA-05 | 14/05 |
| 7 | Rodrigo Aspeti | Cursor Rule de seguridad `.cursor/rules/seguridad.mdc` — estándar OWASP ASVS L2 para Java/Spring: prevención de secrets en código, PII en logs y credenciales hardcodeadas | Rule | `.cursor/rules/seguridad.mdc` | 14/05 |
| 8 | Rodrigo Aspeti | BRD EduSync V1: documento inicial de requerimientos de negocio con visión de producto, análisis de las tres personas UX primarias (Marcela, Wendy, Jeanneth) y requerimientos base del sistema | BRD | `docs/BRD_EduSync_V1.md` §§ 1..8 | 14/05 |
| 9 | Rodrigo Aspeti | BRD EduSync V2: consolidado v2.0 que integra la arquitectura funcional y los diagramas de estado; incluye BR-001..BR-012, RB-01..RB-11, RACI, KPIs SMART, Business Model Canvas y Amazon PR-FAQ | BRD | `docs/BRD_EduSync_V2.md` §§ 0..15 | 14/05 |
| 10 | Rodrigo Aspeti | Diagrama de estados del Docente en Mermaid `stateDiagram-v2` — 18 estados que cubren el ciclo completo de carga de notas: desde habilitación inicial hasta expiración de ventana y publicación de notas | Diagrama | `docs/diagramas/estados.cargarnotas.mmd` | 14/05 |
| 11 | Rodrigo Aspeti | Especificación formal del flujo de estados del Docente — descripción narrativa de cada estado, condiciones de transición, actores involucrados e invariantes de negocio aplicables | FSD | `docs/diagramas/estados_cargar_notas.md` | 14/05 |
| 12 | Rodrigo Aspeti | Diagrama de estados del Director en Mermaid `stateDiagram-v2` — 23 estados que cubren la gestión académica anual: creación de gestión, configuración de parámetros, apertura y cierre secuencial de trimestres | Diagrama | `docs/diagramas/estados_administracion.mmd` | 14/05 |
| 13 | Rodrigo Aspeti | Especificación formal del flujo de estados del Director — descripción de estados, validaciones de apertura secuencial, reglas de inmutabilidad de parámetros y condiciones de cierre institucional | FSD | `docs/diagramas/estados_administracion.md` | 14/05 |
| 14 | Rodrigo Aspeti | MRD EduSync v1.0: Market Requirements Document con TAM/SAM/SOM del mercado boliviano, 3 personas de mercado, 8 JTBD, análisis competitivo (5 alternativas), pricing por tiers, estrategia go-to-market y 10 MRD-N-* priorizados | MRD | `docs/MRD_EduSync.md` §§ 0..16 | 15/05 |
| 15 | Rodrigo Aspeti | PRD EduSync v1.0: Product Requirements Document con Constitution de 5 principios no negociables, 17 user stories INVEST con Gherkin, 6 épicas, priorización RICE top-10, 3 user journeys Mermaid, 20 PRD-REQ-* y 15 PRD-NFR-* | PRD | `docs/PRD_EduSync.md` §§ 0..16 | 15/05 |
| 16 | Rodrigo Aspeti | FSD-UC-001: especificación completa del registro descentralizado de calificaciones por dimensión — flujo principal, 5 flujos alternativos, precondiciones, datos entrada/salida y criterios Gherkin | UC | `docs/FSD_EduSync.md` §4.1 | 15/05 |
| 17 | Rodrigo Aspeti | FSD-UC-003: especificación del motor de consolidación algorítmica — regla `floor`, estados PROVISIONAL y OFICIAL, promedio anual condicionado a 3 trimestres cerrados, con Gherkin de truncado | UC | `docs/FSD_EduSync.md` §4.2 | 15/05 |
| 18 | Rodrigo Aspeti | FSD-UC-004: especificación de exportación masiva al SIE por RUDE — idempotencia `rude + periodo_id`, filtro pre-exportación, reintentos asíncronos cada 5 min, con Gherkin de reanudación sin duplicados | UC | `docs/FSD_EduSync.md` §4.3 | 15/05 |
| 19 | Rodrigo Aspeti | FSD-UC-005: especificación de autorización jerárquica de modificación retroactiva — ventana temporal 1–72 h, modelo append-only, alerta 30 min antes del vencimiento y revocación automática, con Gherkin | UC | `docs/FSD_EduSync.md` §4.4 | 15/05 |
| 20 | Rodrigo Aspeti | FSD-UC-009: especificación de administración de periodos académicos — apertura secuencial (T2 bloqueado sin T1 cerrado), parámetros inmutables post-apertura, cierre con verificación al 100%, con Gherkin | UC | `docs/FSD_EduSync.md` §4.5 | 15/05 |
| 21 | Rodrigo Aspeti | 16 requerimientos no funcionales (NFR-001..NFR-016) con métrica cuantificable, umbral de aceptación y método de verificación; categorías: rendimiento, disponibilidad, seguridad, usabilidad, accesibilidad, mantenibilidad, escalabilidad y auditabilidad | NFR | `docs/FSD_EduSync.md` §10 | 15/05 |
| 22 | Rodrigo Aspeti | 12 reglas de negocio (BR-001..BR-012) con tipo (validación / cálculo / política / arquitectura), origen normativo y lista de casos de uso afectados; incluye BR-003 (criterio `floor`), BR-008 (dominio exclusivo de cálculo) y BR-010 (inmutabilidad del audit_log) | FSD | `docs/FSD_EduSync.md` §5 | 15/05 |
| 23 | Rodrigo Aspeti | FSD EduSync v1.0 completo en modo FSD Clásico: modelo ER con 16 entidades y relaciones, diccionario de datos con 60+ atributos, 3 prompt-contratos internos (UC-001, UC-003, UC-005), 14 tasks del Spec Kit, plan de pruebas funcionales y glosario de 15 términos | FSD | `docs/FSD_EduSync.md` §§ 0..15 | 15/05 |
| 24 | Rodrigo Aspeti | LFSD EduSync v1.0: Low-Level Functional Specification Document con arquitectura hexagonal (Domain/Application/Infrastructure), estructura de paquetes Java, 5 diagramas de clases Mermaid, 15 contratos API REST con request/response completos, DDL lógico de 14 tablas con políticas RLS, 4 diagramas de secuencia, configuración Spring Security 6, AOP de auditoría, 2 schedulers y 16 tasks técnicas con estimaciones | FSD | `docs/lfsd/LFSD-EduSync.md` §§ 0..20 | 15/05 |
| 25 | Rodrigo Aspeti | PROMPT_MAPPING.md v0.5: catálogo completo de 18 prompt-contratos del proyecto (PR-ARCH-001..PR-LFSD-001) con índice, flowchart de flujo de información entre prompts, matriz de responsabilidades por agente, invariantes globales del ecosistema, trazabilidad completa y 5 versiones documentadas | Prompt | `docs/PROMPT_MAPPING.md` v0.5 | 15/05 |
| 26 | Rodrigo Aspeti | Prompt-contrato PR-ARCH-001: especificación del prompt para la arquitectura funcional, con los 6 elementos canónicos (Role/Task/Context/Reasoning/Stop condition/Output) más Invariants y Failure modes | Prompt | `docs/PROMPT_MAPPING.md` §Prompts / PR-ARCH-001 | 14/05 |
| 27 | Rodrigo Aspeti | Prompt-contrato PR-BRD-002: especificación del prompt para el BRD V2 consolidado, cubriendo Business Model Canvas, Amazon PR-FAQ y 12 requerimientos de negocio | Prompt | `docs/PROMPT_MAPPING.md` §Prompts / PR-BRD-002 | 14/05 |
| 28 | Rodrigo Aspeti | Prompt-contrato PR-MRD-001: especificación del prompt para el MRD con análisis TAM/SAM/SOM, posicionamiento competitivo y 4 failure modes específicos de mercado | Prompt | `docs/PROMPT_MAPPING.md` §Prompts / PR-MRD-001 | 15/05 |
| 29 | Rodrigo Aspeti | Prompt-contrato PR-PRD-001: especificación del prompt para el PRD con criterios INVEST, Gherkin y cálculo RICE, incluyendo trazabilidad PRD-REQ → BR → MRD-N | Prompt | `docs/PROMPT_MAPPING.md` §Prompts / PR-PRD-001 | 15/05 |
| 30 | Rodrigo Aspeti | Prompt-contrato PR-FSD-001: especificación del prompt para el FSD Clásico con 5 FSD-UC, modelo ER de 16 entidades y prompt-contratos internos de calificación, consolidación y corrección retroactiva | Prompt | `docs/PROMPT_MAPPING.md` §Prompts / PR-FSD-001 | 15/05 |
| 31 | Rodrigo Aspeti | Prompt-contrato PR-LFSD-001: especificación del prompt para el LFSD con pseudoalgoritmos, DDL completo, contratos API, Spring Security 6 y 5 failure modes técnicos (E_DOMINIO_SIN_PSEUDOCODIGO, E_API_SIN_ERRORES, E_DDL_SIN_RLS, etc.) | Prompt | `docs/PROMPT_MAPPING.md` §Prompts / PR-LFSD-001 | 15/05 |
| 32 | Rodrigo Aspeti | Análisis comparativo AI-SDLC Agéntico vs. SDLC tradicional (Cascada/Ágil) para EduSync: tabla comparativa de 7 fases (descubrimiento, análisis, diseño, implementación, prueba, despliegue, operación) con columnas humano vs. agente, más 3 riesgos críticos identificados | Bitácora | `docs/diagramas/ai-sdlc.mmd` | 14/05 |

**Categorías presentes en este release:** `ADR` · `BRD` · `MRD` · `PRD` · `FSD` · `UC` · `NFR` · `Diagrama` · `Prompt` · `Rule` · `Bitácora`

---

## 2. Resumen de aportes por integrante

El siguiente cuadro consolida el total de tareas registradas en §1, agrupadas por integrante, como base para el cálculo del factor de aporte.

| Integrante | Total de tareas | Categorías cubiertas | Observación |
|------------|-----------------|----------------------|-------------|
| Rodrigo Aspeti | 32 | 11 | Dev Lead / PM del proyecto. Responsable de la totalidad de la cadena documental de especificación (BRD→MRD→PRD→FSD→LFSD), las 5 decisiones arquitectónicas, los 5 casos de uso críticos con Gherkin, el ecosistema completo de 18 prompt-contratos y los diagramas de estado de los actores principales. |
| **Total grupo** | **32** | — | — |

### Distribución de tareas por categoría

| Categoría | Cantidad | Artefactos principales |
|-----------|----------|----------------------|
| Prompt | 7 | PROMPT_MAPPING.md (v0.5) + PR-ARCH-001, PR-BRD-002, PR-MRD-001, PR-PRD-001, PR-FSD-001, PR-LFSD-001 |
| FSD | 7 | Arquitectura funcional, specs estados Docente/Director, reglas de negocio, FSD completo, LFSD completo |
| ADR | 5 | DA-01..DA-05 (multitenancy, parametrización, inmutabilidad, consolidación, SIE) |
| UC | 5 | FSD-UC-001, UC-003, UC-004, UC-005, UC-009 |
| BRD | 2 | BRD EduSync V1 y V2 |
| Diagrama | 2 | Diagramas de estados Docente (18 estados) y Director (23 estados) |
| NFR | 1 | NFR-001..NFR-016 (16 requerimientos no funcionales) |
| MRD | 1 | MRD EduSync v1.0 |
| PRD | 1 | PRD EduSync v1.0 |
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

> Aporte promedio del grupo: **32 tareas / 1 integrante = 32.00 tareas/persona**

| Integrante | Tareas (§2) | Factor sin clamp | Factor (clamp 0.5–1.1) | Nota individual |
|------------|-------------|------------------|------------------------|-----------------|
| Rodrigo Aspeti | 32 | 32 / 32.00 = **1.00** | **1.00** | Nota_grupal × 1.00 |

### Interpretación

Dado que el grupo está compuesto por un único integrante que realizó la totalidad de las 32 tareas documentadas, el factor de aporte resulta en `1.00`. Esto significa que la nota individual es igual a la nota grupal, sin ajuste al alza ni a la baja. El cálculo es matemáticamente consistente: no existe diferencia entre el aporte individual y el promedio del grupo cuando n = 1.

---

## 4. Criterios de granularidad aplicados

El grupo adoptó íntegramente los criterios estándar del módulo, con las siguientes aclaraciones y adaptaciones documentadas para el contexto del proyecto:

**Criterios del módulo aplicados sin modificación:**

- **Un caso de uso** con flujo principal + alterno + Gherkin = 1 tarea. Aplicado en tareas #16–#20 (FSD-UC-001, UC-003, UC-004, UC-005, UC-009).
- **Un NFR ISO 25010** cuantificable con métrica + umbral + verificación = 1 tarea. Los 16 NFRs se contabilizaron como 1 tarea porque corresponden a una única sección `##` de un mismo documento con contenido homogéneo (tarea #21).
- **Un diagrama Mermaid** versionado y coherente con el FSD = 1 tarea. Aplicado en tareas #10 y #12.
- **Una sección de nivel `##`** en BRD/MRD/PRD/FSD con contenido sustantivo = 1 tarea. Los documentos completos (BRD v1, BRD v2, MRD, PRD, FSD, LFSD) se contabilizaron cada uno como 1 tarea, dado que representan documentos íntegros de nivel `##` de nivel superior.
- **Un ADR aceptado** = 1 tarea. Aplicado en tareas #2–#6 (DA-01..DA-05).
- **Una cursor rule** específica del dominio = 1 tarea. Aplicado en tarea #7.
- **Un prompt-contrato** con los 6 elementos + Invariants + Failure modes = 1 tarea. Aplicado en tareas #26–#31 (6 contratos individuales) y tarea #25 (PROMPT_MAPPING como artefacto de catálogo de nivel `##`).
- **Una sección de bitácora** = 1 tarea. Aplicado en tarea #32 (análisis AI-SDLC).

**Aclaraciones específicas del grupo:**

1. **FSD vs. LFSD**: se contabilizan como tareas separadas (#23 y #24) porque son documentos con propósitos diferenciados y completamente distintos en alcance: el FSD especifica *qué* hace el sistema a nivel funcional; el LFSD especifica *cómo* está diseñado internamente para su implementación (clases, DTOs, DDL, APIs, diagramas de secuencia, schedulers).

2. **PROMPT_MAPPING.md**: se contabiliza como 1 tarea de tipo `Prompt` (#25) porque es un artefacto de documentación de nivel de catálogo completo (18 contratos, flowchart, matriz de agentes, trazabilidad) y no una simple sección de otro documento. Las filas #26–#31 documentan los prompt-contratos individuales que lo componen, los cuales son verificables de forma independiente por sección.

3. **Especificaciones Markdown de diagramas** (#11, #13): se contabilizan como tareas de categoría `FSD` independientes de los archivos `.mmd` (#10, #12) porque producen un artefacto de texto diferente (descripción narrativa de estados, transiciones y actores) que no se genera automáticamente desde el diagrama.

**No se contabilizaron:** correcciones tipográficas y de formato menores sobre documentos ya creados, ajustes de indentación en archivos Mermaid para compatibilidad de parsers (verificaciones técnicas sin generación de contenido sustantivo nuevo).

---

## 5. Espacio para auditoría del docente

> Esta sección queda reservada para que el docente registre ajustes manuales al factor calculado en §3, si los hubiere. En ausencia de observaciones del docente, se aplica directamente el factor calculado automáticamente.

| Integrante | Factor calculado (§3) | Factor final aplicado | Justificación del ajuste |
|------------|-----------------------|------------------------|---------------------------|
| Rodrigo Aspeti | 1.00 | *(sin ajuste — a criterio del docente)* | *(ninguno; factor calculado se aplica directamente)* |

---

## 6. Checklist de cierre del release

Los siguientes ítems verifican la completitud y validez del presente informe antes de su commit en el repositorio:

- [x] **§0** — Metadatos completos: producto, grupo, release, sesión, fecha, integrante(s) y branch declarados.
- [x] **§1** — Las 32 tareas registradas incluyen integrante, categoría admitida y referencia verificable (archivo + sección).
- [x] **§2** — La suma de tareas por integrante (32) coincide con el total del grupo (32). Tabla de distribución por categoría incluida.
- [x] **§3** — Aporte promedio (32.00 tareas/persona) y factor (1.00) calculados y documentados con su interpretación.
- [x] **§4** — Los criterios estándar del módulo fueron aplicados sin relajación; las adaptaciones específicas del grupo están documentadas explícitamente con justificación.
- [ ] **Commit pendiente** — El archivo debe ser commiteado en el branch `release/1.0.0` con el hash de HEAD actualizado en §0 antes del cierre formal del release.
