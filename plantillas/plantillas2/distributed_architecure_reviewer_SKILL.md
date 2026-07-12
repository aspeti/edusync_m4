---
name: distributed-architecture-reviewer
description: >
  Revisa la sección §6 del DTI (Arquitectura Distribuida) y los
  artefactos derivados (descomposición en microservicios, tabla
  de resiliencia, mapa IPC, API externa, ADRs) contra un
  checklist de calidad derivado de las mejores prácticas de
  microservicios. Detecta Distributed Monolith, falta de
  parámetros numéricos en resiliencia, violaciones de Database
  per service y Hidden Coupling. Produce reporte con score
  0-100, hallazgos críticos must-fix y fortalezas. Skill de
  verificación: NO crea artefactos, solo audita.
allowed-tools:
  - read
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: distributed-architecture-reviewer (auditor de DTI §6)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/distributed-architecture-reviewer/` o
> `.claude/skills/distributed-architecture-reviewer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: cierre del módulo de arquitecturas distribuidas (autoevaluación antes de release), revisión del DTI antes de merge a `release/*`, code review arquitectónico de la entrega grupal o de la entrega del examen.
- ARRANCA cuando: el usuario invoca `"@distributed-architecture-reviewer"`, abre `docs/DTI.md` cerca de §6, o pide "audita mi arquitectura distribuida / mi descomposición / mi resiliencia".
- NO ACTIVAR cuando: aún no existe el §6 del DTI ni la descomposición (correr antes los skills de generación).

## 2. Entradas obligatorias

El usuario MUST proporcionar acceso (al menos lectura) a:

- `docs/DTI.md` con §6 poblada (descomposición, tabla de servicios, IPC, resiliencia, API externa).
- `docs/adr/<n>-monolito-vs-microservicios.md` (ADR de descomposición; típicamente ADR-0003).
- `docs/diagrams/services_map.mmd` o equivalente (mapa de servicios + brokers + dependencias).
- `docs/adr/<n>-api-externa.md` (ADR de API externa, si existe).
- Opcional: diagramas de Aggregates (`docs/diagrams/aggregate_*.mmd`).

Si falta cualquiera, responder: `"Necesito al menos DTI §6, ADR de descomposición y mapa de servicios para auditar. Lista de archivos esperados: <…>."` y listar los ausentes.

## 3. Fuentes de verdad (orden de precedencia)

1. ADRs vigentes del repo del producto.
2. NFRs del PRD.
3. `AGENTS.md` del repo del producto (si existe).

## 4. Procedimiento

Aplicar el siguiente **checklist** y producir un score 0-100 (3.33 puntos por criterio cumplido; 30 criterios; score = aciertos × 3.33, redondeado a entero).

### Checklist (30 criterios)

**A. Descomposición (6 criterios)**

1. Cada microservicio mapea a 1 bounded context o documenta explícitamente por qué se divide.
2. Cada microservicio tiene **datos propios** declarados (sin tablas compartidas con otros servicios).
3. Cero `JOIN` cross-servicio en consultas declaradas en el DTI.
4. Cada microservicio tiene **equipo dueño** asignado.
5. ADR de descomposición evalúa **≥ 3 opciones** (microservicios completos / monolito modular + satélites / híbrido serverless).
6. ADR de descomposición usa el árbol de decisión (preguntas explícitas sobre equipo, tráfico, velocidad de cambio, regulación, ops).

**B. Resiliencia (6 criterios)**

7. Tabla de resiliencia para los **3 servicios más críticos** con sus dependencias externas más riesgosas.
8. Cada fila declara **≥ 4 parámetros numéricos concretos** (timeout, retry max, backoff base, failure rate, sliding window).
9. Retry NUNCA aplicado en 4xx (solo 5xx / timeouts).
10. Cada fila declara la **métrica observable** que valida el patrón.
11. Cada fila declara la dimensión **CAP sacrificada** en caso de partición.
12. Fallback documenta qué se hace cuando todo falla (cero respuestas "error 500 al usuario" sin alternativa).

**C. IPC (5 criterios)**

13. Tabla de IPC con estilo (sync / async / pub-sub) por flujo y tecnología elegida.
14. Cada flujo justifica su elección contra **≥ 2 dimensiones** (latencia, ordering, fan-out, acoplamiento, replay).
15. Ningún flujo síncrono se encadena > 3 saltos sin Circuit Breaker (Hidden Coupling).
16. Cada flujo síncrono declara timeout y política de retry.
17. Cada flujo async declara topic/cola, garantía, particionamiento y DLQ.

**D. API externa (4 criterios)**

18. ADR de API externa evalúa **≥ 3 opciones** (Gateway / BFF / GraphQL) contra **≥ 3 dimensiones**.
19. Cada endpoint público declara auth y rate limit.
20. Cero gRPC expuesto directamente al browser/cliente público.
21. Cada query cross-servicio tiene decisión explícita: API Composition (con timeout/fallback) o CQRS.

**E. DDD (5 criterios)**

