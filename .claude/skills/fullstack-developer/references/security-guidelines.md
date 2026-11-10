# Security guidelines — EduSync (OWASP ASVS L2 intent)

## Confirmed controls

- Authentication: JWT (jjwt), max session policy per FSD/NFR (8h documented).
- Authorization: Spring Security method security + role checks; Angular route guards.
- Tenant isolation: RLS FORCE + application tenant context.
- Secrets: env / secrets manager in target architecture; **never** commit API keys (e.g. `GEMINI_API_KEY` only in env).
- Validation: Bean Validation on inputs; domain range checks for grades.
- Logging: no PII / RUDE / passwords / tokens / per-student grades at INFO+.

## Rules files

- `.claude/rules/seguridad.md` and `.cursor/rules/seguridad.mdc` — follow on every Java/TS change touching auth, data, or AI.

## LLM / AI

- Do not send RUDE, names, or grades to models.
- Provider switches require ADR (`ADR-0023` Gemini optional).

## Checklist for changes

1. Is the endpoint authenticated? Correct roles?
2. Can tenant A see tenant B’s ids if guessed?
3. Are error messages free of sensitive interpolation (e.g. do not echo RUDE in `E_RUDE_DUPLICADO` messages)?
4. Any new dependency with known CVEs? Prefer Boot BOM versions.
5. Flyway: RLS present on new tenant tables?

## Deferred

- Full `audit_log` immutability enforcement in code.
- Production hardening of Swagger exposure (currently open in dev/test profiles).
