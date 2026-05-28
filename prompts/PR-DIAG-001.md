# PR-DIAG-001 — Diagrama de estados del flujo de carga de notas (Docente)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-DIAG-001` |
| Título | Diagrama de estados del flujo del Docente (carga de notas) — stateDiagram-v2 + spec |
| Artefacto origen | Arquitectura funcional |
| ID origen | `UC-01`, `UC-02`, `UC-03`, `UC-05`, `UC-09` |
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
para unidades educativas bolivianas.
```

### 1.2 Task
```text
Analiza y diseña el flujo de estados del Docente durante el proceso de carga
de notas en EduSync. Genera dos artefactos sincronizados:
(1) docs/diagramas/estados.cargarnotas.mmd con un stateDiagram-v2 de Mermaid,
(2) docs/diagramas/estados_cargar_notas.md con la especificación completa del workflow.
```

### 1.3 Context
```text
- Fuente: arquitectura_funcional_EduSync.md §UC-01, UC-02, UC-03, UC-05, UC-09.
- Actor principal: Docente. Actores secundarios: Director (UC-05), Sistema.
- D1: "Borrador" equivale a notas auto-guardadas con periodo ABIERTO.
- D2: No existe revisión previa de Secretaria/Director en el flujo normal.
- D3: La publicación del centralizador es automática cuando el 100% de materias está CERRADAS.
- Escenarios: inicio, habilitación RBAC, periodo abierto/cerrado, carga parcial,
  validaciones en tiempo real, cierre operativo, ventana retroactiva (1h-72h, default 24h),
  revocación automática, periodo cerrado inesperadamente.
- Requisito técnico: .mmd compatible con parsers Mermaid estándar.
```

### 1.4 Reasoning
```text
1. Identificar todos los estados posibles del Docente.
2. Verificar contra UC-01..UC-05 que cada estado tiene invariante referenciada.
3. Construir el grafo sin cierre parcial.
4. Modelar la ventana UC-05 con 4 subestados.
5. Generar catálogo de estados (ID estable E-NN) y tabla de transiciones T-NN.
```

### 1.5 Stop condition
```text
Detente cuando los 2 artefactos cubran: estados iniciales, flujo normal, flujo
retroactivo UC-05, caso excepcional de periodo cerrado inesperadamente,
catálogo de estados, tabla de transiciones, invariantes y escalabilidad.
```

### 1.6 Output
```text
(1) docs/diagramas/estados.cargarnotas.mmd — stateDiagram-v2 limpio.
(2) docs/diagramas/estados_cargar_notas.md — especificación completa con catálogo y transiciones.
```

## 2. Invariantes del prompt

- Cada estado del `.mmd` debe estar referenciado en el `.md` y viceversa (1:1).
- Toda transición debe tener evento disparador y actor responsable explícitos.
- La transición `MateriaCerrada → SOLO_LECTURA` es irreversible.
- La ventana retroactiva siempre modela expiración automática.
- No puede haber estados huérfanos.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_AMBIGUOUS_INPUT` | Regla de negocio no documentada en la fuente | STOP, solicitar confirmación |
| `E_HUERFANO_DETECTADO` | Estado sin transiciones de entrada o salida | Rechazar output |
| `E_INCONSISTENCIA_MD_MMD` | Estado en uno de los archivos pero no en el otro | Rechazar entrega |
| `E_PARSER_INCOMPATIBLE` | `.mmd` no renderiza en parsers estándar | Regenerar con ASCII |

## 4. Guardrails

- MUST: validar consistencia 1:1 entre `.mmd` y `.md` antes de entregar.
- MUST NOT: usar caracteres Unicode decorativos en labels Mermaid.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Arquitectura funcional | `UC-01`, `UC-02`, `UC-03`, `UC-05`, `UC-09` | PR-DIAG-001 | `process-agent` | `docs/diagramas/estados.cargarnotas.mmd` + `estados_cargar_notas.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: arquitectura funcional completa con UC-01 a UC-09.
- **Output esperado**: diagrama con ≥18 estados, catálogo con IDs E-NN, tabla de transiciones T-NN.

### 6.2 Caso borde
- **Input**: fuente sin descripción explícita del estado "borrador".
- **Output esperado**: el agente aplica D1 (notas auto-guardadas) y lo documenta en decisiones de diseño.

### 6.3 Caso adversarial
- **Input**: propuesta de estado "cierre parcial" donde algunas notas están cerradas y otras abiertas.
- **Comportamiento esperado**: rechazado; UC-02 define el cierre como atómico.

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
