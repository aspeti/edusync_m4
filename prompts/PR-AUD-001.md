# PR-AUD-001 — Auditoría de trazabilidad y modelo de audit_log

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-AUD-001` |
| Título | Esquema del modelo de auditoría de EduSync (audit_log) con cobertura de UC críticos |
| Artefacto origen | FSD + arquitectura funcional |
| ID origen | `DA-03`, `UC-01`, `UC-02`, `UC-04`, `UC-05` |
| Tipo de prompt | auditoría |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Borrador |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior QA Architect especializado en auditoria de sistemas criticos,
modelos de datos inmutables y verificacion de trazabilidad en aplicaciones
Java/Spring con requisitos legales de Bolivia.
```

### 1.2 Task
```text
Genera el esquema del modelo de auditoria de EduSync (audit_log), verificando
que toda operacion critica (registro, cierre, modificacion retroactiva, exportacion
SIE) genera una entrada completa, inmutable y trazable al actor, artefacto y
timestamp correspondiente.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §DA-03, UC-01, UC-02, UC-04, UC-05.
- Operaciones auditadas:
  * UC-01: cada nota registrada (actor, dimension, tipo, valor_nuevo).
  * UC-02: cierre de materia (actor, materia_id, periodo_id, timestamp).
  * UC-04: exportacion SIE (actor, periodo_id, registros_enviados/fallidos).
  * UC-05: triple entrada (solicitud docente, decision director, cierre ventana).
- Campos mínimos: usuario_id, tenant_id, accion, entidad_afectada,
  valor_anterior, valor_nuevo, timestamp_utc, ip_origen, prompt_id (si aplica).
- Restriccion: registros inmutables, sin UPDATE ni DELETE.
```

### 1.4 Reasoning
```text
1. Definir el schema completo de la tabla audit_log.
2. Especificar qué operaciones son auditadas y con qué campos en cada UC.
3. Verificar la cobertura: ningún UC crítico puede quedar sin auditoría.
4. Definir las políticas de retención y acceso (solo lectura para todos).
5. Generar 3 casos de prueba que validen la inmutabilidad.
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: schema de audit_log, cobertura por UC,
política de inmutabilidad y 3 casos de prueba de auditoría.
```

### 1.6 Output
```text
Markdown con: schema de audit_log (tabla de campos), matriz de cobertura
UC → entradas audit_log, política de acceso y 3 casos de prueba.
```

## 2. Invariantes del prompt

- Todo registro en `audit_log` es inmutable: no UPDATE, no DELETE.
- El campo `tenant_id` es obligatorio en cada entrada.
- El campo `timestamp_utc` es generado por el servidor, no por el cliente.
- La cobertura de auditoría debe ser del 100% de las operaciones de escritura.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_AUDIT_FALTANTE` | Operación crítica sin entrada en audit_log | Fallo de cobertura |
| `E_AUDIT_MUTABLE` | Intento de UPDATE/DELETE sobre audit_log | Rechazar con `AuditImmutabilityViolation` |
| `E_TIMESTAMP_CLIENTE` | Timestamp del cliente | Rechazar, usar servidor UTC |

## 4. Guardrails

- MUST: verificar cobertura 100% de operaciones de escritura.
- MUST NOT: permitir UPDATE ni DELETE sobre `audit_log`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD + arquitectura funcional | `DA-03`, `UC-01`, `UC-02`, `UC-04`, `UC-05` | PR-AUD-001 | `qa-agent` | Schema `audit_log` + matriz de cobertura |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: FSD con los 4 UCs que generan audit_log (UC-01, UC-02, UC-04, UC-05).
- **Output esperado**: schema completo de 9 campos, matriz de cobertura con 100% de operaciones.

### 6.2 Caso borde
- **Input**: FSD sin documentar explícitamente la triple entrada de UC-05.
- **Output esperado**: el agente infiere las 3 entradas de UC-05 desde el flujo de estados.

### 6.3 Caso adversarial
- **Input**: propuesta de DELETE de entradas vencidas en audit_log.
- **Comportamiento esperado**: rechazo con `E_AUDIT_MUTABLE`; la política de retención usa archivado, no DELETE.

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
| Rodrigo Aspeti | 28/05/2026 | borrador | Pendiente de validación formal por qa-agent |
