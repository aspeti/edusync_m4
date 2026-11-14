# Auditoría de concurrencia del backend — EduSync

> **Módulo 8 · Actividad de auditoría (parte 2 de 2) · Entrega: domingo 18 de octubre de 2026, 23:59.**
> Este archivo es a la vez **las instrucciones para el asistente de IA** y **la ficha que se entrega**. El asistente lo rellena en su lugar; el grupo lo revisa, responde las preguntas y lo sube a su repositorio.

## Cómo se usa (para el grupo)

1. Copien este archivo a su repositorio como `docs/auditoria/concurrencia.md`.
2. Trabajen sobre el mismo commit que la parte 1 (`release/8.3.0`).
3. Abran su asistente con acceso al repositorio y escríbanle:
   > Sigue las instrucciones de la sección 0 de `docs/auditoria/concurrencia.md` y rellena ese archivo.
4. Respondan las preguntas que el asistente les haga al final de cada oleada. Aquí habrá más preguntas que en la parte 1, porque varios datos (cuántas réplicas corren, cuánto tarda la IA, cuánto tráfico esperan) no están en el código.
5. Revisen el resultado, confirmen la autoverificación (sección 6) y hagan los commits **cada integrante con su propia cuenta**.

**Si su asistente no tiene acceso al repositorio:** pídanle los comandos, córranlos ustedes y péguenle la salida.

**Cómo se califica la parte de preguntas:** cada pregunta debe terminar respondida o cerrada como `NO VERIFICABLE` con su motivo. **Si quedan más de 3 preguntas sin responder en este archivo, baja la nota de la actividad.** Si no tienen el dato exacto (por ejemplo, el tráfico esperado), una estimación razonada cuenta como respuesta.

---

## 0. Instrucciones para el asistente de IA (no editar esta sección)

**Rol.** Eres el asistente de auditoría de un grupo de maestría que desarrolla un backend. Vas a revisar cómo maneja la concurrencia su servicio y a **rellenar este mismo archivo**. No produces otros archivos.

**Conceptos que debes aplicar.**
- **Dónde espera una petición:** casi todo el tiempo de una API es espera (base de datos, red, servicio de IA). Saturar es que un recurso finito (hilos, conexiones del pool, el único hilo del event loop, el cupo de la API de IA) esté al 100 % y las peticiones nuevas hagan cola: primero sube la latencia y luego llegan los errores.
- **Modelos por runtime:** Node.js usa un event loop de un hilo (cualquier trabajo síncrono lo bloquea); Java 21+ con hilos virtuales permite código bloqueante que escala, pero el pool de la base de datos sigue siendo el límite; Python con `async def` bloquea el loop si llama a librerías síncronas, y las rutas `def` corren en un threadpool finito (40 hilos por defecto en AnyIO).
- **Ley de Little:** conexiones ocupadas en promedio = peticiones por segundo que usan la base de datos × tiempo medio que cada una retiene la conexión. Cada instancia tiene su propio pool: la base de datos recibe `instancias × tamaño del pool`.
- **Llamadas lentas (LLM):** necesitan timeout explícito, reintentos con backoff y jitter, límite de llamadas simultáneas y no retener una conexión de base de datos mientras se espera.

**Reglas que no puedes romper.**
1. **Solo lectura del producto.** No modificas código ni configuración; el único archivo que editas es este. No ejecutas pruebas de carga contra servicios compartidos o en producción.
2. **No inventas.** Cada estado distinto de `N/A` lleva evidencia comprobable: `ruta/archivo:línea` con el fragmento relevante, o el comando exacto y las primeras líneas de su salida. Las cifras que no salen del código (tráfico, latencias, réplicas) se preguntan, no se suponen.
3. **No copias secretos.** Rutas y líneas sí; valores de claves, nunca.
4. **Todas las comprobaciones aparecen**, una vez cada una (CON-01 a CON-05). No agregas ni quitas comprobaciones.
5. **Escribes en español** y en frases claras.

**Estados permitidos.** `OK`, `PARCIAL`, `FALLA`, `N/A` (con razón) y `NO VERIFICABLE` (con razón). Mientras trabajas puedes usar `PENDIENTE`, pero **al terminar no puede quedar ninguno**.

