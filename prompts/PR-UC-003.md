# PR-UC-003 — Contrato de UC-03: Consolidación algorítmica de centralizadores

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-UC-003` |
| Título | Contrato técnico de UC-03: Consolidación algorítmica de centralizadores |
| Artefacto origen | FSD |
| ID origen | `FSD-UC-003` |
| Tipo de prompt | transformación |
| Modelo recomendado | Opus |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Data Engineer especializado en motores de calculo academico,
algoritmos de truncado y arquitecturas de calculo en tiempo real con
Spring Boot 3, Spring Events y PostgreSQL.
```

### 1.2 Task
```text
Genera el contrato tecnico del motor de consolidacion de centralizadores
(UC-03), diferenciando el modo PROVISIONAL (tiempo real) del modo OFICIAL
(post-cierre total), incluyendo el algoritmo de truncado floor y la regla
de combinacion de N evaluaciones por dimension.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-03, DA-02.
- Modo PROVISIONAL: calcula con materias ABIERTAS. Marcado como PROVISIONAL.
  No valido para boletines (UC-07) ni exportacion SIE (UC-04).
- Modo OFICIAL: solo cuando 100% materias del curso estan CERRADAS.
- Algoritmo de truncado: floor (piso), no redondeo estandar.
  Ejemplo: 64.666... → 64 (no 65). Elimina descuadres de escala.
- Combinacion de N evaluaciones por dimension: parametrica por tenant+periodo.
  Reglas soportadas: PROMEDIO_SIMPLE, SUMA, MEJOR_N.
- Conversion a escala SIE: floor(nota/3) → escala 0-33.
- Indicadores anuales: solo cuando los 3 trimestres estan CERRADOS.
- Restriccion: ningun calculo ocurre en SQL ad-hoc, adaptadores ni frontend.
- Stack: Java 21, Spring Boot 3, Spring Events, PostgreSQL 15.
```

### 1.4 Reasoning
```text
1. Definir la interfaz del motor (input: curso_id, periodo_id, modo).
2. Especificar el algoritmo de combinacion de evaluaciones por dimension
   (aplicar regla parametrica → truncar con floor → escalar al peso de la dimension).
3. Definir las dos salidas: PROVISIONAL (con marca de agua) y OFICIAL (inmutable).
4. Especificar cuando se activa cada modo (evento MateriaCarradaEvent).
5. Definir el comportamiento del indicador anual con trimestres parciales.
```

### 1.5 Stop condition
```text
Detente cuando el contrato cubra: algoritmo de calculo con floor, diferencia
PROVISIONAL/OFICIAL, calculo de escala SIE, indicadores anuales y 3 pruebas.
```

### 1.6 Output
```text
Markdown con: pseudocodigo del algoritmo de consolidacion, tabla de parametros
configurables (DA-02), especificacion de los 2 modos de salida, ejemplos
numericos con floor y 3 casos de prueba.
```

## 2. Invariantes del prompt

- El algoritmo `floor` es ÚNICO y centralizado en el dominio; no se replica.
- El centralizador PROVISIONAL no puede usarse para generar boletines ni exportar.
- El promedio anual solo se calcula y muestra con los 3 trimestres cerrados.
- La regla de combinacion de evaluaciones es parametrica, no hardcodeada.
- `floor(64.666) = 64`; `floor(nota/3)` para la escala SIE.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_MATERIAS_ABIERTAS_MODO_OFICIAL` | Se solicita modo OFICIAL con materias ABIERTAS | Rechazar calculo oficial, retornar PROVISIONAL |
| `E_PARAMETRO_FALTANTE` | Regla de combinacion no configurada para tenant+periodo | STOP, lanzar excepcion de configuracion |
| `E_TRIMESTRE_INCOMPLETO` | Se solicita promedio anual sin los 3 trimestres cerrados | Retornar null con etiqueta EN_CURSO, no calcular |

## 4. Guardrails

- MUST: validar que el output cumple el schema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: calcular el promedio fuera de `ConsolidacionDomainService`.
- MUST NOT: usar `Math.round()`, `RoundingMode.HALF_UP` ni `Math.ceil()`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-003` | PR-UC-003 | `dev-agent` | Contrato motor de consolidación |
| ADR | `ADR-0002` (parametrización), `ADR-0004` (Spring Events) | PR-UC-003 | `dev-agent` | Algoritmo de consolidación y evento trigger |
| BRD | `BR-003` (floor), `BR-008` (cálculo en dominio), `BR-011` (anual) | PR-UC-003 | `dev-agent` | Invariantes del motor |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: curso con todas las materias `CERRADAS`, regla `PROMEDIO_SIMPLE`, notas `[80, 70, 90]` en dimensión SABER (peso 45 pts).
- **Output esperado**: promedio `80.0`, truncado con `floor` → `80`, modo `OFICIAL`, entrada en `audit_log`.

### 6.2 Caso borde
- **Input**: nota con fracción `64.666...`.
- **Output esperado**: `floor(64.666) = 64` (nunca 65); el modo PROVISIONAL es retornado si hay materias abiertas.

### 6.3 Caso adversarial
- **Input**: solicitud de promedio anual con solo 2 trimestres cerrados.
- **Comportamiento esperado**: `promedioAnual = null`, etiqueta `EN_CURSO`; rechazo con `E_TRIMESTRE_INCOMPLETO`.

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
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
