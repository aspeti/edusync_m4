> COPIA VIVA - release/3.0.0 (capa de implementacion)
>
> Este archivo nace como copia editable de docs/baseline/BRD_EduSync_vFinal.md (congelado en release/2.0.0, tag de M4) para evolucionar junto al codigo desde release/3.0.0 en adelante, siguiendo el modelo documental de implementacion (plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md).
>
> **Este archivo SI se edita** a medida que la implementacion lo requiera. El registro historico inmutable de M4 vive en docs/baseline/BRD_EduSync_vFinal.md (tag release/2.0.0) y NUNCA se modifica; cualquier cambio de negocio real durante la implementacion se registra ademas en docs/product/DTP.md §A.2 (delta vs. DTI vFinal) con su ADR si aplica.
>
> | Campo | Valor |
> |-------|-------|
> | Fuente historica (congelada) | docs/baseline/BRD_EduSync_vFinal.md |
> | Version de partida | v2.0 |
> | Fecha de apertura de capa viva | 28/05/2026 |
> | Release vivo | release/3.0.0 |
> | Documento rector | plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md |
> | Agente | docs-agent |

---
# Business Requirements Document (BRD) — EduSync

## 0. Metadatos

| Campo | Valor |
|-------|-------|
| **Producto** | EduSync |
| **Grupo** | G-EduSync |
| **Versión** | v2.0 |
| **Fecha** | 14/05/2026 |
| **Sponsor de negocio** | Dirección Institucional / Propietarios de Unidades Educativas Bolivianas |
| **Stakeholders** | Director Académico · Secretaría / Administrativo · Docentes · Ministerio de Educación (SIE) |
| **Autores** | Equipo G-EduSync — Rodrigo Aspeti (Dev Lead) |
| **Revisores** | Docente + 1 grupo par |
| **Estado** | En revisión |
| **Insumo del Módulo Anterior (M2 UI/UX)** | `01_vision_negocio.md` · `02_parte_dificil.md` · `BRD_EduSync_V1.md` |
| **Prompts utilizados** | `PR-BRD-001`, `PR-ARCH-001`, `PR-DIAG-001`, `PR-DIAG-002` (ver `docs/PROMPT_MAPPING.md`) |

> **Nota sobre versión:** Este documento consolida y amplía `BRD_EduSync_V1.md`. Incorpora requerimientos derivados de la arquitectura funcional (`arquitectura_funcional_EduSync.md`, 10 UCs y 5 DAs), los diagramas de estado del Docente (`estados_cargar_notas.md`, 18 estados) y del Director (`estados_administracion.md`, 23 estados). Los requerimientos BR-001..BR-005 del v1 se conservan y enriquecen; se añaden BR-006..BR-012.

---

## 1. Resumen ejecutivo

**Problema:** Las unidades educativas bolivianas sufren una ineficiencia estructural causada por la "triple digitación manual": los docentes ingresan notas en hojas Excel individuales, la secretaría las consolida copiando y pegando en un centralizador general, y finalmente las carga nota por nota en el sistema estatal SIE. Este proceso genera corrupción de datos por desfase de listas cuando hay alumnos nuevos o retirados, inconsistencias matemáticas por decimales invisibles, alteraciones retroactivas clandestinas, y obliga al personal a trabajar entre las 2:00 AM y las 4:00 AM para evitar la saturación de los servidores del Ministerio de Educación, bajo riesgo de sanciones económicas de hasta cinco días de sueldo.

**Propuesta:** EduSync es una plataforma SaaS B2B multitenant que descentraliza el ingreso de calificaciones por rol (RBAC), consolida automáticamente los centralizadores trimestrales usando un motor algorítmico con truncado `floor`, y sincroniza masivamente con el SIE vinculando cada estudiante exclusivamente por su código RUDE. El Director administra la gestión académica anual y los tres periodos trimestrales con parámetros inmutables post-apertura, y puede autorizar correcciones retroactivas con ventanas temporales de hasta 72 horas. Todo el ciclo queda sellado en un log de auditoría inalterable.

**Valor esperado:**
- Reducción del ciclo de cierre administrativo de madrugadas enteras a menos de 10 minutos (KPI-01).
- Tasa de error de consolidación del 0 % en exportaciones al SIE (KPI-02).
- Eliminación de los 10 ciclos de revisión manual que hoy realiza la secretaría (KPI-03).

**Métricas clave de éxito:** KPI-01 < 10 min (vs. línea base > 15 h), KPI-02 = 0 % errores (vs. alta tasa actual), KPI-03 = 0 ciclos de revisión (vs. 10 actuales).

**Llamada a la acción:** Aprobación del Business Case para iniciar el desarrollo del módulo de Sincronización SIE por RUDE, el motor de consolidación con `floor` y el flujo de administración de gestión académica. Se requiere acceso formal al entorno de pruebas del SIE del Ministerio de Educación.

---

## 2. Contexto del negocio

- **Organización:** Unidades Educativas Privadas y de Convenio del mercado boliviano.
- **Unidad impactada:** Dirección Académica, Secretaría / Administración y Plantel Docente.
- **Procesos de negocio afectados:** Cierre operativo trimestral · Registro pedagógico de aula · Consolidación de centralizadores · Reporte al Sistema de Información Educativa (SIE) · Administración de gestión académica anual (apertura y cierre de periodos).
- **Estrategia de la organización:** Digitalización integral con filosofía *Zero-Training*, garantizando seguridad de datos inmutables, trazabilidad regulatoria y cumplimiento normativo estricto con el Estado boliviano. El objetivo estratégico es posicionar a EduSync como la **fuente única de verdad académica** para instituciones educativas, reduciendo el riesgo legal y operativo del personal.

