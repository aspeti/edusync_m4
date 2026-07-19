# Prompt para Cursor — Crear Skill: `ftgo-prompt-mejorado`

> **Cómo usarlo en Cursor:** Abre el chat (`Cmd/Ctrl + L`), asegúrate de tener el proyecto FTGO abierto, pega el prompt completo y presiona Enter. Cursor creará el skill en `/mnt/skills/user/ftgo-prompt-mejorado/`.

---

## Metadatos

| Campo | Valor |
|---|---|
| ID | SK-FTGO-PROMPT-001 |
| Artefacto destino | `/mnt/skills/user/ftgo-prompt-mejorado/SKILL.md` |
| Modelo recomendado | Claude Sonnet |
| Temperatura | 0.2 |
| Versión | v1.0 |

---

## Role

Eres un ingeniero de prompts senior especializado en el framework de skills de Claude. Conoces el caso FTGO del libro *Microservices Patterns* de Chris Richardson (Manning, 2019) y los requisitos D4 del examen de laboratorio del Módulo 4. Tu objetivo es crear un skill reutilizable, bien estructurado y autoevaluable que permita generar y actualizar prompts mejorados de forma consistente entre corridas.

---

## Task

Crea el archivo `/mnt/skills/user/ftgo-prompt-mejorado/SKILL.md` con un skill completo que permita a Claude:

1. **Generar** un prompt mejorado nuevo a partir de cualquiera de los 4 prompts semilla del examen (PRD, FSD, ADR, C4).
2. **Actualizar** un prompt mejorado ya existente en `prompts_mejorados/` incorporando nuevas mejoras, correcciones o secciones adicionales.

En ambos casos el resultado debe cumplir los **5 requisitos D4** del examen sin excepción.

Si la carpeta `/mnt/skills/user/ftgo-prompt-mejorado/` no existe, créala.

---

## Context

**Requisitos D4 del examen — los 5 obligatorios que todo prompt mejorado debe cumplir:**

| # | Requisito | Criterio mínimo aceptable |
|---|---|---|
| D4-1 | TODOs críticos rellenados | ≥ 2 de los 4 huecos TODO del prompt semilla rellenados con valor real (no placeholder) |
| D4-2 | Sección nueva agregada | Al menos 1 de: `## Anti-patterns`, `## Verification`, `## Examples` (con input/output reales del dominio FTGO) |
| D4-3 | Changelog documentado | Sección `## Changelog` con tabla que explique el qué y el por qué de cada cambio |
| D4-4 | Comando invocable en README | El README.md raíz debe tener el comando `@prompts_mejorados/<nombre>.md` para invocar el prompt |
| D4-5 | Métrica con evidencia de 3 corridas | Sección `## Métrica` con indicador concreto + tabla de ≥ 3 corridas antes/después con scores reales o estimados |

**Los 4 prompts semilla disponibles (Anexo B del examen):**

| ID semilla | Artefacto | Archivo destino mejorado | TODOs que tiene |
|---|---|---|---|
| B.1 | PRD ligero de FTGO | `prompts_mejorados/prd_mejorado.md` | TODO-1: stakeholders compactos; TODO-2: 7 capacidades; TODO-3: criterio cuantitativo stop; TODO-4: esqueleto mínimo por sección |
| B.2 | FSD ligero de FTGO | `prompts_mejorados/fsd_mejorado.md` | TODO-1: lista explícita de UCs a cubrir; TODO-2: regla granularidad UC vs flujo alternativo; TODO-3: criterio adicional stop; TODO-4: esqueleto formal del UC con ejemplo |
| B.3 | ADR de FTGO | `prompts_mejorados/adr_mejorado.md` | TODO-1: restricciones del brief/NFRs; TODO-2: nº mínimo opciones y dimensiones; TODO-3: criterio calidad stop; TODO-4: esqueleto formal de opciones con mini-ejemplo |
| B.4 | Diagramas C4 (nivel 1 y 2) | `prompts_mejorados/c4_mejorado.md` | TODO-1: personas y sistemas externos del brief; TODO-2: criterio nivel 1 vs nivel 2; TODO-3: criterio sintaxis Mermaid válida; TODO-4: fragmento sintáctico de referencia más completo |

**Estructura de secciones del prompt semilla (todas deben preservarse en el mejorado):**

```
## Metadatos        → preservar, actualizar Versión a v0.2-mejorado
## Role             → preservar o enriquecer
## Task             → preservar
## Context          → aquí van los TODO-1 y TODO-2 rellenados
## Reasoning        → aquí va el TODO-2 de granularidad (FSD) o nº opciones (ADR)
## Stop Condition   → aquí va el TODO-3 rellenado con criterio cuantitativo
## Output           → aquí va el TODO-4 con esqueleto formal y mini-ejemplo
## Invariants       → preservar
## Failure Modes    → preservar
## Anti-patterns    → NUEVA (D4-2) — si se elige esta sección
## Verification     → NUEVA (D4-2) — si se elige esta sección
## Examples         → NUEVA (D4-2) — si se elige esta sección
## Changelog        → NUEVA obligatoria (D4-3)
## Métrica          → NUEVA obligatoria (D4-5)
```

