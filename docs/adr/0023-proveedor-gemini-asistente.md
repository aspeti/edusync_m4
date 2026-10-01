# Architecture Decision Record (ADR)

## ADR-0023: Gemini como proveedor opcional del asistente

### Metadatos

| Campo | Valor |
|-------|-------|
| Numero | `0023` |
| Titulo | Agregar Gemini por el endpoint compatible con OpenAI, sin cambiar el default Ollama |
| Fecha | 30/09/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aceptada** |
| Alcance | `edusync.ai.provider=gemini` y `edusync.ai.agente.provider`. No toca `docs/baseline/**`. No supersede a `ADR-0017` ni a `ADR-0018`. |

### 1. Contexto

El asistente (`POST /api/v1/ai/agente`) y el chat (`POST /api/v1/ai/chat`) hablan con Ollama local (`llama3.1:8b` / `llama3.1:latest`) o con Open WebUI. `ADR-0017` dejo un proveedor cloud fuera de alcance. La corrida live del red team no termina los turnos ReAct dentro del timeout de 180 s con el modelo local.

El operador pidio Gemini para poder correr esos escenarios. La clave ya estaba prevista en `.env.example` como `GEMINI_API_KEY`, apuntando al endpoint OpenAI-compatible de Google AI Studio.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras |
|-------------|------|---------|
| A. Seguir solo con Ollama | Cero dependencia y cero datos fuera de la maquina | Los turnos ReAct locales no caben en el timeout |
| B. SDK nativo `spring-ai-google-genai` | Tool calling de primera clase | Artefacto fuera del BOM 2.0.0 ya fijado; auto-config pide la key aunque el proveedor sea Ollama |
| C. Mismo `OpenAiChatModel` de Spring AI 2.0 contra `https://generativelanguage.googleapis.com/v1beta/openai` | Sin dependencia nueva; la key solo existe si `provider=gemini`; el default no cambia | El contrato de tools es el compatible con OpenAI, no el SDK nativo de Google |

### 3. Decision

Alternativa C.

- `EDUSYNC_AI_PROVIDER=gemini` activa `GeminiLlmAdapter` para el chat. Es el default.
- `EDUSYNC_AI_AGENTE_PROVIDER` elige el modelo del asistente. Si se omite, sigue al provider del chat. El default tambien es Gemini. Ollama queda disponible con `EDUSYNC_AI_PROVIDER=ollama`.
- Si el provider del agente es Gemini y `EDUSYNC_AI_AGENTE_MODEL` sigue en un tag `llama*`, se usa `GEMINI_MODEL` (default `gemini-2.0-flash`).
- `GEMINI_API_KEY` vacia no impide arrancar con Ollama. Con Gemini, la llamada falla con `E_LLM_NO_DISPONIBLE` hasta que la variable exista en el entorno.
- No se envian notas ni RUDE reales al proveedor.

### 4. Consecuencias

#### 4.1 Positivas

El red team live puede usar un modelo que responde dentro del timeout, sin tocar el gate determinista.

#### 4.2 Negativas / costos

El texto de la pregunta y el resultado de las tools salen de la maquina hacia Google cuando el provider es Gemini. Hay costo de API.

#### 4.3 Neutras / observables

`schoolId` y `tenantId` siguen sin verse en la respuesta HTTP. Esos dos ataques se miden en `RedTeamAssistantCatalogWebMvcTest`.

### 5. Impacto en el sistema

`AiConfig`, `AgenteAiConfig`, `AiProperties`, `application.yml`. `domain/` y `application/` no importan Spring AI. Sin cambio de contrato REST.

### 6. Plan de reversion

Quitar `EDUSYNC_AI_PROVIDER` / `EDUSYNC_AI_AGENTE_PROVIDER` (o ponerlos en `ollama`). El bean de Gemini no se crea.

### 7. Validacion

`ProveedorGeminiTest`: la URL no gana un `/api` de mas, y un tag `llama*` no se envia a Gemini. `mvn test` del slice. Arranque con provider default sin `GEMINI_API_KEY`.

### 8. Referencias

- `ADR-0017`, `ADR-0018`
- `.env.example` (bloque Gemini)
- Google AI Studio, endpoint OpenAI-compatible `/v1beta/openai/chat/completions`

### 9. Historial

| Fecha | Cambio |
|-------|--------|
| 30/09/2026 | Creacion. Aceptada por pedido explicito de agregar el proveedor. |
| 30/09/2026 | El default del perfil dev pasa a Gemini. Ollama sigue disponible. La key no sale de `.env`. |
