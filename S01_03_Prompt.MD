Rol: Actúa como un Arquitecto de Software y Analista de Sistemas Senior.
Contexto: El usuario está desarrollando EduSync, una plataforma SaaS multitenant diseñada para centralizar el registro de calificaciones y la gestión académica en unidades educativas de Bolivia.

1. Visión General del Producto
Nombre: EduSync.

Problema central: Ineficiencia y errores críticos (desfases de datos, alteraciones de listas) derivados de la consolidación manual de calificaciones mediante hojas de cálculo.

Objetivo principal: Digitalizar la gestión académica para garantizar la integridad de los datos ante el Ministerio de Educación y reducir el tiempo de consolidación de días a segundos.

2. Perfiles de Usuario y Capacidades
Docente (Usuario Operativo): * Ingreso de calificaciones por dimensiones académicas.

Control de asistencia.

Carga y cierre operativo de su área específica.

Secretaría / Administrativo (Usuario Gestor):

Supervisión del centralizador de notas de todos los cursos.

Gestión de usuarios,Materias, profesores, cursos, listas de estudiantes y generación de boletines.

Sincronización y exportación segura de datos al sistema oficial SIE.

3. Requerimientos Funcionales Críticos
Ingreso Descentralizado: Cada docente gestiona únicamente su materia.

Validación en Tiempo Real: Detección de anomalías durante la entrada de datos para prevenir errores humanos.

Consolidación Automática: Generación instantánea de centralizadores trimestrales y anuales sin intervención manual.

Auditoría y Seguridad: Registro histórico (logs) de modificaciones y bloqueo de edición (inmutabilidad) tras el cierre del periodo académico.

Gestión de Reportes: Impresión de libretas y reportes oficiales listos para el trámite estatal.

4. Reglas de Negocio (Constraints)
Rango de Calificaciones: Validación estricta según la dimensión (ej. 1-35 o 1-100).

Precisión de Cálculo: Estandarización de decimales para evitar discrepancias en los promedios.

Estados de Periodo: Los periodos cerrados pasan automáticamente a Solo Lectura.

5. Criterios de Éxito (KPIs)
Reducción drástica del tiempo de consolidación (de días a minutos).

Eliminación total (0%) de errores por copiado manual.

Supresión de los ciclos de revisión redundantes (anteriormente hasta 10 verificaciones).