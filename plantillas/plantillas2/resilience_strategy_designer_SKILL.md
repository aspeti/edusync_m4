---
name: resilience-strategy-designer
description: >
  Define la estrategia de resiliencia para los servicios críticos
  del producto contra sus dependencias externas. Aplica Circuit
  Breaker, Retry + Timeout + Exponential Backoff con Jitter,
  Fallback degradado, Bulkhead, Client-side Load Balancing, Rate
  Limiting, Sharding + Consistent Hashing y Auto-scaling, con
  parámetros numéricos concretos (no "razonable"). Declara la
  métrica observable que valida cada patrón. Aplica CAP theorem
  para fijar la dimensión sacrificada en cada flujo.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: resilience-strategy-designer (matchup resiliencia ↔ servicio con parámetros)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/resilience-strategy-designer/` o a
> `.claude/skills/resilience-strategy-designer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: llenado del DTI §6.2 (Resiliencia), redacción del ADR de resiliencia si la decisión es no trivial, revisión arquitectónica del producto grupal o de la entrega del examen.
- ARRANCA cuando: el usuario invoca `"@resilience-strategy-designer"`, abre el §6.2 del DTI, o pide "define la estrategia de resiliencia / circuit breakers / retries".
- NO ACTIVAR cuando: los servicios críticos aún no están identificados (correr antes `monolith-decomposition-architect`) o las dependencias externas no están listadas.

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- **Servicios críticos** del producto (mínimo 3, típicamente los del happy path principal).
- Para cada servicio crítico: **dependencias externas más riesgosas** (Stripe, SendGrid, servicio legacy, otra BD, otro microservicio, etc.) y su SLA conocido o estimado.
- **NFRs**: p99 latency target, error rate aceptable, throughput pico esperado.
- **Perfil de tráfico**: sostenido vs picos espurios, ratio pico/normal.
- **Tolerancia a degradación**: ¿el producto puede operar en modo "sin esta dependencia" temporalmente?

Si falta cualquiera, responder: `"Necesito los 3 servicios críticos, sus dependencias externas, los NFRs y el perfil de tráfico antes de diseñar la resiliencia."`

## 3. Fuentes de verdad (orden de precedencia)

1. NFRs del PRD (latencia, throughput, disponibilidad, tolerancia a fallos).
2. Catálogo de servicios y dependencias externas (DTI §6.1).
3. ADRs vigentes (broker elegido, stack tecnológico, cloud).
4. `AGENTS.md` del repo del producto (si existe; restricciones operativas, lenguajes y librerías permitidos).

## 4. Procedimiento

1. **Verificar inputs**. Si faltan dependencias externas o NFRs, STOP.
2. **Para cada servicio crítico y dependencia**, decidir cuál combinación de patrones aplica. Los patrones canónicos disponibles son:

   - **Circuit Breaker** (Nygard, *Release It!*): estados Closed → Open → Half-Open. Parámetros: failure rate threshold (típico 50 %), sliding window (típico 100 calls o 60 s), wait duration en Open (típico 30 s), minimum number of calls antes de evaluar (típico 20).
   - **Retry con Timeout + Exponential Backoff + Jitter**: max retries 3, timeout por intento 1-2 s, backoff base 200 ms, jitter [0, base × 2^n] para evitar thundering herd. Retry SOLO en errores transitorios (5xx, timeouts), NUNCA en 4xx.
   - **Fallback degradado**: respuesta cacheada, respuesta vacía documentada, respuesta de un servicio alternativo, o feature flag para apagar la dependencia.
   - **Bulkhead**: pools de threads / conexiones separados por dependencia. Si Stripe se cae no debe agotar el pool de SendGrid. Tamaños típicos: 10-50 threads por dependencia.
   - **Client-side Load Balancing**: el cliente conoce las N instancias (via Service Discovery) y rotea (round-robin, least-conn, latency-aware). Útil si el server-side LB es bottleneck.
   - **Rate Limiting**: token bucket (capacidad N, refill rate R/s) o leaky bucket (rate constante). Proteger backends compartidos. Típico API pública: 100 req/s por API key.
   - **Sharding + Consistent Hashing**: partición horizontal por usuario / tenant / región. Consistent Hashing minimiza re-balanceos al añadir/quitar nodos.
   - **Auto-scaling**: horizontal pod autoscaler / Lambda concurrency. Métricas trigger: CPU > 70 %, queue depth > N, latency p99 > umbral.

3. **Aplicar CAP theorem** por flujo: en una partición de red, ¿sacrificamos Consistency (responder con dato potencialmente stale) o Availability (responder error 503)? Documentar la decisión.
4. **Asignar parámetros numéricos concretos** por patrón. Cero "razonable", "moderado" o "típico" sin número.
5. **Identificar la métrica observable** que valida que el patrón funciona (p99 latency, error rate por dependencia, circuit breaker state transitions, queue depth, retry count).
6. **Plantear plan de Chaos Engineering** ligero: 1-2 game days al trimestre, fault injection por dependencia (kill -9 una instancia, latencia inyectada de 5 s, error 503 forzado).

