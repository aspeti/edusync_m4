---
id: conversational-assistant-design
titulo: "Consultas académicas dinámicas en lenguaje natural"
producto: EduSync
grupo: G-EduSync
fsd_uc:
  - NFR-007
  - FSD-UC-016
  - FSD-UC-013
  - FSD-UC-017
  - FSD-UC-018
  - FSD-UC-019
  - FSD-UC-020
adrs:
  - ADR-0018
  - ADR-0019
  - ADR-0020
design_docs:
  - DD-UC-025
  - DD-UC-026
prompts:
  - PR-IMPL-026
release: release/3.0.0
status: aprobado
fecha: "27/09/2026"
---

# Consultas académicas dinámicas — diseño del asistente conversacional

> Arquitectura de producto para que un usuario autenticado pregunte en español sobre el sistema académico **sin** conocer endpoints, UUIDs, SQL ni el esquema. Complementa `DD-UC-025` / `ADR-0019` (KEYWORD + catálogo) y **no** reabre `LlmPort` ni `POST /api/v1/ai/chat`. Detalle de implementación: `docs/design/DD-UC-026.md`. Decisión: `docs/adr/0020-consultas-academicas-dinamicas-asistente.md`.

## 2.1 Objetivo

EduSync permitirá consultas académicas en lenguaje natural sobre el tenant del JWT. El usuario formula preguntas como «¿Cuáles son las notas de Juan del primer trimestre?» o «¿Qué alumnos están en 1ro A?». El sistema:

1. Interpreta la intención (operación + menciones).
2. **Resuelve** nombres a IDs reales en el backend (nunca inventados por el modelo).
3. Ejecuta **una** consulta parametrizable reutilizando servicios ya existentes (`CalculoNotas`, nómina, listados con `q`).
4. Devuelve texto de producto escrito por **código** (formatter), no un recuento alucinado.

No se crea un Playbook por combinación de filtros. Las combinaciones son argumentos de una misma operación.

## 2.2 Arquitectura

```text
Usuario (Angular /asistente)
    → POST /api/v1/ai/agente  { pregunta, history[], contexto, confirmed? }
    → EjecutarConsultaAgenteService
         ├─ KEYWORD          frases exactas del catálogo (0 LLM)
         ├─ CONSULTA         analizador de intención + resolver + consultar (0 LLM)
         └─ LLM / ReAct      llama3.1:8b elige tools del catálogo tipado
    → Tools (HTTP loopback + JWT del usuario)     [ADR-0018]
         → REST academico / identidad ya público
              → Application services
                   → Repository ports (tenant_id + RLS)
                        → PostgreSQL (SQL parametrizado / Criteria)
    → FormateadorRespuestaAgente → texto al usuario
```

| Componente | Responsabilidad | Qué no hace |
|------------|-----------------|-------------|
| UI `/asistente` | Hilo, `history`, `contexto` en `sessionStorage`, chips | No calcula notas ni resuelve IDs |
| `EjecutarConsultaAgenteService` | Orden KEYWORD → CONSULTA → ReAct → NINGUNO; traza `camino`/`steps` | No importa paquetes de `academico` (Modulith) |
| `AnalizadorIntencionConsultaAcademica` | Extrae operación + menciones de texto (sin IDs) | No consulta BD |
| `OrquestadorConsultaAcademica` | Encadena tools `find_*` + `consultar_academico` por HTTP | No escribe SQL |
| Tools / `EjecutorHerramientaHttpAdapter` | Loopback REST con Bearer del usuario | No eleva privilegios |
| `ResolverEntidadAcademicaUseCase` | Nombre/alias → matches `{id, etiqueta}` del tenant | No adivina si hay 2 Juan |
| `ConsultarAcademicoUseCase` | Filtros UUID opcionales + operación; agrega con `CalculoNotas` | No acepta SQL ni IDs de otro tenant |
| LLM (`AgenteLlmPort`) | Solo elige tool o señala fin; paráfrasis que CONSULTA no cubre | No inventa UUIDs; no redacta el `respuesta` de producto si hubo tools |
| Repositories | ORM / Criteria con `tenantId` | No conocen el asistente |

`shared.ai` **sigue sin importar** `academico` (`ADR-0011`/`0018`). Las tools nuevas son REST en `academico`.

## 2.3 Tool vs ReAct vs Playbook

### Tool

Acción concreta y parametrizable, alineada a un path REST.

| toolId | Rol | REST |
|--------|-----|------|
| `find_estudiante` | Resolver alumno por nombre | `GET /api/v1/consultas-academicas/entidades/estudiantes?q=` |
| `find_materia` | Resolver materia | `GET .../entidades/materias?q=` |
| `find_periodo` | Resolver periodo/trimestre (alias «primer trimestre» → seed `Trimestre 1`) | `GET .../entidades/periodos?q=` |
| `find_curso_paralelo` | Resolver «1ro A» | `GET .../entidades/curso-paralelo?q=` |
| `find_profesor` | Resolver profesor por nombre | `GET .../entidades/profesores?q=` |
| `consultar_academico` | Consulta parametrizable | `POST /api/v1/consultas-academicas/consultar` |

