# PROMPT_MAPPING — libre-ehr

> Catálogo de prompts usados para producir cada artefacto de specs (formato S04 §entregable y FSD-TEMPLATE §0). IDs `PR-XXX-NNN` (XXX = INV / BRD / MRD / PRD / FSD / ADR / DTI / VIBE). Complementa pero **no reemplaza** la bitácora de gobernanza append-only en [prompt_mappings.md](../prompt_mappings.md). Sigue [plantillas/PROMPT_TEMPLATE.md](../plantillas/PROMPT_TEMPLATE.md).

## Índice

| ID | Artefacto producido | Skill | Modelo | Fecha | Entrada gobernanza |
|----|---------------------|-------|--------|-------|--------------------|
| PR-INV-001 | docs/reverse/domain-inventory.md | sql-to-domain-inventory | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |
| PR-BRD-001 | docs/brd/BRD_v0.1.md | brd-author-ehr | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |
| PR-MRD-001 | docs/mrd/MRD_v0.1.md | mrd-author-ehr | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |
| PR-PRD-001 | docs/prd/PRD_v0.1.md | prd-author-ehr | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |
| PR-FSD-001 | docs/fsd/FSD_v0.1.md (LFSD) | fsd-author-ehr | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |
| PR-ADR-001..006 | docs/adr/0001-*.md ... 0006-*.md | adr-recorder-ehr | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |
| PR-DTI-001 | docs/DTI.md + AGENTS.md sync | dti-author-ehr | claude-opus-4.7 | 2026-05-11 | PM-20260511-002 |

## Prompts

### PR-INV-001 — Generación domain-inventory desde SQL legacy

```markdown
# Role
Eres un arquitecto de datos clínicos con experiencia en LibreEHR/OpenEMR y conocimiento de HL7 FHIR R4 para mapeo semántico.

# Task
Lee `libre-ehr-database.sql` (~7050 líneas, 165 tablas) por bloques con Grep, agrupa las tablas en módulos clínicos canónicos (M01..M15), produce un ER mermaid por módulo (≤8 entidades), mapea cada módulo a recursos FHIR R4 (referencia semántica, no técnica), detecta patrones cross-cutting (audit, soft-delete, list/option, LBF), y reporta GAPs y decisiones arquitectónicas observadas (ADR-CAND-NN).

# Context
- SQL fuente: libre-ehr-database.sql (legacy MySQL/MariaDB, mezcla MyISAM/InnoDB).
- Skill compañera: ehr-domain-expert (vocabulario clínico, HIPAA/ONC reglas).
- Plantilla salida: docs/reverse/domain-inventory.md.
- Reglas mermaid: plantillas/skills/_shared/mermaid-conventions.md.

# Reasoning
1. Listar tablas con Grep "^CREATE TABLE" y contar.
2. Agrupar por prefijo común y semántica (Patient, Encounter, Orders, ePrescribing, Billing, Scheduling, Audit, Admin, Forms, Portal, Documents, Decision Support, CCDA, Terminologies, Misc).
3. Para cada módulo: leer 1-2 tablas representativas con Read offset/limit (≤50 líneas).
4. Construir ER mermaid por módulo respetando convenciones (sin espacios en IDs, sin colores).
5. Mapear cada módulo a FHIR R4 (Patient, Encounter, Observation, ServiceRequest, MedicationRequest, Claim, Appointment, AuditEvent, etc.).
6. Reportar GAPs (engine mix, FK lógica no declarada, god-tables, charset, etc.) y ADR candidates.

# Stop condition
Suma de tablas por módulo == 165; cada módulo tiene ER válido; ≥10 GAPs reportados; ≥6 ADR-CAND identificados.

# Output
Markdown con metadatos, tabla resumen, sección por módulo (incluyendo ER mermaid + mapeo FHIR), patrones cross-cutting, GAPs, ADR candidates, trazabilidad.

# Invariants
- No reescribir DDL.
- No proponer migración FHIR (framing as-is).
- Identidades clínicas reales (no ficticias).

# Failure modes
- ≥1 tabla sin asignar a módulo: STOP, replanificar agrupación.
- ER monolítico de toda la BD: STOP, partir por módulo.
```

### PR-BRD-001 — Generación BRD v0.1 as-is desde domain-inventory

```markdown
# Role
Product strategist senior con experiencia healthcare GovTech / EHR open-source.

# Task
Genera `docs/brd/BRD_v0.1.md` siguiendo `plantillas/BRD_TEMPLATE.md` con framing as-is libre-ehr (documenta lo que el sistema legacy ya hace, no reposiciona como FHIR-first). Incluye ≥8 BR-NNN priorizados MoSCoW, BMC con 9 bloques (≥3 elementos cada uno), KPIs healthcare-specific, RACI con CMIO/CFO/CISO/HIM, y PR-FAQ Working Backwards opcional.

# Context
- Insumo: docs/reverse/domain-inventory.md (15 módulos, 165 tablas).
- Skill compañera: ehr-domain-expert.
- Plantilla: plantillas/BRD_TEMPLATE.md.

# Reasoning
1. Construir resumen ejecutivo al final.
2. §3 evidencia cuantitativa healthcare (denial rate, time-to-document, no-show).
3. §4 ≥2 personas (Clinician, Billing officer).
4. §5 VPC con bullets verificables.
5. §6 ≥3 alternativas (Epic, Cerner/Oracle, OpenEMR, paper).
6. §7 BMC 9 bloques con ≥3 elementos.
7. §8 North Star + KPIs healthcare.
8. §11 ≥8 BR-NNN MoSCoW.

# Stop condition
Checklist mínimo BRD cumplido (ver _shared/quality-checklists.md).

# Output
Markdown plantilla BRD completo con §18 trazabilidad headers MRD/PRD/FSD vacíos.
```

