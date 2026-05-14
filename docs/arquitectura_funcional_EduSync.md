# Arquitectura Funcional del Core — EduSync

<!-- Stack: Java 21 · Spring Boot 3 · PostgreSQL · Angular · AWS -->

## Encuadre del Core EduSync

EduSync es una plataforma SaaS B2B multitenant cuyo motor central resuelve el problema de la triple digitación manual que obliga al personal de colegios bolivianos a trabajar de madrugada bajo riesgo de sanciones ministeriales: cada unidad educativa (tenant) opera con aislamiento total de datos, sus docentes registran calificaciones exclusivamente en sus propias materias mediante un control de acceso por rol (RBAC), y el sistema muestra un centralizador provisional en tiempo real a medida que las notas se ingresan —permitiendo que docentes, secretaría y director vean el avance sin esperar al cierre total— mientras el centralizador oficial e inmutable solo se genera al cerrar el 100 % de las materias. Cuando un periodo ya está cerrado, cualquier corrección de nota requiere que el docente solicite autorización al director, quien define el alcance (un estudiante específico o el curso completo) y una ventana de tiempo con fecha de expiración: al vencer ese plazo el sistema bloquea automáticamente el acceso sin acción manual, y si el docente olvidó subir alguna nota, debe solicitar una nueva autorización. Todo el flujo vincula estudiantes por su código RUDE, nunca por nombre ni posición de lista, y deja un rastro de auditoría inalterable en cada operación, convirtiendo a EduSync en la fuente única de verdad académica para directores, secretarías y docentes.

---

## Diez Casos de Uso Críticos

> **Trazabilidad UX:** Marcela (Docente · UC-01, UC-02, UC-08) · Wendy (Secretaría · UC-03, UC-04, UC-07, UC-09) · Jeanneth (Directora · UC-05, UC-09, UC-10).

---

### UC-01 · Registro descentralizado de calificaciones por dimensión

| Campo | Detalle |
|-------|---------|
| **Actores** | Docente (operativo) |
| **Entradas** | Sesión autenticada con JWT y rol DOCENTE · Código RUDE del estudiante (nunca nombre, número de lista ni posición visual) · Nota cruda en escala 0–100 por cada dimensión activa del periodo · **Tipo de nota por dimensión:** `REGULAR` o `AYUDA` (nota de recuperación/apoyo, cuya regla de combinación con la nota regular es paramétrica por tenant y periodo) · Identificador del periodo académico activo |
| **Invariantes** | El docente escribe únicamente en las materias que tiene asignadas (RBAC estricto) · Ningún valor fuera del rango paramétrico de la dimensión puede persistirse · La nómina de estudiantes es de solo lectura para el rol DOCENTE · El periodo debe estar en estado `ABIERTO`; si está `CERRADO` o en `SOLO_LECTURA`, la operación se rechaza con error `PERIODO_NO_MODIFICABLE` · El conjunto de dimensiones activas (p. ej. Ser/Saber/Hacer/Decidir ± Autoevaluación), sus pesos y la regla de combinación de nota AYUDA son parámetros definidos en UC-09 al abrir el periodo; el docente no puede crear ni eliminar dimensiones, pero si la cantidad de evaluaciones que se realiza en cada dimencion · La conversión de nota cruda (0–100) a la escala de reporte del SIE es responsabilidad exclusiva del motor de consolidación (UC-03); el docente nunca ingresa notas ya convertidas |
| **Salidas** | Registro persistido con timestamp, identidad del docente, materia, dimensión, tipo de nota y valor en escala cruda · Entrada inmediata en el log de auditoría (actor, dimensión, tipo, valor nuevo) · Retroalimentación visual inmediata al docente con el promedio provisional actualizado de ese estudiante |

---

### UC-02 · Cierre operativo de materia

