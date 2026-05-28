# PR-PRD-001 — Generación del PRD EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-PRD-001` |
| Título | Generación del Product Requirements Document EduSync v1.0 |
| Artefacto origen | MRD v1 + BRD v2 + arquitectura funcional |
| ID origen | `MRD-N-01..10`, `BR-001..BR-012`, `UC-01..UC-10` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 15/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un experto en Product Management, Product Discovery, Business Analysis y
definición de requerimientos funcionales para productos SaaS empresariales.
Tienes experiencia creando PRDs con Agile, Lean Product e INVEST.
```

### 1.2 Task
```text
Genera docs/PRD_EduSync.md siguiendo plantillas/PRD_TEMPLATE.md, describiendo
QUE debe hacer EduSync para cumplir los requerimientos del MRD v1.0 y BRD v2.0.
El documento debe ser accionable para desarrollo, diseño y QA.
```

### 1.3 Context
```text
- Insumos: MRD v1 (10 MRD-N-*), BRD v2 (12 BR-NNN), arquitectura funcional (10 UCs, 5 DAs),
  diagramas de estado (18 estados Docente, 23 estados Director).
- Constitución del producto (5 principios): Zero-Training, RUDE única clave, inmutabilidad
  post-cierre, sin PII en logs, RLS multitenant.
- Restricción: >=15 US INVEST con Gherkin, RICE top-10, >=2 journeys Mermaid.
```

### 1.4 Reasoning
```text
1. Derivar 6 épicas de los 10 UCs.
2. Generar >=17 user stories con formato INVEST y Gherkin.
3. Construir tabla RICE: top-10 historias.
4. Generar 3 user journeys Mermaid.
5. Documentar 20 PRD-REQ-* funcionales y 15 PRD-NFR-* con umbrales.
6. Definir roadmap v1.0→v2.0 y Discovery track.
7. Completar trazabilidad PRD-REQ → BR → MRD-N → UC/DA → FSD.
```

### 1.5 Stop condition
```text
Detente cuando el PRD tenga: constitución, 17 US con Gherkin, RICE top-10, 3 journeys,
20 PRD-REQ-*, 15 NFRs, roadmap, Discovery track, trazabilidad y checklist completos.
```

### 1.6 Output
```text
docs/PRD_EduSync.md (secciones 0-16 + checklist) listo para planificación Agile.
```

## 2. Invariantes del prompt

- Toda US debe cumplir INVEST.
- Cada criterio Gherkin debe ser verificable sin ambigüedad.
- RICE Score: `(Reach × Impact × Confidence) / Effort`.
- Las invariantes del BRD (RUDE, floor, ventana temporal) deben aparecer en Gherkin.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_US_NO_INVEST` | Historia sin criterio de aceptación | Rechazar |
| `E_GHERKIN_AMBIGUO` | Criterio no verificable por QA | Reescribir con datos concretos |
| `E_RICE_INCOMPLETO` | Tabla RICE con menos de 10 historias | Completar |
| `E_TRAZABILIDAD_ROTA` | PRD-REQ sin BR ni MRD-N | Agregar enlace |

## 4. Guardrails

- MUST: validar que todas las US cumplen INVEST antes de entregar.
- MUST NOT: proponer implementación técnica en el PRD.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| MRD v1 + BRD v2 + arquitectura funcional | `MRD-N-01..10`, `BR-001..BR-012` | PR-PRD-001 | `docs-agent` | `docs/prd/PRD_EduSync.md` v1.0 |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: MRD v1 y BRD v2 completos + arquitectura funcional.
- **Output esperado**: PRD con 17+ US con Gherkin, RICE top-10, 3 journeys, 20 PRD-REQs.

### 6.2 Caso borde
- **Input**: US con criterio de aceptación genérico ("el sistema debe funcionar bien").
- **Output esperado**: `E_GHERKIN_AMBIGUO`; el criterio se reescribe con valores concretos.

### 6.3 Caso adversarial
- **Input**: propuesta de una US que no cumple `I` (Independent) de INVEST.
- **Comportamiento esperado**: la US se descompone en dos historias independientes.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 15/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
