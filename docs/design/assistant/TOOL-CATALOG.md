---
id: TOOL-CATALOG-edusync-v1.3
title: Catálogo de tools — Asistente EduSync
design_parent: DD-UC-026
adr: ADR-0019 / ADR-0020
status: aprobado
fecha: "27/09/2026"
agente_default: general
---

# TOOL-CATALOG — Asistente EduSync

Fuente de verdad de las tools del asistente (`POST /api/v1/ai/agente`). El LLM **solo** ve este catálogo (filtrado por rol JWT). El descubridor OpenAPI de `DD-UC-024` **no** alimenta al modelo; se usa como linter (path/método deben existir).

> **Regla de oro:** el LLM no accede a JPA ni redacta el texto de producto. Cada tool se ejecuta por **HTTP loopback** con el JWT del usuario (`ADR-0018`). El texto lo arma `FormateadorRespuestaAgente`.

## 1. Convenciones

| Campo | Significado |
|-------|-------------|
| `toolId` | Nombre enviado al modelo (snake_case, estable) |
| Side-effect | `read` o `write` |
| Roles | Subconjunto de `ADMIN`, `SECRETARIA`, `PROFESOR` alineado al `@PreAuthorize` del REST |
| Tablas | Nombres de tabla PostgreSQL para la traza `steps[].tablasFuente` (sin dump de filas) |
| KEYWORD | Frases (minúsculas) que disparan el camino sin LLM |

**Prohibido en este catálogo:** `/api/v1/auth/**`, `/plataforma/**`, `/ai/**`, paths con `/calificaciones` o `/nota-provisional`, módulo `notassie`, `DELETE` de usuarios, restablecer password.

## 2. Matriz RBAC (oleada 1)

| toolId | Side-effect | ADMIN | SECRETARIA | PROFESOR | REST |
|--------|-------------|:-----:|:----------:|:--------:|------|
| `list_gestiones_escolares` | read | ✓ | — | — | `GET /api/v1/gestiones-escolares` |
| `get_gestion_activa` | read | ✓ | ✓ | ✓ | `GET /api/v1/gestiones-escolares/activa` |
| `list_cursos` | read | ✓ | ✓ | — | `GET /api/v1/cursos` |
| `list_paralelos` | read | ✓ | ✓ | — | `GET /api/v1/cursos/{id}/paralelos` |
| `list_materias` | read | ✓ | ✓ | — | `GET /api/v1/materias` |
| `list_mis_materias` | read | ✓ | ✓ | ✓ | `GET /api/v1/materias/mias` |
| `list_estudiantes` | read | ✓ | ✓ | — | `GET /api/v1/estudiantes` |
| `list_profesores` | read | ✓ | ✓ | ✓ | `GET /api/v1/profesores` |
| `list_evaluaciones` | read | ✓ | — | ✓ | `GET /api/v1/evaluaciones` (metadatos; **sin** notas) |
| `list_usuarios` | read | ✓ | — | — | `GET /api/v1/usuarios` |
| `list_periodos_evaluacion` | read | ✓ | ✓ | ✓ | `GET /api/v1/gestiones-escolares/activa/periodos` |
| `list_secciones_evaluacion` | read | ✓ | ✓ | ✓ | `GET /api/v1/gestiones-escolares/activa/secciones` |
| `create_curso` | write | ✓ | — | — | `POST /api/v1/cursos` |
| `create_paralelo` | write | ✓ | — | — | `POST /api/v1/cursos/{id}/paralelos` |
| `create_materia` | write | ✓ | ✓ | — | `POST /api/v1/materias` |
| `create_estudiante` | write | ✓ | ✓ | — | `POST /api/v1/estudiantes` |
| `create_inscripcion` | write | ✓ | ✓ | — | `POST /api/v1/inscripciones` |
| `cambiar_estado_gestion` | write | ✓ | — | — | `PATCH /api/v1/gestiones-escolares/{id}/estado` |
| `find_estudiante` | read | ✓ | ✓ | ✓ | `GET /api/v1/consultas-academicas/entidades/estudiantes` |
| `find_materia` | read | ✓ | ✓ | ✓ | `GET /api/v1/consultas-academicas/entidades/materias` |
| `find_periodo` | read | ✓ | ✓ | ✓ | `GET /api/v1/consultas-academicas/entidades/periodos` |
| `find_curso_paralelo` | read | ✓ | ✓ | ✓ | `GET /api/v1/consultas-academicas/entidades/curso-paralelo` |
| `find_profesor` | read | ✓ | ✓ | ✓ | `GET /api/v1/consultas-academicas/entidades/profesores` |
| `consultar_academico` | read | ✓ | ✓ | ✓ | `POST /api/v1/consultas-academicas/consultar` |

