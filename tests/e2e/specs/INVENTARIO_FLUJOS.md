# Inventario de flujos E2E — EduSync (obligatorio entregable)

> Una línea = flujo de persona real. Lo que no está aquí **no se consideró probado**.
> Columna Test: archivo que cubre el flujo (agente preferente; tradicional = patrón / red de seguridad).

| # | Flujo (persona hace → ve) | Rol | Sección plan | Test que cubre | Estado |
|---|---------------------------|-----|--------------|----------------|--------|
| F01 | Abrir /login → ve marca EduSync, formulario y botón Iniciar sesión | Público | Auth | `tradicional/login.spec.ts` + `seed.spec.ts` | Cubierto |
| F02 | Credenciales inválidas → alerta login-error y permanece en /login | Público | Auth | `agente/caso_1_2.spec.ts` + `tradicional/login.spec.ts` | Cubierto (agente) |
| F03 | Login SYSADMIN → /plataforma/tenants, heading Tenants | SYSADMIN | Auth / Plataforma | `agente/caso_1_1.spec.ts` (+ auditado) | Cubierto (agente) |
| F04 | SYSADMIN Cerrar sesión → vuelve a /login | SYSADMIN | Auth | `tradicional/login.spec.ts` | Cubierto |
| F05 | SYSADMIN abre + Nuevo Tenant → formulario Paso 1 | SYSADMIN | Plataforma | `agente/caso_plat_1_1.auditado.spec.ts` | Cubierto (agente) |
| F06 | SYSADMIN crea Tenant con datos únicos → paso Crear admin | SYSADMIN | Plataforma | `agente/caso_plat_1_1.auditado.spec.ts` | Cubierto (agente) |
| F07 | Login ADMIN demo → /usuarios, sin enlace Tenants | ADMIN | Auth / Admin | `agente/caso_admin_1_1.auditado.spec.ts` | Cubierto (agente) |
| F08 | ADMIN ve nav Usuarios, Cursos, Gestión Escolar, Materias, Estudiantes, Profesores | ADMIN | Admin | `tradicional/admin.spec.ts` + plan 1.2 | Cubierto |
| F09 | ADMIN lista Usuarios + enlace + Nuevo Usuario | ADMIN | Usuarios | `tradicional/admin.spec.ts` | Cubierto |
| F10 | ADMIN abre Nuevo Usuario → form; Crear deshabilitado sin roles | ADMIN | Usuarios | `tradicional/admin.spec.ts` / plan 3.3 | Cubierto |
| F11 | ADMIN lista Gestión Escolar + alta | ADMIN | Gestión | `tradicional/admin.spec.ts` | Cubierto |
| F12 | ADMIN crea Gestión Escolar con fechas → vuelve a lista | ADMIN | Gestión | `agente/caso_ges_1_1.auditado.spec.ts` | Cubierto (agente) |
| F13 | ADMIN abre Periodos de una gestión existente | ADMIN | Gestión | `agente/caso_ges_1_1.auditado.spec.ts` | Cubierto (agente) |
| F14 | ADMIN abre Secciones de una gestión existente | ADMIN | Gestión | `agente/caso_ges_1_1.auditado.spec.ts` | Cubierto (agente) |
| F15 | ADMIN lista Cursos + alta | ADMIN | Cursos | `tradicional/admin.spec.ts` | Cubierto |
| F16 | ADMIN crea Curso con nombre único → aparece en lista | ADMIN | Cursos | `agente/caso_admin_3_1.auditado.spec.ts` | Cubierto (agente) |
| F17 | ADMIN abre Paralelos del primer curso de la lista | ADMIN | Cursos | `agente/caso_cur_mat.auditado.spec.ts` | Cubierto (agente) |
| F18 | ADMIN lista Materias + alta | ADMIN | Materias | `tradicional/admin.spec.ts` | Cubierto |
| F19 | ADMIN crea Materia → detalle de la materia | ADMIN | Materias | `agente/caso_cur_mat.auditado.spec.ts` | Cubierto (agente) |
| F20 | ADMIN lista Estudiantes + alta | ADMIN | Estudiantes | `tradicional/admin.spec.ts` | Cubierto |
| F21 | ADMIN crea Estudiante RUDE único → detalle | ADMIN | Estudiantes | `tradicional/admin.spec.ts` / plan 3.2 | Cubierto |
| F22 | ADMIN lista Profesores (solo lectura) + enlace Crear usuario | ADMIN | Profesores | `tradicional/admin.spec.ts` | Cubierto |
| F23 | Público abre /restablecer-password → formulario Token + contraseña | Público | Auth | `agente/caso_auth_2_1.auditado.spec.ts` | Cubierto (agente) |
| F24 | SECRETARIA login / consolas (sin Gestión Escolar nav ADMIN-only) | SECRETARIA | — | — | **Fuera de alcance E2E** (sin usuario seed E2E_SECRETARIA) |
| F25 | PROFESOR Mis materias → evaluaciones → calificaciones | PROFESOR | — | — | **Fuera de alcance E2E** (sin usuario seed E2E_PROFESOR + datos) |
| F26 | ADMIN matriz calificaciones / round HALF_UP | ADMIN/PROF | — | — | **Fuera de alcance E2E** → unit/integration + golden |
| F27 | Exportación SIE / floor() / RLS cross-tenant | Sistema | — | — | **Fuera de alcance E2E** → golden/integration |
| F28 | Chat LLM `POST /api/v1/ai/chat` | — | — | — | **Fuera de alcance E2E** → evals (redacción del modelo) |
| F29 | Alta usuario completa + reset password end-to-end (email) | ADMIN | Usuarios | — | **Descartado**: reset es log-only; alta completa diferida (ruido de datos) |

## Totales inventario

| Categoría | Cantidad |
|-----------|----------|
| Flujos en alcance | 23 (F01–F23) |
| Cubiertos con test | 23 |
| Fuera de alcance / descartados | 6 (F24–F29) |
| Tests agente dedicados | ver `AUDITORIA_TABLA.md` |
