# Testing guidelines — EduSync

## Backend

| Layer | Tooling | Focus |
|-------|---------|--------|
| Domain | JUnit 5 | Aggregates, `CalculoNotas`, state transitions |
| Application | JUnit + Mockito | Use cases, port interactions |
| Integration | `@SpringBootTest` + Testcontainers PostgreSQL | REST + RLS + Flyway |
| Modular | Spring Modulith test | No illegal module cycles |
| Coverage | JaCoCo | ≥80% lines on `domain` + `application` |
| Mutation | Pitest (CI job / profile) | Critical domain |

Commands: `mvn test`; before PR prefer `mvn verify`. Golden: `CalculoNotasTest.redondeoHalfUpNoFloor`, multitenant tests; SIE payload when export exists.

## Frontend

| Layer | Tooling | Focus |
|-------|---------|--------|
| Unit | Vitest + jsdom | Pure utils (e.g. grid helpers), services when valuable |
| Build gate | `ng build` | Compilation / budgets |
| E2E | — | **Unknown / not established** in app (no Playwright/Cypress in `package.json`) |

`ng test` may need browser packages in some environments — document limitations; do not claim E2E coverage that does not exist.

## QA practice

- Add tests with every UC change; prefer reproducing bugs with a failing test first.
- Do not disable tests or lower JaCoCo gates to “make green”.
- Red-team catalog for AI assistant: `tools/red-team-agent` (separate skill).