SYSADMIN no usa `/asistente` (fuera de alcance). El REST sigue siendo la autoridad: si el JWT no puede, el loopback responde 403 y el formatter lo traduce sin filtrar PII a logs.

## 3. Tools `read`

### 3.1 `list_cursos`

| | |
|--|--|
| Tablas | `curso` |
| KEYWORD | `lista los cursos`, `cuántos cursos`, `cuantos cursos`, `qué cursos hay`, `que cursos hay` |
| Args | `q` (opcional), `page`, `size` |

### 3.2 `list_paralelos`

| | |
|--|--|
| Tablas | `paralelo` |
| KEYWORD | (ninguna sin `cursoId` UUID en la pregunta) |
| Args | `cursoId` (UUID, requerido) |

### 3.3 `list_gestiones_escolares`

| | |
|--|--|
| Tablas | `gestion_escolar` |
| KEYWORD | `lista las gestiones`, `gestiones escolares` |
| Args | `q`, `estado` opcionales |
| Roles | solo `ADMIN` |

### 3.3b `get_gestion_activa`

| | |
|--|--|
| Tablas | `gestion_escolar` |
| KEYWORD | `gestión activa`, `gestion activa`, `cuál es la gestión activa` |
| REST | `GET /api/v1/gestiones-escolares/activa` |
| Notas | Sin UUID. 404 si no hay gestión `ACTIVA`. |

### 3.4 `list_materias`

| | |
|--|--|
| Tablas | `materia` |
| KEYWORD | `lista las materias`, `qué materias hay`, `que materias hay` |
| Args | `q` opcional |

### 3.4b `list_mis_materias`

| | |
|--|--|
| Tablas | `materia` |
| KEYWORD | `mis materias`, `lista mis materias`, `materias asignadas` |
| REST | `GET /api/v1/materias/mias` |

### 3.5 `list_estudiantes`

| | |
|--|--|
| Tablas | `estudiante` |
| KEYWORD | `lista los estudiantes`, `lista los alumnos`, `cuántos estudiantes`, `cuantos alumnos` |
| Args | `q` opcional |
| Notas | No devolver `rude` al formatter de producto si el DTO REST lo trae: **enmascarar** en el formatter (NFR-007). El loopback puede traerlo; el texto de chat no lo cita. |

### 3.6 `list_profesores`

| | |
|--|--|
| Tablas | `usuario` (perfil profesor; no hay tabla `profesor`) |
| KEYWORD | `lista los profesores`, `quiénes son los profesores`, `quienes son los profesores` |

### 3.7 `list_evaluaciones`

| | |
|--|--|
| Tablas | `evaluacion` |
| KEYWORD | (ninguna en código: no hay `GET /evaluaciones` de colección) |
| Notas | Solo metadatos vía `GET /materias/{id}/evaluaciones` (oleada B). **Prohibido** encadenar a calificaciones. |

### 3.8 `list_usuarios`

| | |
|--|--|
| Tablas | `usuario`, `usuario_rol` |
| KEYWORD | `lista los usuarios`, `quiénes son los usuarios` |
| Roles | solo `ADMIN` |

### 3.9 `list_periodos_evaluacion` / `list_secciones_evaluacion`

En código: `list_periodos_gestion_activa` / `list_secciones_gestion_activa` (sin UUID).

| | |
|--|--|
| Tablas | `periodo_evaluacion` / `seccion_evaluacion` |
| KEYWORD | `lista los periodos`, `periodos de evaluación`; `lista las secciones`, `secciones de evaluación` |
| REST | `GET /api/v1/gestiones-escolares/activa/periodos` y `.../activa/secciones` |

`list_evaluaciones` (metadatos, sin notas) **sigue fuera de KEYWORD**: no existe `GET /evaluaciones` de colección; requiere `materiaId` (`GET /materias/{id}/evaluaciones`, oleada B).

## 4. Tools `write` (confirmación obligatoria)

Protocolo (`DD-UC-025` §2.6):

1. Primera invocación → **no** HTTP de mutación → `confirmacionRequerida=true` + preview.
2. Segunda con `confirmed=true` y mismos args → loopback POST/PATCH.

