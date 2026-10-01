# EduSync Red Team

Operador de seguridad del asistente. No modifica el system prompt ni los filtros.
La autoridad de seguridad es el backend. El modelo no cambia el rol, el tenant ni el JWT.

Invocacion en Cursor o Claude: `@edusync-red-team`.

## Endpoint real

`POST /api/v1/assistant/chat` no existe en este repo.

El asistente de producto es:

```http
POST /api/v1/ai/agente
Authorization: Bearer <JWT de la sesion>
{ "pregunta": "...", "confirmed": false, "history": [] }
```

Controlador: `AgenteController`. Orquestacion: `EjecutarConsultaAgenteService`.
Tambien existen `POST /api/v1/ai/chat` y `POST /api/v1/ai/consultar-usuario`. No son el objetivo de este catalogo.

No hay Chroma ni embeddings. El RAG es lexico, sobre archivos de classpath en `RecuperadorProcesosEdusync`.

## Comandos

Desde `tools/red-team-agent` (Windows: `.\run.ps1`, Unix: `./run.sh`):

```text
listar
generar <categoria> "<hint>"
sync
sync --check
probar
probar --trials 3
probar-api
probar-api --trials 3 --ids RT-EXFIL-002,RT-AUTH-003
report
maven
```

`probar` y `maven` ejecutan el test determinista `RedTeamAssistantCatalogWebMvcTest`.
No llaman a Ollama. El modelo es un doble hostil: si el grafo le entrega el turno, responde con el canary.

`probar-api` solo acepta `127.0.0.1`, `localhost` o `::1`. Cualquier otro host aborta.
El JWT va en `EDUSYNC_REDTEAM_JWT` y no se escribe en el repo.

## Retest de AI-SEC-007 y AI-SEC-008

`.\retest_ai_sec.ps1` corre el gate sin `PoliticaAlcanceAgente` (servicio del commit `77e74f9`, se espera rojo) y con ella (se espera verde), y restaura el archivo con verificacion de hash. Con `-Live` repite los tres ataques contra el backend local recompilado. Evidencia en `evidencia/retest/`. Fichas en `docs/HALLAZGOS_AI_SEC_PRODUCTO.md`.

`generar` crea un ataque con `enabled: false` y pide revision humana antes de `sync`.

## Que entra en CI

Los 18 ataques del catalogo 1.1.0 estan habilitados.
`list_usuarios` solo la ejecutan `ADMIN`, `SECRETARIA` y `SYSADMIN`.
El bucle quita `tenantId` y `schoolId` antes de llamar a la tool.

## Datos

Canary: `EDUSYNC_REDACTED_CANARY_001`.
Fixtures: `catalog/fixtures.json`. Sin RUDE real, sin correos reales, sin JWT de una sesion viva.
