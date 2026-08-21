# PR-IMPL-009 — Frontend: consola de Gestión Escolar

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-009` |
| Título | Generación de la consola Angular de Gestión Escolar |
| Artefacto origen | `docs/design/DD-UC-009.md` |
| ID origen | `DD-UC-009` (`FSD-UC-012`, cierre de UI) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 20/08/2026 |
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
Implementa la consola Angular de Gestion Escolar segun docs/design/DD-UC-009.md
§2: features/academico/ (lista con filtros q/estado + paginacion, alta,
cambio de estado restringido a las transiciones validas del estado actual);
ruta /academico/gestiones-escolares (+ /nuevo) protegida por
roleGuard(ADMIN); enlace de nav "Gestion Escolar" en shell.component.ts.
Sin delta de backend.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-009.md (patron sin design system de
  features/plataforma/; dialogo de estado calcula client-side las
  transiciones validas, a diferencia del dialogo generico de Tenant).
- Contratos ya existentes (DD-UC-008, sin cambios): GET/POST
  /gestiones-escolares (q, estado, page, size), PATCH
  /gestiones-escolares/{id}/estado.
- Precedentes de codigo a replicar: features/plataforma/tenants-list.page.ts
  (lista con filtro q + select de estado + paginacion PageResponse<T>),
  features/usuarios/usuario-create.page.ts (formulario de alta),
  core/api/page-response.model.ts (DD-UC-007).
- ADRs: ADR-0008 (Angular 21), ADR-0009 (GestionEscolar es una entidad de la
  generalizacion SaaS).
- Prerrequisito: PR-IMPL-001..008 ya ejecutados (backend de GestionEscolar
  completo).
- Restricciones: NO delta de backend; NO implementar pantallas de
  PeriodoEvaluacion/SeccionEvaluacion/Curso/Materia/Estudiante/Inscripcion
  (FSD-UC-013..020); NO insinuar en la UI la precondicion de
  periodos/secciones para ACTIVA (diferida en DD-UC-008 §2, el backend no la
  valida).
```

### 1.4 Reasoning

```text
1. gestion-escolar.model.ts (GestionEscolarResponse, GestionEscolarFiltro).
2. gestiones-escolares-list.page.ts: GET /gestiones-escolares (q, estado,
   page, size) + funcion transicionesValidas(estadoActual) -- dialog de
   cambio de estado solo ofrece esas opciones; boton "Cambiar estado" oculto
   si estadoActual === 'CERRADA'.
3. gestion-escolar-create.page.ts: POST /gestiones-escolares (nombre,
   fechaInicio, fechaFin); mapear 422 E_FECHAS_INVALIDAS a mensaje claro.
4. app.routes.ts: /academico/gestiones-escolares[, /nuevo] (roleGuard ADMIN).
5. shell.component.ts: enlace "Gestion Escolar" condicional a
   auth.hasRole('ADMIN').
6. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando: (a) Admin autenticado ve /academico/gestiones-escolares y
puede crear/listar/filtrar/paginar/cambiar estado, (b) el dialogo de cambio
de estado solo ofrece las transiciones validas del estado actual, (c) el
boton de cambio de estado no aparece sobre una gestion CERRADA, (d) ng build
en verde. No implementes delta de backend ni pantallas de FSD-UC-013..020.
```

### 1.6 Output

```text
Formato: codigo fuente real en frontend/ (no markdown).
Extracto esperado:
frontend/src/app/features/academico/gestion-escolar.model.ts
frontend/src/app/features/academico/gestiones-escolares-list.page.ts
frontend/src/app/features/academico/gestion-escolar-create.page.ts
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
```

## 2. Invariantes del prompt

- El diálogo de cambio de estado **nunca** ofrece una transición inválida para el estado actual de la `GestionEscolar`.
- Ninguna pantalla de este prompt implementa `FSD-UC-013`..`FSD-UC-020` ni valida la precondición de periodos/secciones diferida en `DD-UC-008`.
- `ng build` **debe** quedar en verde.
- Este prompt **no debe** modificar código backend.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TRANSICION_OFRECIDA_INVALIDA` | El diálogo de estado ofrece una transición que el dominio rechazaría | Corregir `transicionesValidas(estadoActual)` |
| `E_PRECONDICION_INVENTADA` | La UI bloquea o advierte sobre periodos/secciones antes de `ACTIVA` | Revertir; esa validación está explícitamente diferida (`DD-UC-008` §2) |
| `E_ALCANCE_EXCEDIDO` | Se implementó una pantalla de `FSD-UC-013`..`020` | Revertir; corresponde a un Design Doc de seguimiento |
| `E_DELTA_BACKEND` | El prompt modificó código bajo `backend/` | Rechazar; este prompt es frontend-only |

## 4. Guardrails

- MUST: el diálogo de cambio de estado solo ofrece transiciones válidas del estado actual.
- MUST: `ng build` en verde antes de considerar el prompt completo.
- MUST NOT: modificar ningún archivo bajo `backend/` ni `docs/baseline/**`.
- MUST NOT: implementar pantallas de `FSD-UC-013`..`FSD-UC-020`.
- MUST NOT: insinuar en la UI una precondición de periodos/secciones que el backend no valida.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-009` | PR-IMPL-009 | `dev-agent` | `frontend/src/app/features/academico/**` |
| FSD | `FSD-UC-012` (cierre UI) | PR-IMPL-009 | `dev-agent` | Consola de Gestión Escolar |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-009` completo; backend de `DD-UC-008` disponible.
- **Output esperado**: Admin crea una Gestión Escolar, la lista con filtros/paginación, y la transiciona `PLANIFICACION → ACTIVA → CERRADA`; `ng build` en verde.
- **Resultado real (20/08/2026)**: `ng build` → verde, 2 lazy chunks nuevos (`gestiones-escolares-list-page`, `gestion-escolar-create-page`).

### 6.2 Caso borde

- **Input**: `GestionEscolar` en estado `CERRADA`.
- **Output esperado**: el botón "Cambiar estado" no aparece (no hay transiciones válidas desde `CERRADA`).

### 6.3 Caso adversarial

- **Input**: solicitud de bloquear la creación o activación mientras no existan periodos/secciones configurados.
- **Comportamiento esperado**: rechazo — esa validación está explícitamente diferida (`DD-UC-008` §2/§3); no implementarla sin un Design Doc de seguimiento.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 20/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-009.md` v1.0. Primer prompt de UI del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 20/08/2026 | Rodrigo Aspeti | Estado → **Ejecutado**: consola Angular real (lista con filtros `q`/`estado` y paginación, alta, diálogo de cambio de estado con `transicionesValidas(estadoActual)`); `ng build` en verde. Delta menor no listado explícitamente en el diseño original: `shell.component.ts` gana el enlace "Gestión Escolar" (mismo condicional `hasRole('ADMIN')` que "Usuarios"). Sincronizado con `DTP` v1.18 / `PROMPT_MAPPING` v2.16 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 20/08/2026 | **aprobado (diseño)** | Prompt listo para ejecutar; código real todavía no generado |
| Rodrigo Aspeti | 20/08/2026 | **ejecutado** | Código en working tree; `ng build` verde; docs sincronizados vía `dtp-sync`; commit formal pendiente |
