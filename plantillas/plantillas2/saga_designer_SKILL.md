---
name: saga-designer
description: >
  Diseña la saga del flujo crítico del producto en Mermaid
  (`stateDiagram-v2`) y produce el ADR-0004 de orquestación vs
  coreografía. Aplica patrones canónicos de saga distribuida:
  choreography vs orchestration (direct-call o event-driven),
  compensaciones idempotentes y siempre exitosas, timeouts, Wait
  for Task Token, correlation ID. Requiere flujo principal y
  bounded contexts.
allowed-tools:
  - read
  - edit
model-tier: opus
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: saga-designer (saga + ADR orquestación vs coreografía)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/saga-designer/` o a
> `.claude/skills/saga-designer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: tarea para casa 2 de S09, redacción del ADR-0004, llenado del DTI §7 sub-sección "Saga principal".
- ARRANCA cuando: el usuario invoca `"@saga-designer <flujo-crítico>"`, abre `docs/adr/0004-orquestacion-vs-coreografia.md`, o pide "diseña la saga del pedido / inscripción / trámite".
- NO ACTIVAR cuando: el catálogo de eventos aún no existe (correr antes `event-catalog-author`); cuando el flujo no cruza ≥ 2 bounded contexts (no es saga, es UC simple).

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- Nombre y descripción del **flujo crítico** (3-5 líneas).
- Lista ordenada de **pasos del happy path** (mínimo 4 pasos).
- **Bounded contexts involucrados** (mínimo 3).
- Catálogo de eventos del producto (`docs/events/catalog.md`) o pegado parcial.
- Preferencia inicial (si la hay) entre choreography y orchestration, o "el skill decide".

Si falta cualquiera, responder: `"Necesito el flujo, los pasos del happy path y los bounded contexts antes de diseñar la saga. Lista mínima: <flujo, ≥ 4 pasos, ≥ 3 contextos>."`

## 3. Fuentes de verdad (orden de precedencia)

1. Catálogo de eventos del producto (`docs/events/catalog.md`).
2. UCs del FSD que componen el flujo.
3. NFRs del PRD (latencia, throughput pico, tolerancia a fallos, auditoría).
4. ADRs vigentes (broker elegido, estilo arquitectónico).

## 4. Procedimiento

1. **Verificar inputs**. Si el flujo no cruza ≥ 2 contextos, STOP (es un UC simple, no una saga).
2. **Modelar happy path** como secuencia de pasos con su evento principal y su bounded context.
3. **Asignar compensación por paso** según las siguientes reglas:
   - Idempotente (puede ejecutarse 2+ veces con el mismo resultado).
   - Siempre exitosa (si falla, manual operator alert; documentarlo).
   - Documentar reversibilidad: no todo es reversible (un email enviado no se "des-envía"; se mitiga con un nuevo evento "ignorar email anterior").
4. **Declarar timeouts por paso** con valores concretos en segundos (no "razonable").
5. **Identificar pasos con Wait for Task Token**: pasos que esperan acción humana asíncrona o un externo lento (Stripe webhook, restaurante acepta/rechaza, courier confirma). En orchestration → estado Task suspendido; en choreography → consumer espera evento.
6. **Definir correlation ID** que se propaga en cada evento (`flowId` o `<entidad>Id` raíz + `traceId`).
7. **Decidir choreography vs orchestration** comparando contra ≥ 3 dimensiones (acoplamiento, visibilidad del workflow, modificabilidad, testabilidad, tooling AWS, costo, debugging). Para orchestration, sub-decidir direct-call vs event-driven.
8. **Producir el diagrama** Mermaid `stateDiagram-v2` con happy path + compensaciones + timeouts visibles + Wait for Task Token marcados.
9. **Producir el ADR-0004** completo con opciones, decisión y consecuencias positivas y negativas.

## 5. Salida esperada

Tres artefactos:

- `docs/diagrams/saga_<flujo>.mmd` — diagrama Mermaid `stateDiagram-v2` con la convención:
  - Estados nominales del happy path.
  - Transiciones etiquetadas con el evento + timeout (ej. `OrderCreated [t=30s]`).
  - Estados de compensación claramente identificados (`Cancel*`, `Refund*`, `Release*`).
  - Notas inline para Wait for Task Token (`note: callback Stripe webhook, hasta 5 min`).
