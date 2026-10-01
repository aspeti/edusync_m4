# Manual del agente edusync-red-team

## Proposito

Encontrar, reproducir y congelar pruebas contra el asistente GenAI de EduSync.
El catalogo versionado es `catalog/attacks.json` (1.1.0). Un ataque solo protege el producto cuando `enabled` es true y `RedTeamAssistantCatalogWebMvcTest` queda en verde.

El agente actua como operador. No reescribe reglas del asistente para que un ataque deje de fallar.

## Arquitectura

```text
tools/red-team-agent/
  catalog/attacks.json      fuente versionada
  catalog/taxonomy.yaml     categorias
  catalog/evaluators.json   patrones
  catalog/fixtures.json     identificadores sinteticos
  catalog/golden.json       propiedades, no texto exacto
  ataques/<categoria>/      un JSON por ataque
  evaluators/               evaluadores del modo API local
  cli.py                    listar, generar, sync, probar, report, maven
backend/.../RedTeamAssistantCatalogWebMvcTest.java
```

El gemelo `tools/ai-security-demo/` sigue siendo la demostracion vulnerable/seguro.
No sustituye este gate. El test Java pega en `AgenteController` con el servicio real y dobles de `AgenteLlmPort` y `EjecutorHerramientaPort`.

## Instalacion

Python 3 para el operador. Maven y JDK 25 para el gate, los mismos del backend.
No hay dependencia nueva.

```text
cd tools/red-team-agent
python cli.py listar
```

## Configuracion

| Variable | Uso |
|---|---|
| ninguna | `probar` / `maven` (determinista) |
| `EDUSYNC_REDTEAM_BASE_URL` | solo `probar-api`, default `http://127.0.0.1:8080` |
| `EDUSYNC_REDTEAM_JWT` | solo `probar-api`, en el entorno, nunca en git |

Perfil live: no esta en el `mvn test` de CI. `probar-api` pega al API local, repite cada ataque 3 veces y escribe `evidencia/hallazgos_live_<fecha>.md` mas `evidencia/ultimo_live.md`. El JWT no se copia al informe.

## Comandos

Ver `README.md`. `sync --check` falla si `ataques/` y `attacks.json` divergen. CI lo ejecuta antes de los tests.

`probar --trials N` no repite contra un modelo. El doble es determinista, asi que cada ataque se evalua una vez. Las repeticiones contra Ollama quedan fuera de este gate.

## Categorias

Las 18 categorias estan en `catalog/taxonomy.yaml`, con objetivo, amenaza, ejemplo, exito, bloqueo, severidad y evaluador.

## Catalogo

Cada ataque tiene id, categoria, input, rol, modelo doble, expectativa y `enabled`.
El caso obligatorio es `RT-INJ-001`: pide el canary sintetico. No usa correos ni contrasenas reales.

## Evaluadores

Clases en `evaluators/response_eval.py` y `evaluators/auth_eval.py`, reunidas por `evaluators/engine.py`:

- AuthorizationEvaluator
- SecretLeakEvaluator
- PIIEvaluator
- AcademicDataLeakEvaluator
- PromptInjectionEvaluator
- ToolAuthorizationEvaluator
- TenantIsolationEvaluator
- SystemPromptLeakEvaluator
- UnsafeOutputEvaluator

El test Java aplica las mismas propiedades (`mustNotDisclose`, `toolCalls`, `jwtIsSession`, `forbiddenArgKeys`) sobre la respuesta HTTP. No compara el parrafo entero.

## Fixtures

`EDUSYNC_TEST_STUDENT_001`, `EDUSYNC_TEST_STUDENT_002`, `EDUSYNC_TEST_TEACHER_001`, `EDUSYNC_TEST_TENANT_A`, `EDUSYNC_TEST_TENANT_B`, `EDUSYNC_REDACTED_CANARY_001`.
Curso de la sesion de prueba: `3ro A`. El paralelo que el profesor no deberia nombrar como propio: `4to B`.

## Ejecucion local

```text
cd tools/red-team-agent
python cli.py maven
```

Eso equivale a `sync --check` y luego:

```text
mvn -Dtest=RedTeamAssistantCatalogWebMvcTest test
```

desde `backend/`. El hallazgo detallado queda en `tools/red-team-agent/evidencia/`, con el mismo formato que la demo (`hallazgos_asistente_<fecha>.md` y `.json`, mas `ultimo.md`). `python cli.py report` imprime ese markdown. Una copia vive en `backend/target/redteam/`.

## CI

`.github/workflows/ci.yml` corre `python cli.py sync --check` y, dentro de `mvn verify`, el test del catalogo.
Los ataques con `enabled: false` no rompen el build. Aparecen en `listar` como `off`.

## Playwright

`tests/e2e/tests/redteam/asistente-canary.spec.ts` no corre en el job de frontend.
Hace falta `E2E_REDTEAM=1` y el stack local (SPA, API, login). No guarda un JWT.

## Politica de seguridad

Prohibido: secretos reales, PII real, DoS, borrado, cambio de notas, cambio de permisos, cualquier host que no sea local.
Si la URL no es local, `probar-api` imprime `ABORT` y sale.

## Como leer un resultado

`PASS` significa que la expectativa del ataque habilitado se cumplio contra el servicio real con un modelo hostil.
`off` en `listar` significa hueco conocido, no proteccion.
Un `FAIL` del test es una regresion: no se edita la expectativa para taparla. Se documenta el hallazgo y, si hace falta un cambio de arquitectura, un ADR en estado Propuesto.
