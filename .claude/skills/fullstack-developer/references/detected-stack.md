# Detected Stack — EduSync (evidence-based)

> Regenerar o actualizar cuando cambien `pom.xml`, `frontend/package.json`, `infra/`, CI o ADRs de stack.
> Fecha de descubrimiento: **10/11/2026**. Repo: `edusync_m4`.

## Confidence legend

| Tag | Meaning |
|-----|---------|
| **Confirmed** | In dependency file **and** used in source / config |
| **Authorized-not-installed** | ADR / AGENTS authorizes it; not yet in `pom.xml` / `package.json` |
| **Documented-future** | Specs/DTP mention it; no runtime artifact yet |
| **Unknown** | Not determined from repo evidence |

---

## A. Technology inventory

| Layer | Technology | Version | Evidence | Confidence |
|-------|------------|---------|----------|------------|
| Frontend language | TypeScript | ~5.9.2 | `frontend/package.json` `devDependencies.typescript` | Confirmed |
| Frontend framework | Angular (standalone) | **21.2.x** (`@angular/core` ^21.2.0; CLI/build 21.2.19) | `package.json`, `angular.json` (`@angular/build:application`) | Confirmed |
| UI library | PrimeNG + `@angular/cdk` + `@primeuix/themes` | primeng **21.1.10**, cdk **21.2.14**, themes **3.1.0** | `package.json`; `providePrimeNG` in `app.config.ts`; `ADR-0026` | Confirmed |
| Design tokens | Custom CSS variables (SCSS) | n/a | `frontend/src/styles/_tokens.scss`, `ADR-0015` | Confirmed |
| Forms | Angular Reactive Forms / `FormsModule` | Angular 21 | `@angular/forms`; feature pages | Confirmed |
| State | Component signals + services (no NgRx) | Angular signals | e.g. `evaluacion-calificaciones.page.ts` | Confirmed |
| Routing | `@angular/router` + lazy routes | Angular 21 | `app.routes.ts` | Confirmed |
| HTTP | `HttpClient` + interceptor | Angular 21 | `auth.interceptor.ts` | Confirmed |
| CSS | SCSS (inline + global); **no** Tailwind / Bootstrap | — | `angular.json` `inlineStyleLanguage: scss` | Confirmed |
| Build | Angular application builder | 21.2.19 | `angular.json` | Confirmed |
| FE tests | Vitest + jsdom + coverage-v8 | vitest **4.1.10** | `package.json`; CI `npm run test:coverage` | Confirmed |
| FE format | Prettier | ^3.8.1 | `package.json` | Confirmed |
| FE animations | `@angular/animations` (PrimeNG overlays) | **21.2.18** | `package.json`; `provideAnimationsAsync` in `app.config.ts` | Confirmed |
| FE package manager | npm | packageManager `npm@8.5.0`; CI Node **22** | `package.json`, `.github/workflows/ci.yml` | Confirmed |
| Backend language | Java | **25** (LTS) | `pom.xml` `<java.version>25</java.version>`; CI Temurin 25 | Confirmed |
| Backend framework | Spring Boot | **4.1.0** (Spring Framework 7.x) | `spring-boot-starter-parent` | Confirmed |
| Modular monolith | Spring Modulith | **2.1.0** | `spring-modulith-bom` / starters; modules under `com.edusync.*` | Confirmed |
| Architecture | Hexagonal per module (`domain` / `application` / `infrastructure`) | — | package tree; `ADR-0011` | Confirmed |
| Modules | `plataforma`, `identidad`, `academico`, `notassie`, `shared` | — | `backend/src/main/java/com/edusync/` | Confirmed |
| API | REST (Spring MVC) | — | `*Controller` in `infrastructure.adapter.in.rest` | Confirmed |
| API docs | springdoc-openapi | **3.0.3** | `pom.xml`; Swagger UI | Confirmed |
| Validation | Jakarta Bean Validation | Boot BOM | `spring-boot-starter-validation` | Confirmed |
| AuthN/Z | Spring Security + JWT HS256 (jjwt) | jjwt **0.13.0** | `SecurityConfig`, `JwtTokenProvider` | Confirmed |
| Boilerplate | Lombok (domain allowlist) | **1.18.46** | `pom.xml`; `ADR-0012` | Confirmed |
| ORM | Spring Data JPA / Hibernate (via Boot) | Boot-managed | `spring-boot-starter-data-jpa` | Confirmed |
| DB | PostgreSQL | **15** (image) | `infra/docker-compose.yml` `postgres:15` | Confirmed |
| Migrations | Flyway | Boot starter + `flyway-database-postgresql` | `V1`…`V13` under `db/migration/` | Confirmed |
| Multitenancy | PostgreSQL RLS `FORCE` + `SET` tenant + app filters | — | e.g. `V5__…sql`; `TenantAwareDataSource` | Confirmed |
| Scheduling | Spring `@Scheduled` | — | `VencimientoSchedulerJob` | Confirmed |
| Async | Spring Events / `@Async` (pattern for SIE) | — | Specs/`ADR-0004`/`ADR-0025`; full SIE not coded yet | Documented-future (SIE) |
| LLM | Spring AI behind `LlmPort` | **2.0.0** | starters ollama + openai; adapters Gemini/Ollama | Confirmed |
| Resilience SIE | Resilience4j circuit breaker | TBD at `PR-IMPL-031` | `ADR-0025`/`AGENTS` §4; **absent from pom** | Authorized-not-installed |
| PDF | Apache PDFBox | — | AGENTS stack table; **absent from pom** | Documented-future |
| Envers / audit_log | Hibernate Envers + append-only | — | Specs; **deferred** (`ADR-0025` §3.7) | Documented-future |
| BE tests | JUnit 5 + Spring Boot Test + Security Test | Boot BOM | `pom.xml` | Confirmed |
| BE IT DB | Testcontainers PostgreSQL | **2.0.5** | `pom.xml` | Confirmed |
| Coverage | JaCoCo | **0.8.15**, gate **80%** domain+application | `pom.xml`; CI `mvn verify` | Confirmed |
| Mutation | Pitest | **1.25.9** | `pom.xml` profile; CI job | Confirmed |
| Checkstyle | Maven checkstyle (default sun_checks) | — | Gap: not Google Style (`AGENTS` checklist) | Confirmed (misconfigured) |
| Local infra | Docker Compose (Postgres only) | Compose file | `infra/docker-compose.yml` | Confirmed |
| Cloud / IaC | AWS ECS Fargate + Terraform (target) | — | ADRs/`AGENTS`; **no `.tf` / Dockerfile in repo** | Documented-future |
| CI/CD | GitHub Actions | — | `.github/workflows/ci.yml` | Confirmed |
| Red team | Python CLI catalog sync | — | CI step `tools/red-team-agent` | Confirmed |
| Messaging AWS SQS | — | — | Specs for later; not in code | Documented-future |
| Nx / Angular Material / NgRx / Tailwind | — | — | Not in `package.json` | Confirmed unused |
| AG Grid | — | — | Discarded (`ADR-0026`) | Confirmed unused |

