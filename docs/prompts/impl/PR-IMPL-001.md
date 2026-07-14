# PR-IMPL-001 — Bootstrap del esqueleto de código (backend Java 25 + frontend Angular 21 + infra)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-001` |
| Título | Generación del esqueleto Maven (monolito modular Spring Modulith) + esqueleto Angular 21 + `docker-compose.yml` de desarrollo |
| Artefacto origen | `docs/design/DD-UC-001.md` |
| ID origen | `DD-UC-001` (`FSD-UC-011`, `FSD-UC-021`) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 14/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

> **Convención de ruta**: los prompts del área `IMPL` (`PR-IMPL-NNN.md`) viven en `docs/prompts/impl/`, siguiendo exactamente `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5 y `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`. Esto es una excepción deliberada a la convención histórica de M4 (`prompts/PR-<AREA>-NNN.md` en el directorio raíz, que sigue vigente para las áreas `ARCH`/`BRD`/`MRD`/`PRD`/`FSD`/`LFSD`/`UC`/`ADR`/`AUD`/`INF`/`DIAG`/`SKILL`/`C4`/`DTI`/`HEX`/`DTO`/`POC`/`ROADMAP`/`APORTES`/`VFINAL`): el área `IMPL` es la única que usa `docs/prompts/impl/`, porque nace después de M4 junto con la capa viva (`docs/design/`, `docs/product/`) y sigue su propia plantilla.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Software Engineer con experiencia en arquitectura hexagonal,
monolitos modulares con Spring Modulith, Spring Boot 4.x sobre Java 25 LTS,
y bootstrapping de proyectos Angular 21 con standalone components.
```

### 1.2 Task

```text
Genera el esqueleto de código fuente de EduSync para release/3.0.0: estructura
de carpetas backend/frontend/infra, pom.xml del backend con Spring Modulith,
los 5 módulos vacíos bajo el paquete com.edusync, el test ModularityTests,
el esqueleto Angular 21 del frontend y el docker-compose.yml de desarrollo,
exactamente como se describe en docs/design/DD-UC-001.md §2.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-001.md (§1 objetivo, §2 diseño con árbol de carpetas completo).
- ADRs aplicables: ADR-0008 (Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS),
  ADR-0011 (monolito modular Spring Modulith, module-first, paquete base com.edusync).
- Módulos backend a crear (vacíos, solo estructura + package-info.java):
  com.edusync.plataforma, com.edusync.identidad, com.edusync.academico,
  com.edusync.notassie, com.edusync.shared.
- Restricciones de dominio: ningún módulo debe contener lógica de negocio en
  este prompt (eso corresponde a DD-UC-002 en adelante); solo estructura,
  configuración y el test de arquitectura.
- Restricciones técnicas: Java 25 (LTS), Spring Boot 4.1.0 (Spring Framework 7.0.8),
  Spring Modulith (starter-test), PostgreSQL 15 (driver + Flyway), Angular 21 (LTS,
  standalone components), sin Nx.
```

### 1.4 Reasoning

```text
1. Crear la estructura de carpetas backend/, frontend/, infra/ en la raíz del repo.
2. Generar backend/pom.xml (parent Spring Boot 4.1.0, Java 25, dependencias:
   spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security,
   spring-modulith-starter-core + spring-modulith-starter-test, postgresql, flyway-core).
3. Crear com.edusync.EduSyncApplication y los 5 paquetes de módulo (domain/application/
   infrastructure vacíos con .gitkeep o package-info.java) según el árbol de DD-UC-001 §2.
4. Crear backend/src/test/java/com/edusync/ModularityTests.java con
   ApplicationModules.of(EduSyncApplication.class).verify().
5. Crear application.yml/application-dev.yml/application-test.yml y
   db/migration/V1__init.sql (Flyway baseline vacío).
6. Generar frontend/ con Angular 21 (standalone), carpetas core/shared/features vacías.
7. Crear infra/docker-compose.yml con PostgreSQL 15 para desarrollo local.
8. Verificar que mvn -q -DskipTests=false test y ng build no fallan (smoke test).
```

### 1.5 Stop condition

```text
Detente cuando: (a) el árbol de carpetas coincide exactamente con DD-UC-001 §2,
(b) ModularityTests compila y pasa en verde, (c) ng build del frontend no falla,
(d) ningún archivo generado contiene lógica de dominio (solo estructura/config).
No continúes implementando entidades, endpoints ni pantallas — eso es DD-UC-002+.
```

