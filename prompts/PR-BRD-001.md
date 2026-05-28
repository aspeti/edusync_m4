# PR-BRD-001 — Generación del BRD EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-BRD-001` |
| Título | Generación del Business Requirements Document EduSync v1.0 |
| Artefacto origen | Vision de negocio + entrevistas UX |
| ID origen | `01_vision_negocio.md` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Product Strategist Senior con experiencia en EdTech GovTech para
mercados emergentes latinoamericanos y conocimiento de normativa educativa
boliviana (Ley 070 Avelino Siñani).
```

### 1.2 Task
```text
Genera docs/BRD_EduSync.md siguiendo plantillas/BRD_TEMPLATE.md documentando
el problema de la triple digitacion manual, el modelo de negocio SaaS B2B
multitenancy y los requerimientos de negocio priorizados con MoSCoW.
```

### 1.3 Context
```text
- Insumo primario: 01_vision_negocio.md (problema, oportunidad, stakeholders).
- Entrevistas UX con: Marcela (Docente), Wendy (Secretaria), Jeanneth (Directora).
- Mercado objetivo: unidades educativas de Bolivia (privadas y de convenio).
- Modelo de ingresos: SaaS B2B por unidad educativa (tenant).
- Restriccion legal: cumplimiento con formato de exportacion al SIE del
  Ministerio de Educacion de Bolivia.
```

### 1.4 Reasoning
```text
1. Redactar el problema central con evidencia cuantitativa.
2. Definir >=6 BR-NNN priorizados MoSCoW con criterio de aceptacion.
3. Documentar el modelo de negocio (BMC: segmentos, propuesta de valor, canales).
4. Declarar KPIs del producto: tiempo de cierre administrativo, tasa de error SIE.
5. Establecer RACI con Director, Secretaria, Docente, Dev (Rodrigo Aspeti).
```

### 1.5 Stop condition
```text
Detente cuando el BRD tenga: >=6 BR-NNN, BMC de 9 bloques, KPIs, RACI y
seccion de trazabilidad BR → UC completada.
```

### 1.6 Output
```text
Markdown completo segun BRD_TEMPLATE.md con encabezado de metadatos,
todas las secciones completadas y tabla de trazabilidad BR → UC al final.
```

## 2. Invariantes del prompt

- Todo BR-NNN debe tener criterio de aceptacion verificable.
- El RUDE debe aparecer como restriccion critica en al menos un BR.
- No proponer arquitectura tecnica en el BRD (pertenece al FSD/DTI).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_MISSING_UX` | Falta contexto de al menos 2 stakeholders | STOP |
| `E_BR_SIN_CRITERIO` | BR-NNN sin criterio de aceptacion | Rechazar output |
| `E_ARQUITECTURA_EN_BRD` | Output propone stack tecnico | Rechazar y limpiar |

## 4. Guardrails

- MUST: validar que ningún BR contiene código o DDL.
- MUST NOT: proponer arquitectura técnica en el BRD.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Vision de negocio | `01_vision_negocio.md` | PR-BRD-001 | `docs-agent` | `docs/BRD_EduSync.md` |
| Entrevistas UX | Marcela, Wendy, Jeanneth | PR-BRD-001 | `docs-agent` | BR-001..BR-006 |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: `01_vision_negocio.md` completo con dolor de triple digitación documentado.
- **Output esperado**: BRD con ≥6 BRs, BMC de 9 bloques, KPIs con línea base.

### 6.2 Caso borde
- **Input**: visión sin evidencia cuantitativa del tiempo perdido.
- **Output esperado**: el agente usa estimaciones explícitas marcadas como `(assumption)`.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir diagrama ER en el BRD.
- **Comportamiento esperado**: rechazo con `E_ARQUITECTURA_EN_BRD`; el ER pertenece al FSD.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
