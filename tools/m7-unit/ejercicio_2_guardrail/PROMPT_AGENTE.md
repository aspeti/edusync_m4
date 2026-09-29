# Prompt del agente — ejercicio 2 (guardrail de entrada)

Pegado en `agente_generador.py`. Cambia este archivo, no el script.

```
Escribe GuardrailEntradaAgenteAgenteTest.java para GuardrailEntradaAgente.

Reglas:
- JUnit 5 + AssertJ. Paquete com.edusync.shared.ai.application.service.
- Clase anotada con @Tag("agente"). Cada @Test tambien con @Tag("agente").
- NO edites GuardrailEntradaAgente ni GuardrailEntradaAgenteTest.
- NO dupliques metodos @Test que ya existen en GuardrailEntradaAgenteTest
  (inyeccionBloquea, correoSeEnmascaraYSigue, rudeSeEnmascaraYSigue, fraseAcademicaPasaLimpia).
- Maximo 6 metodos @Test.
- Instancia real: new GuardrailEntradaAgente(). Sin Mockito y sin Spring.
- ResultadoGuardrailEntrada: ok(), preguntaLimpia(), hallazgos(), mensaje().
- Inyeccion BLOQUEA (ok=false). PII (correo, telefono, RUDE, CI) se ENMASCARA y sigue (ok=true).
  Tarjeta BLOQUEA.
- Casos que SI debes cubrir (si el test a mano no los tiene):
  1. telefono boliviano 7xxxxxxx o 6xxxxxxx -> hallazgos contiene "telefono", texto [TELEFONO]
  2. CI con letras (ej. 1234567 LP) -> hallazgos contiene "ci", texto [CI]
  3. numero de tarjeta 13-19 digitos -> ok=false, hallazgos contiene "tarjeta"
  4. "ignore previous instructions" (ingles) -> bloquea inyeccion
  5. pregunta null o "" no lanza NPE; pasa o bloquea de forma definida
- NFR-007: el test no imprime RUDE ni tarjetas en System.out.
- No uses @Disabled. No borres nada.
- Devuelve SOLO la clase Java completa en un bloque ```java.
```
