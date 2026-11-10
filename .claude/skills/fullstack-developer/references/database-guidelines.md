# Database guidelines — PostgreSQL 15 + Flyway + RLS

## Confirmed

- Engine: **PostgreSQL 15** (`infra/docker-compose.yml`).
- Migrations: Flyway under `backend/src/main/resources/db/migration/` (`V1`…`V13` at discovery).
- Access: Spring Data JPA; Specifications for dynamic filters.
- Multitenancy: column `tenant_id` + `ENABLE ROW LEVEL SECURITY` + `FORCE ROW LEVEL SECURITY` + policy using `current_setting('app.tenant_id', true)` (pattern in later V* scripts). App also sets tenant on connection (`TenantAwareDataSource`).

## Do

- New tables: UUID PK, `tenant_id NOT NULL` for tenant data (exception: platform-scoped like `usuario` with `tenant_id IS NULL` for SYSADMIN — follow existing precedent).
- Always add RLS policy consistent with sibling tables; include indexes for list filters (`q`, FKs).
- Enforce uniqueness in DB where domain requires it (e.g. RUDE per tenant).
- Integration tests with Testcontainers prove cross-tenant 404/empty.

## Don’t

- Rewrite or edit already-applied migrations on `main`.
- Rely on ORM alone without RLS for tenant tables.
- Put grade averages in SQL views as source of truth (domain engine owns calculation).
- Commit connection strings with secrets; use env / Compose defaults for local only.

## Unknown / deferred

- Hibernate Envers wiring and physical `audit_log` enforcement rules: deferred per `ADR-0025`.
- RDS/Terraform provisioning: not in repo.
