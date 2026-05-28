# PR-ADR-003 — Decisión arquitectónica DA-03: Persistencia inmutable con audit_log

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-003` |
| Título | Generación de ADR DA-03: Persistencia inmutable con audit_log explícito y append-only |
| Artefacto origen | `docs/arquitectura_funcional_EduSync.md` |
| ID origen | `DA-03` |
| Tipo de prompt | generación |
| Modelo recomendado | Opus |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Software Architect con experiencia en sistemas SaaS multitenant,
arquitecturas hexagonales, integraciones gubernamentales y toma de decisiones
arquitectonicas documentadas con criterio de trade-off explicito.
```

### 1.2 Task
```text
Documenta la decision arquitectonica DA-03 (Persistencia inmutable con audit_log
explicito + Hibernate Envers + modelo append-only en UC-05) como un ADR formal,
evaluando las alternativas: triggers PostgreSQL, Hibernate Envers y tabla audit_log + AOP.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §DA-03.
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 15, Hibernate 6.
- DA-03: Modelo de persistencia inmutable (audit_log + Envers + append-only UC-05).
- Normativa: BR-005 del BRD, @Immutable Hibernate, RULE PostgreSQL.
- UCs afectados: UC-01, UC-02, UC-04, UC-05, UC-06.
```

### 1.4 Reasoning
```text
1. Documentar el contexto: trazabilidad legal boliviana de calificaciones.
2. Evaluar >=3 alternativas: triggers PostgreSQL / Hibernate Envers / audit_log + AOP.
3. Declarar la decision combinada (audit_log + AOP + append-only UC-05).
4. Documentar el impacto en los UCs afectados.
5. Especificar el trigger de reevaluacion.
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga las 9 secciones completas con estado Aceptada.
```

### 1.6 Output
```text
Archivo docs/adr/0003-persistencia-inmutable-audit-log.md con las 9 secciones
del ADR_TEMPLATE.md completadas con datos reales.
```

## 2. Invariantes del prompt

- Toda operacion de escritura genera entrada en audit_log en la misma transaccion.
- El audit_log es inmutable: sin UPDATE ni DELETE.
- El modelo append-only de UC-05 es innegociable.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTERNATIVA_INSUFICIENTE` | DA con menos de 3 alternativas | Ampliar |
| `E_DECISION_SIN_JUSTIFICACION` | Sin justificacion tecnica | Rechazar |
| `E_IMPACTO_NO_TRAZABLE` | Sin IDs de UCs | Completar |

## 4. Guardrails

- MUST: validar estructura ADR_TEMPLATE.md antes de entregar.
- MUST NOT: proponer UPDATE/DELETE sobre audit_log en ninguna alternativa.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `DA-03` | PR-ADR-003 | `arch-agent` | `docs/adr/0003-persistencia-inmutable-audit-log.md` |
| BRD | `BR-005` (append-only) | PR-ADR-003 | `arch-agent` | Invariante de inmutabilidad en el ADR |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo de DA-03.
- **Output esperado**: ADR con 9 secciones, estado `Aceptada`.

### 6.2 Caso borde
- **Input**: fuente sin mención de `@Immutable` Hibernate.
- **Output esperado**: el agente lo infiere de la invariante de audit_log inalterable.

### 6.3 Caso adversarial
- **Input**: solicitud de alternativa que permita `DELETE` en audit_log.
- **Comportamiento esperado**: rechazado — ninguna alternativa válida permite DELETE.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Opus |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | ADR formal creado en docs/adr/0003-persistencia-inmutable-audit-log.md |
