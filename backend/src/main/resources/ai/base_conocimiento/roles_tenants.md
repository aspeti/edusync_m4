# Roles, tenants e identidad

EduSync es SaaS multi-tenant (`ADR-0009`, `FSD-UC-011`). Cada colegio es un Tenant con ciclo de suscripción. El SysAdmin no pertenece a un tenant (`tenant_id` nulo) y no se combina con roles de institución (`ADR-0010`).

Roles de tenant (un usuario puede tener varios a la vez):

- ADMIN (Director): configura gestión, cursos, periodos, usuarios.
- SECRETARIA: opera catálogos académicos de lectura/alta según el caso de uso (estudiantes, materias, profesores).
- PROFESOR (Docente): carga evaluaciones y calificaciones de sus materias. No modifica la nómina.
- ASESOR: visibilidad de gestión ACTIVA; el curso asignado al asesor es un refinamiento pendiente.

Login: JWT. Toda consulta del asistente corre con el token del usuario (mismo RBAC y RLS). No se cruzan datos de otro tenant.
