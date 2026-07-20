# Aportes Individuales — `release/3.0.0` EduSync

> Inventario auditable de la **capa viva de implementación** (apertura post-M4).
> Estructura literal de `plantillas/APORTES_TEMPLATE.md` (6 secciones).
> **Caso degenerado n = 1**: el grupo G-EduSync es unipersonal, por lo que el factor de aporte individual colapsa matemáticamente a `1.00`. Este archivo funciona como **inventario auditable** del trabajo individual, no como ajuste de nota relativo.
>
> Complementa (no reemplaza) `docs/aportes/release-2.0.0.md` (defensa M4 / tag histórico). Aquí solo se contabiliza trabajo de `release/3.0.0` (28/05/2026 → 19/07/2026).
>
> **Estado verificado contra el repo (19/07/2026, HEAD `7fb1085`)**: `PR-IMPL-001`/`002`/`003` **ejecutados**; `DD-UC-004` + `PR-IMPL-004` **aprobados, ejecución pendiente** (sin UI Angular ni `GET /tenants` en código fuente todavía).

---

## 0. Metadatos

| Campo | Valor |
|-------|-------|
| Producto | EduSync |
| Grupo | G-EduSync |
| Release evaluable | `release/3.0.0` |
| Sesión asociada | Capa viva (post-S12 / implementación) |
| Fecha de cierre | 19/07/2026 |
| Integrantes del grupo (n) | Rodrigo Aspeti (n = 1) |
| Branch del release | `release/3.0.0` |
| Commit de cierre (HEAD) | `7fb1085` |

---

## 1. Tabla de tareas atribuidas

> Una fila por tarea concreta producida; cada referencia es verificable contra archivo+sección o ruta del repo. Orden cronológico ascendente.

