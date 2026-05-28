# PR-UC-004 — Contrato de UC-04: Exportación y sincronización al SIE

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-UC-004` |
| Título | Contrato técnico de UC-04: Exportación y sincronización masiva al SIE |
| Artefacto origen | FSD |
| ID origen | `FSD-UC-004` |
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
Eres un Senior Integration Engineer especializado en integraciones con sistemas
gubernamentales bolivianos, patrones de resiliencia (circuit breaker, idempotencia)
y Spring Boot 3 con AWS SQS para procesamiento asincrono tolerante a fallos.
```

### 1.2 Task
```text
Genera el contrato tecnico del proceso de exportacion masiva al SIE (UC-04),
incluyendo el filtro pre-exportacion obligatorio, el esquema de idempotencia
por RUDE+periodo_id, el manejo de fallos parciales y el reporte de resultado.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-04, DA-05.
- Actor: Secretaria/Administrativo.
- Prerequisito: todos los centralizadores del periodo en estado CERRADO.
- Vinculacion al SIE: exclusivamente por RUDE. Nunca por nombre ni posicion.
- Filtro pre-exportacion OBLIGATORIO: descartar filas con RUDE nulo/invalido
  y filas con nota nula en cualquier dimension requerida. Reportar como
  EXCLUIDAS_SIN_RUDE o EXCLUIDAS_NOTA_INCOMPLETA (nunca enviar valor 0).
- Idempotencia: clave compuesta RUDE + periodo_id. Evita duplicados en reintentos.
- Resiliencia: estado de exportacion persistido registro a registro (DA-05).
  Al fallar el SIE, el proceso reanuda desde el ultimo exitoso.
- Stack: Java 21, Spring Boot 3, resilience4j (circuit breaker), AWS SQS.
```

### 1.4 Reasoning
```text
1. Definir el flujo completo: validar prerequisitos → filtrar → construir payload
   → enviar por RUDE → persistir estado → reportar resultado.
2. Especificar el schema del payload SIE (parametrico, actualizable sin redespliegue).
3. Definir los 3 estados de exportacion por estudiante: PENDIENTE / ENVIADO / FALLIDO.
4. Especificar el proceso de reintento: solo registros FALLIDO o PENDIENTE.
5. Definir el reporte de resultado (enviados, fallidos, excluidos con razon).
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: filtro pre-exportacion, idempotencia,
estados de exportacion, manejo de fallo parcial SIE y reporte de resultado.
```

### 1.6 Output
```text
Markdown con: diagrama de flujo (texto), schema del payload SIE, tabla de
estados por registro, logica de reintento y 3 casos de prueba.
```

## 2. Invariantes del prompt

- No se puede exportar si alguna materia del periodo esta en estado ABIERTO.
- El RUDE nulo o invalido NUNCA se envia al SIE con valor 0.
- La clave de idempotencia `RUDE + periodo_id` previene duplicados en reintentos.
- El fallo parcial del SIE no reinicia el proceso desde cero.
- El formato de exportacion es parametrico (sin redespliegue ante cambios del SIE).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_PERIODO_NO_CERRADO` | Existen materias ABIERTAS en el periodo | HTTP 409 |
| `E_SIE_TIMEOUT` | El servidor SIE no responde | Persistir FALLIDO, activar circuit breaker, programar reintento |
| `E_RUDE_INVALIDO_PAYLOAD` | RUDE invalido en el payload construido | Excluir registro, reportar en EXCLUIDAS_SIN_RUDE, continuar |
| `E_PAYLOAD_INVALIDO` | El formato SIE cambio sin actualizacion del parametro | STOP, alertar a Secretaria y Administrador tecnico |

## 4. Guardrails

- MUST: validar que el output cumple el schema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: exponer RUDE en logs ni en rutas URL (solo en body HTTPS cifrado).
- MUST NOT: almacenar PII en logs del prompt ni en registros de error.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-004` | PR-UC-004 | `dev-agent` | Contrato proceso exportación SIE |
| ADR | `ADR-0005` (Resilience4j, idempotencia RUDE+periodo_id) | PR-UC-004 | `dev-agent` | Estrategia de resiliencia del adaptador SIE |
| BRD | `BR-004` (RUDE única clave), `BR-006` (fallo parcial no reinicia) | PR-UC-004 | `dev-agent` | Invariantes del proceso de exportación |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: periodo con todos los centralizadores `CERRADOS`, 80 estudiantes con RUDE válido y notas completas.
- **Output esperado**: 80 registros `ENVIADO`, reporte con `enviados: 80`, `fallidos: 0`, `excluidos: 0`.

### 6.2 Caso borde
- **Input**: periodo con 79 estudiantes válidos y 1 con RUDE nulo.
- **Output esperado**: 79 `ENVIADO`, 1 `EXCLUIDAS_SIN_RUDE`; el SIE recibe exactamente 79 registros.

### 6.3 Caso adversarial
- **Input**: SIE devuelve HTTP 503 en el registro 47 de 80.
- **Comportamiento esperado**: registros 1–46 quedan `ENVIADO`, registro 47 queda `FALLIDO`, circuit breaker se activa; en siguiente ciclo reanuda desde el 47.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `sie_error_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