---

## 3. Problema y oportunidad de negocio

### 3.1 Problema

Las unidades educativas de Bolivia gestionan calificaciones mediante un proceso manual en tres etapas: (1) cada docente ingresa notas en su hoja Excel personal, (2) la secretaría copia y pega manualmente en un centralizador general, y (3) transcribe nota por nota al sistema estatal SIE. Esta "triple digitación" produce cuatro clases de fallas críticas con evidencia directa en los archivos Excel analizados (`Centralizador2A_ColegioAbaroa.xlsx`, `REGISTRO SECUNDARIA 2026.xlsx`):

- **Corrupción por desfase de listas:** el ingreso o retiro de un alumno desplaza las calificaciones de los demás cuando el mapeo usa posición visual en lugar de identidad única.
- **Descuadres matemáticos:** valores como `64.666…` aparecen como `22` en un campo y `23` en otro porque el criterio de truncado no está estandarizado.
- **Alteraciones retroactivas clandestinas:** sin bloqueo post-cierre, cualquier usuario puede modificar notas ya reportadas al Ministerio sin dejar rastro.
- **Cuello de botella SIE:** la saturación del servidor gubernamental en horario laboral obliga a jornadas de 2:00 AM–4:00 AM; un fallo parcial obliga a reiniciar desde cero, bajo riesgo de memorándums y descuentos de hasta 5 días de sueldo.

La consecuencia de no actuar es la continuidad del riesgo legal, el agotamiento del personal y la imposibilidad de escalar a más colegios sin multiplicar los errores.

### 3.2 Oportunidad

- **Valor económico:** Ahorro directo en horas extra del personal administrativo (estimado 15 % año 1, 35 % año 3). Eliminación de multas pecuniarias por envío de datos corruptos al Ministerio.
- **Valor estratégico:** EduSync se posiciona como el estándar de "Tranquilidad Administrativa" en un mercado de colegios privados y de convenio bolivianos sin competidor digital que resuelva el cuello de botella SIE con mapeo por RUDE.
- **Ventana de oportunidad:** Implementación preferencial antes de la temporada de inscripciones y el primer cierre operativo trimestral del año escolar. La automatización del cierre es el diferenciador crítico frente a Academium y Colegio360.

### 3.3 Evidencia de Continuous Discovery

- **Entrevistas UX realizadas:** 3 sesiones con perfiles validados.
  - **Marcela** (Docente): principal afectada por el estrés de cierre y el riesgo de descuentos salariales por errores tipográficos.
  - **Wendy** (Secretaría): trabaja de madrugada para subir notas al SIE y audita manualmente 10 ciclos de revisión por trimestre.
  - **Jeanneth** (Directora): sin visibilidad de avance en tiempo real; solo conoce el estado del colegio cuando le reportan a mano.
- **Hipótesis principales validadas:** (H1) el docente no necesita consolidar notas de otros colegas si tiene acceso solo a su materia; (H2) el personal usa Excel por falta de alternativa, no por preferencia; (H3) el desfase de listas es la causa raíz del 100 % de los errores de exportación SIE reportados.
- **Artefactos M2 (UI/UX):** Sistema de diseño con Atomic Design, Design Tokens, WCAG 2.2, semáforos visuales para notas de reprobación.
- **Próxima cadencia de Discovery:** Quincenal con secretaría y director durante el piloto.

---

## 4. Usuarios objetivo / Personas clave

### 4.1 Persona principal — Docente (Marcela)

| Atributo | Valor |
|----------|-------|
| **Nombre / rol** | Docente de aula / Asesor de curso |
| **Contexto** | Cierre de trimestre, bajo presión, lidiando con múltiples hojas Excel y miedo a descuentos salariales por errores tipográficos en el SIE. |
| **Jobs-to-be-done** | 1. Ingresar calificaciones por dimensiones (Ser/Saber/Hacer/Decidir) con validación automática de rangos. 2. Gestionar el número de evaluaciones por dimensión según su planificación pedagógica. 3. Registrar asistencia de sus estudiantes. 4. Ejecutar el cierre operativo de su materia con certeza de completitud. 5. Solicitar corrección post-cierre cuando detecta un error. |
| **Dolores principales** | Carga administrativa por consolidar notas de colegas. Miedo a descuentos salariales por error tipográfico. Imposibilidad de corregir un error post-cierre sin exponer la irregularidad. |
| **Ganancia esperada** | Registrar solo su materia de forma autónoma, recibir feedback inmediato de errores antes de cerrar, y poder solicitar correcciones con trazabilidad y sin penalización. |

### 4.2 Persona secundaria — Secretaría / Administrativo (Wendy)

| Atributo | Valor |
|----------|-------|
| **Nombre / rol** | Secretaría académica / Asesor administrativo |
| **Contexto** | Madrugadas previas al plazo ministerial. Consolida datos de todos los docentes y transcribe nota por nota al SIE bajo presión de tiempo y riesgo de error. |
| **Jobs-to-be-done** | 1. Monitorizar en tiempo real el progreso de la entrega de notas por docente y curso. 2. Exportar masivamente al SIE con un clic cuando todos los cursos están cerrados. 3. Generar boletines oficiales en PDF sin digitación adicional. 4. Administrar altas, bajas y transferencias de estudiantes con identidad RUDE. 5. Notificar a docentes rezagados sin seguimiento manual. |
| **Dolores principales** | Trabajo de madrugada, estrés de "salvar" el colegio ante errores de docentes, riesgo de reincio total ante fallo parcial del SIE, 10 ciclos de revisión manual por trimestre. |
| **Ganancia esperada** | Obtener una fuente única de verdad automatizada, sincronizar al SIE sin digitar y recuperar las noches y fines de semana. |

