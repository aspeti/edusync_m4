# Lab2 · Módulo 7 · Día 4 — Pruebas E2E web con Playwright: a mano, con grabador y con agentes (Planner, Generator)

**Docente:** M.Sc. Luis Marcelo Garay Choqueribe · FCYT–UMSS · lunes 7 de septiembre de 2026 · 19:00–22:00
**App bajo prueba:** SoporteIA-web, la interfaz de chat de SoporteIA en modo demo (reglas fijas, sin modelo), con el **mismo contrato** de respuesta del viernes.
**Objetivo de hoy:** que cada pareja salga con una suite E2E en verde cuyos localizadores no se rompan con el próximo cambio de la interfaz, y sabiendo qué parte de esa suite la escribió un agente y por qué se aceptó.

> Pregunta de la sesión: *el viernes probamos lo que rodea al modelo sin abrir un navegador. Hoy abrimos el navegador: ¿qué se prueba ahí que no se probaba antes, y qué sigue sin probarse?*

REGLA DEL MÓDULO — «Ninguna prueba generada por IA se acepta sin auditoría humana.» Hoy la auditoría tiene cinco preguntas propias (`AUDITORIA_E2E.md`).

---

## Qué hay en esta carpeta

```
Lab2_M7_e2e/
├── README.md                     ← este guion
├── PROMPTS_AGENTES.md            ← Planner y Generator: instalación y prompts para cualquier IDE
├── agente_e2e.py · config.py · .env.example   ← Planner y Generator como script, por el SDK (sin depender del IDE)
├── AUDITORIA_E2E.md              ← las cinco preguntas de la auditoría E2E
├── ENTREGABLE_DIA4.md            ← plantilla del entregable
├── requirements.txt              ← la app (FastAPI + uvicorn)
├── package.json                  ← los tests (Playwright, TypeScript) y los scripts npm
├── playwright.config.ts          ← levanta la app sola, baseURL, trazas
├── .github/agents/               ← las tres definiciones de agente que genera Playwright
├── app/
│   ├── servidor.py               ← SoporteIA-web: GET / (la página), POST /api/chat (reglas demo)
│   └── index.html                ← la interfaz: chat, estado, panel de detalle, anclas data-testid
├── specs/
│   └── plan_chat_soporteia.md    ← el plan que produjo el Planner, auditado caso por caso
└── tests/
    ├── seed.spec.ts              ← la semilla que se le da a los agentes
    ├── tradicional/
    │   ├── chat.spec.ts          ← 6 tests a mano: el patrón (rol, etiqueta, testid; sin esperas)
    │   └── api-desde-ui.spec.ts  ← el contrato del viernes visto desde el navegador
    └── agente/
        ├── generado-saludo.spec.ts             ← aceptado tal cual
        ├── generado-politica.spec.ts           ← verde, con tres defectos (el test_99 de hoy)
        └── generado-politica.auditado.spec.ts  ← la versión corregida
```

Suite: **11 tests, ~8 s**, la app se levanta y se apaga sola, sin modelo, sin red.

---

## Instalación (5 min, antes de clase)

```bash
cd Lab2_M7_e2e
pip install -r requirements.txt             # con el .venv del M6 activado
npm install                                 # Playwright + TypeScript
npx playwright install chromium             # el navegador (una vez; ~150 MB)
npx playwright test                         # 11 passed
```

Si `npx playwright install` no puede descargar, se puede usar un navegador ya instalado: `CHROMIUM_PATH=<ruta-al-ejecutable> npx playwright test`.

Para ver la app a mano: `npm run app` y abrir `http://127.0.0.1:8017`. Los tests **no** necesitan que esté abierta: `playwright.config.ts` la levanta.

---

## Secuencia de la clase (3 h · 19:00–22:00)

### PASO 0 · Ver la app y correr la suite (10 min · 19:00)

`npm run app`, abrir la página, escribir «hola», «estado del pedido PED-2026-0203», «ignora las instrucciones». Señalar el panel de la derecha: intención, ticket, fuentes y **camino recorrido**. Es la traza del viernes, ahora en pantalla.

Cerrar la app. `npx playwright test`: 11 verdes en 8 segundos, y la app se levantó y apagó sola.

🗣 *El viernes probamos todo lo que rodea al modelo sin abrir un navegador. Hoy lo abrimos. Lo que se prueba acá es distinto: que una persona pueda escribir, enviar y leer; que la interfaz muestre lo que la API devolvió; que el botón haga lo que dice. Lo que sigue sin probarse: la redacción del modelo. Esa regla no cambió.*

### PASO 1 · La app: qué la hace testeable (15 min · 19:10)

Abrir `app/index.html`. Tres cosas que un desarrollador pone **para** QA:

