# Architecture Decision Record (ADR)

## ADR-0011: Monolito modular con Spring Modulith (module-first) y nuevo paquete base `com.edusync`

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0011` |
| Título | Modularización interna del backend con Spring Modulith (module-first, un módulo por capacidad de negocio) y renombrado del paquete base de `bo.edusync` a `com.edusync` |
| Fecha | 14/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Estructura interna del artefacto desplegable único del backend (`src/backend/`) para `release/3.0.0`. No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`), que permanece intacto como registro histórico — incluida su referencia narrativa al paquete `bo.edusync` en `docs/arquitectura_hexagonal_EduSync.md`, que se actualiza como spec viva, no como baseline. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0008` (stack Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS), `ADR-0009` (generalización del modelo de dominio a plataforma SaaS multi-tenant), `ADR-0010` (multi-rol + `SysAdmin` sin tenant permanente) |

### 1. Contexto

`ADR-0009` generalizó el modelo de dominio de EduSync de un producto mono-institución boliviano a una plataforma SaaS multi-tenant con capacidades de negocio claramente separables: plataforma (`SysAdmin`/`Tenant`), identidad (`Usuario`/`UsuarioRol`/login), estructura académica configurable (`GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion`/`TipoEvaluacion`/`Evaluacion`/`Curso`/`Paralelo`/`Materia`/`Estudiante`/`Inscripcion`) y el Perfil Bolivia SIE que se conserva sin cambios (`Calificacion`/`Consolidacion`/`ExportacionSIE`/RUDE/`floor()`).

Al iniciar la implementación de código (`DD-UC-001`, bootstrap del proyecto sobre `src/` vacío) es necesario decidir cómo se organiza internamente el backend para que esa separación de capacidades sea explícita y verificable en el código, sin renunciar a los beneficios operativos de un monolito (un solo artefacto desplegable, un solo pipeline de CI/CD, sin la complejidad de red/observabilidad distribuida que las 5 fuerzas confirmadas en `docs/baseline/DTI.md` §6 descartan para el tamaño de equipo actual).

Un segundo tema queda acoplado a esta decisión: el paquete base actual (`bo.edusync`, usado en `docs/arquitectura_hexagonal_EduSync.md` desde M4) refleja el origen boliviano específico del producto. `ADR-0009` ya generalizó el negocio a una plataforma SaaS agnóstica de país; mantener un prefijo de dos letras de país (`bo.`) en el paquete raíz de **todo** el código — incluidas las capacidades genéricas de plataforma (`plataforma`, `identidad`) que no tienen nada de específico boliviano — transmitiría una señal arquitectónica contradictoria con esa decisión.

Nota de alcance: esta decisión es puramente de **organización de paquetes/módulos en código** (`src/backend/`); no reabre el modelo de dominio (`ADR-0009`/`ADR-0010`) ni el stack (`ADR-0008`), y no afecta la separación de carpetas del repositorio (`backend/`/`frontend/`/`infra/`), que es un nivel distinto (organización del repositorio, no arquitectura en tiempo de ejecución del JAR del backend).

### 2. Alternativas consideradas

**Para la modularización interna del backend:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Paquete-por-capa clásico (un único árbol `domain/`, `application/`, `infrastructure/` en la raíz, como documenta hoy `docs/arquitectura_hexagonal_EduSync.md` §1 para el Perfil Bolivia SIE), con las nuevas capacidades (`plataforma`, `identidad`, `academico`) añadidas como sub-paquetes dentro de las mismas tres carpetas | Cambio mínimo sobre la arquitectura ya documentada; sin curva de aprendizaje adicional | A medida que crecen las capacidades (5+: plataforma, identidad, académico, notas SIE, shared) el árbol `domain`/`application`/`infrastructure` mezcla conceptos de negocio no relacionados en las mismas carpetas; nada impide que código de `identidad` llame directamente a una clase interna de `academico`, degradando el acoplamiento con el tiempo sin que el build lo detecte | Bajo a corto plazo; alto a mediano plazo (erosión de límites sin enforcement automático) |
| B. Monolito modular **module-first** con Spring Modulith: un paquete de primer nivel por capacidad de negocio (`plataforma`, `identidad`, `academico`, `notassie`, `shared`), cada uno con su propia sub-estructura hexagonal (`domain`/`application`/`infrastructure`) y con `ApplicationModules.of(...).verify()` (test JUnit) validando en cada build que no hay ciclos ni accesos a paquetes internos de otro módulo | Límites de módulo explícitos y verificados automáticamente en CI (falla el build si se rompe un límite); sigue siendo un único JAR desplegable (sin infraestructura de red adicional); migración futura a microservicios (si el negocio lo requiere) parte de límites ya validados, sin *strangler* adicional; Spring Modulith es soportado directamente por el equipo de Spring y es compatible con Spring Boot 4.1.0 (`ADR-0008`) | Requiere una dependencia adicional (`spring-modulith-starter-test`) y una convención nueva (un `package-info.java` opcional por módulo); disciplina inicial de no crear imports cruzados entre `domain` de módulos distintos | Bajo — una dependencia de test, sin impacto en runtime; el costo real es disciplina de diseño, no infraestructura |
| C. Multi-módulo Maven (un módulo Maven/JAR por capacidad: `plataforma/`, `identidad/`, `academico/`, `notassie/`, `shared/`, más un módulo agregador `app/` que ensambla el `bootJar` final) | Límites físicamente imposibles de violar (el compilador de Maven bloquea el acceso si no hay dependencia declarada en el `pom.xml` del módulo) | Sobre-ingeniería para un equipo de 1 desarrollador en esta etapa: cada cambio que toque 2 capacidades exige tocar 2+ `pom.xml`; tiempos de build más largos por resolución de módulos; el propio DTI (`docs/baseline/DTI.md` §6, POC-Seams) ya identificó que la descomposición prematura en unidades desplegables separadas no está justificada hoy | Alto — fricción de build y mantenimiento de `pom.xml` sin beneficio adicional sobre la Alternativa B para el tamaño de equipo actual |

**Para el paquete base:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| D. Mantener `bo.edusync` como paquete raíz de todo el código, incluidas las capacidades genéricas de plataforma | Cero cambio sobre lo ya documentado en `docs/arquitectura_hexagonal_EduSync.md` | Contradice la señal de `ADR-0009` (generalización a plataforma SaaS agnóstica de país): un desarrollador nuevo leería `bo.edusync.plataforma.SysAdmin` y asumiría (incorrectamente) que la administración de tenants es específica de Bolivia | Bajo hoy; confuso a mediano plazo para onboarding y para un eventual open-core/multi-mercado |
| E. Renombrar el paquete raíz a `com.edusync` (convención estándar de dominio invertido, sin código de país), manteniendo el nombre de producto (`edusync`) | Consistente con la generalización de `ADR-0009`; convención de paquete Java estándar (`com.<organización>`) reconocible por cualquier desarrollador; no depende de tener el dominio `edusync.com` registrado formalmente hoy (práctica común: se reserva la convención antes de registrar el dominio) | Ningún costo real porque `src/` está vacío (greenfield) — si hubiera código existente, renombrar un paquete raíz sería un cambio mecánico pero amplio | Nulo — `src/` vacío, sin migración de código real |

### 3. Decisión

> **Elegimos la Alternativa B (monolito modular con Spring Modulith, module-first) combinada con la Alternativa E (paquete base `com.edusync`)**.

Estructura de módulos resultante (backend, un solo artefacto Spring Boot desplegable):

```
com.edusync
├── EduSyncApplication.java        (clase main, en la raíz — visibilidad de Spring Modulith sobre todos los módulos)
├── plataforma/                    (SysAdmin, Tenant, Suscripcion — FSD-UC-011)
│   ├── domain/
│   ├── application/ (port/in, port/out, service)
│   └── infrastructure/ (adapter/in/rest, adapter/out/persistence)
├── identidad/                     (Usuario, UsuarioRol, login/JWT, RBAC — FSD-UC-021 + autenticación)
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── academico/                     (GestionEscolar, PeriodoEvaluacion, SeccionEvaluacion, TipoEvaluacion,
│   ├── domain/                     Evaluacion, Curso, Paralelo, Materia, Estudiante, Inscripcion — FSD-UC-012..020)
│   ├── application/
│   └── infrastructure/
├── notassie/                      (Perfil Bolivia SIE: Calificacion, Consolidacion, ExportacionSIE,
│   ├── domain/                     RUDE, floor() — FSD-UC-001/003/004/005/009, preservado sin cambios)
│   ├── application/
│   └── infrastructure/
└── shared/                        (TenantContext, audit_log, excepciones comunes, utilidades transversales;
    ├── tenant/                     sin Aggregate Roots propios — "shared kernel" explícito de Spring Modulith)
    ├── audit/
    └── exception/
