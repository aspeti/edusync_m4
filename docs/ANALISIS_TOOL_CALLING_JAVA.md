# Análisis — Tool calling con agente en Java para EduSync

> **Estado 26/09/2026:** este análisis (v2, 13/09/2026) quedó **parcialmente obsoleto**. El backend de tool calling vive en `shared.ai` (`DD-UC-023` / `PR-IMPL-023` / `ADR-0018`) y la consola Angular en `DD-UC-024` / `PR-IMPL-024`. El descubridor OpenAPI se **curó** con allowlist académica (consultas del sistema, no solo usuarios). Conservar este archivo como contexto histórico de la decisión; la fuente de verdad de diseño es `docs/design/DD-UC-023.md` + `docs/design/DD-UC-024.md`.

**Revisión de los laboratorios de Python (Módulo 7), del proyecto `edusync-agente-llm`, y del backend Java tras la adopción de Spring AI (`ADR-0017`) — cambios necesarios antes de implementar tool calling**

| Campo | Valor |
|-------|-------|
| Fecha de esta revisión | 13/09/2026 (v2 — repite el análisis del 13/09/2026 v1 con los cambios que aterrizaron en el repo entre ambas revisiones) |
| Autor | Claude (Cowork), a pedido de Rodrigo Aspeti |
| Alcance revisado | Lo mismo que v1 (`EduSync_LLM/Lab3/5/6`, `edusync-agente-llm`) **más** los cambios nuevos: `docs/adr/0017-adopcion-spring-ai-cliente-llm.md`, `docs/design/DD-UC-022.md`, `docs/prompts/impl/PR-IMPL-022.md`, `backend/pom.xml`, `AGENTS.md` (fila de stack), `.env.example` |
| Naturaleza de este documento | Análisis externo para decidir los cambios previos a implementar. No sustituye un ADR formal — el §6 recomienda redactar uno (ahora `ADR-0018`) antes de tocar código. |
| Limitación de esta revisión | El puente a tu computadora sigue sin poder copiar los `.java` de `shared/ai` (rutas de 10-12 carpetas bajo la raíz conectada; el máximo soportado es 7). Lo nuevo de esta versión se reconstruyó con precisión porque **sí pude leer los documentos que describen el cambio letra por letra**: `ADR-0017`, `DD-UC-022`, `PR-IMPL-022` (todos con estado "Ejecutado"/"Aceptada") y el `pom.xml` real. Solo el contenido exacto de los `.java` sigue sin confirmar directamente. |

---

## 0. Qué cambió desde la v1 de este análisis (mismo día)

Entre la primera versión de este análisis y ahora, se ejecutó **`ADR-0017` + `DD-UC-022` + `PR-IMPL-022`**: el backend reemplazó su cliente HTTP artesanal hacia Ollama/Open WebUI por **Spring AI** (`spring-ai-bom` 2.0.0, starters `spring-ai-starter-model-ollama` y `spring-ai-starter-model-openai`), con `mvn test` en 250/250 y `ModularityTests` 7/7. Esto es exactamente el tipo de decisión que la v1 de este análisis marcaba como paso previo obligatorio (framework de cliente LLM) — **ya está resuelto**, y lo resolvieron bien: Spring AI vive *solo* en `infrastructure`, `LlmPort.completar(String) → RespuestaLlm` no cambió, el contrato `POST /api/v1/ai/chat` no cambió, y el propio `ADR-0017`/`DD-UC-022` dejan explícitamente **fuera de alcance "tool calling de producto"** — literalmente la frase que usan. Es decir: el equipo ya identificó que tool calling es el siguiente paso lógico y decidió no mezclarlo con este refactor.

Esto cambia dos cosas importantes respecto a la v1 de este documento:

