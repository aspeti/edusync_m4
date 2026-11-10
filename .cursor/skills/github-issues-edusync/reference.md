# Referencia — github-issues-edusync

## Comandos `gh` útiles

```bash
gh auth status
gh repo view --json nameWithOwner -q .nameWithOwner
gh issue list --state open --limit 50
gh label list

gh issue create \
  --title "impl: Exportación nota final periodo (PR-IMPL-031)" \
  --label "type:impl" --label "area:notassie" --label "blocked" \
  --body-file - <<'EOF'
## Resumen
Ejecutar la exportación asíncrona al SIE según DD-UC-031.

## Trazabilidad
- FSD: `FSD-UC-004`
- Design Doc: `DD-UC-031`
- Prompt: `PR-IMPL-031`
- ADR: `ADR-0025` (debe estar *Aceptada*)

## Alcance
- [ ] Backend `notassie` + `V14` + Resilience4j
- [ ] Consola Angular (PrimeNG si `PR-IMPL-032` ya corrió)
- Fuera: inmutabilidad, audit_log, centralizador (diferidos)

## Criterio de hecho
- [ ] DoD `DD-UC-031` §7
- [ ] `mvn test` + `ng build` verdes
- [ ] Sin tocar `docs/baseline/**`
EOF
```

## Inventario típico (post ADR-0025/0026)

| Origen | Tipo sugerido | Nota |
|--------|---------------|------|
| `ADR-0025` Propuesta | `type:adr` + `blocked` para IMPL-031 | Aprobar antes de código |
| `PR-IMPL-031` | `type:impl` `area:notassie` | Tras ADR-0025 Aceptada |
| `PR-IMPL-030` | `type:impl` | RAG vectorial pendiente |
| Diferidos `ADR-0025` §3.7 | `type:deferred` | Un issue épico o varios chicos |
| Prueba manual ≥ 45 alumnos (`DD-UC-032`) | `type:docs` / chore frontend | No es código nuevo |

## Título recomendado

`{tipo}: {qué} ({ID})`

Ejemplos: `adr: Aprobar ADR-0025 (SIE nota final)`, `impl: Matriz p-table — prueba manual 45 alumnos (DD-UC-032)`.