22. Aggregates principales identificados con su Root y entities locales/value objects.
23. Cero setters públicos en los Roots; toda mutación pasa por método de dominio con verbo de negocio.
24. Cada método del Root valida **al menos 1 invariante** del dominio.
25. Cada cambio de estado relevante para otros contextos emite un **Domain Event** con nombre en pasado.
26. Las 3 reglas del Aggregate respetadas: referencias solo al Root, 1 TX = 1 Aggregate, consistencia inter-aggregate es eventual via eventos.

**F. Anti-patrones evitados (4 criterios)**

27. **Distributed Monolith** NO detectado (cero BD compartida, cero despliegues acoplados, cero cadenas REST sync de > 3 saltos sin CB).
28. **God microservice** NO detectado (ningún servicio absorbe 3+ bounded contexts).
29. **Anemic Domain Model** NO detectado (Roots con lógica, no solo getters/setters + ServiceClass).
30. **Dual-write directo** NO detectado en flujos cross-servicio (escribir BD + publicar evento en pasos separados sin Outbox).

## 5. Salida esperada

Reporte en Markdown con la siguiente estructura:

```markdown
# Auditoría arquitectura distribuida — <producto> — <fecha>

**Score global**: 82 / 100

| Bloque | Aciertos | Total | % |
|--------|----------|-------|---|
| A. Descomposición | 5 | 6 | 83 |
| B. Resiliencia | 4 | 6 | 67 |
| C. IPC | 5 | 5 | 100 |
| D. API externa | 4 | 4 | 100 |
| E. DDD | 4 | 5 | 80 |
| F. Anti-patrones | 4 | 4 | 100 |

## Hallazgos críticos (must-fix antes de release)

- **B8 (parámetros numéricos)**: la fila de SendGrid declara "retry razonable" sin números concretos. Riesgo: parámetro arbitrario en producción, no se puede ajustar por SLA. **Acción**: declarar retry max, timeout, backoff base, jitter en `docs/DTI.md §6.2`, fila NotificationService.
- **B11 (CAP)**: ningún flujo sync declara dimensión CAP sacrificada. **Acción**: para cada fila de resiliencia, declarar CP o AP por flujo.

## Hallazgos menores (recomendados)

- **A6 (árbol de decisión)**: el ADR-0003 menciona "equipo pequeño" pero no enumera las 5 preguntas del árbol. **Acción**: agregar las 5 preguntas y sus respuestas en la sección Context del ADR.
- **E26 (consistencia eventual)**: 2 Aggregates se modifican en la misma transacción en el caso de uso UC-04. **Acción**: refactorizar a Saga eventual con Domain Events.

## Fortalezas detectadas

- Cada microservicio tiene equipo dueño asignado y datos propios sin tablas compartidas.
- ADR-0003 evalúa 4 opciones (microservicios / modular / serverless / híbrido) y elige con justificación clara.
- Tabla de IPC tiene 5 filas con timeouts y CBs explícitos; cero cadenas sync sospechosas.
```

## 6. Verificación (criterios de "bien hecho")

- El reporte cubre los 30 criterios (sin saltarse ninguno; los no aplicables se marcan explícitamente con justificación).
- Cada hallazgo tiene una **acción accionable** (no solo "mejorar X").
- El score se calcula de manera reproducible (aciertos × 3.33, redondeado).
- Los hallazgos críticos están separados de los menores.
- El reporte cita los archivos exactos donde aplicar las acciones.
- El reporte NO inventa hallazgos: si un criterio no se puede verificar por falta de archivos, marcarlo "no auditable".

## 7. Anti-patrones específicos del propio skill

- **Cero falsos positivos por estilo**: el skill audita estructura y reglas duras, no preferencias estéticas.
- **Cero invención**: si un campo del checklist no se puede verificar por falta de archivos, marcar como "no auditable" en vez de inventar un acierto/falla.
- **Cero opiniones sin evidencia**: cada hallazgo cita la línea o la sección concreta del artefacto auditado.
- **Cero override del usuario**: si el usuario explica por qué un anti-patrón es aceptable (ej. dual-write OK en flujo no crítico), aceptar la justificación y bajar el hallazgo a "informativo".

## 8. Mini ejemplo de invocación

> "Audita mi arquitectura distribuida. Tengo `docs/DTI.md` con §6 completa, ADR-0003 de descomposición, ADR-0005 de API externa, mapa de servicios en `docs/diagrams/services_map.mmd` y 3 diagramas de Aggregates. Usa el skill `distributed-architecture-reviewer` y dame el score + hallazgos críticos antes de cerrar el release."

## 9. Modos de fallo conocidos

- Archivos vacíos o con solo placeholders → STOP, indicar qué archivos contienen únicamente plantilla sin contenido.
- Conflicto entre DTI §6 y ADR (ej. DTI dice microservicios, ADR dice monolito modular) → reportar como **hallazgo crítico de coherencia** y no asumir cuál tiene razón.
- El usuario pide "solo aprueba" sin querer ver hallazgos → recordar que es auditoría, no firma; producir reporte completo igualmente.
- No existe la tabla de IPC ni el mapa de servicios → STOP, solicitar al usuario que primero corra los skills de generación (`monolith-decomposition-architect`, `ipc-style-selector`).

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 21/05/2026  | M.Sc. Edson Terceros   | versión inicial; auditoría DTI §6 + artefactos distribuidos |