### 4.3 Persona terciaria — Director Académico (Jeanneth)

| Atributo | Valor |
|----------|-------|
| **Nombre / rol** | Director de la institución educativa |
| **Contexto** | Toma decisiones sin visibilidad de datos en tiempo real. Solo conoce el estado del colegio cuando la secretaría le reporta manualmente. |
| **Jobs-to-be-done** | 1. Crear y administrar la gestión académica anual (calendario, parámetros de dimensiones, asignaciones docentes). 2. Abrir y cerrar los 3 periodos trimestrales de forma secuencial. 3. Revisar indicadores de rendimiento institucional (reprobación, asistencia, avance de carga). 4. Autorizar correcciones retroactivas con control de alcance y ventana temporal. |
| **Dolores principales** | Ceguera de datos, dependencia total de la secretaría para conocer el avance, incapacidad de auditar correcciones retroactivas clandestinas. |
| **Ganancia esperada** | Visibilidad en tiempo real, control jerárquico de modificaciones con trazabilidad legal, indicadores listos para auditorías ministeriales. |

---

## 5. Propuesta de valor

| Eje | Contenido |
|-----|-----------|
| **Para quién** | Directores, Secretarías y Docentes de unidades educativas bolivianas (privadas y de convenio). |
| **Que necesita** | Cumplir la obligación estatal de reportar calificaciones al SIE libres de errores, sin jornadas de madrugada ni riesgo de sanciones económicas. |
| **Nuestra propuesta es** | EduSync: plataforma SaaS B2B multitenant de gestión académica con consolidación automática, cierre operativo por roles y sincronización SIE one-click por código RUDE. |
| **Que le aporta** | 1. Cero consolidación manual en Excel. 2. Validación antierrores en tiempo real (bloqueo de notas fuera de rango antes de guardar). 3. Exportación automática al SIE sin transcripción. 4. Inmutabilidad post-cierre con log de auditoría inalterable. 5. Control jerárquico de modificaciones retroactivas con ventana temporal. |
| **A diferencia de** | Procesos en Excel con triple digitación manual, Academium y Colegio360 que trasladan la carga cognitiva al usuario. |
| **Nuestro diferencial es** | Interfaz Zero-Training, motor algorítmico con `floor` que elimina descuadres de decimales, mapeo 100 % por RUDE y bloqueo automático al vencer la ventana de modificación. |

---

## 6. Panorama competitivo

| Competidor / alternativa | Tipo | Fortaleza percibida | Debilidad percibida |
|--------------------------|------|---------------------|---------------------|
| **Excel + Transcripción Manual** | *do-nothing* | Familiaridad absoluta, costo monetario cero | Triple digitación, desfases de lista, 10 ciclos de revisión, sin trazabilidad |
| **Sistema SIE (Ministerio)** | Directo / Obligatorio | Estándar gubernamental obligatorio | Sin integración automática, colapsa en horario laboral, UX hostil |
| **Academium** | Directo | Costo accesible | Traslada estrés al docente, sin mapeo por RUDE, sin inmutabilidad |
| **Colegio360** | Directo | Robusto en funciones | Soporte deficiente, alto costo, sin hiper-localización boliviana |
| **Google Sheets colaborativo** | Indirecto | Gratuito, familiar | Sin validación antierrores, sin consolidación automática, sin exportación SIE |

---

## 7. Business Model Canvas

| Bloque | Elementos concretos |
|--------|---------------------|
| **1. Segmentos de clientes** | 1. Directores / compradores B2B institucionales. 2. Secretarías (operadoras y consolidadoras). 3. Docentes (generadores de datos). |
| **2. Propuesta de valor** | 1. Cero digitación manual en sincronización SIE. 2. Tranquilidad legal ante inmutabilidad de registros y log de auditoría. 3. Interfaz antierrores en tiempo real con validación paramétrica. |
| **3. Canales** | 1. Venta directa B2B a directores de colegios. 2. Marketing digital (TikTok, demostraciones en congresos educativos). 3. Programa de referidos entre secretarías. |
| **4. Relación con clientes** | 1. Certificaciones "Colegio Digital EduSync". 2. Upselling de módulos (comunicación con padres, finanzas). 3. Benchmark anónimo de rendimiento entre colegios. |
| **5. Fuentes de ingresos** | 1. Setup Fee inicial (200 Bs por institución). 2. Suscripción anual por estudiante activo (3 cuotas o 10 % descuento pago único). 3. Módulos adicionales (asistencia avanzada, reportería extendida). |
| **6. Recursos clave** | 1. Motor algorítmico de consolidación con `floor` y mapeo RUDE. 2. Infraestructura cloud (AWS) con PostgreSQL multitenant (RLS). 3. Sistema de diseño UI/UX con Atomic Design y Design Tokens. |
| **7. Actividades clave** | 1. Mantenimiento e integración continua del formato SIE ante cambios ministeriales. 2. Desarrollo iterativo del motor de consolidación y auditoría. 3. Soporte activo durante cierres operativos trimestrales. |
| **8. Socios clave** | 1. Sistema de Información Educativa SIE (Ministerio de Educación Bolivia). 2. Federación de Administrativos de Cochabamba. 3. Proveedores Cloud (AWS / Google Cloud). |
| **9. Estructura de costos** | 1. Infraestructura cloud y servidores (OPEX recurrente). 2. Planilla de talento (desarrollo, UX, soporte). 3. Costos de adquisición de clientes (marketing B2B). |

