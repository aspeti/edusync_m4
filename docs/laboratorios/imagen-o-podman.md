# Laboratorio S08: imagen o Podman

- Integrante(s): Rodrigo Aspeti (G-EduSync, n = 1)
- Opción: **A** (auditar y adelgazar la imagen)
- Servicio o stack: `api` (backend Spring Boot 4.1 / Java 25) del stack `infra/docker-compose.yml` (con PostgreSQL 15)
- Fecha: 14–15/11/2026 (medición "antes": 14/11 21:30; "después": 15/11 10:37)
- Máquina: Windows (x64), Docker Desktop 4.84.0 (Engine 29.6.2, cliente 29.6.2, contexto `desktop-linux`), BuildKit; imágenes base Ubuntu 26.04 con Temurin JDK/JRE 25.0.5

> El repositorio no tenía Dockerfile. Por indicación del laboratorio (opción A), el punto de partida
> ("antes") es un primer Dockerfile de una sola etapa (`backend/Dockerfile.antes`) y el "después" es
> `backend/Dockerfile` (multi-stage). Ambos construyen con `docker build` sobre el contexto `backend/`.

## Opción A: antes y después

Imagen: `mi-imagen:antes` / `mi-imagen:despues` (Dockerfiles: `backend/Dockerfile.antes`, `backend/Dockerfile`)

| Medida | Antes | Después | Comando con el que se midió |
|---|---:|---:|---|
| Tamaño en disco (DISK USAGE) | 1.77 GB | 763 MB (−57 %) | `docker image ls` |
| Tamaño comprimido (CONTENT SIZE) | 657 MB | 252 MB (−62 %) | `docker image ls` |
| Número de capas | 35 | 25 | `docker image history -q` |
| Eficiencia (Dive) | 84.36 % | 99.66 % | `dive` con `CI=true` |
| Espacio desperdiciado (Dive) | 329 MB (34.4 % del añadido por el usuario) | 2.5 MB (0.66 %) | `dive` con `CI=true` |
| Usuario con el que corre | `root` (`uid=0`) | `app` (`uid=999`) | `docker run --rm --entrypoint id ...` |
| CVE totales (Trivy 0.76.1) | 183 (SO 72 + jars 110 + 1 UNKNOWN) | 101 (SO 52 + jars 48 + 1 UNKNOWN) (−45 %) | `trivy image --scanners vuln` |
| CVE HIGH + CRITICAL | 67 (47 HIGH + 20 CRITICAL) | 28 (18 HIGH + 10 CRITICAL) (−58 %) | `trivy ... --severity HIGH,CRITICAL` |
| Avisos de hadolint | 0 | 1 (`DL3066`, info) | `hadolint` (v2.15.1) |
| Tiempo de build sin caché | 103.7 s | 102.2 s | `scripts/lab-s08-medir.ps1` |
| ¿Arranca y responde? | Sí (`API_DOCKERFILE=Dockerfile.antes`, `docker compose up -d --build --wait`; `GET /v3/api-docs` respondió) | Sí (`docker compose up -d --build --wait`; `GET /v3/api-docs` respondió) | endpoint probado: `GET http://localhost:8080/v3/api-docs` |

Evidencia cruda: `docs/laboratorios/evidencia/antes.txt` y `docs/laboratorios/evidencia/despues.txt`.

> **Seguimiento (16/11/2026):** tras Spring AI, `/v3/api-docs` puede devolver 500 por conflicto `swagger-annotations` vs `swagger-annotations-jakarta`. Mitigado en `backend/pom.xml` (pin 2.2.47 + exclusión); ver `docs/product/DTP.md` v1.62 y `README.md`.

### Qué encontré en la imagen de antes
- **Capas grandes** (`docker image history`): `RUN mvn -B -DskipTests package` (391 MB: compilación más el caché de Maven `~/.m2`), `COPY . .` (170 MB: copia todo el contexto, incluida la carpeta `target/` local), el JDK de la imagen base (265 MB), `apt-get` de la imagen de Maven (104 MB) y el sistema base (117 MB). Para **ejecutar** el servicio solo hacen falta el JRE y el jar; Maven, el JDK completo, el código fuente y el caché sobran.
- **Espacio desperdiciado** (Dive): 329 MB (efficiency 84.36 %). Casi todo es el jar duplicado: `/app/target/edusync-backend-0.1.0-SNAPSHOT.jar` aparece en dos capas (298 MB desperdiciados) porque el `target/` local se copió con `COPY . .` y luego `mvn package` lo regeneró. También viajaron el `.jar.original`, los reportes de JaCoCo (`target/site/jacoco/...`) y archivos de build, que no deben estar en una imagen de ejecución.
- **Usuario**: `Config.User` vacío y `uid=0(root)`: el servicio corre como root.
- **CVE** (Trivy): 183 en total; 67 HIGH/CRITICAL. Vienen de dos lugares: del **sistema operativo** (Ubuntu 26.04: 72, todas LOW/MEDIUM, ninguna HIGH/CRITICAL) y de **dependencias Java** (110 en jars, de ellas 47 HIGH y 20 CRITICAL). Las dependencias incluyen las de la aplicación (`tomcat-embed-core`, `jackson`, `netty`, `spring-webmvc`, `spring-webflux`…) y además las de las propias herramientas de build que quedaron en `/root/.m2` (p. ej. `guava`, `commons-io`, `aircompressor`).
- **hadolint**: sin avisos en el Dockerfile inicial. No detecta el usuario root ni el jar duplicado; esos hallazgos salen de Dive y de `id` (coincide con lo que advierte el laboratorio sobre hadolint).

