# Architecture Decision Record (ADR)

## ADR-0020: Consultas académicas dinámicas del asistente (resolución de entidades + query parametrizable + camino CONSULTA)

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0020` |
| Título | El asistente resuelve nombres a IDs en backend y consulta notas/agregados con un comando parametrizable; no un Playbook por pregunta |
| Fecha | 27/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | `com.edusync.academico` (nuevos use cases de lectura) + `com.edusync.shared.ai` (camino CONSULTA, catálogo) + UI `/asistente`. No reabre `LlmPort` / `POST /api/v1/ai/chat`. No toca `docs/baseline/**`. **No supersede** `ADR-0018` (loopback JWT) ni `ADR-0019` (KEYWORD + formatter). |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, G-EduSync) |
| ADR relacionado | `ADR-0018`, `ADR-0019`, `ADR-0013` (`CalculoNotas`), `ADR-0011` (Modulith). Habilita `DD-UC-026` / `PR-IMPL-026`. |

### 1. Contexto

El asistente v1.1 (`ADR-0019`) cubre frases KEYWORD de listado y deja el resto a ReAct + `llama3.1:8b`. Eso falla en producto: (1) latencia de varios turnos en CPU; (2) el modelo no extrae `q=` ni inventa IDs; (3) no hay una API de «notas de X en Y en periodo Z»; (4) `TOOL-CATALOG` prohibía `/calificaciones` (NFR-007 / ADR-0018). El producto ahora exige preguntas combinadas (alumno × curso × materia × periodo × agregados) **sin** un playbook por combinación.

Fuerzas:

1. Anti-alucinación de IDs vs. flexibilidad del LLM.
2. Modulith: `shared.ai` no importa `academico`.
3. NFR-007: sin RUDE/notas en **logs**; el chat sí debe mostrar nombres y un número de promedio calculado en dominio.
4. Reusar `CalculoNotas` / nota provisional, no reimplementar `floor` ni round en el agente.
5. `llama3.1:8b` local no es fiable para encadenar 4 tools.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo |
|-------------|------|---------|-------|
| A. Un Playbook/herramienta por tipo de pregunta | Determinista | Explosión combinatoria | Alto y creciente |
| B. Solo ReAct sobre OpenAPI de calificaciones | Menos código nuevo | Lento, IDs inventados, viola exclusión de `/calificaciones` | Bajo código, alto riesgo |
| C. Use cases in-process desde `shared.ai` | Sin HTTP | Viola `ADR-0018` D / Modulith | Alto institucional |
| D. Resolver + `consultar_academico` parametrizable + camino CONSULTA (analizador Java) + ReAct de respaldo sobre el catálogo tipado | Cubre ejemplos sin LLM; IDs reales; un comando para N filtros; loopback intacto | Hay que mantener el analizador de frases | Medio |

### 3. Decisión

> **Elegimos la Alternativa D.** Detalle en `docs/ai/conversational-assistant-design.md` y `DD-UC-026`.

Puntos que este ADR **fija**:

1. **Resolución de entidades en backend.** El modelo (o el analizador) solo envía texto (`q`). Los UUIDs salen de `find_*`. Ambigüedad → aclaración, nunca un match arbitrario.
2. **Una consulta parametrizable** (`POST /api/v1/consultas-academicas/consultar`) con `operacion` y filtros UUID opcionales. Agregados (`PROMEDIO`, `REPROBADOS`, `TOP`) se calculan con `CalculoNotas` / nota provisional. Umbral de reprobación de la consulta (default 51) **no** es una regla de negocio nueva del BRD.
3. **Camino `CONSULTA`:** analizador de intención en Java, 0 turnos de LLM, mismas tools HTTP que ReAct. KEYWORD de listado se evalúa **antes**. ReAct usa el catálogo **tipado** (cierra la deuda de `EjecutarConsultaAgenteService` contra OpenAPI).
4. **Playbooks** no se usan para combinaciones de lectura académica.
5. **NFR-007:** no se exponen paths `/calificaciones` ni `rude` al modelo. El BFF de lectura puede devolver nombre + valor agregado. Logs sin PII.
6. **Contexto conversacional:** `history[]` + `contexto` en el request; `sessionStorage` en UI; sin persistencia BD.
7. **Identidad:** loopback + JWT del usuario (`ADR-0018`).

### 4. Consecuencias

#### 4.1 Positivas

- Preguntas de ejemplo responden en milisegundos si el analizador matchea.
- IDs reales; aclaración si hay dos Juan.
- Un endpoint en vez de N métodos hardcodeados.

#### 4.2 Negativas / costos

- El analizador no cubre todas las paráfrasis → cae a ReAct (sigue siendo lento en CPU).
- Hay que mantener alias de «primer trimestre» vs. nombre seed `Trimestre 1`.

#### 4.3 Neutras

- `ModularityTests` debe permanecer 7/7.
- `CalculoNotas` no cambia (sigue sin `floor()` en el modelo genérico).

### 5. Impacto en el sistema

- Código: `academico` (resolver + consultar) + `shared.ai` + `/asistente`.
- Sin migración Flyway.
- Catálogo `TOOL-CATALOG.md` v1.3.

### 6. Plan de reversión

- Feature-flag implícito: si se quitan las tools nuevas del catálogo, KEYWORD de listado sigue.
- No revertir `LlmPort`.

### 7. Validación

- Tests del analizador (casos 1–6 del pedido).
- Resolver: único / ambiguo / ninguno; acentos.
- Consultar: IDs de otro tenant no devuelven filas.
- Catálogo: `consultar_academico` no apunta a `/calificaciones`.
- `mvn test` + `ng build`.

### 8. Referencias

- `docs/ai/conversational-assistant-design.md`
- `docs/design/DD-UC-026.md`
- `docs/adr/0018-tool-calling-agente-shared-ai.md`, `docs/adr/0019-*.md`

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 27/09/2026 | Rodrigo Aspeti | Aceptada: consultas dinámicas del asistente. |
