# PR-FSD-001 — Generación del FSD EduSync (modo FSD Clásico)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-FSD-001` |
| Título | Generación del Functional Specification Document EduSync v1.0 (FSD Clásico) |
| Artefacto origen | PRD v1 + BRD v2 + MRD v1 + arquitectura funcional |
| ID origen | `PRD-REQ-001..020`, `UC-01..UC-10`, `DA-01..DA-05` |
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
Eres un experto en Functional Analysis, Software Architecture y System Design.
Generas documentos FSD técnicamente precisos, implementables y verificables
para sistemas Java 21 / Spring Boot 3 con arquitectura hexagonal.
```

### 1.2 Task
```text
Genera docs/fsd/FSD_EduSync.md siguiendo plantillas/FSD_TEMPLATE.md en modo
FSD Clásico, especificando QUE hace EduSync con nivel técnico suficiente para
que desarrollo, QA y arquitectura puedan implementar y verificar.
```

### 1.3 Context
```text
- Insumos: PRD v1 (20 PRD-REQ-*, 15 NFRs), BRD v2, MRD v1, arquitectura funcional.
- Stack: Java 21, Spring Boot 3.3, Spring Security 6, Spring Data JPA, PostgreSQL 15, Angular 17, AWS.
- Arquitectura: hexagonal (Domain / Application / Infrastructure).
- Entidades criticas: Calificacion, Centralizador, ExportacionSIE, AuditLog,
  AutorizacionCorreccion, ParametroAcademico, GestionAcademica, Periodo.
- Invariantes absolutas: floor() truncado, RUDE única clave, audit_log inalterable,
  RLS activo en todas las tablas.
```

### 1.4 Reasoning
```text
1. Documentar 5 FSD-UC criticos (UC-001, UC-003, UC-004, UC-005, UC-009).
2. Documentar 12 reglas de negocio BR-001..BR-012.
3. Generar diagrama ER Mermaid con 16 entidades.
4. Completar diccionario de datos.
5. Generar 3 prompt-contratos y 14 Tasks.
6. Documentar 16 NFRs con metrica y umbral.
7. Completar trazabilidad MRD→PRD→FSD→NFR→prueba.
```

### 1.5 Stop condition
```text
Detente cuando el FSD tenga: 5 FSD-UC con Gherkin, 12 BR-NNN, ER 16 entidades,
diccionario, 3 prompt-contratos, 14 tasks, 16 NFRs, trazabilidad y checklist.
```

### 1.6 Output
```text
docs/fsd/FSD_EduSync.md (secciones 0-15 + checklist FSD Clásico).
```

## 2. Invariantes del prompt

- El cálculo de `floor()` y la conversión SIE solo ocurren en la capa de dominio.
- El `audit_log` se escribe en la misma transacción que el INSERT/UPDATE de la entidad.
- Toda tabla nueva debe tener `tenant_id` y política RLS antes de llegar a `main`.
- El modelo append-only en UC-005 es innegociable.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_CALCULO_FUERA_DOMINIO` | Promedio o floor en adaptador/SQL/frontend | Rechazar PR |
| `E_AUDIT_LOG_OMITIDO` | Operación de escritura sin audit_log | Rechazar PR |
| `E_RLS_FALTANTE` | Nueva tabla sin tenant_id o sin política RLS | Rechazar migración |
| `E_APPEND_ONLY_VIOLADO` | Modificación que sobreescribe registro original | Rechazar |

## 4. Guardrails

- MUST: validar las 16 entidades en el ER antes de entregar.
- MUST NOT: incluir código de implementación en el FSD Clásico.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| PRD v1 + BRD v2 + arquitectura funcional | `PRD-REQ-001..020`, `UC-01..UC-10` | PR-FSD-001 | `docs-agent` | `docs/fsd/FSD_EduSync.md` v1.0 |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: PRD completo + BRD v2 + arquitectura funcional + diagramas de estado.
- **Output esperado**: FSD con 5 FSD-UC, ER 16 entidades, 14 tasks, 16 NFRs.

### 6.2 Caso borde
- **Input**: PRD con un UC sin criterios de aceptación formales.
- **Output esperado**: el agente deriva los criterios desde los diagramas de estado.

### 6.3 Caso adversarial
- **Input**: propuesta de `@Query` SQL calculando el promedio en el repositorio JPA.
- **Comportamiento esperado**: rechazo con `E_CALCULO_FUERA_DOMINIO`.

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
