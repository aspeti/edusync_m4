# PR-DTO-001 — Generación de DTOs por capa hexagonal para FSD-UC-001/003/005

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-DTO-001` |
| Título | DTOs, Commands y Domain Events por capa hexagonal — FSD-UC-001, FSD-UC-003, FSD-UC-005 |
| Artefacto origen | FSD + arquitectura hexagonal |
| ID origen | `FSD-UC-001`, `FSD-UC-003`, `FSD-UC-005` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 24/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Senior Backend Engineer especializado en arquitectura hexagonal (Ports & Adapters)
y Domain-Driven Design sobre Java 21 + Spring Boot 3.3. Conoce en profundidad
el modelo de dominio de EduSync (SaaS B2B multitenant Bolivia, PostgreSQL 15
RLS, RBAC con roles DIRECTOR / SECRETARIA / DOCENTE).
```

### 1.2 Task
```text
Generar los DTOs de entrada (Command/Request) y salida (Response) para los 3
casos de uso criticos de EduSync, diferenciando estrictamente las capas
hexagonales: infrastructure/web (API), application (comando de caso de uso)
y domain (eventos de dominio publicados). Por cada UC producir:
  1. Request DTO  -- infrastructure/adapter/in/web/dto/  (Java Record, Spring)
  2. Command      -- application/<uc>/                    (Java Record puro, sin Spring)
  3. Response DTO -- infrastructure/adapter/in/web/dto/  (Java Record, Spring)
  4. Domain Event -- domain/model/<contexto>/event/      (Java Record puro)
  5. Tabla de mapeo DTO <-> Entidad de dominio con la BR que valida cada campo
```

### 1.3 Context
```text
- UCs objetivo: FSD-UC-001 (registro calificacion), FSD-UC-003 (consolidacion),
  FSD-UC-005 (autorizacion correccion retroactiva). Fuente: docs/fsd/FSD_EduSync.md.
- Estructura de paquetes hexagonal: docs/arquitectura_hexagonal_EduSync.md §1.1.
- Convenciones de codigo: docs/AGENTS.md §5 (Java 21, Records, ingles, Bean Validation).
- BRs activas: BR-001 (RBAC), BR-002 (rango), BR-003 (floor), BR-004 (RUDE),
  BR-005 (append-only), BR-007 (parametros inmutables), BR-008 (calculo en dominio),
  BR-009 (ventana 1-72h), BR-010 (audit en TX), BR-011 (anual con 3 cerrados).
- DAs aplicables: DA-01 (RLS), DA-02 (aislamiento dominio), DA-03 (audit_log).
```

### 1.4 Reasoning
```text
1. Por cada UC, derivar el Request DTO del "Datos de entrada" del FSD,
   anadiendo anotaciones Jakarta que reflejen la BR correspondiente.
2. Derivar el Command del Request DTO, eliminando dependencia Spring/Jakarta
   y anadiendo tenantId (SecurityContext) y actorId (JWT claim).
3. Derivar el Response DTO del "Datos de salida" del FSD, con camelCase
   ingles y tipos Java precisos (UUID, BigDecimal, Instant).
4. Definir el Domain Event como Record inmutable con campos minimos que
   consumen los listeners (sin PII innecesaria).
5. Construir la tabla DTO <-> Entidad con: campo DTO, campo entidad, BR
   que lo valida, capa de validacion (Jakarta vs Domain Service).
```

### 1.5 Stop condition
```text
Detente cuando esten completos para los 3 UCs:
- 4 Request DTOs (Java Records con Bean Validation)
- 4 Commands (Java Records sin Spring)
- 3 Response DTOs (Java Records)
- 5 Domain Events
- 3 tablas de mapeo DTO <-> Entidad
```

### 1.6 Output
```text
docs/dtos_EduSync.md v0.1 con: frontmatter, §0 proposito, §1-§3 codigo Java
por UC, §4 verificacion contra invariantes hexagonales, §5 inventario
consolidado, §6 checklist dev-agent, §7 trazabilidad, §8 registro de cambios.
```

## 2. Invariantes del prompt

- `domain/` Records (Commands y Events) sin `imports de org.springframework.*` ni `jakarta.*` (DA-02).
- El campo `rude` NUNCA aparece en `@PathVariable` ni `@RequestParam`; solo en el body (BR-004).
- `valor` en `CalificacionRequestDTO` lleva `@DecimalMin("0")` y `@Digits`.
- Los Response DTOs NO exponen `tenant_id` ni `actor_id` al cliente.
- `promedioAnual` en `CentralizadorResponseDTO` es `Integer nullable` (null = "EN CURSO").

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_DTO_CON_ENTIDAD_JPA` | Record extiende o referencia una `@Entity` | Rechazar |
| `E_RUDE_EN_PATH` | `rude` en `@PathVariable` o `@RequestParam` | Mover al body |
| `E_CALCULO_EN_DTO` | El DTO realiza floor/promedio | Mover al Domain Service |
| `E_CAMPO_SIN_BR` | Campo de negocio sin anotación ni BR documentada | Completar |

## 4. Guardrails

- MUST: validar que los Commands no tienen dependencias de Spring/Jakarta.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia`.
- MUST NOT: exponer `rude` fuera del body de la petición HTTP.
- MUST NOT: realizar cálculos de dominio en ningún DTO.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-001`, `FSD-UC-003`, `FSD-UC-005` | PR-DTO-001 | `dev-agent` | `docs/dtos_EduSync.md` v0.1 |
| Arquitectura hexagonal | `docs/arquitectura_hexagonal_EduSync.md` | PR-DTO-001 | `dev-agent` | Estructura de paquetes de los DTOs |
| BRD | `BR-001..BR-011` | PR-DTO-001 | `dev-agent` | Tabla de mapeo DTO ↔ Entidad con BRs |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: FSD-UC-001 con campos de entrada: `rude`, `materiaId`, `periodoId`, `dimension`, `valor`.
- **Output esperado**: `CalificacionRequestDTO` con `@NotBlank rude`, `@DecimalMin("0") valor`; `RegistrarCalificacionCommand` sin Jakarta; `CalificacionRegistradaEvent` con `occurredAt: Instant`.

### 6.2 Caso borde
- **Input**: UC-003 consolidación, que no tiene Request DTO directo (se dispara por evento).
- **Output esperado**: solo `ConsolidarCentralizadorCommand` con `cursoId`, `periodoId`, `modo`; sin Request DTO de HTTP.

### 6.3 Caso adversarial
- **Input**: propuesta de incluir lógica `floor()` en el `CentralizadorResponseDTO`.
- **Comportamiento esperado**: rechazo con `E_CALCULO_EN_DTO`; `floor` se mantiene en `ConsolidacionDomainService`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 24/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
