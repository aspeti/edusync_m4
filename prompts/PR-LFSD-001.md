# PR-LFSD-001 — Generación del LFSD EduSync (Low-Level Functional Specification)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-LFSD-001` |
| Título | Low-Level Functional Specification de EduSync v1.0 — 20 §§, 15 APIs, 14 DDL, 4 secuencias |
| Artefacto origen | FSD v1 + PRD v1 + BRD v2 + arquitectura funcional |
| ID origen | `FSD-UC-001..005`, `PRD-REQ-001..020`, `BR-001..BR-012`, `DA-01..DA-05` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 15/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un experto en Software Engineering, Solution Design, Low-Level Design y
documentación técnica detallada para sistemas empresariales Java/Spring Boot.
Tienes experiencia creando LFSD que trasladan especificaciones funcionales a
diseño de bajo nivel implementable, con arquitectura hexagonal, DDD y SOLID.
```

### 1.2 Task
```text
Genera docs/LFSD-EduSync.md traduciendo los requerimientos del FSD v1.0
a especificaciones técnicas de bajo nivel listas para implementación y QA.
```

### 1.3 Context
```text
- Insumo principal: FSD v1.0 (5 FSD-UC, 12 BR, 16 entidades, 16 NFRs).
- Stack: Java 21 LTS, Spring Boot 3.3, Spring Security 6 (JWT+RBAC),
  Spring Data JPA, PostgreSQL 15 (RLS), Angular 17, AWS.
- Invariantes absolutas:
  * floor() es la UNICA función de truncado (BR-003).
  * audit_log inalterable: sin UPDATE ni DELETE.
  * tenant_id en toda tabla + política RLS activa.
  * Cálculos de promedio SOLO en ConsolidacionDomainService (BR-008).
  * Modelo append-only en UC-005: original NUNCA sobreescrito.
```

### 1.4 Reasoning
```text
1. Mapear la arquitectura hexagonal en estructura de paquetes Java.
2. Diseñar clases para 5 módulos criticos con diagramas Mermaid.
3. Definir 15+ contratos API REST con request/response JSON completos.
4. Documentar entidades JPA con anotaciones, índices y constraints.
5. Generar DDL lógico completo (14 tablas) con políticas RLS.
6. Crear 4 diagramas de secuencia Mermaid.
7. Definir eventos de dominio (Spring Events).
8. Diseñar Spring Security 6: JwtAuthFilter, RBAC, TenantContext.
9. Documentar AuditLogAspect (AOP) y 2 schedulers.
10. Diseñar GlobalExceptionHandler y 7+ edge cases.
```

### 1.5 Stop condition
```text
Detente cuando el LFSD tenga: estructura de paquetes Java, 5 diagramas de clases,
15+ APIs, 4 diagramas de secuencia, DDL 14 tablas + RLS, eventos de dominio,
Spring Security 6, AOP auditoria, 2 schedulers, 7 edge cases, 16 tasks, glosario.
```

### 1.6 Output
```text
docs/LFSD-EduSync.md (20 secciones §0–§20 + checklist) listo para
implementación, code review y QA técnico.
```

## 2. Invariantes del prompt

- Ningún cálculo de promedio puede aparecer fuera de `ConsolidacionDomainService`.
- Todo endpoint DOCENTE debe tener verificación de asignación antes de persistir.
- El `audit_log` se escribe en la misma transacción que la operación principal.
- Toda tabla del DDL debe tener `tenant_id` + política RLS declarada.
- Los diagramas Mermaid deben usar nombres reales del dominio (no genéricos).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_DOMINIO_SIN_PSEUDOCODIGO` | Módulo crítico sin pseudoalgoritmo | Completar |
| `E_API_SIN_ERRORES` | Endpoint sin tabla de códigos HTTP | Agregar |
| `E_DDL_SIN_RLS` | Tabla en DDL sin política RLS | Agregar antes de entregar |
| `E_DIAGRAMA_GENERICO` | Diagrama con nombres ficticios | Reemplazar con dominio real |

## 4. Guardrails

- MUST: validar que el DDL tiene 14 tablas con RLS.
- MUST NOT: calcular el promedio fuera de `ConsolidacionDomainService`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD v1 + PRD v1 + BRD v2 + arquitectura funcional | `FSD-UC-001..005`, `DA-01..DA-05` | PR-LFSD-001 | `docs-agent` | `docs/LFSD-EduSync.md` v1.0 |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: FSD completo con 5 FSD-UC, ER 16 entidades, 12 BR, 16 NFRs.
- **Output esperado**: LFSD de 800+ líneas con 15 APIs, DDL 14 tablas, 4 secuencias Mermaid.

### 6.2 Caso borde
- **Input**: FSD con diagrama ER incompleto (13 entidades en lugar de 16).
- **Output esperado**: el agente infiere las 3 entidades faltantes desde los UC y las documenta.

### 6.3 Caso adversarial
- **Input**: propuesta de colocar lógica `floor()` en una `@Query` JPQL del repositorio.
- **Comportamiento esperado**: rechazado con `E_CALCULO_FUERA_DOMINIO`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 15/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
