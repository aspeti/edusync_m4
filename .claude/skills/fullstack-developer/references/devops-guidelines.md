# DevOps guidelines — EduSync

## Confirmed local & CI

- Local DB: `infra/docker-compose.yml` → PostgreSQL 15.
- Backend run: Maven Wrapper / `mvn spring-boot:run` from `backend/`.
- Frontend: `npm ci` / `npm start` in `frontend/` (Node **22** in CI).
- CI: `.github/workflows/ci.yml` — JDK 25 Temurin, Node 22, `mvn verify`, `ng build`, Vitest coverage, Pitest, red-team catalog check.

## Git

- Conventional Commits (`feat:`, `fix:`, `docs:`, …).
- No force-push to `main`/`develop`; no `--no-verify` unless human explicitly requests.
- Commit only when the user asks.

## Cloud / IaC (documented future)

- Target: AWS ECS Fargate, RDS PostgreSQL, Secrets Manager (`ADR-0006`).
- **No** production Dockerfile / Terraform modules found in-repo at discovery — do not invent infra unless tasked.
- Folders like `infra/rds|sqs|ecs` may be placeholders; verify before editing.

## Environments

- Spring profiles: `dev`, `test` (and future `prod`).
- Angular `environment` files as present under `frontend/src/environments/`.
- Never commit `.env` with secrets.

## Deploy / rollback

- Until ECS pipelines exist: treat “deploy” as merge + CI green + documented manual runbooks in README/DTP.
- Rollback: revert commit / previous image tag when containers exist — **unknown** operational runbook detail in repo.
