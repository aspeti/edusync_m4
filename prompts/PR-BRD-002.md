# PR-BRD-002 — Generación del BRD EduSync V2 (consolidado)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-BRD-002` |
| Título | Consolidación del BRD EduSync v2.0 — BR-001..BR-012, 11 RBs, BMC completo |
| Artefacto origen | BRD v1 + arquitectura funcional + diagramas de estado |
| ID origen | `BR-001..BR-012`, `DA-01..DA-05`, estados Docente y Director |
| Tipo de prompt | consolidación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Business Analyst (BA), Product Owner y Enterprise Solution Architect
con experiencia en levantamiento de requerimientos, analisis funcional y documentacion
corporativa para sistemas SaaS B2B en el sector educativo latinoamericano.
```

### 1.2 Task
```text
Genera docs/BRD_EduSync_V2.md siguiendo plantillas/BRD_TEMPLATE.md, consolidando
los requerimientos del v1 (BR-001..BR-005) con los requerimientos derivados de la
arquitectura funcional (10 UCs, 5 DAs), los diagramas de estado del Docente
y del Director.
```

### 1.3 Context
```text
- Insumos primarios: BRD v1, arquitectura_funcional_EduSync.md, 01_vision_negocio.md,
  estados_cargar_notas.md (18 estados), estados_administracion.md (23 estados).
- Requerimientos nuevos: apertura secuencial de periodos (UC-09), parametros
  inmutables post-apertura (DA-02), ventana de modificacion retroactiva 1-72h (UC-05),
  dashboard con separacion trimestral/anual (UC-10), audit_log inalterable (DA-03).
- Restriccion: >=12 BR-NNN con MoSCoW; >=11 RB-NNN; >=5 KPIs; >=5 BO-NNN SMART;
  RACI de 6 stakeholders; PR-FAQ Amazon-style en seccion 21.
```

### 1.4 Reasoning
```text
1. Leer y relacionar todos los artefactos fuente antes de escribir.
2. Identificar requerimientos implicitos de los diagramas de estado.
3. Conservar y enriquecer BR-001..BR-005; agregar BR-006..BR-012.
4. Documentar 11 RB-NNN con tipo (politica/normativa) y origen.
5. Generar BMC 9 bloques, 3 personas, KPIs, BO-NNN SMART, RACI, PR-FAQ.
```

### 1.5 Stop condition
```text
Detente cuando el BRD tenga: v2.0, 12 BR-NNN, 11 RB-NNN, BMC 9 bloques,
5 KPIs, 5 BO-NNN, RACI 6 stakeholders, trazabilidad BR→UC, 6 riesgos, PR-FAQ.
```

### 1.6 Output
```text
docs/BRD_EduSync_V2.md (secciones 0-21) listo para revision por stakeholders.
```

## 2. Invariantes del prompt

- BR del v1 (BR-001..BR-005) deben conservarse y enriquecerse, nunca eliminarse.
- El RUDE debe aparecer en BR-004 y RB-01.
- El criterio `floor` debe documentarse en BR-003 y RB-08.
- La ventana 1-72h debe aparecer en BR-009 y RB-07.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_MISSING_SOURCE` | Falta artefacto fuente | STOP, no generar output parcial |
| `E_BR_SIN_METRICA` | BR-NNN sin criterio verificable | Completar antes de entregar |
| `E_INCONSISTENCIA_V1` | BR del v1 eliminado en lugar de enriquecido | Restaurar y re-emitir |

## 4. Guardrails

- MUST: verificar que BR-001..BR-005 del v1 están presentes en el v2.
- MUST NOT: generar output si falta algún artefacto fuente.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| BRD v1 + arquitectura funcional | `BR-001..BR-012`, `DA-01..DA-05` | PR-BRD-002 | `docs-agent` | `docs/brd/BRD_EduSync_v2.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: BRD v1 + arquitectura funcional + diagramas de estado completos.
- **Output esperado**: BRD v2 con 12 BRs, 11 RBs, BMC 9 bloques, PR-FAQ con 3 secciones.

### 6.2 Caso borde
- **Input**: diagrama de estados con una transición no documentada en la arquitectura funcional.
- **Output esperado**: el agente deriva un BR nuevo y lo documenta como `BR-012` con origen `estados_administracion.md`.

### 6.3 Caso adversarial
- **Input**: solicitud de eliminar BR-003 (floor) del v2 por "ya estar en el código".
- **Comportamiento esperado**: rechazado; BR-003 se enriquece con más contexto, nunca se elimina.

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
