# PR-IMPL-006 — Frontend: consola de administración de Usuarios y Roles

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-006` |
| Título | Generación de la consola Angular de Usuarios y Roles + pantalla pública de confirmación de restablecimiento |
| Artefacto origen | `docs/design/DD-UC-006.md` |
| ID origen | `DD-UC-006` (`FSD-UC-021`, cierre de UI) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 04/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Frontend Engineer con experiencia en Angular 21 (standalone,
signals) consumiendo un backend Java 25 / Spring Boot 4.1.0 hexagonal.
```

### 1.2 Task

```text
Implementa la consola Angular de administracion de Usuarios y Roles segun
docs/design/DD-UC-006.md §2: features/usuarios/ (lista, alta multi-rol,
edicion de roles, cambio de estado, restablecer password) y
features/auth/reset-password-confirm/ (publica); ruta /usuarios protegida
por roleGuard(ADMIN); ruta publica /restablecer-password; ajuste de
login.page.ts (redirect ADMIN -> /usuarios). Sin delta de backend.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-006.md (patron sin design system de
  features/plataforma/, checkboxes de rol fijos, mensaje transparente sobre
  reset log-only, sin campo curso/paralelo para ASESOR).
- Contratos ya existentes (DD-UC-005, sin cambios): GET/POST /usuarios,
  PATCH /usuarios/{id}/roles, PATCH /usuarios/{id}/estado,
  POST /usuarios/{id}/restablecer-password,
  POST /api/v1/auth/restablecer-password/confirmar.
- ADRs: ADR-0008 (Angular 21), ADR-0010 (SYSADMIN nunca seleccionable).
- Prerrequisito: PR-IMPL-001..005 ya ejecutados (backend completo).
- Restricciones: NO delta de backend; NO campo curso/paralelo; NO simular
  envio de email; SYSADMIN nunca aparece como checkbox de rol.
```

### 1.4 Reasoning

```text
1. usuario.model.ts (UsuarioResponse).
2. usuarios-list.page.ts: GET /usuarios + dialogs roles/estado + boton reset.
3. usuario-create.page.ts: POST /usuarios con checkboxes ADMIN/SECRETARIA/
   ASESOR/PROFESOR.
4. reset-password-confirm.page.ts: form token+password, POST confirmar,
   mapear 410 a mensaje de enlace invalido/expirado.
5. app.routes.ts: /usuarios (roleGuard ADMIN), /restablecer-password (publica).
6. login.page.ts: redirect ADMIN -> /usuarios.
7. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) Admin autenticado ve /usuarios y puede crear/editar
roles/cambiar estado, (b) el boton "Restablecer password" llama al endpoint
y muestra el mensaje transparente de limitacion log-only, (c)
/restablecer-password publica completa el flujo con un token valido y
rechaza uno invalido/expirado con mensaje claro, (d) ng build en verde. No
implementes delta de backend ni simules envio de email.
```

### 1.6 Output

```text
Formato: codigo fuente real en frontend/ (no markdown).
Extracto esperado:
frontend/src/app/features/usuarios/usuarios-list.page.ts
frontend/src/app/features/usuarios/usuario-create.page.ts
frontend/src/app/features/auth/reset-password-confirm/reset-password-confirm.page.ts
frontend/src/app/app.routes.ts (delta)
```

## 2. Invariantes del prompt

- `SYSADMIN` **no debe** aparecer como opción de rol en ningun formulario de este prompt.
- El mensaje tras iniciar un restablecimiento **debe** ser transparente sobre la limitacion *log-only* (no simular un envio real).
- Ningun formulario **debe** pedir curso/paralelo para el rol `ASESOR`.
- `ng build` **debe** quedar en verde.
- Este prompt **no debe** modificar codigo backend.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_SYSADMIN_SELECCIONABLE` | El formulario de roles ofrece `SYSADMIN` como opcion | Rechazar; eliminar la opcion del template |
| `E_ENVIO_SIMULADO` | La UI muestra un mensaje de "correo enviado" | Corregir al mensaje transparente de `DD-UC-006` §2 |
| `E_CAMPO_CURSO_ASESOR` | Se agrego un campo de curso/paralelo al formulario | Revertir; corresponde a un DD futuro sobre `academico` |
| `E_DELTA_BACKEND` | El prompt modifico codigo bajo `backend/` | Rechazar; este prompt es frontend-only |

## 4. Guardrails

- MUST: `SYSADMIN` nunca seleccionable en la UI.
- MUST: mensaje transparente sobre el restablecimiento *log-only*.
- MUST: `ng build` en verde antes de considerar el prompt completo.
- MUST NOT: modificar ningún archivo bajo `backend/` ni `docs/baseline/**`.
- MUST NOT: agregar un campo de curso/paralelo para `ASESOR`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-006` | PR-IMPL-006 | `dev-agent` | `frontend/src/app/features/usuarios/**`, `features/auth/reset-password-confirm/**` |
| FSD | `FSD-UC-021` (cierre UI) | PR-IMPL-006 | `dev-agent` | Consola de administración de Usuarios y Roles |
| ADR | `ADR-0010` | PR-IMPL-006 | `dev-agent` | `SYSADMIN` nunca seleccionable en la UI |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-006` completo; backend de `DD-UC-005` disponible.
- **Output esperado**: Admin crea/edita/desactiva usuarios; `ng build` en verde.

### 6.2 Caso borde

- **Input**: confirmación con token expirado o ya usado.
- **Output esperado**: mensaje claro de enlace inválido/expirado (`410`).

### 6.3 Caso adversarial

- **Input**: solicitud de ofrecer `SYSADMIN` como rol o de simular un envío de correo.
- **Comportamiento esperado**: rechazo `E_SYSADMIN_SELECCIONABLE` / `E_ENVIO_SIMULADO`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 04/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-006.md` v1.0 | Sonnet |
| v0.2 | 04/08/2026 | Rodrigo Aspeti | Estado → **Ejecutado**: consola Angular real (lista, alta multi-rol, edición de roles, cambio de estado, restablecimiento de contraseña, confirmación pública); `ng build` en verde. Delta menor no listado en el diseño original: enlaces de nav condicionales por rol en `shell.component.ts`. Sincronizado con `DTP` v1.13 / `PROMPT_MAPPING` v2.12 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 04/08/2026 | aprobado (prompt) | Prompt aprobado; ejecución de código real en el mismo ciclo |
| Rodrigo Aspeti | 04/08/2026 | **ejecutado** | Código en working tree; docs sincronizados vía `dtp-sync`; commit formal pendiente |