1. **Ya no hay que decidir "qué cliente HTTP usar para hablar con Ollama/Open WebUI"** — ya es Spring AI. Y Spring AI trae soporte de *tool calling* nativo (`ToolCallback`/`@Tool`, manejo automático del bucle `tool_calls` si se quiere) que es sustancialmente más simple que escribir a mano el JSON de `/api/chat` de Ollama, que es lo que recomendaba la v1 de este análisis (esa sección queda obsoleta y se reemplaza en el §6.2 de abajo).
2. **La numeración de gobernanza avanza**: el próximo ADR es `ADR-0018` (no `0017`, ya usado), y el próximo slice de diseño/implementación es `DD-UC-023`/`PR-IMPL-023` (`PR-IMPL-021` está reservado por `DD-UC-021`, `022` ya se usó).

Todo lo demás de la v1 (qué enseñan los labs de Python, qué hace `edusync-agente-llm`, la brecha de fondo) sigue vigente sin cambios — se repite abajo para que este documento sea autocontenido.

---

## 1. Resumen ejecutivo

Hoy existen **tres cosas distintas** bajo el nombre "IA en EduSync" (una más que en la v1, por el cambio de infraestructura):

1. **`backend/shared.ai` (Java, dentro del monolito)**: un endpoint de chat de un solo turno (`POST /api/v1/ai/chat`), ahora servido por **Spring AI** por debajo (`ADR-0017`), más un extractor ad-hoc de una sola intención ("¿existe un usuario llamado X?" → `ExtraerConsultaUsuarioService` + `LlmStructuredExtractor`). **Sigue sin haber tool calling**: el modelo nunca decide qué endpoint llamar, ni encadena pasos — el cambio de Spring AI fue deliberadamente solo de infraestructura, no de capacidad.
2. **`edusync-agente-llm` (Python, proceso externo)**: un agente real de **tool calling multipaso (ReAct)** que descubre 23 herramientas de solo lectura leyendo el propio OpenAPI del backend Java, deja que Ollama decida cuál(es) llamar y encadenar, y arma la respuesta final siempre por código.
3. **Spring AI ya en el classpath del backend** (nuevo desde `PR-IMPL-022`): no se está usando todavía para tool calling, pero es la pieza que lo hace mucho más barato de construir que si siguiera existiendo solo el cliente HTTP artesanal.

Lo que pides — "tool calling con agente en Java" — significa **traer el mecanismo (2) dentro del backend Java, apoyándose en (3)**. Los laboratorios de Python (`Lab3` → `Lab5` → `Lab6`) siguen siendo la progresión pedagógica que enseña el mecanismo; `edusync-agente-llm` es la aplicación concreta contra el dominio real de EduSync. La diferencia frente a la v1 de este análisis es **cómo** conviene construirlo en Java: ya no hace falta reimplementar a mano el protocolo de `tool_calls` de Ollama — Spring AI lo abstrae.

La sección 6 responde directamente tu pregunta: **qué cambios hacer antes de ejecutar**, actualizada para partir de Spring AI en vez de partir de cero.

---

## 2. Qué enseñan los laboratorios de Python (la mecánica de tool calling) — sin cambios desde v1

| Lab | Día | Qué agrega | Archivo clave |
|-----|-----|------------|----------------|
| `Lab3` | 4 | Tool calling básico: declarar herramientas con schema tipado, bucle modelo→herramienta→modelo, **guardrail de confirmación humana antes de una escritura** (`cancelar_pedido`), y **log de auditoría en JSONL** de cada llamada a herramienta | `fase1_declarar_tools.py` … `fase4_endpoint.py`, `auditoria_tooling.jsonl` |
| `Lab5` | 6 | ReAct multipaso (el modelo encadena varias herramientas antes de responder) + variante donde las tools se descubren/ejecutan vía **servidor MCP** en vez de en el mismo proceso | `agente.py` (el bucle ReAct genérico, reutilizable con tools locales o MCP), `fase5_agente_mcp.py` |
| `Lab6` | 7 | **No es tool calling nuevo** — es orquestación de producción con **LangGraph**: el orden de pasos pasa del prompt al código (grafo con aristas condicionales), estado persistido en SQLite, guardrails de entrada/salida, caché semántica, fallback de modelo y trazabilidad | `grafo.py`, `nodos.py`, `estado.py` |

