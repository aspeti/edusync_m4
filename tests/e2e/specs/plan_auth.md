# Plan E2E — Auth / Login (sección)

## Resumen
Login JWT, redirect por rol, restablecer password público. Sin LLM.

## Auditoría del plan
| Caso | Veredicto | Motivo |
|------|-----------|--------|
| 1.1 SYSADMIN feliz | aceptado | F03 |
| 1.2 Credenciales inválidas | aceptado | F02 |
| 1.3 Formulario login visible | aceptado | F01 (semilla/tradicional) |
| 1.4 Cerrar sesión | aceptado | F04 (tradicional) |
| 2.1 Restablecer password UI | aceptado | F23 |
| 2.2 Verificar texto completo error 500 | descartado | no verifica interfaz prometida |

### 1.1 Camino feliz SYSADMIN
**Pasos:** /login → E2E_SYSADMIN_* → Iniciar sesión
**Resultado esperado:** /plataforma/tenants; tenants-heading

### 1.2 Credenciales inválidas
**Pasos:** /login → email inventado → Iniciar sesión
**Resultado esperado:** login-error contiene Credenciales inválidas; URL /login

### 2.1 Pantalla restablecer password
**Pasos:** goto /restablecer-password
**Resultado esperado:** heading Restablecer contraseña; labels Token y Nueva contraseña
