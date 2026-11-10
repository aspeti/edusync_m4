# API guidelines — REST EduSync

## Style

- Base path: `/api/v1/...` (e.g. `/api/v1/academico/...`, `/api/v1/plataforma/tenants`, `/api/v1/identidad` login/usuarios).
- Resource-oriented JSON; pagination via `page`/`size` → `PageResponse<T>`.
- Auth: `Authorization: Bearer <JWT>` except documented public endpoints (login, password confirm).
- Docs: springdoc → `/v3/api-docs`, Swagger UI (open in current non-prod profiles).

## Contracts

- Request DTOs: Jakarta validation annotations; never accept raw domain objects.
- Response DTOs: map in controller/assembler layer; stable field names matching Angular models.
- Errors: HTTP status + body `ErrorResponse` with business `code` (`E_RANGO_INVALIDO`, `E_PERIODO_NO_ABIERTO`, `E_CURSO_NO_ENCONTRADO`, …).
- Idempotency: critical for future SIE export (`rude+materia_id+periodo_id`).

## RBAC (examples in code)

| Area | Typical roles |
|------|----------------|
| Tenants | `SYSADMIN` |
| Users | `ADMIN` |
| Gestión / periodos / secciones | `ADMIN` |
| Materias / estudiantes / profesores (read-write as designed) | `ADMIN`, often `SECRETARIA` |
| Evaluaciones / calificaciones | `PROFESOR`, `ADMIN` |

Align with the specific controller’s `@PreAuthorize` — do not invent broader access.

## Versioning

- Stay on `/api/v1` unless an ADR introduces breaking version strategy.
- Additive fields preferred over breaking renames; sync FSD + Angular models in the same change set when possible.
