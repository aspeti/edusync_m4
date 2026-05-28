# PR-ADR-004 — Decisión arquitectónica DA-04: Consolidación asíncrona con Spring Events

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-004` |
| Título | Generación de ADR DA-04: Consolidación asíncrona mediante Spring Events (migrable a SQS) |
| Artefacto origen | `docs/arquitectura_funcional_EduSync.md` |
| ID origen | `DA-04` |
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
Documenta la decision arquitectonica DA-04 (Consolidacion post-cierre asincrona
mediante Spring Events internos con diseño migrable a AWS SQS) como un ADR formal,
evaluando las alternativas: sincrona, Spring Events y SQS dedicado.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §DA-04.
- Evento de dominio: MateriaCerradaEvent.
- Alternativas: sincrona / Spring Events internos / AWS SQS FIFO dedicado.
- UCs afectados: UC-02, UC-03.
- NFR-001: p95 de UC-02 < 500 ms — garantizado por asincronía.
```

### 1.4 Reasoning
```text
1. Documentar el contexto: picos de cierre trimestral en Bolivia.
2. Evaluar >=3 alternativas con trade-offs de consistencia vs. complejidad.
3. Declarar la decision (Spring Events con diseño migrable a SQS).
4. Documentar el impacto en UC-02 y UC-03.
5. Especificar la ruta de migración a SQS como Plan B.
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga las 9 secciones completas con estado Aceptada.
```

### 1.6 Output
```text
Archivo docs/adr/0004-async-consolidacion-spring-events.md con las 9 secciones
del ADR_TEMPLATE.md completadas.
```

## 2. Invariantes del prompt

- El diseño debe ser migrable a SQS sin cambiar el dominio.
- `DomainEventPublisher` es un puerto de salida del dominio (no un bean Spring).
- La respuesta de UC-02 debe ser < 500 ms (NFR-001).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTERNATIVA_INSUFICIENTE` | Menos de 3 alternativas | Ampliar |
| `E_DOMINIO_CON_SPRING` | Spring Events acoplado al dominio | Rechazar, usar puerto |
| `E_IMPACTO_NO_TRAZABLE` | Sin IDs de UCs | Completar |

## 4. Guardrails

- MUST: validar estructura ADR_TEMPLATE.md antes de entregar.
- MUST NOT: acoplar `MateriaCerradaEvent` directamente a Spring ApplicationEvent en el dominio.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `DA-04` | PR-ADR-004 | `arch-agent` | `docs/adr/0004-async-consolidacion-spring-events.md` |
| FSD | `FSD-UC-002`, `FSD-UC-003` | PR-ADR-004 | `arch-agent` | Referencias de impacto |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo de DA-04.
- **Output esperado**: ADR con 9 secciones, estado `Aceptada`, ruta de migración a SQS documentada.

### 6.2 Caso borde
- **Input**: fuente sin mención explícita del tiempo de respuesta de UC-02.
- **Output esperado**: el agente infiere el requisito de NFR-001 (p95 < 500 ms).

### 6.3 Caso adversarial
- **Input**: propuesta de consolidación síncrona como decisión tomada.
- **Comportamiento esperado**: el agente documenta la alternativa síncrona como evaluada y descartada.

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
| Rodrigo Aspeti | 28/05/2026 | aprobado | ADR formal creado en docs/adr/0004-async-consolidacion-spring-events.md |
