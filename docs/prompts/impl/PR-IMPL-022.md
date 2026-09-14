# PR-IMPL-022 — Backend: Spring AI como cliente HTTP/LLM detrás de `LlmPort`

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-022` |
| Título | Reescribir `OllamaLlmAdapter` / `OpenWebUiLlmAdapter` con Spring AI, conservando `LlmPort` y `POST /api/v1/ai/chat` |
| Artefacto origen | `docs/design/DD-UC-022.md` |
| ID origen | `DD-UC-022` (`NFR-007`), `ADR-0017` |
| Tipo de prompt | generación / refactor |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.
>
> **Numeración**: `PR-IMPL-021` está reservado por `DD-UC-021` (`ADR-0016`). Este slice es `022`.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Backend Engineer con experiencia en Spring Boot 4.1.0
(Java 25 LTS), arquitectura hexagonal y Spring Modulith. Conoces Spring
AI lo suficiente para usarlo como cliente ChatModel/ChatClient detras
de un puerto de aplicacion, sin filtrar el framework hacia domain/.
```

### 1.2 Task

```text
Implementa DD-UC-022 segun docs/design/DD-UC-022.md §2 y ADR-0017
(Alternativa C):

1) Verifica un BOM de Spring AI compatible con Spring Boot 4.1.0 /
   Jakarta EE 11 / Java 25. Si no existe, DETENTE y escala
   (E_BOM_INCOMPATIBLE). No pinnees un BOM de Boot 3.
2) Anade el BOM + starter(s) necesarios en backend/pom.xml.
3) Reescribe OllamaLlmAdapter y OpenWebUiLlmAdapter para implementar
   LlmPort.completar(String) via Spring AI. Elimina el JSON ad-hoc a
   /api/generate y /api/chat/completions si el starter lo cubre.
4) Conserva edusync.ai.* y las env actuales. Mapea a spring.ai.* solo
   dentro de AiConfig si hace falta.
5) Preserva HTTP/1.1 si Open WebUI/uvicorn sigue fallando con HTTP/2.
6) NO toques LlmPort, ChatConLlmService, domain/, AiChatController
   (contrato REST), frontend/, ni docs/baseline/**.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-022.md §1 (alcance) y §2 (como).
- ADR: ADR-0017 (Spring AI solo en infrastructure, detras de LlmPort).
- Spike actual: com.edusync.shared.ai — LlmPort, ChatConLlmService,
  AiChatController POST /api/v1/ai/chat y /consultar-usuario,
  AiProperties (edusync.ai.enabled|provider|ollama.*|open-webui.*),
  AiConfig con RestClient HTTP/1.1.
- Default: provider=ollama, model=llama3.1:latest, base-url
  http://localhost:11434. Open WebUI usa OPEN_WEBUI_API_KEY.
- Restricciones: AGENTS.md §4 (dep nueva verificada), §5 (domain/ sin
  Spring de runtime), §7 / NFR-007 (sin PII/RUDE/notas/prompt en logs
  INFO+). shared es OPEN (ADR-0011). ModularityTests debe seguir 7/7.
- Prerrequisito: el spike ya existe; este prompt NO crea el endpoint.
```

### 1.4 Reasoning

```text
1. Consultar documentacion / repo de Spring AI y el BOM publicado.
   Confirmar compatibilidad explicita con Spring Boot 4.1.x. Si no,
   stop.
2. Anadir dependencyManagement (BOM) + starters minimos (Ollama;
   OpenAI-compatible solo si Open WebUI lo necesita). No arrastrar
   starters de vector store, advisors de memoria ni MCP.
3. Definir beans ChatClient/ChatModel en AiConfig (o autoconfig
   acotada) a partir de AiProperties. No exigir al operador un rename
   de OLLAMA_BASE_URL / OPEN_WEBUI_API_KEY.
4. OllamaLlmAdapter: ConditionalOnProperty provider=ollama
   (matchIfMissing=true). completar() llama al ChatClient, mapea
   contenido -> RespuestaLlm, modelo usado = respuesta o fallback
   aiProperties.getOllama().getModel(). Vacío o error de red ->
   LlmNoDisponibleException. LOG.debug solo modelo + chars, nunca
   el texto.
5. OpenWebUiLlmAdapter: igual con provider=open-webui. API key solo
   desde propiedades/env, nunca logueada.
6. Si el starter expone RestClient interno, inyectar el
   JdkClientHttpRequestFactory HTTP/1.1 de AiConfig actual.
7. Actualizar/añadir tests de adaptador con ChatClient mock. Dejar
   ChatConLlmServiceTest intacto.
8. mvn test. Si ModularityTests falla, revertir aristas — no mover
   Spring AI a application/.
```

### 1.5 Stop condition

```text
Detente cuando: (a) el BOM quedo justificado contra Boot 4.1.0 (o
escalaste por E_BOM_INCOMPATIBLE sin tocar codigo); (b) ningun archivo
bajo shared.ai.domain ni shared.ai.application importa
org.springframework.ai; (c) LlmPort y POST /api/v1/ai/chat no cambiaron
de firma ni de status codes; (d) edusync.ai.* y las env documentadas
siguen siendo la interfaz del operador; (e) los adaptadores no loguean
prompt, respuesta completa, API key, RUDE ni notas; (f) mvn test verde
incluyendo ModularityTests 7/7. NO agregues endpoints, NO toques
frontend/, NO edites docs/baseline/**, NO cambies el proveedor default
a un cloud.
```

