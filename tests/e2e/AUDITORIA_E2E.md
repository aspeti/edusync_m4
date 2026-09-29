# Auditoría de un test E2E generado por un agente — cinco preguntas (EduSync)

Se aplica **test por test**. Un test se acepta solo si responde SÍ a las cinco.
Adaptación del Lab2 M7 (`AUDITORIA_E2E.md`) al producto EduSync.

| # | Pregunta | Si la respuesta es «no»… |
|---|---|---|
| 1 | **¿El localizador es lo que ve una persona?** `getByRole`, `getByLabel`, `getByText`, o un `data-testid` puesto a propósito. Nada de CSS, ids de estilo, `nth-child`, XPath. | Se rompe con cualquier cambio visual. Se reescribe. |
| 2 | **¿Hay alguna espera fija?** `waitForTimeout`, `sleep`, `setTimeout`. | Suma segundos a cada corrida y no garantiza nada. Se quita: `expect` ya espera. |
| 3 | **¿Verifica lo que la interfaz promete?** URL post-login, heading, nav por rol, mensaje de negocio (`Credenciales inválidas`), estado visible (`ACTIVA`). **No** texto libre de LLM ni redacción completa de errores inventados. | Se cambia por aserción estructural. |
| 4 | **¿Es pequeño e independiente?** Un comportamiento por test; empieza en `/login`; no depende del orden. | Se parte. |
| 5 | **¿Los datos son propios del test?** Credenciales desde env; nombres únicos si crea tenants; no depende de otro test. | Se aísla. |

## Registro por caso

| Caso del plan | Archivo | Veredicto | Pregunta que falló (1-5) | Qué se cambió |
|---|---|---|---|---|
| | | aceptado / corregido / descartado | | |

Tokens: se escriben solos en `REGISTRO_TOKENS.csv` al correr
`python agente_e2e.py plan` o `python agente_e2e.py generar <N.N>`.
Tras la auditoría humana, completar a mano las columnas `tests_aceptados` /
`tests_corregidos` de esa fila.
