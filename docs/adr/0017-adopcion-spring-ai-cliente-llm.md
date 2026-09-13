# Architecture Decision Record (ADR)

## ADR-0017: Adopción de Spring AI como cliente HTTP/LLM detrás del puerto hexagonal `LlmPort`

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0017` |
| Título | Adopción de Spring AI como cliente HTTP/LLM detrás del puerto hexagonal `LlmPort` |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Backend, paquete `com.edusync.shared.ai.infrastructure` únicamente. No entra a `domain/` ni a `application/`. No cambia `POST /api/v1/ai/chat`, `edusync.ai.*`, el modelo default `llama3.1:latest` ni el frontend. No toca el baseline congelado (`docs/baseline/`, tag `release/2.0.0`). No supersede a ningún ADR anterior. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0008` (stack vivo Java 25 / Spring Boot 4.1.0); `ADR-0011` (`shared` OPEN); `ADR-0012` (dependencias de productividad, precedente de BOM). Consume el spike LLM ya existente (`shared.ai`, skill `ollama-edusync`). Habilita `DD-UC-022` / `PR-IMPL-022`. |

### 1. Contexto

El spike de LLM de EduSync (`com.edusync.shared.ai`) ya está en runtime: `POST /api/v1/ai/chat` autenticado, puerto hexagonal `LlmPort.completar(String) → RespuestaLlm`, y dos adaptadores ad-hoc (`OllamaLlmAdapter` → `POST /api/generate` con `RestClient`; `OpenWebUiLlmAdapter` → `POST /api/chat/completions`). La configuración vive en `edusync.ai.*` (provider `ollama` por defecto, modelo `llama3.1:latest`, URLs y timeouts por env). El DTI congelado §9 declaró “sin IA en runtime”; el DTP §B aún lo espeja como “no cambió”. El código real ya contradice ese espejo: hay un cliente HTTP propio, no Spring AI (`org.springframework.ai` no aparece en `pom.xml`).

Fuerzas en tensión: (1) mantener el hexágono (`domain/`/`application/` sin frameworks de runtime) vs. dejar de mantener JSON, DTOs internos y timeouts a mano; (2) no romper el contrato público ni las propiedades ya usadas por `ollama-edusync`; (3) `AGENTS.md` §4 exige ADR + verificación contra Jakarta EE 11 / Spring Framework 7.0.8 antes de fijar cualquier dependencia nueva; (4) NFR-007 / `AGENTS.md` §7: nunca loguear ni enviar PII, RUDE ni notas al modelo.

Lo que se sabe: el caso de uso de aplicación (`ChatConLlmService`) y el controlador no necesitan cambiar si el puerto se conserva. Lo que no se sabe todavía: si existe un BOM de Spring AI compatible con Spring Boot 4.1.0 / Java 25. Esa verificación es **gate de ejecución** de `PR-IMPL-022`, no de esta decisión de dirección.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Statu quo: `RestClient` + JSON ad-hoc hacia Ollama / Open WebUI | Cero dependencias nuevas; ya funciona; HTTP/1.1 ya resuelto para uvicorn | Cada proveedor nuevo (timeouts, streaming, tool calling) se reimplementa a mano; el cliente HTTP no es el contrato de Spring | Bajo hoy, alto acumulado |
| B. Reemplazar `LlmPort` por tipos de Spring AI (`ChatClient`, `ChatModel`) en `application/`/`domain/` | Menos adaptadores; API “oficial” de Spring | Viola hexagonal (`domain/` no importa frameworks de runtime, `AGENTS.md` §5); acopla el caso de uso a un vendor; costoso de revertir | Alto (rompe el puerto y los tests de aplicación) |
| C. Adoptar Spring AI **solo** en `infrastructure` como implementación de `LlmPort`; conservar el puerto, el endpoint y `edusync.ai.*` | Un solo lugar de I/O HTTP; `ChatConLlmService` / tests de aplicación intactos; cumple `AGENTS.md` §4 (ADR + dep nueva); no cambia el contrato público | Dependencia nueva; hay que verificar BOM vs Boot 4.1.0 antes de fijar versión; hay que preservar HTTP/1.1 si Spring AI negocia HTTP/2 contra Open WebUI | Medio (refactor de 2 adaptadores + `pom.xml`) |
| D. Cambiar de proveedor (OpenAI cloud, otro starter) y abandonar Ollama local | Ecosistema Spring AI “feliz path” | Contradice el default documentado (`llama3.1:latest` local); `ollama-agent` prohíbe cambiar de proveedor sin ADR dedicado a *ese* cambio; secretos/costo cloud | Alto y fuera de alcance |

### 3. Decisión

> **Elegimos la Alternativa C.** Spring AI entra como cliente HTTP/LLM **detrás** de `LlmPort`. `domain/` y `application/` no importan `org.springframework.ai`. El contrato `POST /api/v1/ai/chat`, las propiedades `edusync.ai.*` y el default Ollama `llama3.1:latest` se conservan.

Criterio decisivo: el hexágono ya aísla al proveedor; el dolor real es el cliente HTTP ad-hoc, no el puerto. Meter Spring AI en `application/` (B) compraría poco y rompería la regla de `domain/` sin frameworks de runtime. El statu quo (A) pospone el mismo trabajo cada vez que se añada streaming, tools o un tercer proveedor.

