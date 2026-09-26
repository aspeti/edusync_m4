# Plan E2E — EduSync (sección Login + Tenants)

> Generado / auditado por el equipo. El Planner propone; se audita ANTES de generar tests.
> Máximo 8 casos. No verificar redacción de LLM (EduSync no usa chat en este flujo).

## Resumen

SPA Angular 21: login JWT, redirect por rol. SYSADMIN → `/plataforma/tenants`.
Anclas: labels «Correo electrónico» / «Contraseña», botón «Iniciar sesión»,
`data-testid=login-error`, `data-testid=tenants-heading`.

## 1. Login

### 1.1 Camino feliz SYSADMIN
**Pasos:** Abrir /login → rellenar correo y contraseña del seed SYSADMIN → clic Iniciar sesión
**Resultado esperado:** URL contiene `/plataforma/tenants`; heading Tenants visible; enlace Tenants en nav

### 1.2 Credenciales inválidas
**Pasos:** Abrir /login → correo inventado + contraseña incorrecta → Iniciar sesión
**Resultado esperado:** alerta `login-error` contiene «Credenciales inválidas»; URL sigue en `/login`

### 1.3 Formulario visible al entrar
**Pasos:** Abrir /login
**Resultado esperado:** heading «Bienvenido de nuevo»; campos Correo y Contraseña; botón Iniciar sesión

## 2. Sesión

### 2.1 Cerrar sesión
**Pasos:** Login SYSADMIN → clic Cerrar sesión
**Resultado esperado:** URL `/login`; heading «Bienvenido de nuevo» visible

## 3. Tenants (SysAdmin)

### 3.1 Lista tras login
**Pasos:** Login SYSADMIN
**Resultado esperado:** heading Tenants; enlace «+ Nuevo Tenant»; botón Buscar

### 3.2 (descartado — borde sin valor)
~~Cinco logins seguidos~~ — no prueba nada nuevo (AUDITORIA pregunta 4).
