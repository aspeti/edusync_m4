---
id: DD-UC-026
titulo: "Asistente — consultas académicas dinámicas (resolver + query parametrizable + contexto)"
producto: "EduSync"
grupo: "G-EduSync"
fsd_uc:
  - "NFR-007"
  - "FSD-UC-016"
prd_refs:
  - "PRD-NFR-007"
adrs:
  - "ADR-0018"
  - "ADR-0019"
  - "ADR-0020"
prompts:
  - "PR-IMPL-026"
release: "release/3.0.0"
status: aprobado
fecha: "27/09/2026"
autores:
  - "Rodrigo Aspeti"
---

# Design Doc `DD-UC-026` — Consultas académicas dinámicas del asistente

> **Qué es**: cómo se implementa el diseño de `docs/ai/conversational-assistant-design.md` sobre el asistente ya existente (`DD-UC-025`). No reemplaza KEYWORD de listado ni el bucle ReAct; añade resolución de entidades, un POST parametrizable y el camino `CONSULTA`.

## 1. Objetivo y contexto

- **Qué resuelve**: el usuario pregunta en lenguaje natural combinando alumno, curso/paralelo, materia, periodo y agregados, sin UUIDs ni Playbooks por combinación. Cierra el fallo «el LLM tarda o no encuentra a Fabián».
- **Trazabilidad**: `NFR-007`, `FSD-UC-016` (`CalculoNotas`), `ADR-0020`.
- **Dentro**: resolver REST, `consultar_academico`, analizador + orquestador, catálogo tipado al LLM, history/contexto, tests, UI hilo + sessionStorage.
- **Fuera**: Playbooks de escritura, RAG, Open WebUI del agente (sigue deuda `DD-UC-025`), SQL generado, in-process desde `shared.ai`.

## 2. Diseño

### 2.1 Enfoque

Ver `docs/ai/conversational-assistant-design.md` §2. Orden en `EjecutarConsultaAgenteService`:

1. KEYWORD (frases exactas, incl. `find_profesor` con `q` extraído).
2. Camino `CONSULTA` si el analizador extrae operación + menciones (o follow-up de slots).
3. ReAct sobre `CatalogoHerramientasAgente.todas()` (ya no OpenAPI).
4. `NINGUNO`.

Tras tools en ReAct, el **formatter** arma `respuesta` (última observación), no la prosa del modelo.

### 2.2 Contratos REST nuevos (`academico`)

```http
GET /api/v1/consultas-academicas/entidades/estudiantes?q=
GET /api/v1/consultas-academicas/entidades/materias?q=
GET /api/v1/consultas-academicas/entidades/periodos?q=
GET /api/v1/consultas-academicas/entidades/profesores?q=
GET /api/v1/consultas-academicas/entidades/curso-paralelo?q=
POST /api/v1/consultas-academicas/consultar
```

RBAC: `ADMIN`, `SECRETARIA`, `PROFESOR` (mismo criterio de lectura de notas; el profesor sigue limitado por `MateriaAccesoService`).

Respuesta resolver: `{ estado: UNICO|AMBIGUO|NINGUNO, matches: [{ id, etiqueta, cursoId?, paraleloId? }] }` — sin `rude`.

Body consultar: ver documento de arquitectura §2.4.

### 2.3 Componentes

| Capa | Tipo |
|------|------|
| `academico.application` | `ResolverEntidadAcademicaUseCase`, `ConsultarAcademicoUseCase` |
| `academico.infrastructure.adapter.in.rest` | `ConsultaAcademicaController` + DTOs |
| `shared.ai` | `AnalizadorIntencionConsultaAcademica`, `OrquestadorConsultaAcademica`, delta catálogo/enrutador/formatter/orquestador/request |
| `frontend/features/asistente` | hilo, history, contexto, sessionStorage |

Delta menor de persistencia: `AsignacionMateriaCursoRepositoryPort.listarPorCursoParaleloYTenant` (materias del alumno). Sin Flyway.

### 2.4 Request agente (delta)

`history[]`, `contexto` (ids + etiquetas + `ultimaOperacion`). `pregunta` máx. 4000.

`camino` nuevo: `CONSULTA`.

## 3. Alternativas consideradas

Las de `ADR-0020` §2. Elegida D.

## 4. Impacto en specs vivas

| Artefacto | Cambio | ¿Delta vs DTI? |
|-----------|--------|----------------|
| `docs/adr/0020-*.md` | Nueva decisión | **sí** → DTI §9 |
| `docs/product/DTP.md` | §A.1, §A.2, §B §9 | **sí** |
| `docs/product/FSD.md` | Nota NFR-007: BFF de lectura del asistente | no (logs intactos) |
| `TOOL-CATALOG.md` | v1.3 find_* + consultar | no |
| Baseline | **No se toca** | — |

## 5. Prompts

| Prompt | Tarea | Artefacto |
|--------|-------|-----------|
| `PR-IMPL-026` | Código fullstack | academico consultas + shared.ai + UI |

## 6. Plan de pruebas

1. Analizador: notas Juan + trimestre → estudiante + periodo.
2. Analizador: Juan + Matemática → estudiante + materia.
3. Analizador: promedio 1ro A + segundo trimestre → curso/paralelo + periodo + PROMEDIO.
4. Analizador: reprobaron Matemática → materia + REPROBADOS.
5. Resolver: dos Juan → AMBIGUO (formatter pregunta).
6. Periodo: alias «primer trimestre» vs `Trimestre 1` de la gestión ACTIVA.
7. Consultar: UUID de otro tenant → sin filas / NINGUNO (no fuga).

Más: KEYWORD `existe un profesor con nombre fabian` → `find_profesor` + `q=fabian`; formatter de matches.

## 7. Definition of Done

- [x] Documento `docs/ai/conversational-assistant-design.md`
- [x] Resolver + consultar + tests
- [x] Camino CONSULTA + ReAct catálogo tipado
- [x] history/contexto UI
- [x] `mvn test` + `ng build` (verificación en este turno)
- [x] `dtp-sync` / PROMPT_MAPPING
- [x] Baseline intacto

## 8. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 27/09/2026 | Rodrigo Aspeti | Diseño aprobado; ejecución `PR-IMPL-026`. |
