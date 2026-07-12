# Prompt para Crear un Skill EduSync — Conforme al Template del Módulo 4

> **Cómo usarlo:** Abre Claude Code, Cursor o Claude Desktop con el repositorio EduSync montado.
> Pega el prompt completo en el chat. Reemplaza los placeholders `<...>` antes de enviar.
> El agente creará el Skill en `.claude/skills/<slug>/SKILL.md` (y opcionalmente en `.cursor/skills/<slug>/SKILL.md`).

---

## Metadatos del prompt

| Campo            | Valor                                                                 |
|------------------|-----------------------------------------------------------------------|
| ID del prompt    | `PR-SKILL-001`                                                        |
| Título           | Generador de Skills conformes al template EduSync                     |
| Artefacto origen | `plantillas/SKILL_TEMPLATE.md` + `AGENTS.md`                    |
| Tipo de prompt   | generación                                                            |
| Modelo sugerido  | Claude Sonnet                                                         |
| Temperatura      | 0.2                                                                   |
| Versión          | v1.0                                                                  |
| Fecha            | 28/05/2026                                                            |
| Estado           | Aprobado                                                              |

---

## Role

Eres un ingeniero de software senior especializado en el diseño de Skills para agentes de IA
(Claude Code / Cursor) en el contexto del proyecto **EduSync** — plataforma SaaS B2B multitenant
para gestión académica en unidades educativas bolivianas (stack: Java 21, Spring Boot 3.3,
PostgreSQL 15 con RLS, Angular 17, AWS ECS Fargate, arquitectura hexagonal Ports & Adapters).

Conoces a fondo:
- La plantilla canónica de Skills del módulo: `plantillas/SKILL_TEMPLATE.md`.
- El contrato de agentes del repositorio: `AGENTS.md`.
- Los 5 FSD-UC implementables: FSD-UC-001, FSD-UC-003, FSD-UC-004, FSD-UC-005, FSD-UC-009.
- Las 12 reglas de negocio vigentes: BR-001..BR-012.
- Los 5 NFRs medibles del FSD (latencia, uptime, seguridad, auditoría, exportación SIE).
- Los 4 golden tests de zero-tolerance: `FloorTest`, `SIEPayloadTest`, `VentanaTest`, `MultitenantTest`.

Tu única fuente de verdad es el repositorio EduSync. No inventas reglas ni artefactos.

---

## Task

Crea un Skill completo para el proyecto EduSync que cumpla **todos** los requisitos de la
`plantillas/SKILL_TEMPLATE.md` y sea compatible con los guardrails de `AGENTS.md`.

El Skill a crear es:

- **Nombre del Skill (slug kebab-case):** `<slug-del-skill>`
- **Propósito en una frase:** `<qué hace el Skill — verbo + objeto + restricción>`
- **Artefacto FSD de entrada:** `<FSD-UC-NNN | BR-NNN | §sección del FSD | Gherkin>`
- **Tipo de salida:** `<código Java / migración Flyway / test JUnit / diagrama Mermaid / documento>`
- **Agente que lo usa:** `<dev-agent | arch-agent | docs-agent | qa-agent | process-agent | compliance-agent>`

Si alguno de estos datos no está especificado, DETENTE y responde:
`"Necesito <campo faltante> antes de generar el Skill."`

---

## Context

### Estructura obligatoria del SKILL.md (plantillas/SKILL_TEMPLATE.md)

El archivo generado MUST seguir exactamente esta estructura de 10 secciones:

```
---                          ← frontmatter YAML obligatorio
name: <slug-kebab-case>
description: >
  <frase concreta: CUÁNDO usar, artefacto FSD de entrada, stack, tipo de salida>
  <NO usar "ayudar"; usar "implementar", "generar", "validar", "convertir">
allowed-tools:
  - read
  - edit
  - run-tests              ← incluir solo si el Skill ejecuta tests
model-tier: sonnet         ← haiku | sonnet | opus
fsd-version-min: v1.0      ← versión mínima del FSD que consume
status: stable             ← draft | stable | deprecated
owner: G-EduSync
---

## 1. Cuándo activarlo (triggers)
## 2. Entradas obligatorias (Inputs)
## 3. Fuentes de verdad (orden de precedencia)
## 4. Procedimiento
## 5. Salida esperada
## 6. Verificación (criterios de "bien hecho")
## 7. Anti-patrones específicos
## 8. Mini ejemplo de invocación
## 9. Modos de fallo conocidos
## 10. Registro de cambios del Skill
```

