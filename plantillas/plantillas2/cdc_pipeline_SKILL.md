---
name: cdc-pipeline-designer
description: >
  Diseña el pipeline de Data Liberation para liberar datos de una
  tabla legacy hacia un stream consumible. Decide entre query-based
  loading, CDC binlog (Debezium/AWS DMS) y Outbox tables según 4
  variables clave (acceso al binlog/WAL, modificabilidad del
  servicio, tasa de cambios, captura de DELETEs), declara garantía
  resultante y produce el diagrama Mermaid + tabla de configuración
  del connector.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: cdc-pipeline-designer (Data Liberation legacy → stream)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/cdc-pipeline-designer/` o a
> `.claude/skills/cdc-pipeline-designer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: diseño de la fase Strangler Fig (S08), migración incremental, integración con un legacy que no se reemplaza en el corto plazo, llenado del DTI §7 sub-sección "Data Liberation".
- ARRANCA cuando: el usuario invoca `"@cdc-pipeline-designer <tabla>"`, pide "libera la tabla X al stream", o abre `docs/diagrams/cdc_<tabla>.mmd`.
- NO ACTIVAR cuando: el dato fuente está bajo control del propio servicio nuevo (en ese caso, emitir el evento directamente desde el servicio, no usar CDC).

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- **Motor de BD origen**: MySQL / PostgreSQL / SQL Server / Oracle / DynamoDB / otro.
- **Acceso al binlog/WAL**: sí / no (DBA puede habilitar replication).
- **Modificabilidad del servicio origen**: sí / no (¿puedo agregar tabla outbox y modificar código?).
- **Tasa de cambios estimada**: eventos por segundo o minuto.
- **Necesidad de capturar DELETEs**: sí / no.
- **Broker destino**: Kafka / MSK / Kinesis / otro.

Si falta cualquiera, responder: `"Necesito las 4 variables clave: motor de BD, acceso al binlog/WAL, modificabilidad del servicio origen, tasa de cambios, y si necesitas capturar DELETEs."`

## 3. Fuentes de verdad (orden de precedencia)

1. Esquema de la tabla legacy y su contrato (`docs/legacy-schema.md` o similar).
2. ADR de broker (`docs/adr/0002-broker.md`) — define el destino.
3. NFRs de auditoría y consistencia eventual del PRD.
4. `AGENTS.md` del repo del producto (si existe; restricciones sobre tocar el legacy).

## 4. Procedimiento

1. **Verificar inputs**. Si faltan las 4 variables clave, STOP.
2. **Decidir patrón** según la siguiente tabla de decisión:

   | Acceso binlog/WAL | Puedo modificar servicio origen | Necesito DELETEs | Tasa | Patrón recomendado |
   |---|---|---|---|---|
   | sí | sí | sí | alta | **Outbox + CDC**: máximo control, dual-write resuelto, captura todo |
   | sí | no | sí | media-alta | **CDC binlog (Debezium / AWS DMS)**: requiere replication slot, no toca el servicio |
   | no | sí | sí | media | **Outbox table + worker**: el servicio escribe en outbox transaccionalmente; un worker publica |
   | no | no | no | baja | **Query-based loading**: `SELECT * WHERE updated_at > last_run`, polling periódico; no captura DELETEs |
   | no | sí | no | baja | **Query-based incremental** (`autoincrementing id`) o eventos de aplicación si se puede |

3. **Si CDC binlog**: definir connector (Debezium para Kafka / Kafka Connect; AWS DMS para Kinesis/MSK), configuración mínima (database hostname, slot name, included tables, key columns para particionar el topic), formato de output (Debezium envelope con `op:c|u|d`, before/after).
4. **Si Outbox**: definir esquema de la tabla outbox (`id`, `aggregate_id`, `event_type`, `payload jsonb`, `status enum('pending','sent')`, `created_at`, `sent_at`), worker (Spring Boot scheduled, AWS Lambda con DynamoDB Streams, o Debezium leyendo la outbox table directamente).
5. **Declarar garantía resultante**:
   - **CDC binlog**: at-least-once (puede haber duplicados al reiniciar el connector); consumer debe ser idempotente.
   - **Outbox + worker**: at-least-once (el worker puede publicar antes de marcar `sent` y caer); consumer idempotente obligatorio.
   - **Outbox + CDC sobre la outbox**: at-least-once con orden garantizado por commit timestamp; consumer idempotente.
   - **Query-based**: at-least-once aproximado; cuidado con cambios entre polls.
