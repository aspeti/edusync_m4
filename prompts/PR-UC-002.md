# PR-UC-002 — Contrato de UC-02: Cierre operativo de materia

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-UC-002` |
| Título | Contrato técnico de UC-02: Cierre operativo de materia |
| Artefacto origen | FSD |
| ID origen | `FSD-UC-002` |
| Tipo de prompt | transformación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Backend Engineer especializado en transacciones atomicas,
consistencia eventual y gestion de estados en Spring Boot 3 con PostgreSQL.
```

### 1.2 Task
```text
Genera el contrato tecnico del endpoint POST /api/v1/materias/{id}/cierre
para UC-02 (Cierre operativo de materia), incluyendo la logica de verificacion
de completitud, la transicion de estado a SOLO_LECTURA y el disparo del evento
de consolidacion (UC-03).
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-02.
- El cierre es ATOMICO: no existe cierre parcial.
- Completitud se verifica contra el conjunto de evaluaciones declaradas por el
  propio docente (no contra un numero fijo).
- Post-cierre: la materia transiciona a SOLO_LECTURA de forma irreversible.
- El docente no puede agregar evaluaciones nuevas despues de solicitar el cierre.
- Al cerrar la ultima materia del curso: se dispara MateriaCarradaEvent (UC-03).
- Stack: Java 21, Spring Boot 3, Spring Events, Spring Data JPA.
```

### 1.4 Reasoning
```text
1. Definir el schema de request (materia_id, periodo_id, confirmacion_docente).
2. Especificar la secuencia de verificacion: RBAC → estado periodo → completitud
   (todos los estudiantes con todas sus evaluaciones declaradas completadas).
3. Definir la transicion de estado: ABIERTO → CERRADO → SOLO_LECTURA.
4. Declarar el evento de dominio MateriaCarradaEvent y sus consumidores.
5. Definir los schemas de response (200 OK, errores 400/403/409).
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: verificacion de completitud, transicion de
estado, disparo del evento de dominio y 3 casos de prueba.
```

### 1.6 Output
```text
Markdown con schema de request/response, diagrama de secuencia simplificado
(texto), tabla de estados de la materia y 3 casos de prueba.
```

## 2. Invariantes del prompt

- No se puede cerrar si existe un estudiante con evaluacion declarada sin nota.
- El cierre es irreversible sin intervencion del Director (UC-05).
- El evento `MateriaCarradaEvent` solo se dispara si el cierre fue exitoso.
- El docente que cierra no puede ser diferente del docente asignado (RBAC).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_COMPLETITUD_FALLIDA` | Al menos 1 evaluacion sin nota | HTTP 409 con lista de estudiantes y dimensiones faltantes |
| `E_MATERIA_YA_CERRADA` | La materia ya esta en SOLO_LECTURA | HTTP 409 |
| `E_RBAC_VIOLATION` | Docente no asignado a esta materia | HTTP 403 |

## 4. Guardrails

- MUST: validar que el output cumple el schema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: emitir `MateriaCarradaEvent` antes de confirmar la persistencia exitosa.
- MUST NOT: exponer secretos ni credenciales en el context.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-002` | PR-UC-002 | `dev-agent` | Contrato endpoint `POST /api/v1/materias/{id}/cierre` |
| ADR | `ADR-0004` (Spring Events async) | PR-UC-002 | `dev-agent` | Contrato del evento `MateriaCarradaEvent` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: Docente autenticado, materia con todos los estudiantes con notas completas.
- **Output esperado**: HTTP 200, materia en estado `SOLO_LECTURA`, `MateriaCarradaEvent` publicado.

### 6.2 Caso borde
- **Input**: Materia con exactamente 1 estudiante con 1 dimensión sin nota.
- **Output esperado**: HTTP 409 `E_COMPLETITUD_FALLIDA`, lista con el estudiante y dimensión faltante.

### 6.3 Caso adversarial
- **Input**: Intento de cerrar una materia ya en `SOLO_LECTURA`.
- **Comportamiento esperado**: HTTP 409 `E_MATERIA_YA_CERRADA`; el estado no cambia.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
