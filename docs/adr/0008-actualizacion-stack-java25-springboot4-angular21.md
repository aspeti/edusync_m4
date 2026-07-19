# Architecture Decision Record (ADR)

## ADR-0008: Actualización de stack para la capa viva — Java 25 LTS + Spring Boot 4.1.0 + Angular 21

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0008` |
| Título | Actualización de stack para la capa viva (`docs/product/`) — Java 25 LTS + Spring Boot 4.1.0 (Spring Framework 7.0.8) + Angular 21 LTS |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Stack tecnológico vigente para `release/3.0.0` en adelante (capa viva). El baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`) permanece documentado con Java 21 / Spring Boot 3.3 / Angular 17 y no se modifica. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |

### 1. Contexto

El Documento Técnico Inicial (`docs/DTI.md`) y el baseline congelado de M4 (`docs/baseline/DTI.md`, tag `release/2.0.0`) fijaron el stack autoritativo en Java 21 (LTS) + Spring Boot 3.3 + Angular 17 + PostgreSQL 15 RLS. Ese stack fue correcto para el ciclo de especificación y evaluación del Módulo 4, donde el objetivo era producir documentación evaluable, no código en producción.

Al cerrar M4 y abrir la fase de implementación (`release/3.0.0`, ver `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`), el directorio `src/` está **vacío** (`⚠ pendiente de implementación` en `AGENTS.md` §3): no existe código productivo que migrar. Esta condición *greenfield* elimina el costo de migración que normalmente frenaría una adopción temprana de versiones mayores nuevas, y abre la pregunta de si conviene fijar el stack vivo en las versiones más recientes disponibles a la fecha (julio 2026) en lugar de heredar automáticamente las versiones usadas para especificar en M4.

Las fuerzas en tensión son: **actualidad del stack** (madurez de virtual threads, AOT Cache, Signal Forms) vs. **madurez del ecosistema** (documentación, terceros, respuestas de comunidad) vs. **coherencia con las decisiones ya congeladas del baseline** (ADR-0001..0006, que no dependen de una versión específica del framework, solo del motor PostgreSQL 15 y del patrón arquitectónico).

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. Mantener el stack del baseline (Java 21 / Spring Boot 3.3 / Angular 17) también para la capa viva | Cero riesgo de incompatibilidad; coherencia total con la documentación de M4; máxima disponibilidad de ejemplos y librerías de terceros | Se abandona voluntariamente la ventaja de partir de cero (`src/` vacío) para adoptar mejoras ya estables (AOT Cache, virtual threads maduros, Signal Forms); el stack quedaría desactualizado al momento de la defensa de `release/3.0.0` en el siguiente módulo | Sin costo de migración; costo de oportunidad por quedar un ciclo de LTS atrás |
| B. Máxima actualidad: Spring Boot 4.1.0 (Spring Framework 7.0.8) + Angular 22 | Última versión estable de cada componente; AOT Cache de Boot 4 sobre Java 25 disponible desde el día uno | Angular 22 recién liberado (03/06/2026): menor horas de vuelo en producción, menor cantidad de respuestas de comunidad/StackOverflow, mayor riesgo de encontrar bugs de día 1 en librerías de terceros del ecosistema Angular | Sin costo de migración (greenfield); riesgo de estabilidad ligeramente mayor en el frontend |
| C. Punto medio elegido: Spring Boot 4.1.0 (Spring Framework 7.0.8) + Angular 21 LTS | Adopta la serie nueva de Spring Boot (Jakarta EE 11, AOT Cache sobre Java 25) sin esperar; mantiene el frontend en la versión Angular con mayor ventana de soporte a largo plazo (LTS hasta mayo de 2027) en lugar de la recién lanzada Angular 22; equilibrio entre actualidad backend y madurez frontend | Angular 21 queda un release "detrás del filo" respecto a Angular 22; Spring Boot 4.1.0 sigue siendo una serie mayor nueva (menor antigüedad que la línea 3.x) | Sin costo de migración (greenfield); riesgo de estabilidad bajo-medio, mitigado por el mayor tiempo de maduración de Angular 21 |

