---
name: fsd-modelo-datos-a-jpa-flyway
description: >
  Implementación de §6 del FSD (ER + diccionario de datos) a entidades JPA y
  migraciones Flyway alineadas al diccionario, con índices, constraints y seed
  mínimo. Aplica a sistemas académicos / administrativos UMSS sobre PostgreSQL.
  No inventa atributos; si falta detalle, marca GAP-FSD.
allowed-tools:
  - read
  - edit
  - run-tests
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: §6 FSD → entidades JPA + Flyway

> Skill canónica del módulo. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/fsd-modelo-datos-a-jpa-flyway/` o a
> `.claude/skills/fsd-modelo-datos-a-jpa-flyway/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: implementación inicial del modelo de datos del producto, o adición de campos / entidades nuevas.
- ARRANCA cuando: el usuario cita §6 del FSD o adjunta el diagrama ER + diccionario y pide modelado.
- NO ACTIVAR cuando: el usuario está diseñando el modelo desde cero (eso pertenece al FSD; este Skill ejecuta lo ya especificado).

## 2. Entradas obligatorias

- §6.1 diagrama ER (Mermaid) del FSD.
- §6.2 diccionario de datos del FSD: `entidad | atributo | tipo | obligatorio | validaciones | origen`.
- Versión de PostgreSQL del proyecto (declarada en `AGENTS.md §4`).

Si falta cualquiera, responder: "Falta <X>; sin diccionario o ER no implemento entidades."

## 3. Fuentes de verdad (orden de precedencia)

1. §6.2 diccionario de datos del FSD (autoritativo en tipos y validaciones).
2. §6.1 diagrama ER del FSD (autoritativo en cardinalidades y relaciones).
3. `AGENTS.md` (stack, convenciones de nombres, capas).
4. Esquema actual del repo si ya existe (no romper migraciones aplicadas).

## 4. Procedimiento

1. Para cada entidad del diccionario:
   - Crear `@Entity` en `domain` o `adapter/out/persistence` según convención del repo.
   - Mapear tipos según el diccionario:
     - `string(N)` → `VARCHAR(N)`.
     - `UUID` → `uuid` (PostgreSQL nativo).
     - `decimal(p,s)` → `numeric(p,s)`.
     - Fechas → `timestamptz`, salvo que el FSD diga `date` explícito.
   - Anotar `nullable`, `length`, `unique`, `precision/scale` exactamente como diga el diccionario.
2. Generar migración Flyway `V<N>__<entidad>.sql`:
   - Tabla con tipos exactos del paso 1.
   - PK (`id` como `uuid` por defecto), FKs con `ON DELETE` razonable.
   - `UNIQUE` y `CHECK` que reflejen las validaciones del diccionario.
   - Índices en columnas de búsqueda obvia (FKs, columnas de filtro listadas en los UCs).
3. Para cada relación del ER:
   - `||--o{` (uno a muchos) → FK + index.
   - `}o--o{` (muchos a muchos) → tabla de unión con PK compuesta.
4. Seed mínimo (solo si el FSD lo pide explícitamente): catálogos de estados, roles, tipos de trámite, períodos académicos.
5. Test de migración:
   - `@DataJpaTest` con Testcontainers PostgreSQL.
   - Verificar persistencia de cada entidad y `roundtrip` (insert + read + assert).
   - Verificar que las constraints rechazan datos inválidos (longitud, nulos, regex si aplica).

## 5. Salida esperada

- Entidades JPA, repositorios Spring Data, migraciones Flyway numeradas y tests de persistencia.
- Tabla de trazabilidad al cerrar el PR:

| Diccionario §6.2                | Columna SQL                | Constraint / Validación             |
|---------------------------------|----------------------------|--------------------------------------|
| `Estudiante.numeroCarnet`       | `numero_carnet VARCHAR(15)`| `UNIQUE`, `NOT NULL`, `CHECK len > 0`|
| `Inscripcion.periodoAcademico`  | `periodo_academico VARCHAR(10)` | `NOT NULL`, FK a `periodo`     |

## 6. Verificación

- El diccionario es el "diff" canónico: cualquier discrepancia entre código, migración SQL y diccionario es un bug.
- Cada validación del diccionario aparece en al menos uno de:
  - BD (`CHECK`, `UNIQUE`, `NOT NULL`).
  - Entidad JPA (`@Size`, `@NotNull`, `@Pattern`).
  - Dominio (regla de negocio si aplica).
- No hay columnas en SQL que no estén en el diccionario.
- `mvn -Dtest=*JpaTest test` pasa con Testcontainers.

## 7. Anti-patrones del dominio universitario

- Modelar `numeroCarnet` como `int` (puede tener letra como `1A12345`). Usar `varchar`.
- FK ausente entre `Inscripcion` y `Materia` "para flexibilidad" — rompe integridad referencial.
- Faltar índice sobre `(idEstudiante, periodoAcademico)` en `Inscripcion`: queries lentas en publicación de horarios.
- Mezclar `monto` en `double` en vez de `numeric(10,2)` para pagos en BOB.
- Olvidar zona horaria en fechas académicas (publicación, ventana de matrícula).

## 8. Mini ejemplo de invocación

> "Implementa §6 del FSD del trámite 'Cambio de carrera' (`docs/fsd/cambio_carrera.md`). Genera entidades JPA, migraciones Flyway y tests de persistencia. Usa el Skill `fsd-modelo-datos-a-jpa-flyway`."

## 9. Modos de fallo conocidos

- El diccionario tiene un atributo sin tipo o sin validación → STOP, marcar `GAP-FSD §6.2.<entidad>.<atributo>` y pedir completar.
- El ER muestra cardinalidad ambigua (`}o--o{` cuando el dominio sugiere `||--o{`) → STOP, escalar al docente o autor del FSD.
- Conflicto con migración Flyway ya aplicada en `main` → STOP, generar migración aditiva (nueva versión `V<N+1>__alter_…`), nunca modificar la anterior.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 04/05/2026  | M.Sc. Edson Terceros   | versión inicial |
