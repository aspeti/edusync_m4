# PR-IMPL-004 — Frontend: autenticación y consola SysAdmin (login + Tenants)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-004` |
| Título | Generación de la UI Angular (login + consola SysAdmin) y delta backend `GET /tenants` |
| Artefacto origen | `docs/design/DD-UC-004.md` |
| ID origen | `DD-UC-004` (`FSD-UC-021` parcial + `FSD-UC-011` UI) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 19/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5 — el área `IMPL` es la única que se desvía de la convención plana `prompts/PR-<AREA>-NNN.md` usada por el resto de áreas.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Frontend Engineer con experiencia en Angular 21 (standalone) y un
backend Java 25 / Spring Boot 4.1.0 hexagonal. Dominas JWT en sessionStorage,
interceptores HttpClient, guards funcionales y consola admin minima.
```

### 1.2 Task

```text
Implementa el primer vertical slice de UI de EduSync segun docs/design/DD-UC-004.md
§2: (1) core/auth con JWT en sessionStorage, interceptor Bearer, authGuard y
roleGuard; (2) features/auth login; (3) features/plataforma (lista, alta tenant,
alta admin en dos pasos, cambio de estado); (4) layout shell minimo y proxy
Angular → backend; (5) delta backend GET /api/v1/plataforma/tenants restringido
a SYSADMIN. No implementes CRUD de usuarios ni el tenant demo.
```

### 1.3 Context

```text
- Documento fuente: docs/design/DD-UC-004.md (§1 objetivo, §2 diseno, §3
  alternativas: un DD UI, sessionStorage, GET /tenants incluido).
- ADRs: ADR-0008 (Angular 21), ADR-0010 (SYSADMIN), ADR-0011 (modulos backend;
  este prompt no crea modulo nuevo, solo delta en plataforma + frontend).
- Prerrequisito: PR-IMPL-001/002/003 ya ejecutados (APIs login y tenants existen
  excepto GET /tenants).
- Restricciones: JWT SOLO en sessionStorage (nunca localStorage); alta
  tenant+admin en DOS llamadas REST (nunca combinadas); no crear endpoint /me;
  no implementar DD-UC-005 ni tenant demo; UI funcional/admin (sin design system).
```

### 1.4 Reasoning

```text
1. Backend: ListarTenantsUseCase + TenantRepositoryPort.listarTodos() +
   GET en TenantController (@PreAuthorize SYSADMIN) + test integration.
2. Frontend core/auth: AuthService, jwt.util decode, interceptor, guards.
3. features/auth/login: form email/password, mapear 401 y 403 E_TENANT_NO_ACTIVO.
4. features/plataforma: list, create tenant, create admin, patch estado.
5. Routes: /login public; /plataforma/** auth+SYSADMIN; /home placeholder.
6. proxy.conf.json /api → http://localhost:8080.
7. Verificar ng build + mvn test (incl. ModularityTests).
```

### 1.5 Stop condition

```text
Detente cuando: (a) login SysAdmin seed funciona y guarda JWT en sessionStorage,
(b) GET /tenants lista en la consola, (c) wizard crea tenant + admin en dos
pasos, (d) PATCH estado actualiza y el admin queda bloqueado al suspender
(BR-014), (e) ng build y mvn test en verde. No implementes CRUD usuarios ni
tenant demo.
```

### 1.6 Output

```text
Formato: codigo fuente real en frontend/ y delta en backend/ (no markdown).
Extracto esperado:
frontend/src/app/core/auth/auth.service.ts
frontend/src/app/features/auth/login/login.page.ts
frontend/src/app/features/plataforma/tenants-list.page.ts
backend/.../TenantController.java (+ GET)
```

## 2. Invariantes del prompt

- El JWT **debe** persistirse solo en `sessionStorage` (`DD-UC-004` §2/§3).
- La alta de tenant y admin **deben** ser dos pasos / dos llamadas REST.
- `GET /tenants` **debe** requerir rol `SYSADMIN`.
- El prompt **no debe** implementar CRUD usuarios (`DD-UC-005`) ni tenant demo.
- `ModularityTests` y `ng build` **deben** quedar en verde.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_JWT_LOCALSTORAGE` | Se usó `localStorage` en vez de `sessionStorage` | Rechazar; usar `sessionStorage` |
| `E_ALTA_COMBINADA` | Un solo formulario/endpoint combina tenant+admin | Separar en dos pasos (`DD-UC-003`/`DD-UC-004`) |
| `E_SIN_LISTA_TENANTS` | No se implementó `GET /tenants` | Añadir endpoint + página lista |
| `E_ALCANCE_EXCEDIDO` | Se implementó CRUD usuarios o tenant demo | Revertir; corresponde a DD posteriores |

## 4. Guardrails

- MUST: JWT solo en `sessionStorage`.
- MUST: `GET /api/v1/plataforma/tenants` con `@PreAuthorize("hasRole('SYSADMIN')")`.
- MUST: mantener dos endpoints REST separados para tenant y admin.
- MUST: `ng build` + `mvn test` verdes antes de considerar el prompt completo.
- MUST NOT: modificar ningún archivo bajo `docs/baseline/**`.
- MUST NOT: implementar `DD-UC-005` ni el diseño del tenant demo.
- MUST NOT: hardcodear secretos, contraseñas del seed ni el JWT secret en el frontend.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-004` | PR-IMPL-004 | `dev-agent` | `frontend/src/app/**` + delta `GET /tenants` |
| FSD | `FSD-UC-021` (login UI) | PR-IMPL-004 | `dev-agent` | Login Angular |
| FSD | `FSD-UC-011` (UI) | PR-IMPL-004 | `dev-agent` | Consola SysAdmin tenants |
| ADR | `ADR-0008` / `ADR-0010` | PR-IMPL-004 | `dev-agent` | Angular 21 + RBAC SysAdmin |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-004` completo; APIs de `PR-IMPL-002`/`003` disponibles.
- **Output esperado**: login → lista → crear tenant → crear admin; `ng build` OK; `mvn test` OK.

### 6.2 Caso borde

- **Input**: tenant suspendido; admin intenta login.
- **Output esperado**: UI muestra mensaje por `403 E_TENANT_NO_ACTIVO`.

### 6.3 Caso adversarial

- **Input**: solicitud de usar `localStorage` o combinar alta tenant+admin.
- **Comportamiento esperado**: rechazo `E_JWT_LOCALSTORAGE` / `E_ALTA_COMBINADA`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `ng_build_pass`, `mvn_test_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 19/07/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-004.md` v1.0 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 19/07/2026 | aprobado (prompt) | Prompt aprobado; ejecución (código real en `frontend/` + delta `GET /tenants`) queda pendiente |