### PR-MRD-001 — Generación MRD v0.1 desde BRD

```markdown
# Role
Product manager senior con experiencia en mercado EHR (US + LATAM).

# Task
Genera `docs/mrd/MRD_v0.1.md` siguiendo `plantillas/MRD_TEMPLATE.md` con TAM/SAM/SOM EHR, competencia (Epic/Cerner/OpenEMR), JTBD healthcare, GTM, regulación HIPAA/ONC/AGEMED.

# Context
- Insumo: docs/brd/BRD_v0.1.md, docs/reverse/domain-inventory.md.
- Skill compañera: ehr-domain-expert.

# Reasoning
1. Derivar MRD-N-NN desde BR-NNN.
2. Personas: Clinician, Billing, MA, Patient.
3. Competencia con quadrantChart mermaid.
4. Modelo open-source: GPL + servicios.

# Stop condition
≥5 MRD-N-NN, ≥3 JTBD por persona, ≥3 competidores, North Star + 3 KPIs.

# Output
Markdown plantilla MRD completo con §14 trazabilidad BR ↔ MRD-N completada.
```

### PR-PRD-001 — Generación PRD v0.1 con ≥15 user stories

```markdown
# Role
Product Manager con background técnico healthcare.

# Task
Genera `docs/prd/PRD_v0.1.md` siguiendo `plantillas/PRD_TEMPLATE.md` con ≥15 user stories priorizadas MoSCoW + RICE, criterios Gherkin, ≥10 PRD-REQ-NNN, ≥5 PRD-NFR-NNN HIPAA-aware, ≥2 user journeys mermaid.

# Context
- Insumos: BRD v0.1, MRD v0.1, domain-inventory.

# Reasoning
1. 8 épicas (E1..E8 por módulo clínico crítico).
2. ≥15 stories distribuidas en épicas.
3. Cada story con Gherkin AC.
4. RICE para top-10.
5. NFRs HIPAA-aware.

# Stop condition
≥15 stories con Gherkin; ≥10 PRD-REQ; ≥5 NFR; checklist PRD cumplido.

# Output
Markdown plantilla PRD con §14 trazabilidad bidireccional iniciada.
```

### PR-FSD-001 — Generación FSD v0.1 modo LFSD con 3 UCs críticos

```markdown
# Role
Solutions architect EHR con experiencia HIPAA/ONC.

# Task
Genera `docs/fsd/FSD_v0.1.md` modo LFSD ⚡ siguiendo `plantillas/FSD_TEMPLATE.md` con ≥3 UCs críticos (FSD-UC-001 Registro paciente, FSD-UC-002 Encuentro SOAP, FSD-UC-003 ePrescribing con DDI), prompt-contract por UC, ER mermaid por UC, ≥5 NFRs HIPAA, §11 matriz trazabilidad MRD→PRD→FSD→NFR→TC.

# Context
- Insumos: PRD v0.1, domain-inventory (módulos M01, M02, M04 para los 3 UCs).
- Spec Kit fase: Specify ✅ + Tasks ✅.

# Reasoning
1. Selección 3 UCs basados en stories Must del PRD.
2. ER mermaid por UC ≤8 entidades.
3. ≥5 BR-CLINICAL-NNN.
4. Prompt-contract anatomía completa.
5. NFRs HIPAA con métricas.

# Stop condition
3 UCs documentados; checklist LFSD cumplido; matriz §11 poblada.

# Output
Markdown plantilla FSD/LFSD completo.
```

### PR-ADR-001..006 — ADRs retrospectivos

```markdown
# Role
Software architect con criterio para decisiones legacy.

# Task
Crea ADRs 0001..0006 retrospectivos a partir de `docs/reverse/domain-inventory.md §"Decisiones arquitectónicas observadas"` siguiendo `plantillas/ADR_TEMPLATE.md`. Estado inicial = "Aceptada (retrospectiva)".

# Context
- Insumos: domain-inventory.md ADR-CAND-01..08, FSD §10 NFR.

# Reasoning
1. ADR-0001 mezcla MyISAM/InnoDB.
2. ADR-0002 audit central.
3. ADR-0003 cifrado PHI TDE.
4. ADR-0004 PKs int(11).
5. ADR-0005 modo LFSD.
6. ADR-0006 audit retention 6 años.

# Stop condition
≥3 ADRs producidos, cada uno con ≥3 alternativas y plan de reversión.

# Output
6 archivos docs/adr/000N-*.md + docs/adr/README.md índice.
```

### PR-DTI-001 — Generación DTI consolidado + AGENTS.md sync

```markdown
# Role
Arquitecto técnico senior healthcare con experiencia HIPAA + DDD.

# Task
Genera `docs/DTI.md` siguiendo `plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md` consolidando FSD + ADRs. Sincroniza `AGENTS.md` raíz preservando la sección de "Trazabilidad prompt → código" existente.

# Context
- Insumos: BRD/MRD/PRD/FSD/ADRs.
- AGENTS.md actual: contiene sección de gobernanza (creada en turno previo), DEBE preservarse.

# Reasoning
1. C4 niveles 1/2/3.
2. Bounded contexts: Clinical, Billing, Scheduling, Admin, Audit, Patient Portal.
3. Hexagonal: declarar N/A (libre-ehr legacy es MVC PHP).
4. Capa IA: N/A actual.
5. ≥2 POCs.
6. AGENTS.md ampliado, no reemplazado.

# Stop condition
DTI checklist completo; AGENTS.md preserva sección anterior.

# Output
docs/DTI.md + AGENTS.md ampliado.
```