---

## 8. Métricas clave de éxito (North Star + apoyo)

| ID | KPI | North Star? | Línea base | Meta | Horizonte | Fuente del dato |
|----|-----|-------------|------------|------|-----------|-----------------|
| KPI-01 | Time-on-Task del ciclo de sincronización SIE (minutos) | **Sí** | > 15 horas (jornada de madrugada) | < 10 minutos | Cierre 1er trimestre | Telemetría del sistema (logs de sesión) |
| KPI-02 | Tasa de error de integridad de datos en exportación SIE | No | Alta (desfases de lista, decimales incorrectos) | 0 % | Cierre 1er trimestre | Reportes de auditoría del motor de exportación |
| KPI-03 | Ciclos de revisión manual por trimestre (Secretaría) | No | 10 ciclos promedio | 0 ciclos | Año escolar 1 | Entrevistas UX con secretarías piloto |
| KPI-04 | Porcentaje de docentes que cierran su materia antes del plazo | No | Sin medición (proceso manual) | ≥ 95 % | Año escolar 1 | Dashboard de avance docente (UC-10) |
| KPI-05 | Tiempo de respuesta del Director ante solicitudes retroactivas (UC-05) | No | Sin medición | ≤ 4 horas | Año escolar 1 | Log de auditoría (timestamp solicitud → resolución) |

---

## 9. Objetivos de negocio (SMART)

| ID | Objetivo | Métrica | Línea base | Meta | Horizonte |
|----|----------|---------|------------|------|-----------|
| BO-01 | Reducir el tiempo del ciclo de cierre operativo trimestral | Minutos de sesión de sincronización SIE | > 900 min (15+ horas) | ≤ 10 min | Cierre 1er trimestre |
| BO-02 | Erradicar la corrupción de datos por desfase de listas | Tasa de error de mapeo RUDE ↔ nota | N/D (sin medición formal) | 0 % | Cierre 1er trimestre |
| BO-03 | Incrementar la adopción del registro directo por docentes | % de docentes que ingresan notas directamente en EduSync | 0 % (proceso Excel) | ≥ 95 % | Año escolar 1 |
| BO-04 | Garantizar trazabilidad legal de todas las modificaciones retroactivas | % de correcciones con registro en audit_log | 0 % (sin trazabilidad) | 100 % | Año escolar 1 |
| BO-05 | Asegurar la disponibilidad del sistema durante el cierre operativo | Uptime en ventanas de cierre trimestral | Sin SLA definido | ≥ 99.5 % | Q1 lanzamiento |

---

## 10. Stakeholders y roles (modelo RACI)

| Stakeholder | Interés | R / A / C / I |
|-------------|---------|----------------|
| Director de la Institución | Estratégico, cumplimiento normativo, visibilidad de indicadores | **A** |
| Secretaría / Asesores | Operativo: consolidación, exportación SIE, boletines | **R** |
| Docentes de Materia | Experiencia de carga, reducción de estrés, cierre operativo | **C** |
| Ministerio de Educación (SIE) | Cumplimiento normativo, formato de exportación | **I** |
| Dev Lead (Rodrigo Aspeti) | Técnico: desarrollo, arquitectura, despliegue | **R** |
| Equipo UX/UI | Diseño del sistema, accesibilidad, Design Tokens | **R** |

---

## 11. Requerimientos de negocio

> Los requerimientos BR-001..BR-005 provienen de `BRD_EduSync_V1.md`. Los requerimientos BR-006..BR-012 son requerimientos nuevos derivados del análisis de `arquitectura_funcional_EduSync.md` y los diagramas de estado.

