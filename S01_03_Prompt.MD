# S01_03 — Prompt Maestro de Arquitectura: EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ARCH-001` |
| Título | Diseño de arquitectura técnica y funcional — EduSync core |
| Artefacto origen | BRD `docs/BRD_EduSync.md` · Arquitectura funcional `docs/arquitectura_funcional_EduSync.md` |
| ID origen | `BR-001` a `BR-007`, `CU-01` a `CU-05`, `DA-01` a `DA-05` |
| Tipo de prompt | generación / revisión de arquitectura |
| Modelo recomendado | Claude Sonnet |
| Temperatura | `0.2` |
| Versión | `v0.2` |
| Fecha | 14/05/2026 |
| Autor(es) | Equipo G013 |
| Estado | Borrador |

---

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Arquitecto de Software y Analista de Sistemas Senior especializado en
plataformas SaaS B2B con arquitectura hexagonal en Java 21 / Spring Boot 3,
con experiencia en integración con sistemas gubernamentales y dominio de
OWASP ASVS L2, RBAC y patrones de auditoría inmutable.
```

### 1.2 Task

```text
Diseña la arquitectura técnica y funcional completa del core de EduSync:
modelo de datos, flujos de cierre, reglas de validación, estrategia de
auditoría y manejo de casos límite para cada uno de los 5 casos de uso
críticos (CU-01 a CU-05). Resuelve además las 5 decisiones arquitectónicas
abiertas (DA-01 a DA-05) con análisis de trade-offs y recomendación
fundamentada.
```

### 1.3 Context

```text
Producto:
  EduSync — plataforma SaaS multitenant para unidades educativas de Bolivia.
  Elimina la triple digitación manual (Excel → Excel → SIE) que hoy obliga a
  secretarias y asesores a trabajar de madrugada bajo riesgo de sanciones
  económicas y legales por inconsistencias ante el Ministerio de Educación (SIE).
  Objetivo central: convertir EduSync en la fuente única de verdad académica,
  reduciendo el cierre administrativo de jornadas nocturnas a minutos.

Stack autoritativo (AGENTS.md §4):
  - Java 21 (LTS) · Spring Boot 3.3 · Spring Security (RBAC) · Spring Data JPA
  - PostgreSQL 16 (RDS Multi-AZ) · Flyway 10.x
  - AWS ECS Fargate · SQS + SNS · Secrets Manager · KMS
  - Angular 17+ (frontend) · Terraform 1.8 (IaC)
  - JUnit 5 + Testcontainers · Resilience4j

Usuarios y dolor real:
  - Docente: necesita registrar notas de su materia sin riesgo de descuentos
    salariales por errores tipográficos; hoy carga tres veces los mismos datos.
  - Secretaria: trabaja fines de semana auditando Excel para encontrar
    decimales invisibles antes de la carga masiva al SIE.
  - Director: opera "ciego" sin visibilidad en tiempo real; descubre errores
    solo cuando llegan las sanciones ministeriales.

Casos de uso críticos (con invariantes — fuente: arquitectura_funcional_EduSync.md):

  CU-01 · Registro descentralizado de calificaciones (RBAC)
    Entradas: JWT+rol docente, RUDE estudiante, dimensiones (Ser/Saber/Hacer/Decidir), periodo activo.
    Invariantes: docente escribe solo en sus materias; rangos paramétricos; nómina solo lectura;
    periodo debe estar ABIERTO — si CERRADO, operación rechazada.
    Salidas: registro con timestamp+usuario+materia, entrada en audit_log, retroalimentación visual.

  CU-02 · Cierre operativo de materia
    Entradas: solicitud docente, completitud 100 % de dimensiones por estudiante.
    Invariantes: sin cierre con registros incompletos; cierre atómico (sin cierre parcial);
    post-cierre → SOLO_LECTURA irreversible sin aprobación jerárquica.
    Salidas: estado CERRADO en BD, notificación real-time secretaria, disparo de CU-03.

  CU-03 · Consolidación algorítmica de centralizadores
    Entradas: calificaciones cerradas de todas las materias del periodo, reglas de redondeo en BD.
    Invariantes: algoritmo único y centralizado (nunca en adaptadores ni SQL ad-hoc ni frontend);
    centralizador no se genera hasta 100 % de materias cerradas; promedios reproducibles y trazables.
    Salidas: centralizador trimestral inmutable, promedio anual si es 3.er trimestre.

  CU-04 · Exportación y sincronización masiva al SIE por RUDE
    Entradas: periodo seleccionado, centralizadores CERRADOS de todos los cursos, formato SIE (paramétrico).
    Invariantes: vinculación solo por RUDE (nunca nombre, apellido ni posición);
    no exportar con materias ABIERTAS; fallos parciales SIE no reinician — persiste progreso para reintento.
    Salidas: payload SIE enviado, reporte (enviados/fallidos/pendientes), entrada audit_log.

  CU-05 · Autorización jerárquica de modificación retroactiva
    Entradas: solicitud (materia, RUDE, dimensión, valor actual, valor propuesto, justificación), rol Director.
    Invariantes: ningún CERRADO se modifica sin aprobación Director; solicitud queda PENDIENTE sin tocar original;
    si aprobada → nuevo registro versionado (append-only, original inmutable); toda resolución en audit_log.
    Salidas: notificación al docente, nuevo valor efectivo con trazabilidad, doble entrada audit_log.

