# E2E EduSync — Playwright + agente Planner/Generator

flujo sobre la SPA Angular de EduSync.
LLM del agente: el mismo `.env` de la raíz del repo (`EDUSYNC_AI_PROVIDER`).

## Prerrequisitos (stack local)

En tres terminales (o ya levantados):

```powershell
# 1) Postgres
cd infra; docker compose up -d postgres

# 2) API
cd backend; mvn -q spring-boot:run

# 3) SPA con proxy /api → :8080
cd frontend; npm start
```

Seed SYSADMIN (defaults `application.yml`):

- email: `sysadmin@edusync.local`
- password: `changeme_local_dev`

ADMIN de tenant demo (en `.env.e2e`):

- `E2E_ADMIN_EMAIL` / `E2E_ADMIN_PASSWORD` (ej. `demo@mail.com`)

## Instalación (una vez)

```powershell
cd tests\e2e
copy .env.e2e.example .env.e2e
npm install
npx playwright install chromium
pip install openai python-dotenv   # para el agente
```

> Si ves `Executable doesn't exist ... ms-playwright\chromium_headless_shell-...`,
> el npm package está instalado pero faltan los browsers en tu perfil Windows.
> Corre `npx playwright install chromium` **desde tu propia PowerShell**
> (no solo desde el agente de Cursor, que a veces los deja en una caché distinta).

## Correr la suite tradicional

```powershell
cd tests\e2e
npx playwright test tests/seed.spec.ts tests/tradicional
# o: npm test -- tests/tradicional
```

## Agente (Planner → Generator → auditoría)

Usa el `.env` de la **raíz** del repo (Ollama u Open WebUI), igual que `tools/ai-test-generator-edusync`.

```powershell
cd tests\e2e

# 1) Plan (o edita specs/plan_edusync_login.md a mano)
python agente_e2e.py plan
# → specs/plan_agente.md  — AUDITAR antes de generar

# 2) Un caso por vez
python agente_e2e.py generar 1.1
npx playwright test tests/agente/caso_1_1.spec.ts

# 3) Auditoría: AUDITORIA_E2E.md (5 preguntas) + REGISTRO_TOKENS.csv
```

### Prompts nativos Playwright (opcional)

```powershell
npx playwright init-agents --loop=vscode
# o el loop de tu IDE — genera .github/agents/*
```

## Estructura

```
tests/e2e/
├── README.md
├── AUDITORIA_E2E.md
├── REGISTRO_TOKENS.csv
├── agente_e2e.py · config.py
├── playwright.config.ts
├── .env.e2e.example
├── specs/plan_edusync_login.md   ← plan auditado de referencia
└── tests/
    ├── seed.spec.ts
    ├── helpers/auth.ts
    ├── tradicional/login.spec.ts
    └── agente/                   ← salida del Generator
```

## Anclas añadidas en la UI

| Ancla | Dónde |
|-------|--------|
| `role="alert"` + `data-testid="login-error"` | Login |
| `data-testid="tenants-heading"` | Lista Tenants |

## Entregable actividad (cobertura de flujos)

Artefactos listos para el PDF/Word:

| Artefacto | Ruta |
|-----------|------|
| Inventario flujo → test | `specs/INVENTARIO_FLUJOS.md` |
| Planes por sección | `specs/plan_*.md` |
| Tabla auditoría + anclas + fuera E2E | `AUDITORIA_TABLA.md` |
| Tokens | `REGISTRO_TOKENS.csv` |
| Tests | `tests/tradicional/`, `tests/agente/` |

Capturas obligatorias (hacer a mano y anotar en la imagen):

```powershell
cd tests\e2e
npx playwright test
# Capturar el resumen final de la terminal (N passed)

npx playwright show-report
# Capturar la lista de tests del HTML
```
