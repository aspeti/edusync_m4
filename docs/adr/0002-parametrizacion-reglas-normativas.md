# Architecture Decision Record (ADR)

## ADR-0002: Parametrización de reglas normativas sin redespliegue

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0002` |
| Título | Parametrización de reglas normativas sin redespliegue |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Motor de validación de calificaciones y motor de consolidación — todo el sistema |
| Stakeholders consultados | Directores de unidades educativas, Equipo de arquitectura G-EduSync |

### 1. Contexto

El Ministerio de Educación de Bolivia puede modificar sin previo aviso los rangos de calificación por dimensión pedagógica (Ser, Saber, Hacer, Decidir, Autoevaluación), el criterio de truncado de decimales o el formato exacto de exportación al SIE. Históricamente, cada cambio ministerial obligaba a los colegios a actualizar sus planillas Excel manualmente y a reenviar todo el lote de calificaciones.

El análisis de archivos Excel reales (`Centralizador2A_ColegioAbaroa.xlsx`, `REGISTRO SECUNDARIA 2026.xlsx`) revela que dos colegios del mismo mercado usan estructuras de dimensiones distintas:

- Colegio Abaroa: Ser (5 pts) · Saber (45 pts) · Hacer (40 pts) · Decidir (5 pts)
- Colegio con Autoevaluación: Ser (5 pts) · Saber (45 pts) · Hacer (40 pts) · Decidir (5 pts) · Autoevaluación (5 pts)

Además, el mismo colegio aplica truncado inconsistente: `64.666…` aparece como `22` en un campo y `23` en otro dentro del mismo Excel. Hardcodear estas reglas en el código fuente haría inviable responder a cambios ministeriales sin redespliegue y QA completo.

Las fuerzas en tensión son: **flexibilidad ante cambio ministerial** vs. **riesgo de configuración incorrecta por operadores no técnicos** vs. **velocidad de respuesta ante cambio**.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. Valores hardcodeados en el dominio Java (`bo.edusync.domain`) | Sin infraestructura adicional; el compilador detecta errores de tipo | Cualquier cambio ministerial requiere commit + pipeline CI + despliegue (días); no soporta diferencias entre tenants | Cero costo de infraestructura, pero alto costo operativo ante cambios |
| B. Configuración en `application.yml` versionado en Git | Sin BD adicional; cambio controlado por PR | Requiere commit + despliegue (horas); no soporta diferencias entre tenants/periodos; el historial es el historial de Git, no el del periodo académico | Bajo — solo costo de CI/CD |
| C. Tabla de configuración paramétrica en PostgreSQL con alcance `tenant + periodo` | Cambio en minutos sin redespliegue; alcance por tenant y por periodo; versionado y auditable en BD; el valor por defecto ministerial es sobrescribible por tenant | Requiere UI de administración para que el Director configure parámetros; riesgo de configuración incorrecta por un Director sin asistencia técnica | Medio — requiere tabla `parametro_academico` + endpoints de administración |

### 3. Decisión

> **Elegimos la Alternativa C: tabla de configuración paramétrica en PostgreSQL con alcance `tenant + periodo`.**

Los parámetros que controlan el comportamiento del motor de dominio (dimensiones activas, pesos, regla de combinación de evaluaciones, criterio de truncado, umbral de reprobación y formato SIE) se almacenan como registros editables con versión y fecha de vigencia en la tabla `parametro_academico`. Esta tabla se carga al abrir cada periodo académico (UC-09) y sus valores son inmutables durante la vigencia del periodo (no pueden modificarse una vez que el Director abre el periodo).

El valor por defecto de cada parámetro refleja la normativa ministerial vigente; cada tenant puede sobrescribir su valor dentro de los rangos permitidos por la normativa.

### 4. Consecuencias

#### 4.1 Positivas

- Respuesta ante cambio ministerial: el administrador actualiza la tabla `parametro_academico` en minutos, sin redespliegue ni intervención del equipo de desarrollo.
- Soporte nativo a diferencias entre tenants: Colegio Abaroa y Colegio con Autoevaluación conviven en la misma instancia con configuraciones distintas sin bifurcación de código.
- Trazabilidad completa: cada versión de los parámetros queda registrada con fecha de vigencia, autor y periodo asociado.
- El motor de dominio lee los parámetros al inicio de cada operación de escritura (UC-01) y los almacena en caché por `(tenant_id, periodo_id)` durante el tiempo de vida de la sesión.

#### 4.2 Negativas / costos

- Requiere implementar la tabla `parametro_academico` con DDL y políticas RLS (coherente con ADR-0001).
- El Director debe configurar los parámetros antes de abrir cada periodo; un olvido puede resultar en parámetros incorrectos por defecto.
- Aumenta la complejidad del bootstrap de UC-09: abrir un periodo implica validar que todos los parámetros obligatorios están configurados.
- Los tests de dominio deben poblar la tabla `parametro_academico` en el contexto de prueba (Testcontainers PostgreSQL).

#### 4.3 Neutras / observables

- Los parámetros son inmutables una vez que el periodo está `ABIERTO` (BR-007): esto impide que un cambio de configuración afecte calificaciones ya registradas en el periodo vigente.
- El criterio de truncado (`floor`) es un parámetro más de la tabla, pero su validación en CI mediante `FloorTest` es independiente del valor en BD: el golden test verifica que el código nunca use `round()` o `HALF_UP` en el dominio.

### 5. Impacto en el sistema

- **Código**: nueva entidad `ParametroAcademico` en `bo.edusync.domain.model.gestionacademica`; nuevo puerto de salida `ParametroAcademicoRepository` en `bo.edusync.domain.port.out`; adaptador JPA en `bo.edusync.infrastructure.adapter.out.persistence`. El motor de consolidación `ConsolidacionDomainService` recibe los parámetros por inyección en el constructor (no por lookup directo a BD).
- **Operaciones**: la tabla `parametro_academico` incluye la política RLS de ADR-0001; la migración Flyway V002 crea la tabla con los valores por defecto ministeriales.
- **Seguridad**: solo el rol `DIRECTOR` puede modificar parámetros; los roles `DOCENTE` y `SECRETARIA` tienen acceso de solo lectura. El `tenant_id` asegura que un Director no vea ni modifique parámetros de otro colegio.
- **Equipo**: los contratos de UC-01, UC-03 y UC-09 deben documentar cuáles parámetros consumen.
- **Costo**: sin costo adicional de infraestructura; la tabla vive en la misma instancia RDS.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si los Directores cometen errores sistemáticos de configuración que producen calificaciones incorrectas en más del 5 % de los periodos, o si la complejidad de la UI de administración supera la capacidad del equipo.
- **Costo estimado de revertir**: 1–2 semanas para migrar los valores de la tabla a `application.yml` versionado (Alternativa B); el modelo de dominio no cambia, solo la fuente de los parámetros.
- **Plan B**: mantener los valores ministeriales por defecto en `application.yml` como fallback si la tabla `parametro_academico` no tiene entrada para un `(tenant_id, periodo_id)` dado.

### 7. Validación

- **Golden test `FloorTest`**: `FloorTest.floor_64_666_equals_64()` — verifica que el motor de consolidación aplica `Math.floor()` y nunca `Math.round()`, `RoundingMode.HALF_UP` ni `Math.ceil()`. Ejecuta en CI en cada merge a `release/*` y bloquea el merge si falla.
- **Test de inmutabilidad de parámetros**: `ParametroAcademicoTest.parametro_inmutable_en_periodo_abierto()` — verifica que el intento de modificar un parámetro de un periodo en estado `ABIERTO` devuelve `HTTP 409 Conflict`.
- **Métrica**: cero errores de tipo `E_PARAMETRO_NO_ENCONTRADO` en logs de producción para periodos en estado `ABIERTO` (alertado en CloudWatch).

### 8. Referencias

- `FSD-UC-01` (Registro de calificaciones — la validación del rango de valor usa `rango_min` y `rango_max` de `parametro_academico`).
- `FSD-UC-03` (Consolidación — el criterio de truncado y la regla de combinación de evaluaciones dentro de una dimensión se leen de `parametro_academico`).
- `FSD-UC-04` (Exportación SIE — el formato de mapeo de escala `floor(nota/3)` es un parámetro configurable).
- `FSD-UC-09` (Administración de periodos — al abrir un periodo se fijan y congelan todos los parámetros).
- `BR-002` (El valor de calificación debe estar en el rango `[rango_min, rango_max]` definido para la dimensión activa del periodo).
- `BR-007` (Los parámetros del periodo son inmutables una vez que el periodo está `ABIERTO`).
- `BR-008` (El cálculo de promedio y escala SIE es exclusivo del motor de dominio — no en SQL ni en frontend).
- `DA-02` en `docs/arquitectura_funcional_EduSync.md`.
- Archivos Excel de referencia: `Centralizador2A_ColegioAbaroa.xlsx`, `REGISTRO SECUNDARIA 2026.xlsx`.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado a partir de DA-02 en arquitectura_funcional_EduSync.md; estado Aceptada |
