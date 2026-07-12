---
name: serverless-architect
description: >
  Decide qué partes del producto van como FaaS (Function as a
  Service) y cuáles van como containers (Fargate / ECS / EKS).
  Aplica los criterios de FaaS sweet spot vs anti-patrones, modela
  arquitecturas híbridas (monolito modular + satélites FaaS),
  decide entre Lambda + API Gateway, Step Functions y workflows
  long-running, evalúa vendor lock-in vs costo por invocación.
  Produce tabla de decisiones por servicio/función para el DTI
  y ADR opcional si la decisión es no trivial.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: serverless-architect (FaaS vs containers + arquitecturas híbridas)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/serverless-architect/` o a
> `.claude/skills/serverless-architect/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: decisión de plataforma de cómputo por servicio, planificación de arquitectura híbrida, redacción del DTI §6 (sub-sección runtime), redacción del ADR de plataforma si la decisión es no trivial.
- ARRANCA cuando: el usuario invoca `"@serverless-architect"`, abre `docs/adr/<n>-runtime.md`, o pide "Lambda o ECS / containers vs serverless".
- NO ACTIVAR cuando: la decisión cloud aún no está tomada (multi-cloud / on-premises pendiente); cuando hay restricción regulatoria que prohíbe serverless (algunos sectores).

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- **Lista de servicios/funciones del producto** que pueden ir a serverless o containers (típicamente entre 5 y 30 unidades).
- **Perfil de tráfico por servicio**:
  - Sostenido (req/s constante) vs picos espurios (factor pico/normal).
  - Frecuencia (continuo, batch nocturno, evento esporádico).
- **Latencia tolerable por servicio** (sub-100 ms con cold start aceptable, 100-500 ms, segundos OK).
- **Criticidad**: P0 (caída no aceptable) / P1 (degradación tolerable) / P2 (best-effort).
- **Capacidad operativa del equipo**: ¿pueden operar Kubernetes? ¿Fargate? ¿solo SaaS / FaaS?
- **Restricciones**: cloud (AWS-only típico para el módulo), lenguajes permitidos, vendor lock-in tolerable.

Si falta cualquiera, responder: `"Necesito la lista de servicios/funciones con perfil de tráfico, latencia tolerable, criticidad y capacidad operativa del equipo antes de decidir FaaS vs containers."`

## 3. Fuentes de verdad (orden de precedencia)

1. NFRs del PRD (latencia p99, throughput pico, costo, disponibilidad).
2. Catálogo de servicios del DTI §6.
3. ADRs vigentes (cloud, broker, stack).
4. `AGENTS.md` del repo del producto (si existe; lenguajes y frameworks permitidos).

## 4. Procedimiento

1. **Verificar inputs**. Si falta perfil de tráfico o latencia tolerable, STOP.
2. **Aplicar los criterios de FaaS sweet spot** por servicio/función:

   | Criterio | FaaS gana cuando | Container gana cuando |
   |----------|------------------|----------------------|
   | Tráfico | picos espurios, idle largo, eventos esporádicos | sostenido alto, miles req/s constante |
   | Cold start tolerable | sí (latencia 100 ms - 2 s OK al primer req) | no (sub-100 ms requerido siempre) |
   | Estado | stateless, idempotente | estado en memoria, caché en proceso |
   | Compute | corto, < 15 min, < 10 GB RAM | largo, batch, GPU, > 15 min |
   | Equipo ops | pequeño, sin K8s expertise | maduro, opera cluster |
   | Costo | bajo uso (paga por invocación) | uso alto sostenido (paga capacidad) |
   | Lock-in | tolerable (AWS-only definido) | quiero portabilidad / multi-cloud |