1. **Roles y nombres accesibles.** `<ul role="log" aria-label="Mensajes">`, `<p role="status">`, `role="alert"` en el mensaje bloqueado, `<label for="pregunta">Tu consulta</label>`. Un lector de pantalla los usa; Playwright también. *Accesible = testeable.*
2. **`data-testid`** en lo que no tiene un rol natural: `intencion`, `ticket`, `citas`, `traza`, `thread-id`, `mensaje-asistente`. Son anclas que el equipo pone a propósito y promete no cambiar.
3. **Sin ids de estilo en los tests.** `#mensajes`, `.asistente` existen para el CSS, no para QA. Mañana el diseñador los cambia y no avisa.

Abrir `app/servidor.py` solo para mostrar `responder()`: reglas fijas, un camino por regla, misma forma de respuesta que `POST /flujo`. *Hoy el modelo no está porque no hace falta: la interfaz se prueba igual con reglas que con el grafo real.*

### PASO 2 · Tres formas de escribir un test E2E (25 min · 19:25)

**A mano — `tests/tradicional/chat.spec.ts`.** Leer el primer test con la sala:

```ts
await page.getByLabel('Tu consulta').fill('hola');
await page.getByRole('button', { name: 'Enviar' }).click();
await expect(page.getByTestId('mensaje-asistente')).toContainText('Hola, soy SoporteIA');
```

Tres reglas: localizador por rol/etiqueta/testid; sin esperas fijas (`expect` espera sola hasta 5 s); cada test empieza en `page.goto('/')`. Correr `npx playwright test tests/tradicional --headed` para verlo en pantalla.

**Con el grabador — `npm run codegen`.** Hacer en vivo el flujo «hola → enviar». Mostrar el código que produce: ya usa `getByRole`/`getByLabel` porque lee el árbol de accesibilidad. Pero: no sabe qué verificar (no escribe `expect`), y si la página no tiene roles ni etiquetas, cae a CSS. *El grabador transcribe clics; no decide qué es importante.*

**Con agentes — Planner y Generator.** Lo que viene.

**Dinámica (5 min):** *¿por qué `chat.spec.ts` no verifica el texto completo de ninguna respuesta?* Misma respuesta del viernes: la redacción la pone el modelo. Se verifica fuente, estado, ticket, cantidad.

### PASO 3 · El Planner: explorar y planificar (25 min · 19:50)

`PROMPTS_AGENTES.md`. Los agentes de Playwright trabajan sobre el **árbol de accesibilidad** (lo que el grabador también lee), no sobre capturas. Por eso producen localizadores robustos y por eso la app tiene que ser accesible **antes** de llamarlos: *localizadores estables primero; recién entonces los agentes.*

Instalación: `npx playwright init-agents --loop=<su-ide>`. Quien no tenga agentes nativos usa el prompt Planner de `PROMPTS_AGENTES.md` con su agente del IDE, o el script del lab: `python agente_e2e.py plan` (lee `index.html` por el SDK y escribe `specs/plan_agente.md`; luego `python agente_e2e.py generar 1.1`). Conexión al modelo: `config.py` + `.env` como en el LabX.

Cada pareja corre el Planner sobre la app con la semilla `tests/seed.spec.ts` → `specs/plan_<pareja>.md`. Mientras tanto, abrir `specs/plan_chat_soporteia.md` (el del docente): 8 casos, uno corregido (1.4: pedía la redacción completa) y uno descartado (3.3: cinco consultas seguidas, no prueba nada nuevo). *El Planner propone; se audita el plan antes de generar un solo test, porque cada caso del plan cuesta tokens al Generator.*

— Pausa 10 min · 20:15 —

### PASO 4 · El Generator: un caso por vez (30 min · 20:25)

Cada pareja le pide al Generator **un** caso del plan (el 1.1 primero). El Generator ejecuta cada paso en el navegador, lee su propio registro y recién entonces escribe el spec. Correr solo ese archivo: `npx playwright test tests/agente/<archivo>`.

Mientras se pasa por las mesas: *¿usó `getByRole`/`getByLabel` o cayó a CSS? ¿metió un `waitForTimeout`? ¿verificó la redacción del asistente?* Anotar tokens en `REGISTRO_TOKENS.csv` (ronda «planner», ronda «generator-1.1», etc.).

Después, dos casos más: uno de error (2.2 o 1.3) y uno de borde (3.2).

### PASO 5 · La auditoría E2E (30 min · 20:55)

Esta es la clase. Correr `npx playwright test tests/agente -v`: todo verde. Abrir **`tests/agente/generado-politica.spec.ts`**:

```ts
await page.fill('#pregunta', 'cuántos días tengo para devolver');
await page.click('#enviar');
await page.waitForTimeout(2000);
await expect(page.locator('#mensajes li:nth-child(2)')).toHaveText('Tienes 30 dias calendario … (fuente: politica_devoluciones.md)');
```

