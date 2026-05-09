# Arquitectura funcional del core — EduSync

<!-- Stack: Java 21 · Spring Boot 3 · PostgreSQL · Angular · AWS -->

---

## Casos de uso

### CU-01 · Registro descentralizado de calificaciones (RBAC)

**Entradas**
- JWT + rol del docente autenticado
- Código RUDE del estudiante (clave de vinculación obligatoria)
- Valores por dimensión: Ser, Saber, Hacer, Decidir
- Identificador de periodo académico activo

**Invariantes**
- El docente escribe solo en sus materias asignadas (RBAC estricto)
- Rangos paramétricos por dimensión; ningún valor fuera de rango persiste
- Nómina de estudiantes es de solo lectura para el docente
- Periodo debe estar en estado `ABIERTO`; si es `CERRADO` la operación se rechaza

**Salidas**
- Registro persistido con timestamp, usuario y materia
- Entrada inmediata al log de auditoría (quién, qué, cuándo)
- Retroalimentación visual inmediata al docente

---

### CU-02 · Cierre operativo de materia

**Entradas**
- Solicitud explícita de cierre por docente para su materia/periodo
- Estado de completitud: todos los estudiantes con nota en todas las dimensiones

**Invariantes**
- No se puede cerrar con registros incompletos
- El cierre es atómico: sin cierre parcial
- Post-cierre la materia pasa a `SOLO_LECTURA` de forma irreversible sin aprobación jerárquica

**Salidas**
- Estado actualizado a `CERRADO` en base de datos
- Notificación en tiempo real al dashboard de secretaria
- Disparo del proceso de consolidación del centralizador trimestral

---

### CU-03 · Consolidación algorítmica de centralizadores

**Entradas**
- Calificaciones cerradas de todas las materias del periodo para un curso
- Reglas de truncado/redondeo almacenadas como parámetro en base de datos

**Invariantes**
- Algoritmo de redondeo único y centralizado; ningún cálculo fuera del motor
- Centralizador no se genera hasta que el 100 % de materias del curso estén cerradas
- Promedios matemáticamente reproducibles y trazables a sus inputs

**Salidas**
- Centralizador trimestral calculado e inmutable
- Promedio anual actualizado si corresponde al tercer trimestre
- Disponibilidad inmediata para secretaria y director

---

### CU-04 · Exportación y sincronización masiva al SIE por RUDE

**Entradas**
- Periodo académico seleccionado para exportar
- Centralizadores en estado `CERRADO` para todos los cursos
- Formato de mapeo SIE vigente (paramétrico, actualizable sin redespliegue)

**Invariantes**
- Vinculación al SIE exclusivamente por código RUDE; nunca por nombre ni posición
- No se exporta un periodo con materias en estado `ABIERTO`
- Fallos parciales del servidor SIE no reinician el proceso; el estado de progreso se persiste para reintentos asíncronos

**Salidas**
- Payload en formato exacto del SIE generado y enviado
- Reporte de resultado: registros enviados, fallidos, pendientes
- Entrada en log de auditoría de la operación completa

---

### CU-05 · Autorización jerárquica de modificación retroactiva

**Entradas**
- Solicitud formal: materia, RUDE, dimensión, valor actual, valor propuesto, justificación
- Identidad y rol del director autorizador

**Invariantes**
- Ningún registro en periodo `CERRADO` se modifica sin aprobación del director
- Solicitud queda en `PENDIENTE`; el registro original no se altera durante la espera
- Si se aprueba, se genera un nuevo registro versionado; el original nunca se sobreescribe (append-only)
- Toda aprobación o rechazo se registra en el log con identidad del director y timestamp

**Salidas**
- Notificación al docente del estado de la resolución
- Si aprobada: nuevo valor efectivo con trazabilidad al registro original
- Entrada doble en log de auditoría: solicitud + resolución

---

## Decisiones críticas

### DA-01 · Estrategia de multitenancy
Decidir entre schemas separados por tenant en PostgreSQL (mayor aislamiento, mayor complejidad operativa) o discriminador de tenant en tablas compartidas (menor costo, mayor riesgo de filtración). Impacta seguridad, modelo de pricing por estudiante y complejidad del RBAC.

### DA-02 · Motor de reglas paramétricas (rangos y formato SIE)
Decidir si los límites normativos por dimensión y el formato SIE se almacenan como configuración en base de datos (modificables sin redespliegue) o se codifican en la capa de dominio. Determina si un cambio ministerial requiere hotfix o solo una actualización de configuración.

### DA-03 · Modelo de inmutabilidad e historial (append-only vs auditoría separada)
Definir si la persistencia de calificaciones es append-only con estado explícito por registro, o si se mantiene una tabla de auditoría separada. Con Spring Boot 3 + PostgreSQL, implica evaluar Hibernate Envers o solución custom, y la estrategia de consulta del "valor vigente".

### DA-04 · Consolidación síncrona vs asíncrona
Decidir si el centralizador se calcula de forma síncrona en la misma transacción del cierre o de forma asíncrona mediante eventos/cola (SQS + consumer en AWS). La sincronía es simple pero bloquea la respuesta al usuario; la asincronía introduce consistencia eventual.

### DA-05 · Gestión de estado de exportación SIE ante fallos
Definir cómo se modela el progreso de una exportación parcialmente exitosa: estado registro a registro en base de datos con idempotencia por RUDE+periodo, o exportación atómica completa con reintento desde cero. Define la tolerancia a fallos del cuello de botella más crítico del sistema.