El núcleo reutilizable es `Lab5/agente.py` (`ejecutar_agente`): un bucle de máximo N pasos que (a) si el modelo pide herramientas, las ejecuta y le devuelve el resultado como mensaje `role="tool"`; (b) si el modelo responde texto, corta y esa es la respuesta final; (c) si se agota el tope de pasos, corta con un aviso. El bucle **no sabe qué herramientas existen**: recibe los *schemas* y un callback `ejecutar`. Este es el contrato conceptual a reproducir en Java — con la diferencia de que, con Spring AI en el classpath, gran parte de este bucle ya lo puede correr el propio `ChatClient` si se le registran los `ToolCallback` (ver §6.3).

`Lab6` sigue sin ser prerrequisito de tool calling; es la referencia para más adelante si EduSync necesita flujos que sobrevivan a un reinicio o se pausen esperando confirmación humana.

---

## 3. Qué implementa hoy `edusync-agente-llm` (el agente real contra EduSync) — sin cambios desde v1

Cliente **externo** al backend Java (no comparte código ni base de datos, solo HTTP + JWT); no se detectaron cambios en esta carpeta entre v1 y v2 de este análisis:

- **Descubrimiento de herramientas**: lee `GET /v3/api-docs` y genera una herramienta por cada endpoint que pase un filtro de seguridad: bajo `/api/v1/**`, **excluyendo** `/api/v1/auth/**`, `/api/v1/plataforma/**` y `/api/v1/ai/**`, y solo si es `GET` o un `POST` de consulta explícita. Resultado: **23 herramientas, todas de lectura**.
- **Tres caminos de decisión**: `KEYWORD` (catálogo de frases fijas, 0 turnos), `LLM` (ReAct multipaso, hasta `AGENTE_MAX_TURNOS`) y `NINGUNO` (si `IA_HABILITADA=false` o EduSync no responde).
- **Resolutor compuesto en código puro** (sin LLM) para "estudiantes de un curso+paralelo", el caso donde más alucina un modelo.
- **El LLM nunca redacta el texto final**: siempre lo arma `formateador_respuestas.py` por plantilla.
- **MCP opcional** (`MCP_HABILITADO=true`): las mismas 23 tools se pueden servir vía servidor MCP stdio.
- **Seguridad**: JWT nunca se loguea; reautenticación automática; argumentos inventados por el modelo se descartan antes de llamar a EduSync.
- **Modelo fijado explícitamente** (`llama3.1:8b`, nunca `latest`) — por reproducibilidad.

---

## 4. Qué existe hoy en el backend Java (`shared.ai`) — actualizado con `ADR-0017`/`DD-UC-022`

Fuentes de esta sección: `AGENTS.md` §4 y §8.1, `docs/adr/0017-*.md`, `docs/design/DD-UC-022.md`, `docs/prompts/impl/PR-IMPL-022.md` (los tres con estado **Aceptada/Ejecutado**, no propuestas) y `backend/pom.xml` real.

