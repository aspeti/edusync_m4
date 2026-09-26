# Generador de tests unitarios con IA — EduSync

Herramienta simple: **lee los tests del proyecto**, lista lo que ya existe y **solo agrega escenarios faltantes** (sin duplicar métodos `@Test`).

## Uso (3 modos)

```powershell
cd tools\ai-test-generator-edusync

# 1) Solo analizar (default) — no llama LLM, no escribe
java GenerarTest.java --clase backend/src/main/java/com/edusync/academico/application/service/CrearEstudianteService.java

# 2) Generar solo lo que falta (fusiona en *Test.java existente)
java GenerarTest.java --clase backend/src/main/java/com/edusync/academico/application/service/CrearEstudianteService.java --escribir

# 3) Generar + ejecutar Maven
java GenerarTest.java --clase ...\CrearEstudianteService.java --escribir --run
```

Opcional: `--tarea "..."`, `--contexto <archivo>`, `--salida <ruta>`.

## Qué hace automáticamente

| Paso | Comportamiento |
|------|----------------|
| Localiza salida | `src/main/java/Foo.java` → `src/test/java/FooTest.java` |
| Lee tests existentes | `FooTest`, `FooAgenteTest`, tests del paquete que usan `Foo`, y heurística de dominio (`CrearEstudianteService` → `EstudianteTest`) |
| Anti-duplicado | Lista métodos `@Test` existentes; el LLM recibe esa lista; al fusionar se omiten nombres repetidos |
| Escritura | Si el test existe → **agrega** métodos nuevos. Si no → crea el archivo |
| HITL | `--escribir` es la confirmación humana. La IA **no aprueba**; revisa el diff |

## Requisitos

- JDK del backend
- Para `--escribir`: Ollama/Open WebUI vía `.env` (`EDUSYNC_AI_PROVIDER`, `OLLAMA_*` o `OPEN_WEBUI_*`)
- Para `--run`: `mvn` / `mvn.cmd` en PATH

## Ejemplo de salida (análisis)

```text
========== ANALISIS ==========
Clase:     backend/src/main/java/.../CrearEstudianteService.java
Salida:    backend/src/test/java/.../CrearEstudianteServiceTest.java (existe)
Tests leidos (2):
  - .../CrearEstudianteServiceTest.java
  - .../EstudianteTest.java
Metodos @Test existentes (6):
  - creaUnEstudianteCuandoElRudeEsUnicoEnElTenant
  - rechazaCon409CuandoElRudeYaExisteEnElTenant
  - ...
Escenarios faltantes propuestos (1):
  1. fallo del puerto/repositorio al persistir
==============================
```

Si no hay faltantes → termina sin llamar al LLM.
