# Estudiantes, cursos, materias y profesores

Curso y Paralelo (`FSD-UC-017`) son catálogo del tenant (ej. «1ro» + paralelo «A»). Materia (`FSD-UC-018`) se asigna a curso/paralelo y a un profesor; no se asigna profesor si la materia aún no tiene curso (`E_MATERIA_SIN_CURSO`).

Estudiante (`FSD-UC-020`) se identifica en escritura y exportación por código **RUDE**, no por nombre, apellido ni número de lista (BR-004). El RUDE es único por tenant. La inscripción vincula estudiante + gestión + curso/paralelo; no se duplica la misma inscripción (`E_INSCRIPCION_DUPLICADA`).

Profesor (`FSD-UC-019`) no es una tabla aparte: es el perfil de un Usuario con rol PROFESOR. El alta de la persona está en usuarios (`FSD-UC-021`); las asignaciones de materia se consultan en sentido inverso.

El asistente resuelve nombres a IDs con `find_*` y consulta notas o nómina con `consultar_academico`. Si hay dos «Juan», pide aclaración; no elige uno al azar.