- **Contrato público (v0), sin cambios**: `POST /api/v1/ai/chat` con JWT EduSync, body `{"prompt": "..."}` → `{"respuesta": "...", "modelo": "..."}` (un solo turno, sin `tools`). También existe `POST /api/v1/ai/consultar-usuario` (el extractor de intención única), tampoco tocado.
- **Cliente LLM ahora es Spring AI, no HTTP artesanal**: `backend/pom.xml` agrega `spring-ai-bom` **2.0.0** + `spring-ai-starter-model-ollama` + `spring-ai-starter-model-openai` (este último para hablar el protocolo compatible-OpenAI de Open WebUI). La autoconfiguración de modelo de Spring AI está **desactivada** (`spring.ai.model.chat=none`): `AiConfig` construye el/los `ChatClient` **a mano** a partir de `edusync.ai.*`, precisamente para no obligar a configurar `spring.ai.openai.api-key` cuando el proveedor activo es Ollama. Esto es un detalle de diseño importante para lo que sigue: **cualquier bean nuevo de tool calling tiene que registrarse contra ese mismo `ChatClient` construido a mano**, no asumir que Spring Boot autoconfigura uno.
- **Spring AI vive *solo* en `infrastructure`**: `OllamaLlmAdapter` y `OpenWebUiLlmAdapter` implementan `LlmPort.completar(String) → RespuestaLlm` llamando al `ChatClient`/`ChatModel` de Spring AI por debajo. `domain/` y `application/` (`LlmPort`, `ChatConLlmService`) **no cambiaron ni una línea** — es una regla explícita verificada en `PR-IMPL-022` (`E_SPRING_AI_EN_DOMAIN` como *failure mode* si se viola).
- **`LlmPort` sigue siendo de un solo turno**: `completar(String) → RespuestaLlm`. Esto es lo que hay que extender (no reemplazar) para tool calling — ver §6.3.
- **Modelo por defecto: sigue siendo `llama3.1:latest`** — `ADR-0017` lo preserva **a propósito** ("no cambia el modelo default"). Sigue siendo la misma inconsistencia que marcaba la v1 frente a la práctica de `edusync-agente-llm` (nunca `latest`), y sigue sin resolverse.
- **Se preservó el detalle de HTTP/1.1** contra Open WebUI/uvicorn (si Spring AI negociaba HTTP/2 por defecto rompía Open WebUI) — `AiConfig` lo fuerza vía el `HttpClient`/`ClientHttpRequestFactory` subyacente. Cualquier adaptador nuevo que hable con Open WebUI tiene que respetar el mismo detalle.
- **`ExtraerConsultaUsuarioService`/`LlmStructuredExtractor`/`ConsultarUsuarioService`/`BuscarUsuarioPorNombrePort`**: sin cambios; siguen siendo el único precedente de "puerto de salida por el que `shared.ai` llega a datos de otro módulo (`identidad`) sin violar Spring Modulith" — no escala a 23 herramientas repitiendo un puerto por endpoint.
- **Guardrails vigentes, reafirmados por `ADR-0017`/`PR-IMPL-022`**: MUST NOT loguear/enviar PII, `rude`, calificaciones, passwords, JWT ni API keys al modelo (NFR-007, verificado explícitamente como parte del *Definition of Done* de este refactor); MUST NOT introducir un tercer proveedor LLM sin ADR; `docs/baseline/**` intocable; `ModularityTests` en 7/7.
- **Explícitamente fuera de alcance de lo ya ejecutado** (cita literal de `DD-UC-022` §1): *"Nuevos endpoints, UI de chat, **tool calling de producto**, cambio de proveedor default"* y *"streaming SSE, memoria de conversación persistida"*. También el `pom.xml` deja constancia de que **no** se agregaron starters de vector store, *advisors* de memoria ni MCP de Spring AI — se mantuvo el cambio mínimo a propósito.
- **`notassie` (calificaciones) sigue en 0 % de implementación** — sin cambio frente a v1, sigue sin haber riesgo inmediato de exponer notas como tool, pero el guardrail va a importar en cuanto ese módulo exista.
- **Hallazgo de higiene documental nuevo**: `AGENTS.md` §4 (tabla de stack) ya se actualizó con la fila de Spring AI, pero `.claude/agents/ollama-agent.md` y `.claude/skills/ollama-edusync/SKILL.md` (y sus espejos `.cursor/`) **no se tocaron** — siguen describiendo el `OllamaLlmAdapter` hablando `/api/generate` a mano, que ya no es cierto. Es el mismo patrón de brecha que ya se había señalado en `docs/PLAN_CONTINUACION_IMPLEMENTACION.md` (H4, `AGENTS.md` con sync pendiente) — conviene cerrarlo antes o junto con el ADR de tool calling, no después.

