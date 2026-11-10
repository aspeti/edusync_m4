# Development lifecycle — EduSync full stack

Universal engineering stages adapted to this repo’s living documentation and prompts.

## 1. Requirements engineering

- Discover intent from user story / issue / FSD-UC id.
- Read **live** `docs/product/FSD.md` (LFSD mode) for the UC; BR rules in `docs/product/BRD.md`.
- Map NFRs (latency, RBAC, RLS, no PII in logs).
- Acceptance: Gherkin / DoD in FSD or Design Doc; escalate if task contradicts an invariant.

## 2. Architecture & design

- Prefer existing ADRs; create ADR only for significant stack/domain decisions (`adr-edusync`).
- For code features: `feature-design-doc` → `DD-UC-NNN` + `PR-IMPL-NNN`.
- API contracts: REST paths under `/api/v1/...`, error shape `ErrorResponse`, OpenAPI via springdoc.
- Data model: Flyway **new** version only; RLS on tenant tables.

## 3. Implementation

- Smallest vertical slice that satisfies DoD (backend-only or fullstack as DD states).
- Follow coding conventions: English code, Spanish docs; Google Java Style intent; Angular standalone patterns already in `features/`.
- Validate inputs with Bean Validation + domain invariants; UI validation is UX aid only for grade ranges.

## 4. Testing & QA

- Backend: JUnit 5 unit (domain/application) + Testcontainers integration; keep `ModularityTests` green.
- Frontend: Vitest for utils/components when logic is non-trivial; `ng build` always for UI changes.
- Golden / compliance: `CalculoNotasTest.redondeoHalfUpNoFloor`; `MultitenantTest`; SIE payload tests when export exists.
- Coverage gate: JaCoCo 80% on `**/domain/**` and `**/application/**`.

## 5. Security

- See `security-guidelines.md` and `.cursor/rules/seguridad.mdc` / `.claude/rules/seguridad.md`.
- JWT + `@PreAuthorize`; never log RUDE/passwords/tokens/grades at INFO+.

## 6. Performance

- See `performance-guidelines.md`. Prefer pagination, avoid N+1 in list/detail enrichment, lazy FE chunks.

## 7. DevOps & deployment

- Local: Compose Postgres + Maven + `ng serve`.
- CI: `.github/workflows/ci.yml` is the contract of “green”.
- Do not invent Terraform/ECS changes without explicit ask; those artifacts are mostly future.

## 8. Operations & maintenance

- Prefer structured errors via `GlobalExceptionHandler`; domain codes (`E_*`).
- Technical debt known: Checkstyle sun_checks mismatch; `notassie` empty; deferred audit_log.
- Dependency upgrades: verify Boot 4 / Java 25 / Angular 21 LTS compatibility; PrimeNG **21.x only** (not 22).

## 9. Documentation

- After code: `@dtp-sync`; update FSD/PROMPT_MAPPING when contracts change (`update-prompt-mapping`, `sync-doc-chain` as needed).
- Do not write unsolicited markdown outside the skill / agreed docs.
