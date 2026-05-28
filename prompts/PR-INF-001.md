# PR-INF-001 — Informe de indicadores institucionales (UC-10)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-INF-001` |
| Título | Contrato del módulo de reportería estadística e indicadores institucionales UC-10 |
| Artefacto origen | FSD + arquitectura funcional |
| ID origen | `UC-10` |
| Tipo de prompt | extracción |
| Modelo recomendado | Haiku |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Borrador |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Data Analyst especializado en indicadores academicos, dashboards
educativos y reporteria estadistica para directivos de unidades educativas
bolivianas.
```

### 1.2 Task
```text
Genera el contrato del modulo de reporteria estadistica (UC-10), diferenciando
los indicadores trimestrales de los anuales y garantizando que los indicadores
anuales solo se calculan y muestran cuando los 3 trimestres estan cerrados.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-10.
- Actor: Director (acceso exclusivo a indicadores globales).
- Dos vistas:
  * "Por trimestre": disponible al cerrar cada trimestre.
  * "Anual final": disponible SOLO al cerrar los 3 trimestres.
- Regla: NO calcular ni mostrar el índice de reprobación anual con datos parciales.
- Indicador de cumplimiento: % materias cerradas vs. pendientes (tiempo real).
- Stack: Java 21, Spring Boot 3, PostgreSQL 15, Angular 17.
```

### 1.4 Reasoning
```text
1. Definir los endpoints del dashboard: GET /reportes/trimestre/{id},
   GET /reportes/anual, GET /reportes/avance-docente.
2. Especificar las agregaciones SQL (% aprobados, promedio por materia, ranking).
3. Definir la lógica de guarda: indicadores anuales bloqueados hasta T3 cerrado.
4. Especificar la exportación PDF del reporte estadístico.
5. Verificar el aislamiento por tenant_id en todas las consultas.
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: endpoints, lógica de guarda anual,
exportación PDF, aislamiento por tenant y 3 casos de prueba.
```

### 1.6 Output
```text
Markdown con: tabla de endpoints, lógica de guarda para indicadores anuales,
ejemplo JSON del dashboard y 3 casos de prueba.
```

## 2. Invariantes del prompt

- Los indicadores anuales son `NULL` hasta que los 3 trimestres están `CERRADOS`.
- Toda consulta filtra por `tenant_id` del Director autenticado.
- El % de reprobación solo se calcula sobre centralizadores en estado `CERRADO`.
- La exportación PDF solo está disponible para el rol `DIRECTOR`.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TRIMESTRE_NO_CERRADO` | Indicador anual con T1/T2/T3 incompleto | Retornar NULL con etiqueta EN_CURSO |
| `E_ACCESO_NO_AUTORIZADO` | Rol distinto de DIRECTOR consulta indicadores globales | HTTP 403 |
| `E_TENANT_VIOLATION` | Consulta intenta acceder a otro tenant | HTTP 403, registrar en audit_log |

## 4. Guardrails

- MUST: validar que los indicadores anuales solo se calculan con 3 trimestres cerrados.
- MUST NOT: exponer datos de otro tenant en ninguna consulta.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD + arquitectura funcional | `UC-10` | PR-INF-001 | `docs-agent` | Contrato dashboard de reportería `GET /reportes/*` |
| BRD | `BR-011` (anual con 3 trimestres) | PR-INF-001 | `docs-agent` | Lógica de guarda de indicadores anuales |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: periodo con 3 trimestres `CERRADOS`, Director autenticado con `tenant_id` válido.
- **Output esperado**: `promedioAnual` calculado, `porcentajeReprobacion` numérico, botón PDF disponible.

### 6.2 Caso borde
- **Input**: solo T1 y T2 cerrados, T3 abierto.
- **Output esperado**: `promedioAnual: null`, etiqueta `"EN_CURSO"`, botón PDF deshabilitado.

### 6.3 Caso adversarial
- **Input**: token JWT de un Docente intentando acceder a `GET /reportes/anual`.
- **Comportamiento esperado**: HTTP 403 `E_ACCESO_NO_AUTORIZADO`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `query_latency_p95`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Haiku |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | borrador | Pendiente de validación formal |
