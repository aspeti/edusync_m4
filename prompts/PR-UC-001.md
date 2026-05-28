# PR-UC-001 — Contrato de UC-01: Registro de calificaciones

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-UC-001` |
| Título | Contrato técnico de UC-01: Registro descentralizado de calificaciones por dimensión |
| Artefacto origen | FSD |
| ID origen | `FSD-UC-001` |
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
Eres un Senior Backend Engineer especializado en Java 21, Spring Boot 3 y
sistemas academicos con RBAC estricto para entornos multitenant.
```

### 1.2 Task
```text
Genera el contrato tecnico del endpoint POST /api/v1/calificaciones para
el caso de uso UC-01 (Registro descentralizado de calificaciones por dimension),
incluyendo schema de request/response, validaciones, invariantes y pruebas.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-01.
- Actores: Docente (JWT con rol DOCENTE).
- Dimensiones activas: Ser / Saber / Hacer / Decidir (+ Autoevaluacion parametrica).
- Tipo de nota: REGULAR o AYUDA (regla de combinacion parametrica por tenant+periodo).
- Escala de ingreso: 0–100 (cruda). La conversion a escala SIE es exclusiva de UC-03.
- Restricciones:
  * BR-RUDE: identificacion de estudiante solo por codigo RUDE.
  * BR-RBAC: el docente solo escribe en sus materias asignadas.
  * BR-PERIODO: solo se acepta si el periodo esta en estado ABIERTO.
  * BR-RANGO: el valor debe estar dentro del rango parametrico de la dimension.
- Stack: Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA.
```

### 1.4 Reasoning
```text
1. Definir el schema JSON del request (RUDE, materia_id, periodo_id, dimension,
   tipo_nota, valor, indice_evaluacion).
2. Especificar las validaciones en orden: autenticacion JWT → RBAC → estado
   del periodo → rango parametrico → persistencia.
3. Definir el schema de response exitoso (201) y de errores (400, 403, 409).
4. Declarar las entradas en el audit_log generadas por cada llamada exitosa.
5. Verificar que la conversion de escala NO ocurre en este endpoint.
```

### 1.5 Stop condition
```text
Detente cuando el contrato tenga: schema request, schema response, 3 codigos
de error con descripcion, invariantes verificables y 3 casos de prueba.
```

### 1.6 Output
```text
Markdown con: schema OpenAPI simplificado, tabla de validaciones en orden,
tabla de codigos de respuesta y 3 casos de prueba (feliz, borde, adversarial).
```

## 2. Invariantes del prompt

- El campo `valor` debe rechazarse si excede el rango parametrico del periodo.
- El campo `RUDE` es obligatorio; no se acepta nombre ni numero de lista.
- El response exitoso debe incluir el promedio provisional recalculado del estudiante.
- Toda llamada exitosa genera una entrada en `audit_log` (inmutable).
- La conversion de escala SIE no ocurre en este endpoint.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_PERIODO_NO_MODIFICABLE` | Periodo CERRADO o SOLO_LECTURA | HTTP 409 |
| `E_RBAC_VIOLATION` | Docente sin asignacion en la materia | HTTP 403 |
| `E_NOTA_FUERA_DE_RANGO` | Valor fuera del rango parametrico | HTTP 400 |
| `E_RUDE_INVALIDO` | RUDE nulo, vacio o con formato incorrecto | HTTP 400 |
| `E_MISSING_CONTEXT` | Falta periodo_id o materia_id en el request | HTTP 400 |

## 4. Guardrails

- MUST: validar que el output cumple el schema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: exponer RUDE en logs ni en rutas URL (solo en body cifrado).
- MUST NOT: calcular la escala SIE en este endpoint (exclusivo de UC-03).

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-001` | PR-UC-001 | `dev-agent` | Contrato endpoint `POST /api/v1/calificaciones` |
| BRD | `BR-001` (RBAC), `BR-002` (rango), `BR-004` (RUDE) | PR-UC-001 | `dev-agent` | Validaciones del contrato |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: Docente autenticado, periodo ABIERTO, RUDE válido, valor dentro del rango paramétrico (ej. `{"rude":"1234567","materia_id":"uuid","periodo_id":"uuid","dimension":"SABER","tipo_nota":"REGULAR","valor":75}`).
- **Output esperado**: HTTP 201, response con `calificacion_id`, `promedio_provisional`, `audit_log_id`.

### 6.2 Caso borde
- **Input**: valor exactamente en el límite del rango paramétrico (ej. `valor: 100`).
- **Output esperado**: HTTP 201 si el rango máximo incluye 100; HTTP 400 con `E_NOTA_FUERA_DE_RANGO` si lo excede.

### 6.3 Caso adversarial
- **Input**: request con `nombre_estudiante` en lugar de `rude`.
- **Comportamiento esperado**: rechazo con `E_RUDE_INVALIDO` (HTTP 400); el nombre no se procesa ni se registra.

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
