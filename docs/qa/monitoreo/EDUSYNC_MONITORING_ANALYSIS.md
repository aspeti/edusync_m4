# Análisis — monitoreo online del asistente EduSync

Fecha: 01/10/2026. Fase de descubrimiento, antes de implementar. El laboratorio de referencia es `plantillas/Labs/monitoreo-lab/`. No se copia su dominio (reembolsos, pedidos, Chroma) ni sus umbrales.

## Arquitectura relevante que ya existe

| Pieza | Dónde está | Qué hace hoy |
|---|---|---|
| Asistente | `POST /api/v1/ai/agente` (`AgenteController` → `EjecutarConsultaAgenteService`) | Una pregunta, un JWT de sesión, una respuesta |
| Enrutado | `RutasGrafoAsistente` | `BLOQUEADO`, `KEYWORD`, `CONSULTA`, `PROCESO`, `SALUDO`, `LLM`, `NINGUNO` |
| Guardia de entrada | `GuardrailEntradaAgente` | Corta inyección, tarjeta, y limpia PII de la pregunta |
| Guardia de salida | `GuardrailSalidaAgente` | Redacta RUDE, CI, tarjeta y frases de system prompt |
| Alcance de tools | `PoliticaAlcanceAgente` (ADR-0022) | Quita `schoolId`/`tenantId`. `list_usuarios` solo para ADMIN, SECRETARIA, SYSADMIN |
| RAG | `RecuperadorProcesosEdusync` | Léxico, archivos en `backend/src/main/resources/ai/base_conocimiento/`. No hay Chroma |
| LLM | Spring AI detrás de `LlmPort` / `AgenteLlmPort` | Default Gemini (`ADR-0023`). Ollama sigue disponible. El puerto no devuelve uso de tokens |
| Dataset dorado offline | `tools/red-team-agent/catalog/golden.json` | Propiedades (`secret is absent`, `tenant is correct`), no el párrafo esperado |
| Red team | `tools/red-team-agent/` + `RedTeamAssistantCatalogWebMvcTest` | Compuerta offline del catálogo 1.1.0. CI: `python cli.py sync --check` y luego `mvn verify` |
| Hallazgos de producto | `docs/HALLAZGOS_AI_SEC_PRODUCTO.md` | AI-SEC-007 y AI-SEC-008, ya mitigados en Java |
| CI | `.github/workflows/ci.yml` | GitHub Actions. No hay Jenkins en el producto |
| Feedback de usuario | No existe | No hay pulgar ni endpoint de valoración |
| Log de interacciones | No existe como JSONL | SLF4J en debug, sin pregunta ni respuesta (NFR-007) |

No hay un `evals-lab` dentro del repo. La compuerta offline del producto es el red team más `mvn verify`, no un juez de fidelidad con dataset de párrafos.

## Cómo funciona el laboratorio

`monitor.py` lee un JSONL (una interacción por línea) y ofrece `resumen`, `tiempo`, `alertas`, `casos-nuevos`.

- Métricas sin respuesta de referencia: fidelidad léxica contra los markdown recuperados, frases prohibidas, feedback, fuera de alcance, p95 de latencia del camino LLM, tokens, ráfaga de bloqueos por usuario en una ventana.
- Umbrales en `umbrales_monitoreo.json`, versionados.
- Severidades del lab: CRÍTICA, ALTA, MEDIA. Cualquier alerta termina con código 1.
- `casos-nuevos` escribe candidatos. Una persona tiene que escribir la referencia. No hay alta automática al dataset.
- Tráfico sintético día 1 (sano) y día 2 (v2 mala, tema nuevo, ráfaga de un usuario).
- CI de ejemplo: GitHub Actions y un Jenkinsfile opcional. El job de ejemplo usa el día 2 a propósito, así que queda en rojo.

## Diferencias y adaptación

