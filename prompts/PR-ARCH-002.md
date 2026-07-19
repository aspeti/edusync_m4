# PR-ARCH-002 — Actualización de AGENTS.md v0.2

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ARCH-002` |
| Título | Actualización de AGENTS.md v0.1 → v0.2: corrección de 6 rutas y 15 artefactos nuevos |
| Artefacto origen | `AGENTS.md` v0.1 + repositorio EduSync |
| ID origen | `BR-001..BR-012`, `DA-01..DA-05`, `NFR-001..016` |
| Tipo de prompt | consolidación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 17/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Documentation Architect con experiencia en sistemas multiagente y AI-SDLC.
Tu responsabilidad es mantener AGENTS.md sincronizado con la estructura real
del repositorio del proyecto EduSync (Java 21, Spring Boot 3.3, PostgreSQL 15).
```

### 1.2 Task
```text
Actualiza AGENTS.md v0.1 corrigiendo las 6 rutas rotas y añadiendo referencias
a los 15 artefactos nuevos generados en la release 1.0.0 (MRD, PRD, FSD, LFSD,
APORTES, 5 diagramas, seguridad.mdc, reorganizacion de brd/ mrd/ prd/ fsd/).
```

### 1.3 Context
```text
- Documento a actualizar: AGENTS.md v0.1 (15 secciones, 326 lineas)
- Nuevos artefactos: docs/brd/BRD_EduSync_v2.md, docs/mrd/MRD_EduSync.md,
  docs/prd/PRD_EduSync.md, docs/fsd/FSD_EduSync.md, docs/LFSD-EduSync.md,
  docs/APORTES_EduSync.md, docs/diagrams/*.mmd/*.md, .cursor/rules/seguridad.mdc
- Rutas rotas: docs/DTI.md (no existe), docs/BRD_EduSync.md (movido a docs/brd/),
  docs/adr/ADR-001..005 (pendiente de creacion)
- Stack autoritativo: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS
```

### 1.4 Reasoning
```text
1. Listar todos los archivos del repo (excluyendo .git) y comparar con AGENTS.md.
2. Identificar 6 rutas rotas y 15 archivos nuevos no referenciados.
3. Corregir §1 (identidad con tabla de documentos), §2 (orden de lectura),
   §3 (arbol de estructura real del repositorio).
4. Añadir 4 nuevos agentes: arch-agent, qa-agent, process-agent, compliance-agent.
5. Definir 4 golden tests de zero-tolerance; actualizar metricas y registro de cambios.
```

### 1.5 Stop condition
```text
Detente cuando todos los paths esten verificados contra la estructura real del
repositorio, las 6 rutas rotas corregidas, los 15 archivos nuevos referenciados
y el registro de cambios incluya la entrada v0.2.
```

### 1.6 Output
```text
AGENTS.md v0.2 (417 lineas) con: tabla de documentos actualizada, arbol de
estructura real del repositorio, 6 agentes documentados con guardrails, 4 golden
tests de zero-tolerance, checklist con 10 items completados y 4 pendientes.
```

## 2. Invariantes del prompt

- Todos los paths referenciados deben existir en el repositorio real.
- Archivos pendientes marcados como "pendiente de creacion".
- Sin secretos en texto plano.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_RUTA_ROTA` | Path referenciado no existe en el repo | Corregir o marcar pendiente |
| `E_ARCHIVO_NUEVO_OMITIDO` | Nuevo artefacto sin referencia | Añadir a §1 y §3 |
| `E_VERSION_NO_BUMPED` | Registro de cambios no actualizado | Añadir fila v0.2 |

## 4. Guardrails

- MUST: verificar paths contra el filesystem antes de confirmar.
- MUST NOT: eliminar referencias a artefactos existentes.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| AGENTS.md v0.1 + repo real | `BR-001..BR-012`, `DA-01..DA-05` | PR-ARCH-002 | `docs-agent` | `AGENTS.md` v0.2 (417 líneas) |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: AGENTS.md v0.1 + listado completo del repo.
- **Output esperado**: AGENTS.md v0.2 con 6 rutas corregidas y 15 artefactos nuevos referenciados.

### 6.2 Caso borde
- **Input**: artefacto nuevo en `docs/` (p. ej. `docs/LFSD-EduSync.md`) no en la estructura esperada.
- **Output esperado**: el agente actualiza el árbol de estructura y §2 orden de lectura.

### 6.3 Caso adversarial
- **Input**: solicitud de eliminar referencias a BRD v1 ya superado.
- **Comportamiento esperado**: BRD v1 se marca como `[OBSOLETO]` pero no se elimina (trazabilidad histórica).

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 17/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
