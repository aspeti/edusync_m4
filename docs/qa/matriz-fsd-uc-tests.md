# Matriz FSD-UC ↔ tests (capa viva `release/3.0.0`)

Inventario de trazabilidad para auditoría de cobertura funcional.  
Actualizar cuando se agregue o cierre un `FSD-UC` / `PR-IMPL`.

**Leyenda de estado**

| Estado | Significado |
|--------|-------------|
| OK | Domain + application (+ integration o UI specs si aplica) |
| PARCIAL | Hay tests pero falta alguna capa |
| GAP | Sin tests dedicados o UC no implementado |
| N/A | Fuera del alcance vivo actual (SIE / `notassie`) |

## UCs SaaS genérico (implementados)

| FSD-UC | Feature | Domain tests | Application / service tests | Integration | Frontend specs | Estado |
|--------|---------|--------------|-----------------------------|-------------|----------------|--------|
| FSD-UC-011 | Tenants / suscripción | `TenantTest` | `RegistrarTenantServiceTest`, `CambiarEstadoTenantServiceTest`, `CrearAdminTenantServiceTest`, `ListarTenantsServiceTest`, `VencimientoSchedulerServiceTest` | `TenantIntegrationTest` | (consola SysAdmin — sin `*.spec` de feature) | PARCIAL (UI sin specs) |
| FSD-UC-012 | Gestión Escolar | `GestionEscolarTest` | `CrearGestionEscolarServiceTest`, `ListarGestionesEscolaresServiceTest`, `CambiarEstadoGestionEscolarServiceTest` | `GestionEscolarIntegrationTest` | — | PARCIAL (UI sin specs) |
| FSD-UC-013 | Periodos | `PeriodoEvaluacionTest` | `PeriodoEvaluacionServicesTest` | `PeriodoEvaluacionIntegrationTest` | — | PARCIAL |
| FSD-UC-014 | Secciones | `SeccionEvaluacionTest` | `SeccionEvaluacionServicesTest` | `SeccionEvaluacionIntegrationTest` | — | PARCIAL |
| FSD-UC-015 | Evaluaciones | `EvaluacionTest` | `EvaluacionServicesTest` | `EvaluacionIntegrationTest` | — | PARCIAL |
| FSD-UC-016 | Calificaciones / `CalculoNotas` | `CalificacionEvaluacionTest`, `CalculoNotasTest` | (vía integración + dominio) | `CalificacionEvaluacionIntegrationTest` | — | PARCIAL |
| FSD-UC-017 | Cursos / Paralelos | `CursoTest`, `ParaleloTest` | `CrearCursoServiceTest`, `ListarCursosServiceTest`, `CrearParaleloServiceTest`, `ListarParalelosServiceTest` | `CursoIntegrationTest` | — | PARCIAL |
| FSD-UC-018 | Materias | `MateriaTest`, `AsignacionMateriaCursoTest`, `AsignacionMateriaProfesorTest` | `CrearMateriaServiceTest`, `CrearAsignacionCursoServiceTest`, `CrearAsignacionProfesorServiceTest`, `ObtenerMateriaServiceTest` | `MateriaIntegrationTest` | — | PARCIAL |
| FSD-UC-019 | Profesores | — | `ListarProfesoresServiceTest`, `ObtenerProfesorServiceTest`, `ListarAsignacionesPorProfesorServiceTest` | `ProfesorIntegrationTest` | — | PARCIAL |
| FSD-UC-020 | Estudiantes / Inscripciones | `EstudianteTest`, `InscripcionTest` | `CrearEstudianteServiceTest`, `ObtenerEstudianteServiceTest`, `CrearInscripcionServiceTest` | `EstudianteIntegrationTest` | — | PARCIAL |
| FSD-UC-021 | Usuarios / Roles / login | `UsuarioTest`, `PasswordResetTokenTest` | `AutenticarUsuarioServiceTest`, `CrearUsuarioServiceTest`, `ActualizarRolesUsuarioServiceTest`, `CambiarEstadoUsuarioServiceTest`, `RestablecerPasswordServiceTest`, `ListarUsuariosServiceTest` | `AuthIntegrationTest`, `UsuarioIntegrationTest` | `auth.service.spec`, `auth.interceptor.spec`, `guards.spec`, `jwt.util.spec` | OK (backend + auth UI) |

## Transversal / arquitectura

| Tema | Tests | Estado |
|------|-------|--------|
| Spring Modulith (ADR-0011) | `ModularityTests` (7 ejecuciones) | OK |
| Paginación shared | `PageQueryTest`, `PageResultTest`, `PageResponseTest` | OK |
| Spike LLM (`shared.ai`) | `ChatConLlmServiceTest`, `ConsultarUsuarioServiceTest`, `ExtraerConsultaUsuarioServiceTest`, `LlmStructuredExtractorTest`, `OpenWebUiLlmAdapterTest`, `BuscarUsuarioPorNombrePortImplTest` | OK (fuera de FSD-UC de negocio) |
| JWT util | `JwtTokenProviderTest` | OK |

## Perfil Bolivia SIE (baseline / pendiente de código)

| FSD-UC | Golden / tests exigidos (`AGENTS.md` §8.3) | Estado |
|--------|--------------------------------------------|--------|
| FSD-UC-001..009 / consolidación / SIE | `FloorTest`, `SIEPayloadTest`, `VentanaTest`, `MultitenantTest` | GAP — módulo `notassie` vacío |

## Gaps priorizados (auditoría)

1. **Specs Angular de features** (`plataforma`, `usuarios`, `academico`): solo hay cobertura unitaria en `core/auth`.
2. **Golden tests SIE** aún no materializados.
3. **Service tests explícitos de `CalculoNotas` en application**: la lógica vive en domain + integration; valorar `CalculoNotasServiceTest` si aparece un use case dedicado.
4. **Pitest** limitado a `CalculoNotas`, `Usuario`, `GestionEscolar` — ampliar tras estabilizar el score.

## Cómo actualizar esta matriz

1. Tras un `PR-IMPL-NNN`, añadir filas/columnas en la tabla del UC.
2. Marcar PARCIAL → OK solo si existen al menos domain + application (+ integration para APIs mutadoras).
3. Ejecutar `mvn verify` y `npm run test:coverage` antes de cerrar el DoD del Design Doc.