La versión concreta del BOM **no se fija en este ADR**. `PR-IMPL-022` MUST verificar un BOM compatible con Spring Boot 4.1.0 / Jakarta EE 11 / Java 25. Si no existe, se detiene la ejecución y se escala — no se inventa un BOM de Boot 3.

**Pin de ejecución (13/09/2026):** `PR-IMPL-022` fijó `spring-ai-bom` **2.0.0** (documentación oficial: Spring AI 2.0.x soporta Boot 4.0.x y 4.1.x). Auto-config de modelos desactivada (`spring.ai.model.chat=none`); `AiConfig` construye `ChatClient` desde `edusync.ai.*`.

### 4. Consecuencias

#### 4.1 Positivas

- Un único stack de cliente (ChatClient / ChatModel) para Ollama y, vía API compatible OpenAI, Open WebUI.
- `ChatConLlmService`, `LlmPort` y `ChatConLlmServiceTest` permanecen; el riesgo de regresión funcional del chat queda acotado a los adaptadores.
- Cumple el guardrail de “dependencia nueva ⇒ ADR” (`AGENTS.md` §4) sin reabrir el modelo de dominio.

#### 4.2 Negativas / costos

- Dependencia nueva en `pom.xml`; hay que vigilar el BOM frente a Boot 4.1.0.
- Si Spring AI no respeta HTTP/1.1, Open WebUI (uvicorn) puede volver a responder 400 — hay que reaplicar el `HttpClient` HTTP/1.1 de `AiConfig` o equivalente.
- Superficie de autoconfiguración de Spring AI que no debemos activar a ciegas (no queremos que pise `edusync.ai.*` ni que loguee prompts).

#### 4.3 Neutras / observables

- No cambia RBAC (`isAuthenticated()` en `/api/v1/ai/**`), ni el frontend, ni Flyway.
- `shared` sigue `OPEN` (`ADR-0011`); `ModularityTests` debe permanecer 7/7.
- El DTP §B §9 pasa de “espejo DTI sin IA” a “cliente Spring AI detrás de `LlmPort`” (delta vs DTI vFinal, este ADR).

### 5. Impacto en el sistema

- **Código**: reescritura de `OllamaLlmAdapter` y `OpenWebUiLlmAdapter`; posible simplificación de `AiConfig` (los `RestClient` ad-hoc se eliminan si el starter los cubre). `pom.xml` gana BOM + starter(s) de Spring AI. `AiProperties` / `edusync.ai.*` se conservan (pueden mapearse internamente a `spring.ai.*`).
- **Operaciones**: mismas env (`OLLAMA_BASE_URL`, `OPEN_WEBUI_API_KEY`, etc.). Sin servicio cloud nuevo.
- **Seguridad**: sin cambio de superficie. MUST NOT loguear prompt, respuesta completa, API key, RUDE, notas ni PII (`AGENTS.md` §7, NFR-007).
- **Equipo**: skill `ollama-edusync` sigue válido (mismo endpoint y modelo); el adaptador interno cambia.
- **Costo**: cero en factura AWS (Ollama local). Licencia Apache-2.0 de Spring AI.

### 6. Plan de reversión

- Señal temprana de decisión incorrecta: (1) no hay BOM compatible con Boot 4.1.0 — no se implementa, se vuelve a A; (2) Spring AI no permite HTTP/1.1 y Open WebUI queda roto sin workaround; (3) la autoconfiguración filtra prompts a logs INFO+.
- Costo de revertir: bajo — los adaptadores se vuelven a `RestClient` + JSON; `LlmPort` no cambia; el endpoint no cambia.
- Plan B: Alternativa A (statu quo) hasta que exista un BOM verificable.

### 7. Validación

- `mvn test` en verde, incluyendo `ModularityTests` 7/7 y `ChatConLlmServiceTest`.
- Ningún import de `org.springframework.ai` bajo `shared.ai.domain` ni `shared.ai.application`.
- `POST /api/v1/ai/chat` conserva status codes (`200` / `400` / `401` / `502 E_LLM_NO_DISPONIBLE` / `503 E_AI_DESHABILITADO`).
- Grep de logs de adaptadores: sin prompt, sin respuesta completa, sin API key.

### 8. Referencias

- `backend/src/main/java/com/edusync/shared/ai/**` (spike actual).
- `AGENTS.md` §4 (tabla de stack; dependencia nueva ⇒ ADR), §5 (hexágono), §7 (PII).
- `docs/product/FSD.md` NFR-007; `docs/product/DTP.md` §B §9.
- Skill `.claude/skills/ollama-edusync/SKILL.md` (espejo `.cursor/`).
- `docs/design/DD-UC-022.md` (cómo); `docs/prompts/impl/PR-IMPL-022.md` (**ejecutado**, BOM 2.0.0).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 13/09/2026 | Rodrigo Aspeti | Propuesta y aceptación en el mismo turno (Alternativa C): Spring AI como cliente HTTP/LLM detrás de `LlmPort`, sin entrar a `domain/`/`application/`. Versión del BOM a fijar solo tras verificar compatibilidad con Spring Boot 4.1.0. Habilita `DD-UC-022` / `PR-IMPL-022`. |
| 2 | 13/09/2026 | Rodrigo Aspeti | Pin de ejecución: `spring-ai-bom` 2.0.0 (`PR-IMPL-022`). `mvn test` 250/250. |