6. **Plantear estrategia de bootstrap (snapshot inicial)**: Debezium puede snapshot inicial + tail incremental; AWS DMS soporta full + CDC.
7. **Diseñar topic destino**: nombre (`<source>.<table>.v1`), particionamiento (por `entityId` para preservar orden por entidad), retention.
8. **Producir diagrama Mermaid** + tabla de configuración + nota de garantía.

## 5. Salida esperada

Tres artefactos:

- `docs/diagrams/cdc_<tabla>.mmd` — diagrama Mermaid `flowchart` con:
  - Tabla origen.
  - Componente de captura (Debezium / AWS DMS / Outbox worker).
  - Topic destino (con su particionamiento).
  - Consumidores principales.
  - Indicación de bootstrap snapshot vs incremental.
- Tabla de configuración del connector / worker:

| Parámetro | Valor | Justificación |
|-----------|-------|---------------|
| Patrón elegido | CDC binlog (Debezium) | Acceso al binlog disponible + no se modifica el servicio legacy |
| Tabla origen | `legacy.restaurants` | Tabla del catálogo del monolito |
| Topic destino | `ftgo.restaurants.v1` | Naming `<dominio>.<entidad>.v<schema-major>` |
| Particionamiento | `restaurantId` | Preserva orden de cambios por entidad |
| Particiones | 12 | 5x throughput pico estimado + headroom |
| Formato | Avro + Schema Registry (Glue) | Coherente con el broker MSK del ADR-0002 |
| Bootstrap | snapshot initial + tail | Materializar histórico antes del corte |
| Garantía | at-least-once | Consumer debe ser idempotente por `restaurantId + lsn` |

- Nota de garantía + plan de DLQ:

```markdown
**Garantía resultante**: at-least-once. Cada consumer debe deduplicar por `(restaurantId, lsn)` o por `eventId` derivado del binlog position.

**DLQ**: si un mensaje no se puede procesar tras 5 retries con backoff exponencial, va a `ftgo.restaurants.dlq` con metadata (error, attempts, payload). Política de TTL en DLQ: 30 días; archivo posterior a S3.
```

## 6. Verificación (criterios de "bien hecho")

- Patrón elegido coincide con las 4 variables de input (la tabla de decisión está justificada).
- Garantía resultante declarada explícitamente en la nota.
- Topic destino con naming `<dominio>.<entidad>.v<schema-major>`.
- Particionamiento elegido para preservar orden por entidad (si el negocio lo requiere).
- Bootstrap snapshot + tail (o equivalente) declarado para no perder histórico.
- DLQ explícita con TTL y plan de archivo.

## 7. Anti-patrones específicos

- **Dual-write directo** (servicio escribe en DB y en broker en pasos separados): inconsistencia eventual. Mitigación: Outbox + CDC.
- **CDC sin idempotencia downstream**: los reinicios del connector duplican; el consumer rompe. Mitigación: idempotency key `(entityId + lsn)`.
- **Query-based loading con `SELECT *` sin paginar**: revienta la BD origen. Mitigación: incremental + LIMIT.
- **Query-based perdiendo DELETEs**: el legacy borra registros y nadie se entera. Mitigación: CDC o eventos de aplicación.
- **No declarar el bootstrap**: el histórico queda fuera del stream; los nuevos consumidores no pueden reconstruir estado. Mitigación: snapshot inicial obligatorio en Debezium / DMS.
- **Topic destino sin particionamiento por entidad**: out-of-order entre cambios del mismo registro. Mitigación: particionar por `entityId`.

## 8. Mini ejemplo de invocación

> "Quiero liberar la tabla `legacy.restaurants` (MySQL 8, acceso al binlog sí, no puedo modificar el monolito, ~50 cambios/min, debo capturar DELETEs). Broker MSK. Usa el skill `cdc-pipeline-designer`."

## 9. Modos de fallo conocidos

- El DBA no autoriza habilitar replication slots → STOP, ofrecer Outbox table como alternativa si se puede modificar el legacy; si no, query-based con caveats.
- El motor es Oracle y no hay licencia GoldenGate → STOP, evaluar AWS DMS o cambio de patrón a query-based.
- La tabla origen no tiene `primary key` ni timestamp de actualización → STOP, query-based incremental no es viable; pedir adaptación del schema o usar CDC.
- El usuario pide effectively-once cross-region → STOP, recordar que la garantía de CDC binlog es at-least-once; effectively-once requiere transacciones cliente-broker en el consumer.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 20/05/2026  | M.Sc. Edson Terceros   | versión inicial; cubre DTI §7 Data Liberation y patrón Strangler Fig |
