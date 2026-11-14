# Auditoría 12-factor — EduSync

> **Módulo 8 · Actividad de auditoría (parte 1 de 2) · Entrega: domingo 18 de octubre de 2026, 23:59.**
> Este archivo es a la vez **las instrucciones para el asistente de IA** y **la ficha que se entrega**. El asistente lo rellena en su lugar; el grupo lo revisa, responde las preguntas y lo sube a su repositorio.

## Cómo se usa (para el grupo)

1. Copien este archivo a su repositorio como `docs/auditoria/12factor.md`.
2. Creen la etiqueta o rama `release/8.3.0` sobre su rama principal actual (es el estado de partida del módulo) y trabajen sobre ese commit.
3. Abran su asistente con acceso al repositorio (Claude Code, Codex, Cursor u otro) y escríbanle:
   > Sigue las instrucciones de la sección 0 de `docs/auditoria/12factor.md` y rellena ese archivo.
4. Respondan las preguntas que el asistente les haga al final de cada oleada. Pueden contestar en el chat o escribiendo la respuesta directamente en la sección 4.
5. Revisen el resultado, corran ustedes la autoverificación (sección 6) y hagan los commits **cada integrante con su propia cuenta**.

**Si su asistente no tiene acceso al repositorio** (un chat sin herramientas): pídanle que les indique qué comandos correr, córranlos ustedes y péguenle la salida. El resto del proceso es igual.

**Cómo se califica la parte de preguntas:** cada pregunta debe terminar respondida o cerrada como `NO VERIFICABLE` con su motivo. **Si quedan más de 3 preguntas sin responder en este archivo, baja la nota de la actividad.** No saber un dato no es un error; dejar sin respuesta lo que el grupo sí podría averiguar, sí.

---

## 0. Instrucciones para el asistente de IA (no editar esta sección)

**Rol.** Eres el asistente de auditoría de un grupo de maestría que desarrolla un backend. Vas a revisar su repositorio contra seis prácticas 12-factor y a **rellenar este mismo archivo**. No produces otros archivos.

**Reglas que no puedes romper.**
1. **Solo lectura del producto.** No modificas código, configuración ni dependencias del repositorio. El único archivo que editas es este.
2. **No inventas.** Cada estado distinto de `N/A` lleva evidencia comprobable: `ruta/archivo:línea` con el fragmento relevante, o el comando exacto que ejecutaste y las primeras líneas de su salida. Si no puedes comprobar algo, no lo supones: lo preguntas o lo marcas `NO VERIFICABLE` con su motivo.
3. **No copias secretos.** Si encuentras una contraseña, token o clave, reportas la ruta y la línea, **nunca el valor**. No abras ni muestres archivos `.env` ni de claves en el chat.
4. **Todas las comprobaciones aparecen**, una vez cada una (F12-01 a F12-06), aunque el resultado sea negativo. No agregas ni quitas comprobaciones.
5. **Escribes en español** y en frases claras.

**Estados permitidos.** `OK`, `PARCIAL`, `FALLA`, `N/A` (no aplica, con razón) y `NO VERIFICABLE` (no se pudo comprobar, con razón). Mientras trabajas puedes usar `PENDIENTE`, pero **al terminar no puede quedar ninguno**.

**Cómo preguntar sin bloquear.** Cuando te falte un dato que no está en el código (por ejemplo, cómo se despliega en producción):
- Marca la comprobación como `PENDIENTE`, agrega la pregunta a la sección 4 con un número (P1, P2…) y la comprobación a la que afecta, y **continúa con la siguiente**. Nunca te detengas a esperar.
- Al **terminar cada oleada**, muestra al grupo todas las preguntas nuevas juntas, cada una con su número. Si tu herramienta permite preguntas con opciones, úsalas; si no, escríbelas en el chat.
- Si el grupo responde, anota la respuesta en la sección 4 y actualiza la comprobación. Si no responde o no lo sabe, en la oleada 3 cierra la comprobación como `NO VERIFICABLE` y la pregunta como "sin respuesta".
- Pregunta solo lo necesario: **antes de preguntar, búscalo en el repositorio** (README, `docker-compose`, Dockerfile, manifiestos, configuración, scripts).