| Laboratorio | EduSync | Adaptación | Razón |
|---|---|---|---|
| Python `monitor.py` | Backend Java 25 / Spring Boot | Motor y CLI en Java (`MonitorAsistenteCli`). Sin dependencia nueva | El producto no debe ganar un runtime paralelo solo porque el lab es Python |
| Chroma / markdown de soporte | Corpus léxico de procesos + tools HTTP | La fidelidad usa el campo `contexto` del evento (texto ya recuperado). No se crea otro RAG | El retriever actual no es un vector store |
| Caminos `LLM`, `KEYWORD`, `FUERA_DE_ALCANCE`, `BLOQUEADO` | `KEYWORD`, `CONSULTA`, `PROCESO`, `SALUDO`, `LLM`, `NINGUNO`, `BLOQUEADO` | El monitor entiende los caminos reales. `FUERA_DE_ALCANCE` y `NINGUNO` cuentan como fuera de alcance | No se cambia el enrutador para imitar el lab |
| Fidelidad solo en `LLM` | Respuestas de proceso también citan corpus | Se puntúa `LLM` y `PROCESO` | Ahí hay texto generado o citado. `KEYWORD` y `CONSULTA` leen el API, no un párrafo del modelo |
| Frases de reembolso y un canary de comercio | Guardrails ya definidos (system prompt, RUDE) y promesas académicas que el sistema no puede cumplir | Lista propia en el JSON de umbrales | Copiar «reembolso aprobado» no describe a EduSync |
| `guard` truthy = bloqueo | Hace falta distinguir permitido de bloqueado | Solo `bloqueado`, `rechazado` y `redactado` cuentan | La cadena `permitido` no puede disparar la alerta de abuso |
| Código 1 si hay cualquier alerta | El lab mezcla tema nuevo (producto) con fuga (seguridad) | Código 1 solo con `CRITICAL` o `HIGH`. `MEDIUM` se imprime y no rompe el pipeline | Una ola de «¿qué clima hace?» no es un fallo de despliegue |
| Tokens siempre presentes | El puerto LLM no informa tokens | `tokens` null = desconocido. No se estiman ni se alerta por costo | Inventar tokens falsearía la métrica |
| Dataset dorado de párrafos `debe_contener` | `golden.json` de propiedades | Los casos aceptados van a `tools/monitor-asistente/dataset/golden-online.json`. No se reescribe `golden.json` ni `attacks.json` | El red team ya tiene compuerta. El monitor propone; una persona acepta |
| Jenkins | GitHub Actions ya corre `mvn verify` | Los tests del monitor entran en ese job. No se agrega Jenkins ni un cron que deje `master` en rojo | El día 2 es una demo, no el tráfico de cada push |

## Dónde se engancha la instrumentación

Un solo punto: `AgenteController.consultar`, después de obtener la `RespuestaAgente`. Ahí ya están el camino, las tools, la traza y la latencia medida en el controlador. El servicio de orquestación no gana un parámetro nuevo: los tests que lo construyen a mano siguen igual.

El registro queda apagado salvo `EDUSYNC_AI_MONITOR_REGISTRO=true`. Apagado, el bean es un no-op.

## Privacidad (decisión)

Prohibido en la bitácora: JWT, contraseñas, API keys, correos como identificador.

El usuario se guarda como `U-` más un prefijo SHA-256 del claim `sub`. Si no hay `sub`, `anonimo`.

Dos modos de texto:

- `minimizado` (default): no se escribe la pregunta ni la respuesta, solo su hash, el camino, el guard, la versión, la latencia y las fuentes (nombres de archivo o tablas, que ya son los de la traza). Esas líneas no entran en fidelidad ni en frases prohibidas, porque no hay texto que juzgar.
- `laboratorio`: texto con RUDE, CI, tarjeta y correo ya sustituidos. Solo para la demo local y el JSONL sintético del repo.

Los archivos `trafico_dia1.jsonl` y `trafico_dia2.jsonl` son sintéticos. No hay estudiantes reales.
