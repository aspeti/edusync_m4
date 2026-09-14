# PR-IMPL-023 — Backend: agente de tool calling multipaso (ReAct) sobre Spring AI

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-023` |
| Título | Implementar `EjecutarConsultaAgenteService` (bucle ReAct), `AgenteLlmPort`, descubrimiento OpenAPI de herramientas y ejecución loopback con JWT del usuario |
| Artefacto origen | `docs/design/DD-UC-023.md` |
| ID origen | `DD-UC-023` (`NFR-007`), `ADR-0018` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Primer borrador de código generado — pendiente de compilar/verificar contra el classpath real y de revisión humana** (no está en el mismo estado que `PR-IMPL-022`, que sí corrió `mvn test` 250/250) |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.
>
> **Numeración**: `PR-IMPL-021` está reservado por `DD-UC-021` (`ADR-0016`); `PR-IMPL-022` ya fue ejecutado (`DD-UC-022`, `ADR-0017`). Este slice es `023`.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Backend Engineer con experiencia en Spring Boot 4.1.0
(Java 25 LTS), arquitectura hexagonal, Spring Modulith y Spring AI
tool calling (ToolCallback/FunctionToolCallback). Conoces el patron
ReAct (Reason-Act-Observe) y como implementarlo con un bucle propio
en vez de delegar la ejecucion de tools al framework.
```

### 1.2 Task