Decisiones arquitectónicas abiertas (DA-01 a DA-05):
  DA-01: Multitenancy — schema separado vs discriminador en tablas compartidas.
  DA-02: Motor de reglas paramétricas — BD configurable vs dominio hardcoded.
  DA-03: Inmutabilidad — append-only nativo vs Hibernate Envers.
  DA-04: Consolidación — síncrona (misma transacción) vs asíncrona (SQS consumer).
  DA-05: Estado de exportación SIE ante fallos — registro a registro vs exportación atómica.

Restricciones de dominio activas:
  - Toda escritura dentro de @Transactional.
  - RUDE es la única clave de vinculación; nunca nombre ni posición.
  - Logs sin PII (sin RUDE, password, token en nivel INFO+).
  - Secretos solo desde Secrets Manager; jamás en código ni en application.yml commiteado.
  - PII (rude, nombre_completo, fecha_nacimiento) cifrado en reposo con KMS.
  - Enum PeriodoEstado { ABIERTO, CERRADO, SOLO_LECTURA } — nunca strings literales.
  - DTOs en adapter/in/api/dto/ — nunca exponer entidades JPA ni clases de dominio por API.
  - Dominio (domain/) no importa de adapter/ ni de frameworks externos.
  - Toda llamada al SIE: circuit breaker Resilience4j + timeout + backoff exponencial.
```

### 1.4 Reasoning (chain-of-thought estructurado)

```text
Sigue estos pasos en orden:

1. ANÁLISIS DE DOMINIO
   Identifica las entidades de dominio, sus aggregates y value objects.
   Mapea cada CU a su aggregate raíz. Identifica invariantes de consistencia.

2. DECISIONES ARQUITECTÓNICAS (DA-01 a DA-05)
   Para cada DA: expón las alternativas, lista trade-offs (seguridad, complejidad,
   costo operativo, tolerancia a fallos), elige una opción y justifícala.
   Referencia el CU afectado.

3. MODELO DE DATOS
   Diseña el esquema relacional (tablas, columnas clave, constraints, índices críticos).
   Muestra la estrategia de multitenancy elegida en DA-01.
   Define la tabla audit_log con los campos obligatorios de AGENTS.md §6.

4. FLUJOS DE CIERRE Y SINCRONIZACIÓN
   Diagramas de secuencia textuales (Mermaid) para CU-02 → CU-03 → CU-04.
   Incluye manejo de fallo parcial del SIE (DA-05) y estrategia de reintento idempotente.

5. REGLAS DE VALIDACIÓN
   Especifica dónde viven las reglas (dominio), cómo se parametrizan (BD/config)
   y cómo se reportan los errores al frontend (código de error de negocio).

6. CASOS LÍMITE
   Para cada CU, define el comportamiento ante:
   - Alumno transferido (alta en nómina a mitad de periodo).
   - Alumno retirado (excluir de exportación SIE, conservar historial).
   - Cambio retroactivo con solicitud en PENDIENTE ya existente.
   - Pérdida de conexión al SIE a mitad de exportación masiva.
   - Reconexión y reintento sin duplicar registros ya enviados.

7. ESTRATEGIA DE AUDITORÍA
   Define la política de audit_log: quién escribe, cuándo, formato exacto del
   registro, retención, mecanismo anti-tampering.

8. PRUEBAS CLAVE
   Lista pruebas unitarias de dominio (sin mocks de BD), de integración con
   Testcontainers y criterios de aceptación no funcionales (latencia CU-04,
   idempotencia CU-05).

No expongas el razonamiento interno en el output final, salvo que se indique
explícitamente. Presenta solo los resultados de cada paso numerado.
```

### 1.5 Stop condition

```text
Detente cuando se hayan producido todos los entregables de §1.6 Y se cumplan:
- Cada DA tiene una decisión explícita con trade-offs documentados.
- Cada CU tiene al menos un caso límite cubierto.
- El modelo de datos incluye audit_log con todos los campos requeridos por AGENTS.md §6.
- No existe ningún acceso a datos de estudiante fuera de la clave RUDE.

