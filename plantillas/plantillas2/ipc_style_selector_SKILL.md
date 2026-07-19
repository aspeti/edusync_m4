---
name: ipc-style-selector
description: >
  Selecciona el estilo y la tecnología de Inter-Process
  Communication (IPC) por cada flujo entre microservicios.
  Aplica la matriz de estilos de interacción (one-to-one
  síncrono, one-to-one asíncrono, one-to-many asíncrono), decide
  entre REST + OpenAPI, gRPC + Protocol Buffers, o mensajería
  asíncrona (Kafka / Kinesis / SQS / RabbitMQ), define Service
  Discovery y mitiga Hidden Coupling. Produce la tabla de IPC
  por flujo para el DTI §6.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: ipc-style-selector (IPC por flujo entre servicios)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/ipc-style-selector/` o a
> `.claude/skills/ipc-style-selector/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: diseño del DTI §6 (Arquitectura Distribuida), definición de las interacciones entre microservicios, redacción del ADR de IPC si la decisión es no trivial.
- ARRANCA cuando: el usuario invoca `"@ipc-style-selector"`, abre `docs/DTI.md` cerca de la sub-sección IPC, o pide "cómo se comunican mis servicios" / "REST vs gRPC vs mensajería".
- NO ACTIVAR cuando: los microservicios aún no están identificados (correr antes `monolith-decomposition-architect`); cuando solo hay un servicio (no hay IPC que diseñar).

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- **Lista de pares productor → consumidor** entre microservicios del producto.
- Para cada par: **caso de uso** (qué se intercambia y por qué).
- **Criterios duros por flujo**:
  - Latencia tolerable (sub-100 ms, 1 s, segundos, asíncrono "cuando llegue").
  - Ordering requerido (ninguno, por entidad, global).
  - Fan-out (1-a-1, 1-a-N).
  - Replay del histórico (sí/no).
  - Acoplamiento aceptable (consumidor conoce productor, o ambos conocen un schema, o ambos publican/escuchan eventos sin saber del otro).
- **Restricciones del proyecto**: AWS-only sí/no, lenguajes permitidos, capacidad de operar Kafka self-managed.

Si falta cualquiera, responder: `"Necesito la lista de pares productor-consumidor y, por flujo, latencia tolerable, ordering, fan-out, replay y acoplamiento aceptable."`

## 3. Fuentes de verdad (orden de precedencia)

1. Catálogo de microservicios del DTI §6.
2. NFRs del PRD (latencia p99, throughput, ordering regulatorio).
3. ADRs vigentes (broker elegido, estilo arquitectónico).
4. `AGENTS.md` del repo del producto (si existe; lenguajes, frameworks, restricciones operativas).

## 4. Procedimiento

1. **Verificar inputs**. Si faltan los criterios duros por flujo, STOP.
2. **Aplicar la matriz de estilos de interacción** por flujo:

   | Estilo | Productor sabe del consumidor | Latencia | Fan-out típico | Cuándo usarlo |
   |--------|------------------------------|----------|----------------|---------------|
   | One-to-one **síncrono** (request/response) | Sí, conoce su endpoint | < 1 s | 1-a-1 | Query interna que necesita respuesta inmediata para responder al cliente |
   | One-to-one **asíncrono** (notification con respuesta) | Sí (callback o correlation id) | segundos a minutos | 1-a-1 | Comando con resultado diferido (procesar pago) |
   | One-to-many **asíncrono** (publish-subscribe) | No, publica al broker | segundos a "cuando llegue" | 1-a-N | Domain Event / Integration Event |

3. **Para flujos síncronos**, decidir REST + OpenAPI vs gRPC + Protobuf:
   - **REST + OpenAPI + SemVer**: ecosistema HTTP estándar, debuggable con curl, navegable con browser, CDN caching nativo, versionado por URL (`/v1`, `/v2`). Default para APIs públicas y para mayoría de flujos internos cuando latencia no es ultra-crítica.
   - **gRPC + Protocol Buffers**: streaming bidireccional, binario eficiente, contratos fuertes, latencia ~30 % menor. Para internal east-west traffic en stack polyglot con generación de clientes; NUNCA exponer directo al browser.