### 1.6 Output

```text
Formato: código fuente real en backend/, frontend/, infra/ (no markdown).
Ejemplo de estructura esperada (extracto):
backend/src/main/java/com/edusync/EduSyncApplication.java
backend/src/main/java/com/edusync/plataforma/package-info.java
backend/src/test/java/com/edusync/ModularityTests.java
frontend/src/app/app.config.ts
infra/docker-compose.yml
```

## 2. Invariantes del prompt

- El paquete base de todo el código Java **debe** ser `com.edusync` (`ADR-0011`), nunca `bo.edusync`.
- Ningún módulo (`plataforma`/`identidad`/`academico`/`notassie`) **debe** importar clases internas de otro módulo; solo `shared` puede ser importado por todos.
- `ModularityTests` **debe** existir y pasar antes de dar por cerrado este prompt.
- El esqueleto **no debe** incluir credenciales reales en `application*.yml` (usar variables de entorno o valores de desarrollo local no sensibles).
- El frontend **debe** generarse como una única aplicación Angular 21 (sin Nx, sin múltiples proyectos).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_PAQUETE_INCORRECTO` | Código generado bajo `bo.edusync` o cualquier paquete distinto de `com.edusync` | Rechazar y regenerar bajo el paquete correcto |
| `E_ACOPLAMIENTO_ENTRE_MODULOS` | Import directo entre `domain`/`application` de dos módulos distintos (fuera de `shared`) | `ModularityTests` debe fallar el build; corregir antes de mergear |
| `E_LOGICA_PREMATURA` | Se generó lógica de negocio (entidades JPA, endpoints reales) en este prompt | Revertir; esa lógica corresponde a `DD-UC-002` en adelante |

## 4. Guardrails

- MUST: generar únicamente estructura/configuración, sin lógica de dominio.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST: dejar `ModularityTests` en verde antes de considerar el prompt completo.
- MUST NOT: modificar ningún archivo bajo `docs/baseline/**`.
- MUST NOT: exponer secretos ni credenciales reales en `application*.yml`/`docker-compose.yml`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-001` | PR-IMPL-001 | `dev-agent` | `backend/`, `frontend/`, `infra/docker-compose.yml` |
| ADR | `ADR-0011` | PR-IMPL-001 | `dev-agent` | Estructura de paquetes `com.edusync.*` + `ModularityTests` |
| FSD | `FSD-UC-011`, `FSD-UC-021` | PR-IMPL-001 | `dev-agent` | Base de código habilitante (sin lógica de negocio todavía) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `docs/design/DD-UC-001.md` completo + `ADR-0011` aprobado.
- **Output esperado**: árbol de carpetas exacto de §2, `ModularityTests` en verde, `ng build` sin errores.

### 6.2 Caso borde

- **Input**: `DD-UC-001.md` sin uno de los 5 módulos listados (ej. falta `notassie`).
- **Output esperado**: el agente crea igualmente los 5 módulos definidos en `ADR-0011` (fuente de verdad de la lista de módulos), y señala la inconsistencia detectada en el DD.

### 6.3 Caso adversarial

- **Input**: solicitud de "aprovechar" este prompt para ya implementar el endpoint de login o el alta de tenant.
- **Comportamiento esperado**: rechazo con `E_LOGICA_PREMATURA`; se limita a estructura y remite a `DD-UC-002`/`DD-UC-003`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `modularity_test_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/07/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-001.md` v1.0 y `ADR-0011` (materializado inicialmente en `prompts/PR-IMPL-001.md`) | Sonnet |
| v0.2 | 14/07/2026 | Rodrigo Aspeti | **Corrección de ruta**: movido de `prompts/PR-IMPL-001.md` a `docs/prompts/impl/PR-IMPL-001.md`, siguiendo `FEATURE_DESIGN_DOC_TEMPLATE.md`/`MODELO_DOCUMENTAL_IMPLEMENTACION.md`. Sin cambios de contenido (Role/Task/Context/Reasoning/Stop/Output/Invariants/Failure modes idénticos) | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 14/07/2026 | aprobado (prompt) | Prompt aprobado; ejecución del prompt (generación real de código en `backend/`/`frontend/`/`infra/`) queda **pendiente** como siguiente paso |
