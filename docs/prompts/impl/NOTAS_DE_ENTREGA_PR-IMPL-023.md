# Notas de entrega — primer borrador de código de `PR-IMPL-023`

> Este archivo no es parte del código; es la trazabilidad de qué se generó, qué se asumió y qué falta verificar antes de dar `PR-IMPL-023` por "Ejecutado" (como sí lo está `PR-IMPL-022`).

## Qué se generó

18 archivos Java nuevos y aditivos + 3 archivos de test, todos bajo `com.edusync.shared.ai`, siguiendo exactamente el árbol de `DD-UC-023` §2:

- `domain/`: `HerramientaLlm`, `ParametroHerramienta`, `LlamadaHerramienta`, `MensajeAgente`, `RespuestaAgente`, `LimiteTurnosAlcanzadoException`, `HerramientaNoDisponibleException`.
- `application/port/in/`: `EjecutarConsultaAgenteUseCase`.
- `application/port/out/`: `AgenteLlmPort`, `DescubridorHerramientasPort`, `EjecutorHerramientaPort`.
- `application/service/`: `EjecutarConsultaAgenteService` (el bucle ReAct).
- `infrastructure/config/`: `AgenteAiConfig` (nuevo — ver más abajo por qué no se tocó `AiConfig`).
- `infrastructure/adapter/out/agente/`: `AgenteLlmAdapter`.
- `infrastructure/adapter/out/descubrimiento/`: `DescubridorHerramientasOpenApiAdapter`.
- `infrastructure/adapter/out/ejecucion/`: `EjecutorHerramientaHttpAdapter`.
- `infrastructure/adapter/in/rest/`: `AgenteController`, `AgenteRequest`, `AgenteResponse`.
- Tests: `EjecutarConsultaAgenteServiceTest` (completo, sin dependencias externas más allá de Mockito/AssertJ), `DescubridorHerramientasOpenApiAdapterTest` (incluye el test de regresión de seguridad obligatorio, con fixture de OpenAPI embebido), `EjecutorHerramientaHttpAdapterTest` (con `MockRestServiceServer`).

**`backend/pom.xml` no se tocó** — Spring AI ya está en el classpath desde `ADR-0017`/`PR-IMPL-022`; este slice no agrega dependencias.

**`LlmPort`, `ChatConLlmService`, `AiChatController`, `POST /api/v1/ai/chat` no se tocaron.**

## Por qué `AiConfig.java` y `AiProperties.java` NO se modificaron

El entorno desde el que se generó este código no pudo leer esos dos archivos: `device_stage_files` rechaza rutas a más de 7 carpetas de profundidad bajo la carpeta conectada (`backend/src/main/java/com/edusync/shared/ai/infrastructure/config/` está a 10), y `device_bash` sigue bloqueado por el problema de montaje de Windows del 8 de septiembre (confirmado de nuevo en esta sesión). Sobrescribir esos archivos a ciegas habría arriesgado destruir código ya probado (`mvn test` 250/250 en `PR-IMPL-022`).

En su lugar, se creó `AgenteAiConfig.java` como una `@Configuration` **separada y aditiva**: define su propio bean `agenteChatClient` leyendo `edusync.ai.agente.model` y reutilizando `edusync.ai.ollama.base-url` como *fallback* vía un placeholder anidado de Spring (`${edusync.ai.agente.ollama-base-url:${edusync.ai.ollama.base-url:http://localhost:11434}}`), sin necesitar conocer la forma Java interna de `AiProperties`. `EjecutarConsultaAgenteService` tampoco depende de `AiProperties`: usa `@Value` directo sobre `edusync.ai.agente.max-turnos` y `edusync.ai.agente.model`.

**Recomendación**: si prefieres que estas propiedades vivan formalmente dentro de `AiProperties` (como decía `DD-UC-023` §2 originalmente), es un cambio de bajo riesgo y aislado que se puede hacer después, una vez pueda leerse el archivo real — por ejemplo conectando `backend/src/main/java/com/edusync/shared/ai` como carpeta independiente desde la app de escritorio (así queda a ≤3 niveles de profundidad para `device_stage_files`).

## Lo que hay que verificar antes de compilar (no se pudo verificar en este entorno)

1. **API exacta de Spring AI 2.0.0 para tool calling manual** (`AgenteLlmAdapter`, `AgenteAiConfig`): `ToolCallingChatOptions`, `ChatResponse.hasToolCalls()/getToolCalls()`, `ToolCall.id()/name()/arguments()`, `ToolResponseMessage`, `ToolDefinition.builder()`, `OllamaApi`/`OllamaChatModel`/`OllamaOptions`. Están tomados de la documentación pública de Spring AI (`docs.spring.io/spring-ai/reference/api/tools.html` y el blog "Tool Calling in Spring AI 2.0: A Composable, Agentic Architecture"), **no verificados por bytecode**: el sandbox donde se generó este código tiene bloqueado el acceso a Maven Central por política de red de la organización (`repo.maven.apache.org` devuelve 403 en el proxy de salida), así que no pude resolver `spring-ai-bom:2.0.0` para inspeccionar las clases reales.
   - **Acción recomendada**: correr `mvn -o dependency:resolve-sources` o abrir el jar de `spring-ai-client-chat`/`spring-ai-model` ya presente en tu `~/.m2` local, y ajustar únicamente `AgenteLlmAdapter.java`/`AgenteAiConfig.java` si algún nombre difiere. El resto del slice (`domain/`, `application/`, los otros dos adaptadores, el controller) no depende de esa API y no debería necesitar cambios.
2. **`DescubridorHerramientasOpenApiAdapterTest`**: el fixture de `/v3/api-docs` es sintético (inventado a mano, no extraído de tu OpenAPI real). Es suficiente para probar el filtro, pero conviene correr también el catálogo contra el `/v3/api-docs` real del backend levantado, para confirmar que `academico`/`identidad`/`notassie` no exponen ningún endpoint de escritura mal etiquetado.
3. **Paridad manual**: una vez compile, correr las 4 preguntas de referencia de `INFORME_EDUSYNC_AGENTE_LLM.md` contra `POST /api/v1/ai/agente` y comparar `camino`/herramienta usada con el agente Python (ADR-0018 §7).

## Limpieza pendiente en tu carpeta (de la entrega anterior)

Sigue pendiente borrar manualmente `Maestria\edusync_m4\edusync_m4\` (carpeta duplicada por un error mío al guardar `ADR-0018`/`DD-UC-023`/`PR-IMPL-023`, ya corregido) y el archivo suelto `backend/src/main/java/com/edusync/shared/ai/infrastructure/config/_depth_test.txt` (un archivo de prueba de 4 bytes que usé para confirmar que sí se puede escribir a esa profundidad). Ninguno de los dos afecta la compilación, pero conviene limpiarlos. `device_bash` sigue sin poder borrar archivos por el problema de montaje, así que no pude hacerlo yo.
