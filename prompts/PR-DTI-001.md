# PR-DTI-001 — Generación del DTI completo de EduSync (§0–§23)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-DTI-001` |
| Título | Generación del Documento Técnico Inicial completo de EduSync v0.1 |
| Artefacto origen | FSD + LFSD + AGENTS.md + C4 diagrams |
| ID origen | `FSD-UC-001..009`, `DA-01..DA-05`, `BR-001..BR-012` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 17/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Solution Architect y Technical Writer especializado en
documentacion tecnica de productos SaaS B2B, con dominio profundo de
EduSync: stack Java 21 / Spring Boot 3.3 / PostgreSQL 15 / Angular 17 /
AWS ECS Fargate, arquitectura hexagonal, multitenancy RLS y cumplimiento
regulatorio boliviano (SIE, Ley 070, Ley 164).
```

### 1.2 Task
```text
Genera el Documento Tecnico Inicial (DTI) completo de EduSync cubriendo las
23 secciones obligatorias (§0-§23) segun la plantilla del modulo, con
audiencia dual (humanos + agentes IA).
```

### 1.3 Context
```text
- Skill guia: .cursor/skills/dti-edusync/SKILL.md
- Plantilla: plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE (1).md (620 lineas)
- FSD: docs/fsd/FSD_EduSync.md (FSD-UC-001..009, BR-001..BR-012, 16 NFRs)
- LFSD: docs/LFSD-EduSync.md (puertos, adaptadores, DDL, secuencias, APIs)
- AGENTS.md v0.2: 6 agentes, golden tests, stack autoritativo
- Diagramas C4: docs/diagrams/c4_level1.mmd, c4_level2.mmd
- Decisiones: DA-01 (RLS), DA-02 (hexagonal), DA-03 (audit_log),
  DA-04 (async), DA-05 (Resilience4j SIE)
- BRD v2: docs/brd/BRD_EduSync_v2.md (vision, metricas, restricciones)
```

### 1.4 Reasoning
```text
1. Leer el skill dti-edusync para identificar los datos reales de cada seccion.
2. Generar el frontmatter YAML con producto, version, stack, audiencia.
3. Poblar §0-§3: metadatos, tabla de agentes SDLC, vision, C4 L1/L2 embebidos,
   C4 L3 flowchart del contenedor critico (API Gateway), sequence diagram FSD-UC-001.
4. Poblar §4-§9: modelo de dominio, hexagonal, distribuida, async, AWS, IA SDLC.
5. Poblar §10-§19: prompt mapping, NFRs x16, 2 POCs, seguridad STRIDE,
   observabilidad, DevOps, antipatrones, trade-offs, riesgos, roadmap.
6. Poblar §20-§23: glosario, ADRs (DA-01..DA-05 provisionales),
   auditoria IA y eval de guardrails (4 golden tests).
7. Cerrar con checklist de entrega y pie de firma.
```

### 1.5 Stop condition
```text
Detente cuando docs/DTI.md exista, tenga >= 800 lineas, todas las 23 secciones
esten pobladas (sin placeholders), y el checklist marque >= 24/27 items completados.
```

### 1.6 Output
```text
docs/DTI.md v0.1 (883 lineas) con: frontmatter YAML valido, 23 secciones
completas, C4 L1/L2/L3 embebidos, sequence diagram FSD-UC-001, 5 bounded
contexts, 16 NFRs, 2 POCs con criterio medible, 5 ADRs provisionales,
4 golden tests, checklist 24/27 completado.
```

## 2. Invariantes del prompt

- `§3.5` marcado N/A — EduSync v1.0 no tiene agentes IA en runtime (DA-02).
- Diagramas Mermaid sin Unicode decorativo en labels.
- Cada decision cita su DA-NN o ADR provisional.
- Cero secretos ni PII en el documento.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_SECCION_PLACEHOLDER` | Sección con texto de plantilla sin reemplazar | Completar con datos EduSync |
| `E_DA_SIN_CITA` | Decisión arquitectónica sin referencia a DA-NN | Añadir cita |
| `E_NFR_SIN_UMBRAL` | NFR sin threshold numérico | Completar con valor medible |
| `E_AGENTS_NO_ACTUALIZADO` | AGENTS.md sigue con DTI como pendiente | Actualizar |

## 4. Guardrails

- MUST: validar que todas las 23 secciones están pobladas antes de entregar.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia`.
- MUST NOT: incluir secretos de producción ni PII en el DTI.
- MUST NOT: omitir los 4 golden tests en §23.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD + LFSD + C4 + AGENTS.md | `FSD-UC-001..009`, `DA-01..DA-05` | PR-DTI-001 | `docs-agent` | `docs/DTI.md` v0.1 (883 líneas, §0–§23) |
| BRD v2 | `BR-001..BR-012`, `NFR-001..016` | PR-DTI-001 | `docs-agent` | Secciones de requisitos y NFRs del DTI |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: FSD completo, LFSD completo, C4 L1/L2, AGENTS.md v0.2.
- **Output esperado**: DTI de 883+ líneas con las 23 secciones completas, checklist 24/27.

### 6.2 Caso borde
- **Input**: C4 Level 3 no disponible como archivo separado.
- **Output esperado**: el agente genera el C4 L3 inline como flowchart Mermaid del API Gateway.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir credenciales de RDS en §8 Despliegue.
- **Comportamiento esperado**: rechazado; las credenciales se referencian como Secrets Manager, no se incluyen en claro.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 17/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
