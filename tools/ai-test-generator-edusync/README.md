# Generador de tests UNIT / INTEGRATION con IA — EduSync

Herramienta simple: **lee los tests del proyecto**, lista lo que ya existe y **solo agrega escenarios faltantes** (sin duplicar métodos `@Test`).

## Uso

```powershell
cd tools\ai-test-generator-edusync

# --- UNIT (default) ---
java GenerarTest.java --clase backend/src/main/java/.../CrearEstudianteService.java
java GenerarTest.java --tipo unit --clase ... --escribir

# --- INTEGRATION ---
java GenerarTest.java --tipo integration `
  --clase backend/src/main/java/com/edusync/academico/infrastructure/adapter/in/rest/EstudianteController.java

java GenerarTest.java --tipo integration --clase ...\EstudianteController.java `
  --tarea "- rechazo HTTP 401 sin JWT" --escribir
```

También acepta un `*Service` como `--clase` en integration: resuelve `EstudianteIntegrationTest` y busca el `*Controller` automáticamente.

## Qué hace

| | Unit | Integration |
|--|------|-------------|
| Salida | `.../FooServiceTest.java` | `com/edusync/{modulo}/FooIntegrationTest.java` |
| Stack | JUnit + Mockito + AssertJ | `@SpringBootTest` + Testcontainers PG 15 + TestRestTemplate |
| Anti-duplicado | Por nombre `@Test` | Igual; fusiona en el IT existente |
| Ejecutar | `mvn -Dtest=...` | Igual (**requiere Docker**) |

`--escribir` = confirmación humana. La IA **no aprueba**.

## Proveedores LLM

| `EDUSYNC_AI_PROVIDER` | Variables |
|----------------------|-----------|
| `ollama` (default) | `OLLAMA_*` |
| `open-webui` | `OPEN_WEBUI_*` |
| `openai` | `OPENAI_API_KEY`, `OPENAI_MODEL`, ... |
| `gemini` | `GEMINI_API_KEY`, `GEMINI_MODEL` (default `gemini-3.8-flash`), ... |

```powershell
$env:EDUSYNC_AI_PROVIDER="ollama"
$env:OLLAMA_MODEL="llama3.1:8b"

# Gemini (Google AI Studio → API key)
$env:EDUSYNC_AI_PROVIDER="gemini"
$env:GEMINI_API_KEY="AIza..."
$env:GEMINI_MODEL="gemini-3.8-flash"
```

Gemini usa el endpoint OpenAI-compat de Google (`.../v1beta/openai/chat/completions`); no hace falta otro cliente HTTP.