### Exact Angular package pins (from `package.json`)

- Runtime: `@angular/*` ^21.2.0 (resolved with lockfile; CLI/build **21.2.19**).
- `rxjs` ~7.8.0.
- CI Node **22** (not declared as `engines` in package.json beyond lockfile `node >= 14`).

---

## B. Architecture overview

### Frontend

- SPA Angular 21 **standalone** components; feature folders under `frontend/src/app/features/{auth,plataforma,usuarios,academico,asistente,…}`.
- `core/auth` (JWT in `sessionStorage`), `core/api`, `shared/layout`, `shared/ui` (tokens-era atoms), PrimeNG for new UI (`ADR-0026`).
- Lazy-loaded routes; HTTP via interceptor; **no** global NgRx store.
- Notes grid must **not** compute averages (backend `CalculoNotas` only).

### Backend

- Package base `com.edusync`; Spring Modulith **module-first**: `plataforma`, `identidad`, `academico`, `notassie` (mostly empty), `shared` (OPEN).
- Each business module: hexagonal `domain` (no Spring/JPA), `application` (use cases/ports), `infrastructure` (REST/JPA/security adapters).
- Cross-module via explicit ports (e.g. `ProfesorConsultaPort`, `TenantConsultaPort`).

### Database

- PostgreSQL 15; Flyway `V1`–`V13`; UUID PKs; `tenant_id` + RLS `FORCE` on tenant tables; app also filters by tenant.
- No checked-in stored procedures/views as primary design; policies + indexes in SQL migrations.

### Communication & externals

- Browser → Angular → REST `/api/v1/**` → Spring controllers → use cases → ports → JPA / LLM adapters.
- LLM: Ollama default; Gemini optional (`ADR-0023`) via OpenAI-compatible endpoint.
- SIE HTTP export: designed (`DD-UC-031` / `ADR-0025`), **not implemented**; Resilience4j not on classpath yet.

### Infrastructure

- Local: Compose Postgres only; `mvn spring-boot:run` / `ng serve`.
- Target (docs): AWS ECS Fargate, RDS, Secrets Manager — **not** provisioned in-repo.
- CI: backend `mvn verify` + JaCoCo; frontend `npm ci` + `ng build` + Vitest coverage; Pitest job; red-team catalog check.

---

## C. Non-goals / explicit absences

- Do **not** assume .NET, React, Vue, Nest, Prisma, MongoDB, Kafka-in-prod, or Kubernetes manifests exist here.
- Do **not** reintroduce `floor()` or dual Bolivia-dimension engines (`ADR-0025` / `ADR-0013`).
- Do **not** edit `docs/baseline/**`.
