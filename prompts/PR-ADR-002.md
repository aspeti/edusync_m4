# PR-ADR-002 — Decisión arquitectónica DA-02: Parametrización de reglas normativas

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-002` |
| Título | Generación de ADR DA-02: Parametrización de reglas normativas sin redespliegue |
| Artefacto origen | `docs/arquitectura_funcional_EduSync.md` |
| ID origen | `DA-02` |
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
Documenta la decision arquitectonica DA-02 (Parametrizacion de reglas normativas
sin redespliegue) como un ADR formal, evaluando las alternativas: hardcoded en
dominio, application.yml versionado y tabla parametro_academico en BD.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §DA-02.
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS.
- DA-02: Parametrizacion de reglas normativas (BD vs. application.yml vs. hardcoded).
- Contexto boliviano: el MInisterio puede cambiar reglas sin previo aviso.
- UCs afectados: UC-01, UC-03, UC-04, UC-09.
- Template: plantillas/ADR_TEMPLATE.md (9 secciones).
```

### 1.4 Reasoning
```text
1. Documentar el contexto: cambios ministeriales frecuentes sin redespliegue.
2. Evaluar >=3 alternativas: hardcoded / application.yml / tabla BD parametro_academico.
3. Declarar la decision recomendada con justificacion.
4. Documentar el impacto en los UCs afectados.
5. Especificar el trigger de reevaluacion.
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga las 9 secciones completas con datos reales del proyecto.
```

### 1.6 Output
```text
Archivo docs/adr/0002-parametrizacion-reglas-normativas.md con las 9 secciones
del ADR_TEMPLATE.md completadas.
```

## 2. Invariantes del prompt

- Cada DA debe evaluar >=3 alternativas reales.
- La decision debe justificarse con el contexto boliviano actual.
- El impacto debe referenciar IDs de UCs reales.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTERNATIVA_INSUFICIENTE` | DA con menos de 3 alternativas | Ampliar |
| `E_DECISION_SIN_JUSTIFICACION` | DA sin justificacion tecnica | Rechazar output |
| `E_IMPACTO_NO_TRAZABLE` | Impacto sin IDs de UCs | Completar |

## 4. Guardrails

- MUST: validar estructura ADR_TEMPLATE.md antes de entregar.
- MUST NOT: hardcodear reglas normativas en el ADR — solo documentar la decisión.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `DA-02` | PR-ADR-002 | `arch-agent` | `docs/adr/0002-parametrizacion-reglas-normativas.md` |
| FSD | `FSD-UC-001`, `FSD-UC-003`, `FSD-UC-009` | PR-ADR-002 | `arch-agent` | Referencias de impacto |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo de DA-02 desde `arquitectura_funcional_EduSync.md`.
- **Output esperado**: ADR con 9 secciones, estado `Aceptada`, tabla de 3 alternativas.

### 6.2 Caso borde
- **Input**: fuente sin mención explícita del costo de la alternativa BD.
- **Output esperado**: el agente estima el costo con datos conocidos del stack.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir código SQL de la tabla `parametro_academico` en el ADR.
- **Comportamiento esperado**: rechazo; el ADR solo documenta la decisión, no el DDL.

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
| Rodrigo Aspeti | 28/05/2026 | aprobado | ADR formal creado en docs/adr/0002-parametrizacion-reglas-normativas.md |