### Restricciones del dominio EduSync que el Skill MUST respetar

Cada Skill generado MUST verificar y nunca violar las siguientes reglas:

| Regla       | Descripción                                                                                           |
|-------------|-------------------------------------------------------------------------------------------------------|
| BR-001      | La nómina de estudiantes es de solo lectura para DOCENTE.                                             |
| BR-002      | Rango paramétrico de dimensiones validado en `CalificacionDomainService`, nunca en el controller.     |
| BR-004      | Vincular calificación al estudiante solo por código RUDE. MUST NOT usar nombre, apellido, posición.   |
| BR-005      | Correcciones retroactivas: append-only. El registro original es inmutable.                            |
| BR-008      | `Math.floor()` solo en `ConsolidacionDomainService`. MUST NOT usar `round()`, `HALF_UP`, ni SQL.     |
| BR-009      | `AutorizacionCorreccion` tiene `ventana_fin` máximo 72 h. Revocación automática.                      |
| BR-010      | `audit_log` se escribe en la MISMA transacción que cada escritura. El log es inmutable.               |
| DA-01       | Multitenancy: `SET LOCAL app.tenant_id` antes de cada TX. RLS en toda tabla nueva.                   |
| DA-03       | `audit_log` solo se escribe desde `AuditLogAspect`. MUST NOT UPDATE/DELETE en audit_log.             |
| DA-04       | Consolidación asíncrona vía Spring Events → AWS SQS.                                                  |
| DA-05       | SIE: circuit breaker Resilience4j, timeout 30 s, reintentos con backoff.                              |
| AGENTS §7   | MUST NOT registrar RUDE, password, token, JWT en logs. Solo `id` interno.                            |
| AGENTS §5   | Código en inglés. Docs en español. Google Java Style. Hexagonal estricta.                             |

### Convenciones de paths del repositorio EduSync

| Elemento              | Path canónico                                                      |
|-----------------------|--------------------------------------------------------------------|
| Skill nuevo           | `.claude/skills/<slug>/SKILL.md`                                   |
| Skill (Cursor)        | `.cursor/skills/<slug>/SKILL.md`                                   |
| BRD (versionado)      | `docs/brd/BRD_EduSync_v<N>.md` — leer siempre el mayor N          |
| FSD (versionado)      | `docs/fsd/FSD_EduSync[_v<N>].md` — leer siempre el mayor N        |
| PRD (versionado)      | `docs/prd/PRD_EduSync[_v<N>].md` — leer siempre el mayor N        |
| MRD (versionado)      | `docs/mrd/MRD_EduSync[_v<N>].md` — leer siempre el mayor N        |
| LFSD (versionado)     | `docs/LFSD-EduSync[_v<N>].md` — leer siempre el mayor N           |
| DTI (versionado)      | `docs/DTI[_v<N>].md` — leer siempre el mayor N                    |
| AGENTS.md             | `AGENTS.md`                                                   |
| Prompt mapping        | `docs/PROMPT_MAPPING.md`                                           |
| Controllers           | `src/infrastructure/web/`                                          |
| Domain services       | `src/domain/`                                                      |
| Application use cases | `src/application/`                                                 |
| Tests                 | `tests/unit/` y `tests/integration/`                               |
| Migraciones Flyway    | `src/infrastructure/persistence/migration/`                        |
| Diagramas             | `docs/diagrams/`                                                   |

### Regla de versión más reciente

Los documentos de especificación se versionan con el sufijo `_v<N>` (entero creciente).
Cuando existan múltiples versiones de un mismo documento, **siempre leer la de mayor N**.
El procedimiento para determinarlo es:

```
1. Listar todos los archivos del directorio correspondiente (ej. `docs/brd/`).
2. Filtrar los que coincidan con el patrón `<Nombre>_v<número>.md`.
3. Seleccionar el archivo con el número de versión más alto.
4. Si no existe sufijo de versión (archivo único, ej. `FSD_EduSync.md`), leer ese archivo.
5. Registrar en §10 del Skill la versión del documento leído (ej. "basado en BRD v2, FSD v1").
```

Si durante la ejecución aparece un documento con versión superior al que se leyó inicialmente,
DETENER, releer el documento actualizado y revisar si algún paso ya ejecutado debe corregirse.

---

## Reasoning

Sigue estos pasos en orden al generar el SKILL.md:

1. **Descubrir y leer la versión más reciente de cada artefacto de contexto** (en este orden):

   a. `AGENTS.md` — siempre un único archivo; leerlo completo.

   b. **BRD más reciente** — listar `docs/brd/`, seleccionar el archivo con el mayor número
      de versión (patrón `BRD_EduSync_v<N>.md`). Actualmente: `BRD_EduSync_v2.md`.
      Si existe `v3` o superior, leer esa en su lugar.

   c. **FSD más reciente** — listar `docs/fsd/`, seleccionar el mayor N de
      `FSD_EduSync[_v<N>].md`. Actualmente: `FSD_EduSync.md` (sin sufijo = v1).
      Si aparece `FSD_EduSync_v2.md` o superior, leer esa.

   d. **PRD más reciente** — listar `docs/prd/`, misma lógica.
      Actualmente: `PRD_EduSync.md`.

   e. **MRD más reciente** — listar `docs/mrd/`, misma lógica.
      Actualmente: `MRD_EduSync.md`.

   f. **LFSD más reciente** — listar `docs/`, seleccionar el mayor N de
      `LFSD-EduSync[_v<N>].md`. Actualmente: `LFSD-EduSync.md`.

   g. **DTI más reciente** — listar `docs/`, seleccionar el mayor N de
      `DTI[_v<N>].md`. Actualmente: `DTI.md`.

   h. `plantillas/SKILL_TEMPLATE.md` — estructura exacta del archivo a producir.

   > **Regla de parada:** si al listar los directorios se encuentran versiones superiores
   > a las indicadas en los ejemplos anteriores, leer siempre las de mayor N y anotar
   > las versiones efectivamente leídas en el paso 11 (registro de cambios del Skill §10).

2. **Verificar coherencia entre versiones leídas:**
   - Confirmar que el BRD más reciente no introduce BR nuevas que contradigan el FSD leído.
   - Si hay contradicción (ej. BRD v3 agrega BR-013 que el FSD v1 no contempla): STOP —
     indicar la discrepancia y pedir aclaración antes de continuar.
   - Anotar las versiones efectivamente leídas; se usarán en §10 (Registro de cambios del Skill).

3. **Construir el frontmatter YAML:**
   - `name`: slug kebab-case único, sin espacios.
   - `description`: una frase "pushy" que incluya: cuándo activar, artefacto FSD de entrada, stack y tipo de salida. Verbo activo (implementar/generar/validar).
   - `allowed-tools`: solo las herramientas que el Skill realmente usa.
   - `model-tier`: `sonnet` por defecto salvo que el Skill sea exclusivamente de lectura simple (haiku) o diseño arquitectónico complejo (opus).
   - `fsd-version-min: v1.0` — los FSD del proyecto están en v1.0+.
   - `status: stable` si el Skill cubre un FSD-UC completo; `draft` si es parcial.
   - `owner: G-EduSync`.

4. **Redactar la sección §1 — Cuándo activarlo:**
   - DURANTE: indicar la fase del SDLC (implementación / refactor / test / revisión de PR / migración / diseño).
   - ARRANCA cuando: condición textual concreta (ej. "el usuario cita FSD-UC-001 y pide código backend").
   - NO ACTIVAR cuando: especificar fases incompatibles (ej. "durante definición BRD/MRD/PRD").

5. **Redactar la sección §2 — Entradas obligatorias:**
   - Listar los artefactos FSD mínimos necesarios (ID UC, sección del diccionario, Gherkin, NFR).
   - Indicar qué mensaje devuelve el Skill si falta alguno.