| Campo | Detalle |
|-------|---------|
| **Actores** | Docente (solicitante) · Secretaría (receptora de notificación) |
| **Entradas** | Solicitud explícita de cierre del docente para su materia y periodo · Verificación de completitud: todos los estudiantes con nota registrada en **todas las evaluaciones de todas las dimensiones que el docente declaró** para esa materia en ese periodo |
| **Invariantes** | No se puede cerrar si existe algún estudiante de la nómina con al menos una evaluación declarada sin nota · La completitud se verifica contra el **conjunto de evaluaciones que el propio docente definió** (no contra un número fijo); si el docente declaró 3 evaluaciones en "Saber", los 3 campos deben estar llenos para todos los estudiantes · El cierre es atómico: no existe cierre parcial · Post-cierre, la materia transiciona a `SOLO_LECTURA` de forma irreversible sin aprobación jerárquica del Director · El docente no puede agregar nuevas evaluaciones a una dimensión una vez que solicita el cierre; la adición de evaluaciones solo está disponible mientras la materia esté `ABIERTO` |
| **Salidas** | Estado de la materia actualizado a `CERRADO` en la base de datos · Notificación en tiempo real al dashboard de la secretaría con el resumen de evaluaciones cerradas por dimensión · Disparo automático del proceso de consolidación (UC-03) cuando todas las materias del curso están cerradas |

---

### UC-03 · Consolidación algorítmica de centralizadores

| Campo | Detalle |
|-------|---------|
| **Actores** | Sistema (cálculo continuo y automático) · Docente (visualiza vista previa de su materia) · Secretaría (visualiza avance y resultado final) · Director (visualiza avance y resultado final) |
| **Entradas** | **Modo vista previa:** calificaciones ya registradas en materias con estado `ABIERTO` o `CERRADO` para el curso y periodo activo · Reglas de truncado, redondeo y **regla de combinación de evaluaciones dentro de cada dimensión** almacenadas como parámetro en base de datos · **Modo oficial:** calificaciones cerradas de la totalidad de materias del periodo para el curso |
| **Invariantes** | El algoritmo de redondeo es único y centralizado en el dominio; ningún cálculo de promedio ocurre en adaptadores, consultas SQL ad-hoc ni en el frontend · **Combinación de evaluaciones dentro de una dimensión:** cuando un docente registró N evaluaciones en una dimensión (p. ej. 4 notas en "Saber"), el motor las combina usando la regla paramétrica del periodo (`PROMEDIO_SIMPLE`, `SUMA`, `MEJOR_N`); el resultado es el puntaje de esa dimensión, que luego se escala al peso máximo definido en DA-02 · El motor aplica **piso (floor)** como criterio de truncado de decimales, no redondeo estándar ni redondeo bancario, para garantizar consistencia con la escala de reporte — este criterio es el único que elimina los descuadres de decimales largos observados en los registros Excel actuales (p. ej. `64.666…` → `64`, no `65`) · **Vista previa (provisional):** mientras existan materias en estado `ABIERTO`, el centralizador calcula y muestra promedios parciales en tiempo real a medida que los docentes ingresan notas; estos valores se marcan como `PROVISIONAL` y no tienen validez oficial · **Centralizador oficial:** solo se genera y bloquea en estado `CERRADO` cuando el 100 % de las materias del curso están cerradas; en ese momento el valor provisional es reemplazado por el oficial e inmutable · La vista previa no puede usarse para generar boletines (UC-07) ni para exportar al SIE (UC-04) · **Promedio anual e indicadores de reprobación final:** solo se calculan y muestran cuando los 3 trimestres del año académico están en estado `CERRADO`; con uno o dos trimestres cerrados, el sistema muestra únicamente los promedios de los trimestres disponibles con etiqueta `EN CURSO — promedio anual no disponible` para evitar falsos indicadores de reprobación total · Los promedios son matemáticamente reproducibles y trazables a sus calificaciones de origen en ambos modos |
| **Salidas** | **Vista previa continua:** tabla de promedios por estudiante y materia marcada como `PROVISIONAL — en curso`, visible en tiempo real para docentes, secretaría y director mientras el periodo está `ABIERTO` · **Centralizador oficial:** promedio trimestral calculado e inmutable, disponible para secretaría y director una vez que todas las materias estén `CERRADAS` · Promedio anual calculado solo al cerrar el 3.er trimestre; antes de eso, columna anual en blanco con etiqueta `EN CURSO` · Habilitación del flujo de generación de boletines (UC-07) solo al alcanzar estado oficial |

