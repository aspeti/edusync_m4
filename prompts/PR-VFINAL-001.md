# PR-VFINAL-001 — Freeze documental de BRD/MRD/PRD/FSD para release/2.0.0

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-VFINAL-001` |
| Título | Freeze documental atómico de los 4 artefactos canónicos (BRD, MRD, PRD, FSD) hacia aliases `_vFinal.md` inmutables para la entrega `release/2.0.0` |
| Artefacto origen | `docs/brd/BRD_EduSync_v2.md` + `docs/mrd/MRD_EduSync.md` + `docs/prd/PRD_EduSync.md` + `docs/fsd/FSD_EduSync.md` |
| ID origen | `BRD v2`, `MRD v1.0`, `PRD v1.0`, `FSD v1.0` |
| Tipo de prompt | transformación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Documentation Curator + Release Manager del grupo G-EduSync.
Tu única función es congelar documentos canónicos a snapshots
inmutables `_vFinal.md` para que la rúbrica del Módulo 4 pueda auditar
la entrega `release/2.0.0` sin depender de ediciones posteriores.
Operas sobre `docs/brd/`, `docs/mrd/`, `docs/prd/` y `docs/fsd/`.
No tomas decisiones de contenido; eres un transformador determinístico.
```

### 1.2 Task
```text
Genera 4 archivos `_vFinal.md` (uno por documento canónico) siguiendo
la convención de nombre `<doc>_vFinal.md`. Cada destino es la copia
literal del documento canónico fuente, precedida por un banner uniforme
de freeze. No se modifica ningún otro archivo del repo en esta tarea.

Destinos:
1. docs/brd/BRD_EduSync_vFinal.md  ← docs/brd/BRD_EduSync_v2.md
2. docs/mrd/MRD_EduSync_vFinal.md  ← docs/mrd/MRD_EduSync.md
3. docs/prd/PRD_EduSync_vFinal.md  ← docs/prd/PRD_EduSync.md
4. docs/fsd/FSD_EduSync_vFinal.md  ← docs/fsd/FSD_EduSync.md
```

### 1.3 Context
```text
- Documentos fuente canónicos:
  - docs/brd/BRD_EduSync_v2.md   (BRD v2.0)
  - docs/mrd/MRD_EduSync.md      (MRD v1.0)
  - docs/prd/PRD_EduSync.md      (PRD v1.0)
  - docs/fsd/FSD_EduSync.md      (FSD v1.0)
- Release: release/2.0.0
- Fecha de freeze: 28/05/2026
- Autor: Rodrigo Aspeti
- Overwrite: false
- Banner uniforme: bloque Markdown inicial con fuente canónica, versión
  congelada, fecha, release, prompt origen y agente.
```

### 1.4 Reasoning
```text
1. Validar que existen exactamente 4 pares source→target.
2. Validar que cada source existe y que el target no existe si
   `overwrite=false`.
3. Leer cada source completo sin modificarlo.
4. Escribir cada target con banner uniforme + separador `---` + contenido
   literal del source.
5. Verificar conteos normativos por prefijo: BR/RB/KPI para BRD,
   MRD-N para MRD, PRD-REQ para PRD, FSD-UC para FSD.
6. Confirmar que los sources no fueron modificados y reportar tabla de
   resultado.
```

### 1.5 Stop condition
```text
Detente cuando existan los 4 `_vFinal.md`, todos empiecen con banner de
freeze, los conteos normativos target >= source y no se haya modificado
ningún documento fuente.
```

### 1.6 Output
```text
4 aliases:
- docs/brd/BRD_EduSync_vFinal.md
- docs/mrd/MRD_EduSync_vFinal.md
- docs/prd/PRD_EduSync_vFinal.md
- docs/fsd/FSD_EduSync_vFinal.md

Reporte tabular source → target con líneas y conteos normativos.
```

## 2. Invariantes del prompt

- Cada target debe comenzar con un banner uniforme de freeze.
- Cada target reside en la misma carpeta que su source.
- Ningún source debe modificarse.
- No se toca ningún archivo fuera de los 4 targets durante la ejecución del freeze.
- Los conteos normativos de cada target deben ser iguales o mayores que los del source; la diferencia esperada sólo puede venir del banner.
- Cero PII introducida por el banner.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_INVENTARIO_INCOMPLETO` | No hay exactamente 4 pares source→target | STOP; completar INPUT |
| `E_MISSING_SOURCE` | Algún source no existe | STOP; corregir ruta |
| `E_TARGET_EXISTS` | Target existe con overwrite=false | STOP; decidir overwrite |
| `E_PATH_MISMATCH` | Target no está en la misma carpeta del source | STOP; corregir destino |
| `E_NORMATIVE_DRIFT` | Target pierde IDs normativos del source | Revertir target |
| `E_BANNER_MISSING` | Target no inicia con banner de freeze | Revertir target |
| `E_CANONICO_MUTADO` | Source modificado durante el freeze | Revertir lote |
| `E_OUT_OF_SCOPE_EDIT` | Se tocó archivo fuera de los 4 targets | Revertir |

## 4. Guardrails

- **MUST**: validar rutas antes de escribir.
- **MUST**: preservar el contenido del source debajo del banner.
- **MUST**: emitir reporte con los 4 resultados.
- **MUST NOT**: editar los sources.
- **MUST NOT**: editar `PROMPT_MAPPING.md`, `AGENTS.md` o `DTI.md` dentro del freeze; esa propagación corre después con `sync-doc-chain`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| BRD canónico | `docs/brd/BRD_EduSync_v2.md` | PR-VFINAL-001 | `docs-agent` | `docs/brd/BRD_EduSync_vFinal.md` |
| MRD canónico | `docs/mrd/MRD_EduSync.md` | PR-VFINAL-001 | `docs-agent` | `docs/mrd/MRD_EduSync_vFinal.md` |
| PRD canónico | `docs/prd/PRD_EduSync.md` | PR-VFINAL-001 | `docs-agent` | `docs/prd/PRD_EduSync_vFinal.md` |
| FSD canónico | `docs/fsd/FSD_EduSync.md` | PR-VFINAL-001 | `docs-agent` | `docs/fsd/FSD_EduSync_vFinal.md` |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: 4 sources existen, ningún target existe, `overwrite=false`.
- **Output esperado**: 4 aliases `_vFinal.md` con banner y sources intactos.

### 6.2 Caso borde
- **Input**: target existe y `overwrite=false`.
- **Comportamiento esperado**: `E_TARGET_EXISTS`.

### 6.3 Caso adversarial
- **Input**: solicitud de corregir contenido normativo durante el freeze.
- **Comportamiento esperado**: rechazo; primero se corrige el canónico y luego se regenera el alias.

## 7. Instrumentación

- Herramienta: Langfuse / OpenTelemetry.
- Métricas: `success_rate`, `n_targets_creados`, `n_bytes_copiados`, `normative_drift_count`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 28/05/2026 | Rodrigo Aspeti | Creación — freeze de BRD/MRD/PRD/FSD para `release/2.0.0` | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado y ejecutado en el mismo flujo |
