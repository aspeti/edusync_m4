---
name: broker-selector
description: >
  Selecciona el broker correcto (conceptual + AWS) por cada flujo
  asíncrono del producto y produce la tabla de decisiones para
  `docs/DTI.md §7` y para el ADR de mensajería. Aplica matriz
  síntesis de selección, árbol conceptual de decisión y matriz de
  servicios AWS de mensajería. Requiere el catálogo de flujos
  asíncronos y sus criterios (replay, ordering, throughput,
  retention, fan-out, latencia).
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: broker-selector (decisión de broker por flujo)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/broker-selector/` o a
> `.claude/skills/broker-selector/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: llenado del DTI §7, redacción del ADR de mensajería (ADR-0002 típico), revisión arquitectónica del producto grupal o de la entrega del examen.
- ARRANCA cuando: el usuario invoca `"@broker-selector <lista de flujos>"`, abre `docs/adr/0002-broker.md`, o pide "selecciona el broker para mis flujos asíncronos".
- NO ACTIVAR cuando: aún no hay catálogo de eventos (correr antes `event-catalog-author`).

## 2. Entradas obligatorias

El usuario MUST proporcionar al menos una de:

- Lista de **flujos asíncronos** (catálogo de eventos por flujo, o lista narrativa con caso de uso por flujo).
- Para cada flujo: criterios duros (replay sí/no, ordering por entidad/global/none, throughput estimado, retention requerida, fan-out 1-a-N o N-a-1, latencia tolerable).
- Restricciones del proyecto: AWS-only sí/no, presupuesto, equipo (capacidad de operar Kafka self-managed), regulación (auditoría con replay).

Si falta cualquiera, responder: `"Necesito la lista de flujos y por cada flujo: replay, ordering, throughput, retention, fan-out. ¿Es AWS-only?"`

## 3. Fuentes de verdad (orden de precedencia)

1. Catálogo de eventos del producto (`docs/events/catalog.md`).
2. NFRs del PRD (latencia p95, throughput pico, auditoría regulatoria).
3. ADRs vigentes (estilo arquitectónico, AWS vs multi-cloud).
4. `AGENTS.md` del repo del producto (si existe; restricciones operativas declaradas).

## 4. Procedimiento

1. **Verificar inputs**. Si faltan criterios duros por flujo, STOP y listar lo que falta.
2. **Por cada flujo**, contestar las 7 preguntas-eje del árbol conceptual de decisión:
   - ¿Necesito replay del histórico? → sí → ramas Kafka/MSK/Kinesis/Pulsar; no → ramas SQS/SNS/RabbitMQ.
   - ¿Es AWS-only? → sí → MSK / Kinesis / SQS / SNS / EventBridge; no → Kafka self-managed / Confluent / Pulsar / RabbitMQ.
   - ¿Multi-tenancy estricto + geo-replication nativa? → Pulsar.
   - ¿Ruteo declarativo por reglas? → RabbitMQ.
   - ¿Task queue zero-ops? → SQS Standard o FIFO.
   - ¿JMS legacy? → ActiveMQ Artemis / Amazon MQ.
   - ¿Edge / footprint mínimo? → NATS JetStream.
3. **Cruzar la elección conceptual con la matriz de servicios AWS de mensajería** si AWS-only:
   - Streaming con replay 24h-365d sin todo Kafka → Kinesis Data Streams.
   - Kafka completo managed → MSK (Provisioned o Serverless).
   - Stream a destino (S3/Redshift/OpenSearch) sin replay queryable → Kinesis Firehose.
   - Fan-out a múltiples consumidores con cero ops → SNS + SQS subscribers.
   - Event bus cross-account / SaaS partners / low-code → EventBridge.
   - Task queue → SQS Standard; orden + dedup → SQS FIFO.
4. **Justificar cada elección contra al menos 2 dimensiones** de la matriz síntesis (replay, ordering, throughput, fan-out, retention, costo, ops). Cero "porque sí".
5. **Detectar conflictos**: si el flujo exige replay + zero-ops + AWS-only, MSK Serverless o Kinesis; si exige ruteo declarativo, RabbitMQ o EventBridge con rules.
6. **Producir tabla de decisiones** y bullet de impacto sobre el DTI §7 y ADR-0002.

