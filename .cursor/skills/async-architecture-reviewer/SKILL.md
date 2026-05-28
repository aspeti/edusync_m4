---
name: async-architecture-reviewer
description: >
  Revisa la sección §7 del DTI (Arquitectura Asíncrona) y los
  artefactos derivados (catálogo, schemas, saga, broker, CDC) contra
  un checklist de calidad EDA derivado de S09. Produce reporte con
  hallazgos accionables y score 0-100. Skill de verificación, no de
  generación: NO crea artefactos nuevos, solo audita.
allowed-tools:
  - read
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: async-architecture-reviewer (auditor de DTI §7)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/async-architecture-reviewer/` o a
> `.claude/skills/async-architecture-reviewer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: cierre del módulo de arquitecturas asíncronas (autoevaluación antes de release), revisión del DTI antes de merge a `release/*`, code review arquitectónico de la entrega grupal.
- ARRANCA cuando: el usuario invoca `"@async-architecture-reviewer"`, abre `docs/DTI.md` cerca de §7, o pide "audita mi arquitectura asíncrona / mi saga / mi catálogo".
- NO ACTIVAR cuando: aún no existe el §7 del DTI ni catálogo (correr antes los skills de generación).

## 2. Entradas obligatorias

El usuario MUST proporcionar acceso (al menos lectura) a:

- `docs/DTI.md` con §7 poblada.
- `docs/events/catalog.md` y `docs/events/schemas/*`.
- `docs/diagrams/saga_*.mmd`.
- `docs/adr/0002-broker.md` y `docs/adr/0004-orquestacion-vs-coreografia.md` (si existen).

Si falta cualquiera, responder: `"Necesito al menos DTI §7, catálogo de eventos, schemas y saga para auditar. Lista de archivos esperados: <…>."` y listar los archivos ausentes.

## 3. Fuentes de verdad (orden de precedencia)

1. ADRs vigentes del repo del producto.
2. NFRs del PRD.
3. `AGENTS.md` del repo del producto (si existe).

## 4. Procedimiento

Aplicar el siguiente **checklist** y producir un score 0-100 (3 puntos por criterio cumplido; 32 criterios; score = aciertos × 3.125, redondeado a entero).

### Checklist (32 criterios)

**A. Catálogo de eventos (8 criterios)**

1. ≥ 6 eventos en el catálogo.
2. Cada evento clasificado como Unkeyed / Entity / Keyed.
3. Cada evento con productor único (Single Writer Principle).
4. Cada evento con `IdempotencyKey` declarada.
5. Cada evento con garantía explícita (at-least-once / effectively-once).
6. Nombres `<Entidad><AcciónEnPasado>` (cero `DataChanged`, `Event`, `Update`).
7. Payload con narrow types (cero `string` para todo; cero `double` para money).
8. Cada evento mapeado a un UC del FSD.

**B. Schemas (4 criterios)**

9. ≥ 2 schemas concretos en `docs/events/schemas/` (Avro/Protobuf/JSON Schema).
10. Cada schema declara su política de compatibility (backward/forward/full).
11. Cada schema declara productor único en su comentario inicial.
12. Cero campos `string` para conceptos con dominio cerrado; cero `double` para money.

**C. Saga (8 criterios)**

13. Existe `docs/diagrams/saga_<flujo>.mmd` para al menos 1 flujo crítico.
14. Diagrama renderiza (sintaxis `stateDiagram-v2` válida, cero `style` / `classDef`).
15. Cada paso del happy path tiene compensación documentada.
16. Cada compensación es idempotente (declarado explícitamente).
17. Cada compensación es siempre exitosa o tiene plan de mitigación.
18. Cada paso tiene timeout explícito en segundos.
19. ≥ 1 paso con Wait for Task Token o equivalente (si involucra humanos / externos lentos).
20. Correlation ID definido y propagado.

**D. Broker selection (4 criterios)**

21. Tabla de selección de broker por flujo presente en DTI §7 o ADR-0002.
22. Cada elección justifica contra ≥ 2 dimensiones de la matriz síntesis de selección de broker (replay, ordering, throughput, fan-out, retention, costo, ops).
23. Si el producto declaró AWS-only, ningún broker fuera de AWS en la tabla.
24. Cada flujo tiene un Plan B documentado.

**E. ADRs (4 criterios)**

