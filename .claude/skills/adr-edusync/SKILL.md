---
name: adr-edusync
description: >
  Redacta y formaliza Architecture Decision Records para el proyecto EduSync
  siguiendo plantillas/ADR_TEMPLATE.md. Activar cuando el usuario dice "crea
  un ADR para <decisión>", "formaliza DA-NN como ADR", "registra la decisión
  de <tema>". Consume FSD, LFSD, AGENTS.md y los ADRs existentes. Produce
  docs/adr/NNNN-kebab.md con las 9 secciones completas, actualiza DTI §21 y
  AGENTS.md §adrs_vigentes en el mismo commit.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: adr-edusync — Redactor de ADRs para EduSync

> Copiar a `.cursor/skills/adr-edusync/` y `.claude/skills/adr-edusync/` en la raíz del repo.
> Activar con: `@adr-edusync <tema de la decisión>`

---

## 1. Cuándo activarlo (triggers)

- **DURANTE**: diseño, implementación, revisión de PR, o cuando una decisión costosa de revertir necesita ser documentada.
- **ARRANCA cuando**:
  - Usuario dice: "crea un ADR para \<decisión\>", "formaliza DA-NN como ADR", "necesito un ADR de \<tema\>".
  - Se acepta una PR que introduce una dependencia tecnológica nueva no declarada en `AGENTS.md §4`.
  - `docs/DTI.md §21` lista una DA provisional que aún no tiene ADR formal en `docs/adr/`.
- **NO ACTIVAR cuando**:
  - La decisión ya tiene ADR formal en `docs/adr/` (ver §2 — tabla de ADRs existentes).
  - Se trata de una decisión operativa menor (configuración de timeout, tamaño de pool de conexiones) sin impacto en escalabilidad o seguridad.
  - El usuario está en fase de exploración sin decisión tomada aún — en ese caso usar `arch-agent` primero.

---

## 2. Entradas obligatorias

El usuario DEBE proporcionar al menos uno de:

| # | Dato | Ejemplo |
|---|------|---------|
| 1 | Tema / título de la decisión | `"estrategia de caché para centralizadores"` |
| 2 | DA provisional a formalizar | `DA-03` (ver tabla de DAs en DTI §17) |
| 3 | Contexto del problema a resolver | `"necesitamos decidir si usar Redis o caché en memoria"` |

Si falta, responder: `"Necesito el tema de la decisión o el número de DA provisional antes de redactar el ADR."`

### ADRs ya existentes en EduSync (no duplicar)

| Número | Título | Estado | DA origen |
|--------|--------|--------|-----------|
| ADR-0001 | Multitenancy mediante Row-Level Security en PostgreSQL 15 | Aceptada | DA-01 |
| ADR-0002 | Parametrización de reglas normativas sin redespliegue | Aceptada | DA-02 |
| ADR-0003 | Persistencia inmutable del audit_log con Hibernate Envers | Aceptada | DA-03 |
| ADR-0004 | Consolidación asíncrona con Spring Events (migrable a SQS) | Aceptada | DA-04 |
| ADR-0005 | Resiliencia en integración SIE con Resilience4j | Aceptada | DA-05 |
| ADR-0006 | Cloud provider y estilo de despliegue — AWS ECS Fargate | Aceptada | DA-06 |

**Próximo número disponible: `0007`.**

### Temas pendientes de ADR (candidatos documentados en el DTI)

| Tema sugerido | Origen |
|---------------|--------|
| Estrategia de migración a microservicios (Strangler Fig) | DTI §6.2 — Seam 2 |
| Generación de boletines PDF (PDFBox vs. Jasper) | AGENTS.md §4 — Apache PDFBox |
| Autenticación JWT y rotación de secretos | LFSD §seguridad, NFR-008 |
| Estrategia de branching y release (GitFlow vs. trunk-based) | DTI §15 |

---

## 3. Fuentes de verdad (orden de precedencia)

1. **Versión más reciente del FSD** — listar `docs/fsd/`, leer el mayor N de `FSD_EduSync[_vN].md`. Buscar UC, BR y NFR relacionados con la decisión.
2. **Versión más reciente del LFSD** — listar `docs/`, leer el mayor N de `LFSD-EduSync[_vN].md`. Buscar el componente técnico afectado.
3. **Versión más reciente del BRD** — listar `docs/brd/`, leer `BRD_EduSync_vN.md` mayor N. Verificar BR que la decisión debe respetar.
4. **`AGENTS.md`** — stack autoritativo, guardrails, prompts prohibidos §11.
5. **ADRs existentes** en `docs/adr/` — verificar que la nueva decisión no contradice ninguna aceptada.
6. **`plantillas/ADR_TEMPLATE.md`** — estructura exacta de las 9 secciones.

---

## 4. Procedimiento

### Paso 1 — Verificar que el ADR no existe aún
Listar `docs/adr/`. Si ya existe un ADR sobre el mismo tema → STOP, indicar el número existente.

