# Plan E2E — Gestión Escolar (ADMIN)

## Resumen
Lista, alta, Periodos y Secciones anidados.

## Auditoría del plan
| Caso | Veredicto | Motivo |
|------|-----------|--------|
| 1.1 Alta gestión | aceptado | F12 |
| 1.2 Abrir Periodos | aceptado | F13 |
| 1.3 Abrir Secciones | aceptado | F14 |
| 1.4 Verificar seed 3 trimestres texto exacto | corregido | verificar heading/lista visible, no copy fijo |

### 1.1 Alta Gestión Escolar
**Pasos:** Login ADMIN → Gestión Escolar → Nueva → nombre+fechas → Crear
**Resultado esperado:** lista gestiones; nombre visible

### 1.2 Periodos
**Pasos:** Login ADMIN → Gestión Escolar → primer enlace Periodos
**Resultado esperado:** URL contiene /periodos; contenido de periodos visible

### 1.3 Secciones
**Pasos:** Login ADMIN → Gestión Escolar → primer enlace Secciones
**Resultado esperado:** URL contiene /secciones
