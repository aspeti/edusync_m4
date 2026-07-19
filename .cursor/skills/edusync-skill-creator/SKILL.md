---
name: edusync-skill-creator
description: >
  Genera un nuevo SKILL.md para el proyecto EduSync cumpliendo exactamente la
  plantillas/SKILL_TEMPLATE.md y los guardrails de AGENTS.md. Activar cuando
  el usuario pide "crea un skill para FSD-UC-NNN", "genera el skill de <función>",
  "nuevo skill para <agente>". Requiere: slug, propósito, artefacto FSD de entrada,
  tipo de salida y agente target. Produce el archivo .claude/skills/<slug>/SKILL.md
  y su copia .cursor/skills/<slug>/SKILL.md listos para usar.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: edusync-skill-creator — Generador de Skills EduSync

> Skill canónica del proyecto EduSync. Copiar a `.claude/skills/edusync-skill-creator/`
> en la raíz del repositorio del grupo para activarla en Claude Code o Claude Desktop.
> Para Cursor: copiar también a `.cursor/skills/edusync-skill-creator/`.

---

## 1. Cuándo activarlo (triggers)

- **DURANTE**: cualquier fase del proyecto en que se necesite automatizar una tarea repetitiva
  mediante un agente (implementación, testing, documentación, revisión de PR, migración).
- **ARRANCA cuando**: el usuario pide crear un skill nuevo con cualquiera de estas frases:
  - "crea un skill para FSD-UC-NNN"
  - "genera el skill de <función>"
  - "nuevo skill para el agente <nombre>"
  - "quiero automatizar <tarea> con un skill"
- **NO ACTIVAR cuando**:
  - El usuario solo quiere ejecutar un skill ya existente (activar ese skill directamente).
  - El skill solicitado ya existe en `.claude/skills/` con el mismo slug (preguntar si actualizar).
  - El usuario está en fase de descubrimiento de negocio (BRD/MRD/PRD) sin FSD disponible.

---

## 2. Entradas obligatorias

El usuario DEBE proporcionar los siguientes 5 datos. Si falta alguno, responder:
`"Necesito <dato faltante> antes de generar el skill. Proporciona: slug, propósito, artefacto FSD de entrada, tipo de salida y agente target."`

| # | Dato | Descripción | Ejemplo |
|---|------|-------------|---------|
| 1 | **slug** | Nombre único en kebab-case, sin espacios | `registrar-calificacion-uc001` |
| 2 | **propósito** | Una frase: verbo + objeto + restricción | `Implementar FSD-UC-001 en Spring Boot 3` |
| 3 | **artefacto FSD de entrada** | ID del UC, BR, NFR o sección del FSD que consume el skill | `FSD-UC-001 con Gherkin y BR-002` |
| 4 | **tipo de salida** | Qué produce el skill | `vertical slice Java (controller, use case, domain, test)` |
| 5 | **agente target** | Qué agente lo usa | `dev-agent` |

---

## 3. Fuentes de verdad (orden de precedencia)

Antes de generar cualquier sección del skill nuevo, leer en este orden:

1. **Versión más reciente del FSD** — listar `docs/fsd/`, seleccionar el archivo
   `FSD_EduSync[_v<N>].md` con mayor N. Leer el UC/BR/NFR específico solicitado.
2. **Versión más reciente del BRD** — listar `docs/brd/`, seleccionar `BRD_EduSync_v<N>.md`
   con mayor N. Leer las BR que apliquen al skill.
3. **Versión más reciente del LFSD** — listar `docs/`, seleccionar `LFSD-EduSync[_v<N>].md`
   con mayor N. Leer contratos API, DDL y paquetes del componente afectado.
4. **Versión más reciente del PRD** — listar `docs/prd/`, seleccionar mayor N.
5. **Versión más reciente del MRD** — listar `docs/mrd/`, seleccionar mayor N.
6. **`AGENTS.md`** — stack autoritativo, guardrails, agentes, golden tests, prompts prohibidos.
7. **`plantillas/SKILL_TEMPLATE.md`** — estructura exacta de las 10 secciones a generar.

> **Regla de versión más reciente:** si al listar un directorio aparece un archivo con
> número de versión mayor al conocido (ej. `BRD_EduSync_v3.md` cuando se esperaba v2),
> leer siempre el de mayor N y anotar las versiones efectivamente leídas.

---

## 4. Procedimiento

### Paso 1 — Verificar que el slug no existe aún

```
Comprobar si existe .claude/skills/<slug>/SKILL.md.
  → Si existe: preguntar "¿Actualizar el skill existente o crear uno con slug alternativo?"
               Esperar respuesta antes de continuar.
  → Si no existe: continuar.
```

### Paso 2 — Leer los documentos más recientes

Ejecutar la búsqueda de versión más reciente para cada documento según §3.
Registrar las versiones encontradas; se usarán en §10 del skill generado.

### Paso 3 — Verificar coherencia entre versiones