## 5. Salida esperada

Tabla para `docs/DTI.md` §6.2 — **parámetros numéricos obligatorios**:

| Servicio | Dependencia externa | Patrón | Parámetros numéricos | Métrica observable | CAP elegido |
|----------|---------------------|--------|----------------------|--------------------|-------------|
| OrderService | Stripe Payments | Circuit Breaker + Retry + Fallback | failure rate 50 %, sliding window 100 calls, wait open 30 s, retry max 3, timeout 1.5 s, backoff base 200 ms, jitter [0, base × 2^n], fallback: encolar para procesar luego | p99 latency Stripe, error rate, CB state transitions | AP (acepto stale, no aceptable error) |
| NotificationService | SendGrid | Retry + Bulkhead | retry max 5, timeout 2 s, backoff base 500 ms, bulkhead 20 threads, fallback: encolar en SQS DLQ tras 5 fallos | error rate SendGrid, bulkhead saturation | AP |
| KitchenService | OrderService (gRPC) | Circuit Breaker + Client-side LB | failure rate 30 %, window 50 calls, wait open 15 s, LB least-connections, timeout 500 ms | latency p99 entre servicios, CB state | CP (consistencia gana en kitchen) |

Plus: bullets de plan de Chaos Engineering trimestral (1-2 experimentos por dependencia con criterios de éxito).

## 6. Verificación (criterios de "bien hecho")

- Cada fila tiene **al menos 4 parámetros numéricos concretos** (no "razonable", no "moderado").
- Cada fila declara la **métrica observable** que valida el patrón en producción.
- Cada fila declara explícitamente la **dimensión CAP sacrificada** en caso de partición.
- Retry NUNCA está en errores 4xx (solo 5xx y timeouts).
- Circuit Breaker tiene los 3 parámetros mínimos (failure rate, window, wait duration).
- Backoff exponencial siempre con jitter (evita thundering herd).
- Fallback documenta explícitamente qué se hace cuando todo falla (cero respuestas "error 500 al usuario").
- El plan de Chaos Engineering tiene al menos 1 experimento por dependencia crítica.

## 7. Anti-patrones específicos

- **Retry sin backoff**: amplifica la sobrecarga del backend caído. Mitigación: exponential backoff + jitter siempre.
- **Retry en 4xx**: nunca recuperará (es un error del cliente). Mitigación: lista blanca de códigos retriables (5xx, timeouts, conexión rechazada).
- **Timeout muy largo o sin timeout**: hilo bloqueado, recursos agotados. Mitigación: timeout < SLA del consumidor (típico 1-3 s en sync REST).
- **Sin Circuit Breaker en sync cascade**: 1 dependencia lenta → todos los timeouts → toda la cadena cae. Mitigación: CB en cada salto sync.
- **Fallback que oculta el problema sin alertar**: el sistema parece sano pero está degradado. Mitigación: el fallback emite métrica y alerta (no solo log).
- **Bulkhead inexistente**: 1 dependencia lenta → satura todo el pool. Mitigación: pool dedicado por dependencia.
- **Parámetros copiados de Internet sin justificar**: típico `retries = 3, timeout = 5s` para todo. Mitigación: cada parámetro justificado por SLA observado o requerimiento del NFR.
- **Pretender que CAP no aplica**: declarar el sistema "CA" en una arquitectura distribuida es un error categorial. Mitigación: aceptar que CAP es CP o AP por flujo.

## 8. Mini ejemplo de invocación

> "Mis servicios críticos: OrderService (dependencia Stripe), NotificationService (dependencia SendGrid), KitchenService (dependencia OrderService vía gRPC). NFRs: p99 latency 500 ms, error rate < 0.5 %, throughput pico 200 req/s. Tráfico: picos al mediodía y a las 19 h. Tolerancia: notificación puede demorar 5 min sin problema; pago NO puede fallar silenciosamente. Usa el skill `resilience-strategy-designer` y genera la tabla para DTI §6.2 + plan de Chaos Engineering."

## 9. Modos de fallo conocidos

- El usuario pide "5 retries para todo" sin distinguir entre operaciones idempotentes y no idempotentes → STOP, recordar que retry de un POST no idempotente puede duplicar el efecto (cobro doble); requiere idempotency key.
- La dependencia externa NO publica SLA → STOP, instrumentar primero con métricas pasivas (latency histograms) durante 2-4 semanas; sin datos, los parámetros son adivinanzas.
- El usuario quiere "consistencia estricta" en una llamada sync entre microservicios → STOP, recordar CAP: en partición, debes elegir; documentar elección.
- El producto no tiene observabilidad mínima (ni p99 latency ni error rate por dependencia) → STOP, no se puede validar resiliencia sin métricas.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 21/05/2026  | M.Sc. Edson Terceros   | versión inicial; matchup resiliencia ↔ servicio con parámetros numéricos concretos |