- `docs/adr/0004-orquestacion-vs-coreografia.md` siguiendo formato estándar ADR (Context, Decision, Options evaluadas contra dimensiones explícitas, Consequences positivas y negativas):
  - ≥ 3 opciones evaluadas: choreography pura, orchestration direct-call, orchestration event-driven (más opcional híbrido).
  - Cada opción contra ≥ 3 dimensiones.
  - Consecuencias positivas y negativas explícitas.
- Tabla de **compensaciones por paso** (Markdown):

| Paso | Evento happy path | Bounded context | Compensación | Idempotente | Siempre exitosa | Reversible | Timeout |
|------|-------------------|-----------------|---------------|-------------|-----------------|------------|---------|
| Crear pedido | OrderCreated | Orders | CancelOrder | sí | sí | sí | 5 s |
| Cobrar pago | PaymentCharged | Billing | RefundPayment | sí | sí | sí (parcial: stripe cobra 2 % fee) | 10 s |
| Aceptar ticket | TicketAccepted | Kitchen | RejectTicket | sí | sí | sí | 300 s (Wait for Task Token) |
| Asignar courier | CourierAssigned | Delivery | ReleaseCourier | sí | sí | sí | 60 s |
| Notificar consumidor | ConsumerNotified | Notifications | (no se compensa) | sí | sí | no (se mitiga con NotificationCorrection) | 5 s |

## 6. Verificación (criterios de "bien hecho")

- Todas las compensaciones declaradas son idempotentes y siempre exitosas (o tienen plan de mitigación documentado).
- Cada paso tiene timeout explícito en segundos.
- ≥ 1 paso con Wait for Task Token si el flujo involucra humanos o externos lentos.
- Correlation ID definido y propagado en cada evento.
- El ADR-0004 evalúa ≥ 3 opciones contra ≥ 3 dimensiones.
- El ADR-0004 lista consecuencias positivas y negativas (ambas obligatorias).
- El diagrama Mermaid renderiza (sintaxis `stateDiagram-v2` válida, sin `style`, sin caracteres conflictivos).

## 7. Anti-patrones específicos

- **Compensación que puede fallar**: viola la regla de compensaciones idempotentes y siempre exitosas. Mitigación: rediseñar para que la compensación sea idempotente y siempre exitosa, o documentar plan de manual operator alert.
- **Saga sin correlation ID**: imposible debuggear en producción. Mitigación: definir y propagar `<flujo>Id` en cada evento.
- **Saga coreografiada sin distributed tracing**: ceguera operativa. Mitigación: agregar X-Ray / OpenTelemetry + dashboard que reconstruye el flujo.
- **Orchestrator central que llama síncrono REST a todos los servicios**: acoplamiento al hardcodear URLs. Mitigación: orchestration event-driven o introducir API Gateway con discovery.
- **Compensación que cancela el pedido sin notificar al usuario**: mala UX. Mitigación: cada compensación que afecta al usuario genera un evento de notificación.
- **Saga con > 7 pasos**: probablemente cruza demasiados bounded contexts; revisar la descomposición.

## 8. Mini ejemplo de invocación

> "Diseña la saga del pedido FTGO. Pasos: (1) crear pedido en Orders, (2) cobrar a Stripe en Billing, (3) aceptar ticket en Kitchen (requiere humano), (4) asignar courier en Delivery, (5) notificar consumidor en Notifications. Broker MSK. Usa el skill `saga-designer`."

## 9. Modos de fallo conocidos

- Un paso no tiene compensación viable (ej. "enviar email" no se puede des-enviar) → STOP, proponer mitigación explícita (evento `NotificationCorrection`) y documentar en el ADR como consecuencia negativa.
- El usuario insiste en orchestration con direct-call cuando el flujo tiene > 5 pasos y servicios desplegados independientemente → STOP, mostrar el trade-off y pedir confirmación explícita.
- El catálogo de eventos no incluye los eventos de la saga → STOP, derivar al skill `event-catalog-author` antes.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 20/05/2026  | M.Sc. Edson Terceros   | versión inicial; cubre diseño de saga distribuida y ADR de orquestación vs coreografía |
