
# VISIÓN DE NEGOCIO Y ESTRATEGIA DE PRODUCTO: EDUSYNC

## ¿PROBLEMA QUE RESUELVE?
El sistema resuelve la grave ineficiencia y fragmentación de datos en la gestión de calificaciones escolares, la cual actualmente depende de hojas de cálculo individuales de Excel y de un proceso manual de "copiar y pegar" (triple digitación). Esto provoca desfases de alumnos en las listas, corrupciones de datos, inconsistencias por decimales invisibles y modificaciones retroactivas clandestinas. Además, elimina el cuello de botella extremo de la carga manual nota por nota al sistema estatal (SIE), el cual obliga a las secretarias y asesores a trabajar en horarios de madrugada bajo un alto nivel de estrés y miedo a sanciones económicas y legales.

## USUARIO PRINCIPAL #1 (Docente)
*   **¿Qué quiere lograr?**
    Registrar de manera autónoma, rápida y segura las calificaciones exclusivamente de su materia, eliminando la responsabilidad operativa de consolidar datos de otros colegas y perdiendo el temor a recibir descuentos salariales o sanciones por errores de tipeo manual.
*   **Tareas más importantes:**
    1. Ingresar calificaciones paramétricas organizadas por las dimensiones exigidas (Ser, Saber, Hacer, Decidir) con validación automática de rangos.
    2. Realizar el control de asistencia de sus estudiantes en su área correspondiente.
    3. Asegurar y enviar su registro mediante el "cierre operativo" de su materia, liberándose del proceso de transcripción de libretas.

## USUARIO PRINCIPAL #2 (Secretaria)
*   **¿Qué quiere lograr?**
    Obtener una "Única Fuente de Verdad" consolidada de manera automática para reportar al Ministerio de Educación (SIE), sin invertir fines de semana ni jornadas de madrugada limpiando, cruzando y auditando datos matemáticos manualmente.
*   **Tareas más importantes:**
    1. Monitorizar en tiempo real el progreso de la entrega de notas y visualizar los centralizadores consolidados automáticamente.
    2. Administrar la base de datos inmutable de estudiantes y generar los boletines oficiales en PDF con un clic.
    3. Exportar masivamente y sincronizar las calificaciones de manera segura con el formato del sistema gubernamental SIE.

## USUARIO PRINCIPAL #3 (Director)
*   **¿Qué quiere lograr?**
    Tener visibilidad estratégica y control institucional en tiempo real sobre el rendimiento académico, eliminando la "ceguera de datos" para poder auditar el colegio de manera ágil y tomar decisiones sin depender cien por ciento del trabajo manual de la secretaría.
*   **Tareas más importantes:**
    1. Revisar reportes estadísticos automatizados (mejores estudiantes, índices de reprobación por materia/nivel).
    2. Supervisar y auditar el progreso de la carga de calificaciones de todo el plantel docente en tiempo real.
    3. Autorizar o rechazar de manera jerárquica las solicitudes de modificación retroactiva de calificaciones para periodos académicos ya cerrados.

## ¿QUÉ DEBE HACER EL SISTEMA? (Capacidades funcionales clave)
1. Habilitar la carga de notas descentralizada con un estricto control de accesos por rol (RBAC) para que cada profesor manipule únicamente su área.
2. Validar datos en tiempo real mediante una interfaz antierrores proactiva que bloquee instantáneamente valores fuera de los rangos normativos (ej. no guardar si la nota es mayor a 35 o menor a 20).
3. Estandarizar algorítmicamente el truncado o redondeo de decimales directamente desde la base de datos para impedir descuadres matemáticos invisibles en el promedio anual.
4. Consolidar automáticamente los promedios en centralizadores trimestrales y anuales, erradicando al 100% el esfuerzo de "copiado y pegado" de Excel a Excel.
5. Gestionar nóminas estudiantiles inmutables donde los docentes no tengan permisos para alterar, eliminar o reordenar listas, previniendo asignaciones cruzadas de notas.
6. Aplicar un congelamiento temporal (estado de "Solo Lectura") a los datos al cierre de cada periodo académico para bloquear cualquier alteración retroactiva clandestina.
7. Registrar un "Log de Auditoría" inalterable que detalle el historial de quién, cuándo y qué calificación específica fue modificada.
8. Generar reportes académicos, estadísticas globales y boletines oficiales listos para su impresión o distribución.
9. Sincronizar y exportar la sábana masiva de calificaciones asociadas por el código único (RUDE) de cada estudiante hacia el formato exacto requerido por el sistema ministerial (SIE).

## ¿CÓMO SABRÍAMOS QUE FUNCIONA? (Criterio de éxito)
Sabremos que funciona cuando el "Time-on-Task" del ciclo administrativo de cierre y carga de notas al Ministerio se reduzca drásticamente de madrugadas y fines de semana enteros a tan solo cuestión de minutos. Además, se debe evidenciar el alcance de una tasa de error del 0% en inconsistencias derivadas de transcribir información manualmente, eliminando los históricos 10 ciclos de auditoría humana y logrando una disminución visible del nivel de agotamiento y estrés del personal docente y administrativo.