```

1. Cada paquete de primer nivel bajo `com.edusync` (`plataforma`, `identidad`, `academico`, `notassie`, `shared`) es un **módulo de Spring Modulith**. Ningún módulo importa clases internas (`domain`/`application`/`infrastructure`) de otro módulo directamente; la comunicación entre módulos ocurre vía los puertos públicos expuestos (interfaces en la raíz del módulo, patrón *Open Host Service*) o eventos de dominio (Spring Events, ya decidido en `ADR-0004` para consolidación asíncrona).
2. `shared` es un módulo especial (*shared kernel*, sin Aggregate Root propio) visible para todos los demás; ningún otro módulo puede depender de él en sentido inverso.
3. Se añade un test de arquitectura `ModularityTests` (JUnit 5 + `spring-modulith-starter-test`) que ejecuta `ApplicationModules.of(EduSyncApplication.class).verify()` en cada build de CI; el build **falla** si se detecta un ciclo entre módulos o un acceso a un paquete interno no expuesto.
4. El paquete base de **todo** el código nuevo pasa de `bo.edusync` (M4, específico de Bolivia) a `com.edusync` (genérico, alineado con `ADR-0009`). Esto incluye al módulo `notassie`, que preserva el 100% de sus reglas de negocio del Perfil Bolivia SIE (RUDE, `floor()`, 3 trimestres) — el cambio es solo de paquete Java, no de comportamiento.
5. `docs/arquitectura_hexagonal_EduSync.md` (spec viva, no baseline) se actualiza para reflejar `com.edusync.notassie.*` en lugar de `bo.edusync.*` y para documentar la nueva organización module-first; el `docs/baseline/DTI.md` y el resto de `docs/baseline/**` permanecen intactos con su referencia histórica a `bo.edusync`, como registro fiel de lo evaluado en M4.

### 4. Consecuencias

#### 4.1 Positivas

- Los límites entre capacidades de negocio (plataforma SaaS vs. identidad vs. académico genérico vs. Perfil Bolivia SIE) quedan verificados automáticamente en cada build, no solo documentados.
- El nombre de paquete deja de transmitir una señal contradictoria con la generalización ya decidida en `ADR-0009`.
- Se conserva un único artefacto desplegable (sin costos de red/observabilidad distribuida), consistente con las 5 fuerzas de `docs/baseline/DTI.md` §6 que descartan microservicios para el tamaño de equipo actual.
- Si en el futuro el negocio confirma la necesidad de extraer un módulo como servicio independiente (ej. `notassie` como servicio de exportación SIE de alto volumen), los límites de Spring Modulith ya validados reducen el costo de ese *strangler* (`ADR-0007`, hoy *gated*).

#### 4.2 Negativas / costos

- Disciplina adicional: cualquier PR que introduzca un import cruzado entre `domain` de dos módulos distintos rompe el build (`ModularityTests`); esto es intencional pero exige que el equipo entienda la regla desde el primer commit.
- `docs/arquitectura_hexagonal_EduSync.md` requiere una actualización de contenido (renombrado de paquete + reorganización module-first) antes de que el primer PR de código se apoye en ese documento como referencia.

#### 4.3 Neutras / observables

- No se crea ningún ADR que "supersede" a `ADR-0008`, `ADR-0009` o `ADR-0010`; los tres permanecen `Aceptada` sin cambios. Este ADR-0011 es una decisión de organización de código derivada de ellos.
- `src/` sigue vacío al momento de esta decisión (greenfield): no hay costo de migración de código real, solo de la spec `docs/arquitectura_hexagonal_EduSync.md`.

### 5. Impacto en el sistema

- **Documentación**: `docs/design/DD-UC-001.md` (Design Doc de bootstrap del proyecto) cita este ADR como decisión de arquitectura de referencia. `docs/arquitectura_hexagonal_EduSync.md` se actualiza (paquete `com.edusync`, organización module-first) como tarea derivada, sin tocar `docs/baseline/**`. `docs/product/DTP.md` §A.1/§A.2 registra este ADR como delta.
- **Código**: cuando se implemente `PR-IMPL-001` (bootstrap), la estructura de paquetes de `src/backend/src/main/java/com/edusync/` debe seguir exactamente el árbol de la sección 3; el test `ModularityTests` debe existir desde el primer commit de código de dominio.
- **Seguridad**: sin impacto directo; el módulo `identidad` sigue siendo el único responsable de JWT/RBAC (`ADR-0010`).
- **Equipo**: cualquier Design Doc (`DD-UC-NNN`) que introduzca una capacidad de negocio nueva debe declarar explícitamente a qué módulo de Spring Modulith pertenece (o si requiere un módulo nuevo).
- **Costo**: una dependencia de test adicional (`spring-modulith-starter-test`), sin impacto en tiempo de ejecución ni en infraestructura.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si `ModularityTests` se convierte en un obstáculo recurrente que el equipo empieza a *silenciar* (ej. `@Modulithic(sharedModules = {...})` usado para evitar el enforcement en vez de para casos legítimos), o si la separación en 5 módulos resulta insuficiente/excesiva una vez completado el primer módulo real.
- **Costo estimado de revertir**: bajo — no hay código productivo (`src/` vacío); revertir a paquete-por-capa (Alternativa A) implica mover clases de paquete sin cambiar lógica, un *refactor* mecánico soportado por el IDE.
- **Plan B**: si Spring Modulith resulta insuficiente, se documenta un ADR de seguimiento (`ADR-0012` o siguiente disponible) que evalúe la Alternativa C (multi-módulo Maven) con evidencia real del tamaño del código en ese momento.

### 7. Validación

- **Revisión de consistencia documental**: `docs/arquitectura_hexagonal_EduSync.md` y `docs/design/DD-UC-001.md` deben citar este ADR y usar `com.edusync` en todo ejemplo de código o paquete.
- **Checklist de no divergencia**: `docs/baseline/**` no queda editado; solo se reescribe la spec viva `docs/arquitectura_hexagonal_EduSync.md`.
- **Verificación automatizada**: `ModularityTests` (`ApplicationModules.of(EduSyncApplication.class).verify()`) debe pasar en CI desde el primer commit de `src/backend/` que contenga más de un módulo con clases de dominio.

### 8. Referencias

- `ADR-0008` (stack Java 25 LTS / Spring Boot 4.1.0 — Spring Modulith es compatible con esta versión).
- `ADR-0009` (generalización del modelo de dominio; motiva el renombrado de paquete).
- `ADR-0010` (multi-rol + `SysAdmin`; el módulo `identidad` implementa esta decisión).
- `docs/baseline/DTI.md` §6 (Seams / POC-Seams; confirma que la descomposición en unidades desplegables separadas no está justificada hoy).
- `docs/design/DD-UC-001.md` (Design Doc de bootstrap que consume esta decisión).
- `docs/product/DTP.md` §A.2 (delta registrado a partir de este ADR).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 14/07/2026 | Rodrigo Aspeti | ADR formal creado durante el diseño de `DD-UC-001` (bootstrap del proyecto); decide monolito modular Spring Modulith module-first (5 módulos: `plataforma`, `identidad`, `academico`, `notassie`, `shared`) y renombrado del paquete base `bo.edusync` → `com.edusync`; estado Aceptada |