---

## 5. La brecha, resumida (actualizada)

| Capacidad del agente Python | ¿Existe hoy en Java? |
|---|---|
| Cliente LLM sobre un framework mantenido (no JSON a mano) | **Sí, desde `ADR-0017`** — Spring AI, sin capacidad de tools todavía |
| Conversación multi-mensaje con `tools` | No — `LlmPort.completar(String)` sigue siendo de un turno |
| Descubrimiento automático de herramientas desde OpenAPI | No |
| Filtro de seguridad de endpoints (excluir auth/plataforma/ai, solo lectura) | No existe porque no hay descubrimiento |
| Bucle ReAct con tope de turnos | No — pero Spring AI puede correrlo internamente una vez haya `ToolCallback`s |
| Camino `KEYWORD` (atajo sin gastar turnos de modelo) | No aplica hoy |
| Ejecución validada de la tool (descartar argumentos inventados) | No |
| Auditoría de qué herramienta se usó y de dónde salió el dato | No |
| Modelo fijado por versión explícita | No — sigue `latest` en Java, preservado a propósito por `ADR-0017` |
| MCP opcional | No — y `PR-IMPL-022` evitó a propósito arrastrar los starters MCP de Spring AI |

La única fila que cambió de estado desde v1 es la primera. Todo lo demás sigue exactamente igual de pendiente — pero ahora es más barato de cerrar.

---

## 6. Cambios a realizar antes de ejecutar (implementar) — actualizado post-`ADR-0017`

Ordenados por dependencia.

### 6.1 Gobernanza — decidir por `ADR-0018` antes de escribir código

El siguiente número de ADR libre es **`0018`** (`0017` ya lo tomó Spring AI). Igual que antes, esto encaja en el criterio propio del repo de "ADR si cambia proveedor/streaming/RAG **de forma significativa**" — y además `DD-UC-022` ya dejó constancia por escrito de que tool calling de producto quedó fuera de ese slice a propósito, es decir: el propio proyecto ya está pidiendo este ADR como el siguiente paso. Puntos a decidir:

1. **Cómo declarar/registrar las herramientas ante Spring AI**: Spring AI ofrece dos caminos —
   - **`@Tool` en métodos anotados**: rápido, pero exige que el método viva en un bean concreto conocido en tiempo de compilación — no encaja con "23 endpoints descubiertos dinámicamente por OpenAPI, que pueden cambiar cuando se agreguen endpoints nuevos".
   - **`ToolCallback`/`FunctionToolCallback` construidos dinámicamente**: se arma una lista de `ToolCallback` en runtime (nombre, descripción, JSON schema de parámetros, función `Function<String, String>` o similar que ejecuta la llamada) y se pasa a `chatClient.prompt().toolCallbacks(lista).call()`. **Esta es la opción que encaja** con el patrón de `DescubridorTools` de Python: un `ToolCallback` por endpoint filtrado del OpenAPI, construido en runtime, no en tiempo de compilación.
2. **Mecanismo de descubrimiento de herramientas** (igual dilema que en v1, sigue sin resolver):
   - **(a) Loopback HTTP a `GET /v3/api-docs`**, igual que Python — reutiliza el contrato REST público, fácil de testear con WireMock, no toca límites de Modulith. **Sigue siendo la recomendación.**
   - **(b) Introspección en proceso** del modelo de springdoc — evita el salto de red, más código de plomería.
