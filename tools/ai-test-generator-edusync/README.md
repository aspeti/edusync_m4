# Generador de Pruebas Unitarias con IA — EduSync (Fase 1 HITL)

CLI en Java puro (`GenerarTest.java`) para generar **unit tests** del backend con el LLM
ya configurado en el repo (Ollama / Open WebUI, mismas vars que `shared.ai` / ADR-0017).

Principio: **AI generates, human audits.** La herramienta no aprueba tests.

## Flujo Human-in-the-Loop

```text
--proponer  →  revisión humana del alcance  →  --generar --aprobar-alcance
        →  --ejecutar-tests  →  auditoría 3 preguntas  →  --decidir APPROVE|REJECT|MODIFIED
```

| Paso | Comando | ¿Llama LLM? | ¿Escribe `*Test.java`? |
|------|---------|-------------|-------------------------|
| 1. Proponer | default / `--proponer` | No | No (solo sesión JSON) |
| 2. Generar | `--generar --aprobar-alcance --sesion <id>` | Sí (`temperature=0`) | Sí (`@Tag("agente")`) |
| 3. Ejecutar | `--ejecutar-tests --sesion <id>` | No | No |
| 4. Decidir | `--decidir --sesion <id> --veredicto ...` | No | No |

Sesiones: `docs/qa/ai-test-sessions/<id>.json`.

## Requisitos

- JDK 25 (o al menos el JDK del backend).
- Ejecución directa: `java GenerarTest.java` (JEP 330).
- `.env` en la raíz del repo (`EDUSYNC_AI_PROVIDER`, `OLLAMA_*` o `OPEN_WEBUI_*`) solo para `--generar`.
- Maven en PATH para `--ejecutar-tests`.
- Al menos un `--test-manual` de referencia (`E_SIN_TEST_MANUAL`).

## Argumentos

| Flag | Descripción |
|------|-------------|
| `--clase` | Clase bajo prueba (ruta desde la raíz del repo) |
| `--salida` | Destino del `*Test.java` |
| `--contexto` | Archivos de soporte (repetible) |
| `--test-manual` | Tests existentes a no duplicar (repetible, ≥1) |
| `--tarea` | Prompt del desarrollador (texto o ruta `.md`) |
| `--proponer` | Dry-run (default si no hay otro modo) |
| `--generar` | Generar código (exige `--aprobar-alcance`) |
| `--aprobar-alcance` | Confirmación humana del alcance propuesto |
| `--sesion` | Id de sesión HITL |
| `--ejecutar-tests` | `mvn -Dtest=<clase> test` en `backend/` |
| `--decidir` | Registrar veredicto humano |
| `--veredicto` | `APPROVE` \| `REJECT` \| `MODIFIED` |
| `--nota` | Nota libre de auditoría |
| `--forzar` | Permitir sobrescribir salida existente (solo borradores agente) |

## Ejemplo

```bash
cd tools/ai-test-generator-edusync

# 1) Propuesta (sin LLM)
java GenerarTest.java \
  --clase backend/src/main/java/com/edusync/academico/application/service/CrearEstudianteService.java \
  --contexto backend/src/main/java/com/edusync/academico/domain/Estudiante.java \
  --test-manual backend/src/test/java/com/edusync/academico/domain/EstudianteTest.java \
  --salida backend/src/test/java/com/edusync/academico/application/service/CrearEstudianteServiceTest.java \
  --tarea "Focus on duplicate RUDE, null tenant, repository failures. Do not duplicate existing tests."

# 2) Generar (tras revisar la propuesta; sustituye SESSION_ID)
java GenerarTest.java --generar --aprobar-alcance --sesion SESSION_ID

# 3) Ejecutar
java GenerarTest.java --ejecutar-tests --sesion SESSION_ID

# 4) Decisión humana (obligatoria; PASSED ≠ APPROVED)
java GenerarTest.java --decidir --sesion SESSION_ID --veredicto APPROVE \
  --nota "Checklist 3 preguntas OK"
```

## Qué no hace (aún — fases siguientes)

- Integration / Contract / E2E
- Lectura automática de % JaCoCo en la propuesta
- Aprobación automática
- Modificación de código productivo (`src/main`)

Ver skill `ai-test-generator-edusync` y `docs/qa/README.md`.