3. **Identificar arquetipos de servicios/funciones**:
   - **Lambda pegamento** (glue code): orquestación entre AWS services, triggers de eventos S3/DynamoDB/SNS, transformación de datos en pipelines, autorización con Lambda Authorizers en API Gateway. **FaaS gana casi siempre**.
   - **Lambda CRUD detrás de API Gateway**: endpoints REST simples con bajo throughput. FaaS gana si tráfico < ~500 req/s sostenido y latencia 100-500 ms OK.
   - **Lambda core domain**: lógica de negocio compleja, latencia crítica, throughput sostenido. **Container gana** (Fargate / EKS) por evitar cold start, mejor debugging, mejor caching.
   - **Workflow / Saga long-running**: orquestación de pasos con espera (humano, externo). **Step Functions** sweet spot (Wait for Task Token, retries, error handling).
   - **Worker batch nocturno**: procesa N items en ventana específica. FaaS si cabe en 15 min y memoria razonable; container si dura horas.
   - **Stream processor**: consume Kafka / Kinesis continuamente. **Container gana** (Flink, Spark, Kafka Streams); FaaS solo para streams con throughput bajo (Lambda + Kinesis trigger).
4. **Mapear a runtime concreto** (AWS):
   - **Lambda + API Gateway**: REST/HTTP endpoints, baja-media frecuencia.
   - **Lambda + EventBridge / SNS / SQS triggers**: event-driven glue.
   - **Lambda + Kinesis trigger**: stream processing baja escala.
   - **Step Functions Express**: workflows < 5 min, alto throughput.
   - **Step Functions Standard**: workflows hasta 1 año, baja-media frecuencia, Wait for Task Token.
   - **Fargate (ECS o EKS)**: containers sin manejo de servers; default para servicios core de mediano tamaño.
   - **EKS** (Kubernetes): cuando se necesita ecosistema K8s (operators, GitOps, multi-tenant) y equipo capaz.
   - **EC2**: solo casos especiales (GPU, software legacy con licencias).
5. **Aplicar arquitecturas híbridas**: el producto puede ser **monolito modular en Fargate + satélites Lambda** para las funciones glue/event-driven. Es muy común y suele ser óptimo cuando el equipo es pequeño y el dominio core no necesita microservicios completos.
6. **Evaluar vendor lock-in vs costo**:
   - Lambda + API Gateway + DynamoDB + Step Functions = stack altamente AWS-específico. Migrar a otro cloud requiere reescritura significativa.
   - Containers en Fargate son portables a EKS (mismo container) y luego a otro cloud con menos esfuerzo.
   - Documentar tolerancia al lock-in en el ADR.
7. **Calcular costo aproximado** por servicio (al menos cualitativo): Lambda paga $0.20 por millón de invocaciones + tiempo de ejecución; Fargate paga por CPU + RAM por hora. Punto de cruce típico: si tu servicio recibe > 1-2 millones de invocaciones/día sostenidas, Fargate es más barato.
8. **Producir tabla de decisiones por servicio/función**.

## 5. Salida esperada

Tabla obligatoria en `docs/DTI.md` §6 (sub-sección runtime):

| Servicio / Función | Runtime elegido | Justificación (≥ 2 dimensiones) | Lock-in | Plan B |
|---------------------|-----------------|----------------------------------|---------|--------|
| OrderService (API REST) | Fargate ECS | throughput sostenido 200 req/s + latencia p99 < 200 ms requerida (cold start no aceptable) + equipo opera Fargate | medio (containers portables) | EKS si se necesita ecosistema K8s |
| KitchenNotifier (glue Lambda) | Lambda + EventBridge | tráfico esporádico (1-2 req/s pico tras OrderConfirmed) + glue code corto + costo bajo | alto (AWS) | container ECS scheduled si se quita el lock-in |
| SagaPedidoOrchestrator | Step Functions Standard | workflow long-running con Wait for Task Token (espera webhook Stripe hasta 5 min) + visibilidad nativa | alto (AWS) | orchestrator propio en container con DB de estado |
| ReportingBatch (nocturno) | Lambda | corre 10 min cada noche, dentro del límite de 15 min de Lambda, sin servers idle | alto (AWS) | Fargate scheduled task si crece > 15 min |
| StreamProcessor (Kafka) | Fargate ECS (Kafka Streams) | stream sostenido > 1000 ev/s, estado en proceso (state stores), latency crítica | medio | Flink en EMR si escala 10x |
| Lambda Authorizer | Lambda | invocado por API Gateway, latencia OK, cold start < 100 ms con provisioned concurrency | alto | Authorizer en Fargate ALB |

Plus: 2-3 bullets describiendo trade-offs declarados (vendor lock-in, costo, ops, debugging).

