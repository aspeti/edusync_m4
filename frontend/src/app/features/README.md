# `features/`

Un subdirectorio por *feature* de negocio (p. ej. `features/auth/`, `features/tenants/`),
cada uno con sus propios componentes, rutas y llamadas HTTP. Convención fijada en
`docs/design/DD-UC-001.md` §2: "todo feature nuevo de frontend vive bajo
`src/app/features/<feature>/`".

Vacío en este bootstrap (`DD-UC-001` / `PR-IMPL-001`). El primer feature real (login) se
añade en `DD-UC-002`.