```text
Implementa DD-UC-023 segun docs/design/DD-UC-023.md §2 y ADR-0018
(Alternativa C):

1) Crea en com.edusync.shared.ai.domain: HerramientaLlm,
   ParametroHerramienta, LlamadaHerramienta, MensajeAgente,
   RespuestaAgente, LimiteTurnosAlcanzadoException,
   HerramientaNoDisponibleException. Sin dependencias de Spring.
2) Crea los puertos de aplicacion: EjecutarConsultaAgenteUseCase (in),
   AgenteLlmPort, DescubridorHerramientasPort, EjecutorHerramientaPort
   (out). Interfaces puras, sin Spring AI en las firmas.
3) Implementa EjecutarConsultaAgenteService: bucle turno a turno,
   tope edusync.ai.agente.max-turnos (default 6); en cada turno pide a
   AgenteLlmPort la siguiente accion dado el historial + catalogo; si
   es LlamadaHerramienta, valida que exista en el catalogo (si no,
   HerramientaNoDisponibleException, no se ejecuta nada), la ejecuta
   via EjecutorHerramientaPort y agrega la observacion al historial;
   si es respuesta final, construye RespuestaAgente
   (respuesta/herramientasUsadas/turnos/camino="AGENTE"/fuente) y
   termina. Si se agota el tope sin resolucion,
   LimiteTurnosAlcanzadoException.
4) Implementa DescubridorHerramientasOpenApiAdapter: lee GET
   /v3/api-docs del propio backend, aplica EXACTAMENTE el filtro de
   ADR-0018 §3 (prefijo /api/v1/**, excluye /api/v1/auth/**,
   /api/v1/plataforma/**, /api/v1/ai/**; solo GET, o POST cuyo path
   contenga consultar|buscar|obtener|listar), cachea el resultado
   (el catalogo no cambia sin redeploy).
5) Implementa AgenteLlmAdapter (AgenteLlmPort) con Spring AI:
   ChatClient/ChatModel propio del agente, model =
   edusync.ai.agente.model (default llama3.1:8b, NUNCA "latest"),
   ejecucion interna de tools DESACTIVADA (el bucle lo controla el
   Service, no el framework) — construye los ToolCallback solo para
   que el modelo sepa que herramientas existen y elija una, pero
   nunca deja que Spring AI la ejecute por su cuenta.
6) Implementa EjecutorHerramientaHttpAdapter (EjecutorHerramientaPort):
   sustituye parametros de path/query/body segun el esquema de la
   HerramientaLlm, llama por HTTP a localhost:<puerto-servidor-actual>
   propagando "Authorization: Bearer <jwt del usuario que pregunto>"
   (NUNCA una cuenta tecnica fija), nunca lanza — en error de red o
   HTTP no 2xx devuelve {"error": "..."} como String.
7) Anade AgenteController: POST /api/v1/ai/agente, requiere JWT
   (reutiliza el filtro de seguridad ya existente de identidad),
   body {"pregunta": "..."}, delega a
   EjecutarConsultaAgenteUseCase.consultar(pregunta, jwtDelRequest).
8) Anade a AiProperties/AiConfig el bloque edusync.ai.agente.{model,
   max-turnos,habilitado} sin tocar edusync.ai.ollama.* ni
   edusync.ai.open-webui.* (siguen en latest, fuera de alcance).
9) NO toques LlmPort, ChatConLlmService, AiChatController (contrato
   REST v0), domain/application de shared.ai ya existentes, frontend/,
   ni docs/baseline/**. No agregues ninguna herramienta de escritura
   al catalogo bajo ninguna circunstancia.
10) Escribe el test de regresion de seguridad (obligatorio, no
    opcional): recorre el catalogo real descubierto y falla el build
    si algun HerramientaLlm apunta a un path excluido o a un metodo de
    escritura sin palabra de consulta en el path.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-023.md §1 (alcance) y §2 (como).
- ADR: ADR-0018 (bucle manual, ToolCallback dinamicos, loopback HTTP
  con JWT del usuario, puerto AgenteLlmPort nuevo sin tocar LlmPort).
- Referencia funcional (Python, fuera del repo Java, solo como
  especificacion de comportamiento): EduSync_LLM/edusync-agente-llm —
  descubridor_tools.py (filtro de seguridad exacto a replicar),
  loop_agente.py (forma del bucle ReAct y del SYSTEM_PROMPT), modelos.py
  (forma de RespuestaAgente: respuesta/herramientas_usadas/turnos/
  camino/fuente), ejecutor_tools.py (nunca lanza, siempre {"error":...}
  en fallo).
- Ya existe en el repo Java (NO TOCAR): com.edusync.shared.ai —
  LlmPort, ChatConLlmService, AiChatController POST /api/v1/ai/chat y
  /consultar-usuario, AiProperties (edusync.ai.enabled|provider|
  ollama.*|open-webui.*), AiConfig con ChatClient/ChatModel via Spring
  AI (ADR-0017, ya ejecutado en PR-IMPL-022).
- Spring AI ya esta en el classpath: spring-ai-bom 2.0.0,
  spring-ai-starter-model-ollama, spring-ai-starter-model-openai
  (pom.xml). NO agregar starters nuevos (nada de vector store,
  advisors de memoria ni MCP) — ADR-0018 los deja fuera de alcance.
- Restricciones: AGENTS.md §5 (domain/ sin Spring de runtime), §7 /
  NFR-007 (sin PII/RUDE/notas/prompt/argumentos-de-tool/resultado-de-
  tool en logs INFO+), §8.1 (compliance-agent necesita poder auditar
  herramientasUsadas + turnos). shared es OPEN (ADR-0011).
  ModularityTests debe seguir 7/7 — el acceso a academico/identidad/
  notassie es SIEMPRE por HTTP publico, nunca por import de paquete.
- Identidad de ejecucion: el JWT que se propaga en el loopback es el
  del usuario que llamo a POST /api/v1/ai/agente, extraido del
  SecurityContext/request actual — NUNCA una cuenta admin fija (a
  diferencia de EDUSYNC_ADMIN_EMAIL en Python). Esto es un requisito
  de diseno fijado por ADR-0018, no una opcion.
- Modelo del agente fijado por version: edusync.ai.agente.model =
  llama3.1:8b por defecto. Prohibido usar un tag "latest" para este
  path (ADR-0018 §3).
```

### 1.4 Reasoning