6. **Redactar la sección §3 — Fuentes de verdad:**
   - Orden de precedencia obligatorio para EduSync:
     1. UC/BR/Gherkin/NFR citado del FSD (versión más reciente leída en paso 1).
     2. LFSD-EduSync.md versión más reciente (versión leída en paso 1).
     3. `AGENTS.md` y ADRs vigentes.
     4. Código existente en el repo (convenciones de nombres y paquetes).

7. **Redactar la sección §4 — Procedimiento:**
   - Pasos numerados y concretos.
   - Incluir: verificar trazabilidad FSD → código → test.
   - Incluir: respetar arquitectura hexagonal (domain/ sin deps Spring).
   - Incluir: actualizar `docs/PROMPT_MAPPING.md` si el Skill genera un prompt-contrato nuevo.
   - Incluir: verificar golden tests si el Skill toca consolidación, exportación SIE, ventanas o multitenancy.

8. **Redactar la sección §5 — Salida esperada:**
   - Lista de archivos creados o modificados.
   - Tabla de trazabilidad obligatoria con columnas: `FSD ID | Archivo implementación | Test que lo verifica`.

9. **Redactar la sección §6 — Verificación:**
   - Criterios binarios (checklist) de "bien hecho".
   - MUST incluir: linter verde (`mvn checkstyle:check`), tests pasan (`mvn test`), cobertura ≥ 80% en domain/ y application/.
   - Si el Skill toca `ConsolidacionDomainService`: incluir `FloorTest` en el checklist.
   - Si el Skill toca exportación SIE: incluir `SIEPayloadTest`.
   - Si el Skill toca ventanas de corrección: incluir `VentanaTest`.
   - Si el Skill crea tablas nuevas: incluir `MultitenantTest`.

10. **Redactar la sección §7 — Anti-patrones:**
    - Al menos 3 anti-patrones específicos del dominio EduSync.
    - Cada anti-patrón con su mitigación concreta.
    - Incluir siempre: floor() fuera de ConsolidacionDomainService, PII en logs, entidades JPA expuestas en API.

11. **Redactar §8 — Mini ejemplo, §9 — Modos de fallo, §10 — Changelog:**
    - §8: ejemplo de invocación en una sola línea de comando con UC y path reales.
    - §9: al menos 2 modos de fallo con instrucción de STOP y acción correctiva.
    - §10: tabla con versión 0.1.0, fecha actual, autor `G-EduSync`, cambio "versión inicial"
      **y** columna adicional "Documentos base" donde se listan las versiones efectivamente
      leídas en el paso 1 (ej. `BRD v2, FSD v1, PRD v1, LFSD v1`).

12. **Verificar coherencia interna:**
    - ¿El `description` del frontmatter coincide con §1 (triggers)?
    - ¿Las entradas en §2 son suficientes para el procedimiento en §4?
    - ¿La salida en §5 cubre todos los pasos de §4?
    - ¿El checklist en §6 verifica todos los criterios mencionados en §4?
    - ¿El §10 registra las versiones de documentos leídas en el paso 1?

---

## Stop Condition

Detente cuando:

- Exista el archivo `.claude/skills/<slug>/SKILL.md` con las 10 secciones completas y no vacías.
- El frontmatter YAML sea válido y contenga los 8 campos obligatorios.
- La tabla de trazabilidad en §5 cubre todos los FSD-UC / BR / NFR que el Skill implementa.
- El checklist de §6 incluye los golden tests relevantes al Skill.
- §7 tiene al menos 3 anti-patrones específicos del dominio EduSync (no genéricos).
- El archivo tiene entre 80 y 220 líneas (ni esqueleto vacío, ni novel).
- §10 (Registro de cambios) incluye la columna "Documentos base" con las versiones exactas
  de BRD, FSD, PRD, MRD, LFSD y DTI que se leyeron en el paso 1 del Reasoning.

No generes archivos adicionales en esta primera versión (sin referencia.md, sin scripts de soporte).
No copies el contenido del SKILL.md como texto en el chat — solo confirma el path creado.

---

## Output

