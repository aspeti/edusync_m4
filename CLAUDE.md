# Claude Code — EduSync

@AGENTS.md

Lee y sigue **siempre** `AGENTS.md` (importado arriba) como fuente de verdad del producto, stack, invariantes de dominio y guardrails. El resto de este archivo solo añade lo específico de Claude Code para no duplicar reglas.

## Paridad Cursor ↔ Claude

| Recurso | Cursor | Claude Code |
|---------|--------|-------------|
| Instrucciones del repo | `AGENTS.md` | `AGENTS.md` (import) + este `CLAUDE.md` |
| Skills del proyecto | `.cursor/skills/<slug>/SKILL.md` | `.claude/skills/<slug>/SKILL.md` (**mismos 13 slugs**) |
| Reglas | `.cursor/rules/*.mdc` | `.claude/rules/*.md` (espejo) |
| Subagentes | descritos en `AGENTS.md` §8.1 | `.claude/agents/*.md` |
| Hooks de protección baseline | `.cursor/hooks.json` | Sin equivalente automático; respetar rules + `AGENTS.md` §8.2 |

## Skills disponibles (invocar con `/nombre` o pedir la tarea)

| Skill | Cuándo usarlo |
|-------|----------------|
| `feature-design-doc` | Crear/actualizar `DD-UC-NNN` + `PR-IMPL-NNN` |
| `dtp-sync` | Tras implementar código: sincronizar `docs/product/DTP.md` |
| `update-prompt-mapping` | Registrar un prompt-contrato en `docs/PROMPT_MAPPING.md` |
| `materialize-prompt-files` | Backfill de `prompts/PR-*.md` desde el catálogo |
| `adr-edusync` | Crear un ADR formal |
| `c4-edusync` | Diagramas C4 Mermaid |
| `dti-edusync` | (Histórico) secciones DTI — el vivo es `docs/product/DTP.md` |
| `sync-doc-chain` | Propagar BRD→PRD→FSD→ADR↔diagramas |
| `edusync-skill-creator` | Nuevo skill EduSync (**MUST** escribir espejo en `.cursor/` y `.claude/`) |
| `poc-runner-edusync` | Scaffold ejecutable de POC-01/POC-02 |
| `async-architecture-reviewer` | Auditar arquitectura asíncrona (DTI §7) |
| `distributed-architecture-reviewer-edusync` | Auditar arquitectura distribuida (DTI §6) |
| `monolith-decomposition-architect` | Propuesta de descomposición / seams |

## Subagentes

Definidos en `.claude/agents/` (`dev-agent`, `docs-agent`, `arch-agent`, `qa-agent`, `process-agent`, `compliance-agent`). Preferir el subagente cuyo alcance coincida. Límites: `AGENTS.md` §8.1.

## Guardrails no negociables (recordatorio)

1. **Nunca** editar `docs/baseline/**`. Cambios → `docs/product/` (+ ADR si aplica).
2. Stack vivo: Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS (`ADR-0008`).
3. Sin secretos ni PII en código/logs (`AGENTS.md` §7, `.claude/rules/seguridad.md`).
4. Antes de proponer PR: `mvn test` (y `ng build` si toca frontend) en verde.

## Flujo recomendado (capa viva `release/3.0.0`)

```text
FSD-UC vivo → feature-design-doc → DD-UC-NNN + PR-IMPL-NNN
  → ejecutar PR-IMPL (código)
  → dtp-sync
  → commit (solo si el humano lo pide)
```