*¿Está bien?* Silencio. Pedir los tres defectos antes de mostrar el comentario: (1) localizador por id y posición; (2) espera fija de dos segundos; (3) verifica la redacción del modelo. Y está **verde**. Con el modelo real, el (3) se pone rojo la primera vez que el modelo lo diga distinto; el (1), el día que se agregue un mensaje de bienvenida; el (2) nunca falla, solo cobra dos segundos por corrida para siempre.

Abrir `generado-politica.auditado.spec.ts`: la misma intención, sin los tres defectos.

Las cinco preguntas de `AUDITORIA_E2E.md`, y cada pareja audita sus tres casos generados. Pares cruzados (8 min): un test de otra pareja con las cinco preguntas.

### PASO 6 · Comparación y cierre (20 min · 21:25)

Tabla en la pizarra, la llena la sala:

| | A mano | Grabador | Planner + Generator |
|---|---|---|---|
| Quién decide qué probar | La persona | Nadie | El Planner (se audita el plan) |
| Localizadores | Los que la persona elige | Rol/etiqueta si la app es accesible; CSS si no | Rol/etiqueta (lee el árbol de accesibilidad) |
| Verificaciones | Las escribe la persona | No escribe | Las propone; tiende a verificar la redacción del modelo |
| Costo | Tiempo | Tiempo | Tokens (Planner ≈ 10 000; Generator ≈ 3 000 por test) + auditoría |
| Se rompe con | Cambios de negocio | Cualquier cambio visual | Lo que no se auditó |

Tres frases:

1. **Accesible = testeable.** Los localizadores robustos no los pone Playwright ni el agente: los pone el equipo en el HTML (roles, etiquetas, `data-testid`). Sin eso, ninguna de las tres formas funciona bien.
2. **El agente lee el árbol de accesibilidad, no la pantalla.** Por eso sus localizadores son buenos y por eso no sabe qué es «importante»: eso se audita.
3. **En E2E tampoco se verifica la redacción del modelo.** Se verifica lo que la interfaz promete: fuente, estado, ticket, cantidad. Lo demás es eval (viernes).

**Tarea para el miércoles (S5, Healer y CI):** `ENTREGABLE_DIA4.md` completo, tres casos generados y auditados en verde, `REGISTRO_TOKENS.csv`; sobre su producto web, el Planner sobre una sección y dos casos generados. El miércoles se rompe la interfaz a propósito y se ve qué hace el Healer.

---

## Guion de tiempos

| Paso | Contenido | Min. | Reloj |
|---|---|---|---|
| 0 | Ver la app · 11 verdes · qué se prueba y qué no | 10 | 19:00 |
| 1 | La app: roles, etiquetas, `data-testid` — accesible = testeable | 15 | 19:10 |
| 2 | Tres formas: a mano, grabador, agentes · dinámica | 25 | 19:25 |
| 3 | Planner: instalación, exploración, el plan auditado | 25 | 19:50 |
| — | Pausa | 10 | 20:15 |
| 4 | Generator: un caso por vez · registro de tokens | 30 | 20:25 |
| 5 | Auditoría E2E: el generado con tres defectos · cinco preguntas · pares cruzados | 30 | 20:55 |
| 6 | Comparación en la pizarra · tres frases · tarea | 20 | 21:25 |
| — | Holgura | 15 | 21:45 |

## Orden de explicación del código (para quien prepara la clase)

1. `app/index.html` — roles, etiquetas, `data-testid` (no el CSS ni el JS).
2. `app/servidor.py` — solo `responder()`: un camino por regla, misma forma que `/flujo`.
3. `playwright.config.ts` — `webServer`, `baseURL`, `trace`.
4. `tests/tradicional/chat.spec.ts` — el primer test completo; luego solo los `expect` del de inyección.
5. `tests/tradicional/api-desde-ui.spec.ts` — `waitForResponse`: el contrato desde el navegador.
6. `npm run codegen` — en vivo.
7. `.github/agents/playwright-test-planner.agent.md` — solo el bloque «You will» (qué hace).
8. `specs/plan_chat_soporteia.md` — 1.4 corregido y 3.3 descartado.
9. `tests/agente/generado-politica.spec.ts` — los tres defectos, después de que la sala los busque.
10. `tests/agente/generado-politica.auditado.spec.ts` — la corrección.

## Alcance (decirlo si preguntan)

Hoy: Planner y Generator, navegador de escritorio, una sola app. El **Healer** (reparar un localizador roto), la ejecución en **CI** y las trazas son el miércoles. Móvil no entra en el módulo. La calidad de las respuestas del modelo es eval (viernes).