4. **Para flujos asíncronos**, decidir entre brokers comunes (la decisión detallada por flujo es propia del skill `broker-selector` si está disponible; este skill solo recomienda la **familia** de broker):
   - **Event broker con replay** (Kafka, AWS MSK, Kinesis Data Streams, Pulsar): cuando se necesita replay del histórico, ordering por entidad, throughput sostenido alto, fan-out 1-a-N.
   - **Message broker sin replay** (RabbitMQ, AWS SQS, ActiveMQ): cuando se necesita task queue / comando con ack, sin replay, ruteo declarativo (RabbitMQ) o zero-ops (SQS).
   - **Event bus serverless** (EventBridge, Google Pub/Sub): cuando el throughput es bajo-medio, se necesita ruteo por reglas, integración con SaaS partners, cero ops.
5. **Definir Service Discovery** para los flujos síncronos:
   - **Server-side LB** (ALB / NLB / K8s Service): los clientes conocen una sola URL estable; el LB sabe dónde están las instancias. Default para mayoría de casos.
   - **Client-side LB con Service Registry** (Consul, Eureka, K8s DNS + headless service): cliente consulta registry y rotea con su política (least-conn, latency-aware). Útil cuando el LB es bottleneck o se necesita rounding más sofisticado.
6. **Mitigar Hidden Coupling** (síncrono entre servicios crea dependencia operativa transitiva):
   - Si A → B → C → D síncronos en cascada → la disponibilidad combinada cae geométricamente. Mitigación: convertir uno o más saltos a async (eventos), introducir CB, o cachear respuestas.
   - Si los servicios cambian de versión a la vez → acoplamiento por versionado. Mitigación: contratos versionados (SemVer en REST, package versionado en Protobuf), tolerancia a campos desconocidos.
7. **Para sync REST/gRPC**, declarar:
   - Versionado (SemVer URL para REST, package + go_package en Protobuf).
   - Timeout (típico 1-3 s para REST público; 100-500 ms para gRPC internal).
   - Política de retry en el cliente (solo en 5xx/timeouts; idempotency obligatorio para POST/PATCH).
8. **Para async**, declarar tema/cola, garantía (at-least-once / effectively-once), particionamiento (por entidad si ordering importa), DLQ.
9. **Producir tabla de IPC por flujo** para el DTI.

## 5. Salida esperada

Tabla obligatoria en `docs/DTI.md` §6:

| Flujo | Productor → Consumidor | Estilo | Tecnología | Justificación (≥ 2 dimensiones) | Versionado / topic | Plan B |
|-------|------------------------|--------|------------|----------------------------------|---------------------|--------|
| Validar dirección | OrderService → GeoService | One-to-one sync | REST + OpenAPI | latencia < 200 ms requerida + caching CDN posible | `/v1/addresses/validate` | Cache local de validaciones recientes si GeoService caído |
| Confirmar pedido a cocina | OrderService → KitchenService | One-to-one sync (low latency) | gRPC + Protobuf | latency-critical < 50 ms, contrato fuerte, mismo cluster | package `kitchen.v1`, binary | Reintento con backoff + CB; al saturar, encolar via SQS |
| Notificar evento de pedido confirmado | OrderService → (todos los interesados) | One-to-many async | Event broker (MSK / Kafka) | replay para auditoría, fan-out 1-a-N, ordering por orderId | topic `orders.events.v1`, key=orderId, at-least-once | Kinesis Data Streams si MSK fuera de presupuesto |
| Cobrar al cliente | OrderService → BillingService | One-to-one async con callback | SQS + correlation id | task queue, retry built-in, zero-ops; latency tolerable de seg a min | queue `billing-charge.fifo` con dedup | RabbitMQ si se requiere ruteo declarativo |
| Enviar email/SMS/push | NotificationService → (3 providers) | One-to-many async | SNS + SQS subscribers por canal | fan-out a 3 canales independientes, sin necesidad de replay | topic `notification-request`; subs email-q, push-q, sms-q | EventBridge si se introducen reglas complejas |