### Qué cambié
1. **`.dockerignore`** (`backend/.dockerignore`): excluye `target`, `.git`, `.idea`, `.env*`, logs; el contexto de build deja de arrastrar la carpeta `target/` local.
2. **Multi-stage**: la etapa `build` (JDK + Maven) compila; la etapa final solo trae el JRE y el jar. Ya no viajan Maven, el JDK, el código fuente ni el caché `~/.m2`.
3. **Orden de capas**: primero `pom.xml` + `dependency:go-offline`, después `src/`. Un cambio de código no vuelve a descargar dependencias.
4. **Caché de Maven en el build** (`RUN --mount=type=cache,target=/root/.m2`): el caché no queda dentro de ninguna capa de la imagen.
5. **Jar en capas** (`-Djarmode=tools ... extract --layers`): dependencias, loader y aplicación en capas separadas; un cambio de código solo invalida la capa `application`.
6. **Imagen base más chica**: `eclipse-temurin:25-jre` en la etapa final (en lugar de `maven:3-eclipse-temurin-25`).
7. **Usuario no root**: se crea el usuario `app` y se declara `USER app`.
8. **Sin secretos en la imagen**: el `.env` queda fuera por `.dockerignore`; la configuración (DB, perfil) entra por variables de entorno en `infra/docker-compose.yml`.

### Qué no pude resolver o no me convenció
- **CVE en dependencias Java**: siguen 28 HIGH/CRITICAL en `mi-imagen:despues`, y todas vienen de las dependencias de la aplicación (`tomcat-embed-core`, `jackson-databind`/`jackson-core`, `netty-*`, `spring-webmvc`/`spring-webflux`, `postgresql`). Cambiar la imagen base no las arregla: hay que actualizar versiones (BOM de Spring Boot) y volver a escanear. Del sistema operativo no queda ninguna HIGH/CRITICAL (ni antes ni después).
- **Las CVE del SO bajaron (72 → 52)** solo por usar un JRE sin Maven ni herramientas de build; la base sigue siendo Ubuntu 26.04. Una base distroless o Docker Hardened Image reduciría más, a costa de no tener shell.
- **Tiempo de build**: prácticamente igual sin caché (103.7 s vs 102.2 s). La mejora de capas y `go-offline` solo se nota en reconstrucciones **con** caché (no se midió).
- **hadolint `DL3066`** (info) en la versión final: `USER app` no es numérico. Se resuelve con `USER 999:999`; no se aplicó para dejar la medición tal como se hizo.
- **Etiquetas sin digest**: las imágenes base (`maven:3-eclipse-temurin-25`, `eclipse-temurin:25-jre`) usan etiqueta flotante; el laboratorio recomienda fijar versión exacta y digest (`docker image inspect --format "{{index .RepoDigests 0}}" <imagen>`). Pendiente.
- **Comparabilidad**: "antes" se midió el 14/11 y "después" el 15/11; la base de datos de Trivy y las imágenes base pueden haberse actualizado entre ambas corridas, así que una parte pequeña de la diferencia de CVE puede no deberse a los cambios del Dockerfile. Fue una sola corrida en la misma máquina.

## Comandos usados
```powershell
cd C:\Users\Admin\Documents\Maestria\edusync_m4
docker version
.\scripts\lab-s08-medir.ps1 -Etiqueta antes
.\scripts\lab-s08-medir.ps1 -Etiqueta despues
docker compose -f infra/docker-compose.yml up -d --build --wait
docker compose -f infra/docker-compose.yml ps
curl http://localhost:8080/v3/api-docs
docker compose -f infra/docker-compose.yml down
```
(Las herramientas Dive, Trivy y hadolint corren como contenedores con versión y digest fijos dentro del script.)
