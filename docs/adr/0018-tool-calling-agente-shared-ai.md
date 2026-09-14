# Architecture Decision Record (ADR)

## ADR-0018: Tool calling multipaso (ReAct) con agente en `shared.ai`, sobre Spring AI

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0018` |
| Título | Adopción de un agente de tool calling multipaso (ReAct) dentro del backend, apoyado en Spring AI (`ToolCallback`), detrás de un puerto de aplicación nuevo que convive con `LlmPort` |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Propuesta** — pendiente de aprobación humana explícita antes de ejecutar `PR-IMPL-023` |
| Alcance | Backend, paquete `com.edusync.shared.ai` (dominio, aplicación e infraestructura nuevos; no reabre `LlmPort`/`ChatConLlmService` existentes). No entra a `academico`/`identidad`/`notassie` más allá de consumir sus endpoints REST ya públicos. No toca el baseline congelado (`docs/baseline/`, tag `release/2.0.0`). No supersede a ningún ADR anterior. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0011` (`shared` OPEN, límites de Spring Modulith); `ADR-0017` (Spring AI ya adoptado detrás de `LlmPort`, primer consumidor de esta decisión). Consume el agente de referencia externo `EduSync_LLM/edusync-agente-llm` (Python) y los laboratorios `Lab3`/`Lab5` del Módulo 7. Habilita `DD-UC-023` / `PR-IMPL-023`. |

### 1. Contexto

`shared.ai` ya resuelve un chat de un solo turno (`POST /api/v1/ai/chat`, `ADR-0017`: Spring AI detrás de `LlmPort.completar(String)`) y una extracción de una sola intención (`ExtraerConsultaUsuarioService`). Ninguno de los dos deja que el modelo decida **qué endpoint invocar**: siempre es código Java el que decide qué dato traer.

En paralelo existe `EduSync_LLM/edusync-agente-llm`, un agente **externo** (Python) que sí resuelve esto: descubre en runtime, leyendo `GET /v3/api-docs`, un catálogo de 23 herramientas de solo lectura (filtrando `/api/v1/auth/**`, `/api/v1/plataforma/**`, `/api/v1/ai/**`, y solo `GET`/`POST` de consulta explícita), deja que Ollama decida cuáles encadenar (bucle ReAct, tope de turnos), las ejecuta vía HTTP contra el propio backend, y arma la respuesta final siempre por código — nunca el LLM redacta el texto que ve el usuario. Es la prueba de que el mecanismo funciona contra el dominio real de EduSync; lo que falta es traerlo **dentro** del artefacto desplegable Java.

Fuerzas en tensión:

1. **Mantener el hexágono** (`AGENTS.md` §5): igual que `ADR-0017`, Spring AI no debe filtrarse a `domain/` ni `application/`; y **no debe tocarse** `LlmPort.completar(String) → RespuestaLlm`, que ya tiene tests (`ChatConLlmServiceTest`) y un contrato público (`POST /api/v1/ai/chat`) estable.
2. **No violar Spring Modulith** (`ADR-0011`): el catálogo de herramientas cubre endpoints de `academico` e `identidad`; `shared.ai` no puede importar sus paquetes internos.
3. **NFR-007 / `AGENTS.md` §7** (nunca PII/RUDE/notas al modelo) se extiende aquí más allá del prompt: ahora también los **argumentos y resultados de cada herramienta** pasan por el modelo, y hay que decidir con qué identidad se ejecutan esas herramientas.
4. **Reproducibilidad**: el modelo del chat v0 sigue en `llama3.1:latest` (`ADR-0017` lo preservó a propósito, fuera de su alcance). Un modelo que decide qué acción tomar es más sensible a un cambio de peso silencioso que un modelo que solo redacta texto.
5. **Auditabilidad**: `shared/audit` (`ADR-0003`) y `compliance-agent` (`AGENTS.md` §8.1) existen precisamente para poder verificar qué hizo el sistema; un agente que decide acciones por su cuenta necesita ese mismo nivel de trazabilidad (qué herramienta, con qué argumentos, con qué resultado, en qué turno).