### 3. Decisión

> **Elegimos la Alternativa C: Java 25 (LTS) + Spring Boot 4.1.0 (Spring Framework 7.0.8) + Angular 21 (LTS) + PostgreSQL 15 RLS (sin cambio) como stack vigente de la capa viva (`docs/product/`, `release/3.0.0` en adelante).**

El baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`) **no se modifica**: sigue documentando Java 21 / Spring Boot 3.3 / Angular 17 como el stack con el que se especificó y evaluó el proyecto. Este ADR únicamente fija el stack de la implementación que arranca en `release/3.0.0`, registrado como delta en `docs/product/DTP.md` §A.2.

La justificación de la Alternativa C sobre la B es de gestión de riesgo: dado que EduSync es mantenido por un equipo de una sola persona, el frontend (Angular) se beneficia más de la estabilidad de una versión LTS con ventana de soporte larga (mayo de 2027) que de estar en la última release de seis semanas. El backend, en cambio, sí adopta la última serie de Spring Boot (4.1.0) porque el beneficio del AOT Cache sobre Java 25 y de Jakarta EE 11 es inmediato y no depende de la madurez del ecosistema de terceros en la misma medida que un SPA framework.

### 4. Consecuencias

#### 4.1 Positivas

- Arranque de `release/3.0.0` sin deuda técnica de versión: no hay código legado que migrar desde Spring Boot 3.3/Spring Framework 6 hacia Spring Boot 4.1.0/Spring Framework 7.
- Disponibilidad del AOT Cache de Spring Boot 4.x (reemplazo de CDS) apenas el proyecto empaquete su primera imagen Docker, mejorando tiempo de arranque en ECS Fargate (relevante para `ADR-0006`).
- Angular 21 LTS da una ventana de soporte de aproximadamente un año desde la adopción, evitando una migración de frontend forzada durante la implementación activa.
- Java 25 LTS mantiene la base de virtual threads y records ya prevista en el baseline (`AGENTS.md` §4), con mejoras incrementales de rendimiento sobre Java 21.

#### 4.2 Negativas / costos

- Spring Boot 4.1.0 requiere Jakarta EE 11 y Spring Security 7.x / Spring Data 4.x: toda dependencia de terceros que se incorpore (Resilience4j de `ADR-0005`, librerías PDF, JWT) debe verificarse contra esa línea antes de fijar versión en `pom.xml`.
- Menor cantidad de ejemplos, tutoriales y respuestas de comunidad para Spring Boot 4.x comparado con la línea 3.x, al ser una serie mayor reciente.
- El baseline (M4) y la capa viva (`release/3.0.0`) documentan explícitamente dos stacks distintos; cualquier agente o persona que lea la documentación debe distinguir cuál aplica según si está en `docs/baseline/` o en `docs/product/`.

#### 4.3 Neutras / observables

- PostgreSQL 15 RLS no cambia: las decisiones de `ADR-0001` (multitenancy RLS) y `ADR-0003` (audit_log append-only) siguen vigentes sin modificación.
- `ADR-0006` (AWS ECS Fargate) no cambia; solo se actualiza la imagen base Docker de OpenJDK 21 a OpenJDK 25 cuando se genere el primer `Dockerfile` real.
- El estilo arquitectónico hexagonal (`docs/arquitectura_hexagonal_EduSync.md`) es agnóstico a la versión del framework: los puertos y adaptadores no cambian de diseño por este ADR.

### 5. Impacto en el sistema

- **Código**: cuando se cree `pom.xml` (todavía no existe), la versión parent debe ser `org.springframework.boot:spring-boot-starter-parent:4.1.0` sobre `<java.version>25</java.version>`. Cuando se cree el proyecto Angular (todavía no existe), `ng new` debe fijar la versión `21.x` (`@angular/cli@21`).
- **Documentación**: `docs/product/DTP.md` §A.2 registra este delta contra `docs/baseline/DTI.md`; `AGENTS.md` §4 documenta la dualidad de stack (baseline vs. vivo); `docs/roadmap.md` referencia este ADR en el horizonte `release/3.0.0`.
- **Seguridad**: Spring Security 7.x (parte de Spring Boot 4.1.0) reemplaza a Spring Security 6.x; los filtros `JwtAuthFilter` y la configuración de `SecurityConfig` descritos en `docs/LFSD-EduSync.md` deben revisarse contra la API de Spring Security 7.x al implementarse, sin cambiar el diseño funcional (JWT, RBAC, `TenantContextProvider`).
- **Equipo**: Rodrigo Aspeti (único integrante) debe validar, al iniciar la implementación, que las dependencias de terceros previstas (Resilience4j, PDFBox, cliente HTTP para el SIE) publican artefactos compatibles con Jakarta EE 11 / Spring Framework 7.0.8.
- **Costo**: sin impacto en la factura AWS estimada en `ADR-0006`; el cambio es de versión de runtime/framework, no de arquitectura de despliegue.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si al iniciar la implementación una dependencia crítica (Resilience4j, cliente SIE, librería PDF) no publica artefacto compatible con Spring Boot 4.1.0 / Jakarta EE 11 dentro de un plazo razonable, o si Angular 21 presenta un defecto bloqueante sin parche disponible.
- **Costo estimado de revertir**: bajo, dado que no hay código productivo aún (`src/` vacío); revertir implica solo cambiar la versión parent de Maven y la versión del CLI de Angular antes de escribir la primera línea de código de dominio.
- **Plan B**: descender a Spring Boot 3.5.x (última de la línea 3.x, mismo major que el baseline) manteniendo Java 25 LTS y Angular 21, sin reabrir este ADR salvo que el bloqueo persista.

### 7. Validación

- **Verificación de build**: el primer `mvn -v` / `mvn dependency:tree` tras crear `pom.xml` debe confirmar `Spring Boot 4.1.0`, `Spring Framework 7.0.8` y JDK `25` activos, sin warnings de incompatibilidad de Jakarta EE.
- **Verificación de frontend**: `ng version` tras `ng new` debe confirmar Angular `21.x`; el pipeline CI de frontend (cuando exista) fija la versión de Node.js compatible con Angular 21.
- **Golden test de arranque**: primer test de humo (`ContextLoadsTest` o equivalente) que confirme que el contexto de Spring Boot 4.1.0 levanta correctamente sobre Java 25 antes de aceptar cualquier PR de implementación.

### 8. Referencias

- `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md` (modelo documental que abre la capa viva y motiva este ADR).
- `docs/product/DTP.md` §A.2 (delta de stack vs. DTI vFinal).
- `docs/baseline/DTI.md` §4 (stack tecnológico congelado de M4 — Java 21 / Spring Boot 3.3 / Angular 17, sin cambios).
- `AGENTS.md` §4 (stack tecnológico autoritativo — dualidad baseline/vivo).
- `docs/roadmap.md` (horizonte `release/3.0.0`).
- `ADR-0001` (Multitenancy RLS — PostgreSQL 15 sin cambio).
- `ADR-0005` (Resiliencia SIE con Resilience4j — dependencia a verificar contra Jakarta EE 11).
- `ADR-0006` (Cloud provider AWS ECS Fargate — imagen Docker base a actualizar a OpenJDK 25).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado al abrir la capa viva de implementación (`release/3.0.0`) según `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`; fija Java 25 LTS + Spring Boot 4.1.0 + Angular 21 LTS como stack vigente para `docs/product/`; el baseline de M4 permanece en Java 21 / Spring Boot 3.3 / Angular 17; estado Aceptada |
