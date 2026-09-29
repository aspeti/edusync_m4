# Tabla de auditoría E2E — EduSync (obligatorio entregable)

Checklist de 5 preguntas: (1) localizador persona (2) sin espera fija (3) no redacción LLM (4) pequeño/independiente (5) datos propios.

## Tests generados / auditados (agente)

| Caso plan | Archivo | Veredicto | Pregunta falló | Qué se cambió |
|-----------|---------|-----------|----------------|---------------|
| Auth 1.1 SYSADMIN | `caso_1_1.spec.ts` | corregido | — (compilación) | Se agregó `import { sysadminCreds }` |
| Auth 1.1 | `caso_1_1.auditado.spec.ts` | aceptado | — | Referencia limpia con helper |
| Auth 1.2 inválidas | `caso_1_2.spec.ts` | aceptado | — | — |
| Admin 1.1 | `caso_admin_1_1.auditado.spec.ts` | aceptado | — | — |
| Admin 3.1 Curso | `caso_admin_3_1.auditado.spec.ts` | aceptado | — | — |
| Auth 2.1 reset UI | `caso_auth_2_1.auditado.spec.ts` | aceptado | — | Labels `for`/`id` en pantalla |
| Plat 1.1 form Tenant | `caso_plat_1_1.auditado.spec.ts` | aceptado | — | Labels en tenant-create |
| Plat 1.2 crear Tenant | (mismo archivo) | aceptado | — | Datos con `Date.now()` |
| Ges 1.1 alta gestión | `caso_ges_1_1.auditado.spec.ts` | aceptado | — | Labels en gestion-create |
| Ges 1.2 Periodos | (mismo) | aceptado | — | — |
| Ges 1.3 Secciones | (mismo) | aceptado | — | — |
| Cur 1.1 Paralelos | `caso_cur_mat.auditado.spec.ts` | aceptado | — | — |
| Mat 1.1 alta Materia | (mismo) | aceptado | — | Labels en materia-create; assert detalle |

## Totales Planner (planes en `specs/`)

| Plan | Propuestos | Aceptados | Corregidos | Descartados |
|------|------------|-----------|------------|-------------|
| `plan_auth.md` | 6 | 5 | 0 | 1 |
| `plan_plataforma.md` | 4 | 2 | 0 | 2 |
| `plan_edusync_admin.md` | 8 | 7 | 0 | 1 |
| `plan_gestion.md` | 4 | 3 | 1 | 0 |
| `plan_cursos_materias.md` | 4 | 3 | 0 | 1 |
| **Total** | **26** | **20** | **1** | **5** |

## Totales Generator / agente

| Métrica | Valor |
|---------|-------|
| Specs agente | 9 archivos |
| Tests agente (aprox.) | 13 |
| Aceptados | 12 |
| Corregidos | 1 (`caso_1_1` import) |
| Descartados post-generación | 0 |

## Anclas agregadas al producto

| Ancla | Pantalla |
|-------|----------|
| `role="alert"` + `data-testid="login-error"` | Login |
| `data-testid="tenants-heading"` | Lista Tenants |
| `data-testid="usuarios-heading"` | Lista Usuarios |
| `data-testid="gestiones-heading"` | Gestión Escolar |
| `data-testid="cursos-heading"` | Cursos |
| `data-testid="materias-heading"` | Materias |
| `data-testid="estudiantes-heading"` | Estudiantes |
| `data-testid="profesores-heading"` | Profesores |
| `label for` + `id` en forms | Curso, Estudiante, Usuario, Tenant, Gestión, Materia, Reset password |

## Tokens (REGISTRO_TOKENS.csv)

| Ronda | Entrada | Salida | Notas |
|-------|---------|--------|-------|
| planner-openwebui-timeout | 0 | 0 | Timeout 365 s |
| planner-* (5 secciones) | 0 | 0 | Planes auditados a mano tras fallo LLM |
| generator-1.1 | 1235 | 147 | Ollama; corregido |
| generator-1.2 | 1235 | 182 | Ollama; aceptado |
| generator-*-auditado (7 rondas) | 0 | 0 | Patrón Generator + 5Q; sin LLM |
| **Promedio Generator LLM** | **1235** | **165** | Solo 1.1 y 1.2 |

Ver filas completas en `REGISTRO_TOKENS.csv`.

## Lo que no se pudo probar en E2E

- Redacción / calidad del LLM (`/api/v1/ai/chat`) → **evals**
- `floor()` SIE / `round` HALF_UP motor → **unit + golden**
- RLS cross-tenant exhaustivo → **integration Testcontainers**
- Flujos SECRETARIA / PROFESOR (Mis materias, calificaciones) → sin credenciales seed E2E dedicadas
- Reset password end-to-end con token real → backend log-only (sin correo)
