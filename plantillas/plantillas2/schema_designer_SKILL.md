---
name: event-schema-designer
description: >
  Diseña el contrato (schema) Avro, Protobuf o JSON Schema de un
  Integration Event a partir del Domain Event interno y la decisión
  de broker. Aplica narrow types, política de compatibility
  (backward/forward/full) y publica en Schema Registry. Cubre
  convenciones de schema design para Integration Events.
  Literatura recomendada: Bellemare Cap 3 y 7.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: event-schema-designer (contrato de Integration Event)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/event-schema-designer/` o a
> `.claude/skills/event-schema-designer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: derivación del schema de un evento crítico del catálogo, redacción de `docs/events/schemas/`, integración con Schema Registry.
- ARRANCA cuando: el usuario invoca `"@event-schema-designer <evento>"`, abre un `.avsc` / `.proto` / `.json` vacío, o pide "genera el schema Avro del integration event X".
- NO ACTIVAR cuando: el evento aún no existe en `docs/events/catalog.md` (correr antes `event-catalog-author`).

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- Nombre del **Integration Event** (formato `<Entidad><AcciónEnPasado>IntegrationEvent`).
- **Domain event origen** o descripción de qué cambia (campos internos del aggregate).
- **Bounded context productor** y **bounded contexts consumidores**.
- **Broker decidido** (Kafka/MSK, Kinesis, SQS, EventBridge, RabbitMQ...) — define el formato preferido (Avro para Kafka/MSK; Protobuf para Kinesis o gRPC; JSON Schema para SQS/EventBridge/SNS).
- Política de evolución preferida si la hay (backward por defecto en Kafka).

Si falta cualquiera, responder: `"Necesito el nombre del evento, el domain event origen, los contextos productor/consumidor y el broker antes de generar el schema."`

## 3. Fuentes de verdad (orden de precedencia)

1. Catálogo de eventos del producto (`docs/events/catalog.md`).
2. Domain event interno del bounded context productor.
3. ADR de broker (`docs/adr/0002-broker.md`) — fija el formato y registry.
4. Diccionario de tipos del FSD §6 (alinearse a tipos existentes: `Money`, `Address`, `Timestamp`).

## 4. Procedimiento

1. **Verificar inputs**. Si el evento no está en el catálogo, STOP.
2. **Identificar campos cross-boundary**: del domain event interno, llevar solo lo que los consumidores externos necesitan. Cero exposición del modelo interno completo (anti-patrón "Payload gigante").
3. **Aplicar narrow types**:
   - ISO-8601 timestamps (`logicalType: timestamp-millis` en Avro).
   - UUID explícito (`logicalType: uuid`).
   - Enums (status, currency, language) en vez de string libre.
   - Money types con `amount` (long) + `currency` (enum ISO-4217), no `double`.
   - Address como sub-record explícito, no `string`.
4. **Decidir opcional vs requerido** por campo:
   - Requerido (`type: ["..."]` sin default) si forma parte de la identidad del evento.
   - Opcional con default (`["null", "..."], default: null` en Avro) si puede ser ausente en algunas variantes.
5. **Elegir formato según broker**:
   - Kafka / MSK + Schema Registry → Avro (`.avsc`).
   - Kinesis + Glue Schema Registry → Avro o Protobuf.
   - gRPC sync → Protobuf (`.proto`).
   - SQS / SNS / EventBridge → JSON Schema (`.json`).
6. **Declarar política de compatibility**:
   - **Backward** (default Kafka): consumidores se actualizan antes que productores. Útil con un solo productor y muchos consumidores.
   - **Forward**: productor se actualiza antes que consumidores. Útil con muchos productores → un consumer central.
   - **Full**: cualquier orden. Solo si despliegues son completamente independientes.
7. **Justificar cada campo** con su origen (domain event, FSD §6, NFR).
8. **Generar el schema** y guardarlo en `docs/events/schemas/<evento>.<avsc|proto|json>`.

## 5. Salida esperada

Dos artefactos:

- Archivo de schema en `docs/events/schemas/<evento>.avsc` (o `.proto` / `.json`):

```text
# (Avro) — comentario inicial obligatorio:
# Productor único: <bounded context>
# Consumidores: <lista>
# Política de compatibility: backward | forward | full
# Versión: 1.0.0
# Referencias: docs/events/catalog.md, docs/FSD.md §6
```

- Tabla justificando cada campo:

| Campo | Tipo | Requerido | Default | Origen / justificación |
|-------|------|-----------|---------|------------------------|
| orderId | UUID | sí | — | Identificador del aggregate (FSD §6.1) |
| restaurantId | UUID | sí | — | Necesario para Kitchen y Notifications |
| items | Array<OrderItem> | sí | — | Necesario para Kitchen (qué preparar) |
| totalAmount | Money(long amount + enum currency) | sí | — | Necesario para Billing (auditoría); narrow type |
| deliveryAddress | Address (sub-record) | sí | — | Necesario para Delivery |
| createdAt | Timestamp (millis, UTC) | sí | — | Auditoría regulatoria (NFR-07) |
| promoCode | string | no | null | Opcional; permitir evolución backward sin romper |

## 6. Verificación (criterios de "bien hecho")

- Cero campos `string` para conceptos con dominio cerrado (status, currency, language); cero `double` para money.
- 100 % de campos opcionales con `default` declarado.
- Política de compatibility documentada explícitamente en el comentario inicial.
- Schema validado: para Avro, parseable con `avro-tools`; para Protobuf, compila con `protoc`; para JSON Schema, valida con `ajv` o equivalente.
- Productor único declarado (Single Writer Principle).
- Schema sub-≤ 30 campos top-level (síntoma de payload gigante si excede).

## 7. Anti-patrones específicos

- **Reexponer el aggregate completo**: rompe encapsulación del bounded context. Mitigación: solo campos cross-boundary necesarios.
- **`string` para todo**: pérdida de invariantes. Mitigación: enums, narrow types.
- **Quitar un campo requerido sin alias**: breaking change. Mitigación: marcar deprecated y mantener por al menos 1 release.
- **Cambiar tipo de un campo** (`string → int`): breaking. Mitigación: nuevo campo + deprecación del viejo.
- **No declarar política de compatibility**: deja al Schema Registry sin guardrail; el primer breaking change rompe consumidores. Mitigación: declarar explícitamente.
- **Renombrar sin alias**: rompe consumidores que esperaban el nombre viejo. Mitigación: usar `aliases` en Avro o `reserved` en Protobuf.

## 8. Mini ejemplo de invocación

> "Necesito el schema Avro del evento `OrderCreatedIntegrationEvent`. Domain event interno: `Order` (id, items, total, address, status, createdAt). Productor: Orders. Consumidores: Kitchen, Billing, Notifications, Analytics. Broker MSK. Compatibility backward. Usa el skill `event-schema-designer`."

## 9. Modos de fallo conocidos

- El usuario pide effectively-once + schema breaking → STOP, recordar que cambiar a un schema breaking requiere período de coexistencia y bumping de versión major, no es un patch.
- El consumidor exige un campo que el productor no tiene en su domain event → STOP, el consumidor está sobre-acoplándose al productor; rediseñar.
- El producto está en JSON Schema pero el broker es Kafka/MSK con Schema Registry → STOP, recomendar Avro o Protobuf por eficiencia.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 20/05/2026  | M.Sc. Edson Terceros   | versión inicial; cubre diseño de schemas para Integration Events |
