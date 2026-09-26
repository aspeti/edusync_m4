# Plan E2E — Cursos y Materias (ADMIN)

## Auditoría del plan
| Caso | Veredicto | Motivo |
|------|-----------|--------|
| 1.1 Alta Curso | aceptado | F16 (ya caso_admin_3_1) |
| 1.2 Paralelos de un curso | aceptado | F17 |
| 2.1 Alta Materia | aceptado | F19 |
| 2.2 Asignar profesor con CSS nth | descartado | localizador frágil |

### 1.2 Paralelos
**Pasos:** Login ADMIN → Cursos → primer enlace a paralelos / Ver
**Resultado esperado:** URL contiene /paralelos

### 2.1 Alta Materia
**Pasos:** Login ADMIN → Materias → Nueva → nombre único → Crear
**Resultado esperado:** detalle materia (heading con nombre) o lista