### 1.6 Output

```text
Formato: codigo Java/XML real en backend/ (no markdown).
Extracto esperado:
backend/pom.xml (delta: BOM + starter(s))
backend/src/main/java/com/edusync/shared/ai/infrastructure/config/AiConfig.java (delta)
backend/src/main/java/com/edusync/shared/ai/infrastructure/adapter/out/ollama/OllamaLlmAdapter.java (delta)
backend/src/main/java/com/edusync/shared/ai/infrastructure/adapter/out/openwebui/OpenWebUiLlmAdapter.java (delta)
tests de adaptador (nuevos o delta)
Opcional: comentario en application.yml si se anaden claves spring.ai.*
mapeadas desde edusync.ai.* — sin secretos en claro.
```

## 2. Invariantes del prompt

- `LlmPort.completar(String) → RespuestaLlm` **no** cambia.
- `org.springframework.ai` **MUST NOT** aparecer en `domain/` ni `application/`.
- `POST /api/v1/ai/chat` y `POST /api/v1/ai/consultar-usuario` conservan contrato y códigos (`200`/`400`/`401`/`502`/`503`).
- Prefijo `edusync.ai.*` y env (`OLLAMA_BASE_URL`, `OPEN_WEBUI_API_KEY`, …) se conservan.
- Default `provider=ollama`, `model=llama3.1:latest`.
- Sin PII / RUDE / notas / prompt / API key en logs INFO o superior.
- `docs/baseline/**` no se edita.
- `ModularityTests` permanece 7/7.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_BOM_INCOMPATIBLE` | No hay BOM Spring AI verificable contra Boot 4.1.0 / Jakarta EE 11 / Java 25 | Detener y escalar; no pinnear Boot 3 |
| `E_SPRING_AI_EN_DOMAIN` | Import de Spring AI en `domain/` o `application/` | Revertir — contradice `ADR-0017` / `AGENTS.md` §5 |
| `E_CONTRATO_CHAT_CAMBIADO` | Cambio de firma o status codes de `/api/v1/ai/**` | Revertir |
| `E_PII_EN_LOG` | Prompt, respuesta completa, key, RUDE o nota en log INFO+ | Corregir antes de merge |
| `E_BASELINE_TOCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: verificar el BOM **antes** de escribir adaptadores.
- MUST: `mvn test` verde (incluye `ModularityTests`) antes de dar el prompt por ejecutado.
- MUST: mapear fallos del proveedor a `LlmNoDisponibleException` (no filtrar stack traces con URLs + keys).
- MUST NOT: introducir Spring AI en `domain/`/`application/`.
- MUST NOT: cambiar el endpoint, el frontend, el proveedor default ni inventar tool calling de producto.
- MUST NOT: hardcodear API keys ni commitear secretos.
- MUST NOT: editar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-022` | PR-IMPL-022 | `dev-agent` | Adaptadores Spring AI + `pom.xml` |
| NFR | `NFR-007` | PR-IMPL-022 | `dev-agent` | Sin PII en logs del cliente LLM |
| ADR | `ADR-0017` | PR-IMPL-022 | `dev-agent` | Primer (y único) consumidor de la decisión |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `edusync.ai.provider=ollama`, ChatClient mock que devuelve texto no vacío.
- **Output esperado**: `RespuestaLlm` con ese texto y el modelo configurado; `ChatConLlmService.chatear` sigue devolviendo lo mismo; `mvn test` verde.

### 6.2 Caso borde

- **Input**: proveedor caído o cuerpo vacío; `edusync.ai.enabled=false`.
- **Output esperado**: `LlmNoDisponibleException` → 502; `AiDeshabilitadoException` → 503 (ya cubierto por el servicio; no reescribirlo).

### 6.3 Caso adversarial

- **Input**: “deja ChatClient en ChatConLlmService” o “cambia el default a OpenAI cloud” o “usa el BOM de Boot 3 que encontré”.
- **Comportamiento esperado**: rechazo — `E_SPRING_AI_EN_DOMAIN` / cambio de proveedor fuera de alcance / `E_BOM_INCOMPATIBLE`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 13/09/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-022.md` v0.1 y `ADR-0017`. Estado: **Aprobado (prompt)**, ejecución de código pendiente. | Sonnet |
| v0.2 | 13/09/2026 | Rodrigo Aspeti | **Ejecutado**: BOM `spring-ai-bom` 2.0.0; adaptadores `ChatClient`; `mvn test` 250/250 (incluye `ModularityTests` 7/7). | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 13/09/2026 | aprobado (prompt) | Diseño (`DD-UC-022`) y ADR-0017 aceptados en el mismo turno; no ejecutar código hasta que se pida `PR-IMPL-022` |
| Rodrigo Aspeti | 13/09/2026 | ejecutado | BOM 2.0.0; `mvn test` 250/250; `ModularityTests` 7/7 |