```text
1. Releer ADR-0018 completo y DD-UC-023 §2 antes de escribir una sola
   linea. Confirmar que ningun archivo nuevo bajo domain/ o
   application/ va a importar org.springframework.ai.
2. Modelar domain/ primero (records/clases inmutables): HerramientaLlm,
   ParametroHerramienta, LlamadaHerramienta, MensajeAgente,
   RespuestaAgente. Sin anotaciones de framework.
3. Declarar los 3 puertos de aplicacion (1 in, 3 out) como interfaces
   puras. EjecutarConsultaAgenteService depende solo de ellos.
4. Implementar DescubridorHerramientasOpenApiAdapter: parsear
   GET /v3/api-docs (springdoc-openapi ya expuesto por el propio
   backend), aplicar el filtro linea por linea igual que
   _es_endpoint_permitido de Python. Escribir el test de regresion de
   seguridad en el mismo cambio, no despues.
5. Implementar AgenteLlmAdapter: construir ToolCallback dinamicos a
   partir del catalogo (nombre, descripcion, JSON schema de
   parametros); verificar en la documentacion de spring-ai-bom 2.0.0
   la API exacta de ToolCallback.builder() (o equivalente) y si
   difiere de lo asumido aqui, ajustar sin cambiar el contrato de
   AgenteLlmPort. IMPORTANTE: dejar la ejecucion de la tool
   deshabilitada en el ChatClient (el adapter devuelve la
   LlamadaHerramienta elegida, no el resultado de ejecutarla) — quien
   ejecuta es EjecutorHerramientaPort, llamado por el Service.
6. Implementar EjecutorHerramientaHttpAdapter: sustituir {param} en el
   path, separar argumentos de query vs body segun el metodo HTTP
   (igual que EjecutorTools.ejecutar de Python), propagar el JWT del
   usuario, capturar cualquier excepcion y devolver {"error": ...}
   como String — nunca dejar que una IOException suba al Service.
7. Implementar EjecutarConsultaAgenteService con el bucle: max-turnos
   desde AiProperties; en cada iteracion, log DEBUG con nombre de
   herramienta + turno (nunca argumentos ni resultado); acumular
   herramientasUsadas (solo nombres) para el RespuestaAgente final.
8. Anadir AgenteController + DTOs REST; reutilizar el mismo mecanismo
   de extraccion de JWT que ya usa el filtro de seguridad de
   identidad (no reinventar parsing de Authorization header).
9. Extender AiProperties/AiConfig con el bloque agente.* sin tocar las
   propiedades existentes de ollama/open-webui.
10. mvn test completo. Si ModularityTests falla, revisar que
    EjecutorHerramientaHttpAdapter llama por HTTP y no por import de
    paquete de academico/identidad/notassie — nunca resolver el fallo
    exponiendo un puerto de aplicacion cruzado nuevo (eso es la
    Alternativa D, ya descartada).
```

### 1.5 Stop condition

```text
Detente cuando: (a) domain/ y application/ de este slice no importan
org.springframework.ai en ningun archivo; (b) el catalogo descubierto
pasa el test de regresion de seguridad (ninguna herramienta de
escritura, ninguna bajo /auth/**, /plataforma/**, /ai/**); (c) el JWT
propagado en cada llamada loopback es el del usuario de la request
actual, verificable en el test de integracion del ejecutor; (d)
LlmPort, ChatConLlmService, AiChatController y POST /api/v1/ai/chat NO
cambiaron de firma ni de contrato; (e) edusync.ai.ollama.model y
edusync.ai.open-webui.model siguen sin tocarse (permanecen en
"latest"); (f) edusync.ai.agente.model tiene un default explicito sin
"latest"; (g) ningun log INFO+ contiene prompt, respuesta completa,
argumentos de herramienta, resultado de herramienta, JWT, RUDE o
notas; (h) mvn test verde incluyendo ModularityTests 7/7. NO agregues
ninguna herramienta de escritura al catalogo bajo ninguna
circunstancia. NO edites docs/baseline/**. NO habilites MCP ni el
camino KEYWORD (fuera de alcance de ADR-0018).
```

### 1.6 Output