---

### UC-04 · Exportación y sincronización masiva al SIE por RUDE

| Campo | Detalle |
|-------|---------|
| **Actores** | Secretaría / Administrativo |
| **Entradas** | Periodo académico seleccionado · Centralizadores en estado `CERRADO` para todos los cursos del periodo · Formato de mapeo SIE vigente (almacenado como configuración paramétrica actualizable sin redespliegue) |
| **Invariantes** | La vinculación al SIE es exclusivamente por código RUDE; nunca por nombre, apellido ni posición visual en la lista · No se puede exportar un periodo que tenga materias en estado `ABIERTO` · Los alumnos transferidos con RUDE activo y notas completas se incluyen en el payload · Los alumnos retirados se excluyen del payload sin desplazar las calificaciones de los demás · **Filtro pre-exportación obligatorio:** antes de construir el payload SIE, el motor descarta automáticamente toda fila que tenga RUDE nulo, vacío o con formato inválido, y toda fila con nota nula en cualquier dimensión requerida; estas filas se reportan como `EXCLUIDAS_SIN_RUDE` o `EXCLUIDAS_NOTA_INCOMPLETA` en el reporte de resultado — nunca se envían al SIE como registros con valor 0 · Los fallos parciales del servidor SIE no reinician el proceso; el estado de progreso por estudiante se persiste para reintentos asíncronos |
| **Salidas** | Payload en el formato exacto del SIE generado y transmitido · Reporte de resultado desglosado: registros enviados, fallidos y pendientes · Alerta visual de estado (verde "Validado" / amarillo "Parcial" / rojo "Error") · Entrada en el log de auditoría de la operación completa con actor y timestamp |

---

### UC-05 · Autorización jerárquica de modificación retroactiva con ventana temporal

| Campo | Detalle |
|-------|---------|
| **Actores** | Docente (solicitante) · Director (autorizador y configurador de alcance y ventana) |
| **Entradas** | Solicitud del docente: materia, justificación escrita y **alcance propuesto**: (a) una evaluación específica dentro de una dimensión para un estudiante por RUDE (identificada por `dimensión + índice_de_evaluación + RUDE + valor_propuesto`), o (b) todo el curso para una dimensión/evaluación concreta · Decisión del Director: aprobación o rechazo, **alcance efectivo** (el Director puede restringir a un estudiante aunque el docente solicitara el curso completo) y **duración de la ventana** elegida por el Director dentro del rango permitido (mínimo: 1 hora — máximo: 72 horas; valor por defecto del sistema: 24 horas) |
| **Invariantes** | Ningún registro en materia `CERRADO` puede modificarse sin autorización explícita del Director, sin importar el estado general del periodo · **Alcance de la autorización:** el Director define si el docente puede modificar un único estudiante (RUDE específico) o la totalidad del curso en esa materia; el docente no puede ampliar el alcance recibido · **Ventana temporal obligatoria:** toda autorización de modificación incluye siempre una fecha y hora de expiración; no existe autorización de duración indefinida ni permanente — el sistema rechaza cualquier intento de aprobar sin ventana definida · La duración la elige el Director dentro del rango parametrizable (1 h – 72 h); si el Director no especifica un valor, el sistema aplica el valor por defecto de 24 horas · Al expirar la ventana, el sistema revoca automáticamente el permiso de escritura sin intervención manual ni del Director ni de la secretaría · **Durante la ventana:** el docente puede crear o corregir calificaciones solo dentro del alcance autorizado y su materia; las validaciones de rango de UC-01 permanecen activas · **Al expirar la ventana:** cualquier nota que el docente no haya ingresado queda bloqueada; si necesita modificar o agregar más notas, debe iniciar una nueva solicitud de autorización al Director; no existe prórroga automática · La solicitud permanece en `PENDIENTE` sin alterar el registro original mientras el Director no resuelve · Toda corrección aprobada genera un nuevo registro versionado con referencia al anterior (append-only); el registro original es inmutable y nunca se sobreescribe · El centralizador provisional (UC-03) se recalcula automáticamente al persistir cada cambio durante la ventana activa |
| **Salidas** | Notificación al docente con el estado de la resolución, el alcance autorizado y la fecha/hora exacta de expiración de la ventana · Alerta automática al docente cuando falten 30 minutos para que expire la ventana · Notificación de cierre automático al expirar, con resumen de notas modificadas y notas que quedaron sin ingresar · Si aprobada y ejecutada: nuevo valor efectivo con trazabilidad completa al registro original · Triple entrada en el log de auditoría: (1) solicitud del docente con justificación, (2) resolución del Director con alcance, duración elegida y timestamp de expiración, (3) cierre de ventana con inventario de cambios realizados y pendientes |

