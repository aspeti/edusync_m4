# PR-MRD-001 — Generación del MRD EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-MRD-001` |
| Título | Generación del Market Requirements Document EduSync v1.0 |
| Artefacto origen | BRD v2 + arquitectura funcional + entrevistas UX |
| ID origen | `BR-001..BR-012`, `MRD-N-01..10`, `DA-01..DA-05` |
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
Eres un experto en Product Management, Business Analysis y documentación de
productos digitales con experiencia en Product Discovery, Lean Product, Agile
y documentación técnica empresarial para mercados latinoamericanos.
```

### 1.2 Task
```text
Genera docs/MRD-EduSync.md siguiendo plantillas/MRD_TEMPLATE.md, describiendo
el mercado, usuarios y oportunidad comercial que justifican EduSync.
El documento debe responder: "¿qué pide el mercado boliviano de gestión académica
y por qué EduSync ganará?"
```

### 1.3 Context
```text
- Insumos: docs/BRD_EduSync_V2.md, docs/arquitectura_funcional_EduSync.md,
  01_vision_negocio.md, entrevistas UX con Marcela, Wendy, Jeanneth.
- Mercado objetivo: ~4 000 unidades educativas privadas y de convenio en Bolivia.
- Competidores: Excel+SIE manual, Academium, Colegio360, Google Sheets, SIE gubernamental.
- Modelo de negocio: SaaS B2B con Setup Fee Bs 200 + suscripción anual por estudiante.
```

### 1.4 Reasoning
```text
1. Calcular TAM/SAM/SOM con fuentes y notas de asunción explícitas.
2. Construir 3 personas completas (Wendy/Marcela/Jeanneth) con JTBD y dolores.
3. Documentar >=8 JTBD alineados a los 10 UCs.
4. Generar tabla competitiva con >=5 alternativas.
5. Construir Positioning Statement. Diseñar pricing en tiers. Definir go-to-market.
6. Documentar 10 MRD-N-* con MoSCoW y >=8 hipótesis con método de validación.
```

### 1.5 Stop condition
```text
Detente cuando el MRD tenga: TAM/SAM/SOM, 3 personas, 8 JTBD, 5 competidores,
positioning statement, pricing, go-to-market, 10 MRD-N-*, 8 hipótesis, trazabilidad.
```

### 1.6 Output
```text
docs/MRD-EduSync.md (secciones 0-16 + checklist) listo para revisión por
stakeholders de negocio y producto.
```

## 2. Invariantes del prompt

- Todo MRD-N-* debe tener prioridad MoSCoW y justificación de mercado verificable.
- El TAM/SAM/SOM debe tener fuente o nota de asunción explícita.
- Los supuestos de precio y competencia deben marcarse con "(assumption)".
- El positioning statement debe referir a un competidor concreto.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TEMPLATE_VIOLADO` | Estructura distinta a MRD_TEMPLATE.md | Rechazar y regenerar |
| `E_PLACEHOLDER_VACIO` | Sección con marcadores sin completar | Completar |
| `E_TAM_SIN_FUENTE` | TAM/SAM/SOM sin fuente ni asunción | Agregar |
| `E_COMPETIDOR_GENERICO` | Positioning sin competidor concreto | Especificar |

## 4. Guardrails

- MUST: validar que todos los MRD-N-* tienen MoSCoW.
- MUST NOT: proponer arquitectura técnica en el MRD.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| BRD v2 + arquitectura funcional + UX | `BR-001..BR-012`, `MRD-N-01..10` | PR-MRD-001 | `docs-agent` | `docs/mrd/MRD_EduSync.md` v1.0 |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: BRD v2 completo + entrevistas UX con las 3 personas.
- **Output esperado**: MRD con TAM/SAM/SOM, 3 personas JTBD, 10 MRD-N-*, positioning.

### 6.2 Caso borde
- **Input**: sin datos de precio de competidores bolivianos.
- **Output esperado**: tabla competitiva con columna de precio marcada como `(assumption)`.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir DDL de tabla de precios en el MRD.
- **Comportamiento esperado**: rechazado; el DDL pertenece al LFSD.

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