25. ADR-0002 (broker) existe con ≥ 3 opciones evaluadas + consecuencias positivas y negativas.
26. ADR-0004 (orquestación vs coreografía) existe con ≥ 3 opciones evaluadas (choreography, orchestration direct-call, orchestration event-driven, opcional híbrido).
27. Cada ADR cita literatura técnica relevante (p. ej. Bellemare, Richardson u otra referencia reconocida).
28. Cada ADR declara consecuencias negativas explícitas (no es panfleto).

**F. Anti-patrones evitados (4 criterios)**

29. Cero eventos semáforo (sin payload útil).
30. Cero eventos con payload > 1 MB declarado en el catálogo.
31. Cero brokers usados fuera de su sweet spot (SQS como event log; Kafka como queue RPC; EventBridge para throughput sostenido alto).
32. Cero "dual-write directo" sin Outbox cuando hay escritura a DB + broker en mismo flujo.

## 5. Salida esperada

Reporte en Markdown con la siguiente estructura:

```markdown
# Auditoría EDA del producto <nombre> — <fecha>

**Score global**: 87 / 100

| Bloque | Aciertos | Total | % |
|--------|----------|-------|---|
| A. Catálogo de eventos | 7 | 8 | 87.5 |
| B. Schemas | 4 | 4 | 100 |
| C. Saga | 6 | 8 | 75 |
| D. Broker selection | 4 | 4 | 100 |
| E. ADRs | 3 | 4 | 75 |
| F. Anti-patrones | 4 | 4 | 100 |

## Hallazgos críticos (must-fix antes de release)

- **C16 (compensación idempotente)**: `RefundPayment` no declara idempotencia explícitamente. Riesgo: doble reembolso si el worker se reinicia. **Acción**: agregar `paymentId + retryCount` como idempotency key en `docs/events/schemas/PaymentRefunded.avsc`.
- **E26 (ADR-0004 opciones)**: el ADR-0004 evalúa solo 2 opciones (choreography y orchestration direct-call). **Acción**: agregar opción "orchestration event-driven" con sus pros y contras, comparable a las 2 existentes.

## Hallazgos menores (recomendados)

- **A2 (taxonomía)**: el evento `OrderUpdated` está clasificado como Entity pero su payload solo lleva el delta. **Acción**: revisar si es Keyed (delta) o Entity (snapshot completo) y alinear.

## Fortalezas detectadas

- Catálogo con 8 eventos, todos con narrow types y trazables a UCs del FSD.
- Selección de broker justificada contra 3 dimensiones por flujo.
```

## 6. Verificación (criterios de "bien hecho")

- El reporte cubre los 32 criterios (sin saltarse ninguno; los no aplicables se marcan explícitamente con justificación).
- Cada hallazgo tiene una **acción accionable** (no solo "mejorar X").
- El score se calcula de manera reproducible (aciertos × 3.125, redondeado).
- Los hallazgos críticos están separados de los menores.
- El reporte cita los archivos exactos donde aplicar las acciones.

## 7. Anti-patrones específicos del propio skill

- **Cero falsos positivos por estilo**: el skill audita estructura y reglas duras, no preferencias estéticas.
- **Cero invención**: si un campo del checklist no se puede verificar por falta de archivos, marcar como "no auditable" en vez de inventar un acierto/falla.
- **Cero opiniones sin evidencia**: cada hallazgo cita la línea o la sección concreta del artefacto auditado.
- **Cero override del usuario**: si el usuario explica por qué un anti-patrón es aceptable (ej. SQS para tasks puras, no event log), aceptar la justificación y bajar el hallazgo a "informativo".

## 8. Mini ejemplo de invocación

> "Audita la arquitectura asíncrona de mi producto. Tengo `docs/DTI.md` con §7 completa, `docs/events/catalog.md` con 7 eventos, 2 schemas en `docs/events/schemas/`, `docs/diagrams/saga_pedido.mmd` y los ADRs 0001 a 0004. Usa el skill `async-architecture-reviewer` y dame el score + hallazgos críticos antes de cerrar el release."

## 9. Modos de fallo conocidos

- Archivos vacíos o con solo placeholders → STOP, indicar qué archivos contienen únicamente plantilla sin contenido.
- Conflicto entre DTI §7 y ADR (ej. DTI dice Kafka, ADR dice SQS) → reportar como **hallazgo crítico de coherencia** y no asumir cuál tiene razón.
- El usuario pide "solo aprueba" sin querer ver hallazgos → recordar que es auditoría, no firma; producir reporte completo igualmente.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 20/05/2026  | M.Sc. Edson Terceros   | versión inicial; auditoría DTI §7 + artefactos EDA |
