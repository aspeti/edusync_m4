# syntax=docker/dockerfile:1
# LAB S08 — Opción A — versión "DESPUÉS": multi-stage, capas ordenadas, JRE, usuario no root.

# ---------- Etapa 1: compilar (JDK + Maven) ----------
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /build

# Primero solo el pom: si no cambian las dependencias, esta capa se reutiliza.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# Después el código (cambia más seguido).
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package \
 && cp target/edusync-backend-*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ---------- Etapa 2: ejecutar (solo JRE + jar en capas) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Usuario sin privilegios (no root).
RUN groupadd --system app && useradd --system --gid app --no-create-home app

# De la capa que menos cambia a la que más cambia.
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

USER app
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
