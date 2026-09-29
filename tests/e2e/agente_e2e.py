"""agente_e2e.py — Planner y Generator E2E para EduSync (patrón Lab2 M7).

    python agente_e2e.py plan                 -> specs/plan_agente.md
    python agente_e2e.py generar 1.1          -> tests/agente/caso_1_1.spec.ts
    npx playwright test tests/agente/caso_1_1.spec.ts

Lee HTML/templates Angular + semilla (no abre navegador). LLM via config.py
(.env raíz: Ollama u Open WebUI).
"""
from __future__ import annotations

import csv
import re
import sys
import time
from datetime import date
from pathlib import Path

import config

RAIZ = Path(__file__).resolve().parent
REPO = RAIZ.parent.parent
PLAN = RAIZ / "specs" / "plan_agente.md"
FRONTEND = REPO / "frontend" / "src" / "app"
TOKENS_CSV = RAIZ / "REGISTRO_TOKENS.csv"
PAREJA = "G-EduSync"

CSV_HEADER = [
    "fecha",
    "pareja",
    "ronda",
    "modelo",
    "tokens_entrada",
    "tokens_salida",
    "segundos",
    "tests_generados",
    "tests_aceptados",
    "tests_corregidos",
    "nota",
]


def registrar_tokens(
    ronda: str,
    entrada: int | None,
    salida: int | None,
    segundos: float,
    *,
    tests_generados: int = 0,
    nota: str = "",
) -> None:
    """Append una fila a REGISTRO_TOKENS.csv tras cada llamada al LLM (Lab M7)."""
    nuevo = not TOKENS_CSV.exists() or TOKENS_CSV.stat().st_size == 0
    with TOKENS_CSV.open("a", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=CSV_HEADER)
        if nuevo:
            w.writeheader()
        w.writerow(
            {
                "fecha": date.today().isoformat(),
                "pareja": PAREJA,
                "ronda": ronda,
                "modelo": config.resumen().replace(",", ";"),
                "tokens_entrada": entrada if entrada is not None else "",
                "tokens_salida": salida if salida is not None else "",
                "segundos": f"{segundos:.1f}",
                "tests_generados": tests_generados,
                "tests_aceptados": "",  # se completa a mano tras auditoría
                "tests_corregidos": "",
                "nota": nota.replace(",", ";"),
            }
        )
    print(f"tokens registrados en {TOKENS_CSV.name} (ronda={ronda})")


def llamar(mensajes: list[dict], *, ronda: str, tests_generados: int = 0, nota: str = "") -> str:
    t0 = time.time()
    r = config.crear_cliente().chat.completions.create(
        model=config.MODEL, temperature=0, messages=mensajes
    )
    segundos = time.time() - t0
    u = r.usage
    pin = u.prompt_tokens if u else None
    pout = u.completion_tokens if u else None
    print(
        f"modelo: {config.resumen()}  tokens: entrada={pin if pin is not None else '?'} "
        f"salida={pout if pout is not None else '?'}  tiempo={segundos:.1f}s"
    )
    registrar_tokens(ronda, pin, pout, segundos, tests_generados=tests_generados, nota=nota)
    return r.choices[0].message.content or ""


def sin_cerca(texto: str, lenguaje: str = "") -> str:
    m = re.search(r"```(?:%s|markdown|md)?\n(.*?)```" % lenguaje, texto, re.S)
    return (m.group(1) if m else texto).strip() + "\n"


def _leer_contexto_ui() -> str:
    partes: list[str] = []
    for rel in (
        "features/auth/login/login.page.ts",
        "shared/layout/shell.component.ts",
        "features/plataforma/tenants-list.page.ts",
    ):
        p = FRONTEND / rel
        if p.exists():
            partes.append(f"### {rel}\n```ts\n{p.read_text(encoding='utf-8')[:8000]}\n```")
    return "\n\n".join(partes)


PROMPT_PLAN = """Actúa como planificador de pruebas E2E de EduSync (SPA Angular 21 + API Spring).
La app corre en http://127.0.0.1:4200. tests/seed.spec.ts muestra cómo se abre el login.
Escribe un plan en Markdown con: un resumen de la sección, y luego grupos numerados
(## 1. Nombre, ## 2. ...) con casos numerados (### 1.1 Nombre, ### 1.2 ...).
Cada caso tiene una línea "**Pasos:**" (acciones de usuario, en orden, separadas por " → ")
y una línea "**Resultado esperado:**" (lo que se ve en pantalla, verificable).
Cubre: camino feliz, un caso de error, un caso de borde. Máximo 8 casos.
Alcance SOLO: login + consola SysAdmin Tenants (no toda la app).
NO verifiques textos generados por LLM. Verifica: URL, headings, botones, nav por rol,
mensajes de error de negocio (Credenciales inválidas), data-testid.
Devuelve SOLO el Markdown."""