**Cómo preguntar sin bloquear.** Cuando te falte un dato que no está en el código:
- Marca la comprobación como `PENDIENTE`, agrega la pregunta a la sección 4 con un número (P1, P2…) y la comprobación a la que afecta, y **continúa con la siguiente**. Nunca te detengas a esperar.
- Al **terminar cada oleada**, muestra al grupo todas las preguntas nuevas juntas. Si tu herramienta permite preguntas con opciones, úsalas (por ejemplo, para el número de réplicas: 1, 2, 3 o más, no sabemos).
- Si el grupo responde, anota la respuesta y actualiza la comprobación. Si no responde, en la oleada 3 cierra la comprobación como `NO VERIFICABLE` y la pregunta como "sin respuesta".
- **Antes de preguntar, búscalo** en el README, `docker-compose`, Dockerfile, manifiestos de Kubernetes, archivos de configuración y scripts de arranque.

**Oleadas.** Trabaja en este orden y actualiza la sección 2 al terminar cada una:
- **Oleada 0 · Reconocimiento.** Rellena la sección 1: runtime y framework de cada servicio, punto de arranque, librería de acceso a datos y cliente HTTP o SDK de IA. Escribe en la sección 2 el plan (qué archivos y comandos usarás).
- **Oleada 1 · Evidencia automática.** Ejecuta los comandos de cada comprobación (adaptados a las carpetas reales) y anota la evidencia.
- **Oleada 2 · Juicio.** Decide el estado de CON-01 a CON-04 y pregunta lo que falte. Calcula con la ley de Little las conexiones necesarias cuando tengas los datos (tráfico estimado y tiempo de retención); si no los tienes, pregúntalos.
- **Oleada 3 · Cierre.** Integra las respuestas; escribe la **hipótesis de cuello de botella** (CON-05) con base en las comprobaciones anteriores; cierra lo pendiente como `NO VERIFICABLE`; escribe los hallazgos (sección 5), marca la autoverificación (sección 6) y rellena el resumen (sección 7). Termina diciendo cuántas preguntas quedaron sin respuesta.

**Si el proyecto tiene varios servicios**, evalúa cada uno y anota la evidencia por servicio; el estado es el del peor evaluado.

---

## 1. Datos del repositorio (oleada 0)

