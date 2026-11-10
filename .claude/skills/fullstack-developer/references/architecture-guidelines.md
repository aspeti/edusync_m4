# Architecture guidelines — EduSync

Load this when designing features, reviewing module boundaries, or writing ADRs/DDs.

## System shape

- **Style**: modular monolith (Spring Modulith) + SPA, not microservices (Strangler Fig `ADR-0007` gated).
- **Backend package**: `com.edusync.{plataforma|identidad|academico|notassie|shared}`.
- **Layering per module**: `domain` → `application` → `infrastructure` (hexagonal). Domain **MUST NOT** import Spring/JPA/AWS.
- **Frontend**: Angular standalone features; HTTP only through authenticated API; no domain calculation of grades in UI.

## Bounded contexts (modules)

| Module | Responsibility | Maturity |
|--------|----------------|----------|
| `plataforma` | Tenants, subscription lifecycle | Implemented |
| `identidad` | Users, roles, JWT, password reset | Implemented |
| `academico` | Gestión escolar → calificaciones + `CalculoNotas` | Implemented (core SaaS) |
| `notassie` | Perfil Bolivia SIE export / consolidación | Mostly empty; `PR-IMPL-031` pending |
| `shared` | Tenant context, web errors, AI, OPEN module | Implemented |

Prefer **Open Host Service** ports at module roots over leaking infrastructure types across modules. Verify with `ModularityTests` / `ApplicationModules.verify()`.

## Design artifacts (order)

1. Live specs: `docs/product/{BRD,PRD,FSD}.md`, technical contract `docs/product/DTP.md`.
2. Significant decisions → `docs/adr/NNNN-*.md` (skill `adr-edusync`).
3. Feature slice → `docs/design/DD-UC-NNN.md` + `docs/prompts/impl/PR-IMPL-NNN.md` (`feature-design-doc`).
4. After code → `@dtp-sync`. Never edit `docs/baseline/**`.

## Domain invariants (non-negotiable)

- Student identity key: **RUDE** only on grade/SIE writes.
- Single grade engine: `CalculoNotas`, `round` HALF_UP; **no** `floor()` / `Math.floor` (`ADR-0025`).
- Period must be `ABIERTO` for grade writes (`422 E_PERIODO_NO_ABIERTO`).
- Multitenancy: `tenant_id` + RLS; no cross-tenant reads/writes.
- Export SIE: idempotent by `rude+materia_id+periodo_id`; only `CERRADO` + complete sections (when implemented).
- `audit_log` / append-only grades / `AutorizacionCorreccion`: **deferred** — do not invent implementations without ADR + DD.

## Patterns in use

- Aggregate roots with private ctor + factory (`crear()`), controlled mutators; Lombok `@Getter` allowlist in domain (`ADR-0012`).
- Application services as use cases; ports in/out; REST DTOs in infrastructure (never expose JPA/domain on API).
- Shared pagination: `PageQuery` / `PageResult` / `PageResponse`.
- Specs: Spring Data `Specification` for list filters.
- Frontend: feature services + signals; PrimeNG for interactive UI (`ADR-0026`); design tokens (`ADR-0015`).

## Anti-patterns

- Dual calculation profiles (Bolivia dimensions vs generic) — removed as product policy.
- Calculating section/period averages in SQL, controllers, or Angular.
- Cross-module cycles; dumping all types into `shared`.
- New UI library besides PrimeNG without a new ADR.
- Editing frozen baseline docs for “quick fixes”.
