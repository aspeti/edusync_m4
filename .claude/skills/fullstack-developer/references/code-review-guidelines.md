# Code review guidelines — EduSync full stack

Use when reviewing PRs or self-checking before handoff.

## Block merge if

1. Domain invariant broken (RUDE, `floor()`, cross-tenant, grades outside `CalculoNotas`).
2. `docs/baseline/**` touched.
3. Flyway history rewritten.
4. Secrets / PII in code or logs.
5. Modulith cycle / `ModularityTests` red.
6. JaCoCo gate failed on domain/application for touched code without justification + human OK.
7. New UI library or PrimeNG 22 without ADR.
8. SIE re-send of `ENVIADO` or export of non-`CERRADO` data (when export code exists).

## Strongly request changes if

- Controllers contain business rules that belong in domain/application.
- Angular duplicates server calculations.
- Missing tests for new branches / error codes.
- Public API shape drifts from FSD/DD without doc sync.
- Checkstyle/noise only: note known sun_checks gap; don’t demand mass reformat.

## Nit / optional

- Naming English consistency; Spanish only in docs/Javadoc of domain concepts.
- Extract shared list/filter UI when a third copy appears.

## Reviewer checklist (short)

- [ ] Correct module + hexagonal placement
- [ ] RBAC + tenant on every new endpoint/table
- [ ] Tests + build commands green as claimed
- [ ] DTP / DD / FSD / PROMPT_MAPPING updated if the change is a closed IMPL slice
- [ ] Diff size reasonable (~400 LOC net guideline) or split explained