- **Grupo:** G-EduSync
- **Commit auditado (`release/8.3.0`):** `c2b9121edaa02ee6bcb1f3e8164af85a803d9032` (`c2b9121` Merge pull request #25 from aspeti/release/8.2.0; la etiqueta `release/8.3.0` apunta a este commit)
- **Fecha de la auditoría:** 2026-11-14
- **Servicios y runtime:** api · Java 25 LTS + Spring Boot 4.1.0 (Spring Modulith 2.1.0, `spring-boot-starter-web` → Tomcat embebido, modelo servlet con pool de hilos de plataforma; sin `spring.threads.virtual` ni WebFlux). Un solo backend monolítico en `backend/`. Frontend Angular 21 (SPA) fuera del alcance de esta auditoría de concurrencia del backend.
- **Acceso a datos:** Spring Data JPA / Hibernate · **base de datos:** PostgreSQL 15 (`infra/docker-compose.yml`; perfil `dev` en `application-dev.yml`)
- **Cliente HTTP o SDK de IA:** Spring AI 2.0.0 (`ChatClient` + `OllamaChatModel` / `OpenAiChatModel` para Gemini y Open WebUI) detrás de `LlmPort` / `AgenteLlmPort`; `RestClient` (JDK `HttpClient` HTTP/1.1) para Ollama y para el loopback de herramientas del agente
- **Integrantes y qué revisó cada uno:** Rodrigo Aspeti (n = 1) → CON-01, CON-02, CON-03, CON-04, CON-05

## 2. Plan y avance por oleadas

| Oleada | Qué se hizo | Estado |
|--------|-------------|--------|
| 0 · Reconocimiento | Identificado el monolito `backend/` (Java 25 / Boot 4.1.0 / Tomcat servlet). Punto de arranque `EduSyncApplication`. Datos vía JPA + PostgreSQL 15. IA vía Spring AI `ChatClient`. Plan: greps CON-01..04 sobre `backend/`, `infra/`, ADR-0006; leer `application*.yml`, `AiConfig`, scheduler, pool Hikari. | Hecha |
| 1 · Evidencia | Ejecutados greps de réplicas/scheduler/virtual threads, I/O bloqueante, pool/datasource, timeouts/reintentos/semáforos. Anotada evidencia por comprobación. | Hecha |
| 2 · Juicio | Estados propuestos para CON-01..04; preguntas P1–P5 al grupo. | Hecha |
| 3 · Cierre | Integradas respuestas P1–P5; estados finales CON-01..05; Little parcial; hipótesis; hallazgos; autoverificación y resumen. | Hecha |

**Plan de archivos/comandos (oleada 0):**
- CON-01: `git grep` réplicas/workers/virtual threads; `EduSyncApplication.java`; `VencimientoSchedulerJob.java`; `application.yml` (cron); `infra/docker-compose.yml`; ADR-0006; `pom.xml` (`spring-boot-starter-web`).
- CON-02: `git grep` `synchronized` / `Files.read*` / WebFlux; revisar si están en el camino HTTP.
- CON-03: `git grep` hikari/pool; `application-dev.yml`; `GlobalExceptionHandler`; ley de Little con datos del grupo.
- CON-04: `AiConfig` / `AiProperties` / `application.yml` (timeouts); greps retry/backoff/Semaphore; `EjecutorHerramientaHttpAdapter`; ausencia de `@Transactional` en servicios de chat/agente.

## 3. Comprobaciones

### CON-01 · Modelo de concurrencia e instancias

- **Qué se busca:** saber cómo atiende peticiones simultáneas cada servicio (event loop, hilos, hilos virtuales, procesos o *workers*) y **cuántas instancias** corren en el entorno donde se despliega.
- **Señales de falla:** nadie sabe cuántas instancias corren; varias instancias pero un trabajo programado o un estado en memoria que supone una sola.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "NestFactory|app\.listen|cluster|worker_threads|spring\.threads\.virtual|uvicorn|gunicorn|granian|--workers|replicas:|deploy:" -- .
  ```
- **Evidencia:**
  - Comando: `git grep -nE "spring\.threads\.virtual|replicas:|deploy:|@Scheduled|ShedLock" -- backend/ infra/`
    - Sin coincidencias de `spring.threads.virtual` ni `replicas:` / manifiestos K8s/ECS en el repo.
    - `@Scheduled` en `backend/src/main/java/com/edusync/plataforma/infrastructure/adapter/in/scheduler/VencimientoSchedulerJob.java:24` con comentario en líneas 12–14: «suficiente para 1 instancia… si … 2+ réplicas, este job necesitara … ShedLock».
  - `backend/pom.xml:72` — dependencia `spring-boot-starter-web` (Tomcat servlet; no WebFlux).
  - `backend/src/main/java/com/edusync/EduSyncApplication.java:17-22` — `@SpringBootApplication` + `@EnableScheduling` + `SpringApplication.run`.
  - `backend/src/main/resources/application.yml:61-64` — cron de vencimiento «sin ShedLock … suficiente para 1 instancia».
  - `backend/src/main/java/com/edusync/shared/tenant/TenantContextProvider.java:11-12` — documenta «pool de Tomcat».
  - `infra/docker-compose.yml` — solo servicio `postgres`; no define réplicas de la API ni Dockerfile de runtime.
  - `docs/adr/0006-cloud-provider-y-estilo-de-despliegue.md` — objetivo ECS Fargate con escalado; `infra/ecs/` aún no materializado.
  - Respuesta del grupo (P1): aún no desplegado → en la práctica **1 proceso local**, alineado con el diseño del scheduler.
- **Estado:** `OK`
- **Observación:** El backend atiende con el **pool de hilos de Tomcat** (modelo servlet bloqueante). Java 25 permitiría hilos virtuales, pero no están activados. Hoy no hay despliegue multi-réplica (P1); el `@Scheduled` sin ShedLock es coherente con 1 instancia. Antes de subir réplicas en ECS (ADR-0006) hará falta bloqueo distribuido en el job de vencimiento.
- **Auditó:** Rodrigo Aspeti

### CON-02 · Llamadas bloqueantes en el camino de una petición

- **Qué se busca:** que ningún manejador de petición haga trabajo síncrono que congele el servicio: lectura de archivos síncrona, bucles pesados, `JSON.parse` de archivos grandes, librerías síncronas dentro de código asíncrono.
- **Señales de falla:** en Node, `readFileSync`, `execSync`, cálculos pesados en el handler; en Python, `requests.` o `time.sleep` dentro de `async def`, o un SDK síncrono de IA llamado desde código asíncrono; en Java, bloqueo en WebFlux o `synchronized` alrededor de E/S con JDK anterior a 24.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "readFileSync|writeFileSync|execSync|spawnSync|JSON\.parse\(fs" -- <carpetas de código>
  git grep -nE "async def" -- '*.py'    # luego revisar si dentro se usa requests., time.sleep o un SDK síncrono
  git grep -nE "Mono|Flux|WebClient|synchronized" -- '*.java'
  ```
- **Evidencia:**
  - Comando: `git grep -nE "Files\.readAllLines|Files\.readString|synchronized|Mono|Flux|WebFlux" -- backend/src/main`
    - `MotorMonitoreo.java:29` — `Files.readAllLines` (usado solo desde `MonitorAsistenteCli`, CLI offline; **no** es camino HTTP).
    - `PromotorCasoOnline.java:29,61,81` — `Files.readString` (mismo CLI).
    - `RegistroInteraccionJsonl.java:50` — `public synchronized void registrar(...)` invocado desde `AgenteController.java:74` cuando el monitor JSONL está activo.
    - Sin rutas WebFlux/`Mono`/`Flux` de negocio; `WebClient.builder()` aparece solo al construir el bean Ollama en `AiConfig.java:41` (SDK Spring AI), no como stack reactivo de la API.
  - El camino LLM (`ChatConLlmService`, `EjecutarConsultaAgenteService`, adaptadores) es **síncrono bloqueante** sobre hilos Tomcat: esperado en servlet, no congela un event loop de un solo hilo.
  - Runtime Java 25 (≥ 24): el `synchronized` del monitor no cae en la señal de falla «JDK anterior a 24».
- **Estado:** `PARCIAL`
- **Observación:** No hay anti-patrones tipo event-loop. El I/O de archivos síncrono del monitor queda fuera del request path (CLI). Sí hay un `synchronized` en el registro JSONL del agente en el camino de `POST /api/v1/ai/agente` cuando el monitor está encendido (opt-in; apagado por defecto en `application.yml:102`). Las llamadas al LLM bloquean el hilo Tomcat hasta el timeout (hasta 120 s): coherente con servlet, pero consumen hilos del pool durante toda la espera.
- **Auditó:** Rodrigo Aspeti

### CON-03 · Pool de conexiones a la base de datos

- **Qué se busca:** que el tamaño del pool, el tiempo de espera para obtener una conexión y el error que devuelve la API cuando el pool se agota estén **definidos a propósito**, y que `instancias × tamaño del pool` no supere lo que admite la base de datos. Lo ideal al agotarse es un error rápido y claro (por ejemplo, 503), no una espera larga ni un 500 genérico.
- **Señales de falla:** valores por defecto sin revisar; pool enorme "por si acaso"; transacciones abiertas mientras se espera al servicio de IA.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "poolSize|max:|connectionLimit|connection_limit|maximum-pool-size|maximumPoolSize|pool_size|max_overflow|pool_timeout|connectionTimeout|acquireTimeout" -- .
  ```
- **Cálculo (ley de Little):** conexiones ocupadas en promedio = 5 req/s (P3) × &lt;retención desconocida — P4&gt; = **no calculable sin P4**. Cota: para saturar el pool de 10 con λ = 5 haría falta retención media sostenida &gt; 2 s (L = λW ⇒ W = 10/5 = 2 s). Total en la base de datos = 1 instancia (P1) × 10 (default Hikari) = **10**, frente a `max_connections` ≈ **100** (P2, default Docker) → holgura clara (10 ≪ 100).
- **Evidencia:**
  - Comando: `git grep -nE "maximum-pool-size|maximumPoolSize|hikari|connectionTimeout|pool-size" -- backend/`
    - **Cero** coincidencias de configuración explícita de HikariCP / tamaño de pool / `connectionTimeout`.
  - `backend/src/main/resources/application-dev.yml:5-8` — solo `url` / `username` / `password`; sin bloque `hikari`.
  - `backend/src/main/resources/application.yml:21-24` — JPA `open-in-view: false` (positivo: no alarga la conexión al renderizar).
  - Default Spring Boot / HikariCP: `maximumPoolSize=10`, `connectionTimeout=30000` ms.
  - `GlobalExceptionHandler` — no mapea agotamiento de pool a 503.
  - Servicios de IA sin `@Transactional` envolviendo el LLM — no retienen JDBC mientras esperan al modelo.
  - Respuestas del grupo: P1 = 1 local / no desplegado; P2 = default Docker (~100); P3 ≈ 5 req/s; P4 = no sabemos.
- **Estado:** `FALLA`
- **Observación:** El pool queda en **defaults de Hikari sin revisión explícita** (señal de falla). Al agotarse, espera ~30 s y suele acabar en 500 genérico, no en 503. Capacidad actual 1×10 frente a ~100 de Postgres es holgada; con ~5 req/s el pool no debería ser el primer cuello salvo retenciones muy largas (&gt;2 s), pero P4 no se midió. No hay transacción abierta durante la espera al LLM.
- **Auditó:** Rodrigo Aspeti

### CON-04 · Timeouts y reintentos en llamadas externas (incluida la IA)

- **Qué se busca:** que toda llamada a otro servicio (HTTP, IA, colas) tenga **timeout explícito**; que los reintentos usen backoff con jitter y solo en operaciones idempotentes; y que las llamadas al LLM tengan un límite de llamadas simultáneas y no retengan conexiones de base de datos.
- **Señales de falla:** clientes HTTP sin timeout; reintentos inmediatos en bucle; la petición del usuario espera sin límite la respuesta del LLM.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "axios|fetch\(|HttpService|httpx|requests\.|RestClient|WebClient|OpenAI|Anthropic|ChatClient" -- <carpetas de código>
  git grep -nE "timeout|retry|retries|backoff|AbortSignal|maxRetries|Semaphore|p-limit|bulkhead" -- <carpetas de código>
  ```
- **Evidencia:**
  - Timeouts LLM **sí** definidos:
    - `application.yml:73,79,85,97` — `timeout-seconds` (default 120) vía env.
    - `AiConfig.java:64,134` — `.timeout(Duration.ofSeconds(...))`.
    - `AiConfig.java:158-165` — `connectTimeout(5s)` + `setReadTimeout(timeoutSeconds)` (Ollama).
  - Comando: `git grep -nE "retry|backoff|jitter|Semaphore|bulkhead|RateLimiter|CircuitBreaker" -- backend/src/main`
    - **Sin** reintentos con backoff/jitter ni semáforo/bulkhead/límite de concurrencia LLM.
  - `EjecutorHerramientaHttpAdapter.java:53-63` — `RestClient.builder()` **sin** timeout.
  - Sin `@Transactional` en chat/agente durante la espera al LLM.
  - Respuesta del grupo (P5): no saben concurrencia ni latencia típica del LLM bajo pico.
- **Estado:** `FALLA`
- **Observación:** El LLM tiene timeout (120 s), así que la petición no espera sin límite. Fallan el resto de requisitos: sin reintentos con backoff+jitter, sin límite de llamadas simultáneas al LLM, y el `RestClient` de herramientas del agente sin timeout. P5 no aporta cifra de carga IA; el riesgo de saturar hilos Tomcat o la cuota del proveedor permanece sin acotar.
- **Auditó:** Rodrigo Aspeti

### CON-05 · Hipótesis de cuello de botella

- **Qué se busca:** una frase que diga **qué recurso se saturará primero** cuando crezca el tráfico y por qué, basada en CON-01 a CON-04. Se comprobará con la primera prueba de carga con k6 (S04).
- **Formato:** "Bajo carga, se saturará primero <recurso> en <endpoint o flujo>, porque <motivo basado en CON-0x>."
- **Hipótesis:** Bajo carga, se saturará primero el pool de hilos de Tomcat en `POST /api/v1/ai/agente` (y `/api/v1/ai/chat`), porque cada llamada al LLM bloquea un hilo servlet hasta 120 s sin límite de concurrencia ni bulkhead (CON-01, CON-02, CON-04), mientras el pool JDBC (10) con ~5 req/s de negocio y `max_connections`≈100 deja holgura en la BD (CON-03; retención media no medida en P4).
- **Estado:** `OK`
- **Auditó:** Rodrigo Aspeti

## 4. Preguntas al grupo

| N.º | Comprobación | Pregunta | Respuesta del grupo | Estado (respondida / sin respuesta) |
|-----|--------------|----------|---------------------|--------------------------------------|
| P1 | CON-01, CON-03, CON-05 | ¿Cuántas instancias/réplicas del backend corren en el entorno que consideran de despliegue (demo/producción/local compartido)? | Aún no desplegado; en la práctica 1 proceso local | respondida |
| P2 | CON-03 | ¿Cuál es el `max_connections` (o límite efectivo) de PostgreSQL en ese entorno? Si no lo saben, ¿usan el default de Postgres (~100) en Docker local? | Default Docker (~100) | respondida |
| P3 | CON-03, CON-05 | ¿Qué tráfico estimado esperan en picos (peticiones/segundo que tocan la BD), p. ej. cierre de notas? Una estimación razonada vale. | ~5 req/s | respondida |
| P4 | CON-03 | ¿Tiempo medio estimado de retención de una conexión JDBC por petición de negocio (ms o s)? Si no miden, ¿orden de magnitud (p. ej. &lt;50 ms lecturas, 100–300 ms escrituras)? | No sabemos | respondida (sin dato medido) |
| P5 | CON-04, CON-05 | En un pico, ¿cuántas consultas concurrentes al asistente IA (`POST /api/v1/ai/agente` o `/ai/chat`) esperan como máximo? ¿Latencia típica observada del LLM (segundos)? | No sabemos | respondida (sin dato medido) |

## 5. Hallazgos priorizados

| # | Comprobación | Severidad (alta / media / baja) | Qué falla | Corrección propuesta | Responsable |
|---|--------------|---------------------------------|-----------|----------------------|-------------|
| 1 | CON-04 | alta | Sin límite de concurrencia ni bulkhead hacia el LLM; cada `POST /api/v1/ai/agente` puede ocupar un hilo Tomcat hasta 120 s | Semáforo/bulkhead (p. ej. Resilience4j Bulkhead o `Semaphore`) acotando llamadas LLM simultáneas; cola o 503 rápido al saturar | Rodrigo Aspeti |
| 2 | CON-04 | alta | `EjecutorHerramientaHttpAdapter` usa `RestClient` sin timeout | Configurar `connect`/`read` timeout en el `RestClient` del loopback (mismo criterio que `AiConfig.http11RestClientBuilder`) | Rodrigo Aspeti |
| 3 | CON-03 | media | Pool Hikari en defaults (10 / 30 s) sin configuración explícita ni mapeo a 503 al agotar | Fijar `spring.datasource.hikari.maximum-pool-size` y `connection-timeout` a propósito; manejar `CannotGetJdbcConnectionException` → 503 | Rodrigo Aspeti |
| 4 | CON-04 | media | Sin reintentos con backoff + jitter en llamadas LLM (solo timeout) | Reintentos acotados solo en errores transitorios/idempotentes, con backoff exponencial y jitter | Rodrigo Aspeti |
| 5 | CON-01 | baja | Scheduler de vencimiento sin ShedLock: romperá si se escalan réplicas ECS | Añadir bloqueo distribuido (ShedLock u otro) antes del primer despliegue multi-réplica | Rodrigo Aspeti |
| 6 | CON-02 | baja | `synchronized` en `RegistroInteraccionJsonl` serializa escrituras si el monitor está activo | Mantener monitor apagado en carga; o cola asíncrona / append sin lock global | Rodrigo Aspeti |

## 6. Autoverificación (la marca el asistente; el grupo la confirma)

- [x] Las cinco comprobaciones CON-01 a CON-05 están en la sección 3, una vez cada una.
- [x] Ninguna quedó en `PENDIENTE` ni sin estado; todos los estados son de los cinco permitidos.
- [x] Todo estado distinto de `N/A` y `NO VERIFICABLE` tiene evidencia con ruta y línea, o con comando y salida.
- [x] Las cifras usadas (tráfico, latencias, réplicas) vienen del código o de una respuesta del grupo, no de una suposición.
- [x] CON-05 está formulada con el formato pedido y cita las comprobaciones en que se basa.
- [x] Todas las preguntas de la sección 4 están respondidas o marcadas "sin respuesta".
- [x] No hay ningún secreto copiado en este archivo.
- [x] No se modificó código del producto.
- [ ] Cada integrante aparece como auditor de al menos una comprobación y tiene al menos un commit con su propia identidad. *(pendiente del commit del grupo; n = 1 → Rodrigo Aspeti)*

## 7. Resumen

| OK | PARCIAL | FALLA | N/A | NO VERIFICABLE | Preguntas sin respuesta |
|---:|--------:|------:|----:|---------------:|------------------------:|
| 2 | 1 | 2 | 0 | 0 | 0 |

**Hipótesis de cuello de botella (copia de CON-05):** Bajo carga, se saturará primero el pool de hilos de Tomcat en `POST /api/v1/ai/agente` (y `/api/v1/ai/chat`), porque cada llamada al LLM bloquea un hilo servlet hasta 120 s sin límite de concurrencia ni bulkhead (CON-01, CON-02, CON-04), mientras el pool JDBC (10) con ~5 req/s de negocio y `max_connections`≈100 deja holgura en la BD (CON-03; retención media no medida en P4).