3. **Fijar el modelo por versión explícita** (ej. `llama3.1:8b`) — sigue pendiente, `ADR-0017` lo preservó como `latest` a propósito porque no era su alcance tocarlo; el ADR de tool calling es el lugar natural para cerrarlo, dado que un modelo que cambia de peso sin aviso es más riesgoso todavía cuando además decide qué herramienta invocar.
4. **Alcance de herramientas expuestas**: heredar el filtro de Python (excluir `/auth/**`, `/plataforma/**`, `/ai/**`; solo `GET`/`POST` de consulta) y dejar escrito que ninguna escritura se expone sin decisión explícita posterior.
5. **Contrato nuevo vs. `POST /api/v1/ai/chat`**: no mutar el contrato v0 (`DD-UC-022` reafirmó que ese contrato es intocable) — nuevo endpoint (ej. `POST /api/v1/ai/agente`) con `respuesta` + `herramientasUsadas` + `turnos` + `camino` + `fuente`.
6. **Destino de `ExtraerConsultaUsuarioService`/`LlmStructuredExtractor`**: mismo dilema que v1 — una tool genérica `get_api_v1_usuarios` lo vuelve redundante para ese caso puntual; decidir si se retira, queda de *fallback*, o convive.
7. **Nuevo, específico de Spring AI**: decidir explícitamente **no** agregar los starters de MCP/*advisors*/*vector store* de Spring AI en este ADR (siguiendo la misma disciplina de alcance mínimo que ya aplicó `PR-IMPL-022`), y dejarlo para un ADR posterior si en algún momento se quiere exponer el catálogo de tools también vía MCP (paridad con el `MCP_HABILITADO` de Python).

### 6.2 Contrato Ollama/Open WebUI — ya no es "bajo nivel", ahora es Spring AI (sección reemplazada respecto a v1)

La v1 de este análisis recomendaba migrar el adaptador de `/api/generate` a `/api/chat` a mano y escribir DTOs Jackson para `tool_calls`. **Eso ya no aplica**: con Spring AI en el classpath, ese trabajo lo hace el framework.

- `OllamaLlmAdapter` y `OpenWebUiLlmAdapter` ya usan `ChatClient`/`ChatModel` de Spring AI (`ADR-0017`). Spring AI's `OllamaChatModel` ya habla `/api/chat` (no `/api/generate`) internamente cuando se le pasan `tools` — no hay que tocar el protocolo HTTP a mano.
- Lo que sí falta: hoy los adaptadores solo llaman `.prompt(texto).call()` (un turno, sin tools). Falta el camino que llame `.prompt().messages(historial).toolCallbacks(tools).call()` y exponga la respuesta con sus posibles *tool calls* — esto no es un cambio de proveedor ni de dependencia (ya está en el `pom.xml`), es un cambio de **cómo se usa** la dependencia que ya existe. Menor riesgo, no requiere el gate de BOM que sí exigió `ADR-0017`.
- Verificar que el mismo `HttpClient` HTTP/1.1 que `AiConfig` fuerza para Open WebUI siga aplicando cuando se agregue esta nueva forma de invocar al `ChatClient` (no hay razón para que cambie, pero es el tipo de detalle que `PR-IMPL-022` ya tuvo que resolver una vez y vale la pena no romper).

### 6.3 `LlmPort` y el bucle ReAct — el corazón del cambio (actualizado)