**Formato:** un único archivo `.claude/skills/<slug>/SKILL.md`.

**Estructura interna requerida:**

```markdown
---
name: <slug>
description: >
  <descripción pushy: cuándo, entrada FSD, stack, tipo salida>
allowed-tools:
  - read
  - edit
  - run-tests
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: <Título legible — máx 8 palabras>

> Skill canónica del proyecto EduSync. Para activarla copiar esta carpeta a
> `.claude/skills/<slug>/` en la raíz del repositorio del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: <fase del SDLC>
- ARRANCA cuando: <condición concreta con artefacto FSD>
- NO ACTIVAR cuando: <fases incompatibles>

## 2. Entradas obligatorias

<lista de artefactos FSD mínimos + mensaje de error si faltan>

## 3. Fuentes de verdad (orden de precedencia)

1. <UC/BR/Gherkin citado del FSD>
2. LFSD-EduSync.md (contratos API, DDL, paquetes)
3. AGENTS.md y ADRs vigentes
4. Código existente del repo

## 4. Procedimiento

<pasos numerados concretos — mínimo 4, máximo 8>

## 5. Salida esperada

<lista de archivos + tabla de trazabilidad FSD ID | Implementación | Test>

## 6. Verificación (criterios de "bien hecho")

<checklist binario — al menos 5 ítems, incluir golden tests relevantes>

## 7. Anti-patrones específicos

<tabla o lista: anti-patrón → mitigación — mínimo 3, con BR/DA referenciado>

## 8. Mini ejemplo de invocación

> "<comando de una sola línea con UC y path reales del proyecto EduSync>"

## 9. Modos de fallo conocidos

<al menos 2: código de error → STOP → acción correctiva>

## 10. Registro de cambios del Skill

| Versión | Fecha      | Autor      | Cambio          | Documentos base                    |
|---------|------------|------------|-----------------|------------------------------------|
| 0.1.0   | 28/05/2026 | G-EduSync  | versión inicial | BRD v?, FSD v?, PRD v?, LFSD v?   |
```

---

## Invariants

- El SKILL.md MUST tener frontmatter YAML válido con los 8 campos (name, description, allowed-tools, model-tier, fsd-version-min, status, owner).
- El SKILL.md MUST tener las 10 secciones numeradas exactamente como en `plantillas/SKILL_TEMPLATE.md`.
- La `description` del frontmatter MUST mencionar el artefacto FSD de entrada y el tipo de salida. MUST NOT usar el verbo "ayudar".
- El Skill MUST NOT inventar reglas de negocio, artefactos FSD ni IDs de BR que no estén en el repositorio.
- El Skill MUST NOT proponer código que viole los guardrails de `AGENTS.md §8` (prompts prohibidos §11).
- La tabla de trazabilidad en §5 MUST cubrir todos los FSD-UC / BR / NFR que el procedimiento §4 toca.
- §7 MUST tener al menos 3 anti-patrones específicos del dominio EduSync con referencia a BR/DA.
- El archivo resultante MUST tener entre 80 y 220 líneas.
- El Skill MUST guardarse en `.claude/skills/<slug>/SKILL.md`. Si el directorio no existe, crearlo.

---

## Failure Modes

| Código                   | Descripción                                                                 | Acción                                              |
|--------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------|
| `E_MISSING_INPUT`        | Falta slug, propósito, artefacto FSD, tipo de salida o agente target        | STOP — pedir el dato faltante antes de continuar    |
| `E_NONEXISTENT_FSD_ID`   | El UC, BR o NFR citado no existe en FSD_EduSync.md ni en BRD_EduSync_v2.md | STOP — indicar qué ID no existe y pedir corrección  |
| `E_INVARIANT_VIOLATION`  | El procedimiento del Skill propuesto viola una regla de AGENTS.md §11       | STOP — señalar la regla violada y reformular        |
| `E_MISSING_SECTION`      | El SKILL.md generado no tiene alguna de las 10 secciones                    | Completar la sección faltante antes de guardar      |
| `E_PLACEHOLDER_REMAINING`| El SKILL.md contiene `<placeholder>` sin reemplazar                         | Sustituir con valor real del dominio EduSync        |
| `E_TOO_LONG`             | El SKILL.md supera 220 líneas                                               | Condensar §4 y §7 usando tablas compactas           |
| `E_YAML_INVALID`         | El frontmatter YAML no es sintácticamente válido                             | Corregir sintaxis antes de guardar                  |
| `E_DUPLICATE_SKILL`      | Ya existe `.claude/skills/<slug>/SKILL.md` con el mismo nombre              | Preguntar si actualizar o crear con slug alternativo|