Si el BRD más reciente introduce reglas (BR-NNN) que el FSD más reciente no contempla:
→ **STOP** — indicar la discrepancia y pedir aclaración antes de continuar.

### Paso 4 — Verificar que el artefacto FSD de entrada existe

Confirmar que el UC / BR / NFR solicitado está efectivamente en el FSD leído.
→ Si no existe: responder `E_NONEXISTENT_FSD_ID` y listar los IDs disponibles.

### Paso 5 — Generar el frontmatter YAML

```yaml
---
name: <slug>
description: >
  <frase concreta: CUÁNDO usar + artefacto FSD de entrada + stack + tipo de salida.
   Verbo activo (implementar/generar/validar/convertir). Sin "ayudar".>
allowed-tools:
  - read
  - edit
  - run-tests   # incluir solo si el skill ejecuta tests
model-tier: sonnet   # haiku si es solo lectura; opus si es diseño arq. complejo
fsd-version-min: v1.0
status: stable       # draft si el skill cubre el UC parcialmente
owner: G-EduSync
---
```

### Paso 6 — Generar las 10 secciones del SKILL.md

Redactar cada sección usando los datos leídos en el paso 2. Reglas por sección:

**§1 — Cuándo activarlo:**
- DURANTE: fase SDLC real (no genérica).
- ARRANCA cuando: citar el artefacto FSD de entrada exacto.
- NO ACTIVAR cuando: fases donde el artefacto FSD de entrada aún no existe.

**§2 — Entradas obligatorias:**
- Campos reales del FSD-UC: trazabilidad PRD-REQ, actor, precondiciones, flujo,
  postcondiciones, BR aplicables, datos in/out, Gherkin.
- Mensaje de error exacto si falta alguno.

**§3 — Fuentes de verdad:**
- Mismo orden de precedencia que §3 de este skill, particularizado al UC/BR del skill nuevo.

**§4 — Procedimiento:**
- Mínimo 4, máximo 8 pasos numerados y concretos.
- Incluir siempre: verificar trazabilidad FSD → código → test.
- Incluir siempre: arquitectura hexagonal (domain/ sin deps de Spring ni JPA).
- Si el skill toca ConsolidacionDomainService: añadir paso de validar Math.floor().
- Si el skill crea tablas nuevas: añadir paso de agregar tenant_id + política RLS.
- Si el skill toca audit_log: añadir paso de verificar escritura en la misma TX.
- Si el skill llama al SIE: añadir paso de circuit breaker + timeout + retry.

**§5 — Salida esperada:**
- Lista de archivos a crear/modificar con sus paths canónicos (ver AGENTS.md §3).
- Tabla de trazabilidad obligatoria:

  | FSD ID | Archivo de implementación | Test que lo verifica |
  |--------|--------------------------|----------------------|
  | <id>   | src/...                  | <Clase>#<método>     |

**§6 — Verificación:**
- mvn checkstyle:check verde (sin warnings nuevos).
- mvn test verde.
- Cobertura >= 80% en domain/ y application/ (mvn jacoco:report).
- Golden tests según lo que toca el skill:
  - Toca ConsolidacionDomainService → FloorTest.
  - Toca exportación SIE → SIEPayloadTest.
  - Toca ventanas de corrección → VentanaTest.
  - Crea tablas nuevas → MultitenantTest.

**§7 — Anti-patrones:**
- Mínimo 3, con referencia a BR/DA del proyecto. Incluir siempre:
  - Math.floor() fuera de ConsolidacionDomainService (BR-008).
  - PII (rude, nombre, fecha_nac) en logs (AGENTS.md §7).
  - Entidad JPA expuesta directamente en la API sin DTO (AGENTS.md §5).

**§8 — Mini ejemplo:**
- Una sola línea con UC y path reales del repositorio EduSync.

**§9 — Modos de fallo:**
- Mínimo 2: código de error → STOP → acción correctiva.

**§10 — Registro de cambios:**

  | Versión | Fecha | Autor | Cambio | Documentos base |
  |---------|-------|-------|--------|-----------------|
  | 0.1.0 | <hoy> | G-EduSync | versión inicial | BRD v?, FSD v?, PRD v?, LFSD v? |

  Rellenar "Documentos base" con las versiones efectivamente leídas en el paso 2.

### Paso 7 — Verificar coherencia interna del skill generado

- ¿El description del frontmatter coincide con §1 (triggers)?
- ¿Las entradas de §2 son suficientes para ejecutar §4?
- ¿La salida de §5 cubre todos los pasos de §4?
- ¿El checklist de §6 verifica todos los criterios de §4?
- ¿§10 tiene las versiones de documentos leídas en el paso 2?
- ¿El archivo tiene entre 80 y 220 líneas?

Si alguna verificación falla: corregir antes de guardar.

### Paso 8 — Guardar los archivos

```
Crear: .claude/skills/<slug>/SKILL.md   ← Claude Code / Claude Desktop
Crear: .cursor/skills/<slug>/SKILL.md   ← Cursor (mismo contenido)
```