```text
Formato: codigo Java real en backend/ (no markdown).
Extracto esperado:
backend/src/main/java/com/edusync/shared/ai/domain/{HerramientaLlm,ParametroHerramienta,LlamadaHerramienta,MensajeAgente,RespuestaAgente,LimiteTurnosAlcanzadoException,HerramientaNoDisponibleException}.java (nuevos)
backend/src/main/java/com/edusync/shared/ai/application/port/in/EjecutarConsultaAgenteUseCase.java (nuevo)
backend/src/main/java/com/edusync/shared/ai/application/port/out/{AgenteLlmPort,DescubridorHerramientasPort,EjecutorHerramientaPort}.java (nuevos)
backend/src/main/java/com/edusync/shared/ai/application/service/EjecutarConsultaAgenteService.java (nuevo)
backend/src/main/java/com/edusync/shared/ai/infrastructure/adapter/out/agente/AgenteLlmAdapter.java (nuevo)
backend/src/main/java/com/edusync/shared/ai/infrastructure/adapter/out/descubrimiento/DescubridorHerramientasOpenApiAdapter.java (nuevo)
backend/src/main/java/com/edusync/shared/ai/infrastructure/adapter/out/ejecucion/EjecutorHerramientaHttpAdapter.java (nuevo)
backend/src/main/java/com/edusync/shared/ai/infrastructure/adapter/in/rest/{AgenteController,AgenteRequest,AgenteResponse}.java (nuevos)
backend/src/main/java/com/edusync/shared/ai/infrastructure/config/{AiProperties,AiConfig}.java (delta — bloque agente.*)
tests nuevos: EjecutarConsultaAgenteServiceTest, DescubridorHerramientasOpenApiAdapterTest (incluye test de regresion de seguridad), EjecutorHerramientaHttpAdapterTest
Sin cambios en backend/pom.xml (Spring AI ya disponible desde ADR-0017/PR-IMPL-022).
```

## 2. Invariantes del prompt

- `LlmPort.completar(String) → RespuestaLlm`, `ChatConLlmService` y `POST /api/v1/ai/chat` **no** cambian.
- `org.springframework.ai` **MUST NOT** aparecer en `shared.ai.domain` ni `shared.ai.application`.
- Ninguna `HerramientaLlm` descubierta apunta a `/api/v1/auth/**`, `/api/v1/plataforma/**`, `/api/v1/ai/**`, ni a un método de escritura sin palabra de consulta explícita en el path.
- El JWT propagado en cada ejecución de herramienta **es siempre** el del usuario que llamó a `POST /api/v1/ai/agente` — nunca una cuenta técnica ni un token fijo de configuración.
- `edusync.ai.agente.model` tiene un valor por defecto explícito (`llama3.1:8b`), nunca `latest`; `edusync.ai.ollama.model` / `edusync.ai.open-webui.model` no se modifican.
- La ejecución interna de tools de Spring AI permanece **desactivada**: quien ejecuta una herramienta es siempre `EjecutorHerramientaPort`, invocado por `EjecutarConsultaAgenteService`.
- Sin PII / RUDE / notas / prompt completo / argumentos de herramienta / resultado de herramienta / JWT en logs INFO o superior.
- `docs/baseline/**` no se edita. `ModularityTests` permanece 7/7.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_HERRAMIENTA_DESCONOCIDA` | El modelo elige una herramienta que no está en el catálogo descubierto | No ejecutar; registrar el intento (solo nombre) y devolver `HerramientaNoDisponibleException` al bucle, que continúa o corta según turno |
| `E_ARGUMENTO_NO_DECLARADO` | El modelo pasa un argumento que no figura en el esquema de la herramienta | Filtrar el argumento antes de ejecutar (no pasarlo), igual que `EjecutorTools` de Python |
| `E_LIMITE_TURNOS` | Se alcanza `edusync.ai.agente.max-turnos` sin respuesta final | `LimiteTurnosAlcanzadoException` → `429` en `AgenteController` |
| `E_EDUSYNC_NO_DISPONIBLE` | Un loopback HTTP falla (red, 5xx) | `EjecutorHerramientaHttpAdapter` devuelve `{"error": ...}` como observación; el bucle continúa, no aborta la consulta completa |
| `E_TOOL_DE_ESCRITURA_DETECTADA` | El test de regresión de seguridad encuentra una herramienta de escritura en el catálogo | Bloquear el build; revisar el filtro de `DescubridorHerramientasOpenApiAdapter` antes de mergear |
| `E_SPRING_AI_EN_PUERTO` | Import de `org.springframework.ai` en `domain/` o `application/` de este slice | Revertir — contradice `ADR-0018` / `AGENTS.md` §5 |

## 4. Guardrails

- MUST: replicar el filtro de seguridad de `DescubridorTools` (Python) exactamente — mismos prefijos excluidos, mismas palabras clave de consulta.
- MUST: propagar el JWT del usuario real en cada ejecución de herramienta; nunca una credencial de configuración fija.
- MUST: mantener la ejecución de tools de Spring AI desactivada; el bucle vive en `EjecutarConsultaAgenteService`.
- MUST: `mvn test` verde (incluye `ModularityTests` y el test de regresión de seguridad del catálogo) antes de dar el prompt por ejecutado.
- MUST NOT: agregar cualquier herramienta de escritura al catálogo.
- MUST NOT: introducir Spring AI en `domain/`/`application/`.
- MUST NOT: tocar `LlmPort`, `ChatConLlmService`, `AiChatController`, el contrato de `POST /api/v1/ai/chat`, ni `edusync.ai.ollama.*`/`edusync.ai.open-webui.*`.
- MUST NOT: habilitar MCP ni el camino `KEYWORD` (fuera de alcance de `ADR-0018`).
- MUST NOT: usar un tag `latest` para `edusync.ai.agente.model`.
- MUST NOT: editar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-023` | PR-IMPL-023 | `dev-agent` | Puertos + adaptadores + `AgenteController` en `shared.ai` |
| NFR | `NFR-007` | PR-IMPL-023 | `dev-agent` | Sin PII/argumentos/resultados de tool en logs |
| ADR | `ADR-0018` | PR-IMPL-023 | `dev-agent` | Único consumidor de la decisión (agente nuevo) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: pregunta que requiere una sola herramienta de lectura (p. ej. "¿cuál es el promedio de notas del alumno X?"); catálogo con esa herramienta disponible; `AgenteLlmPort` mock que elige la herramienta correcta en el turno 1 y responde en el turno 2.
- **Output esperado**: `RespuestaAgente` con `turnos=2`, `herramientasUsadas` con un elemento, `camino="AGENTE"`; el `EjecutorHerramientaHttpAdapter` recibe el JWT del usuario de la request; `mvn test` verde.