| # | Integrante | Tarea concreta | Categoría | Referencia | Fecha |
|---|------------|----------------|-----------|------------|-------|
| 1 | Rodrigo Aspeti | ADR-0008 — Stack vivo Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS para `release/3.0.0` | ADR | `docs/adr/0008-actualizacion-stack-java25-springboot4-angular21.md` | 28/05 |
| 2 | Rodrigo Aspeti | Apertura de la capa viva — `docs/product/{BRD,PRD,FSD}.md` con banner "COPIA VIVA" editables | Bitácora | `docs/product/BRD.md`, `PRD.md`, `FSD.md` | 28/05 |
| 3 | Rodrigo Aspeti | DTP v1.0 — Documento Técnico del Producto (continuación viva del DTI congelado) | AGENTS | `docs/product/DTP.md` v1.0 | 28/05 |
| 4 | Rodrigo Aspeti | Freeze de baseline M4 — `docs/baseline/**` marcado `status: congelado` + `CODEOWNERS` | Rule | `docs/baseline/`, `CODEOWNERS` | 28/05 |
| 5 | Rodrigo Aspeti | Rule `baseline-congelado` — prohibición de editar `docs/baseline/**` (Cursor + espejo Claude) | Rule | `.cursor/rules/baseline-congelado.mdc`, `.claude/rules/baseline-congelado.md` | 28/05 |
| 6 | Rodrigo Aspeti | Skill `feature-design-doc` — generación de `DD-UC-NNN` + `PR-IMPL-NNN` | Skill | `.cursor/skills/feature-design-doc/SKILL.md` | 28/05 |
| 7 | Rodrigo Aspeti | Skill `dtp-sync` — sincronización de implementación hacia `docs/product/DTP.md` | Skill | `.cursor/skills/dtp-sync/SKILL.md` | 28/05 |
| 8 | Rodrigo Aspeti | PROMPT_MAPPING v2.0 — área `IMPL` reservada para prompts de implementación | Prompt | `docs/PROMPT_MAPPING.md` historial v2.0 | 28/05 |
| 9 | Rodrigo Aspeti | Roadmap v0.3 — reconciliación `release/1.1.0` ≈ `release/3.0.0` + hito Design Doc | AGENTS | `docs/roadmap.md` v0.3 | 28/05 |
| 10 | Rodrigo Aspeti | AGENTS.md v0.12 — dualidad de stack, guardrails de baseline, skills de capa viva | AGENTS | `AGENTS.md` §15 historial v0.12 | 28/05 |
| 11 | Rodrigo Aspeti | Hooks Cursor — `protect-baseline`, `warn-shell-baseline`, `dtp-sync-reminder` | Código | `.cursor/hooks.json`, `.cursor/hooks/*.js` | 28/05 |
| 12 | Rodrigo Aspeti | ADR-0009 — Generalización del modelo a plataforma SaaS multi-tenant configurable | ADR | `docs/adr/0009-generalizacion-modelo-dominio-multitenant-configurable.md` | 12/07 |
| 13 | Rodrigo Aspeti | BRD vivo v3.0 — extensión aditiva BR-013..BR-024 + persona SysAdmin | BRD | `docs/product/BRD.md` v3.0 | 12/07 |
| 14 | Rodrigo Aspeti | PRD vivo v2.0 — épicas E7..E11 + PRD-US-018..030 + PRD-REQ-021..031 | PRD | `docs/product/PRD.md` v2.0 | 12/07 |
| 15 | Rodrigo Aspeti | FSD vivo v2.0 — actores ampliados + modelo ER genérico §6.3 | FSD | `docs/product/FSD.md` v2.0 | 12/07 |
| 16 | Rodrigo Aspeti | FSD-UC-011 — Gestión de Tenants y Suscripciones (flujo + Gherkin) | UC | `docs/product/FSD.md` §4.6.1 | 12/07 |
| 17 | Rodrigo Aspeti | FSD-UC-012 — Gestión Escolar | UC | `docs/product/FSD.md` §4.6.2 | 12/07 |
| 18 | Rodrigo Aspeti | FSD-UC-013 — Configuración de Periodos de Evaluación | UC | `docs/product/FSD.md` §4.6.3 | 12/07 |
| 19 | Rodrigo Aspeti | FSD-UC-014 — Configuración de Secciones de Evaluación | UC | `docs/product/FSD.md` §4.6.4 | 12/07 |
| 20 | Rodrigo Aspeti | FSD-UC-015 — Gestión de Evaluaciones y Tipos de Evaluación | UC | `docs/product/FSD.md` §4.6.5 | 12/07 |
| 21 | Rodrigo Aspeti | FSD-UC-016 — Cálculo de Notas configurable | UC | `docs/product/FSD.md` §4.6.6 | 12/07 |
| 22 | Rodrigo Aspeti | FSD-UC-017 — Gestión de Cursos y Paralelos | UC | `docs/product/FSD.md` §4.6.7 | 12/07 |
| 23 | Rodrigo Aspeti | FSD-UC-018 — Gestión de Materias | UC | `docs/product/FSD.md` §4.6.8 | 12/07 |
| 24 | Rodrigo Aspeti | FSD-UC-019 — Gestión de Profesores | UC | `docs/product/FSD.md` §4.6.9 | 12/07 |
| 25 | Rodrigo Aspeti | FSD-UC-020 — Gestión de Estudiantes e Inscripciones | UC | `docs/product/FSD.md` §4.6.10 | 12/07 |
| 26 | Rodrigo Aspeti | FSD-UC-021 — Gestión de Usuarios y Roles (login + multi-rol) | UC | `docs/product/FSD.md` §4.6.11 | 12/07 |
| 27 | Rodrigo Aspeti | DTP v1.1 — delta `ADR-0009` en §A.2 + estado `FSD-UC-011`..`021` en §A.3 | Bitácora | `docs/product/DTP.md` changelog v1.1 | 12/07 |
| 28 | Rodrigo Aspeti | ADR-0010 — Modelo multi-rol (`UsuarioRol` N:M) + invariante SysAdmin sin tenant | ADR | `docs/adr/0010-modelo-multirol-usuario-y-sysadmin-sin-tenant.md` | 14/07 |
| 29 | Rodrigo Aspeti | BRD vivo v3.1 — reescritura de BR-024 (multi-rol + invariante permanente) | BRD | `docs/product/BRD.md` v3.1 | 14/07 |
| 30 | Rodrigo Aspeti | PRD vivo v2.2 — `PRD-REQ-031` / `PRD-US-029` + escenario Gherkin multi-rol | PRD | `docs/product/PRD.md` v2.2 | 14/07 |
| 31 | Rodrigo Aspeti | FSD vivo v2.2 — ER con `USUARIO_ROL` + endpoint `roles: [...]` en FSD-UC-021 | FSD | `docs/product/FSD.md` v2.2 | 14/07 |
| 32 | Rodrigo Aspeti | DTP v1.2 — delta `ADR-0010` registrado en §A.2 | Bitácora | `docs/product/DTP.md` changelog v1.2 | 14/07 |
| 33 | Rodrigo Aspeti | Design Doc `DD-UC-001` — bootstrap del proyecto (monolito modular + paquete `com.edusync`) | Otro | `docs/design/DD-UC-001.md` | 14/07 |
| 34 | Rodrigo Aspeti | ADR-0011 — Monolito modular Spring Modulith (module-first) + paquete base `com.edusync` | ADR | `docs/adr/0011-monolito-modular-spring-modulith-package-base.md` | 14/07 |
| 35 | Rodrigo Aspeti | PR-IMPL-001 — Prompt-contrato de bootstrap del esqueleto backend/frontend/infra | Prompt | `docs/prompts/impl/PR-IMPL-001.md` | 14/07 |
| 36 | Rodrigo Aspeti | Design Doc `DD-UC-002` — módulo `identidad` (Usuario/UsuarioRol, login JWT, seed SYSADMIN) | Otro | `docs/design/DD-UC-002.md` | 14/07 |
| 37 | Rodrigo Aspeti | PR-IMPL-002 — Prompt-contrato del módulo identidad | Prompt | `docs/prompts/impl/PR-IMPL-002.md` | 14/07 |
| 38 | Rodrigo Aspeti | Design Doc `DD-UC-003` — módulo `plataforma` (Tenant, suscripción, scheduler, TenantConsultaPort) | Otro | `docs/design/DD-UC-003.md` | 14/07 |
| 39 | Rodrigo Aspeti | PR-IMPL-003 — Prompt-contrato del módulo plataforma / FSD-UC-011 | Prompt | `docs/prompts/impl/PR-IMPL-003.md` | 14/07 |
| 40 | Rodrigo Aspeti | Corrección de ruta del área `IMPL` → `docs/prompts/impl/` (única excepción a convención plana) | Bitácora | `PROMPT_MAPPING` v2.2, `AGENTS.md` v0.17 | 14/07 |
| 41 | Rodrigo Aspeti | Ejecución `PR-IMPL-001` — esqueleto Maven/Spring Boot 4.1 + Angular 21 + docker-compose | Código | `backend/`, `frontend/`, `infra/docker-compose.yml` / commit `4113123` | 18/07 |
| 42 | Rodrigo Aspeti | Módulos Spring Modulith vacíos (`plataforma`/`identidad`/`academico`/`notassie`/`shared`) | Código | `backend/src/main/java/com/edusync/**/package-info.java` | 18/07 |
| 43 | Rodrigo Aspeti | `ModularityTests` — verificación de módulos sin ciclos (`ApplicationModules.verify`) | Test | `backend/src/test/java/.../ModularityTests.java` | 18/07 |
| 44 | Rodrigo Aspeti | Flyway baseline `V1__init.sql` + perfiles `application{,-dev,-test}.yml` | Código | `backend/src/main/resources/` | 18/07 |
| 45 | Rodrigo Aspeti | Ejecución `PR-IMPL-002` — dominio `Usuario`/`UsuarioRol` + factory con invariantes ADR-0010 | Código | `backend/.../identidad/domain/Usuario.java` | 18-19/07 |
| 46 | Rodrigo Aspeti | Login JWT — `AutenticarUsuarioService`, `JwtTokenProvider`, `AuthController`, filtro Bearer | Código | `backend/.../identidad/**` / commit `a550bdd` | 18-19/07 |
| 47 | Rodrigo Aspeti | `TenantContextProvider` real + seed `SYSADMIN` (cierra placeholder de ADR-0001) | Código | `shared/tenant/`, `identidad/infrastructure/SysAdminSeeder.java` | 18-19/07 |
| 48 | Rodrigo Aspeti | Puerto público `UsuarioCreacionPort` (contrato inter-módulo para alta de ADMIN) | Código | `identidad/UsuarioCreacionPort.java` | 18-19/07 |
| 49 | Rodrigo Aspeti | Suite de tests del módulo identidad (dominio, servicios, JWT, integración Auth) | Test | `backend/src/test/java/com/edusync/identidad/**` | 18-19/07 |
| 50 | Rodrigo Aspeti | ADR-0012 — Lombok (allowlist `domain/`), springdoc-openapi 3.0.3, Bean Validation | ADR | `docs/adr/0012-lombok-openapi-validation-productividad-backend.md` | 19/07 |
| 51 | Rodrigo Aspeti | Aplicación ADR-0012 — `OpenApiConfig`, `GlobalExceptionHandler`, `@Valid` en DTOs REST | Código | `shared/web/**`, commit `0a53581` | 19/07 |
| 52 | Rodrigo Aspeti | Ejecución `PR-IMPL-003` — Aggregate Root `Tenant` + ciclo ACTIVO/SUSPENDIDO/VENCIDO | Código | `backend/.../plataforma/domain/Tenant.java` / commit `32b1d53` | 19/07 |
| 53 | Rodrigo Aspeti | Casos de uso plataforma — Registrar / CambiarEstado / CrearAdmin Tenant + REST RBAC SYSADMIN | Código | `plataforma/application/**`, `TenantController.java` | 19/07 |
| 54 | Rodrigo Aspeti | Scheduler de vencimiento `@Scheduled` + `VencimientoSchedulerService`/`Job` | Código | `plataforma/.../VencimientoScheduler*.java` | 19/07 |
| 55 | Rodrigo Aspeti | `TenantConsultaPort` en `identidad` + enforcement BR-014 (`E_TENANT_NO_ACTIVO`) | Código | `identidad/TenantConsultaPort.java`, `AutenticarUsuarioService` | 19/07 |
| 56 | Rodrigo Aspeti | Migración Flyway `V3__plataforma_tenant.sql` + adaptador JPA de Tenant | Código | `db/migration/`, `TenantRepositoryAdapter` | 19/07 |
| 57 | Rodrigo Aspeti | Suite de tests del módulo plataforma (dominio, servicios, scheduler, integración) | Test | `backend/src/test/java/com/edusync/plataforma/**` | 19/07 |
| 58 | Rodrigo Aspeti | Cierre API de `FSD-UC-011` — primer FSD-UC con implementación backend completa (45/45 tests) | Bitácora | `docs/product/DTP.md` §A.3 + `DD-UC-003` | 19/07 |
| 59 | Rodrigo Aspeti | Design Doc `DD-UC-004` — UI login + consola SysAdmin (`sessionStorage`, wizard 2 pasos) | Otro | `docs/design/DD-UC-004.md` | 19/07 |
| 60 | Rodrigo Aspeti | PR-IMPL-004 — Prompt-contrato de UI Angular + delta `GET /tenants` (**ejecución pendiente**) | Prompt | `docs/prompts/impl/PR-IMPL-004.md` | 19/07 |
| 61 | Rodrigo Aspeti | DTP v1.7..v1.10 — sync incremental post PR-IMPL-001..003 y creación de DD/PR-IMPL-004 | Bitácora | `docs/product/DTP.md` changelog v1.7–v1.10 | 18-19/07 |
| 62 | Rodrigo Aspeti | PROMPT_MAPPING v2.1..v2.7 — registro PR-IMPL-001..004 (001–003 ejecutados; 004 aprobado) | Bitácora | `docs/PROMPT_MAPPING.md` historial v2.1–v2.7 | 14-19/07 |
| 63 | Rodrigo Aspeti | FSD vivo v2.3→v2.4 — referencias DD-UC-003/004 + paso GET previsto en flujo SysAdmin | FSD | `docs/product/FSD.md` v2.4 | 14-19/07 |
| 64 | Rodrigo Aspeti | Paridad Cursor ↔ Claude Code — 13 skills espejo + entrada `CLAUDE.md` | Skill | `.claude/skills/**`, `CLAUDE.md` / commit `7fb1085` | 19/07 |
| 65 | Rodrigo Aspeti | Subagentes Claude — `dev`/`docs`/`arch`/`qa`/`process`/`compliance-agent` | Otro | `.claude/agents/*.md` | 19/07 |
| 66 | Rodrigo Aspeti | Skill `async-architecture-reviewer` — auditoría §7 arquitectura asíncrona | Skill | `.cursor/skills/async-architecture-reviewer/SKILL.md` | 19/07 |
| 67 | Rodrigo Aspeti | Skill `monolith-decomposition-architect` — propuesta de descomposición / seams | Skill | `.cursor/skills/monolith-decomposition-architect/SKILL.md` | 19/07 |
| 68 | Rodrigo Aspeti | Skill `distributed-architecture-reviewer-edusync` — auditoría §6 (slug EduSync) | Skill | `.cursor/skills/distributed-architecture-reviewer-edusync/SKILL.md` | 19/07 |
| 69 | Rodrigo Aspeti | AGENTS.md v0.20..v0.24 — sync con ejecuciones PR-IMPL-001..003 y paridad Claude | AGENTS | `AGENTS.md` §15 historial v0.20–v0.24 | 18-19/07 |
| 70 | Rodrigo Aspeti | Informe de aportes `release/3.0.0` (este archivo) | Bitácora | `docs/aportes/release-3.0.0.md` | 19/07 |

