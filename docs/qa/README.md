# QA — Auditoría de pruebas EduSync

Herramientas y artefactos para auditar la suite de tests (no sustituyen `mvn test` local).

## Herramientas

| Herramienta | Qué mide | Comando | Gate |
|-------------|----------|---------|------|
| **JaCoCo** | Cobertura de líneas en `domain/` + `application/` | `cd backend && mvn verify` (suite completa + Docker) | ≥ **80 %** (`NFR-013`) |
| **JaCoCo (sin Docker)** | Igual, excluye `*IntegrationTest` | `cd backend && mvn -Punit verify` | ≥ **70 %** (muchos services solo se cubren en integration) |
| **Pitest** | Mutaciones sobrevivientes en dominio crítico | `cd backend && mvn -Punit test org.pitest:pitest-maven:mutationCoverage` | Umbral mutación 60 % (también perfil `mutation` en verify) |
| **Vitest (Angular)** | Cobertura frontend (`core/auth` y specs existentes) | `cd frontend && npm run test:coverage` | Report + umbrales en `angular.json` |
| **Matriz FSD-UC** | Trazabilidad caso de uso ↔ tests | [`matriz-fsd-uc-tests.md`](./matriz-fsd-uc-tests.md) | Revisión humana / `qa-agent` |
| **CI** | Enforcement en PR | `.github/workflows/ci.yml` | `mvn verify` + `npm run test:coverage` |

## Reportes locales

- JaCoCo HTML: `backend/target/site/jacoco/index.html`
- Pitest HTML: `backend/target/pit-reports/index.html`
- Frontend coverage: `frontend/coverage/`

## Alcance del umbral JaCoCo

Incluye solo clases bajo `**/domain/**` y `**/application/**`.  
Excluye: `package-info`, `*Exception`, `*Id` (value objects triviales), e **infrastructure** (adapters REST/JPA).

## Baseline medido (2026-09-10)

| Métrica | Resultado |
|---------|-----------|
| JaCoCo `-Punit` (domain+application) | **70.6 %** líneas → gate local 70 % |
| Pitest (`CalculoNotas`, `Usuario`, `GestionEscolar`) | **74 %** mutaciones killed (umbral 60 %) |
| Frontend `npm run test:coverage` (`core/auth`) | **97 %** líneas / 22 tests verdes |

El gate CI de backend exige **80 %** con suite completa (incluye `*IntegrationTest` + Docker/Testcontainers).

## Nota

`checkstyle` sigue con el gap documentado (`sun_checks`); no forma parte de este pipeline de auditoría de tests.