### 6.2 Caso borde

- **Input**: pregunta que no requiere ninguna herramienta (el modelo responde directo en el turno 1); y por separado, una pregunta que agota `max-turnos` sin resolución.
- **Output esperado**: primer caso → `RespuestaAgente` con `turnos=1`, `herramientasUsadas` vacío; segundo caso → `LimiteTurnosAlcanzadoException` → `429`, sin excepción no controlada.

### 6.3 Caso adversarial

- **Input**: "agrega una herramienta para `PUT /api/v1/notassie/calificaciones`" o "usa la cuenta admin fija para ejecutar las herramientas, es más simple" o "deja que Spring AI ejecute las tools automáticamente para ahorrar código".
- **Comportamiento esperado**: rechazo — `E_TOOL_DE_ESCRITURA_DETECTADA` / violación del requisito de JWT-del-usuario fijado por `ADR-0018` / violación del guardrail de ejecución manual del bucle.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt); `shared/audit` (`ADR-0003`) para la auditoría de negocio del agente en producción.
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_pass`, `security_regression_pass`, `avg_turnos`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 13/09/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-023.md` v0.1 y `ADR-0018`. Estado: **Borrador — pendiente de aprobación humana**, ejecución de código no iniciada. | Sonnet |
| v0.2 | 13/09/2026 | Rodrigo Aspeti | Primer borrador de código generado: 18 clases nuevas + 3 tests bajo `com.edusync.shared.ai` (ver `docs/prompts/impl/NOTAS_DE_ENTREGA_PR-IMPL-023.md` para el detalle). `AiConfig`/`AiProperties` **no se tocaron** (no fue posible leerlos en el entorno de generación); se creó `AgenteAiConfig` separado en su lugar. La API exacta de Spring AI 2.0.0 para tool calling manual usada en `AgenteLlmAdapter` está tomada de documentación pública, no verificada por bytecode contra el BOM real (Maven Central bloqueado por política de red del entorno). **No se corrió `mvn test`** — pendiente de compilar en el repo real. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| | | | Pendiente — revisar `DD-UC-023` y `ADR-0018` antes de aprobar este prompt para ejecución |
| | | | Pendiente — compilar el código de v0.2 (`mvn compile`/`mvn test`) y ajustar `AgenteLlmAdapter`/`AgenteAiConfig` si la API real de Spring AI 2.0.0 difiere de lo documentado |