| ID | Requerimiento de negocio | Prioridad (MoSCoW) | Justificación | Métrica de aceptación |
|----|---------------------------|--------------------|---------------|-----------------------|
| BR-001 | El sistema debe permitir la carga de calificaciones descentralizada, con acceso restringido por rol (RBAC): cada docente ingresa notas únicamente de su materia asignada. | Must | Aísla la responsabilidad operativa al docente y elimina la necesidad de consolidación manual por la secretaría. | 100 % de accesos verificablemente restringidos a materias propias. Sin excepción documentada. |
| BR-002 | El sistema debe validar en tiempo real que ninguna calificación supere los rangos paramétricos de cada dimensión activa del periodo, bloqueando el guardado de valores inválidos. | Must | Evita notas fuera de norma ministerial desde el ingreso. Elimina descuadres de decimales invisibles. | 0 % de notas fuera del rango paramétrico almacenadas en la base de datos. |
| BR-003 | El sistema debe consolidar y estandarizar algorítmicamente los promedios trimestrales y anuales usando el criterio de truncado `floor`, de forma centralizada en el motor de dominio. | Must | El criterio `floor` es el único que elimina los descuadres de decimales largos observados en los Excel reales (ej. `64.666…` → `64`). El cálculo no puede ocurrir en el frontend ni en SQL ad-hoc. | Coincidencia matemática exacta en boletines y exportación SIE para el 100 % de los registros. |
| BR-004 | El sistema debe exportar y sincronizar masivamente las calificaciones al SIE vinculando cada estudiante exclusivamente por su código RUDE, nunca por nombre, apellido ni posición de lista. | Must | Es el cuello de botella que causa el trabajo de madrugada y las multas ministeriales. El RUDE es la única clave que garantiza mapeo correcto ante altas, bajas y transferencias. | Carga exitosa al SIE sin ninguna digitación manual. Tasa de error de mapeo = 0 %. |
| BR-005 | El sistema debe aplicar inmutabilidad post-cierre: una vez concluido el cierre operativo de una materia, el periodo pasa a estado Solo Lectura y bloquea cualquier modificación retroactiva sin autorización jerárquica del Director. | Must | Bloquea alteraciones clandestinas retroactivas. Garantiza integridad del registro histórico para auditorías ministeriales. | 0 cambios en registros cerrados sin entrada correspondiente en el log de auditoría. |
| BR-006 | El sistema debe permitir al Director crear y administrar la gestión académica anual con un calendario de 3 periodos trimestrales, cuya apertura sea obligatoriamente secuencial: no se puede abrir el Trimestre 2 sin cerrar completamente el Trimestre 1. | Must | Refleja la estructura obligatoria del año académico boliviano. La apertura no secuencial generaría inconsistencias en los centralizadores y en los indicadores anuales. | 100 % de los intentos de apertura no secuencial son bloqueados con error `E_TRIMESTRE_PREVIO_ABIERTO`. |
| BR-007 | El sistema debe permitir al Director fijar, al momento de abrir cada periodo, los parámetros académicos (dimensiones activas, pesos, reglas de combinación de evaluaciones, umbral de reprobación, formato SIE) que serán inmutables durante la vigencia del periodo. | Must | Los parámetros deben ser configurables sin redespliegue ante cambios ministeriales, pero inmutables durante el periodo para garantizar consistencia del centralizador. | Los parámetros no pueden ser modificados mientras el periodo está en estado `ABIERTO`. El sistema bloquea el intento con error `E_PARAMETRO_INMUTABLE`. |
| BR-008 | El sistema debe permitir al Director habilitar accesos del personal: asignar roles (DOCENTE / SECRETARÍA) y mapear cada docente a sus materias y cursos para el año, como prerequisito antes de abrir el primer periodo. | Must | Sin la asignación docente-materia, el RBAC de UC-01 no puede restringir el acceso correctamente. Es el prerequisito funcional de toda la carga de notas. | Toda materia activa del periodo tiene al menos un docente asignado. El sistema bloquea la apertura del periodo si hay materias sin cobertura. |
| BR-009 | El sistema debe gestionar solicitudes de modificación retroactiva con autorización jerárquica del Director, incluyendo definición de alcance (estudiante específico por RUDE o curso completo) y una ventana temporal obligatoria entre 1 y 72 horas. Al expirar la ventana, el sistema revoca el permiso automáticamente sin intervención manual. | Must | Proporciona el mecanismo controlado para corregir errores reales post-cierre sin comprometer la inmutabilidad ni habilitar alteraciones clandestinas. | 100 % de las modificaciones en periodos cerrados tienen autorización del Director registrada con timestamp. 0 % de ventanas con duración indefinida. Revocación automática verificable en logs. |
| BR-010 | El sistema debe proveer al Director un dashboard de indicadores institucionales con dos vistas diferenciadas: (a) indicadores trimestrales, disponibles al cerrar cada trimestre; (b) indicadores anuales finales, disponibles únicamente cuando los 3 trimestres están cerrados. Nunca se debe calcular ni mostrar el índice de reprobación anual con datos parciales. | Must | Evita el falso "100 % reprobados" que ocurre en los Excel actuales cuando solo el 1er trimestre está cargado. Los indicadores parciales distorsionan decisiones institucionales y pueden generar conflictos con el Ministerio. | El campo de promedio anual muestra `EN CURSO` con 1 o 2 trimestres cerrados. Solo muestra valor numérico cuando los 3 trimestres están en estado `CERRADO`. |
| BR-011 | El sistema debe registrar un log de auditoría inalterable para toda operación de escritura: cada nota registrada, cada cierre de materia, cada exportación SIE y cada modificación retroactiva. El log debe incluir: actor, materia, acción, valor anterior, valor nuevo y timestamp UTC. | Must | Requisito legal boliviano para trazabilidad de registros académicos oficiales. Es la evidencia ante auditorías ministeriales y el mecanismo de detección de irregularidades. | 100 % de las operaciones de escritura tienen entrada en `audit_log`. 0 entradas con `UPDATE` o `DELETE` sobre el log (inmutabilidad del log). |
| BR-012 | El sistema debe generar boletines académicos oficiales en formato PDF a partir del centralizador cerrado, usando una plantilla ministerial parametrizable sin necesidad de redespliegue, disponible únicamente cuando el centralizador del periodo está en estado `CERRADO`. | Should | Elimina la generación manual de boletines y garantiza que el documento entregado a los padres refleja los datos oficiales del centralizador inmutable. | Boletín generado en < 5 segundos por curso. 0 boletines generados a partir de centralizadores en estado `PROVISIONAL`. |

---

## 12. Reglas de negocio y políticas