---

### UC-06 · Gestión de nóminas estudiantiles

| Campo | Detalle |
|-------|---------|
| **Actores** | Secretaría / Administrativo |
| **Entradas** | Datos del estudiante (RUDE, nombre completo, fecha de nacimiento, curso asignado) · Tipo de movimiento: alta, baja o transferencia · Periodo académico en el que aplica el movimiento |
| **Invariantes** | El RUDE es campo obligatorio sin valor por defecto; el sistema bloquea el guardado del alta de cualquier estudiante si el RUDE está vacío, nulo o tiene formato inválido — no existe alumno en EduSync sin RUDE · El RUDE es la clave única del estudiante; no se admite duplicado de RUDE dentro del mismo tenant · La nómina **nunca reasigna posiciones numéricas**: el alta de un alumno le asigna un identificador interno nuevo; la baja lo marca como `RETIRADO` sin alterar el identificador ni la posición de los demás estudiantes; esto elimina el riesgo de desplazamiento de notas observado en los registros Excel actuales · Una baja no elimina el historial de calificaciones registradas antes de la baja (inmutabilidad histórica) · Una transferencia entre cursos requiere que no existan calificaciones incompletas en el curso de origen · Los docentes no tienen permiso para modificar, agregar ni eliminar registros de nómina |
| **Salidas** | Nómina actualizada con el nuevo estado del estudiante · Registro en el log de auditoría del movimiento (actor, tipo, RUDE afectado) · Notificación a los docentes del curso afectado |

---

### UC-07 · Generación de boletines académicos oficiales

| Campo | Detalle |
|-------|---------|
| **Actores** | Secretaría · Director · Sistema (habilitado automáticamente tras UC-03) |
| **Entradas** | Centralizador cerrado del curso y periodo · Plantilla de boletín oficial vigente (paramétrica, actualizable sin redespliegue) · Identidad del solicitante |
| **Invariantes** | Solo se puede generar el boletín si el centralizador del periodo está en estado `CERRADO` · El boletín refleja los datos inmutables del centralizador; no admite edición antes de la generación · El formato, encabezados e información institucional respetan la plantilla ministerial oficial parametrizada |
| **Salidas** | Documento PDF de boletín académico listo para impresión y distribución como respaldo legal · Registro en el log de auditoría de la generación (actor, curso, periodo, timestamp) |

---

### UC-08 · Control de asistencia por materia

| Campo | Detalle |
|-------|---------|
| **Actores** | Docente |
| **Entradas** | Sesión autenticada con JWT y rol DOCENTE · RUDE del estudiante · Fecha de la clase · Estado de asistencia: presente, ausente o justificado · Materia y periodo activo |
| **Invariantes** | El docente registra asistencia únicamente en su materia asignada (RBAC) · No se registra asistencia en fechas correspondientes a periodos `CERRADOS` · Un registro del día puede rectificarse dentro del mismo día hábil; posterior a eso requiere un flujo de justificación aprobado |
| **Salidas** | Registro de asistencia persistido con timestamp · Retroalimentación visual inmediata al docente · Indicadores de asistencia actualizados en el dashboard de secretaría y director |

---

### UC-09 · Administración de periodos académicos institucionales

