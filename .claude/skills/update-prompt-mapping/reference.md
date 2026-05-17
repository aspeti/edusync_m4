# Referencia — Plantillas exactas de PROMPT_MAPPING

> Este archivo contiene los fragmentos de código exactos (copiar-pegar) para cada sección
> que debe modificarse al registrar un nuevo prompt en `docs/PROMPT_MAPPING.md`.

---

## 1. Fila del índice de prompts

```markdown
| PR-AREA-NNN | `docs/ruta/artefacto.md` (descripción breve — v1.0) | generación | `docs-agent` | Sonnet | 17/05/2026 | Aprobado |
```

**Columnas del índice en orden:**
`ID` | `Artefacto producido` | `Tipo` | `Agente` | `Modelo` | `Fecha` | `Estado`

---

## 2. Ejemplo completo de bloque de contrato

Basado en `PR-MRD-001` (real del proyecto):

```markdown
---

### PR-MRD-001 — Generación del MRD EduSync

```markdown
# Role
Eres un experto en Product Management, Business Analysis y documentacion de
productos digitales con experiencia creando documentos MRD para proyectos de
software usando Product Discovery, Lean Product y Agile.

# Task
Genera docs/mrd/MRD_EduSync.md utilizando el template plantillas/MRD_TEMPLATE.md,
documentando el mercado objetivo, segmentacion, pain points, JTBD, propuesta de
valor, go-to-market y metricas de exito para EduSync en Bolivia.

# Context
- Documentos fuente: docs/brd/BRD_EduSync_v2.md, docs/arquitectura_funcional_EduSync.md,
  01_vision_negocio.md, plantillas/MRD_TEMPLATE.md.
- Entradas: BR-001..BR-012, stakeholders (Marcela/Wendy/Jeanneth), Excel reales.
- Restricciones de dominio: mercado boliviano (unidades privadas y convenio);
  cumplimiento SIE; stack Java 21, Spring Boot 3.3, PostgreSQL 15.
- Orientacion: negocio y producto, NO tecnico.

# Reasoning
1. Analizar BRD v2 para extraer MRD-N-01..10 (necesidades de mercado).
2. Definir TAM/SAM/SOM para el mercado educativo boliviano.
3. Mapear Jobs-to-be-Done desde las entrevistas UX.
4. Formular propuesta de valor diferencial vs. Excel manual.
5. Diseñar estrategia go-to-market y metricas de exito.

# Stop condition
Detente cuando todas las secciones del MRD_TEMPLATE.md esten completas, sin
placeholders vacios y con formato Markdown valido.

# Output
Archivo docs/mrd/MRD_EduSync.md completo con: resumen ejecutivo, analisis de
mercado (TAM/SAM/SOM), 3 personas, JTBD, analisis competitivo, propuesta de
valor canvas, pricing modelo SaaS, go-to-market, 10 MRD-N-*, metricas de exito.

# Invariants
- El documento debe ser orientado a negocio, no tecnico (IG-06).
- Toda necesidad de mercado referencia al menos un BR-NNN del BRD v2 (IG-08).
- El stack tecnico solo se menciona como restriccion, no como diseno.

# Failure modes
- E_MISSING_CONTEXT: falta BRD v2 o vision_negocio.md — STOP, solicitar.
- E_ARQUITECTURA_EN_SPECS: el MRD contiene diseno tecnico — rechazar y regenerar.
- E_BMC_INCOMPLETO: alguna seccion del Business Model Canvas vacia — completar
  antes de considerar el documento entregable.
` `` `
```

---

## 3. Fila de trazabilidad completa

```markdown
| <Artefacto origen> | `<ID-origen>` | PR-AREA-NNN | `agente` | <descripción artefacto generado> | `docs/ruta/archivo.md` |
```

**Ejemplo real:**
```markdown
| BRD v2 + Arq. funcional | `BR-001..BR-012, MRD-N-01..10` | PR-MRD-001 | `docs-agent` | MRD EduSync v1.0 | `docs/mrd/MRD_EduSync.md` |
```

---

## 4. Entrada del historial de versiones

```markdown
| vX.Y | dd/mm/aaaa | Equipo G-EduSync | Incorporación de PR-AREA-NNN (<nombre del artefacto>); actualización del índice (N nuevos prompts), áreas de IDs (<areas nuevas si aplica>), flowchart (<cambios en nodos/aristas>), matriz del <agente> y trazabilidad ampliada a N prompts |
```

**Ejemplo real:**
```markdown
| v0.4 | 15/05/2026 | Equipo G013 | Incorporación de PR-MRD-001, PR-PRD-001 y PR-FSD-001; actualización del índice (3 nuevos prompts), áreas de IDs (MRD/PRD/FSD), flowchart (cadena MRD→PRD→FSD), matriz del docs-agent y trazabilidad ampliada a 17 prompts |
```

---

## 5. Áreas de IDs y agentes válidos

### Áreas válidas

| Área | Descripción | Agente típico |
|------|-------------|---------------|
| `ARCH` | Arquitectura funcional | `docs-agent` |
| `BRD` | Business Requirements | `docs-agent` |
| `MRD` | Market Requirements | `docs-agent` |
| `PRD` | Product Requirements | `docs-agent` |
| `FSD` | Functional Specification | `docs-agent` |
| `LFSD` | Low-Level Functional Spec | `docs-agent` |
| `UC` | Use Case contract | `dev-agent` |
| `ADR` | Architectural Decision | `arch-agent` |
| `AUD` | Auditoría / trazabilidad | `qa-agent` |
| `INF` | Informe / dashboard | `docs-agent` |
| `DIAG` | Diagrama de estados/procesos | `process-agent` |

### Numeración de IDs

El siguiente número disponible por área (al 17/05/2026):

| Área | Último usado | Próximo |
|------|-------------|---------|
| ARCH | 001 | 002 |
| BRD | 002 | 003 |
| MRD | 001 | 002 |
| PRD | 001 | 002 |
| FSD | 001 | 002 |
| LFSD | 001 | 002 |
| UC | 009 | 010 |
| ADR | 005 | 006 |
| AUD | 001 | 002 |
| INF | 001 | 002 |
| DIAG | 002 | 003 |

---

## 6. Invariantes globales del ecosistema (referencia rápida)

| ID | Invariante resumida |
|----|---------------------|
| IG-01 | RUDE es la única clave de estudiantes |
| IG-02 | Toda escritura genera entrada en audit_log |
| IG-03 | `floor()` SIE exclusivo en motor UC-03 |
| IG-04 | Parámetros académicos inmutables con periodo ABIERTO |
| IG-05 | Toda consulta acotada a `tenant_id` del usuario |
| IG-06 | Specs (BRD/FSD) no contienen código de implementación |
| IG-07 | Reprobación anual solo con 3 trimestres cerrados |
| IG-08 | Todo prompt cita el ID de su artefacto origen |
| IG-09 | `.mmd` y `.md` sincronizan 1:1 en estados y transiciones |
| IG-10 | `.mmd` sin caracteres Unicode decorativos en labels |

---

## 7. Tipos de prompt válidos

| Tipo | Cuándo usarlo |
|------|---------------|
| `generación` | El prompt produce un artefacto nuevo desde cero |
| `transformación` | El prompt convierte/adapta un artefacto existente |
| `consolidación` | El prompt fusiona múltiples artefactos en uno |
| `auditoría` | El prompt verifica invariantes o trazabilidad |
| `extracción` | El prompt extrae información de documentos existentes |
| `revisión` | El prompt evalúa la calidad de un artefacto |
