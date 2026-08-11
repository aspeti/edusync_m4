---
name: ollama-edusync
description: >
  Integra o extiende el consumo de LLM (Ollama o Open WebUI) en EduSync vía
  POST /api/v1/ai/chat y adaptadores hexagonales en shared.ai. Activar cuando el
  usuario pide "conecta Ollama", "Open WebUI", "endpoint de chat LLM",
  "diagnostica E_LLM_NO_DISPONIBLE", o pide al ollama-agent trabajar sobre el spike LLM.
disable-model-invocation: false
---

# Skill: ollama-edusync — LLM en runtime (EduSync)

## 1. Cuándo activarlo

- Conectar / reparar / extender `POST /api/v1/ai/chat` (Ollama o Open WebUI).
- Diagnosticar `E_LLM_NO_DISPONIBLE` / `E_AI_DESHABILITADO` / API key faltante.
- Añadir UI mínima de chat (solo si se pide explícitamente).

**NO activar** para agentes del AI-SDLC (dev/docs/qa); eso no es runtime LLM.

## 2. Fuentes de verdad

1. Código: `backend/src/main/java/com/edusync/shared/ai/**`
2. Config: `application.yml` → `edusync.ai.*` + env (`EDUSYNC_AI_PROVIDER`, `OPEN_WEBUI_API_KEY`, …)
3. Plantilla secretos: `.env.example` (commiteable); valores reales en `.env` (**gitignored**)
4. Agente: `.claude/agents/ollama-agent.md` / `.cursor/agents/ollama-agent.md`
5. Guardrails: `AGENTS.md` §7 (sin PII; sin secretos en git)
6. Baseline DTI §9 “sin IA en runtime” es histórico → deltas en capa viva; **nunca** `docs/baseline/**`

## 3. Contrato público (v0)

```http
POST /api/v1/ai/chat
Authorization: Bearer <JWT EduSync>
{ "prompt": "..." }

→ 200 { "respuesta": "...", "modelo": "..." }
→ 502 { "codigo": "E_LLM_NO_DISPONIBLE", ... }
→ 503 { "codigo": "E_AI_DESHABILITADO", ... }
```

### Proveedores (`edusync.ai.provider`)

| Valor | Adaptador | Upstream | Auth upstream |
|-------|-----------|----------|---------------|
| `ollama` (default) | `OllamaLlmAdapter` | `POST {base}/api/generate` | ninguna |
| `open-webui` | `OpenWebUiLlmAdapter` | `POST {base}/api/chat/completions` | `Bearer ${OPEN_WEBUI_API_KEY}` |

## 4. Secretos (obligatorio)

- **MUST NOT** hardcodear ni commitear API keys.
- Key solo vía env `OPEN_WEBUI_API_KEY` (YAML: `api-key: ${OPEN_WEBUI_API_KEY:}`).
- `.gitignore` ignora `.env`, `.env.*`, `application-local.yml`.
- Spring Boot **no** carga `.env` solo: exportar en shell/IDE o usar Run Configuration.
- Ejemplo PowerShell (desde raíz, sin commitear):

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
  $k,$v = $_.Split('=',2); Set-Item -Path "Env:$k" -Value $v.Trim()
}
cd backend; mvn spring-boot:run
```

## 5. Procedimiento

1. Elegir provider (`ollama` o `open-webui`).
2. Si Open WebUI: crear API key en la UI, ponerla solo en `.env` / env del proceso.
3. Verificar upstream (Ollama `:11434` o WebUI `:3000`).
4. Cambios en `shared.ai` respetando hexagonal.
5. Tests unitarios sin red; CI con `edusync.ai.enabled=false`.
6. `mvn test` verde. `@dtp-sync` solo si el humano pide documentar.

## 6. Invariantes

- Auth JWT EduSync obligatoria en `/api/v1/ai/chat`.
- Sin log de prompt/respuesta completa ni de API keys.
- Sin hardcode de URL/modelo/secretos.
- Nuevo proveedor distinto → ADR + aprobación humana.
- No tocar `docs/baseline/**`.