Las tools KEYWORD de listado (`list_cursos`, etc.) **se conservan**.

### ReAct / agente

El bucle ya existente (`max-turnos`, catálogo tipado, ejecución HTTP). El modelo decide **qué** `find_*` llamar y con qué `q` (string), luego `consultar_academico` con los **IDs que devolvieron las tools**. Si un `find_*` vuelve `estado=AMBIGUO`, el formatter pide aclaración y el bucle termina.

### Playbook

Flujo de negocio con pasos controlados y side-effects (cerrar periodo, inscribir, boletín, cerrar gestión). **Fuera de este slice.** No se usa un playbook «notas-de-juan-trimestre-1».

## 2.4 Consultas dinámicas

Un solo comando, filtros opcionales (nulos = no aplican):

```json
{
  "operacion": "NOTAS",
  "estudianteId": "uuid-o-null",
  "cursoId": null,
  "paraleloId": null,
  "materiaId": "uuid-o-null",
  "periodoEvaluacionId": "uuid-o-null",
  "gestionEscolarId": null,
  "umbral": 51
}
```

`operacion`:

| Valor | Pregunta típica | Filtros mínimos |
|-------|-----------------|-----------------|
| `NOTAS` | Notas de Juan del trimestre / en Matemática | `estudianteId` + (`periodoId` y/o `materiaId`) |
| `PROMEDIO` | Promedio de 1ro A del 2.º trimestre; promedio de Juan en Matemática | (`paraleloId` o `estudianteId`) + `periodoId` |
| `REPROBADOS` | Quién reprobó Matemática | `materiaId` (+ `periodoId`; `umbral` default 51, **no es una BR**) |
| `TOP` | Promedio más alto de 2do B | `paraleloId` + `periodoId` |
| `NOMINA` | Qué alumnos están en 1ro A | `paraleloId` |
| `MATERIAS_ESTUDIANTE` | Qué materias tiene Juan | `estudianteId` |

La gestión por defecto es la **ACTIVA** del tenant (`DD-UC-021`). «Este trimestre» = periodo `ABIERTO` de esa gestión, si hay exactamente uno.

## 2.5 Resolución de entidades

El LLM **nunca** elige `evaluationPeriodId = 1` porque «primer trimestre». Debe llamar `find_periodo` con `q=primer trimestre`. El backend:

1. Normaliza (minúsculas, NFD sin diacríticos).
2. Compara contra `PeriodoEvaluacion.nombre` y `orden` (alias: primer/1er/trimestre 1 → orden 1; el seed es `Trimestre N`).
3. Devuelve `{ estado, matches[{id, etiqueta}] }`.
4. `UNICO` → el orquestador usa ese id. `AMBIGUO` → pregunta. `NINGUNO` → no hay en el tenant.

Igual para alumnos, cursos/paralelos, materias, profesores, gestiones. IDs que no pertenezcan al tenant del JWT se tratan como inexistentes (404 / match vacío), sin filtrar datos de otra UE.

`rude` **no** viaja en el JSON que ve el modelo ni en el texto de chat (`NFR-007`).

## 2.6 Flujo de ejemplo

Usuario: «¿Cuál es el promedio de Juan en Matemática durante el primer trimestre?»

```text
1. Analizador (camino CONSULTA, 0 LLM) o ReAct:
   operacion=PROMEDIO, estudiante="Juan", materia="Matemática", periodo="primer trimestre"
2. find_estudiante("Juan")     → UUID real o AMBIGUO
3. find_materia("Matemática")  → UUID real
4. find_periodo("primer trimestre") → UUID del PeriodoEvaluacion de la gestión ACTIVA
5. consultar_academico(PROMEDIO, estudianteId, materiaId, periodoEvaluacionId)
6. ObtenerNotaProvisionalUseCase + CalculoNotas (round HALF_UP, ADR-0013)
7. Formatter: «El promedio de Juan Pérez en Matemática (Trimestre 1) es 85.»
```

El usuario no ve estos pasos; la UI puede mostrar `camino=CONSULTA` y `steps[]`.

## 2.7 Conversación contextual

El backend es **stateless**. El cliente envía:

- `history[]`: últimos turnos `user`/`assistant` (máx. 30).
- `contexto`: slots ya resueltos `{ estudianteId, materiaId, …, etiquetas, ultimaOperacion }`.

Ejemplo:

1. «Muéstrame las notas de Juan.» → falta periodo → «¿De qué período?» + contexto.estudianteId.
2. «Del primer trimestre.» → se rellena periodo; misma operación `NOTAS`.
3. «¿Y en Matemática?» → se rellena materia; no se pide de nuevo a Juan.

Si el usuario cambia de sujeto («ahora las de Ana»), el analizador pisa el slot estudiante.

## 2.8 Seguridad