---

## 2. Resumen por integrante

| Integrante | Total de tareas | Categorías cubiertas (#) | Observación |
|------------|-----------------|--------------------------|--------------|
| Rodrigo Aspeti | 70 | 13 (`ADR`, `AGENTS`, `BRD`, `PRD`, `FSD`, `UC`, `Skill`, `Rule`, `Prompt`, `Código`, `Test`, `Bitácora`, `Otro`) | Único integrante; capa viva → ADRs 0008..0012 → specs → Design Docs → PR-IMPL-001..003 ejecutados; PR-IMPL-004 pendiente de código |
| **Total grupo** | **70** | — | — |

> Distribución por categoría:
>
> | Categoría | Filas |
> |-----------|-------|
> | Código | 14 |
> | UC | 11 |
> | Bitácora | 8 |
> | Skill | 6 |
> | ADR | 5 |
> | Prompt | 5 |
> | Otro | 5 |
> | AGENTS | 4 |
> | Test | 3 |
> | FSD | 3 |
> | BRD | 2 |
> | PRD | 2 |
> | Rule | 2 |
> | **Total** | **70** |

---

## 3. Cálculo del factor de aporte individual

> **Caso degenerado n = 1**: el factor es trivialmente `1.00`; este archivo funciona como **inventario auditable** del trabajo individual, no como ajuste de nota relativo entre integrantes.

Fórmula del módulo (idéntica en los releases evaluables):

```
aporte_promedio_grupo = total_tareas_grupo / n_integrantes
factor_i              = clamp(tareas_i / aporte_promedio_grupo, 0.5, 1.1)
Nota_individual_i     = Nota_grupal × factor_i
```

Aplicación con `n = 1`:

- `aporte_promedio = 70 / 1 = 70.00`
- `factor_raw      = 70 / 70 = 1.00`
- `factor          = clamp(1.00, 0.5, 1.1) = 1.00`

### Aplicación

| Integrante | Tareas (de §2) | factor sin clamp = tareas / promedio | factor (clamp 0.5–1.1) |
|------------|----------------|--------------------------------------|------------------------|
| Rodrigo Aspeti | 70 | 70 / 70 = 1.00 | **1.00** |

> **Aporte promedio del grupo**: `70 / 1 = 70` tareas/persona.
> **Nota individual**: la columna se omite porque `nota_grupal = null` (el docente la asignará tras la auditoría).

### Ejemplo numérico (referencia)

Conservado de la plantilla para no perder el sentido pedagógico del instrumento:

Grupo de 4 integrantes, total 20 tareas, nota grupal = 80/100.

| Integrante | Tareas | Sin clamp | Con clamp | Nota individual |
|------------|--------|-----------|-----------|-----------------|
| Ana | 10 | 10/5 = 2.0 | 1.10 | 88 |
| Beto | 6 | 6/5 = 1.2 | 1.10 | 88 |
| Carla | 3 | 3/5 = 0.6 | 0.60 | 48 |
| Dani | 1 | 1/5 = 0.2 | 0.50 | 40 |

(Aporte promedio = 20/4 = 5 tareas.)

---

## 4. Reglas del grupo sobre qué cuenta como tarea

> Granularidad estándar recomendada por el módulo. El grupo puede afinar pero no relajar.

- **Un UC** (con flujo principal + alterno + Gherkin) = 1 tarea.
- **Un NFR ISO 25010** cuantificable con métrica + umbral + verificación = 1 tarea.
- **Un diagrama Mermaid** (`.mmd`) versionado y coherente con FSD = 1 tarea.
- **Una sección de un documento** del nivel `##` (BRD/MRD/PRD/FSD/DTI) con contenido sustantivo = 1 tarea.
- **Un ADR aceptado** = 1 tarea.
- **Una POC ejecutada con evidencia** = 1 tarea.
- **Un skill propio** (`docs/skills/<skill>.md`) accionable = 1 tarea.
- **Una cursor rule** (`.cursor/rules/<dominio>.mdc`) específica del dominio = 1 tarea.
- **Un prompt-contrato** con los 6 elementos + Invariants + Failure modes = 1 tarea.
- **Una user story** INVEST con criterios de aceptación = 1 tarea.
- **Una sección de bitácora** o **una sesión de demo** preparada y entregada = 1 tarea.
- **Una función o módulo no trivial de código** (con su prueba) = 1 tarea.
- **Co-autoría**: si dos personas hicieron la misma tarea de forma sustantiva, registrarla **dos veces** (una por autor) con la observación `co-autoría con <otro>`.

No cuentan: cambios cosméticos, correcciones tipográficas aisladas, commits de configuración sin contenido sustantivo, copiar/pegar de otra fuente sin adaptación.

> **Ajuste de categoría para este release**: los Design Docs (`DD-UC-NNN`) y los subagentes Claude no tienen etiqueta propia en la lista cerrada; se registran como `Otro`. Los bumps de DTP/PROMPT_MAPPING/AGENTS se agrupan como `Bitácora` cuando no son un documento canónico nuevo. Un prompt-contrato **aprobado pero no ejecutado** (p. ej. `PR-IMPL-004`) cuenta como tarea `Prompt`, no como `Código`.

---

## 5. Auditoría del docente (opcional)

> Espacio para que el docente registre observaciones, ajustes manuales o justificaciones aprobadas. Si está vacío, se aplica el cálculo automático de §3.

| Integrante | Factor calculado (§3) | Factor final aplicado | Justificación del ajuste |
|------------|-----------------------|------------------------|---------------------------|
| Rodrigo Aspeti | 1.00 | — | (a poblar por el docente) |

---

## 6. Checklist de cierre del release

- [x] §0 Metadatos completos con `n_integrantes` y branch del release.
- [x] §1 Cada tarea tiene Integrante, Categoría y Referencia verificable contra archivo+sección del repo.
- [x] §2 Suma de tareas por integrante = total del grupo (70 = 70).
- [x] §3 Aporte promedio y factor calculado para el único integrante (factor = 1.00 por caso degenerado n = 1).
- [x] §4 El grupo confirma que respetó la granularidad estándar (texto literal de `plantillas/APORTES_TEMPLATE.md §4` + nota de categorías `Otro`/`Bitácora`).
- [ ] Archivo commiteado en el branch del release `release/3.0.0` antes del cierre (pendiente del commit/push de este informe).

---

*Inventario de capa viva `release/3.0.0` | Agente: `docs-agent` | Fecha: 19/07/2026 | Verificado contra HEAD `7fb1085` | Trazabilidad: `docs/product/DTP.md` v1.10, `PROMPT_MAPPING` v2.7, `AGENTS.md` v0.24 | Complemento de `docs/aportes/release-2.0.0.md`.*
