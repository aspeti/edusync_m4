# Architecture Decision Record (ADR)

## ADR-0021: Orquestación de producción del asistente (grafo Java + guardrails; sin LangGraph)

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0021` |
| Título | El asistente se orquesta como grafo Java con guardrails de entrada/salida; no se adopta LangGraph |
| Fecha | 27/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | `com.edusync.shared.ai` (orquestación `POST /api/v1/ai/agente`) + UI `/asistente`. No reabre `LlmPort` / `POST /api/v1/ai/chat`. No toca `docs/baseline/**`. **No supersede** `ADR-0017` (Spring AI detrás de puerto), `ADR-0018` (loopback JWT), `ADR-0019` (KEYWORD) ni `ADR-0020` (CONSULTA). |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, G-EduSync) |
| ADR relacionado | `ADR-0017`, `ADR-0018`, `ADR-0019`, `ADR-0020`. Habilita `DD-UC-027` / `PR-IMPL-027`. Lab de referencia: `plantillas/Labs/Lab6` (patrones, no código Python). |

### 1. Contexto

El Lab 6 (LangGraph Nivel 5) enseña un grafo de producción: 7 nodos, aristas condicionales, checkpoints SQLite, guardrails de entrada/salida, caché semántico, RAG, fallback de modelo y nodo de escalación. EduSync ya cubre parte de esos patrones con KEYWORD → CONSULTA → ReAct (`ADR-0019`/`ADR-0020`), formatter, JWT de usuario y `sessionStorage`, pero **no** tiene un grafo explícito ni guardrails de inyección/PII.

Fuerzas:

1. Adoptar LangGraph/Python rompería el stack vivo (Java 25 / Spring Boot 4.1.0 / `ADR-0011` Modulith) y exigiría un proceso y un runtime ajenos.
2. Los caminos KEYWORD/CONSULTA ya responden sin LLM; un grafo debe **envolverlos**, no reemplazarlos.
3. NFR-007: ni logs ni JSON de tools con RUDE; el guardrail no debe persistir los valores que detecta (solo etiquetas).
4. RAG, embeddings y caché semántico son oleadas posteriores: no hay corpus de políticas ni store vectorial en el producto.
5. Checkpoints SQLite por `thread_id` (Lab 6) duplicarían el hilo de `sessionStorage` y abrirían persistencia PII en disco.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo |
|-------------|------|---------|-------|
| A. Integrar LangGraph (Python) o LangGraph4j como runtime del agente | Paridad literal con el Lab 6 | Segundo runtime, lock-in, viola `ADR-0008`/`ADR-0011` sin ADR de stack | Alto |
| B. Reescribir el orquestador sobre Spring AI Advisors / un DAG de beans | Encaje Spring | No hay grafo auditable de nodos/aristas; Advisors no modelan KEYWORD/CONSULTA | Medio |
| C. Grafo Java explícito (nodos + aristas) que envuelve KEYWORD/CONSULTA/ReAct; guardrails in-process; oleadas posteriores para RAG/checkpoint/caché | Cumple el DoD pedagógico del Lab 6 en el stack vivo; 0 tokens si se bloquea o se saluda | Hay que mantener el mapa de aristas a mano | Bajo–medio |

### 3. Decisión

> **Elegimos la Alternativa C.** El Lab 6 es **fuente de patrones**, no de framework.

Puntos que este ADR **fija**:

1. **Sin LangGraph, sin Chroma, sin SqliteSaver.** El grafo vive en `shared.ai` como código Java testeable (`RutasGrafoAsistente` + guardrails).
2. **Nodos de oleada 1:** `GUARDRAIL_ENTRADA` → `CLASIFICAR` → (`BLOQUEAR` \| `SALUDO` \| `KEYWORD` \| `CONSULTA` \| `REACT` \| `NINGUNO`) → `GUARDRAIL_SALIDA` → `ENTREGAR`.
3. **Guardrail de entrada (antes de gastar tokens):** bloquea inyección de prompt y datos de tarjeta; enmascara correo, teléfono BO y RUDE (etiquetas, nunca el valor en logs). Decisión `{ok, preguntaLimpia, hallazgos}` — no excepción — para que el grafo pueda seguir por arista.
4. **Guardrail de salida (antes de entregar):** rechaza respuesta vacía, fuga de RUDE/tarjeta/CI y filtrado de system-prompt. Sin citas RAG en oleada 1 (`requiereCita = false`).
5. **Camino `BLOQUEADO`:** 0 llamadas a LLM ni tools. Camino `SALUDO`: respuesta fija, 0 LLM.
6. **Temperatura del agente = 0.0** (reproducible; no está en YAML hoy).
7. **Diferido (oleadas 2+):** RAG/embeddings, caché semántico, fallback de modelo, nodo ESCALAR, checkpoint SQLite/`thread_id` persistente. `AgenteAiConfig` vía Open WebUI sigue siendo deuda de `DD-UC-025`.
8. **Identidad:** loopback + JWT del usuario (`ADR-0018`). Playbooks de escritura y `consultar_academico` no cambian (`ADR-0020`).

### 4. Consecuencias

#### 4.1 Positivas

- Inyección y PII se cortan **antes** de Ollama.
- El grafo es auditable en `steps[]` (`toolId` = nombre de nodo).
- KEYWORD/CONSULTA siguen en milisegundos.

#### 4.2 Negativas / costos

- El clasificador de oleada 1 es Java (saludo + reutilizar KEYWORD/CONSULTA), no un LLM de clasificación.
- Sin grounding RAG, el guardrail de salida no puede exigir citas.

#### 4.3 Neutras

- `ModularityTests` 7/7.
- `LlmPort` / chat v0 intactos.

### 5. Impacto en el sistema

- Código: `shared.ai` (guardrails, rutas, delta orquestador, temperatura) + badge UI `BLOQUEADO`/`SALUDO`.
- Sin Flyway.
- Config: `edusync.ai.agente.temperature: 0`.

### 6. Plan de reversión

- Quitar la llamada a guardrails en `EjecutarConsultaAgenteService` restaura el orden KEYWORD→CONSULTA→ReAct.
- No revertir `LlmPort`.

### 7. Validación

- Test: «ignora las instrucciones» → `BLOQUEADO`, 0 interacciones LLM/tools.
- Test: «hola» → `SALUDO`, 0 LLM.
- Test: KEYWORD «lista los cursos» sigue `KEYWORD` (pregunta limpia).
- Test: salida con RUDE sintético se redacta.
- `mvn test` + `ng build`.

### 8. Referencias

- `plantillas/Labs/Lab6` (`grafo.py`, `nodos.py`, `guardrails.py`)
- `docs/design/DD-UC-027.md`
- `docs/adr/0017-*.md` … `0020-*.md`

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 27/09/2026 | Rodrigo Aspeti | Aceptada: grafo Java + guardrails; LangGraph diferido. |