Lo que se sabe: Spring AI (`ADR-0017`) ya está en el classpath y soporta *tool calling* nativamente vía `ToolCallback`/`FunctionToolCallback`, construibles en runtime (no exige `@Tool` en un método conocido en compilación), lo que encaja con un catálogo que cambia cada vez que se agrega un endpoint nuevo. Lo que no se sabe todavía: el detalle exacto de la API de `ToolCallback.builder()` en `spring-ai-bom` 2.0.0 (versión ya fijada por `ADR-0017`) — verificarlo es parte de la ejecución (`PR-IMPL-023`), no de esta decisión de dirección.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. No implementar tool calling en Java; `edusync-agente-llm` (Python, externo) sigue siendo la única vía | Cero código nuevo, cero riesgo | Dos superficies de "IA sobre EduSync" que divergen con el tiempo; no hay demo end-to-end dentro del propio artefacto desplegable; no cumple el objetivo explícito de tener el agente *en* el backend | Bajo hoy, alto en objetivo incumplido |
| B. Tool calling con **ejecución interna** de Spring AI (`internalToolExecutionEnabled=true`, el default del framework): se registran los `ToolCallback` y el `ChatClient` corre el bucle solo, devolviendo únicamente el texto final | Mínimo código; aprovecha el framework al máximo | Se pierde la granularidad de auditoría turno a turno (`camino`, `herramientasUsadas`, `turnos`, `fuente`) que sí reporta hoy `edusync-agente-llm`; `compliance-agent` no tendría con qué verificar qué hizo el agente | Bajo en código, alto en trazabilidad perdida |
| C. Tool calling con **bucle controlado a mano** (ejecución interna de Spring AI desactivada), `ToolCallback`s construidos dinámicamente desde el mismo filtro de seguridad de `edusync-agente-llm`, ejecutados por **HTTP loopback contra los propios controladores REST ya públicos**, propagando el JWT del usuario que preguntó (no una cuenta técnica fija), detrás de un **puerto de aplicación nuevo** que convive con `LlmPort` sin tocarlo | Paridad de auditabilidad con el agente Python; no viola Modulith (no importa `academico`/`identidad`, solo llama a su contrato REST público); reutiliza Spring AI ya adoptado; el agente respeta el RBAC del usuario real (mejora sobre Python, que usa una cuenta admin fija para todo) | Más código que B; un salto HTTP interno por cada herramienta ejecutada (latencia de milisegundos, aceptable en un monolito local); duplica en Java la lógica de filtrado de seguridad que ya existe en Python (riesgo de que diverjan) | Medio |
| D. Tool calling por **puertos de aplicación directos** (un puerto de salida por endpoint expuesto como herramienta, siguiendo el precedente `BuscarUsuarioPorNombrePort`) | Sin salto HTTP; "hexagonal puro" | 23 puertos de salida (uno por endpoint) que hay que mantener sincronizados a mano con cada cambio de API; cada módulo tendría que exponer un puerto público nuevo solo para servir de herramienta, duplicando lo que ya expone su propio controlador REST; no escala — es exactamente el problema que Python evita leyendo el OpenAPI en vez de mantener una lista a mano | Alto y creciente con cada endpoint nuevo |

### 3. Decisión

> **Elegimos la Alternativa C.** El agente de tool calling vive en `shared.ai`, con el bucle ReAct controlado por código de aplicación (no por la ejecución interna de Spring AI), herramientas descubiertas dinámicamente desde `GET /v3/api-docs` con el mismo filtro de seguridad que ya prueba `edusync-agente-llm`, ejecutadas por HTTP loopback propagando el JWT del usuario original, detrás de un puerto nuevo (`AgenteLlmPort`) que **no reemplaza** a `LlmPort`.

Criterio decisivo: el mismo que ya usó `ADR-0017` — el hexágono aísla al proveedor y ahora aísla también al mecanismo de *tools*; el trabajo real es reproducir el filtro de seguridad y el bucle turno-a-turno con la trazabilidad que este proyecto exige (`shared/audit`, `compliance-agent`), no inventar un puerto nuevo del chat que ya funciona. La Alternativa B se descarta porque compra menos código a cambio de una propiedad — la auditabilidad por turno — que el propio repo ya trata como no negociable en otros módulos (`ADR-0003`, persistencia inmutable de audit log). La Alternativa D se descarta por el mismo motivo que ya evitó Python: mantener manualmente un puerto por endpoint no escala y es exactamente el trabajo que leer el OpenAPI evita.