Si el directorio no existe, crearlo. Confirmar al usuario el path exacto de cada archivo creado.

### Paso 9 — Actualizar PROMPT_MAPPING.md (condicional)

Si el skill nuevo introduce un prompt-contrato que no está en docs/PROMPT_MAPPING.md:
→ Agregar la fila correspondiente al catálogo.

---

## 5. Salida esperada

- `.claude/skills/<slug>/SKILL.md` — skill listo para Claude Code y Claude Desktop.
- `.cursor/skills/<slug>/SKILL.md` — skill listo para Cursor.
- Confirmación en chat con paths creados y versiones de documentos usadas.
- (Condicional) Fila nueva en `docs/PROMPT_MAPPING.md` si aplica.

---

## 6. Verificación (criterios de "bien hecho")

- [ ] El SKILL.md generado tiene las 10 secciones numeradas exactamente como en plantillas/SKILL_TEMPLATE.md.
- [ ] El frontmatter YAML tiene los 8 campos obligatorios y es sintácticamente válido.
- [ ] La description del frontmatter menciona el artefacto FSD de entrada y el tipo de salida.
- [ ] La tabla de trazabilidad en §5 cubre todos los FSD-UC / BR / NFR que el skill implementa.
- [ ] §6 incluye los golden tests relevantes al skill (FloorTest, SIEPayloadTest, VentanaTest, MultitenantTest).
- [ ] §7 tiene al menos 3 anti-patrones con referencia a BR/DA del proyecto EduSync.
- [ ] §10 registra las versiones exactas de BRD, FSD, PRD, LFSD leídas durante la generación.
- [ ] El archivo tiene entre 80 y 220 líneas.
- [ ] El skill se guardó en ambas rutas: .claude/skills/<slug>/ y .cursor/skills/<slug>/.

---

## 7. Anti-patrones específicos

| Anti-patrón | Por qué es un error | Mitigación |
|-------------|---------------------|------------|
| Generar un skill sin leer el FSD | El skill inventa reglas de negocio no existentes | STOP en paso 4 si el artefacto FSD no se encuentra |
| Fijar versiones de documentos en el skill generado (ej. BRD_v2) en vez de la regla "mayor N" | El skill generado queda desactualizado cuando sube el BRD v3 | En §3 del skill generado escribir la regla de descubrimiento, no el nombre fijo |
| Omitir la columna "Documentos base" en §10 | Imposible saber con qué versión del FSD fue diseñado el skill | El paso 7 verifica explícitamente esta columna antes de guardar |
| Crear el skill solo en .claude/skills/ y olvidar .cursor/skills/ | Los integrantes que usan Cursor no tienen acceso al skill | El paso 8 crea ambas rutas siempre |
| Generar §4 con pasos genéricos sin paths ni clases reales | El agente genera código fuera de la arquitectura hexagonal | Usar los paths canónicos de AGENTS.md §3 y nombres del diccionario FSD §6 |

---

## 8. Mini ejemplo de invocación

> "Usa el skill `edusync-skill-creator` para generar el skill `exportar-sie-uc004`.
> Propósito: implementar FSD-UC-004 (exportación de calificaciones al SIE).
> Entrada: FSD-UC-004 con DA-05 y NFR-005. Salida: vertical slice Java + k6.
> Agente: dev-agent."

---

## 9. Modos de fallo conocidos

- **`E_MISSING_INPUT`** — Falta alguno de los 5 datos obligatorios →
  STOP, listar el dato faltante y esperar respuesta.

- **`E_NONEXISTENT_FSD_ID`** — El UC / BR / NFR citado no existe en el FSD leído →
  STOP, listar los IDs disponibles en el FSD y pedir corrección.

- **`E_VERSION_CONFLICT`** — El BRD más reciente tiene reglas que contradicen el FSD →
  STOP, describir la contradicción y escalar al responsable técnico.

- **`E_SLUG_DUPLICATE`** — Ya existe .claude/skills/<slug>/SKILL.md →
  Preguntar si actualizar o crear con slug alternativo. No sobreescribir sin confirmación.

- **`E_INVARIANT_VIOLATION`** — El procedimiento propuesto viola AGENTS.md §11 →
  STOP, señalar la regla violada y reformular el propósito.

- **`E_TOO_LONG`** — El SKILL.md generado supera 220 líneas →
  Condensar §4 y §7 usando tablas; nunca recortar §5 ni §6.

---

## 10. Registro de cambios del Skill

| Versión | Fecha      | Autor          | Cambio          | Documentos base                           |
|---------|------------|----------------|-----------------|-------------------------------------------|
| 0.1.0   | 28/05/2026 | Rodrigo Aspeti | versión inicial | BRD v2, FSD v1, PRD v1, MRD v1, LFSD v1 |
| 0.2.0   | 28/05/2026 | Rodrigo Aspeti | §2 tabla de skills instalados para evitar duplicados (6 skills activos v0.1–v0.2) | ADRs 0001-0006, DTI v0.2 |