**Oleadas.** Trabaja en este orden y actualiza la sección 2 al terminar cada una:
- **Oleada 0 · Reconocimiento.** Rellena la sección 1 (commit, stack, carpetas de código, cómo se ejecuta) y escribe en la sección 2 un plan breve: qué carpetas revisarás y qué comandos usarás para cada comprobación. No preguntes nada todavía, salvo que no encuentres el código fuente.
- **Oleada 1 · Evidencia automática.** Ejecuta los comandos de cada comprobación (adáptalos a las carpetas reales del proyecto) y anota la evidencia. No decidas estados aún.
- **Oleada 2 · Juicio.** Con la evidencia, decide el estado de cada comprobación y escribe la observación. Lo que no se pueda decidir sin un dato externo, pregúntalo como se indica arriba. Al terminar, muestra las preguntas.
- **Oleada 3 · Cierre.** Integra las respuestas recibidas; cierra lo que quede sin respuesta como `NO VERIFICABLE`; escribe los hallazgos priorizados (sección 5); marca la autoverificación (sección 6) y rellena el resumen (sección 7). Termina diciendo al grupo cuántas preguntas quedaron sin respuesta.

**Si el proyecto tiene varios servicios** (por ejemplo, un backend en NestJS y otro en Python), evalúa cada comprobación sobre todos y anota la evidencia de cada uno; el estado es el del servicio peor evaluado.

---

## 1. Datos del repositorio (oleada 0)

- **Grupo:** G-EduSync
- **Repositorio:** https://github.com/aspeti/edusync_m4
- **Commit auditado (`release/8.3.0`):** `c2b9121edaa02ee6bcb1f3e8164af85a803d9032`
- **Fecha de la auditoría:** 2026-11-14
- **Stack detectado:** Java 25 LTS, Spring Boot 4.1.0 (Spring Modulith 2.1.0), Spring Data JPA / Hibernate, PostgreSQL 15, Spring AI 2.0.0 (Ollama / Open WebUI / Gemini detrás de `LlmPort`), Angular 21 (SPA, fuera del alcance estricto del backend 12-factor salvo sesión JWT en cliente)
- **Carpetas de código fuente:** `backend/src/main/java/com/edusync/`, `backend/src/main/resources/`, `frontend/src/`, `infra/`
- **Cómo se ejecuta (verificado):** documentado en `README.md` — `docker compose -f infra/docker-compose.yml up -d` (Postgres) y `mvn -q spring-boot:run` desde `backend/`. En esta auditoría no se dejó levantado el proceso largo; se verificó la existencia de `infra/docker-compose.yml`, `backend/pom.xml` (`spring-boot-maven-plugin`) y las instrucciones del README.
- **Integrantes y qué revisó cada uno:** Rodrigo Aspeti (n = 1) → F12-01 … F12-06

## 2. Plan y avance por oleadas

| Oleada | Qué se hizo | Estado |
|--------|-------------|--------|
| 0 · Reconocimiento | Identificado monolito `backend/` + Postgres en Compose; plan de greps por F12-01..06. | Hecha |
| 1 · Evidencia | Ejecutados greps de config/secretos/estado/logs/backing/Docker-shutdown; anotada evidencia. | Hecha |
| 2 · Juicio | Estados F12-01..06; preguntas P1–P3 al grupo. | Hecha |
| 3 · Cierre | Integradas P1–P3; F12-06 → FALLA; hallazgos; autoverificación y resumen. | Hecha |

**Plan (oleada 0):**
- F12-01: `git grep` localhost/URLs/`@Value`/`@ConfigurationProperties` en `backend/src/main` + `application*.yml`.
- F12-02: `git ls-files` / `check-ignore` / historial `.env`; patrones password/secret (solo rutas); `.env.example`; Compose.
- F12-03: writes a disco, `@Scheduled`, caches `Map`, sesión.
- F12-04: logback/File appenders vs SLF4J consola.
- F12-05: datasource env, `LlmPort` + proveedores, URLs configurables.
- F12-06: Dockerfile/compose, `server.shutdown`, SIGTERM/hooks.

## 3. Comprobaciones

### F12-01 · Configuración fuera del código (factor III)

