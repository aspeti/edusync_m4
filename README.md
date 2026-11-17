# EduSync

Plataforma SaaS B2B multitenant (EdTech / GovTech académico, mercado boliviano) que descentraliza el registro de calificaciones por rol, consolida centralizadores con la regla de cálculo vigente y sincroniza con el SIE mediante el código **RUDE**, eliminando la triple digitación.

Grupo **G-EduSync**. Capa viva: `release/3.0.0`. Baseline evaluado de M4 (inmutable): tag `release/2.0.0` en `docs/baseline/`.

## El problema

- Consolidación en hojas de cálculo y desfases de lista (notas asignadas al estudiante incorrecto).
- Transcripción nocturna al SIE por saturación de servidores.
- Rangos, redondeos y cierres de periodo sin enforcement; correcciones retroactivas sin auditoría.

## Qué hay implementado (`release/3.0.0`)

Monolito modular Spring Modulith (`com.edusync`) + SPA Angular. Casos de uso **completos backend + UI**:

| Área | FSD-UC |
|------|--------|
| Tenants y suscripciones | FSD-UC-011 |
| Usuarios, roles, login | FSD-UC-021 |
| Gestión escolar, periodos, secciones | FSD-UC-012, 013, 014 |
| Cursos, materias, profesores, estudiantes | FSD-UC-017, 018, 019, 020 |
| Evaluaciones y calificaciones (`round` HALF_UP, sin `floor()`) | FSD-UC-015, 016 |

Pendiente o parcial: perfil Bolivia SIE (`notassie`, FSD-UC-001 / 003 / 004 / 005 / 009 — 009 parcial), exportación SIE, `audit_log` formal, POCs con evidencia.

Asistente IA: `POST /api/v1/ai/chat` y `POST /api/v1/ai/agente` (ReAct + KEYWORD + CONSULTA + PROCESO). Proveedores: Ollama, Open WebUI, Gemini (`ADR-0023`). Monitor en línea opcional (`EDUSYNC_AI_MONITOR_*`).

## Stack vivo (`ADR-0008`)

| Capa | Tecnología |
|------|------------|
| Backend | Java 25 LTS, Spring Boot 4.1.0, Spring Modulith, PostgreSQL 15 + RLS, Flyway |
| Frontend | Angular 21 LTS (standalone) + PrimeNG 21.x (`ADR-0026`) |
| API docs | springdoc-openapi 3.0.3 + `swagger-annotations-jakarta` 2.2.47 (`ADR-0012`) |
| IA | Spring AI 2.0.0 detrás de `LlmPort` (Ollama / Open WebUI / Gemini) |
| Local | Docker Compose (`infra/docker-compose.yml`: `postgres` + `api`) |

El DTI congelado documenta Java 21 / Boot 3.3 / Angular 17 como hecho histórico. No editar `docs/baseline/**`.

## Cómo correr en local

Requisitos: JDK 25, Maven 3.9+, Node 20+, Docker.

### Opción A — stack completo en Docker (API + PostgreSQL)

```bash
# Variables locales (nunca commitear .env)
cp .env.example .env

# Build + up (API en :8080, Postgres en :5432)
docker compose -f infra/docker-compose.yml up -d --build

# Smoke de documentación viva
curl http://localhost:8080/v3/api-docs
# UI: http://localhost:8080/swagger-ui.html
```

La imagen de la API es multi-stage (`backend/Dockerfile`, usuario no root). Para la medición del laboratorio S08 (“antes”): `API_DOCKERFILE=Dockerfile.antes docker compose -f infra/docker-compose.yml up -d --build`.

### Opción B — API en el host + solo Postgres en Docker

```bash
docker compose -f infra/docker-compose.yml up -d postgres

cp .env.example .env
# Completar GEMINI_API_KEY / OPEN_WEBUI_API_KEY según el proveedor

cd backend
mvn -q spring-boot:run
# http://localhost:8080
```

### Frontend (ambas opciones)

```bash
cd frontend
npm install
npm start
# http://localhost:4200 → proxy a :8080
```

Seed local: `sysadmin@edusync.local` / `changeme_local_dev` (solo entorno vacío). JWT y password se sobreescriben con `JWT_SECRET` y `SYSADMIN_PASSWORD`.

### OpenAPI / Swagger

| Recurso | URL |
|---------|-----|
| Contrato JSON (springdoc) | http://localhost:8080/v3/api-docs |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Contrato versionado (CI) | `openapi.json` (raíz; regenerar con `scripts/export-openapi.ps1` / `.sh`) |

Públicos en perfiles `dev`/`test` (`SecurityConfig`). Si `/v3/api-docs` devolviera 500 por conflicto de jars Swagger, ver el pin `swagger-annotations-jakarta` 2.2.47 y la exclusión de `swagger-annotations` (no-Jakarta) en `backend/pom.xml` (Spring AI / `openai-java-core`).

### Proveedor LLM

En `.env`: `EDUSYNC_AI_PROVIDER` y `EDUSYNC_AI_AGENTE_PROVIDER` = `ollama` | `open-webui` | `gemini`. En el servicio `api` de Compose, `EDUSYNC_AI_ENABLED=false` por defecto. Claves solo por entorno. No enviar PII, RUDE ni notas al modelo ni a logs.

## Verificación

```bash
cd backend && mvn test
cd frontend && npm run build
```

Golden tests de dominio (perfil SIE): `CalculoNotasTest.redondeoHalfUpNoFloor`, `SIEPayloadTest`, `MultitenantTest` (`VentanaTest` diferido, `ADR-0025`).

## Estructura

```
backend/          # módulos plataforma, identidad, academico, notassie, shared (+ Dockerfile)
frontend/         # Angular 21 — core / shared / features
infra/            # docker-compose: postgres + api
openapi.json      # contrato OpenAPI versionado (drift en CI)
docs/
  product/        # BRD, PRD, FSD, DTP vivos
  baseline/       # congelado M4 — prohibido editar
  design/         # DD-UC-NNN
  adr/            # decisiones (0007 Strangler Fig gated)
  prompts/impl/   # PR-IMPL-NNN
  laboratorios/   # evidencias (p. ej. S08 imagen Docker)
AGENTS.md         # contrato multi-agente (fuente de verdad operativa)
```

## Documentación

| Documento | Ruta |
|-----------|------|
| Visión | `01_vision_negocio.md` |
| Arquitectura funcional | `docs/arquitectura_funcional_EduSync.md` |
| Spec viva | `docs/product/FSD.md` |
| Contrato técnico vivo | `docs/product/DTP.md` |
| Catálogo de prompts | `docs/PROMPT_MAPPING.md` |
| Lab S08 (imagen Docker) | `docs/laboratorios/imagen-o-podman.md` |
| Convenciones de agentes | `AGENTS.md` |

## Roles

`SYSADMIN` (sin tenant), `ADMIN`, `SECRETARIA`, `PROFESOR`, `ASESOR` — multi-rol (`ADR-0010`). Identidad estudiantil de escritura: solo **RUDE**.

## Curso

Trabajo académico UMSS — G-EduSync.