| ID | Regla | Tipo | Origen |
|----|-------|------|--------|
| RB-01 | **Mapeo por Identidad Única (RUDE):** La vinculación de calificaciones al SIE y la identificación de estudiantes en todo el sistema se realiza exclusivamente por código RUDE. Nunca por nombre, apellido, orden visual o posición de lista. | Política interna + Normativa SIE | Ministerio de Educación Bolivia · `02_parte_dificil.md` |
| RB-02 | **Límites paramétricos de dimensión:** Ninguna calificación puede guardarse si supera o no alcanza los límites normativos de la dimensión activa del periodo (ej. Ser/Decidir ≤ 5, Saber ≤ 45, Hacer ≤ 40). Los rangos son configurables por periodo sin redespliegue. | Normativa ministerial | Ministerio de Educación Bolivia · DA-02 |
| RB-03 | **Inmutabilidad de nóminas:** Los docentes no tienen permiso para alterar, agregar ni eliminar estudiantes de las listas. Solo la Secretaría puede realizar altas, bajas o transferencias. Una baja no elimina el historial histórico de calificaciones previas. | Política interna de seguridad de datos | `arquitectura_funcional_EduSync.md §UC-06` |
| RB-04 | **Autoridad de apertura/cierre de periodos:** Solo el Director puede abrir o cerrar un periodo académico institucional. La Secretaría monitoriza pero no actúa sobre el estado del periodo. | Política institucional | `arquitectura_funcional_EduSync.md §UC-09` |
| RB-05 | **Apertura secuencial de periodos:** No se puede abrir un nuevo periodo trimestral si el anterior no está completamente cerrado (todos los centralizadores en estado `CERRADO`). | Normativa académica boliviana | `estados_administracion.md §Invariantes` |
| RB-06 | **Inmutabilidad de parámetros post-apertura:** Los parámetros académicos del periodo (dimensiones, pesos, reglas de combinación, umbral de reprobación) se fijan al abrir el periodo y no pueden modificarse mientras está en estado `ABIERTO`. | Política de integridad del centralizador | `arquitectura_funcional_EduSync.md §DA-02` |
| RB-07 | **Ventana temporal obligatoria en modificaciones retroactivas:** Toda autorización de modificación post-cierre incluye una ventana de expiración entre 1 y 72 horas (default: 24 h). No existe autorización indefinida. La revocación al expirar es automática y sin intervención manual. | Política de control interno | `arquitectura_funcional_EduSync.md §UC-05` |
| RB-08 | **Truncado `floor` como criterio único de redondeo:** El motor de consolidación aplica la función `floor` (piso) para el truncado de decimales. No se usa redondeo estándar ni bancario. Esto garantiza consistencia con la escala de reporte SIE (`floor(nota/3)` → escala 0–33). | Normativa de cálculo académico | `arquitectura_funcional_EduSync.md §UC-03 · DA-02` |
| RB-09 | **Cierre atómico de materia:** El cierre operativo de una materia es atómico. No existe cierre parcial. El sistema solo acepta el cierre si el 100 % de los estudiantes de la nómina tiene todas las evaluaciones declaradas completadas. | Política de integridad | `estados_cargar_notas.md §Invariantes · UC-02` |
| RB-10 | **Modelo append-only en modificaciones retroactivas:** Toda corrección aprobada mediante UC-05 genera un nuevo registro versionado con referencia al anterior. El registro original es inmutable y nunca se sobreescribe. | Política de auditoría legal | `arquitectura_funcional_EduSync.md §DA-03 · UC-05` |
| RB-11 | **Indicadores anuales con 3 trimestres cerrados:** El índice de reprobación anual y el promedio anual se calculan y muestran únicamente cuando los 3 trimestres del año académico están en estado `CERRADO`. Con datos parciales, el campo muestra `EN CURSO — promedio anual no disponible`. | Política de integridad estadística | `arquitectura_funcional_EduSync.md §UC-03 · UC-10` |

---

## 13. Supuestos, restricciones y dependencias

**Supuestos:**
- Los docentes tienen acceso a internet y dispositivo con navegador moderno durante el periodo de carga de notas.
- La experiencia guiada Zero-Training vencerá la resistencia al cambio en docentes con baja alfabetización digital, sin capacitaciones formales costosas.
- Cada estudiante tiene un código RUDE único y válido asignado por el Ministerio antes del inicio del año escolar.
- El Director de la institución tiene autoridad suficiente para aprobar la adopción del sistema a nivel institucional.

**Restricciones:**
- El mapeo de datos al SIE es obligatorio e inquebrantable; el código RUDE es de uso obligatorio para toda identificación de estudiantes en exportaciones.
- Los rangos paramétricos de calificación por dimensión están sujetos a cambios ministeriales sin previo aviso; la arquitectura debe soportar actualización sin redespliegue (DA-02).
- El equipo de desarrollo es de 1 desarrollador principal (Rodrigo Aspeti) asistido por agentes de IA; la arquitectura debe minimizar la complejidad operativa.
- El sistema opera sobre infraestructura multitenant con aislamiento estricto por `tenant_id` y Row-Level Security en PostgreSQL; ningún dato de un colegio puede ser visible desde otro.
- Los registros de auditoría son legalmente inmutables; el sistema no puede proveer mecanismos de `UPDATE` o `DELETE` sobre `audit_log`.

**Dependencias:**
- Disponibilidad del servidor SIE del Ministerio de Educación Bolivia (dependencia crítica externa con alta tasa de fallos en horario pico — DA-05).
- Acceso al entorno de pruebas del SIE para validar el formato de exportación antes del lanzamiento.
- Aprobación formal de la plantilla de boletín ministerial vigente para parametrización sin redespliegue.
- Integración con proveedor de infraestructura AWS (EC2, RDS PostgreSQL, SQS para reintentos asincrónicos de exportación SIE).

---

## 14. Alcance de negocio

### 14.1 En alcance

- Gestión descentralizada de calificaciones por dimensión con validación paramétrica en tiempo real (UC-01).
- Cierre operativo atómico de materia con verificación de completitud (UC-02).
- Consolidación algorítmica de centralizadores provisionales y oficiales con criterio `floor` (UC-03).
- Exportación y sincronización masiva al SIE por código RUDE con resiliencia ante fallos parciales (UC-04).
- Autorización jerárquica de modificaciones retroactivas con ventana temporal 1–72 h (UC-05).
- Gestión de nóminas estudiantiles inmutables con identificación por RUDE (UC-06).
- Generación de boletines académicos oficiales en PDF (UC-07).
- Control de asistencia por materia (UC-08).
- Administración de periodos académicos institucionales con parametrización (UC-09).
- Reportería estadística e indicadores institucionales con separación trimestral/anual (UC-10).
- Log de auditoría inalterable para todas las operaciones de escritura.
- Aislamiento multitenant con Row-Level Security en PostgreSQL.