- **Qué se busca:** todo lo que cambia entre entornos (URLs, puertos, credenciales, banderas) se lee del entorno al arrancar, y la aplicación valida al inicio que esté presente. Mismo código en dev, pruebas y producción; solo cambia la configuración.
- **Señales de falla:** `localhost`, IPs o puertos escritos en el código; URLs de servicios fijas; condiciones del tipo `if (env === 'production')` repartidas por el código.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "localhost|127\.0\.0\.1|:5432|:3306|:6379|:27017|https?://" -- <carpetas de código>
  git grep -nE "process\.env|ConfigService|getOrThrow|@Value|@ConfigurationProperties|BaseSettings|os\.environ|os\.getenv" -- <carpetas de código>
  ```
- **Evidencia:**
  - `application-dev.yml:6-8` — JDBC vía `${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}`, `${DB_USER}`, `${DB_PASSWORD}` (defaults locales).
  - `application.yml:29-30,51-52,68-97` — `SERVER_PORT`, `JWT_SECRET`, proveedor IA y URLs vía env (`OLLAMA_BASE_URL`, `GEMINI_*`, etc.).
  - `@ConfigurationProperties(prefix = "edusync.ai")` en `AiProperties.java:11`; múltiples `@Value("${...}")` en seguridad, seed e IA.
  - Comando: `git grep -nE "if.*(production|prod)" -- backend/src/main` → sin ramas `if (production)` en código de producto.
  - Defaults `localhost` en YAML/Java para Ollama/Open WebUI y DB (sobreescribibles por env). Loopback fijo `http://localhost:${server.port}` en `EjecutorHerramientaHttpAdapter.java:63` y `DescubridorHerramientasOpenApiAdapter.java:69` (intencional, misma JVM).
  - `OpenApiConfig.java:49` — servidor OpenAPI documentado como `http://localhost:8080`.
  - No hay `getRequiredProperty` / fail-fast que **exija** `JWT_SECRET` u otras claves al arrancar: el proceso arranca con el default inseguro del YAML.
- **Estado:** `PARCIAL`
- **Observación:** La configuración variable está externalizada con `${ENV:default}`. Falta validación estricta al inicio (factor III completo: “valida que esté presente”). Los defaults `localhost` son aceptables para local si siempre se sobreescriben fuera de local; el loopback del agente es diseño, no acoplamiento a un host de despliegue.
- **Auditó:** Rodrigo Aspeti

### F12-02 · Secretos fuera del repositorio y de su historial (factor III)

- **Qué se busca:** ninguna contraseña, token o clave en el código, en archivos versionados ni en el historial de git. El `.env` real está en `.gitignore`; solo se versiona un `.env.example` sin valores reales.
- **Señales de falla:** `.env` versionado; asignaciones del tipo `password = "..."` o `apiKey: '...'`; claves en `docker-compose.yml`, en un `Dockerfile` (`ENV` o `ARG`) o en archivos de configuración.
- **Comandos sugeridos** (no muestran valores):
  ```bash
  git ls-files | grep -E '(^|/)\.env$' ; git check-ignore -v .env
  git log --all --oneline -- .env
  git grep -c -I -i -E '(password|secret|api[_-]?key|token)[[:space:]]*[:=]' -- . ':!*.lock'
  git log --all --pickaxe-regex -S'(?i)password' --oneline --name-only
  gitleaks detect --source . --redact --no-banner   # opcional, si está instalado
  ```
- **Evidencia:**
  - `git ls-files` filtrado por `.env` → **vacío** (`.env` no versionado).
  - `git check-ignore -v .env` → `.gitignore:19:.env`.
  - `git log --all --oneline -- .env` → vacío (sin historial de `.env`).
  - Versionados: `.env.example`, `tests/e2e/.env.e2e.example`, `tools/m7-unit/.env.example` (plantillas; claves sin valores reales de producción).
  - `gitleaks` no instalado en el entorno de auditoría.
  - Señales en config versionada (valores **no** citados aquí; solo rutas/líneas):
    - `application.yml:51` — default de `JWT_SECRET` (marcado “dev-only” en comentario).
    - `application.yml:59` — default de `SYSADMIN_PASSWORD`.
    - `application-dev.yml:8` y `infra/docker-compose.yml:11` — default de password de Postgres local.
  - API keys de Gemini/Open WebUI: placeholders vacíos / solo `${GEMINI_API_KEY}` / `${OPEN_WEBUI_API_KEY}` (sin valor real en repo).