### Paso 2 — Determinar el número correlativo
Contar los archivos `docs/adr/NNNN-*.md` existentes y asignar el siguiente (actualmente: `0007`).

### Paso 3 — Leer los documentos más recientes
Ejecutar la regla de versión más reciente del §3 para FSD, LFSD y BRD.
Extraer: UC afectados, BR aplicables, NFR relacionados, componentes del stack involucrados.

### Paso 4 — Verificar coherencia con ADRs existentes
Leer los 6 ADRs en `docs/adr/`. Si la decisión nueva contradice alguna aceptada:
→ **STOP** — indicar el conflicto y proponer superseder el ADR anterior en vez de contradecirlo.

### Paso 5 — Construir las 9 secciones del ADR

**Metadatos:**
```
Número:   0NNN (4 dígitos, correlativo)
Título:   sustantivo o imperativo corto (≤ 8 palabras)
Fecha:    fecha de hoy
Autor(es): Rodrigo Aspeti (o quien indique el usuario)
Estado:   Propuesta (cambia a Aceptada solo tras revisión humana explícita)
Alcance:  módulo/servicio afectado o "todo el sistema"
Stakeholders: roles involucrados del proyecto (Director, Docente, Secretaria, Equipo G-EduSync)
```

**§1 — Contexto (5–15 líneas):**
- Problema concreto en EduSync (no genérico).
- Restricciones reales: Ley 070, Ley 164 Bolivia, SIE protocolo, presupuesto SaaS Bolivia.
- Fuerzas en tensión extraídas del FSD/BRD (ej. BR-008 floor vs. rendimiento SQL).
- Citar los FSD-UC y BR afectados.

**§2 — Alternativas (tabla de 3 opciones):**
- Mínimo 3 alternativas reales y nombradas (no "opción genérica").
- Costo aproximado en contexto EduSync (AWS, equipo de 1-2 devs, < 50 unidades año 1).
- Pros/contras con referencia a NFRs del FSD cuando aplique.

**§3 — Decisión:**
- Frase única: "Elegimos la Alternativa X: \<descripción concisa\>."
- 3–8 líneas de justificación citando criterios decisivos con IDs de BR/DA/NFR.

**§4 — Consecuencias:**
- Positivas: beneficios medibles (citar NFR o KPI del BRD cuando aplique).
- Negativas: costos reales (tiempo de ingeniería, riesgo operativo, deuda técnica).
- Neutras: efectos observables sin juicio de valor.

**§5 — Impacto en el sistema:**
- **Código**: paquetes/clases afectadas usando los paths canónicos de `AGENTS.md §3`.
- **Operaciones**: cambios en `infra/`, Flyway, schedulers, configuración AWS.
- **Seguridad**: nuevas superficies de ataque o nuevas defensas — referenciar `AGENTS.md §7`.
- **Equipo**: habilidades requeridas (el equipo es 1-2 devs + agentes IA).
- **Costo**: impacto en factura AWS o licencias (relevante para modelo SaaS boliviano).

**§6 — Plan de reversión:**
- Señal temprana concreta (ej. "p95 > 500 ms en NFR-001 por 3 semanas consecutivas").
- Costo estimado de revertir en días-ingeniero.
- Plan B referenciando la alternativa descartada en §2.

**§7 — Validación:**
- Métrica objetiva y umbral (ej. "MultitenantTest pasa en 100% de 1000 requests").
- Referencia al golden test de `AGENTS.md §8.3` si aplica.
- Responsable: `compliance-agent` en CI o `qa-agent` en revisión semanal.

**§8 — Referencias:**
- FSD-UC-NNN, BR-NNN, NFR-NNN relacionados.
- ADRs relacionados de `docs/adr/`.
- POC que validó la decisión (si existe en `pocs/`).
- Enlace a DA-NN en `docs/arquitectura_funcional_EduSync.md` si es formalización.

**§9 — Historial:**
- Versión 1, fecha de hoy, autor, "propuesta inicial".

### Paso 6 — Guardar el archivo
```
docs/adr/0NNN-kebab-case-titulo.md
```
Si el directorio no existe, crearlo. Confirmar path exacto al usuario.

### Paso 7 — Actualizar DTI §21 y AGENTS.md
Agregar el nuevo ADR a:
- `docs/DTI.md §21` — tabla de ADRs registrados.
- `AGENTS.md` — campo `adrs_vigentes:` en el frontmatter YAML.

Esto DEBE hacerse en el mismo commit con mensaje:
```
docs(adr): ADR-0NNN <título corto> [<BR/DA/NFR clave>]
```

---

## 5. Salida esperada

- `docs/adr/0NNN-kebab-titulo.md` — ADR con las 9 secciones completas.
- Diff de `docs/DTI.md §21` — nueva fila en la tabla de ADRs.
- Diff de `AGENTS.md` — nuevo entry en `adrs_vigentes:`.
- Confirmación en chat: número de ADR, path creado, secciones del DTI/AGENTS actualizadas.

