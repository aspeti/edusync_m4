# PR-UC-005 — Contrato de UC-05: Modificación retroactiva con ventana temporal

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-UC-005` |
| Título | Contrato técnico de UC-05: Autorización jerárquica de modificación retroactiva |
| Artefacto origen | FSD |
| ID origen | `FSD-UC-005` |
| Tipo de prompt | transformación |
| Modelo recomendado | Opus |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Backend Engineer especializado en sistemas de autorizacion
jerarquica, modelos append-only, ventanas temporales con revocacion automatica
y auditoria inmutable en Spring Boot 3 con PostgreSQL.
```

### 1.2 Task
```text
Genera el contrato tecnico del flujo completo de UC-05 (Autorizacion jerarquica
de modificacion retroactiva con ventana temporal), desde la solicitud del docente
hasta la revocacion automatica al expirar la ventana.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-05.
- Actores: Docente (solicitante), Director (autorizador).
- Alcance de la autorizacion: estudiante especifico (RUDE) o curso completo.
  El Director puede restringir el alcance. El docente no puede ampliarlo.
- Ventana temporal OBLIGATORIA: rango 1h-72h. Default: 24h.
  No existe autorizacion indefinida. Sistema rechaza aprobacion sin ventana.
- Al expirar: revocacion automatica sin intervencion manual.
  Alerta al docente cuando faltan 30 minutos.
- Modelo de persistencia: append-only. El registro original NUNCA se sobreescribe.
  Cada correccion genera un nuevo registro versionado con referencia al anterior.
- El centralizador provisional (UC-03) se recalcula en cada cambio de la ventana.
- Triple entrada en audit_log: (1) solicitud docente, (2) decision director,
  (3) cierre de ventana con inventario de cambios.
```

### 1.4 Reasoning
```text
1. Definir los estados de la solicitud: PENDIENTE → APROBADA/RECHAZADA → EXPIRADA.
2. Especificar el schema de la solicitud del docente
   (materia, justificacion, alcance: RUDE o CURSO, dimension, indice_evaluacion).
3. Definir la respuesta del Director (alcance_efectivo, duracion_horas).
4. Especificar el modelo append-only de registro de correcciones.
5. Definir el job de revocacion automatica (scheduler) y las alertas.
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: estados de la solicitud, schema de autorizacion,
modelo append-only, revocacion automatica, triple audit_log y 3 casos de prueba.
```

### 1.6 Output
```text
Markdown con: diagrama de estados de la solicitud (texto), schema de request
del docente, schema de decision del Director, modelo de registro append-only
y 3 casos de prueba (aprobacion, rechazo, ventana expirada).
```

## 2. Invariantes del prompt

- No existe autorizacion sin ventana temporal definida.
- El Director no puede aprobar con duracion fuera del rango 1h–72h.
- El docente no puede ampliar el alcance recibido del Director.
- El registro original es inmutable. Solo se crea un nuevo registro versionado.
- La revocacion al expirar es automatica; no requiere accion del Director.
- Las validaciones de rango de UC-01 permanecen activas durante la ventana.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_VENTANA_NO_DEFINIDA` | Director intenta aprobar sin duracion | HTTP 400 |
| `E_ALCANCE_EXCEDIDO` | Docente intenta modificar fuera del alcance autorizado | HTTP 403, registrar intento en audit_log |
| `E_VENTANA_EXPIRADA` | La ventana vencio | HTTP 409, redirigir a nueva solicitud |
| `E_REGISTRO_INMUTABLE` | Intento de UPDATE sobre registro original | Rechazar, forzar modelo append-only |

## 4. Guardrails

- MUST: validar que el output cumple el schema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: permitir UPDATE sobre el registro original de calificación (solo INSERT con `registro_padre_id`).
- MUST NOT: almacenar PII en logs del prompt.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-005` | PR-UC-005 | `dev-agent` | Contrato flujo modificación retroactiva |
| ADR | `ADR-0003` (append-only, audit_log) | PR-UC-005 | `dev-agent` | Modelo de persistencia del registro versionado |
| BRD | `BR-005` (append-only), `BR-009` (ventana 1-72h) | PR-UC-005 | `dev-agent` | Invariantes del flujo de autorización |

## 6. Pruebas del prompt

### 6.1 Caso feliz (aprobación)
- **Input**: Docente solicita corrección para RUDE `1234567`, Director aprueba con `duracion_horas: 24`.
- **Output esperado**: solicitud `APROBADA`, nueva calificación con `registro_padre_id`, triple `audit_log`, scheduler registrado.

### 6.2 Caso borde (ventana expirada)
- **Input**: Docente intenta modificar cuando la ventana ya expiró.
- **Output esperado**: HTTP 409 `E_VENTANA_EXPIRADA`; alerta al Docente indicando que debe crear una nueva solicitud.

### 6.3 Caso adversarial
- **Input**: Docente intenta modificar un RUDE no incluido en el alcance autorizado por el Director.
- **Comportamiento esperado**: HTTP 403 `E_ALCANCE_EXCEDIDO`; el intento queda registrado en `audit_log`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Opus |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