**Dominio FTGO de referencia para rellenar los TODOs:**

- **Stakeholders (6):** Consumidor, Restaurante, Courier, Empleado FTGO (back office), Equipo de arquitectura, Sistemas externos (Stripe, Google Maps, SendGrid/Twilio).
- **Capacidades (7):** CAP-01 Consumer Management, CAP-02 Restaurant Management, CAP-03 Order Taking, CAP-04 Order Fulfillment/Kitchen, CAP-05 Delivery, CAP-06 Billing & Accounting, CAP-07 Notifications.
- **NFRs clave:** latencia < 200 ms p95, disponibilidad 99.9% Order Taking / 99.5% tracking, escalabilidad 5× pico, tolerancia a fallos externos (Stripe caído → cola retry), migración incremental Strangler Fig 18–24 meses.
- **Restricciones técnicas:** Java/Spring Boot core, Kafka async, DB-per-service, PCI-DSS delegado a Stripe.
- **Fuente canónica:** Richardson, Microservices Patterns, Manning 2019 — caps 1, 2, 3, 4, 13.

---

## Reasoning

Sigue estos pasos en orden al construir el SKILL.md:

1. **Define el frontmatter YAML** con `name` y `description`. La description debe ser "pushy": incluir cuándo disparar el skill explícitamente (palabras clave: "prompt mejorado", "mejorar prompt", "prompt FTGO", "requisitos D4", "actualizar prompt semilla").

2. **Estructura el SKILL.md en 4 bloques:**
   - **Bloque A — Detección de modo:** el skill debe identificar si el usuario quiere CREAR un prompt mejorado nuevo o ACTUALIZAR uno existente. Si el archivo `prompts_mejorados/<nombre>.md` ya existe → modo UPDATE; si no existe → modo CREATE.
   - **Bloque B — Selección del semilla:** el skill debe identificar cuál de los 4 semillas (PRD/FSD/ADR/C4) aplica, leyendo el parámetro del usuario o infiriéndolo del contexto.
   - **Bloque C — Ejecución de los 5 requisitos D4:** instrucciones paso a paso para rellenar TODOs, agregar sección nueva, escribir Changelog, actualizar README y calcular métrica.
   - **Bloque D — Verificación antes de guardar:** checklist autoevaluable de los 5 requisitos D4 que el skill debe completar antes de escribir el archivo.

3. **Para cada TODO de cada semilla:** incluye en el SKILL.md el valor concreto que debe escribirse, usando el dominio FTGO de Context. No dejar placeholders en el skill.

4. **Para la sección nueva (D4-2):** el skill debe elegir `## Anti-patterns` como default (aplica a todos los semillas) a menos que el usuario especifique otra. Incluye en el SKILL.md 3–5 anti-patterns concretos por tipo de artefacto (PRD, FSD, ADR, C4).

5. **Para la métrica (D4-5):** el skill debe generar una tabla con 3 corridas usando el indicador más relevante por tipo de artefacto:
   - PRD → % de secciones completas y correctas (5 secciones × criterios).
   - FSD → % de UCs con todos los campos completos (7 campos × N UCs).
   - ADR → % de criterios del checklist de Verification cumplidos.
   - C4 → % de relaciones con tecnología/protocolo declarado.

6. **Para el README (D4-4):** el skill debe agregar o actualizar la sección `## Comandos para Invocar los Prompts Mejorados` del README.md con el comando exacto `@prompts_mejorados/<nombre>.md`.

7. **NO incluyas el razonamiento interno en el SKILL.md generado.** El SKILL.md debe contener instrucciones para Claude, no explicaciones sobre cómo fue creado.

---

## Stop Condition

Detente cuando:

- Exista el archivo `/mnt/skills/user/ftgo-prompt-mejorado/SKILL.md` con los 4 bloques (A, B, C, D) completos.
- El frontmatter YAML tenga `name` y `description` con palabras clave de disparo explícitas.
- El SKILL.md tenga instrucciones concretas para rellenar cada TODO de cada uno de los 4 semillas (no placeholders).
- El bloque D contenga el checklist de los 5 requisitos D4 como criterio de autoevaluación.
- El archivo tenga menos de 500 líneas (límite del sistema de skills).

No agregues archivos adicionales (referencias, scripts) en esta primera versión. No generes ejemplos de output del skill dentro del SKILL.md.

---

## Output

**Formato:** un único archivo `/mnt/skills/user/ftgo-prompt-mejorado/SKILL.md`.