---

## 6. Verificación (criterios de "bien hecho")

- [ ] Número correlativo correcto (0007 o superior según lo que exista en `docs/adr/`).
- [ ] Las 9 secciones presentes y no vacías (sin placeholders `<…>`).
- [ ] §1 cita al menos 1 FSD-UC, 1 BR y 1 NFR del proyecto EduSync.
- [ ] §2 tiene exactamente 3 alternativas nombradas con pros/contras/costo.
- [ ] §3 empieza con "Elegimos la Alternativa X: …" y justifica con IDs reales.
- [ ] §5 usa paths canónicos de `AGENTS.md §3` (no paths inventados).
- [ ] §7 tiene umbral medible y responsable nombrado.
- [ ] Estado inicial = **Propuesta** (no Aceptada sin revisión humana).
- [ ] MUST NOT contradecir ADR-0001..ADR-0006 sin supersederlos explícitamente.
- [ ] MUST NOT incluir secretos, tokens ni campo `rude` de estudiantes.
- [ ] DTI §21 y AGENTS.md `adrs_vigentes` actualizados en el mismo commit.

---

## 7. Anti-patrones específicos EduSync

| Anti-patrón | Por qué falla | Mitigación |
|-------------|--------------|------------|
| Crear ADR que contradice ADR-0001 (RLS) sin supersederlo | Rompe el aislamiento multitenant garantizado — riesgo legal Ley 164 Bolivia | STOP en paso 4; proponer ADR que supersede con campo `Superada por ADR-NNNN` |
| §2 con solo 2 alternativas o con "no hacer nada" como única alternativa real | Rúbrica Módulo 4 exige trade-offs explícitos; 2 alternativas no demuestran análisis | Siempre 3 alternativas técnicas reales y nombradas |
| §7 sin golden test ni umbral numérico | La validación no puede verificarse en CI; el `compliance-agent` no puede rechazar PRs | Referenciar `AGENTS.md §8.3` o definir nuevo test con umbral medible |
| Estado = Aceptada antes de revisión humana | Los ADRs se aceptan por humanos, no por el agente | Estado inicial siempre **Propuesta**; cambiar a Aceptada solo en PR aprobada |
| ADR sobre decisión operativa menor (ej. "tamaño del pool de conexiones = 10") | Infla el registro de ADRs; dificulta encontrar las decisiones reales costosas de revertir | STOP: solo registrar como ADR si la decisión afecta estructura, seguridad, costo o escalabilidad |
| §5 con paths inventados como `src/main/java/EduSync/` | Rompe la trazabilidad código-ADR | Usar siempre paths de `AGENTS.md §3`: `src/domain/`, `src/application/`, `src/infrastructure/` |

---

## 8. Mini ejemplo de invocación

> `@adr-edusync Formaliza DA-03 como ADR: estrategia de persistencia inmutable del audit_log con Hibernate Envers. Consultar LFSD §audit y BR-010.`

> `@adr-edusync Crea ADR-0007 para la decisión de usar PDFBox en generación de boletines. Alternativas: PDFBox, JasperReports, iText. Ver FSD-UC-007 y NFR-012.`

---

## 9. Modos de fallo conocidos

- **`E_ADR_DUPLICATE`** — Ya existe ADR sobre el mismo tema → STOP, indicar el número existente y ofrecer crear uno que lo superseda si la decisión cambió.
- **`E_CONTRADICTS_EXISTING`** — La decisión nueva contradice un ADR aceptado (ej. propone calcular `floor()` en SQL, contradiciendo ADR-0002) → STOP, señalar el conflicto y proponer ADR de supersesión.
- **`E_NO_TOPIC`** — El usuario no especifica el tema ni DA → STOP, pedir el dato faltante y mostrar la lista de temas candidatos de §2.
- **`E_MISSING_FSD_ID`** — El contexto no referencia ningún FSD-UC ni BR → completar §1 con al menos 1 UC y 1 BR antes de guardar.
- **`E_STATUS_ACCEPTED_AUTO`** — El agente intenta marcar estado como Aceptada sin revisión humana → corregir a Propuesta.
- **`E_DTI_NOT_UPDATED`** — El ADR se guarda pero DTI §21 y AGENTS.md no se actualizan → completar el paso 7 antes de cerrar la tarea.

---

## 10. Registro de cambios del Skill

| Versión | Fecha      | Autor          | Cambio                                                                 | Documentos base |
|---------|------------|----------------|------------------------------------------------------------------------|-----------------|
| 0.1.0   | 28/05/2026 | Rodrigo Aspeti | Versión inicial — skill creado desde ADR_TEMPLATE.md con contexto real EduSync: 6 ADRs existentes, temas candidatos, stack, BR/DA/NFR y reglas de dominio | BRD v2, FSD v1, LFSD v1, DTI v0.2 |