**Puntos de diseño que este ADR fija** (detalle de implementación en `DD-UC-023`):

- **Identidad de ejecución**: las llamadas loopback a los controladores REST se hacen **propagando el JWT del usuario que hizo la pregunta al agente**, no con una cuenta técnica/admin fija. A diferencia de `edusync-agente-llm` (que se autentica una vez como `sysadmin@edusync.local` y todo usuario del agente ve lo mismo), el agente Java **nunca ve más de lo que el propio usuario podría consultar** llamando los endpoints directamente. Esto elimina además la necesidad de guardar una credencial técnica en configuración.
- **Modelo fijado para el camino agente**: nueva propiedad `edusync.ai.agente.model` (default `llama3.1:8b`, tag explícito, nunca `latest`), independiente de `edusync.ai.ollama.model`/`edusync.ai.open-webui.model` (que siguen en `latest` — `ADR-0017` los dejó fuera de su alcance; este ADR no los toca, solo fija el modelo del camino nuevo). Un agente que decide qué acción tomar no puede depender de un peso de modelo que cambia sin aviso.
- **Alcance de herramientas expuestas**: idéntico al de `edusync-agente-llm` — bajo `/api/v1/**`, excluyendo `/api/v1/auth/**`, `/api/v1/plataforma/**`, `/api/v1/ai/**`; solo `GET`, o `POST` cuyo path contenga una palabra de consulta explícita (`consultar`, `buscar`, `obtener`, `listar`). **Ninguna escritura** se expone como herramienta sin un ADR de seguimiento dedicado — esto es lo que impide hoy que el modelo dispare un `PUT /calificaciones` por su cuenta.
- **Contrato nuevo, no se toca el v0**: `POST /api/v1/ai/agente` (JWT obligatorio), devuelve `respuesta` + `herramientasUsadas` + `turnos` + `camino` + `fuente`, igual forma que ya reporta Python. `POST /api/v1/ai/chat` y `POST /api/v1/ai/consultar-usuario` no cambian.
- **Fuera de alcance de este ADR** (deuda técnica declarada, no olvido): camino `KEYWORD` y resolutores compuestos de Python (optimización de costo/latencia, se puede portar después sin romper nada de lo decidido aquí); servidor/cliente MCP (Spring AI trae starters de MCP que `PR-IMPL-022` evitó a propósito; se revisita en un ADR propio si hace falta paridad con `MCP_HABILITADO` de Python); cualquier herramienta de escritura.

### 4. Consecuencias

#### 4.1 Positivas

- Paridad funcional entre el backend Java y el agente de referencia validado en Python, pero **dentro** del artefacto desplegable único (consistente con `ADR-0011`).
- El agente respeta el RBAC real del usuario (mejora de seguridad sobre la referencia Python, que usa una cuenta admin fija).
- Reutiliza Spring AI ya adoptado (`ADR-0017`) sin ampliar el radio de esa decisión ni reabrir su gate de BOM.
- `LlmPort`, `ChatConLlmService`, `ChatConLlmServiceTest` y `POST /api/v1/ai/chat` permanecen intactos — el riesgo de regresión del chat existente queda acotado a código nuevo.

#### 4.2 Negativas / costos

- Superficie nueva de ataque: un endpoint que deja que un modelo decida qué leer del sistema. Mitigado por: filtro de solo-lectura, propagación del JWT del usuario (nunca más privilegio del que ya tiene), y exclusión explícita de `/auth/**`/`/plataforma/**`/`/ai/**`.
- Duplicación de lógica: el filtro de seguridad de descubrimiento existe ahora en dos lenguajes (Python y Java). Si se agrega un endpoint nuevo con una regla de exclusión distinta en un solo lado, divergen. Mitigación: test de regresión en Java que falla si alguna herramienta descubierta apunta a un path de escritura o a un prefijo excluido, corriendo en cada build.
- Latencia adicional: cada herramienta ejecutada es una llamada HTTP loopback (localhost) — aceptable para un monolito de un solo proceso, pero es overhead real frente a invocar un puerto de aplicación directo (Alternativa D, descartada por otras razones).

#### 4.3 Neutras / observables

