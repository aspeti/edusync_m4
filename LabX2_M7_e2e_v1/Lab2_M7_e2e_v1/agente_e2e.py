"""agente_e2e.py — Planner y Generator en 90 lineas, por el SDK, sin depender del IDE.

    python agente_e2e.py plan                 -> lee app/index.html y escribe specs/plan_agente.md
    python agente_e2e.py generar 1.1          -> toma el caso 1.1 del plan y escribe tests/agente/caso_1_1.spec.ts
    npx playwright test tests/agente/caso_1_1.spec.ts

Diferencia con los agentes nativos de Playwright: estos NO abren el navegador;
leen el HTML (plan) y el patron a mano (generar). Pierden la exploracion en
vivo, pero muestran exactamente que hace un agente: leer archivos, armar un
prompt, llamar al modelo, escribir un archivo, reportar tokens.
Conexion al modelo: config.py + .env (copiar .env.example), igual que el LabX.
"""
import re
import sys
import time
from pathlib import Path

import config

RAIZ = Path(__file__).resolve().parent
PLAN = RAIZ / "specs" / "plan_agente.md"

PROMPT_PLAN = """Actúa como planificador de pruebas E2E. La app es una página única cuyo HTML
te doy abajo; tests/seed.spec.ts muestra cómo se abre. Escribe un plan en Markdown con:
un resumen de la aplicación, y luego grupos numerados (## 1. Nombre, ## 2. ...) con casos
numerados (### 1.1 Nombre, ### 1.2 ...). Cada caso tiene una línea "**Pasos:**" (acciones de
usuario, en orden, separadas por " → ") y una línea "**Resultado esperado:**" (lo que se ve en
pantalla, verificable). Cubre: camino feliz, un caso de error, un caso de borde. Máximo 8 casos.
NO verifiques la redacción de las respuestas del asistente: verifica que haya respuesta, qué
fuente cita y qué estado muestra la interfaz. Devuelve SOLO el Markdown."""

PROMPT_GENERAR = """Actúa como generador de tests Playwright en TypeScript. Escribe UN solo test para el caso
que te doy, dentro de un test.describe con el nombre del grupo, imitando el estilo del archivo
a mano. Reglas: localizadores SOLO con getByRole, getByLabel, getByText o getByTestId (nunca CSS,
ids ni nth-child); ninguna espera fija (nada de waitForTimeout); verificaciones con
expect(...).toBeVisible/toHaveText/toContainText/toHaveCount; un comentario con el texto del paso
antes de cada acción; el test empieza con page.goto('/'). No verifiques la redacción de las
respuestas del asistente. Devuelve SOLO el código TypeScript, sin explicaciones."""


def llamar(mensajes):
    t0 = time.time()
    r = config.crear_cliente().chat.completions.create(model=config.MODEL, temperature=0, messages=mensajes)
    u = r.usage
    print(f"modelo: {config.resumen()}  tokens: entrada={u.prompt_tokens if u else '?'} "
          f"salida={u.completion_tokens if u else '?'}  tiempo={time.time() - t0:.1f}s")
    return r.choices[0].message.content


def sin_cerca(texto, lenguaje=""):
    m = re.search(r"```(?:%s|markdown|md)?\n(.*?)```" % lenguaje, texto, re.S)
    return (m.group(1) if m else texto).strip() + "\n"


def plan():
    html = (RAIZ / "app" / "index.html").read_text(encoding="utf-8")
    semilla = (RAIZ / "tests" / "seed.spec.ts").read_text(encoding="utf-8")
    salida = llamar([{"role": "system", "content": PROMPT_PLAN},
                     {"role": "user", "content": f"### tests/seed.spec.ts\n```ts\n{semilla}\n```\n\n### app/index.html\n```html\n{html}\n```"}])
    PLAN.write_text(sin_cerca(salida), encoding="utf-8")
    print(f"escrito: {PLAN.relative_to(RAIZ)}  -> auditar el plan ANTES de generar (AUDITORIA_E2E.md)")


def generar(caso):
    texto = PLAN.read_text(encoding="utf-8")
    grupo = re.search(r"^## (\d+)\.\s*(.+)$", texto, re.M)
    grupos = {m.group(1): m.group(2).strip() for m in re.finditer(r"^## (\d+)\.\s*(.+)$", texto, re.M)}
    m = re.search(rf"^### {re.escape(caso)}\s+(.+?)\n(.*?)(?=^### |\Z)", texto, re.S | re.M)
    if not m:
        sys.exit(f"no encuentro el caso {caso} en {PLAN.name}")
    nombre, cuerpo = m.group(1).strip(), m.group(2).strip()
    patron = (RAIZ / "tests" / "tradicional" / "chat.spec.ts").read_text(encoding="utf-8")
    salida = llamar([{"role": "system", "content": PROMPT_GENERAR},
                     {"role": "user", "content": f"### Patrón a imitar (tests/tradicional/chat.spec.ts)\n```ts\n{patron}\n```\n\n"
                                                 f"### Grupo: {grupos.get(caso.split('.')[0], 'Casos')}\n### Caso {caso}: {nombre}\n{cuerpo}"}])
    destino = RAIZ / "tests" / "agente" / f"caso_{caso.replace('.', '_')}.spec.ts"
    codigo = sin_cerca(salida, "(?:ts|typescript)")
    if "import { test, expect }" not in codigo:
        codigo = "import { test, expect } from '@playwright/test';\n\n" + codigo
        print("aviso: el modelo olvido el import; se agrego (leer siempre lo que devuelve)")
    destino.write_text(codigo, encoding="utf-8")
    print(f"escrito: {destino.relative_to(RAIZ)}\nahora: npx playwright test {destino.relative_to(RAIZ)}  y despues la auditoria (5 preguntas)")


if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "plan":
        plan()
    elif len(sys.argv) == 3 and sys.argv[1] == "generar":
        generar(sys.argv[2])
    else:
        sys.exit("uso: python agente_e2e.py plan | python agente_e2e.py generar <N.N>")