### 4.1 `create_curso`

| | |
|--|--|
| Tablas | `curso` |
| Args | `nombre` (requerido) |
| KEYWORD | `crea el curso`, `crear el curso`, `crea un curso`, `crear curso` + nombre |
| Preview | «Se creará el curso *{nombre}*» — sin POST hasta `confirmed=true` |

### 4.2 `create_paralelo`

| | |
|--|--|
| Tablas | `paralelo` |
| Args | `cursoId`, `nombre` |

### 4.3 `create_materia`

| | |
|--|--|
| Tablas | `materia` |
| Args | `nombre` |
| KEYWORD | `crea la materia`, `crear la materia`, `crea una materia`, `crear materia` + nombre |

### 4.4 `create_estudiante`

| | |
|--|--|
| Tablas | `estudiante` |
| Args | los del `POST /estudiantes` (sin loguear `rude`) |
| Preview | citar nombre; **no** citar `rude` en el chat |

### 4.5 `create_inscripcion`

| | |
|--|--|
| Tablas | `inscripcion` |
| Args | `estudianteId`, `gestionId`, `paraleloId` |

### 4.6 `cambiar_estado_gestion`

| | |
|--|--|
| Tablas | `gestion_escolar` |
| Args | `id`, `estado` (`PLANIFICACION` \| `ACTIVA` \| `CERRADA`) |
| Preview | estado actual → propuesto; el REST rechaza transiciones inválidas |

## 5. Fuera de catálogo (explícito)

| Tema | Motivo |
|------|--------|
| `GET/PUT .../calificaciones` y `.../nota-provisional` | NFR-007: el LLM no llama esos paths; `consultar_academico` es el BFF |
| RAG / `search_normative_docs` | Diferido (`DD-UC-025`) |
| Tenants / login / agente | Superficie de ataque |
| Copilotos `phases`/`users`/`evidence` | Fuera de alcance |
| SQL generado por el modelo | Prohibido (`ADR-0020`) |

## 6. Tools de resolución y consulta (`ADR-0020`)

El LLM **nunca inventa UUIDs**. Primero `find_*` con `q` en texto; luego `consultar_academico` con los IDs del JSON.

### 6.1 `find_estudiante` / `find_materia` / `find_periodo` / `find_curso_paralelo` / `find_profesor`

| | |
|--|--|
| Side-effect | read |
| Args | `q` (texto; en periodo opcional = periodo `ABIERTO` de la gestión ACTIVA) |
| Respuesta | `{ estado: UNICO\|AMBIGUO\|NINGUNO, matches: [{ id, etiqueta, cursoId?, paraleloId? }] }` — **sin RUDE** |
| KEYWORD | solo `find_profesor`: `existe un profesor`, `existe algun profesor`, `profesor con nombre` (extrae `q`) |

«Primer trimestre» se resuelve contra el nombre/orden de la gestión **ACTIVA**, no contra `orden=1` inventado.

### 6.2 `consultar_academico`

| | |
|--|--|
| Método | `POST` |
| Args | `operacion` (`NOTAS`\|`PROMEDIO`\|`REPROBADOS`\|`TOP`\|`NOMINA`\|`MATERIAS_ESTUDIANTE`), IDs opcionales (`estudianteId`, `cursoId`, `paraleloId`, `materiaId`, `periodoEvaluacionId`, `gestionEscolarId`), `umbral` (default 51, no es BR) |
| Agregados | backend (`CalculoNotas` / nota provisional). El LLM solo narra el número recibido |
| RBAC | REST + `MateriaAccesoService`; UUID de otro tenant → `SIN_RESULTADOS` |

## 7. Historial

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v1 | 26/09/2026 | Oleada 1: 10 read + 6 write. |
| v1.1 | 26/09/2026 | Oleada A KEYWORD sin UUID: `get_gestion_activa`, periodos/secciones de la activa, `list_mis_materias`. `list_evaluaciones` sigue pendiente de `materiaId`. |
| v1.2 | 26/09/2026 | Oleada C: `create_curso` / `create_materia` con preview + `confirmed=true`. Paralelo, estudiante, inscripción y cambio de estado de gestión siguen fuera de KEYWORD (UUID/RUDE). |
| v1.3 | 27/09/2026 | `ADR-0020` / `DD-UC-026`: `find_*` + `consultar_academico`. Calificaciones REST siguen fuera; el BFF de lectura es el único camino de notas del asistente. |
