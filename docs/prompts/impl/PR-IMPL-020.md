# PR-IMPL-020 — Frontend: rediseño visual del login (Design System EduSync)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-020` |
| Título | Tokens visuales + `shared/ui/` (card, form-field, button) y rediseño de `login.page.ts` según el prototipo Figma |
| Artefacto origen | `docs/design/DD-UC-020.md` |
| ID origen | `DD-UC-020` (`FSD-UC-021`), `ADR-0015` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Frontend Engineer con experiencia en Angular 21 (standalone
components, signals) y sistemas de diseño basados en design tokens (CSS
custom properties / SCSS), sin dependencias de librerías de componentes de
terceros.
```

### 1.2 Task

```text
Implementa DD-UC-020 (crea ADR-0015) segun docs/design/DD-UC-020.md §2:
(1) tokens visuales globales extraidos del Figma "Prototipo Administrativo"
(nodo 399:20447, "Iniciar Sesion"); (2) tres componentes standalone minimos
en frontend/src/app/shared/ui/ (card, form-field, button); (3) rediseno de
login.page.ts/html/scss consumiendo esos componentes. NO toques AuthService,
el interceptor JWT, los guards de rol ni el contrato POST /api/v1/auth/login.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-020.md §1 (alcance dentro/fuera) y §2
  (diseno detallado, fidelidad al Figma).
- ADR: ADR-0015 (tokens propios + shared/ui/, sin Angular Material/PrimeNG/
  Tailwind — Alternativa C).
- Prerrequisito: PR-IMPL-004/006 ya ejecutados (login.page.ts existe, sin
  sistema de diseno, DD-UC-006 §2).
- Restricciones: sin libreria de UI de terceros nueva; sin cambios en
  core/auth/ (AuthService, auth.interceptor.ts, role.guard.ts); sin cambios
  en el contrato POST /api/v1/auth/login; "Olvidaste tu contraseña" y
  "Solicitar acceso institucional" se renderizan como elementos
  informativos/no funcionales (DD-UC-020 §1, fuera de alcance su backend).
```

### 1.4 Reasoning

```text
1. Crear frontend/src/styles/_tokens.scss: variables de color (primario navy,
   fondo, texto, borde), tipografia (familia, tamaños heading/body/label),
   espaciado, radios y sombra, extraidos 1:1 de las capturas del Figma
   (nodo 399:20447).
2. Crear frontend/src/app/shared/ui/card/card.component.ts: contenedor
   standalone con @Input() para el acento decorativo opcional (esquina
   superior, aria-hidden).
3. Crear frontend/src/app/shared/ui/form-field/form-field.component.ts:
   @Input() label (se transforma a mayusculas via CSS, no en el dato),
   @Input() icon opcional, proyecta un <input> vía ng-content o
   [formControl] pasado por @Input().
4. Crear frontend/src/app/shared/ui/button/button.component.ts:
   @Input() variant ('primary'|'secondary'), @Input() fullWidth boolean.