| Campo | Detalle |
|-------|---------|
| **Actores** | Director (autoriza apertura y cierre) · Secretaría (opera y monitoriza) |
| **Entradas** | Definición del periodo: nombre, fechas de inicio y fin, cursos incluidos, rangos paramétricos de calificación por dimensión · Acción solicitada: apertura o cierre institucional del periodo |
| **Invariantes** | Solo el Director puede abrir o cerrar un periodo académico institucional · No se puede abrir un nuevo periodo trimestral si el periodo anterior no está completamente cerrado · Los rangos paramétricos de calificación se fijan al momento de abrir el periodo y son inmutables durante su vigencia · El cierre institucional requiere que todos los cursos del periodo tengan centralizadores en estado `CERRADO` |
| **Salidas** | Periodo creado o cerrado en la base de datos con estado y parámetros publicados · Notificación a docentes y secretaría del cambio de estado del periodo · Parámetros activos de validación disponibles para UC-01 |

---

### UC-10 · Reportería estadística e indicadores institucionales

| Campo | Detalle |
|-------|---------|
| **Actores** | Director |
| **Entradas** | Parámetros de consulta: curso, materia, periodo y rango de fechas · Sesión autenticada con JWT y rol DIRECTOR · Identificador del tenant activo |
| **Invariantes** | Toda consulta está acotada al tenant autenticado; ninguna consulta accede a datos de otra institución · Solo el Director puede acceder a indicadores globales de la institución · **Separación obligatoria de indicadores por alcance temporal:** el dashboard distingue siempre entre (a) indicadores de trimestre cerrado (basados en centralizador oficial del trimestre) y (b) indicadores anuales finales (basados en el promedio anual, solo disponibles cuando los 3 trimestres están cerrados); nunca se calcula ni muestra el índice de reprobación anual con datos parciales para evitar el falso "100% reprobados" que ocurre en los Excel actuales cuando solo el 1er trimestre está cargado · El acceso a PII de estudiantes (nombre completo, RUDE) está restringido por rol y tenant |
| **Salidas** | Dashboard con dos vistas diferenciadas: **"Por trimestre"** (disponible al cerrar cada trimestre, muestra % aprobados/reprobados por materia y curso en ese trimestre) y **"Anual final"** (disponible solo al cerrar los 3 trimestres, muestra promedio anual, ranking y tendencia comparativa entre trimestres) · Indicador de cumplimiento de carga docente en tiempo real (% de materias cerradas vs. pendientes por curso) · Exportación PDF del reporte estadístico listo para auditoría interna y externa |

---

## Cinco Decisiones Arquitectónicas

---

### DA-01 · Estrategia de aislamiento multitenant en PostgreSQL

**Contexto:** EduSync atiende a múltiples colegios (tenants) sobre la misma infraestructura. La fuga de datos entre colegios es un riesgo legal y de negocio de primer nivel.

**Alternativas evaluadas:**

| Alternativa | Aislamiento | Complejidad operativa | Costo por tenant |
|-------------|-------------|----------------------|------------------|
| Schema separado por tenant | Alto | Alto (migraciones Flyway por schema) | Mayor |
| Discriminador `tenant_id` en tablas compartidas + Row-Level Security (RLS) | Medio-alto | Medio (una sola migración, política RLS en PostgreSQL) | Menor |
| Base de datos separada por tenant | Muy alto | Muy alto | Prohibitivo en etapa temprana |

**Decisión recomendada:** Discriminador `tenant_id` en tablas compartidas con Row-Level Security de PostgreSQL habilitado en todas las tablas sensibles. El RBAC de Spring Security garantiza que el `tenant_id` del contexto de seguridad se inyecte en cada consulta.

**Justificación:** Permite arrancar con un solo equipo de desarrollo (Rodrigo + agentes IA), mantener migraciones Flyway únicas y escalar a schema separado por tenant en una futura versión sin cambiar el modelo de dominio, solo la estrategia de routing de conexión. Es el punto de equilibrio entre aislamiento suficiente y complejidad operativa para el mercado boliviano actual.

**Impacto:** Afecta directamente UC-01, UC-04, UC-06, UC-09 y UC-10. El `tenant_id` es una restricción implícita en toda consulta del sistema.

---

### DA-02 · Parametrización de reglas normativas sin redespliegue

