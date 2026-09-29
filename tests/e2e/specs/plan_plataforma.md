# Plan E2E — Plataforma Tenants (SYSADMIN)

## Resumen
Consola SysAdmin: lista, alta Tenant paso 1, wizard admin paso 2.

## Auditoría del plan
| Caso | Veredicto | Motivo |
|------|-----------|--------|
| 1.1 Abrir formulario Nuevo Tenant | aceptado | F05 |
| 1.2 Crear Tenant → paso admin | aceptado | F06 |
| 1.3 Cinco altas seguidas | descartado | no aporta (pregunta 4) |
| 1.4 Verificar UUID en pantalla | descartado | no es lo que ve la persona |

### 1.1 Abrir Nuevo Tenant
**Pasos:** Login SYSADMIN → + Nuevo Tenant
**Resultado esperado:** heading Nuevo Tenant — Paso 1 de 2; campos nombre y fechas

### 1.2 Crear Tenant
**Pasos:** Login SYSADMIN → Nuevo Tenant → nombre único + fechas → Crear Tenant
**Resultado esperado:** URL contiene /admin; heading o texto de crear admin
