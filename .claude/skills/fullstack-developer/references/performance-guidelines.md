# Performance guidelines — EduSync

## Frontend

- Lazy routes already used — keep heavy PrimeNG screens in their feature chunks.
- Notes `p-table`: avoid recalculating aggregates client-side; bind server provisional/final values.
- Watch `ng build` budgets; PrimeNG chunk can be large — do not eagerly import entire PrimeNG in `app.config` beyond what’s required for theme/providers.
- Prefer pagination over loading unbounded lists.

## Backend

- FSD NFR: POST calificaciones p95 &lt; 500 ms (target) — design indexes and avoid N+1 when enriching lists.
- Use `Specification` + pagination; do not `findAll()` entire tenants in list endpoints.
- Virtual threads / Java 25: available for future high-throughput SIE export; use when implementing export jobs, not preemptively everywhere.

## Database

- Index FK and filter columns introduced in Flyway.
- RLS policies should remain simple (tenant match); avoid per-row functions that kill plans.
- Export/batch: process in chunks; idempotent skips for `ENVIADO`.

## Caching

- No Redis/cache layer confirmed in runtime stack — do not introduce without ADR.
- HTTP caching of authenticated academic data is generally inappropriate.

## Concurrency

- Grade updates while period `ABIERTO`: last-write-wins today (append-only deferred). Document races in DD if tightening.
- Schedulers (`@Scheduled`): assume single instance locally; ShedLock not adopted yet.
