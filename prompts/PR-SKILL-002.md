# PR-SKILL-002 — Creación del skill c4-edusync (Cursor + Claude)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-SKILL-002` |
| Título | Creación del Agent Skill `c4-edusync` para generación de diagramas C4 |
| Artefacto origen | PROMPT_MAPPING.md v0.6 + plantillas/c4.md |
| ID origen | `20 prompt-contratos + stack EduSync real` |
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
arquitectura de software C4 y en la creacion de skills para Cursor IDE y
Claude Code, con dominio del stack EduSync (Java 21, Spring Boot 3.3,
PostgreSQL 15, Angular 17, AWS ECS Fargate).
```

### 1.2 Task
```text
Crea un Agent Skill para Cursor y Claude Code que guie al agente en la
generacion de diagramas C4 (Nivel 1, 2 y 3) para el proyecto EduSync,
usando Mermaid y la arquitectura real del sistema.
```

### 1.3 Context
```text
- Destinos: .cursor/skills/c4-edusync/ y .claude/skills/c4-edusync/.
- Plantilla base: plantillas/c4.md (skill genérico del módulo).
- Stack real: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS ECS Fargate.
- Actores: Director (Jeanneth), Docente (Marcela), Secretaria (Wendy), SIE, AWS KMS.
- Contenedores: Angular SPA, API Gateway, Domain Layer, PostgreSQL 15,
  Event Bus, SIE Adapter, Scheduler.
```

### 1.4 Reasoning
```text
1. Leer plantillas/c4.md para entender la estructura del skill genérico.
2. Identificar actores, sistemas externos y contenedores desde FSD y LFSD.
3. Crear SKILL.md con procedimiento de 4 pasos y trazabilidad FSD-UC ↔ C4.
4. Crear reference.md con bloques Mermaid copy-paste para Level 1, 2 y 3.
5. Copiar a .cursor/skills/ y .claude/skills/.
```

### 1.5 Stop condition
```text
Detente cuando SKILL.md y reference.md existan en ambas rutas, SKILL.md sea < 500 líneas,
y los anti-patrones EduSync estén documentados.
```

### 1.6 Output
```text
.cursor/skills/c4-edusync/SKILL.md (< 500 líneas) + reference.md con bloques Mermaid Level 1/2/3.
.claude/skills/c4-edusync/ (copia idéntica).
```

## 2. Invariantes del prompt

- Sin caracteres Unicode decorativos en labels Mermaid.
- Cada contenedor cita al menos un FSD-UC o DA que lo justifica.
- SKILL.md < 500 líneas.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_UNICODE_EN_LABELS` | Caracteres especiales en labels Mermaid | Reemplazar con ASCII |
| `E_CONTENEDOR_SIN_UC` | Contenedor sin FSD-UC asignado | Rechazar hasta completar |
| `E_SKILL_DEMASIADO_LARGO` | SKILL.md supera 500 líneas | Mover ejemplos a reference.md |

## 4. Guardrails

- MUST: validar que todos los contenedores tienen un FSD-UC asignado.
- MUST NOT: usar caracteres Unicode en labels de diagramas Mermaid.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| PROMPT_MAPPING.md v0.6 + c4.md + FSD + LFSD | `20 contratos + stack real` | PR-SKILL-002 | `docs-agent` | `.cursor/skills/c4-edusync/SKILL.md` + `reference.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: plantillas/c4.md + FSD + LFSD completos.
- **Output esperado**: SKILL.md < 500 líneas, reference.md con bloques L1/L2/L3, en ambas rutas.

### 6.2 Caso borde
- **Input**: LFSD sin documentar el contenedor Scheduler explícitamente.
- **Output esperado**: el agente lo infiere de los schedulers VentanaExpiracionScheduler y SIERetryScheduler.

### 6.3 Caso adversarial
- **Input**: propuesta de incluir em-dash en un label de Mermaid.
- **Comportamiento esperado**: rechazado con `E_UNICODE_EN_LABELS`; reemplazado con ASCII.

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