PROMPT_GENERAR = """Actúa como generador de tests Playwright en TypeScript para EduSync.
Escribe UN solo test para el caso que te doy, dentro de un test.describe con el nombre del grupo,
imitando el estilo del archivo a mano (tests/tradicional/login.spec.ts).
Reglas:
- localizadores SOLO con getByRole, getByLabel, getByText o getByTestId (nunca CSS, ids ni nth-child);
- ninguna espera fija (nada de waitForTimeout);
- verificaciones con expect(...).toBeVisible/toHaveText/toContainText/toHaveURL;
- un comentario con el texto del paso antes de cada acción;
- el test empieza con page.goto('/login') o usa helpers de ../helpers/auth si aplica;
- no dependas de otro test;
- si necesitas SYSADMIN, usa process.env.E2E_SYSADMIN_EMAIL / E2E_SYSADMIN_PASSWORD
  (defaults: sysadmin@edusync.local / changeme_local_dev) o import { loginAsSysAdmin } from '../helpers/auth'.
Devuelve SOLO el código TypeScript, sin explicaciones."""


def plan() -> None:
    semilla = (RAIZ / "tests" / "seed.spec.ts").read_text(encoding="utf-8")
    ui = _leer_contexto_ui()
    salida = llamar(
        [
            {"role": "system", "content": PROMPT_PLAN},
            {
                "role": "user",
                "content": (
                    f"### tests/seed.spec.ts\n```ts\n{semilla}\n```\n\n"
                    f"### UI Angular (fragmentos)\n{ui}"
                ),
            },
        ],
        ronda="planner",
        nota="plan_agente.md",
    )
    PLAN.parent.mkdir(parents=True, exist_ok=True)
    PLAN.write_text(sin_cerca(salida), encoding="utf-8")
    print(f"escrito: {PLAN.relative_to(RAIZ)}  -> auditar el plan ANTES de generar (AUDITORIA_E2E.md)")


def generar(caso: str) -> None:
    if not PLAN.exists():
        # Fallback al plan auditado del equipo
        fuente = RAIZ / "specs" / "plan_edusync_login.md"
        if not fuente.exists():
            sys.exit(f"no hay plan: ejecuta primero `python agente_e2e.py plan`")
        texto = fuente.read_text(encoding="utf-8")
        print(f"aviso: usando {fuente.name} (no existe plan_agente.md)")
    else:
        texto = PLAN.read_text(encoding="utf-8")

    grupos = {m.group(1): m.group(2).strip() for m in re.finditer(r"^## (\d+)\.\s*(.+)$", texto, re.M)}
    m = re.search(rf"^### {re.escape(caso)}\s+(.+?)\n(.*?)(?=^### |\Z)", texto, re.S | re.M)
    if not m:
        sys.exit(f"no encuentro el caso {caso} en el plan")
    nombre, cuerpo = m.group(1).strip(), m.group(2).strip()
    patron = (RAIZ / "tests" / "tradicional" / "login.spec.ts").read_text(encoding="utf-8")
    helpers = (RAIZ / "tests" / "helpers" / "auth.ts").read_text(encoding="utf-8")
    salida = llamar(
        [
            {"role": "system", "content": PROMPT_GENERAR},
            {
                "role": "user",
                "content": (
                    f"### Patrón a imitar\n```ts\n{patron}\n```\n\n"
                    f"### Helpers disponibles\n```ts\n{helpers}\n```\n\n"
                    f"### Grupo: {grupos.get(caso.split('.')[0], 'Casos')}\n"
                    f"### Caso {caso}: {nombre}\n{cuerpo}"
                ),
            },
        ],
        ronda=f"generator-{caso}",
        tests_generados=1,
        nota=f"caso_{caso.replace('.', '_')}.spec.ts",
    )
    destino = RAIZ / "tests" / "agente" / f"caso_{caso.replace('.', '_')}.spec.ts"
    codigo = sin_cerca(salida, "(?:ts|typescript)")
    if "import { test, expect }" not in codigo and "from '@playwright/test'" not in codigo:
        codigo = "import { test, expect } from '@playwright/test';\n\n" + codigo
        print("aviso: el modelo olvido el import; se agrego")
    destino.write_text(codigo, encoding="utf-8")
    print(f"escrito: {destino.relative_to(RAIZ)}")
    print(f"ahora: npx playwright test {destino.relative_to(RAIZ)}  y despues la auditoria (5 preguntas)")
    print("tras auditar: completa tests_aceptados/tests_corregidos en REGISTRO_TOKENS.csv")


if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "plan":
        plan()
    elif len(sys.argv) == 3 and sys.argv[1] == "generar":
        generar(sys.argv[2])
    else:
        sys.exit("uso: python agente_e2e.py plan | python agente_e2e.py generar <N.N>")
