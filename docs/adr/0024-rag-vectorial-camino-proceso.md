# Architecture Decision Record (ADR)

## ADR-0024: RAG vectorial del camino PROCESO, índice en memoria

### Metadatos

| Campo | Valor |
|-------|-------|
| Numero | `0024` |
| Titulo | Embeddings del proveedor ya configurado para recuperar procesos, sin Chroma ni pgvector |
| Fecha | 02/10/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aceptada** |
| Alcance | Recuperación del nodo `PROCESO` en `shared.ai`. No cambia `KEYWORD`, `CONSULTA`, el contrato `POST /api/v1/ai/agente` ni `docs/baseline/**`. No supersede a `ADR-0021` ni a `ADR-0023`. Completa el RAG que `ADR-0021` §7 dejó diferido, con el límite de no añadir un almacén vectorial. |

### 1. Contexto

`DD-UC-028` responde «cómo funciona el proceso» con solapamiento de tokens sobre seis markdown de `ai/base_conocimiento/`. El grafo solo entra a ese camino si la pregunta casa con una expresión regular (`cómo`, `qué es`, `floor`, `cálculo de notas`, `SIE`, `RUDE`). Si casa, devuelve hasta dos párrafos citados y cero turnos de LLM.

Eso falla de dos maneras medidas el 02/10/2026 sobre las 12 preguntas de `tools/evals-offline`:

- «¿Cuándo se puede cargar una nota?» y «¿Qué pasa si una sección no tiene notas?» no entran al recuperador. Siguen al LLM. Dos de esas llamadas devolvieron HTTP 500.
- Cuando sí entra («¿Cómo se redondea el promedio de sección?»), el overlap devuelve también el párrafo de alcance. La respuesta cita el corpus, pero mezcla fragmentos que la pregunta no pedía.

`ADR-0021` difirió embeddings, Chroma y caché semántico porque no había corpus ni store. El corpus ya existe. Chroma y pgvector siguen sin estar en el stack de `AGENTS.md` §4.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras |
|-------------|------|---------|
| A. Dejar el overlap de tokens | Cero red, cero costo, determinista | No recupera paráfrasis. El regex deja fuera preguntas de proceso reales |
| B. Embeddings y luego el LLM redacta la respuesta | Texto más corto y natural | Puede inventar una cifra, un `floor` o una aprobación. Gasta un turno de chat. La compuerta offline castiga la oración que no está en el contexto |
| C. Chroma o pgvector | Índice persistente y escalable | Dependencia o extensión fuera del stack. El corpus son seis archivos de procesos, no datos por tenant |
| D. Embeddings del proveedor ya elegido (Spring AI 2.0.0), índice en memoria, respuesta = fragmentos citados | Misma frontera que `ADR-0017`/`ADR-0023`. Recupera paráfrasis. Sigue sin redactar. Si el embedding falla, queda el recuperador léxico | Con Gemini, el texto de la pregunta sale de la máquina una vez por consulta. El umbral hay que calibrarlo con un puerto falso en el test, no copiarlo de la compuerta offline |

### 3. Decision

Alternativa D.

- El nodo sigue llamándose `PROCESO`. La respuesta sigue el formato de `DD-UC-028`: «Según los procesos de EduSync», párrafos citados y `Fuente: archivo.md`. Cero turnos de chat.
- La similitud es coseno entre el embedding de la pregunta y el de cada párrafo del corpus. Se entregan como máximo 2 párrafos que superen el umbral. Un párrafo por debajo del umbral no se añade.
- El umbral vive en `edusync.ai.proceso.umbral-similitud` (`EDUSYNC_AI_PROCESO_UMBRAL`). No se fija en código. El valor inicial lo elige el test de calibración de `PR-IMPL-030`, con un `EmbeddingPort` falso.
- El modelo de embedding vive en `edusync.ai.embedding.model` (`EDUSYNC_AI_EMBEDDING_MODEL`). El adaptador usa el `EmbeddingModel` de Spring AI del mismo proveedor que el agente (Gemini por el endpoint compatible con OpenAI, u Ollama). El id concreto se verifica contra el proveedor al implementar; no se inventa en el dominio.
- `domain/` y `application/` no importan Spring AI. El puerto es `EmbeddingPort`.
- Si el embedding del corpus o de la pregunta falla, el camino usa el recuperador léxico de `DD-UC-028`. Un fallo del proveedor no se convierte en HTTP 500 ni en una respuesta vacía.
- Si el embedding responde y ningún párrafo supera el umbral, el grafo no vuelve al léxico: sigue a saludo o al LLM. El léxico solo cubre la caída técnica.
- `KEYWORD` y `CONSULTA` se evalúan antes. Esas preguntas no se embeben.
- No se embeben notas, RUDE, resultados de herramientas ni el historial. El corpus es el classpath institucional. El guardrail de entrada sigue corriendo antes de este nodo.
- No se loguea la pregunta ni el vector en INFO.

### 4. Consecuencias

#### 4.1 Positivas

Una paráfrasis («qué pasa si una sección no tiene notas», «cuándo se puede cargar una nota») puede citar el párrafo correcto sin pasar por el LLM. El segundo párrafo débil deja de colarse solo porque comparte tokens.

#### 4.2 Negativas / costos

Con `provider=gemini`, la pregunta y, al arranque, los párrafos del corpus se envían al endpoint de embeddings. Hay costo y una salida de datos que el camino léxico no tenía. El índice en memoria se reconstruye en cada arranque.

#### 4.3 Neutras / observables

`camino` sigue siendo `PROCESO` y `fuente` sigue siendo el corpus. La compuerta offline `python evals/evaluar.py v3` no cambia: mide la función grabada, no este nodo.

### 5. Impacto en el sistema

`EjecutarConsultaAgenteService`, un puerto `EmbeddingPort`, un adaptador Spring AI y el índice en memoria junto a `RecuperadorProcesosEdusync`. `application.yml` gana el modelo y el umbral. Sin migración Flyway. Sin cambio de contrato REST. Sin UI.

### 6. Plan de reversion

Dejar el servicio de aplicación llamando solo a `RecuperadorProcesosEdusync` y no crear el bean de embeddings. El corpus y el formato de cita no se tocan.

### 7. Validacion

Tests con `EmbeddingPort` falso, sin red: una paráfrasis recupera el párrafo esperado; «lista los cursos» no llama al puerto; un fallo del puerto cae al léxico; una pregunta fuera de alcance, por debajo del umbral, no toma `PROCESO`. `mvn test` incluye `ModularityTests`. `domain/` y `application/` no importan Spring AI.

### 8. Referencias

- `ADR-0017`, `ADR-0021`, `ADR-0023`
- `docs/design/DD-UC-028.md`, `docs/design/DD-UC-030.md`
- `docs/prompts/impl/PR-IMPL-030.md`
- Corpus: `docs/ai/base_conocimiento/` y `backend/src/main/resources/ai/base_conocimiento/`
