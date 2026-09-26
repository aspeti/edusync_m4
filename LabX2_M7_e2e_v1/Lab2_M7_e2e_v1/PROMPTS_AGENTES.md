# Los dos agentes de hoy: Planner y Generator

Playwright trae tres agentes propios (desde la versión 1.56): **Planner** (explora la app viva y escribe el plan), **Generator** (convierte cada caso del plan en un spec ejecutable, probando cada paso en el navegador antes de escribirlo) y **Healer** (repara tests rotos; es el tema del miércoles). Los tres trabajan sobre el **árbol de accesibilidad** de la página (roles, nombres, etiquetas), no sobre capturas de pantalla. Por eso producen localizadores como `getByRole('button', { name: 'Enviar' })` y no `div > div:nth-child(3)`.

## Cómo se instalan en el IDE (2 min)

```bash
npx playwright init-agents --loop=<su-ide>     # genera .github/agents/*.agent.md y la config del servidor MCP
```

`<su-ide>` es el nombre del entorno agéntico que cada uno usa; `npx playwright init-agents --help` lista los válidos. Lo que genera: tres definiciones de agente (`playwright-test-planner`, `-generator`, `-healer`) y la conexión al servidor MCP de Playwright (`npx playwright run-test-mcp-server`). Este lab ya trae las tres definiciones en `.github/agents/`; si su IDE usa otra carpeta, el comando de arriba las pone donde corresponde.

**Regla de consumo (la misma del viernes):** el Planner explora la app entera; con una app de una página cuesta ~10 000 tokens. Con una app grande hay que decirle **qué sección** explorar. El Generator cuesta ~3 000 tokens por test: se le pide **un caso por vez**, no el plan entero.

## Si su IDE no tiene agentes nativos de Playwright

Los dos prompts de abajo hacen lo mismo con cualquier agente del IDE que pueda abrir un navegador por MCP (o, sin navegador, leyendo `app/index.html`). Pierden la exploración en vivo, pero el flujo es el mismo.

### Prompt Planner (pegar tal cual; contexto: `tests/seed.spec.ts` y `app/index.html`)

```
Actúa como planificador de pruebas E2E. La app corre en http://127.0.0.1:8017 y
tests/seed.spec.ts muestra cómo se abre. Explora la página (o lee app/index.html)
y escribe specs/plan_<nombre>.md con este formato: un resumen de la aplicación,
y luego grupos numerados (1., 2., …) con casos numerados (1.1, 1.2, …). Cada caso
tiene "Pasos" (acciones de usuario, en orden) y "Resultado esperado" (lo que se
ve en pantalla, verificable). Cubre: camino feliz, un caso de error, un caso de
borde. Máximo 8 casos. NO verifiques la redacción de las respuestas del
asistente: esa parte la escribe un modelo y cambia; verifica que haya respuesta,
qué fuente cita y qué estado muestra la interfaz. No escribas código todavía.
```

### Prompt Generator (pegar tal cual; contexto: el plan, `tests/seed.spec.ts` y `tests/tradicional/chat.spec.ts`)

```
Actúa como generador de tests Playwright. Toma el caso <N.N> de specs/plan_<nombre>.md
y escribe tests/agente/<nombre-del-caso>.spec.ts con UN solo test, dentro de un
test.describe con el nombre del grupo. Imita el estilo de tests/tradicional/chat.spec.ts.
Reglas: localizadores SOLO con getByRole, getByLabel, getByText o getByTestId (nunca
CSS, ids ni nth-child); ninguna espera fija (nada de waitForTimeout); las
verificaciones con expect(...).toBeVisible/toHaveText/toContainText/toHaveCount;
un comentario con el texto del paso antes de cada acción; el test empieza con
page.goto('/') y no depende de ningún otro test. No verifiques la redacción de las
respuestas del asistente. Corre solo tu archivo con npx playwright test <archivo>
y pega la salida.
```

## Forma 3 · El script del lab (sin IDE)

```bash
cp .env.example .env              # modelo local del M6 o proveedor remoto, igual que en el LabX
python agente_e2e.py plan         # lee app/index.html + tests/seed.spec.ts -> specs/plan_agente.md
python agente_e2e.py generar 1.1  # toma el caso 1.1 del plan -> tests/agente/caso_1_1.spec.ts (reporta tokens)
npx playwright test tests/agente/caso_1_1.spec.ts
```

No abre el navegador (pierde la exploración en vivo), pero muestra exactamente qué hace un agente: leer archivos, armar el prompt, llamar al modelo, escribir el archivo. Y sirve para que todas las parejas tengan el mismo punto de partida aunque su IDE no tenga agentes.

## Después de cada caso generado: la auditoría E2E

Ver `AUDITORIA_E2E.md`. Un test generado se acepta solo si pasa las cinco preguntas. Se marca en el nombre del archivo: `<caso>.spec.ts` → `<caso>.auditado.spec.ts`, o se corrige y se deja el original con el comentario de auditoría (como `tests/agente/generado-politica.spec.ts`).
