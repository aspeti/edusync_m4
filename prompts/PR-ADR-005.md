# PR-ADR-005 — Decisión arquitectónica DA-05: Resiliencia en integración con el SIE

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-005` |
| Título | Generación de ADR DA-05: Resiliencia en integración SIE con Resilience4j e idempotencia |
| Artefacto origen | `docs/arquitectura_funcional_EduSync.md` |
| ID origen | `DA-05` |
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
Documenta la decision arquitectonica DA-05 (Resiliencia en integracion con el SIE:
estado por registro, idempotencia RUDE+periodo_id y reintentos con Resilience4j)
como un ADR formal, evaluando las alternativas: exportacion atomica, estado por
registro y lotes con checkpoint.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §DA-05.
- Biblioteca: Resilience4j (circuit breaker + retry).
- Clave de idempotencia: RUDE + periodo_id.
- UCs afectados: UC-04.
- Contexto: servidor SIE gubernamental con alta tasa de fallos en horario pico boliviano.
```

### 1.4 Reasoning
```text
1. Documentar el contexto: SIE sin garantias de idempotencia y alta tasa de fallos.
2. Evaluar >=3 alternativas: exportacion atomica / estado por registro / lotes checkpoint.
3. Declarar la decision (estado por registro + Resilience4j).
4. Documentar el impacto en UC-04.
5. Especificar el trigger de reevaluacion (API SIE con idempotencia nativa).
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga las 9 secciones completas con estado Aceptada.
```

### 1.6 Output
```text
Archivo docs/adr/0005-resiliencia-integracion-sie-resilience4j.md con las 9 secciones
del ADR_TEMPLATE.md completadas.
```

## 2. Invariantes del prompt

- La idempotencia por `RUDE + periodo_id` es innegociable.
- Resilience4j debe configurarse via `parametro_academico` (sin redespliegue).
- El fallo parcial del SIE no reinicia el proceso desde cero.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTERNATIVA_INSUFICIENTE` | Menos de 3 alternativas | Ampliar |
| `E_IDEMPOTENCIA_FALTANTE` | Alternativa sin clave de idempotencia | Rechazar |
| `E_IMPACTO_NO_TRAZABLE` | Sin IDs de UCs | Completar |

## 4. Guardrails

- MUST: validar estructura ADR_TEMPLATE.md antes de entregar.
- MUST NOT: proponer exportación atómica como decisión tomada.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `DA-05` | PR-ADR-005 | `arch-agent` | `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` |
| FSD | `FSD-UC-004` | PR-ADR-005 | `arch-agent` | Referencias de impacto |
| BRD | `BR-006` (fallo parcial no reinicia) | PR-ADR-005 | `arch-agent` | Invariante de resiliencia |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo de DA-05.
- **Output esperado**: ADR con 9 secciones, estado `Aceptada`, tabla de 3 alternativas.

### 6.2 Caso borde
- **Input**: fuente sin mención explícita de Resilience4j.
- **Output esperado**: el agente justifica Resilience4j como biblioteca estándar para el stack Spring Boot 3.3.

### 6.3 Caso adversarial
- **Input**: propuesta de exportación atómica como decisión.
- **Comportamiento esperado**: documentada como alternativa evaluada y descartada por riesgo de duplicados.

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
| Rodrigo Aspeti | 28/05/2026 | aprobado | ADR formal creado en docs/adr/0005-resiliencia-integracion-sie-resilience4j.md |
