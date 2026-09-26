# Entregable Día 4 — Suite E2E con localizadores robustos, generada y auditada

**Pareja:** ______________________ / ______________________
**App bajo prueba:** SoporteIA-web (este lab) · **Agente/IDE usado:** ______________________

Se entrega la carpeta `tests/`, `specs/` y este archivo. Cuatro criterios: que corra, que esté auditado, que esté medido, que sea honesto. Pesa dentro del 20 % de «E2E web con agentes + self-healing» (la otra mitad es el miércoles).

## 1. Que corra

Pegar la salida de:

```
npx playwright test
npx playwright test --project=chromium --reporter=list tests/agente
```

Tiempo total: ____ s. Si un test supera 3 s, cuál y por qué: ______________________

## 2. Que esté auditado

| Caso del plan | Archivo | Veredicto | Pregunta que falló (1-5) | Qué se cambió |
|---|---|---|---|---|
| | | aceptado / corregido / descartado | | |

Plan del Planner: casos propuestos ____ · aceptados ____ · corregidos ____ · descartados ____.
Tests del Generator: generados ____ · aceptados ____ · corregidos ____ · descartados ____.

## 3. Que esté medido

Tokens del Planner: entrada ____ salida ____. Tokens por test del Generator (promedio): ____.
`data-testid` que tuvieron que **agregar a la app** para que un localizador fuera estable: ______________________ (agregar anclas es trabajo legítimo de QA; anotarlo).

## 4. Que sea honesto

Los tres defectos de `tests/agente/generado-politica.spec.ts`, en sus palabras, y por qué estaba verde.
Lo que **no** se puede probar en E2E y por qué (va a evals, viernes): ______________________

## 5. Para su producto (tarea para el miércoles)

Sobre **su propio producto web**: correr el Planner sobre una sección (no toda la app), generar dos casos con el Generator, auditarlos con las cinco preguntas, y traerlos en verde. Si su producto no tiene interfaz web, aplicarlo sobre SoporteIA-web y anotar qué anclas (`data-testid`) tendría que agregar su producto para ser testeable.
