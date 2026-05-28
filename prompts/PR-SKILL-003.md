# PR-SKILL-003 — Creación del skill dti-edusync (Cursor + Claude)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-SKILL-003` |
| Título | Creación del Agent Skill `dti-edusync` para poblar y mantener el DTI de EduSync |
| Artefacto origen | plantillas/dti-author.md + DTI_TEMPLATE + FSD + LFSD |
| ID origen | `DA-01..DA-05`, `FSD-UC-001..009`, `23 secciones DTI` |
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
documentacion tecnica de productos de software y en la creacion de skills
para Cursor IDE y Claude Code.
```

### 1.2 Task
```text
Crea un Agent Skill para Cursor y Claude Code que guie al agente en la
creacion y mantenimiento del Documento Tecnico Inicial (DTI) de EduSync,
adaptando la plantilla generica plantillas/dti-author.md al proyecto real.
```

### 1.3 Context
```text
- Plantilla base: plantillas/dti-author.md (skill genérico del módulo).
- Plantilla DTI: plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md (620 líneas, 23 secciones).
- Destinos: .cursor/skills/dti-edusync/ y .claude/skills/dti-edusync/.
- Stack EduSync: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS ECS Fargate.
- Fuentes disponibles: FSD v1.0, LFSD v1.0, AGENTS.md v0.2, arquitectura funcional,
  PROMPT_MAPPING.md v0.6, C4 diagrams.
```

### 1.4 Reasoning
```text
1. Leer plantillas/dti-author.md para entender la estructura del skill genérico.
2. Leer la plantilla DTI para mapear las 23 secciones.
3. Crear tabla de mapeo: cada sección DTI → datos reales de EduSync.
4. Redactar SKILL.md con procedimiento de 5 pasos, checklist y anti-patrones.
5. Copiar a .cursor/skills/ y .claude/skills/.
```

### 1.5 Stop condition
```text
Detente cuando SKILL.md exista en ambas rutas, tenga < 500 líneas, y la tabla
de mapeo de las 25 secciones esté completa con datos reales.
```

### 1.6 Output
```text
.cursor/skills/dti-edusync/SKILL.md (159 líneas) con: frontmatter válido,
tabla de mapeo de 25 secciones, procedimiento de 5 pasos, checklist de 12 items.
.claude/skills/dti-edusync/ (copia idéntica).
```

## 2. Invariantes del prompt

- Tabla de mapeo usa datos reales del proyecto, no placeholders.
- `§3.5` siempre marcado N/A para EduSync v1.0 (sin agentes IA en runtime).
- SKILL.md < 500 líneas.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_SECCION_SIN_DATOS` | Sección mapeada a placeholder genérico | Reemplazar con datos EduSync |
| `E_SKILL_DEMASIADO_LARGO` | SKILL.md supera 500 líneas | Condensar tabla de mapeo |
| `E_AGENTES_RUNTIME` | §3.5 con contenedores agénticos que no existen | Marcar N/A |

## 4. Guardrails

- MUST: validar que la tabla de mapeo tiene las 25 secciones con datos reales.
- MUST NOT: crear agentes IA en runtime (§3.5 siempre N/A para EduSync v1.0).

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| plantillas/dti-author.md + DTI_TEMPLATE + FSD + LFSD | `DA-01..DA-05`, `FSD-UC-001..009` | PR-SKILL-003 | `docs-agent` | `.cursor/skills/dti-edusync/SKILL.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: dti-author.md + DTI_TEMPLATE + FSD v1.0 completo.
- **Output esperado**: SKILL.md de 159 líneas con tabla de 25 secciones, en ambas rutas.

### 6.2 Caso borde
- **Input**: DTI_TEMPLATE con una sección nueva no documentada en dti-author.md.
- **Output esperado**: el agente añade la sección a la tabla de mapeo con datos derivados del stack.

### 6.3 Caso adversarial
- **Input**: propuesta de documentar en §3.5 un agente IA que ejecuta tests automáticos.
- **Comportamiento esperado**: rechazado; §3.5 se marca N/A (EduSync v1.0 no tiene agentes en runtime).

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