- JWT del **usuario que pregunta** en todo loopback (`ADR-0018`).
- `tenant_id` solo de `TenantContextProvider`, nunca del body.
- RBAC idéntico a las lecturas académicas: `ADMIN`/`SECRETARIA` ven el tenant; `PROFESOR` solo materias asignadas (`MateriaAccesoService`, 404 no 403).
- IDs se revalidan con `buscarPorIdYTenant`. Un UUID de otra UE no existe en este tenant.
- El modelo no puede llamar `/auth`, `/plataforma`, `/ai`, ni `PUT` de calificaciones. `consultar_academico` es **solo lectura**.
- Logs: sin RUDE, sin vector de notas, sin JWT (`AGENTS.md` §7 / `NFR-007`). Los **nombres** sí aparecen en el chat (el usuario ya los ve en la consola); no se loguean a INFO.

**Delta NFR-007 / ADR-0018:** el catálogo v1 prohibía paths `/calificaciones`. Este diseño **no** abre esos paths al LLM. Abre un BFF de lectura ` /consultas-academicas/**` que devuelve resúmenes (nombre + número agregado). El modelo puede ver esos JSON en el bucle ReAct; el texto de producto lo arma Java. Escritura de notas sigue prohibida.

## 2.9 SQL y acceso a datos

```text
LLM / analizador
 → parámetros de tool (strings o UUIDs ya resueltos)
 → validación + RBAC + tenant
 → Application service
 → Repository (Criteria / JPQL parametrizado)
 → PostgreSQL
```

No hay generación de SQL por el modelo. No hay JDBC desde `shared.ai`.

## 2.10 Errores y ambigüedad

| Situación | Comportamiento |
|-----------|----------------|
| Varios «Juan Pérez» | `estado=AMBIGUO` + etiquetas con curso/paralelo. El asistente **pregunta**; no elige. |
| Materias similares | Igual, lista nombres. |
| «Primer trimestre» en varias gestiones | Se acota a la gestión **ACTIVA**. Si no hay ACTIVA → pedir gestión. Si dos periodos matchean en la misma gestión → AMBIGUO. |
| Falta un filtro | `FALTA_CONTEXTO` + pregunta («¿De qué período?»). |
| Fuera de alcance (presupuesto, otra UE, SQL) | Camino `NINGUNO` o ReAct sin tools → texto genérico, sin inventar datos. |
| Cero filas | «No hay resultados en tu institución para esos filtros.» |
| 403/404 del REST | Formatter: sin permiso o no encontrado; sin JSON crudo. |

## 3. Tools necesarias (reuso)

No se duplican listados: `find_*` envuelve `ListarEstudiantesUseCase` / `ListarMateriasUseCase` / `ListarPeriodosEvaluacionUseCase` / `ProfesorConsultaPort` / cursos+paralelos, con matching de alias en aplicación.

`consultar_academico` reutiliza:

- `ObtenerNotaProvisionalUseCase` + `CalculoNotas` (`FSD-UC-016`)
- `InscripcionRepositoryPort.listarActivasPorGestionYParesCursoParalelo` (nómina)
- `ListarInscripcionesEstudianteUseCase` + asignaciones curso/paralelo (materias del alumno)

Agregaciones **en backend**. El LLM no suma promedios.

## 4. Consultas parametrizables

Se evita `getJuanFirstTrimesterGrades()`. Una operación + filtros nulleables, adaptada a UUIDs del dominio EduSync (no enteros).

## 5–8. Resolución, agente, separación, agregaciones

Cubiertos en §2.2–2.6 y §3. Camino CONSULTA cubre las frases de ejemplo **sin** gastar turnos de `llama3.1:8b`. ReAct queda para paráfrasis y encadenamientos no previstos por el analizador. El catálogo que ve el LLM es el **tipado** (`CatalogoHerramientasAgente`), no el dump OpenAPI (`DD-UC-025`; deuda cerrada aquí).

## 9. Pruebas (mínimo)

Ver `DD-UC-026` §6: casos 1–7 del pedido (slots, agregación, ambigüedad, periodo, aislamiento de tenant).

## 10. Compatibilidad conversacional

`history` + `contexto` en el request; UI `sessionStorage['edusync.asistente.conversacion']`. Sin tabla de chats.

## 11. Anti-sobreingeniería

- Sin segundo framework de agentes.
- Sin use cases in-process desde `shared.ai`.
- Sin RAG, MCP, ni copilotos por pantalla.
- Sin Playbooks de consulta.
- KEYWORD de listado intacto.

## 12. Fases de implementación

| Fase | Entrega |
|------|---------|
| 1 | Este documento (análisis del código real) |
| 2 | `ADR-0020` + `DD-UC-026` + `PR-IMPL-026` |
| 3 | `ResolverEntidadAcademica*` |
| 4 | `ConsultarAcademico*` |
| 5 | Catálogo + CONSULTA + ReAct sobre catálogo tipado |
| 6 | `history` + `contexto` + UI |
| 7 | RBAC/tenant (heredado + tests) |
| 8 | Tests unitarios de analizador, resolver, consulta, orquestador |