- **No conviene tocar `LlmPort.completar(String)`** — `ADR-0017`/`DD-UC-022` lo tratan como una interfaz estable y hay tests (`ChatConLlmServiceTest`) que dependen de su forma actual. La opción consistente con el patrón que el propio repo ya usó dos veces (mantener el puerto viejo, añadir uno nuevo al lado) es un **puerto nuevo** en `application/port/out` (ej. `AgenteLlmPort` o `EjecutarConToolsPort`) con una firma conversacional: lista de mensajes + lista de `HerramientaLlm` (nombre, descripción, schema) → respuesta con texto y/o `tool_calls`.
- Ese puerto nuevo, en `infrastructure`, se implementa apoyándose en el `ChatClient` de Spring AI (mismo bean que ya construye `AiConfig`), pasándole los `ToolCallback` construidos dinámicamente (§6.1.1). Aquí hay una decisión de diseño real: **dejar que Spring AI corra el bucle de tool-calling internamente** (le pasás los `ToolCallback` y el `ChatClient` decide cuándo invocarlos, turno tras turno, devolviendo solo el texto final) **vs. controlar el bucle a mano** como hace `Lab5/agente.py` (útil si quieres el registro turno-a-turno de `camino`/`herramientasUsadas`/`turnos` que pide el contrato nuevo de §6.1.5). Spring AI permite ambos modos (`internalToolExecutionEnabled` true/false); dado que el proyecto valora mucho la auditabilidad explícita (ver `shared/audit`, `compliance-agent`), **la recomendación es desactivar la ejecución interna y controlar el bucle a mano**, igual que hace Python, para poder registrar cada paso.
- Nuevo servicio de aplicación (ej. `EjecutarConsultaAgenteService`) con el mismo contrato que `Lab5/agente.py::ejecutar_agente`: tope de turnos (`AiProperties` necesita un campo nuevo), ejecutar todas las tool calls de un turno, anexar resultados, cortar en el primer texto final o al agotar el tope.
- Portar o posponer explícitamente el camino `KEYWORD` y los resolutores compuestos — igual razonamiento que v1.

### 6.4 Ejecución de herramientas — respetando Spring Modulith (sin cambios de fondo desde v1)

- Ejecutor por HTTP loopback contra los propios controladores REST, igual que Python — consistente con la recomendación de descubrimiento por `GET /v3/api-docs` (§6.1.2-a) y no exige tocar `academico`/`identidad` ni crear un puerto de salida por endpoint.
- Filtrar argumentos inventados por el modelo contra el schema real antes de llamar al endpoint.
- Confirmar `ModularityTests` sigue en 7/7 tras el cambio (el propio `PR-IMPL-022` ya dejó ese número como el estándar a mantener).

### 6.5 Seguridad y guardrails (sin cambios de fondo desde v1, reforzado por NFR-007)

- Reproducir el filtro de exclusión de endpoints (§6.1.4).
- El guardrail "MUST NOT enviar PII/rude/calificaciones al modelo" —ahora verificado explícitamente como parte del *Definition of Done* de `PR-IMPL-022` (grep de logs sin prompt/respuesta/API key)— debe extenderse al **resultado de cada tool**, no solo al prompt inicial. Cuando `notassie` exista, sus endpoints quedan excluidos del catálogo por la misma regla de prefijo; vale la pena un test de regresión que falle si alguna tool descubierta apunta a un path con `calificaciones`/`notas`.
- Nunca loguear el JWT ni el cuerpo completo de una respuesta con datos de usuario.
- Guardrail de confirmación humana antes de una escritura (patrón `Lab3/fase3_confirmacion_escritura.py`) — no urgente hoy (catálogo 100% lectura), pero es el guardrail a portar antes de exponer cualquier tool de escritura.

### 6.6 Auditoría (sin cambios desde v1)

- Evaluar si el módulo `shared/audit` ya existente (`ADR-0003`) puede registrar cada llamada a herramienta (`tool`, `argumentos`, `resultado`, `ts`) con una forma como la de `Lab3/auditoria_tooling.jsonl`, en vez de crear un mecanismo paralelo. Le da a `compliance-agent` un artefacto real que auditar.

### 6.7 Configuración y secretos (actualizado)

- Nuevas propiedades en `AiProperties`/`application.yml`: modelo fijado (reemplaza `latest`), tope de turnos, y un feature flag independiente para tool calling que no afecte el chat v0.
- Como `spring.ai.model.chat=none` ya está desactivado a propósito (para no exigir `spring.ai.openai.api-key` con Ollama), cualquier propiedad nueva de Spring AI relacionada a tools debe seguir mapeándose manualmente desde `edusync.ai.*` en `AiConfig`, no asumirse autoconfigurada — es el mismo patrón que ya fijó `PR-IMPL-022`.
- Seguir la convención vigente: nada hardcodeado, todo vía env + `.env.example`/`.env`.

### 6.8 Testing (actualizado)

