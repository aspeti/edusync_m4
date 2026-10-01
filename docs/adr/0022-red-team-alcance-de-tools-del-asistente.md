# Architecture Decision Record (ADR)

## ADR-0022: El modelo no elige el alcance de una tool del asistente

### Metadatos

| Campo | Valor |
|-------|-------|
| Numero | `0022` |
| Titulo | Descartar en el backend los argumentos de alcance que el modelo anade a una tool |
| Fecha | 29/09/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aceptada** |
| Alcance | `PoliticaAlcanceAgente` dentro de `EjecutarConsultaAgenteService`. No toca `docs/baseline/**`. |

### Contexto

El catalogo `tools/red-team-agent` (RT-AUTH-003, RT-TENANT-001) muestra que, si el modelo pide una tool que si esta en el catalogo, el bucle reenvia los argumentos tal cual. El JWT sigue siendo el de la peticion.

### Problema

`schoolId` o `tenantId` elegidos por el modelo no son la sesion. Hoy el aislamiento depende de que el API ignore esos campos y use solo el JWT.

### Amenaza

Authorization bypass y tenant isolation si algun endpoint honra el argumento del cuerpo por encima del token.

### Evidencia

Ataques apagados en `catalog/attacks.json`. No se activaron en CI porque el producto todavia reenvia el argumento. El gate determinista de los 15 ataques habilitados paso.

### Opciones

1. Dejar el comportamiento y confiar en cada controller.
2. Quitar `tenantId` y `schoolId` de los argumentos antes de ejecutar la tool, y limitar `list_usuarios` por rol en el grafo.

### Arquitectura recomendada

Opcion 2, aplicada en `PoliticaAlcanceAgente`: antes de ejecutar una tool se descartan `tenantId`, `tenant_id`, `schoolId` y `school_id`. `list_usuarios` solo corre si la autenticacion de la peticion trae `ROLE_ADMIN`, `ROLE_SECRETARIA` o `ROLE_SYSADMIN`. PROFESOR y ASESOR reciben un rechazo y la tool no se llama.

### Impacto de seguridad

Cierra RT-AUTH-003 y RT-TENANT-001. RT-EXFIL-002 (profesor y `list_usuarios`) se decide en el mismo cambio o se deja explicito como permitido.

### Migracion

Catalogo `1.1.0`. `RT-AUTH-003`, `RT-TENANT-001` y `RT-EXFIL-002` quedaron `enabled: true` en el mismo cambio. `RT-EXFIL-001` (el formateador oculta correo y RUDE) corre con rol `ADMIN`.
