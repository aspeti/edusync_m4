# PR-DIAG-002 — Diagrama de estados de administración académica (Director)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-DIAG-002` |
| Título | Diagrama de estados del flujo del Director (administración académica) — stateDiagram-v2 + spec |
| Artefacto origen | Arquitectura funcional |
| ID origen | `UC-05`, `UC-07`, `UC-09`, `UC-10`, `DA-01`, `DA-02` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Business Process Analyst y Solution Architect especializado en
sistemas academicos, workflows administrativos y modelado de procesos educativos
para directores de unidades educativas bolivianas.
```

### 1.2 Task
```text
Analiza y diseña el flujo de estados del Director durante el ciclo completo
de administración académica en EduSync: creación de gestión, calendario (T1, T2, T3),
parámetros, habilitación de accesos, 3 trimestres y cierre anual. Genera dos
artefactos sincronizados:
(1) docs/diagramas/estados_administracion.mmd — stateDiagram-v2,
(2) docs/diagramas/estados_administracion.md — especificación completa.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-05, UC-07, UC-09, UC-10, DA-01, DA-02.
- Actor principal: Director. Actores secundarios: Sistema, Docente (UC-05).
- D1: "Habilitación de permisos" = asignación de roles + mapeo docente→materia.
- D2: Las fechas de T1/T2/T3 se definen al inicio; apertura es secuencial (T2 requiere T1 cerrado).
- D3: Parámetros académicos: alcance tenant+periodo, inmutables post-apertura.
- D4: Director puede autorizar UC-05 en cualquier trimestre cerrado (flujo paralelo).
- Caso excepcional: reasignación de docente durante trimestre activo.
- .mmd compatible con parsers Mermaid estándar.
```

### 1.4 Reasoning
```text
1. Identificar todos los estados del Director a lo largo de las 8 fases del ciclo.
2. Verificar apertura secuencial contra UC-09.
3. Modelar inmutabilidad de parámetros post-apertura (DA-02).
4. Diseñar patrón "GestionandoTx" replicable para los 3 trimestres.
5. Modelar cierre anual exigiendo los 3 trimestres cerrados antes del promedio anual.
```

### 1.5 Stop condition
```text
Detente cuando los 2 artefactos cubran: las 8 fases, caso excepcional de
reasignación docente, catálogo de estados, tabla de transiciones,
invariantes por fase, errores y relación con UCs.
```

### 1.6 Output
```text
(1) Mermaid stateDiagram-v2 con estados compuestos para cada trimestre.
(2) Markdown con metadatos, decisiones de diseño, catálogo por fase, tabla de transiciones.
```

## 2. Invariantes del prompt

- La apertura es secuencial: T2 nunca antes de cerrar T1; T3 nunca antes de T2 (UC-09).
- Los parámetros académicos son inmutables una vez que el periodo está `ABIERTO` (DA-02).
- El cierre institucional requiere 100% centralizadores `CERRADOS` (UC-09).
- El promedio anual solo se calcula con 3 trimestres `CERRADOS` (UC-03, IG-07).
- Solo el Director puede abrir/cerrar periodos.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_AMBIGUOUS_INPUT` | Regla no documentada en la fuente | STOP, solicitar confirmación |
| `E_APERTURA_NO_SECUENCIAL` | Diagrama permite abrir Tx sin cerrar Tx-1 | Rechazar, ajustar |
| `E_PARAMETROS_MUTABLES_POST_APERTURA` | Permite editar parámetros con periodo ABIERTO | Rechazar |
| `E_INCONSISTENCIA_MD_MMD` | Estado en uno y no en el otro | Rechazar entrega |

## 4. Guardrails

- MUST: validar apertura secuencial T1→T2→T3 en el grafo antes de entregar.
- MUST NOT: usar caracteres Unicode decorativos en labels Mermaid.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `UC-05`, `UC-09`, `UC-10`, `DA-01`, `DA-02` | PR-DIAG-002 | `process-agent` | `docs/diagramas/estados_administracion.mmd` + `estados_administracion.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: arquitectura funcional completa con UC-05, UC-07, UC-09, UC-10 y DA-01/DA-02.
- **Output esperado**: diagrama con ≥23 estados, 8 fases, patrón `GestionandoTx` para 3 trimestres.

### 6.2 Caso borde
- **Input**: fuente sin mención explícita del caso de reasignación docente.
- **Output esperado**: el agente deriva el caso excepcional de la invariante de audit_log y DA-01.

### 6.3 Caso adversarial
- **Input**: propuesta de abrir T2 antes de que T1 esté completamente cerrado.
- **Comportamiento esperado**: rechazado con `E_APERTURA_NO_SECUENCIAL`; la transición se bloquea.

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