Plus: 2-3 bullets describiendo el riesgo de Hidden Coupling detectado y la mitigación aplicada (CB, async, cache).

## 6. Verificación (criterios de "bien hecho")

- Cada flujo tiene **estilo** explícito (sync / async / pub-sub) y tecnología elegida.
- Cada flujo justifica su elección contra **≥ 2 dimensiones** (latencia, ordering, fan-out, acoplamiento, replay).
- Cada flujo síncrono declara: timeout, política de retry, versionado, idempotency si es POST/PATCH/DELETE.
- Cada flujo asíncrono declara: topic/cola, garantía (at-least-once / effectively-once), particionamiento, DLQ.
- Ningún flujo expone gRPC al browser.
- Ningún flujo síncrono se encadena > 3 saltos sin Circuit Breaker (riesgo Hidden Coupling).
- Cada flujo tiene un **Plan B** documentado (broker o tecnología alternativa).

## 7. Anti-patrones específicos

- **Hidden Coupling**: 4+ servicios síncronos en cascada. Mitigación: convertir parte a async, agregar CB en cada salto, considerar choreography con eventos.
- **REST para comando con efecto colateral importante sin idempotency-key**: el retry duplica el cobro. Mitigación: `Idempotency-Key` header obligatorio en POST/PATCH/DELETE críticos.
- **gRPC expuesto al browser**: no funciona nativamente. Mitigación: gRPC internal + REST/GraphQL en el edge.
- **Sin timeout en cliente HTTP**: hilo bloqueado indefinidamente. Mitigación: timeout < SLA del consumidor.
- **Async fire-and-forget sin DLQ**: mensajes perdidos invisiblemente. Mitigación: DLQ con TTL + alerta.
- **Async sin idempotency en consumer**: duplicados rompen el dominio (cobro doble). Mitigación: idempotency key derivada del evento + tabla `processed_events`.
- **Cambiar el schema sin versionar**: rompe consumidores. Mitigación: REST → bump version mayor; Protobuf → reserved fields + nuevos números; broker → schema registry con compatibility policy.
- **Service Discovery hardcodeado** (URL en config sin Service Discovery): cada nueva instancia requiere redeploy de clientes. Mitigación: DNS-based discovery (K8s Service) o registry (Consul / Eureka).

## 8. Mini ejemplo de invocación

> "Tengo estos flujos en mi producto: (1) OrderService valida dirección con GeoService — latencia < 200 ms; (2) OrderService notifica a KitchenService que prepare pedido — < 50 ms; (3) OrderService emite OrderConfirmed para varios consumidores (Kitchen, Billing, Notifications) — replay requerido, ordering por orderId; (4) OrderService dispara cobro a BillingService — async, callback OK; (5) NotificationService envía email/push/SMS a 3 providers — fan-out async sin replay. AWS-only. Usa el skill `ipc-style-selector` y genera la tabla de IPC para el DTI §6."

## 9. Modos de fallo conocidos

- El usuario insiste en REST sync para todo "porque es más fácil de debuggear" → mostrar el riesgo de Hidden Coupling con un ejemplo de cascada caída; documentar trade-off en el ADR.
- El usuario quiere gRPC para el endpoint público → STOP, recomendar REST o GraphQL en el edge, gRPC solo east-west interno.
- Throughput declarado sin unidades (no se sabe si ev/s o ev/min) → STOP, normalizar a ev/s.
- El usuario pide ordering global en un flujo async multi-consumer → STOP, casi siempre se puede vivir con ordering por entidad; el global es muy caro (1 partición → no escala).

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 21/05/2026  | M.Sc. Edson Terceros   | versión inicial; IPC por flujo (REST/gRPC/async) + Hidden Coupling |