- **Estado:** `PARCIAL`
- **Observación:** Buena higiene de `.env` (gitignore + example + sin historial). Quedan **defaults inseguros versionados** para JWT, sysadmin y Postgres local. El grupo (P1) confirma que en entornos compartidos **siempre** sobreescribe por env, lo que mitiga el riesgo operativo; el hallazgo documental (literales en el árbol) permanece.
- **Auditó:** Rodrigo Aspeti

### F12-03 · Procesos sin estado (factor VI)

- **Qué se busca:** nada importante vive en la memoria del proceso ni en su disco local entre peticiones: sesiones, archivos subidos y cachés compartidas van a servicios de apoyo (base de datos, Redis, almacenamiento de objetos). Así se pueden levantar varias instancias.
- **Señales de falla:** archivos subidos guardados en una carpeta local; sesiones en memoria; un `Map` o diccionario global usado como caché compartida; trabajos programados que asumen una sola instancia.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "writeFileSync|writeFile\(|createWriteStream|diskStorage|mkdirSync|open\(.*['\"]w|UploadFile" -- <carpetas de código>
  git grep -nE "express-session|MemoryStore|new Map\(|@Cron|setInterval\(" -- <carpetas de código>
  ```
- **Evidencia:**
  - Auth backend: JWT (sin sesión servidor). El SPA guarda el token en `sessionStorage` (`frontend/.../auth.service.ts`) — estado de **cliente**, no del proceso API.
  - `@Scheduled` en `VencimientoSchedulerJob.java:12-14,24` — documentado “suficiente para 1 instancia”, sin ShedLock (`application.yml:62-64`).
  - Escritura a disco local: `RegistroInteraccionJsonl.java:79` → `logs/asistente.jsonl`, solo si `edusync.ai.monitor.registro=true` (`@ConditionalOnProperty` línea 29); por defecto `false` (`application.yml:102`). CLI `MonitorAsistenteCli` / `PromotorCasoOnline` escriben archivos fuera del camino HTTP.
  - Sin uploads `MultipartFile` / `diskStorage` en `backend/src/main`.
  - Sin caché global tipo `static Map` de negocio detectada en el grep principal.
- **Estado:** `PARCIAL`
- **Observación:** El núcleo (JWT + Postgres) es stateless entre instancias. El scheduler asume una sola réplica; el monitor JSONL, si se enciende, amarra estado al disco del proceso. Aceptable hoy (1 instancia local); bloqueante al escalar.
- **Auditó:** Rodrigo Aspeti

### F12-04 · Logs a la salida estándar (factor XI)

- **Qué se busca:** la aplicación escribe sus eventos en la salida estándar (idealmente en formato estructurado, por ejemplo JSON); recogerlos y guardarlos es tarea de la plataforma.
- **Señales de falla:** el logger escribe en archivos propios (`app.log`, rotación de archivos); `console.log` sueltos con datos sensibles.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "transports\.File|DailyRotateFile|pino\.destination|FileHandler|RotatingFileHandler|logging\.file|logback.*FILE" -- .
  git grep -nE "winston|pino|nestjs-pino|Logger|logging\.basicConfig|logback" -- <carpetas de código>
  ```
- **Evidencia:**
  - Comando file-appender: sin `logback-spring.xml` / `log4j` FILE / `DailyRotateFile` en el producto.
  - Uso de `LoggerFactory.getLogger` (SLF4J) en seeders, scheduler, notificaciones, adaptadores IA — default Spring Boot = **consola (stdout/stderr)**.
  - `System.out.println` solo en `MonitorAsistenteCli` (herramienta CLI, no el servidor HTTP).
  - Sin patrón JSON estructurado configurado en `application*.yml` (texto de consola por defecto).
  - El JSONL del monitor (`logs/asistente.jsonl`) no es el logger de la app; es telemetría opt-in del asistente.
- **Estado:** `OK`
- **Observación:** Los logs de aplicación van a la salida estándar vía SLF4J/Logback default. Mejora futura (no falla): formato JSON estructurado para agregación. El archivo JSONL del monitor no invalida el factor XI del logger.
- **Auditó:** Rodrigo Aspeti

### F12-05 · Servicios de apoyo intercambiables (factor IV)