ADR opcional (solo si la decisión global del producto es no trivial, ej. híbrido fuerte o "todo Lambda" vs "todo containers"):

- Context: perfil de tráfico, restricciones cloud, capacidad ops.
- Decision: arquitectura híbrida elegida con % aproximado de cómputo en FaaS vs containers.
- Options evaluadas: A) todo FaaS / B) todo containers / C) híbrido (recomendado típico).
- Consequences positivas y negativas.

## 6. Verificación (criterios de "bien hecho")

- Cada servicio/función justifica su elección contra **≥ 2 dimensiones** (tráfico, latencia, costo, ops, lock-in).
- Ningún servicio P0 con latencia crítica está en Lambda sin provisioned concurrency declarada.
- Ningún workflow > 5 min está en Step Functions Express (excede el límite).
- Ningún stream sostenido > 1000 ev/s está en Lambda como única opción (degradación a determinar).
- El **vendor lock-in** está declarado por servicio (alto / medio / bajo).
- Cada servicio tiene un **Plan B** si la elección actual no escala o el lock-in se vuelve problemático.
- Para servicios FaaS con cold start crítico, está declarada la mitigación (provisioned concurrency, warming, SnapStart).

## 7. Anti-patrones específicos

- **Lambda para servicio P0 con latencia ultra-baja sin provisioned concurrency**: cold start sorprende en producción. Mitigación: provisioned concurrency, SnapStart (Java), warming sintético, o mover a Fargate.
- **Lambda para procesos batch > 15 min**: timeout forzado, datos perdidos. Mitigación: Step Functions con Map state, o Fargate scheduled task.
- **Step Functions Express para workflow > 5 min**: no soportado. Mitigación: Standard Workflow.
- **Stream processing crítico en Lambda con alto throughput**: limitaciones de paralelismo (1 lambda por partición), gestión de estado limitada. Mitigación: Fargate con Flink/Spark/Kafka Streams.
- **Estado en memoria de Lambda entre invocaciones**: no garantizado (mismo container puede reusarse o no). Mitigación: estado externo (DynamoDB, ElastiCache) o container.
- **Todo Lambda para evitar K8s sin medir costo**: a 5+ millones de invocaciones/día Fargate suele ser más barato. Mitigación: estimación de costo previa.
- **Híbrido sin documentar el límite**: nadie sabe cuándo añadir un servicio nuevo va a Lambda o a Fargate. Mitigación: ADR con criterios claros (latencia, tráfico, equipo).
- **Vendor lock-in no reconocido**: declarar el sistema "portable" mientras se usan 8 servicios AWS-específicos. Mitigación: aceptar el lock-in como trade-off y documentarlo.

## 8. Mini ejemplo de invocación

> "Mis servicios y funciones: (1) OrderService API REST — 200 req/s sostenidos, p99 < 200 ms, P0; (2) KitchenNotifier glue tras evento OrderConfirmed — 1-2 invocaciones/s, latencia OK; (3) Saga del pedido con Wait for Task Token al webhook Stripe (espera hasta 5 min); (4) Reporting batch nocturno — 10 min/día, P2; (5) Stream processor Kafka — > 1000 ev/s sostenidos; (6) Lambda Authorizer en API Gateway. Equipo: 4 devs, no K8s expertise, AWS-only. Usa el skill `serverless-architect` y genera la tabla de runtime para DTI §6."

## 9. Modos de fallo conocidos

- El usuario quiere "todo serverless" sin medir costo → STOP, estimar invocaciones/día y compararlo contra Fargate; a partir de cierto umbral (1-2M invocaciones/día sostenidas) Fargate gana.
- Latencia ultra-baja (< 50 ms p99) con Lambda sin provisioned concurrency → STOP, recomendar provisioned concurrency o moverlo a container.
- Stream processor con estado complejo en Lambda → STOP, Lambda no es buen runtime para esto; recomendar Flink / Kafka Streams en container.
- Workflow long-running implementado como Lambda recursiva → STOP, recomendar Step Functions.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 21/05/2026  | M.Sc. Edson Terceros   | versión inicial; FaaS vs containers + arquitecturas híbridas + Step Functions |