5. Reescribir login.page.html: encabezado de marca (EduSync + "PORTAL
   ACADEMICO") fuera de la tarjeta; app-card con heading "Bienvenido de
   nuevo" + subtitulo; app-form-field x2 (correo, contraseña) dentro del
   <form> reactivo YA existente (mismo FormGroup/onSubmit, sin tocar la
   logica); app-button submit "Iniciar Sesion"; enlace informativo
   "¿Olvidaste tu contraseña?" (sin routerLink funcional, solo texto o
   modal informativo "Contacta a tu administrador"); pie de pagina estatico.
6. login.page.ts: solo ajustar el template/imports (CardComponent,
   FormFieldComponent, ButtonComponent); el FormGroup, el metodo onSubmit()
   y el manejo de errores 401/403 permanecen identicos.
7. login.page.scss: reemplazar estilos ad-hoc por los tokens de
   _tokens.scss.
8. ng build y verificacion visual manual contra el Figma.
```

### 1.5 Stop condition

```text
Detente cuando: (a) _tokens.scss existe y ningun color/tamaño se hardcodea
fuera de el en los archivos tocados; (b) los 3 componentes de shared/ui/
existen y login.page.html los consume; (c) el submit del formulario sigue
invocando exactamente el mismo AuthService.login() que antes (sin cambios
de firma ni de logica); (d) el enlace "olvidaste tu contraseña" y el texto
"solicitar acceso institucional" estan presentes visualmente pero no
navegan a ninguna ruta nueva ni llaman a ningun endpoint nuevo; (e) ng build
en verde. NO implementes un flujo de "recuperar contraseña por email" NUEVO,
NO implementes autoregistro de tenants, NO toques core/auth/**, NO edites
docs/baseline/**.
```

### 1.6 Output

```text
Formato: codigo fuente real en frontend/ (no markdown).
Extracto esperado:
frontend/src/styles/_tokens.scss (nuevo)
frontend/src/app/shared/ui/card/card.component.ts (nuevo)
frontend/src/app/shared/ui/form-field/form-field.component.ts (nuevo)
frontend/src/app/shared/ui/button/button.component.ts (nuevo)
frontend/src/app/features/auth/login.page.ts (delta, solo template/imports)
frontend/src/app/features/auth/login.page.html (delta)
frontend/src/app/features/auth/login.page.scss (delta)
```

## 2. Invariantes del prompt

- `core/auth/` (`AuthService`, `auth.interceptor.ts`, `role.guard.ts`) **no** se modifica.
- El contrato `POST /api/v1/auth/login` **no** cambia (sin delta de backend).
- Ningún color/tipografía/espaciado se hardcodea fuera de `_tokens.scss` en los archivos tocados por este prompt.
- Sin dependencias nuevas en `package.json` (`ADR-0015`, Alternativa C).
- `ng build` **debe** quedar en verde.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_AUTHSERVICE_MODIFICADO` | Se tocó `AuthService`/interceptor/guards | Revertir — fuera de alcance de `DD-UC-020` |
| `E_LIBRERIA_UI_AGREGADA` | Se agregó Angular Material/PrimeNG/Tailwind u otra dependencia de UI | Revertir — contradice `ADR-0015` Alternativa C |
| `E_FUNCIONALIDAD_INVENTADA` | Se implementó un flujo real de "olvidé mi contraseña" por email o autoregistro de tenant | Revertir — requiere un `FSD-UC` nuevo, no definido; `DD-UC-020` los deja explícitamente fuera de alcance |
| `E_ESTILOS_FUERA_DE_TOKENS` | Colores/tamaños hardcodeados fuera de `_tokens.scss` | Corregir, mover a tokens |
| `E_BASELINE_TOCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: reutilizar el `FormGroup`/`onSubmit()` de `login.page.ts` sin cambios de lógica.
- MUST: `ng build` en verde antes de considerar el prompt completo.
- MUST NOT: modificar `core/auth/**` ni el contrato de `POST /api/v1/auth/login`.
- MUST NOT: agregar dependencias de UI de terceros (`ADR-0015`).
- MUST NOT: implementar el flujo funcional de "olvidé mi contraseña" por email ni autoregistro de tenants (fuera de alcance, `DD-UC-020` §1).
- MUST NOT: editar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-020` | PR-IMPL-020 | `dev-agent` | `frontend/src/styles/_tokens.scss`, `frontend/src/app/shared/ui/**`, `login.page.*` (delta) |
| FSD | `FSD-UC-021` (login, superficie de presentación) | PR-IMPL-020 | `dev-agent` | Login rediseñado, sin cambio de contrato |
| ADR | `ADR-0015` | PR-IMPL-020 | `dev-agent` | Primer consumidor real del Design System visual propio |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-020` completo; `login.page.ts` existente de `DD-UC-004`/`006`.
- **Output esperado**: login visualmente alineado al Figma (nodo `399:20447`); el submit sigue autenticando exactamente igual que antes; `ng build` verde.

### 6.2 Caso borde

- **Input**: credenciales inválidas (401) o tenant suspendido (403 `E_TENANT_NO_ACTIVO`).
- **Output esperado**: el manejo de error visual se conserva (mismo mensaje/lugar que antes de este rediseño), solo cambia el envoltorio visual.

### 6.3 Caso adversarial

- **Input**: solicitud de "ya que estás, implementa también el reset de contraseña por email" o "agrega Angular Material para que se vea mejor".
- **Comportamiento esperado**: rechazo — `E_FUNCIONALIDAD_INVENTADA` / `E_LIBRERIA_UI_AGREGADA`; ambos fuera de alcance de `DD-UC-020`/`ADR-0015`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 13/09/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-020.md` v0.2. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 13/09/2026 | Rodrigo Aspeti | **Ejecución**: código real generado — `frontend/src/styles/_tokens.scss`, `frontend/src/app/shared/ui/{card,form-field,button}`, `login.page.ts` reescrito. `FormGroup`/`onSubmit()`/`AuthService.login()` idénticos a `DD-UC-004`; sin cambios en `core/auth/**` ni en el contrato `POST /api/v1/auth/login`. "¿Olvidaste tu contraseña?"/"Solicitar acceso institucional" informativos (toggle local, sin ruta/endpoint nuevo). Verificación: `device_bash` en la máquina del usuario sigue bloqueado (incidente de montaje de Windows del 8/09/2026, ver `AGENTS.md`/notas de sesión); se replicó un *workspace* Angular 21.2.23 aislado con las mismas versiones de `package.json` y la `AuthService`/`jwt.util.ts` reales (sin editar) para compilar exactamente estos 5 archivos con `strictTemplates`/`strictInputAccessModifiers` activos — build verde, sin errores ni warnings (`main.js` 1.55 MB, `styles.css` 879 bytes). El `ng build` real sobre el repositorio del usuario queda pendiente de confirmación. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 13/09/2026 | aprobado (prompt) | Diseño (`DD-UC-020`) y prompt aprobados en el mismo turno; ejecución de código real pendiente |
| Rodrigo Aspeti | 13/09/2026 | ejecutado | Código generado y validado con build Angular 21.2.23 aislado (mismas versiones, `AuthService` real sin editar); `ng build` real sobre el repo del usuario pendiente de confirmación por el bloqueo de `device_bash` |