- **Qué se busca:** base de datos, colas, caché, almacenamiento y servicio de IA se conectan por una URL o variables de configuración, de modo que cambiar uno (por ejemplo, la base local por una gestionada) no exige tocar el código.
- **Señales de falla:** el cliente de un servicio se construye con host, puerto o credenciales fijas; el código depende de un proveedor concreto sin una capa que lo aísle.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "DATABASE_URL|REDIS_URL|AMQP_URL|S3_|BUCKET|OPENAI|ANTHROPIC|_URL\b" -- <carpetas de código>
  git grep -nE "createConnection|TypeOrmModule\.forRoot\(|PrismaClient|create_engine|MongoClient|new Redis\(" -- <carpetas de código>
  ```
- **Evidencia:**
  - Postgres: URL armada con env en `application-dev.yml:6-8` (`DB_HOST`/`DB_PORT`/`DB_NAME`/credenciales).
  - IA: puerto de dominio `LlmPort` / `AgenteLlmPort`; adaptadores `OllamaLlmAdapter`, `OpenWebUiLlmAdapter`, `GeminiLlmAdapter` seleccionados por `edusync.ai.provider` (`@ConditionalOnProperty` en `AiConfig.java` y adaptadores).
  - URLs/modelos/timeouts de IA vía `application.yml` + env (`OLLAMA_BASE_URL`, `GEMINI_BASE_URL`, etc.).
  - Colas/Redis/S3: aún no hay clientes en código de producto (SIE/`notassie` vacío) → no aplica acoplamiento duro a broker.
- **Estado:** `OK`
- **Observación:** BD e IA se cambian por configuración/env y, en IA, detrás de un puerto hexagonal. No hace falta tocar el dominio para pasar de Ollama local a Gemini o a un Postgres gestionado.
- **Auditó:** Rodrigo Aspeti

### F12-06 · Construcción inmutable y apagado limpio (factores V y IX)

- **Qué se busca:** se construye una vez (imagen o artefacto) y se ejecuta igual en todos los entornos, sin secretos dentro de la imagen; y el proceso, al recibir `SIGTERM`, deja de aceptar peticiones, termina lo que tiene en curso y cierra conexiones.
- **Señales de falla:** `COPY .env` o `ENV` con secretos en el Dockerfile; `CMD` en forma de texto (`CMD npm start`) en lugar de forma exec; ningún manejo de cierre (en NestJS falta `app.enableShutdownHooks()`; en Spring alguien puso `server.shutdown: immediate`; en FastAPI no se cierran recursos en el `lifespan`).
- **Comandos sugeridos:**
  ```bash
  git ls-files | grep -iE "dockerfile|compose" 
  git grep -nE "COPY \.env|^ENV .*(PASS|SECRET|KEY|TOKEN)|^CMD [^\[]" -- '*Dockerfile*'
  git grep -nE "SIGTERM|enableShutdownHooks|onModuleDestroy|beforeApplicationShutdown|server\.shutdown|lifespan" -- .
  ```
- **Evidencia:**
  - `git ls-files` → solo `infra/docker-compose.yml` (Postgres). **No hay Dockerfile** de la API en el repo.
  - Grep `server.shutdown` / `SIGTERM` / `beforeApplicationShutdown` en `backend/` → **sin coincidencias**; no está `server.shutdown=graceful` en `application*.yml`.
  - Artefacto Maven (`spring-boot-maven-plugin` en `pom.xml:211`) permite un JAR ejecutable reproducible, pero sin imagen/container contract.
  - ADR-0006 prevé ECS Fargate; IaC/`Dockerfile` aún no materializados.
  - Respuesta del grupo (P2): **no** hay Dockerfile ni pipeline de imagen fuera del repo.
  - Respuesta del grupo (P3): **no** han probado apagado con SIGTERM / stop ordenado.
- **Estado:** `FALLA`
- **Observación:** No existe imagen inmutable (ni en el repo ni fuera). Tampoco hay `server.shutdown=graceful` ni prueba de SIGTERM. El JAR Maven es un artefacto parcial del factor V, insuficiente sin contenedor y sin apagado limpio verificado (factor IX). Prioritario antes de S07/contenedorizar.
- **Auditó:** Rodrigo Aspeti

## 4. Preguntas al grupo

| N.º | Comprobación | Pregunta | Respuesta del grupo | Estado (respondida / sin respuesta) |
|-----|--------------|----------|---------------------|--------------------------------------|
| P1 | F12-02 | En entornos compartidos (demo/clase), ¿sobreescriben siempre `JWT_SECRET`, `SYSADMIN_PASSWORD` y `DB_PASSWORD` por env, o a veces arrancan con los defaults del YAML/Compose? | Siempre por env | respondida |
| P2 | F12-06 | ¿Existe un Dockerfile o pipeline de imagen (ECS/GHCR) **fuera** de este repositorio? | No | respondida |
| P3 | F12-06 | ¿Han probado el apagado del proceso con SIGTERM / stop ordenado (conexiones cerradas, requests en vuelo)? | No | respondida |

## 5. Hallazgos priorizados

| # | Comprobación | Severidad (alta / media / baja) | Qué falla | Corrección propuesta | Responsable | ¿Antes de contenedorizar (S07)? |
|---|--------------|---------------------------------|-----------|----------------------|-------------|---------------------------------|
| 1 | F12-06 | alta | Sin Dockerfile ni pipeline de imagen; no hay construcción inmutable reproducible entre entornos | Añadir `Dockerfile` (OpenJDK 25) con `CMD` en forma exec, multi-stage, sin `COPY .env` ni `ENV` con secretos; publicar imagen en el pipeline | Rodrigo Aspeti | Sí |
| 2 | F12-06 | alta | Sin `server.shutdown=graceful` ni prueba de SIGTERM (P3) | Activar `server.shutdown=graceful` (+ timeout); verificar que al `SIGTERM` se drenan requests y se cierra el pool Hikari | Rodrigo Aspeti | Sí |
| 3 | F12-01 | media | Arranca con defaults inseguros; no falla si faltan secretos reales | Fail-fast en perfil no-local: exigir `JWT_SECRET` (y credenciales) sin default, o validar al `@PostConstruct`/`EnvironmentPostProcessor` | Rodrigo Aspeti | Sí |
| 4 | F12-02 | media | Defaults de password/JWT/DB versionados en YAML/Compose (aunque P1 mitiga en demo) | Quitar defaults sensibles del YAML; dejar solo `.env.example` vacío o con placeholders no funcionales | Rodrigo Aspeti | Recomendado |
| 5 | F12-03 | baja | `@Scheduled` sin ShedLock; JSONL local si el monitor está on | ShedLock (o equivalente) antes de multi-réplica; monitor a stdout/objeto, no disco local | Rodrigo Aspeti | Antes de escalar réplicas |

## 6. Autoverificación (la marca el asistente; el grupo la confirma)

- [x] Las seis comprobaciones F12-01 a F12-06 están en la sección 3, una vez cada una.
- [x] Ninguna quedó en `PENDIENTE` ni sin estado; todos los estados son de los cinco permitidos.
- [x] Todo estado distinto de `N/A` y `NO VERIFICABLE` tiene evidencia con ruta y línea, o con comando y salida.
- [x] Cada `NO VERIFICABLE` y cada `N/A` explica su motivo. *(ninguno en esta auditoría)*
- [x] Todas las preguntas de la sección 4 están respondidas o marcadas "sin respuesta".
- [x] Los hallazgos de severidad alta tienen responsable.
- [x] No hay ningún secreto copiado en este archivo.
- [x] No se modificó código del producto (`git diff --stat <commit auditado>..HEAD` solo muestra este archivo y el de aportes). *(confirmar al commitear)*
- [ ] Cada integrante aparece como auditor de al menos una comprobación y tiene al menos un commit con su propia identidad. *(pendiente del commit del grupo; n = 1 → Rodrigo Aspeti)*

## 7. Resumen

| OK | PARCIAL | FALLA | N/A | NO VERIFICABLE | Preguntas sin respuesta |
|---:|--------:|------:|----:|---------------:|------------------------:|
| 2 | 3 | 1 | 0 | 0 | 0 |

**Conclusión en dos líneas:** Config por env, logs a stdout y backing services intercambiables están bien encaminados. Antes de contenedorizar (S07) hace falta Dockerfile + apagado graceful, y conviene endurecer fail-fast / quitar defaults sensibles del YAML.