- No cambia RBAC de `identidad` (`ADR-0010`) — el agente hereda el RBAC existente en vez de crear uno nuevo.
- `shared` sigue `OPEN` (`ADR-0011`); `ModularityTests` debe permanecer 7/7.
- `docs/product/DTP.md` §B §9 pasa de "cliente Spring AI detrás de `LlmPort`" (delta de `ADR-0017`) a "+ agente de tool calling sobre el mismo cliente" (delta de este ADR).

### 5. Impacto en el sistema

- **Código**: paquetes nuevos bajo `com.edusync.shared.ai.{domain,application,infrastructure}` (detalle completo en `DD-UC-023` §2); `AiConfig`/`AiProperties` reciben propiedades nuevas (`agente.*`); `AiChatController` no se toca, se agrega `AgenteController`.
- **Operaciones**: sin servicio cloud nuevo; mismo Ollama/Open WebUI local. Sin cambio de infraestructura AWS.
- **Seguridad**: nueva superficie (§4.2), mitigada; NFR-007 se extiende explícitamente a argumentos y resultados de herramientas, no solo al prompt.
- **Equipo**: familiarización con `ToolCallback`/tool calling de Spring AI 2.0.0.
- **Costo**: cero en factura AWS (Ollama local); sin dependencias nuevas en `pom.xml` (Spring AI ya está desde `ADR-0017`).

### 6. Plan de reversión

- Señales tempranas de decisión incorrecta: (1) el bucle manual resulta significativamente más complejo de mantener que dejar la ejecución interna de Spring AI, sin que la auditabilidad perdida se note en la práctica; (2) el salto HTTP loopback introduce latencia o fragilidad inaceptable; (3) el catálogo de herramientas descubierto diverge del de Python de forma recurrente pese al test de regresión.
- Costo estimado de revertir: bajo-medio — el endpoint nuevo (`POST /api/v1/ai/agente`) y el puerto nuevo (`AgenteLlmPort`) se pueden retirar sin tocar `LlmPort` ni el chat v0, que nunca se modificaron.
- Plan B: si el bucle manual no compensa, migrar a Alternativa B (ejecución interna de Spring AI) conservando el mismo contrato de endpoint, aceptando reportar `camino`/`turnos` de forma aproximada en vez de exacta.

### 7. Validación

- `mvn test` en verde, incluyendo `ModularityTests` 7/7 y los tests nuevos de `EjecutarConsultaAgenteService`.
- Test de regresión: ninguna herramienta descubierta apunta a `/api/v1/auth/**`, `/api/v1/plataforma/**`, `/api/v1/ai/**`, ni a un método de escritura sin palabra de consulta en el path.
- Grep de logs del agente: sin prompt completo, sin respuesta completa, sin argumentos/resultados de herramientas con datos de usuario, sin JWT.
- Paridad manual: correr contra el agente Java las mismas 4 preguntas que `edusync-agente-llm` ya documentó como escenarios de referencia y comparar `camino`/herramienta usada.

### 8. Referencias

- `EduSync_LLM/edusync-agente-llm/README.md` e `INFORME_EDUSYNC_AGENTE_LLM.md` (agente de referencia, catálogo de 23 herramientas y reglas de filtrado).
- `EduSync_LLM/Lab3` (Día 4, tool calling básico + auditoría) y `EduSync_LLM/Lab5` (Día 6, bucle ReAct genérico, `agente.py`).
- `docs/adr/0011-*.md` (Spring Modulith, `shared` OPEN), `docs/adr/0017-*.md` (Spring AI detrás de `LlmPort`).
- `docs/design/DD-UC-022.md` / `docs/prompts/impl/PR-IMPL-022.md` (precedente directo del patrón ADR→DD→PR-IMPL para `shared.ai`).
- `AGENTS.md` §5 (hexágono), §7 (NFR-007, PII).
- `docs/ANALISIS_TOOL_CALLING_JAVA.md` (análisis previo que originó este ADR).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 13/09/2026 | Rodrigo Aspeti | Propuesta inicial: tool calling multipaso sobre Spring AI (Alternativa C), puerto nuevo sin tocar `LlmPort`, propagación de JWT de usuario en vez de cuenta técnica, modelo del agente fijado por versión. Pendiente de aprobación humana antes de habilitar `PR-IMPL-023`. |