**Contexto:** El Ministerio de Educación puede cambiar los rangos de calificación por dimensión o el formato de exportación al SIE sin previo aviso. Un hotfix de código ante cada cambio ministerial es inviable operativamente.

**Alternativas evaluadas:**

| Alternativa | Flexibilidad ante cambio | Riesgo de error | Tiempo de respuesta |
|-------------|--------------------------|-----------------|---------------------|
| Valores hardcodeados en dominio | Baja | Alto (requiere redespliegue y QA completo) | Días |
| Configuración en `application.yml` versionado | Media | Medio (requiere commit + despliegue) | Horas |
| Tabla de configuración en base de datos | Alta | Bajo (auditable, sin redespliegue) | Minutos |

**Decisión recomendada:** Tabla de configuración paramétrica en PostgreSQL con alcance `tenant + periodo`. Los siguientes elementos se almacenan como registros editables con versión y fecha de vigencia, sin redespliegue:

| Parámetro parametrizable | Ejemplo Colegio Abaroa | Ejemplo Colegio 2 |
|--------------------------|------------------------|-------------------|
| Conjunto de dimensiones activas | Ser · Saber · Hacer · Decidir | Ser · Saber · Hacer · Decidir · Autoevaluación |
| Peso máximo de cada dimensión (pts) | 5 · 45 · 40 · 5 · — | 5 · 45 · 40 · 5 · 5 |
| **Regla de combinación de N evaluaciones dentro de una dimensión** | `PROMEDIO_SIMPLE` (promedio de todas las notas registradas) | `PROMEDIO_SIMPLE` |
| Regla de combinación de nota AYUDA con nota regular | no aplica | `MAX(regular, ayuda)` |
| Máximo de evaluaciones permitidas por dimensión | sin límite configurado | sin límite configurado |
| Criterio de truncado de decimales | floor | floor |
| Umbral de reprobación trimestral | < 51 pts (sobre 100) | < 51 pts (sobre 100) |
| Formato y escala de exportación al SIE | `floor(nota/3)` → escala 0-33 | `floor(nota/3)` → escala 0-33 |

**Justificación:** El análisis de los archivos Excel reales revela que dos colegios del mismo mercado usan estructuras de dimensiones distintas (con y sin Autoevaluación) y que el mismo colegio aplica truncado inconsistente (`64.666…` aparece como `22` en un campo y `23` en otro). Parametrizar todo esto elimina la necesidad de un hotfix ante cualquier cambio ministerial o diferencia de configuración entre colegios, y garantiza que el motor de dominio sea el único árbitro del cálculo.

**Impacto:** Afecta UC-01 (validación de dimensiones, rangos y tipo de nota), UC-03 (algoritmo de truncado y cálculo de escala SIE), UC-04 (formato de exportación) y UC-09 (fijación de parámetros al abrir periodo).

---

### DA-03 · Modelo de persistencia inmutable e historial de cambios

**Contexto:** El BRD exige trazabilidad completa de cualquier cambio en calificaciones: quién, cuándo, qué valor anterior y qué valor nuevo. La normativa y el riesgo legal prohíben sobrescribir registros.

**Alternativas evaluadas:**

| Alternativa | Trazabilidad | Complejidad de consulta del "valor vigente" | Madurez con Spring Boot 3 |
|-------------|--------------|----------------------------------------------|--------------------------|
| Tabla de auditoría separada (triggers o Spring AOP) | Alta | Baja (tabla principal siempre actual) | Alta |
| Hibernate Envers (revisiones automáticas) | Alta | Media (requiere query a tabla `_AUD`) | Alta |
| Append-only nativo (campo `estado` + `registro_padre_id`) | Muy alta | Media (requiere filtrar por estado vigente) | Media (custom) |

**Decisión recomendada:** Tabla `audit_log` explícita con escritura obligatoria desde la capa de aplicación (no triggers), complementada con Hibernate Envers para entidades críticas de calificación. El modelo append-only se aplica específicamente a las modificaciones retroactivas aprobadas en UC-05.

