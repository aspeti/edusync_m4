# Architecture Decision Record (ADR)

## ADR-0019: Catálogo tipado, camino KEYWORD, respuesta formateada por código, escrituras con confirmación y LLM del agente vía Open WebUI

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0019` |
| Título | Evolución del asistente de `shared.ai`: catálogo gobernado, KEYWORD, anti-alucinación por formatter, tools `write` con confirmación, inferencia del agente solo por Open WebUI |
| Fecha | 26/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | `com.edusync.shared.ai` + consola Angular `/asistente`. No reabre `LlmPort` / `POST /api/v1/ai/chat`. No toca `docs/baseline/**`. **No supersede** a `ADR-0018` (se conserva loopback HTTP + JWT del usuario + bucle ReAct controlado). |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, G-EduSync) |
| ADR relacionado | `ADR-0017` (Spring AI detrás de `LlmPort`, chat v0); `ADR-0018` (agente ReAct); `ADR-0011` (Modulith). Habilita `DD-UC-025` / `PR-IMPL-025`. |

### 1. Contexto

`ADR-0018` dejó el asistente Java con OpenAPI dinámico, `camino=AGENTE` siempre, respuesta final del modelo, solo lectura, y `AgenteAiConfig` contra Ollama directo. Eso cierra el spike ReAct, pero no el producto que ahora se pide: gobernanza de tools (catálogo), atajo KEYWORD, texto de dominio escrito por código, mutaciones con confirmación humana, proxy Open WebUI, historial de sesión y traza distinguible (`KEYWORD`/`LLM`/`NINGUNO` + `steps` + tablas).

Fuerzas:

1. **Anti-alucinación** vs. menos código: si el modelo redacta sobre JSON de tools, puede inventar conteos.
2. **Costo/latencia** vs. flexibilidad: KEYWORD evita turnos en frases frecuentes; no cubre sinónimos.
3. **Modulith**: EduSync no puede importar `academico`/`identidad` desde `shared.ai` más que por REST público (`ADR-0018` C).
4. **NFR-007**: writes y previews no deben loguear RUDE/notas.
5. **Operación**: el chat v0 ya sabe hablar Open WebUI; el agente aún no.

RAG queda **fuera** (otro ADR/DD). MCP sigue fuera.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo |
|-------------|------|---------|-------|
| A. No cambiar `ADR-0018` | Cero riesgo | No cumple el pedido de producto | Bajo hoy, alto en objetivo |
| B. Use cases in-process + RAG + copilotos en este slice | Un solo proceso; más alcance | Rompe Modulith; RAG y copilotos no pedidos ahora | Alto |
| C. Catálogo tipado + KEYWORD + formatter + writes confirmados + Open WebUI; **conservar** loopback JWT; RAG/copilotos después | Cumple el pedido; no reabre hexágono | Hay que mantener catálogo markdown↔código | Medio |
| D. Solo formatter, sin KEYWORD ni writes | Mitiga alucinación | Sigue gastando LLM y no hay altas por chat | Bajo |

### 3. Decisión

> **Elegimos la Alternativa C.** Detalle en `DD-UC-025` y `docs/design/assistant/TOOL-CATALOG.md`.

Puntos que este ADR **fija**:

1. **Catálogo:** fuente de verdad `TOOL-CATALOG.md`; el LLM no recibe el dump OpenAPI. OpenAPI queda como linter de path/método.
2. **KEYWORD:** router de frases → tool, `camino=KEYWORD`, `turnos=0`. Si `edusync.ai.agente.llm-habilitado=false`, no hay camino LLM.
3. **Quién redacta:** `FormateadorRespuestaAgente` es la única fuente de `respuesta` de producto. El LLM solo elige tools (o señala fin de bucle).
4. **Writes:** solo tools listadas en el catálogo, con preview y `confirmed=true`. Calificaciones y auth/plataforma/ai siguen prohibidas. Esto **cierra** la deuda de “ninguna escritura” de `ADR-0018` §3 **solo** para la oleada 1 del catálogo.
5. **LLM del agente:** Open WebUI (cliente OpenAI-compatible de Spring AI, HTTP/1.1, API key solo en backend). Ollama directo deja de ser el transporte del agente. Chat v0 (`ADR-0017`) no cambia de proveedor por este ADR.
6. **Memoria:** `history[]` en el request; UI en `sessionStorage`; sin tabla de conversaciones.
7. **Traza:** `camino` ∈ {`KEYWORD`,`LLM`,`NINGUNO`}, `agente` (hoy `general`), `herramientasUsadas`, `steps[]` con `toolId` + `tablasFuente` + `exito`.
8. **Fuera:** RAG, MCP, copilotos por pantalla, streaming, persistencia BD.

`ADR-0018` permanece **Aceptada**: identidad JWT, loopback, bucle manual, no tocar `LlmPort`.

### 4. Consecuencias

#### 4.1 Positivas

- Preguntas frecuentes sin costo de modelo; demo de madurez KEYWORD vs LLM.
- Menos alucinación numérica/de listados.
- Writes auditables (preview + confirmación + `steps`).
- Un solo proxy LLM institucional para el agente.

#### 4.2 Negativas / costos

- El catálogo no aparece “gratis” al agregar un GET: hay que editar markdown + registry + KEYWORD + formatter.
- Open WebUI es dependencia operativa del asistente (API key, modelo publicado).
- Doble confirmación en chat es UX más lenta que un formulario.

#### 4.3 Neutras

- `ModularityTests` 7/7.
- NFR-007 se extiende a previews y `steps` (ids internos, no RUDE/notas).

### 5. Impacto en el sistema

- Código: delta `shared.ai` + `/asistente` (`PR-IMPL-025`).
- Config: `edusync.ai.agente.llm-habilitado`; agente usa `edusync.ai.open-webui.*`.
- Sin migración Flyway (historial no se persiste).

### 6. Plan de reversión

- KEYWORD/formatter/writes se pueden feature-flagear.
- Transporte Open WebUI se puede volver a Ollama cambiando `AgenteAiConfig` (el puerto `AgenteLlmPort` no cambia).
- No revertir `LlmPort`.

### 7. Validación

- Tests de router, formatter, confirmed=false no muta, catálogo sin paths prohibidos, linter OpenAPI.
- Grep de logs: sin JWT, RUDE, notas.
- `mvn test` + `ng build` en la ejecución de `PR-IMPL-025`.

### 8. Referencias

- `docs/design/DD-UC-025.md`, `docs/design/assistant/TOOL-CATALOG.md`
- `docs/adr/0018-tool-calling-agente-shared-ai.md`

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 26/09/2026 | Rodrigo Aspeti | Aceptada: delta de producto del asistente; RAG explícitamente fuera. |
| 2 | 26/09/2026 | Rodrigo Aspeti | Se eliminan referencias a un producto externo del ADR y de `DD-UC-025`. |
