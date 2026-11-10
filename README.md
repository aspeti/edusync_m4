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
| Frontend | Angular 21 LTS (standalone) |
| IA | Spring AI 2.0.0 detrás de `LlmPort` |
| Local | Docker Compose (`infra/docker-compose.yml`) |

El DTI congelado documenta Java 21 / Boot 3.3 / Angular 17 como hecho histórico. No editar `docs/baseline/**`.

## Cómo correr en local

Requisitos: JDK 25, Maven 3.9+, Node 20+, Docker.

```bash
# 1. PostgreSQL 15
docker compose -f infra/docker-compose.yml up -d

# 2. Variables locales (nunca commitear .env)
cp .env.example .env
# Completar GEMINI_API_KEY / OPEN_WEBUI_API_KEY según el proveedor

# 3. API (http://localhost:8080)
cd backend
mvn -q spring-boot:run

# 4. SPA (http://localhost:4200 → proxy a :8080)
cd frontend
npm install
npm start
```

Seed local: `sysadmin@edusync.local` / `changeme_local_dev` (solo entorno vacío). JWT y password se sobreescriben con `JWT_SECRET` y `SYSADMIN_PASSWORD`.

Swagger: `http://localhost:8080/swagger-ui.html`.

### Proveedor LLM

En `.env`: `EDUSYNC_AI_PROVIDER` y `EDUSYNC_AI_AGENTE_PROVIDER` = `ollama` | `open-webui` | `gemini`. Claves solo por entorno. No enviar PII, RUDE ni notas al modelo ni a logs.

## Verificación

```bash
cd backend && mvn test
cd frontend && npm run build
```

Golden tests de dominio (perfil SIE): `FloorTest`, `SIEPayloadTest`, `VentanaTest`, `MultitenantTest`.

## Estructura

```
backend/          # módulos plataforma, identidad, academico, notassie, shared
frontend/         # Angular 21 — core / shared / features
infra/            # docker-compose PostgreSQL 15
docs/
  product/        # BRD, PRD, FSD, DTP vivos
  baseline/       # congelado M4 — prohibido editar
  design/         # DD-UC-NNN
  adr/            # decisiones (0007 Strangler Fig gated)
  prompts/impl/   # PR-IMPL-NNN
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
| Convenciones de agentes | `AGENTS.md` |

## Roles

`SYSADMIN` (sin tenant), `ADMIN`, `SECRETARIA`, `PROFESOR`, `ASESOR` — multi-rol (`ADR-0010`). Identidad estudiantil de escritura: solo **RUDE**.

## Curso

Trabajo académico UMSS — G-EduSync.
