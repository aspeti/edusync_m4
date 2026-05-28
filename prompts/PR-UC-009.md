# PR-UC-009 — Contrato de UC-09: Administración de periodos académicos

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-UC-009` |
| Título | Contrato técnico de UC-09: Administración de periodos académicos institucionales |
| Artefacto origen | FSD |
| ID origen | `FSD-UC-009` |
| Tipo de prompt | transformación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Backend Engineer especializado en gestion del ciclo de vida de
periodos academicos, parametrizacion de reglas de negocio y multitenant con
aislamiento por tenant_id + PostgreSQL RLS.
```

### 1.2 Task
```text
Genera el contrato tecnico del conjunto de endpoints de UC-09 (Administracion
de periodos academicos institucionales), cubriendo la apertura, parametrizacion
y cierre de periodos trimestrales para una unidad educativa (tenant).
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-09, DA-01, DA-02.
- Actor: Director (apertura y cierre), Secretaria (monitoreo).
- Solo el Director puede abrir o cerrar un periodo institucional.
- No se puede abrir un trimestre si el anterior no esta completamente cerrado.
- Los parametros se fijan al abrir el periodo y son INMUTABLES durante su vigencia:
  * Conjunto de dimensiones activas (Ser/Saber/Hacer/Decidir ± Autoevaluacion).
  * Peso maximo de cada dimension (en puntos).
  * Regla de combinacion de evaluaciones (PROMEDIO_SIMPLE, SUMA, MEJOR_N).
  * Criterio de truncado (floor).
  * Umbral de reprobacion trimestral (< 51 pts / 100).
  * Formato de exportacion SIE (floor(nota/3) → escala 0-33).
- El cierre institucional requiere que todos los centralizadores del periodo
  esten en estado CERRADO.
- Aislamiento: alcance de todos los parametros es tenant + periodo.
```

### 1.4 Reasoning
```text
1. Definir los endpoints: POST /periodos (crear), PUT /periodos/{id}/apertura,
   PUT /periodos/{id}/cierre, GET /periodos/{id}/parametros.
2. Especificar el schema de parametros academicos (inmutables post-apertura).
3. Definir la validacion de apertura secuencial (T2 no abre sin T1 cerrado).
4. Especificar la validacion de cierre (100% centralizadores CERRADOS).
5. Declarar las notificaciones generadas: apertura → docentes, cierre → secretaria.
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: schema de parametros, apertura secuencial,
cierre con prerequisito de centralizadores y 3 casos de prueba.
```

### 1.6 Output
```text
Markdown con: tabla de endpoints, schema JSON de parametros, regla de apertura
secuencial, regla de cierre y 3 casos de prueba.
```

## 2. Invariantes del prompt

- Los parametros academicos son inmutables una vez que el periodo esta ABIERTO.
- No se puede abrir un trimestre si el anterior no esta en estado CERRADO.
- El cierre solo es posible si todos los centralizadores del periodo estan CERRADOS.
- El alcance de toda consulta esta restringido al tenant autenticado (RLS).
- Solo el rol DIRECTOR puede ejecutar apertura o cierre de periodo.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_PERIODO_PREVIO_ABIERTO` | El trimestre anterior no esta cerrado | HTTP 409 |
| `E_PARAMETROS_INCOMPLETOS` | Faltan campos requeridos en la configuracion | HTTP 400 |
| `E_CENTRALIZADORES_PENDIENTES` | Existen cursos sin centralizar al intentar cerrar | HTTP 409 con lista de cursos pendientes |
| `E_PARAMETRO_INMUTABLE` | Intento de modificar parametros de un periodo ABIERTO | HTTP 403 |

## 4. Guardrails

- MUST: validar que el output cumple el schema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: permitir apertura de T2 sin que T1 esté en estado `CERRADO`.
- MUST NOT: permitir modificación de parámetros con periodo `ABIERTO`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-009` | PR-UC-009 | `dev-agent` | Contratos endpoints de periodos académicos |
| ADR | `ADR-0001` (RLS multitenant), `ADR-0002` (parametrización) | PR-UC-009 | `dev-agent` | Estrategia de aislamiento y parametrización del periodo |
| BRD | `BR-007` (parámetros inmutables post-apertura) | PR-UC-009 | `dev-agent` | Invariante de inmutabilidad |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: Director abre T1 con parámetros completos (dimensiones, pesos, regla `PROMEDIO_SIMPLE`).
- **Output esperado**: HTTP 200, periodo en estado `ABIERTO`, parámetros persistidos e inmutables.

### 6.2 Caso borde
- **Input**: Director intenta abrir T2 con T1 en estado `ABIERTO`.
- **Output esperado**: HTTP 409 `E_PERIODO_PREVIO_ABIERTO`, T2 no se crea.

### 6.3 Caso adversarial
- **Input**: Director intenta modificar el peso de una dimensión con el periodo ya `ABIERTO`.
- **Comportamiento esperado**: HTTP 403 `E_PARAMETRO_INMUTABLE`; el parámetro no cambia.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
