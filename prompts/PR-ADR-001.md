# PR-ADR-001 — Decisión arquitectónica DA-01: Multitenancy con RLS en PostgreSQL 15

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-001` |
| Título | Generación de ADR DA-01: Aislamiento multitenant mediante Row-Level Security |
| Artefacto origen | `docs/arquitectura_funcional_EduSync.md` |
| ID origen | `DA-01` |
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
Documenta la decision arquitectonica DA-01 (Aislamiento multitenant mediante
tenant_id + Row-Level Security en PostgreSQL 15) como un ADR formal del proyecto
EduSync, evaluando las alternativas: schema separado, discriminador RLS y BD separada.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §DA-01.
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS.
- DA-01: Aislamiento multitenant (tenant_id + RLS vs. schema separado vs. BD separada).
- Contexto boliviano: equipo de 1 desarrollador, mercado de colegios <=1000 alumnos.
- UCs afectados: UC-01, UC-04, UC-06, UC-09, UC-10.
- Template: plantillas/ADR_TEMPLATE.md (9 secciones).
```

### 1.4 Reasoning
```text
1. Documentar el contexto: por qué se necesita aislamiento multitenant.
2. Evaluar >=3 alternativas con trade-offs: schema separado / discriminador RLS / BD separada.
3. Declarar la decision recomendada con justificacion tecnica y de negocio.
4. Documentar el impacto en los UCs afectados.
5. Especificar cuando revisar la decision (trigger de reevaluacion).
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga: contexto, >=3 alternativas con trade-offs, decision
recomendada con justificacion, impacto en UCs y trigger de reevaluacion.
```

### 1.6 Output
```text
Archivo docs/adr/0001-multitenancy-rls-postgresql.md con las 9 secciones del
ADR_TEMPLATE.md completadas con datos reales del proyecto EduSync.
```

## 2. Invariantes del prompt

- Cada DA debe evaluar >=3 alternativas reales.
- La decision recomendada debe ser justificable con el contexto boliviano actual.
- El impacto debe referenciar IDs de UCs reales (UC-01..UC-10).
- Ninguna DA puede proponer herramientas sin considerar la capacidad del equipo de 1 dev.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTERNATIVA_INSUFICIENTE` | DA con menos de 3 alternativas | Ampliar con opciones reales evaluadas |
| `E_DECISION_SIN_JUSTIFICACION` | DA sin justificacion tecnica | Rechazar output |
| `E_IMPACTO_NO_TRAZABLE` | Impacto no referencia UCs por ID | Completar con IDs reales |

## 4. Guardrails

- MUST: validar que el output cumple la estructura de ADR_TEMPLATE.md.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: exponer secretos ni credenciales en el context.
- MUST NOT: almacenar PII en logs del prompt.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `DA-01` | PR-ADR-001 | `arch-agent` | `docs/adr/0001-multitenancy-rls-postgresql.md` |
| FSD | `FSD-UC-001`, `FSD-UC-004`, `FSD-UC-009` | PR-ADR-001 | `arch-agent` | Referencias de impacto en el ADR |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo de DA-01 desde `arquitectura_funcional_EduSync.md`.
- **Output esperado**: ADR con 9 secciones completas, estado `Aceptada`, 3 alternativas con tabla Pros/Contras/Costo.

### 6.2 Caso borde
- **Input**: DA-01 con solo 2 alternativas documentadas en la fuente.
- **Output esperado**: el agente deriva una tercera alternativa (BD separada) de conocimiento del dominio.

### 6.3 Caso adversarial
- **Input**: solicitud de documentar código de implementación en el ADR.
- **Comportamiento esperado**: rechazo con `E_ARQUITECTURA_EN_SPECS`; el ADR solo documenta la decisión.

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
| Rodrigo Aspeti | 28/05/2026 | aprobado | ADR formal creado en docs/adr/0001-multitenancy-rls-postgresql.md |