### 14.2 Fuera de alcance

- Gestión financiera, facturación o cobro de pensiones escolares (previsto para módulo futuro de upselling).
- Módulos de comunicación externa y chat con padres de familia (previsto para upselling).
- Integración con sistemas de nómina del personal docente.
- Funcionalidades de matrícula o inscripción de nuevos estudiantes (se asume que el RUDE llega preregistrado).
- Gestión de infraestructura física o inventario de la institución.

---

## 15. Beneficios esperados y *business case* resumido

| Tipo | Año 1 | Año 2 | Año 3 |
|------|-------|-------|-------|
| Ahorro operativo (horas extra Secretaría) | Estimado 15 % | Estimado 25 % | Estimado 35 % |
| Eliminación de multas ministeriales por error de datos | Por medir (depende de institución) | — | — |
| Ingresos por nuevas instituciones | +20 colegios | +50 colegios | +100 colegios |
| Inversión (CAPEX / Desarrollo) | $X,XXX USD | — | — |
| Costo de operación (OPEX Cloud + Soporte) | $X,XXX USD | $X,XXX USD | $X,XXX USD |
| **Retorno estimado** | Positivo en primer ciclo anual con ≥ 20 colegios activos | | |

> Los valores financieros exactos se completarán en el business case formal antes del cierre del Módulo 4.

---

## 16. Riesgos de negocio

| Riesgo | Probabilidad | Impacto | Mitigación | Responsable |
|--------|--------------|---------|------------|-------------|
| Caída del servidor SIE durante exportación masiva | Alta | Crítico | Reintentos asíncronos con idempotencia por `RUDE + periodo_id`; la exportación reanuda desde el último registro exitoso sin duplicados (DA-05). | Dev Lead |
| Cambios en el formato o dimensiones del Ministerio sin previo aviso | Media | Alto | Arquitectura paramétrica: los rangos, pesos y formato SIE se almacenan en base de datos sin redespliegue (DA-02). | PM / Dev Lead |
| Resistencia docente al ingreso de notas en sistema web | Baja | Medio | Interfaz Zero-Training, validación inmediata en tiempo real, feedback visual de progreso. Piloto con institución aliada. | UX Design |
| Alucinación de invariantes regulatorias por agentes de IA en código generado | Media | Crítico | Validación cruzada por `compliance-agent` antes de cada merge; golden tests con casos reales de exportación SIE; revisión humana obligatoria en outputs de riesgo regulatorio. | Dev Lead |
| Docente olvida subir notas antes del cierre del periodo | Media | Alto | Alertas automáticas de progreso en dashboard de Secretaría y Director; flujo UC-05 para solicitar ventana de corrección con trazabilidad. | PM |
| Fuga de datos entre tenants (colegios) por error de configuración RLS | Baja | Crítico | Auditoría automática en CI de toda nueva tabla: el `multitenant-audit-agent` verifica `tenant_id` y política RLS activa antes de cada despliegue (DA-01). | Dev Lead |

---

## 17. Criterios de éxito del proyecto de negocio

- Reducción demostrable del ciclo de sincronización SIE de jornadas de madrugada a menos de 10 minutos (KPI-01 alcanzado en el primer cierre trimestral piloto).
- Tasa de error de integridad de datos = 0 % en exportaciones al SIE (KPI-02), validado con el caso de prueba canónico: estudiante antiguo, estudiante transferido y estudiante retirado en el mismo curso.
- Desaparición del trabajo administrativo en jornadas nocturnas (2:00 AM – 4:00 AM), verificado en entrevistas de seguimiento con Wendy (Secretaría) al finalizar el primer trimestre.
- Eliminación de los 10 ciclos de revisión manual por trimestre (KPI-03 = 0 ciclos).
- 100 % de las modificaciones retroactivas con registro completo en `audit_log` (BO-04), verificable ante cualquier auditoría ministerial.
- Business case positivo en el primer ciclo anual con ≥ 20 colegios activos.
- Satisfacción del sponsor (Director) ≥ 4/5 en encuesta de cierre de piloto.

---

## 18. Trazabilidad a documentos hijos

| BRD ID | Artefacto de arquitectura | UC / DA relacionado | PRD (pendiente) | FSD (pendiente) |
|--------|--------------------------|---------------------|-----------------|-----------------|
| BR-001 | `arquitectura_funcional_EduSync.md §UC-01` | UC-01, DA-01 | TBD | TBD |
| BR-002 | `arquitectura_funcional_EduSync.md §UC-01` | UC-01, DA-02 | TBD | TBD |
| BR-003 | `arquitectura_funcional_EduSync.md §UC-03` | UC-03, DA-02 | TBD | TBD |
| BR-004 | `arquitectura_funcional_EduSync.md §UC-04` | UC-04, DA-05 | TBD | TBD |
| BR-005 | `arquitectura_funcional_EduSync.md §UC-02, UC-05` | UC-02, UC-05, DA-03 | TBD | TBD |
| BR-006 | `estados_administracion.md` · `arquitectura_funcional_EduSync.md §UC-09` | UC-09 | TBD | TBD |
| BR-007 | `arquitectura_funcional_EduSync.md §DA-02, UC-09` | UC-09, DA-02 | TBD | TBD |
| BR-008 | `estados_administracion.md §Fase 4` | UC-01, UC-09 | TBD | TBD |
| BR-009 | `estados_cargar_notas.md §Flujo retroactivo` · `arquitectura_funcional_EduSync.md §UC-05` | UC-05, DA-03 | TBD | TBD |
| BR-010 | `arquitectura_funcional_EduSync.md §UC-10, UC-03` | UC-10, UC-03 | TBD | TBD |
| BR-011 | `arquitectura_funcional_EduSync.md §DA-03` | UC-01, UC-02, UC-04, UC-05 | TBD | TBD |
| BR-012 | `arquitectura_funcional_EduSync.md §UC-07` | UC-07, UC-03 | TBD | TBD |

