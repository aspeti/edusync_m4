# PR-SKILL-001 — Creación del skill update-prompt-mapping (Cursor + Claude)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-SKILL-001` |
| Título | Creación del Agent Skill `update-prompt-mapping` para Cursor y Claude Code |
| Artefacto origen | PROMPT_MAPPING.md v0.5 + SKILL_TEMPLATE.md |
| ID origen | `18 prompt-contratos PR-ARCH-001..PR-LFSD-001` |
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
Eres un Senior AI Solutions Architect y Prompt Engineer especializado en
documentacion de proyectos de IA y sistemas de gestion del conocimiento para
equipos que usan Cursor IDE y Claude Code / Claude Desktop.
```

### 1.2 Task
```text
Crea un Agent Skill para Cursor y Claude Code que guie al agente en la
actualizacion correcta de docs/PROMPT_MAPPING.md del proyecto EduSync,
cubriendo los 7 pasos obligatorios del protocolo de registro de prompts.
```

### 1.3 Context
```text
- Documento objetivo: docs/PROMPT_MAPPING.md v0.5 (1375 líneas, 18 prompts).
- Destinos: .cursor/skills/update-prompt-mapping/ y .claude/skills/update-prompt-mapping/.
- Plantilla: plantillas/SKILL_TEMPLATE.md.
- Protocolo: 7 secciones de PROMPT_MAPPING a modificar en orden.
```

### 1.4 Reasoning
```text
1. Analizar PROMPT_MAPPING.md completo para identificar las 7 secciones modificables.
2. Definir entradas obligatorias del skill (ID, artefacto, tipo, agente, modelo, fecha).
3. Redactar el procedimiento paso a paso con plantillas exactas.
4. Crear SKILL.md (< 500 líneas) y reference.md con plantillas copy-paste.
5. Copiar a .cursor/skills/ y .claude/skills/.
```

### 1.5 Stop condition
```text
Detente cuando SKILL.md y reference.md existan en ambas rutas y el checklist esté completo.
```

### 1.6 Output
```text
.cursor/skills/update-prompt-mapping/SKILL.md (185 líneas) + reference.md (168 líneas).
.claude/skills/update-prompt-mapping/ (idénticos).
```

## 2. Invariantes del prompt

- SKILL.md < 500 líneas.
- Plantillas en `reference.md` usan datos reales del proyecto EduSync.
- El skill debe ser activable sin modificación en Cursor y en Claude Code.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_SKILL_DEMASIADO_LARGO` | SKILL.md supera 500 líneas | Mover contenido a reference.md |
| `E_PLANTILLA_GENERICA` | Plantillas con datos ficticios | Reemplazar con ejemplos reales |
| `E_RUTA_INCORRECTA` | Skill fuera de .cursor/skills/ | Verificar y mover |

## 4. Guardrails

- MUST: validar que SKILL.md < 500 líneas antes de entregar.
- MUST NOT: incluir datos ficticioos en las plantillas del skill.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| PROMPT_MAPPING.md v0.5 + SKILL_TEMPLATE.md | `18 prompt-contratos` | PR-SKILL-001 | `docs-agent` | `.cursor/skills/update-prompt-mapping/SKILL.md` + `reference.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: PROMPT_MAPPING.md v0.5 completo + SKILL_TEMPLATE.md.
- **Output esperado**: SKILL.md de ~185 líneas, reference.md de ~168 líneas, en ambas rutas.

### 6.2 Caso borde
- **Input**: PROMPT_MAPPING.md con una nueva área no documentada (ej. área `HEX`).
- **Output esperado**: el skill incluye la nueva área en la tabla de IDs válidos de reference.md.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir instrucciones para modificar directamente AGENTS.md desde el skill.
- **Comportamiento esperado**: rechazado; el skill solo gestiona PROMPT_MAPPING.md.

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
