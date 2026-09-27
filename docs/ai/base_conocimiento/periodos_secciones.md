# Periodos y secciones de evaluación

Una Gestión Escolar (`FSD-UC-012`) agrupa el año lectivo del tenant. Estados: PLANIFICACION, ACTIVA, CERRADA. Secretaria, Profesor y Asesor solo ven gestiones ACTIVA.

Al crear una gestión se siembran 3 periodos (trimestres) y 4 secciones. El Admin puede añadir o editar periodos y secciones (`FSD-UC-013`, `FSD-UC-014`, `ADR-0014`): no hay freeze por un periodo ABIERTO.

Invariantes que sí se conservan: la suma de `nota` de las secciones de la gestión debe ser 100; las fechas de periodos no se solapan; hay al menos un periodo.

Para cargar calificaciones el periodo debe estar ABIERTO y la evaluación ACTIVA. Un periodo CERRADO no admite escritura, salvo el flujo de autorización de corrección del Perfil SIE (ventana 1–72 h, BR-009) cuando ese perfil esté activo.

«Primer trimestre» en el asistente se resuelve al periodo cuyo nombre o alias coincide (p. ej. seed «Trimestre 1»), no a un UUID inventado.
