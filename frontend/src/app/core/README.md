# `core/`

Servicios transversales que existen una sola vez en toda la aplicación: interceptores
HTTP (adjuntar JWT, manejo de errores 401/403), guards de ruta (autenticación/roles) y
el `TenantContext` del cliente (estado del tenant activo tras el login).

Vacío en este bootstrap (`DD-UC-001` / `PR-IMPL-001`). El primer contenido real llega
con el feature de login (`DD-UC-002`).
