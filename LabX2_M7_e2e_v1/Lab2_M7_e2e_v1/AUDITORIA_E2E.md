# Auditoría de un test E2E generado por un agente — cinco preguntas

Se aplica **test por test**. Un test se acepta solo si responde SÍ a las cinco. Es la versión E2E del checklist del viernes: los cinco antipatrones de unit siguen valiendo (tautológico, foto del bug, prueba el mock, se traga excepciones, asegura poco); estos cinco son los que aparecen **además** en la capa de interfaz.

| # | Pregunta | Si la respuesta es «no»… |
|---|---|---|
| 1 | **¿El localizador es lo que ve una persona?** `getByRole`, `getByLabel`, `getByText`, o un `data-testid` puesto a propósito. Nada de CSS, ids de estilo, `nth-child`, XPath. | Se rompe con cualquier cambio visual. Se reescribe. |
| 2 | **¿Hay alguna espera fija?** `waitForTimeout`, `sleep`, `setTimeout`. | Suma segundos a cada corrida y no garantiza nada. Se quita: `expect` ya espera. |
| 3 | **¿Verifica lo que la interfaz promete, o la redacción del modelo?** Fuente citada, estado, ticket, cantidad de mensajes: sí. El texto completo de la respuesta: no. | Con el modelo real se pone rojo sin que nada esté mal. Se cambia por fuente/estado. |
| 4 | **¿Es pequeño e independiente?** Un comportamiento por test; empieza con `page.goto('/')`; no depende del orden ni de otro test. | Un test de 30 pasos que falla en el 27 no dice nada. Se parte. |
| 5 | **¿Los datos son propios del test?** No depende de que otro test (o una persona) haya dejado algo antes. | Falla según el orden. Se aísla (nueva conversación, datos creados en el propio test). |

## Los que ya sabemos que están (para calibrar)

- `tests/agente/generado-politica.spec.ts`: falla 1, 2 y 3 a la vez, y está **verde**. Es el `test_99` de hoy.
- `specs/plan_chat_soporteia.md` 1.4: el Planner pedía verificar la redacción completa; se corrigió a «verificar la fuente».
- `specs/plan_chat_soporteia.md` 3.3: cinco consultas seguidas; descartado por no probar nada nuevo (pregunta 4).

## Lo que queda documentado (para el entregable)

Por cada test generado: caso del plan, archivo, veredicto (aceptado / corregido / descartado), pregunta que falló y qué se cambió. Los tokens del Planner y de cada Generator, en `REGISTRO_TOKENS.csv` (mismo formato del viernes).