---

## Guardrails adicionales

- **MUST NOT** incluir secretos, tokens, contraseñas ni el campo `rude` de estudiantes en ninguna sección del Skill.
- **MUST NOT** generar código de ejemplo que calcule promedios fuera de `ConsolidacionDomainService`.
- **MUST NOT** generar código de ejemplo que exponga entidades JPA directamente en la API (sin DTO).
- **MUST NOT** proponer migraciones Flyway que modifiquen versiones ya aplicadas en `main`.
- **MUST** actualizar `docs/PROMPT_MAPPING.md` si el Skill introduce un prompt-contrato nuevo.
- **MUST** crear también `.cursor/skills/<slug>/SKILL.md` con el mismo contenido si el usuario trabaja con Cursor.

---

## Trazabilidad

| Origen           | ID origen              | Este prompt   | Consumidor   | Artefacto generado                         |
|------------------|------------------------|---------------|--------------|--------------------------------------------|
| SKILL_TEMPLATE   | plantillas/SKILL_TEMPLATE.md | PR-SKILL-001 | dev-agent / arch-agent | `.claude/skills/<slug>/SKILL.md` |
| AGENTS.md        | AGENTS.md §8      | PR-SKILL-001  | todos los agentes | Guardrails aplicados en el Skill generado |
| FSD              | docs/fsd/FSD_EduSync.md | PR-SKILL-001 | dev-agent    | Procedimiento y trazabilidad en §4 y §5    |

---

## Pruebas del prompt

### Caso feliz

- **Input:** `slug: registrar-calificacion-uc001 | propósito: implementar FSD-UC-001 en Spring Boot 3 | entrada: FSD-UC-001 con Gherkin | salida: vertical slice Java | agente: dev-agent`
- **Output esperado:** `.claude/skills/registrar-calificacion-uc001/SKILL.md` con 10 secciones, frontmatter válido, tabla de trazabilidad FSD-UC-001 → implementación → test, golden test `FloorTest` en §6, anti-patrón floor() fuera del dominio en §7.

### Caso borde

- **Input:** slug válido, pero propósito que afecta a 3 FSD-UC simultáneamente.
- **Output esperado:** Skill con §2 listando los 3 UC como entrada obligatoria y tabla de trazabilidad con 3 filas en §5. Si supera 220 líneas, condensar usando tablas en §4 y §7.

### Caso adversarial

- **Input:** propósito que pide calcular el promedio en el controller REST.
- **Comportamiento esperado:** `E_INVARIANT_VIOLATION` — señalar BR-008 y AGENTS.md §11, reformular el propósito para delegar el cálculo a `ConsolidacionDomainService`.

---

## Versionado

| Versión | Fecha      | Autor         | Cambio                                      | Modelo validado |
|---------|------------|---------------|---------------------------------------------|-----------------|
| v1.0    | 28/05/2026 | Rodrigo Aspeti | Creación inicial — alineado a SKILL_TEMPLATE y AGENTS.md v0.2 | Claude Sonnet |

---

## Checklist para dar de alta el prompt

- [x] Metadatos completos.
- [x] 6 elementos anatómicos (Role, Task, Context, Reasoning, Stop condition, Output).
- [x] Invariants declaradas.
- [x] Failure modes declarados.
- [x] 3 pruebas mínimas (feliz, borde, adversarial).
- [x] Trazabilidad al artefacto origen (SKILL_TEMPLATE.md, AGENTS.md, FSD).
- [x] Guardrails de dominio EduSync explícitos.
- [ ] Revisión humana pendiente — ver §9.

---

## Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| G-EduSync | | pendiente | |