## 5. Salida esperada

Tabla obligatoria en `docs/DTI.md §7` o en `docs/adr/0002-broker.md`:

| Flujo | Evento principal | Replay | Ordering | Throughput est. | Fan-out | Broker elegido | Justificación (≥ 2 dimensiones) | Plan B |
|-------|------------------|--------|----------|-----------------|---------|----------------|----------------------------------|--------|
| Saga pedido | OrderCreated, PaymentCharged, CourierAssigned | Sí | Por entidad (orderId) | 200 ev/s pico, 50 sostenido | 1-a-N | MSK | replay para auditoría + fan-out con consumer groups + ecosistema (Schema Registry Glue) | Kinesis DS si MSK supera presupuesto |
| Notificaciones | NotificationRequested | No | None | 500 ev/s | 1-a-3 (email, push, SMS) | SNS + SQS subscribers | fan-out zero-ops + sin necesidad de replay + costo bajo | EventBridge si se requieren reglas |
| Tracking courier | LocationUpdate | 24h | Por entidad (courierId) | 5000 ev/s | 1-a-2 (tracking + analytics) | Kinesis Data Streams | high cardinality + replay corto + AWS-native | Kafka si throughput crece 10x |
| Comandos diferidos | EmailScheduled | No | FIFO por usuario | 100 ev/s | 1-a-1 | SQS FIFO | orden por usuario + dedup + zero-ops | RabbitMQ si se necesita ruteo declarativo |

Plus: 3-4 bullets describiendo trade-offs declarados (costo, ops, lock-in).

## 6. Verificación (criterios de "bien hecho")

- Cada flujo justifica su elección contra **≥ 2 dimensiones** de la matriz.
- Ninguna elección viola las reglas anti-patrón (Kafka como queue de tareas RPC, SQS como event log, EventBridge para throughput sostenido alto).
- Si el producto declaró AWS-only en restricciones, no hay servicios fuera de AWS en la tabla.
- Cada flujo tiene un **Plan B** explícito (resiliencia ante constraints futuras).
- La tabla referencia el evento principal del catálogo (`docs/events/catalog.md`), no inventa nombres.

## 7. Anti-patrones específicos

- **Kafka como queue RPC**: usar particiones para tasks individuales (mal). Mitigación: para tasks usar SQS / RabbitMQ.
- **SQS como event log**: pretender que es source-of-truth (mal: no hay replay). Mitigación: usar Kinesis o MSK.
- **EventBridge como bus de throughput sostenido alto**: mal por costo y latencia. Mitigación: usar MSK / Kinesis.
- **Standard SQS cuando se necesita orden**: producirá out-of-order en producción. Mitigación: SQS FIFO o broker con ordering por partición.
- **Mezclar 4+ brokers distintos sin justificación**: complejidad operativa explota. Mitigación: máximo 2-3 brokers en el producto; consolidar.

## 8. Mini ejemplo de invocación

> "Tengo los siguientes flujos en mi producto: (1) saga del pedido (200 ev/s, replay sí, ordering por orderId, fan-out 1-a-N), (2) notificaciones (500 ev/s, sin replay, fan-out a 3 canales), (3) tracking de couriers (5000 ev/s, replay 24h, ordering por courierId). AWS-only. Aplica el skill `broker-selector` y genera la tabla del DTI §7 y un esqueleto de ADR-0002."

## 9. Modos de fallo conocidos

- Throughput declarado sin unidades (no se sabe si es ev/s, ev/min, GB/h) → STOP, normalizar.
- El usuario exige replay 30+ días en SQS → STOP, recordar que SQS no tiene replay; ofrecer Kinesis o MSK.
- Conflicto irresoluble (zero-ops + replay + multi-cloud) → STOP, escalar a un ADR de plataforma.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 20/05/2026  | M.Sc. Edson Terceros   | versión inicial; cubre DTI §7 selección broker y ADR-0002 |
