---
name: event-catalog-author
description: >
  Construye y mantiene el catálogo de eventos del producto en
  `docs/events/catalog.md` y los schemas en `docs/events/schemas/`.
  Aplica la taxonomía Unkeyed/Entity/Keyed, el Single Writer
  Principle y los anti-patrones de eventos. Requiere bounded
  contexts y UCs cross-context del FSD ya definidos. No inventa
  eventos fuera del FSD. Literatura recomendada: Bellemare Cap 2.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: event-catalog-author (catálogo de eventos + schemas semilla)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/event-catalog-author/` (alcance usuario)
> o a `.claude/skills/event-catalog-author/` en la raíz del repo del grupo
> (alcance proyecto).

## 1. Cuándo activarlo (triggers)

- DURANTE: tarea para casa 1 de S09, llenado del DTI §7, diseño del catálogo de eventos del producto grupal o del examen.
- ARRANCA cuando: el usuario invoca `"@event-catalog-author <ruta FSD o lista de UCs>"`, abre `docs/events/catalog.md`, o pide "definir el catálogo de eventos".
- NO ACTIVAR cuando: el FSD aún no existe; el catálogo es una salida derivada del FSD, no un descubrimiento de negocio.

## 2. Entradas obligatorias

El usuario MUST proporcionar al menos una de:

- Ruta a `docs/FSD.md` con los UCs cross-context identificados.
- Lista de bounded contexts del producto y los UCs críticos que cruzan frontera.
- Pegado del fragmento del FSD relevante (UCs + postcondiciones).

Si falta cualquiera, responder: `"Necesito el FSD o la lista de bounded contexts antes de construir el catálogo de eventos. Lista mínima: <bounded contexts, UCs cross-context>."`

## 3. Fuentes de verdad (orden de precedencia)

1. UCs del FSD y sus postcondiciones (un evento nace de un UC que cambia estado relevante para otro contexto).
2. Bounded contexts y mapa estratégico del DTI §6.
3. `AGENTS.md` del repo del producto (si existe) y ADRs vigentes (broker elegido, política de schema).
4. Código existente (eventos ya emitidos en el repo, no duplicar).

## 4. Procedimiento

1. **Verificar inputs**. Si faltan UCs cross-context, STOP.
2. **Extraer candidatos**: por cada UC del FSD que modifica estado relevante para otro bounded context, proponer un Integration Event candidato con nombre `<Entidad><AcciónEnPasado>IntegrationEvent`.
3. **Clasificar cada evento** según taxonomía:
   - **Unkeyed**: hecho sin clave (logs, métricas, click-stream). Sin ordering por entidad.
   - **Entity**: estado completo de la entidad tras el cambio. Compactable por clave.
   - **Keyed**: hecho atómico relativo a una clave (delta o comando).
4. **Asignar productor único** (Single Writer Principle). Si dos servicios pretenden escribir el mismo evento → reorganizar o crear streams separados.
5. **Definir payload con tipos narrow**: enums, money types, ISO 8601 dates, UUIDs explícitos. Rechazar `String` para todo.
6. **Declarar garantía e idempotency key** por evento: at-least-once + clave de idempotencia (`eventId` UUID + `entityId` + `version`); effectively-once solo si está justificado por ADR.
7. **Validar contra anti-patrones de eventos**: eventos semáforo, Frankenstein, payload gigante, nombres genéricos. Rechazar y reescribir.
8. **Generar schemas semilla** para al menos 2 eventos críticos: Avro (`.avsc`) si el broker es Kafka/MSK con Schema Registry; JSON Schema en otros casos. Declarar política de compatibility backward/forward/full.
9. **Producir tabla del catálogo** y emitir trazabilidad a UCs y bounded contexts.

## 5. Salida esperada

Dos artefactos:

- `docs/events/catalog.md` con la siguiente tabla:

| Evento | Tipo (Unkeyed/Entity/Keyed) | Bounded context productor | Bounded contexts consumidores | Payload (campos narrow) | Garantía | IdempotencyKey | UC origen |
|--------|-----------------------------|---------------------------|-------------------------------|-------------------------|----------|----------------|-----------|
| OrderCreatedIntegrationEvent | Entity | Orders | Kitchen, Billing, Notifications, Analytics | orderId:UUID, restaurantId:UUID, items:Array<OrderItem>, totalAmount:Money, deliveryAddress:Address, createdAt:Timestamp | at-least-once | orderId+version | UC-01 |
| PaymentChargedIntegrationEvent | Keyed | Billing | Orders, Notifications | paymentId:UUID, orderId:UUID, amount:Money, status:enum, chargedAt:Timestamp | at-least-once | paymentId | UC-04 |

- `docs/events/schemas/<evento>.avsc` (o `.json`) con al menos 2 schemas concretos y nota de compatibility:

```text
# Política de compatibility: backward
# Productor único: Orders bounded context
# Consumidores: Kitchen, Billing, Notifications, Analytics
```

## 6. Verificación (criterios de "bien hecho")

- ≥ 6 eventos en el catálogo, cada uno mapeado a un UC del FSD.
- 100 % de eventos con productor único (sin dos contextos escribiendo el mismo stream).
- 100 % de eventos con clasificación taxonómica correcta y justificada.
- 100 % de eventos con `IdempotencyKey` declarada.
- ≥ 2 schemas Avro/JSON Schema en `docs/events/schemas/` con política de compatibility explícita.
- Cero violaciones de los anti-patrones de eventos (sin "DataChanged" ni payloads > 1 MB ni eventos semáforo).
- Trazabilidad explícita Evento → UC → bounded context cubierta en la tabla.

## 7. Anti-patrones específicos

- **Evento semáforo**: `SomethingHappened` sin payload útil. Mitigación: que el payload lleve todo lo necesario para el consumidor sin tener que volver a llamar al productor.
- **Frankenstein**: 2+ conceptos en el mismo schema con campos opcionales tipo `if`. Mitigación: separar en eventos distintos.
- **Payload gigante**: > 1 MB con todo el dominio. Mitigación: llevar solo el delta + foreign key; el consumidor enriquece consultando su read model.
- **Nombre genérico**: `DataChanged`, `Event`, `Update`. Mitigación: nombrar `<Entidad><AcciónEnPasado>` (`OrderShipped`, `PaymentRefunded`).
- **Tipos amplios**: `String` para todo. Mitigación: narrow types (enum, money, ISO dates, UUID).
- **Múltiples productores en un stream**: rompe Single Writer. Mitigación: rearmar la topología o crear streams separados con un servicio agregador.

## 8. Mini ejemplo de invocación

> "Tengo `docs/FSD.md` con UC-01 a UC-07 cubriendo Orders, Kitchen, Billing y Delivery. Construye el catálogo de eventos del producto en `docs/events/catalog.md` y los schemas Avro de los 2 eventos más críticos. Usa el skill `event-catalog-author`."

## 9. Modos de fallo conocidos

- Un UC del FSD modifica estado pero su postcondición no lo dice → STOP, pedir al usuario que actualice la postcondición o cite explícitamente el cambio.
- Dos UCs distintos pretenden producir el mismo evento → STOP, abrir conversación sobre el Single Writer Principle y proponer rearmar la topología.
- El usuario pide effectively-once sin justificación de coste/duplicado → STOP, recordar el costo y la complejidad operativa de effectively-once y pedir ADR específico.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 20/05/2026  | M.Sc. Edson Terceros   | versión inicial; catálogo de eventos + schemas semilla para arquitecturas asíncronas |
