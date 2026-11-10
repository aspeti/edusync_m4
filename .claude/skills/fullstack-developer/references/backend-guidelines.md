# Backend guidelines — Java 25 / Spring Boot 4.1 / Modulith

## Stack (confirmed)

- Java **25**, Spring Boot **4.1.0**, Spring Modulith **2.1.0**, package `com.edusync`.
- Hexagonal modules: `domain` / `application` / `infrastructure`.
- JPA + Flyway + PostgreSQL; Security JWT; springdoc 3.0.3; Lombok; Bean Validation.
- Spring AI **2.0.0** only behind `LlmPort` in `shared.ai`.
- Tests: JUnit 5, Mockito, Testcontainers, JaCoCo 80%, Pitest profile.

## Do

- New features: domain aggregate → ports → application service → REST adapter → JPA adapter → Flyway `V{n+1}`.
- Domain: factory methods, invariant checks, Lombok **allowlist** only (`@Getter` / `@EqualsAndHashCode` / `@ToString`); no `@Data`/`@Setter`/`@Builder` public on aggregates.
- REST: DTOs + `@Valid`; `@PreAuthorize` with real roles (`SYSADMIN`, `ADMIN`, `SECRETARIA`, `PROFESOR`, …).
- Errors: domain exceptions → `GlobalExceptionHandler` → `ErrorResponse` codes `E_*`.
- Tenant: resolve via `TenantContextProvider`; never trust client `tenantId` for isolation.
- Grade math: only `academico` `CalculoNotas` (HALF_UP). No `floor()`.
- Keep `ApplicationModules.verify()` green; public ports for cross-module calls.

## Don’t

- Put Spring/JPA annotations on domain entities.
- Expose JPA entities on controllers.
- Modify applied Flyway scripts; only append.
- Add Resilience4j/PDFBox/etc. without aligning ADR + pom (SIE path uses `PR-IMPL-031`).
- Log RUDE, passwords, JWTs, or individual grades at INFO+.
- Edit `docs/baseline/**`.

## Module notes

- `notassie`: implement only via DD/PR-IMPL (export SIE); wire Resilience4j in infrastructure adapter when authorized.
- `shared`: OPEN module — keep thin (web, tenant, AI, exceptions).

## Verification

```bash
cd backend && mvn test
# Prefer mvn verify before PR when touching domain/application (JaCoCo gate)
```