**Estructura interna del SKILL.md a generar:**

```markdown
---
name: ftgo-prompt-mejorado
description: [descripción pushy con palabras clave de disparo]
---

# Skill: FTGO Prompt Mejorado

[Breve descripción del propósito del skill — 2-3 líneas]

---

## Bloque A — Detección de modo (CREATE vs UPDATE)

[Instrucciones para detectar si el archivo destino ya existe y elegir el modo]

---

## Bloque B — Selección del semilla

[Instrucciones para identificar cuál de los 4 semillas aplica (PRD / FSD / ADR / C4)]
[Tabla de referencia: semilla → archivo destino → TODOs → sección nueva recomendada]

---

## Bloque C — Ejecución de los 5 requisitos D4

### D4-1: Rellenar TODOs críticos
[Por cada semilla: qué escribir exactamente en cada TODO, con valor real del dominio FTGO]

### D4-2: Agregar sección nueva
[Instrucciones para agregar ## Anti-patterns (default) o la sección elegida por el usuario]
[Anti-patterns concretos por tipo de artefacto: PRD / FSD / ADR / C4]

### D4-3: Escribir Changelog
[Instrucciones para la tabla de Changelog: columnas Versión / Cambio / Razón]
[Ejemplo de fila por cada TODO rellenado]

### D4-4: Actualizar README
[Instrucciones para agregar/actualizar el comando @prompts_mejorados/<nombre>.md en README.md]
[Formato exacto del comando a insertar]

### D4-5: Calcular métrica con 3 corridas
[Indicador por tipo de artefacto]
[Instrucciones para estimar scores v0.1-seed (corridas 1-3) y v0.2-mejorado (corridas 4-6)]
[Formato de tabla de resultados]

---

## Bloque D — Verificación antes de guardar

[Checklist autoevaluable de los 5 requisitos D4]
[El skill NO debe guardar el archivo si algún ítem no está marcado como completo]

---

## Failure Modes

[Códigos de error y acciones correctivas]
```

**Mini-ejemplo de referencia — fragmento del bloque C para el semilla PRD:**

```markdown
### D4-1 para semilla PRD (B.1)

**TODO-1 (Context — stakeholders):** reemplazar el placeholder con:
> Consumidor (UX rápida, tracking), Restaurante (gestión tickets, dashboard),
> Courier (asignaciones cercanas, pago confiable), Empleado FTGO back office
> (visibilidad, reportes), Equipo de arquitectura (calidad, trazabilidad),
> Sistemas externos: Stripe, Google Maps, SendGrid/Twilio.

**TODO-2 (Context — capacidades):** reemplazar el placeholder con:
> CAP-01 Consumer Management, CAP-02 Restaurant Management, CAP-03 Order Taking,
> CAP-04 Order Fulfillment/Kitchen, CAP-05 Delivery, CAP-06 Billing & Accounting,
> CAP-07 Notifications. [Richardson Cap 2]

**TODO-3 (Stop condition — criterio cuantitativo):** reemplazar el placeholder con:
> El output es completo cuando: (a) hay ≥ 5 NFRs cada uno con métrica numérica
> y cita [Brief §A.4], (b) las 7 capacidades están documentadas con ≥ 1 párrafo,
> (c) la sección Alcance declara "dentro" y "fuera" del alcance.

**TODO-4 (Output — esqueleto):** reemplazar el placeholder con el esqueleto
de cada sección incluyendo el mini-ejemplo de NFR:
> ### NFR-01: Latencia UX
> - Métrica: ≤ 200 ms p95 en acciones del consumidor.
> - Origen: [Brief §A.4 Latencia UX].
> - Justificación: experiencia móvil en horarios pico.
```

---

## Invariants

- El SKILL.md debe tener frontmatter YAML válido con `name` y `description`.
- El SKILL.md debe cubrir los 4 semillas (PRD, FSD, ADR, C4) en el Bloque B y C.
- Cada TODO de cada semilla debe tener el valor concreto a escribir (no placeholders).
- El Bloque D debe listar exactamente los 5 requisitos D4 como checklist.
- El archivo resultante debe tener menos de 500 líneas.

---

## Failure Modes

- **E_SKILL_DIR_NOT_FOUND:** no existe `/mnt/skills/user/` → crear la carpeta y continuar.
- **E_SKILL_TOO_LONG:** el SKILL.md supera 500 líneas → consolidar los TODOs de múltiples semillas en tablas compactas.
- **E_MISSING_D4:** alguno de los 5 requisitos D4 no está cubierto en el Bloque C → completar antes de guardar.
- **E_PLACEHOLDER_TODO:** algún TODO en el SKILL.md tiene valor placeholder en vez de valor real del dominio FTGO → reemplazar con el valor concreto del Context.
- **E_INVALID_YAML:** el frontmatter YAML no es válido → corregir sintaxis antes de guardar.