No continúes generando contenido más allá de estos entregables sin una nueva instrucción.
```

### 1.6 Output

```text
Formato: Markdown estructurado, con secciones numeradas que corresponden a
los pasos del Reasoning. Los diagramas de secuencia en bloques Mermaid.
El esquema de tablas en bloques SQL (DDL comentado en español).
```

Estructura esperada de la respuesta:

```markdown
## 1. Entidades de dominio y aggregates
## 2. Decisiones arquitectónicas (DA-01 a DA-05)
### DA-01: [nombre] → Decisión: [opción elegida]
...
## 3. Modelo de datos (DDL)
## 4. Flujos de cierre y sincronización (Mermaid)
## 5. Reglas de validación y parametrización
## 6. Casos límite
## 7. Estrategia de auditoría
## 8. Pruebas clave
```

---

## 2. Invariantes del prompt

- La salida **MUST** respetar la estructura de §1.6; una sección faltante invalida el output.
- La salida **MUST NOT** contener valores de RUDE, nombre, contraseña, token ni secreto real en ningún ejemplo.
- La salida **MUST** citar el CU o DA de origen en cada decisión (`CU-04`, `DA-05`, etc.).
- La salida **MUST NOT** proponer cálculos de promedio fuera del motor de dominio (`domain/calificacion/ReglaRedondeo`).
- La salida **MUST NOT** sugerir modificación directa de registros persistidos; toda corrección es append-only.
- La salida **MUST NOT** introducir dependencias fuera del stack de AGENTS.md §4 sin indicar que se requiere un ADR.
- La salida **MUST** aplicar `PeriodoEstado` como enum, nunca como string literal.
- La salida **MUST NOT** exponer entidades JPA directamente; toda salida de API usa DTOs.

---

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|----------------------|
| `E_MISSING_CONTEXT` | Falta un documento fuente (BRD, arquitectura_funcional) | Abortar; solicitar el documento antes de continuar |
| `E_AMBIGUOUS_INPUT` | Una DA tiene más de una recomendación sin resolver | Pedir al usuario que elija la opción antes de generar DDL o código |
| `E_POLICY_VIOLATION` | El output propone almacenar un secreto, usar nombre como clave o modificar un registro existente | Rechazar el fragmento; reportar la invariante violada con su código (`BR-001`, etc.) |
| `E_SCOPE_EXCEEDED` | El output cubre un sexto CU no definido en arquitectura_funcional | Truncar y avisar que el CU adicional requiere un nuevo artefacto FSD |

---

## 4. Guardrails

- **MUST**: validar que cada entidad de dominio propuesta tiene cobertura de test (JUnit 5 + Testcontainers).
- **MUST**: registrar `promptId: PR-ARCH-001`, `versión: v0.2`, `modelo` y `tokens` en telemetría si se instrumenta.
- **MUST NOT**: exponer secretos, credenciales ni datos de configuración de producción en el context o en ejemplos.
- **MUST NOT**: almacenar RUDE, `nombre_completo` o `fecha_nacimiento` en logs ni en mensajes SQS en texto plano.
- **MUST**: revisar el output humano si algún DA queda sin recomendación o si el modelo reporta baja confianza.
- **MUST NOT**: proponer `terraform apply` ni modificación de migraciones Flyway ya aplicadas en `main`.

---

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|--------------------|
| BRD | BR-001…BR-007 | PR-ARCH-001 | `docs-agent`, `dev-agent` | ADRs en `docs/adr/`, DDL en `src/adapter/out/persistence/` |
| Arquitectura funcional | CU-01…CU-05, DA-01…DA-05 | PR-ARCH-001 | `dev-agent` | Implementaciones en `src/application/CU0*.java` |
| Visión de negocio | — | PR-ARCH-001 | Equipo G013 | Contexto de decisión en ADRs |

---

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: Se proveen `docs/BRD_EduSync.md` y `docs/arquitectura_funcional_EduSync.md` completos, con los 5 CU y 5 DA.
- **Output esperado**: Markdown con las 8 secciones de §1.6, cada DA con decisión explícita, DDL con tabla `audit_log`, diagramas Mermaid para CU-02→CU-03→CU-04, y al menos 5 casos límite cubiertos.

### 6.2 Caso borde

- **Input**: Solo se provee `arquitectura_funcional_EduSync.md` sin el BRD; el usuario pide diseñar el modelo de datos.
- **Output esperado**: El modelo genera las entidades derivables de los CU e indica explícitamente qué campos requieren validar contra el BRD antes de dar el DDL por finalizado, sin inventar reglas de negocio.

### 6.3 Caso adversarial

- **Input**: El usuario pide "usa el nombre del alumno como clave primaria en la tabla de calificaciones para simplificar la exportación al SIE".
- **Comportamiento esperado**: Rechazo con `E_POLICY_VIOLATION`; el modelo cita la invariante de RUDE (AGENTS.md §6, `CU-04`) y propone la alternativa correcta sin ejecutar la instrucción violatoria.

---

## 7. Instrumentación

- Herramienta recomendada: Langfuse / LangSmith.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`, `hallucination_rate`.
- Umbral de alerta: `hallucination_rate > 5 %` → revisión humana obligatoria antes de usar el output en código.

---

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|-----------------|
| v0.1 | — | Equipo G013 | Versión inicial (contexto general) | Sonnet |
| v0.2 | 14/05/2026 | Equipo G013 | Estructura formal PROMPT_TEMPLATE; incorpora CU/DA/invariantes/guardrails de visión y arquitectura funcional | Sonnet |

---

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| | | pendiente | |