---

## 19. Aprobaciones

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| Sponsor | Dirección Académica | | 14/05/2026 |
| PM / BA | Equipo G-EduSync | | 14/05/2026 |
| Arquitecto / Dev Lead | Rodrigo Aspeti | | 14/05/2026 |
| Revisor UX | Equipo de Diseño | | 14/05/2026 |

---

## 20. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v1.0 | 09/05/2026 | Consultor Estratégico | Creación inicial con BR-001..BR-005, BMC y KPIs base. |
| v2.0 | 14/05/2026 | Equipo G-EduSync | Consolidación con arquitectura funcional (10 UCs, 5 DAs) y diagramas de estado del Docente (18 estados) y Director (23 estados). Incorporación de BR-006..BR-012. Ampliación de reglas de negocio RB-01..RB-11. Nuevas personas: Director (Jeanneth). Nuevos KPIs: KPI-04, KPI-05. Nuevos BO: BO-04, BO-05. Riesgo adicional: alucinación por agentes IA y fuga multitenant. |

---

## 21. Anexo — PR-FAQ Amazon-style (Working Backwards)

### 21.1 Press Release

```text
La Paz, Bolivia — 1 de marzo de 2027

Equipo EduSync anuncia el lanzamiento de EduSync, la primera plataforma SaaS de gestión
académica boliviana que elimina el trabajo de madrugada de secretarias y asesores de colegios.

"Nuestros clientes pasaron de trabajar hasta las 4:00 AM durante el cierre de trimestre a
cerrar todo el proceso en menos de 10 minutos desde su escritorio", dijo Rodrigo Aspeti,
fundador de EduSync.

Hoy, el personal de cientos de colegios bolivianos copia y pega notas manualmente entre
múltiples hojas Excel antes de transcribirlas al sistema estatal SIE durante la madrugada,
bajo riesgo de multas de hasta 5 días de sueldo por errores de digitación.

EduSync permite que cada docente ingrese solo sus calificaciones desde el sistema, que el
motor algorítmico consolide automáticamente los promedios usando el estándar `floor` del
Ministerio, y que la secretaría exporte masivamente al SIE con un solo clic, vinculando
cada nota al código RUDE único del estudiante.

"Antes pasaba el fin de semana auditando filas de Excel. Ahora el sistema me avisa qué
docentes faltan y yo solo apruebo la exportación", comentó Wendy, secretaria de una
unidad educativa del piloto.

EduSync está disponible a partir del primer trimestre de 2027 para colegios privados y
de convenio de Bolivia. Para más información: www.edusync.bo
```

### 21.2 External FAQ

- **¿Qué es EduSync?** Una plataforma web que digitaliza el registro de calificaciones, consolida automáticamente los promedios y exporta al sistema gubernamental SIE sin necesidad de digitación manual.
- **¿Cómo me beneficia si soy docente?** Solo ingresas notas de tu materia. El sistema valida los rangos en tiempo real y te avisa si falta algo antes del cierre, eliminando el riesgo de descuentos salariales por error tipográfico.
- **¿Cómo me beneficia si soy secretaria?** Monitoreas el progreso de todos los docentes en tiempo real y exportas al SIE con un clic cuando todos están listos, sin trabajar de madrugada.
- **¿Qué pasa si el servidor SIE falla a mitad de la exportación?** EduSync guarda el progreso registro a registro. Al reintentar, reanuda desde donde quedó, sin duplicados y sin empezar desde cero.
- **¿Cuánto cuesta?** Setup Fee inicial de 200 Bs más suscripción anual por estudiante activo.
- **¿En qué se diferencia de Academium o Colegio360?** EduSync es la única plataforma que mapea al SIE exclusivamente por código RUDE, aplica el criterio `floor` del Ministerio y bloquea modificaciones retroactivas sin autorización del Director.

### 21.3 Internal FAQ

- **¿Por qué ahora y no en 6 meses?** La ventana de oportunidad es antes del primer cierre trimestral. Cada ciclo sin EduSync es un ciclo más de madrugadas, errores y riesgo de multas para el cliente.
- **¿Cuál es la inversión y el horizonte de retorno?** Business case positivo al primer año con ≥ 20 colegios activos (ver §15). La inversión principal es el desarrollo del motor de consolidación y la integración SIE.
- **¿Qué riesgos críticos existen?** (1) Fallo del servidor SIE — mitigado con reintentos idempotentes. (2) Cambio ministerial en formato de exportación — mitigado con arquitectura paramétrica sin redespliegue. (3) Resistencia docente — mitigado con interfaz Zero-Training.
- **¿Qué dependencias críticas existen?** Acceso al entorno de pruebas del SIE y aprobación de la plantilla de boletín ministerial vigente.
- **¿Cómo escalamos si la demanda supera la proyección?** La arquitectura multitenant con RLS en PostgreSQL y la estrategia de aislamiento por `tenant_id` permiten incorporar nuevos colegios sin cambios de código. La migración a schema separado por tenant es posible sin cambiar el modelo de dominio (DA-01).