**Justificación:** La tabla `audit_log` centralizada con campos `usuario_id`, `accion`, `entidad_afectada`, `valor_anterior`, `valor_nuevo` y `timestamp_utc` responde directamente a los requisitos de auditoría del BRD (BR-005) y de AGENTS.md §6, sin acoplar la inmutabilidad al framework ORM. El modelo append-only en UC-05 garantiza que el original nunca se sobreescribe, satisfaciendo el requisito legal de Bolivia de trazabilidad en modificaciones sobre registros oficiales.

**Impacto:** Afecta UC-01, UC-02, UC-04, UC-05 y UC-06.

---

### DA-04 · Estrategia de consolidación post-cierre: síncrona vs. asíncrona

**Contexto:** Al cerrar la última materia de un curso (UC-02), se dispara el cálculo del centralizador trimestral (UC-03). Este cálculo puede ser costoso computacionalmente en colegios grandes. La decisión afecta la experiencia del docente al cerrar su materia.

**Alternativas evaluadas:**

| Alternativa | Experiencia de usuario | Consistencia | Complejidad operativa |
|-------------|----------------------|-------------|----------------------|
| Síncrona (misma transacción del cierre) | El docente espera el cálculo | Inmediata y fuerte | Baja |
| Asíncrona mediante Spring Events internos | El docente recibe respuesta inmediata | Eventual (segundos) | Media |
| Asíncrona mediante AWS SQS + consumer dedicado | El docente recibe respuesta inmediata | Eventual (configurable) | Alta |

**Decisión recomendada:** Asíncrona mediante Spring Events internos en primera versión, con diseño que permita migrar a SQS sin cambiar la interfaz del dominio. La consolidación se desacopla del cierre mediante un evento de dominio `MateriaCarradaEvent` que dispara el cálculo en un hilo separado.

**Justificación:** Para el volumen esperado en el mercado boliviano (colegios de hasta 1.000 estudiantes), Spring Events internos son suficientes y eliminan la complejidad operativa de SQS en la etapa actual. El diseño orientado a eventos del dominio permite escalar a mensajería distribuida cuando el crecimiento lo justifique, sin reescribir la lógica de consolidación.

**Impacto:** Afecta UC-02, UC-03 y el modelo de consistencia del dashboard de secretaría.

---

### DA-05 · Resiliencia en integración con el SIE ante fallos parciales

**Contexto:** El SIE gubernamental se satura en horarios pico, puede fallar a mitad de una exportación masiva y no ofrece garantías de idempotencia. Un error sin manejo adecuado obliga a la secretaria a reiniciar la carga desde cero, recuperando el escenario de madrugadas.

**Alternativas evaluadas:**

| Alternativa | Tolerancia a fallos | Riesgo de duplicado | Complejidad |
|-------------|--------------------|--------------------|-------------|
| Exportación atómica completa con reintento desde cero | Baja (todo o nada) | Bajo | Baja |
| Estado de exportación registro a registro en BD con idempotencia por RUDE+periodo | Alta (reanuda desde el último enviado exitosamente) | Bajo (idempotencia garantizada) | Media |
| Exportación en lotes con checkpoint en BD | Alta | Bajo | Media-alta |

**Decisión recomendada:** Estado de exportación persistido registro a registro, con clave de idempotencia compuesta por `rude + periodo_id`. La tabla de estado de exportación registra `PENDIENTE`, `ENVIADO` o `FALLIDO` por cada estudiante. Un proceso de reintento asíncrono reprocesa únicamente los registros en estado `FALLIDO` o `PENDIENTE`.

**Justificación:** Es la única alternativa que elimina el trabajo de madrugada ante fallos del SIE (KPI-01 del BRD). Garantiza que si el servidor estatal falla al procesar al estudiante 47 de 80, EduSync reanuda desde el estudiante 48 en el siguiente intento, sin riesgo de duplicar los 46 ya enviados. La idempotencia por `rude + periodo_id` protege contra el reenvío accidental en un entorno donde el SIE no confirma duplicados.

**Impacto:** Afecta directamente UC-04 y es el caso de prueba concreto del escenario crítico documentado en `02_parte_dificil.md`.