- Portar el enfoque de `edusync-agente-llm/tests/` (todo mockeado) a JUnit: mocks del `ChatClient` de Spring AI devolviendo `tool_calls`, mocks del descubridor de OpenAPI, test de integración del filtro de seguridad.
- Mantener `mvn test` en verde y `ModularityTests` en 7/7 — el estándar que ya dejó `PR-IMPL-022` (250/250).
- Dejar `ChatConLlmServiceTest` intacto, igual que hizo `PR-IMPL-022` — el puerto viejo no debería regresar por este cambio.

### 6.9 Documentación a actualizar (dos frentes: el pendiente de `ADR-0017` + el nuevo de tool calling)

- **Pendiente ya generado por `ADR-0017`** (no esperar al ADR de tool calling para esto): sincronizar `.claude/agents/ollama-agent.md` y `.claude/skills/ollama-edusync/SKILL.md` (+ espejos `.cursor/`) para que dejen de describir `OllamaLlmAdapter`/`OpenWebUiLlmAdapter` hablando HTTP a mano — hoy hablan de Spring AI en `AGENTS.md` pero no en estos dos archivos.
- `AGENTS.md` §8.1 (fila `ollama-agent`): corregir `llama3.1:latest` → versión fijada, cuando se cierre el punto 3 del §6.1.
- Una vez cerrado `ADR-0018`: sección "Contrato v1 — tool calling" en el skill, análoga a la que documenta el contrato v0.
- `docs/PROMPT_MAPPING.md`: registrar `PR-ADR-011` (ADR) y `PR-IMPL-023` (implementación) cuando corresponda — siguiendo la misma numeración que ya usó `ADR-0017`/`PR-ADR-010`/`PR-IMPL-022`.
- Seguir pendiente la pregunta de si esto merece su propio `FSD-UC` (sin cambios desde v1: ninguno de los `FSD-UC` vivos lo cubre; `DD-UC-022` mismo lo trata como delta de `NFR-007`, no como feature de producto).

---

## 7. Próxima acción sugerida

El mayor apalancamiento sigue siendo redactar el ADR de tool calling — ahora `ADR-0018` — resolviendo los siete puntos del §6.1, con dos añadidos importantes respecto a la v1 de este análisis: (a) ya no hay que decidir el cliente HTTP, porque `ADR-0017` lo resolvió con Spring AI; el punto de diseño real ahora es **cómo declarar las 23 herramientas como `ToolCallback`s dinámicos** y **si el bucle de tool-calling lo corre Spring AI internamente o se controla a mano** (recomendado, por auditabilidad); (b) conviene cerrar primero el hallazgo de higiene documental del §6.9 (`ollama-agent.md`/`SKILL.md` desincronizados de `ADR-0017`), para no acumular una segunda brecha de documentación sobre la primera.

Puedo ayudarte a redactar `ADR-0018` con el skill `adr-edusync` en cuanto confirmes las decisiones de diseño de cada punto. Y, como en la v1: si me compartes directamente los `.java` de `shared/ai` (o los mueves a una carpeta menos anidada), puedo confirmar contra código real — en particular el `AiConfig` y los dos adaptadores, que son justo los que tocó `PR-IMPL-022` — en vez de partir de lo que documentan el ADR y el Design Doc.

---

## Historial de este documento

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v1 | 13/09/2026 | Primer análisis: brecha entre `edusync-agente-llm` (Python) y `shared.ai` (Java, cliente HTTP artesanal, sin tool calling). |
| v2 | 13/09/2026 | Repetido a pedido de Rodrigo tras detectar que el repo ejecutó `ADR-0017`/`DD-UC-022`/`PR-IMPL-022` (adopción de Spring AI) entre ambas revisiones. Actualiza §1, §4, §5, §6.1-6.3, §6.7-6.9 y agrega §0. El resto (§2, §3) no cambió porque no hubo cambios en los laboratorios ni en `edusync-agente-llm`. |
