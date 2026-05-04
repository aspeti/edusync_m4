NOMBRE DEL SISTEMA: EduSync
PARTE DEL SISTEMA QUE CONSIDERAMOS MÁS DIFÍCIL: Validación y Consolidación Automática en el Centralizador Anual
¿POR QUÉ ES DIFÍCIL? El proceso exige unificar datos provenientes de múltiples docentes, evitando la "explosión combinatoria" de errores generada por el mal manejo de decimales y desajustes en las nóminas de estudiantes (por inscripciones tardías). Si esta consolidación falla, se altera la integridad de la información oficial, provocando que se asignen notas a estudiantes equivocados por desplazamientos de celdas, lo cual acarrea sanciones del Ministerio de Educación (SIE) y problemas legales con los padres de familia
.
ESCENARIO CONCRETO:
Actor: Docente de Asignatura (ej. Lenguaje)
Punto de partida (datos o situación): El docente ingresa al sistema para cargar las calificaciones del primer trimestre en un curso donde la lista de estudiantes ha sido modificada recientemente porque se incorporó un alumno nuevo a mitad del periodo
.
Pasos que ocurren:
El docente introduce las calificaciones parciales en las dimensiones académicas correspondientes (Ser, Saber, Hacer, Decidir) para cada estudiante de su lista.
El sistema valida instantáneamente que ninguna nota viole los límites máximos permitidos por el reglamento, estandariza cualquier intento de usar formatos libres o decimales, y vincula el dato estrictamente al ID único del estudiante (RUDE)
.
Al confirmar y enviar, el sistema impacta el Centralizador Anual actualizando únicamente la columna de la materia de Lenguaje para esos estudiantes, y una vez validado, bloquea la edición
.
Resultado esperado: El centralizador anual refleja las notas exactas e inmutables del trimestre para esa materia, con promedios estandarizados y asociados al estudiante correcto, sin alterar las calificaciones subidas por otros docentes ni desfasar las filas debido al alumno nuevo.
REGLAS / RESTRICCIONES QUE NO PUEDEN ROMPERSE:
Las calificaciones no pueden guardarse en la base de datos si superan los límites numéricos de su dimensión (ej. no se puede ingresar 40 si la dimensión del "Saber" se evalúa sobre 35)
.
La consolidación de notas hacia el centralizador debe enlazar el dato usando el código único del estudiante (RUDE), jamás dependiendo del número de fila visual o el orden en la lista, para blindar el sistema contra errores de "copiar y pegar" por desfases de alumnos
.
Los periodos académicos que han sido marcados como cerrados por la dirección adoptan un estado de "Solo Lectura" (Read-Only) inmutable; el sistema debe bloquear cualquier intento de modificación retroactiva
.
CASO DE PRUEBA CONCRETO (obligatorio):
DATOS DE ENTRADA:
Curso: 6to de Secundaria. Materia: Lenguaje.
Regla de validación: Dimensión "Saber" máximo 35 puntos. Dimensión "Hacer" máximo 35 puntos. No se aceptan decimales.
Estudiante A (RUDE: 1001 - Antiguo): Saber = 30, Hacer = 30.
Estudiante B (RUDE: 1002 - Nuevo alumno, insertado en medio de la lista): Saber = 40 (Error de tipeo intencional), Hacer = 20.
Estudiante C (RUDE: 1003 - Antiguo): Saber = 25.5 (Intento de usar decimales), Hacer = 25.
DATOS DE SALIDA ESPERADOS:
Para Estudiante A: Notas guardadas correctamente y vinculadas al RUDE 1001 en el centralizador.
Para Estudiante B: El sistema bloquea instantáneamente la celda del "Saber", muestra un estado de error visual (alerta roja) indicando que 40 excede el límite de 35, e impide guardar las notas del curso hasta que se corrija
.
Para Estudiante C: El sistema restringe el ingreso de caracteres no enteros (rechaza el ".5") o lo estandariza automáticamente aplicando una regla de redondeo oficial, impidiendo la propagación de decimales invisibles
.
En el Centralizador: Las notas de A y C se mapean a los alumnos correctos sin "saltarse" posiciones debido a la reciente inclusión del Estudiante B
.
¿CÓMO MEDIRÍAMOS QUE FUNCIONA?: Sabremos que funciona cuando la secretaría pueda generar e imprimir el boletín oficial para el sistema estatal SIE directamente desde el centralizador, comprobando que los datos coinciden con un 100% de exactitud con las notas introducidas por los docentes, sin requerir auditorías manuales prolongadas y eliminando definitivamente los históricos 10 ciclos de revisión de errores que tomaba el proceso anterior