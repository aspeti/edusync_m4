# PR-IMPL-030 — RAG vectorial del camino PROCESO

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-030` |
| Título | Embeddings en memoria para el camino PROCESO |
| Artefacto origen | `docs/design/DD-UC-030.md`, `docs/adr/0024-rag-vectorial-camino-proceso.md` |
| ID origen | `DD-UC-030`, `ADR-0024`, `FSD-UC-016`, `NFR-007` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 02/10/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aprobado (prompt)** |

> No ejecutar este prompt como parte del diseño. La implementación es un turno aparte.

## 1. Task

Sustituir el ranking de `PROCESO` por coseno de embeddings, citando los párrafos, según `docs/design/DD-UC-030.md` §2 y `ADR-0024`.

## 2. Stop condition

`mvn test` en verde, incluidos `ModularityTests` y los casos del §6 del design doc. Ningún test llama a Gemini ni a Ollama. `domain/` y `application/` no importan Spring AI.

## 3. Anatomía

### Role

Eres el servicio de aplicación del asistente EduSync (Java 25, Spring Boot 4.1.0, hexagonal). Recuperas procesos institucionales por similitud y citas el corpus.

### Task

Implementa `EmbeddingPort`, el índice en memoria y el adaptador Spring AI del proveedor ya configurado. Engancha `EjecutarConsultaAgenteService` como dice el diagrama de `DD-UC-030` §2.1. El léxico de `DD-UC-028` queda solo como respaldo si el embedding falla.

### Context

- Documentos: `DD-UC-030`, `ADR-0024`, `ADR-0017`, `ADR-0023`, `DD-UC-028`.
- Stack: Java 25, Spring Boot 4.1.0, Spring AI 2.0.0 ya en el BOM. Sin dependencia nueva.
- Restricciones: sin Chroma, sin pgvector, sin que el LLM redacte la cita, sin PII en logs, sin secretos en el repo, sin editar `docs/baseline/**`.
- El id del modelo de embedding se verifica contra el proveedor y se documenta en `application.yml` y `.env.example`. No se commitea `.env`.
- El umbral numérico sale del test de calibración con el puerto falso. No se copia 0,85 de la compuerta offline.

### Reasoning

1. Confirmar que `KEYWORD` y `CONSULTA` siguen primero y no embeben.
2. Indexar los mismos párrafos que el recuperador léxico.
3. Filtrar por umbral y cortar en 2.
4. Si `embed` falla, delegar al léxico. Si responde y nadie supera el umbral, no hay `PROCESO`.
5. Tests del §6 antes de dar el slice por cerrado.

### Stop condition

La de la sección 2 de este archivo.

### Output

Código en `com.edusync.shared.ai`, propiedades, `.env.example` comentado, tests JUnit 5. Al cerrar, `dtp-sync` sobre `docs/product/DTP.md`.

### Invariants

- La respuesta de `PROCESO` sigue siendo cita más `Fuente:`. Cero turnos de chat en ese camino.
- No se embebe historial, notas, RUDE ni salida de tools.
- `KEYWORD` no llama a `EmbeddingPort`.
- Sin edición de `docs/baseline/**`.

### Failure modes

- `E_LLM_REDACTA_CORPUS`: el camino PROCESO pasa los fragmentos al chat. Rechazar.
- `E_SPRING_AI_EN_APPLICATION`: un import de Spring AI fuera de infrastructure. Revertir.
- `E_DEPENDENCIA_NUEVA`: Chroma, pgvector u otro cliente de embeddings fuera del BOM. Rechazar.
- `E_FALLBACK_OMITIDO`: un embedding caído produce HTTP 500 o respuesta vacía. Corregir: usar el léxico.
- `E_PII_EN_LOG`: la pregunta o el vector quedan en INFO. Quitar el log.
- `E_BASELINE_TOCADO`: revertir.
