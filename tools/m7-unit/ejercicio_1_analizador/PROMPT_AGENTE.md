# Prompt del agente — ejercicio 1 (analizador)

Pegado en `agente_generador.py`. Cambia este archivo, no el script.

```
Escribe AnalizadorIntencionConsultaAcademicaAgenteTest.java para
AnalizadorIntencionConsultaAcademica (codigo actual, no el que te gustaria).

Reglas:
- JUnit 5 + AssertJ. Paquete com.edusync.shared.ai.application.service.
- Clase anotada con @Tag("agente"). Cada @Test tambien con @Tag("agente").
- NO edites AnalizadorIntencionConsultaAcademica ni AnalizadorIntencionConsultaAcademicaTest.
- NO dupliques metodos @Test que ya existen en AnalizadorIntencionConsultaAcademicaTest
  (caso1..caso7: notas Juan, matematica, promedio 1ro A, reprobaron, nomina, follow-up).
- Maximo 8 metodos @Test. Nombres camelCase que describan el escenario.
- Instancia real: new AnalizadorIntencionConsultaAcademica(). Sin Mockito.
- analizar() devuelve Optional<IntencionConsultaAcademica>. orElseThrow() si hay match;
  isEmpty() si no.
- Enums reales: NOTAS, PROMEDIO, REPROBADOS, TOP, NOMINA, MATERIAS_ESTUDIANTE,
  BUSCAR_PROFESOR, BUSCAR_ESTUDIANTE. El record tiene profesor() como ultimo campo.
- Contexto: com.edusync.shared.ai.domain.ContextoConsultaAgente.vacio().
- Casos que SI debes cubrir (huecos del test a mano):
  1. cadena vacia o "hola" -> Optional.empty()
  2. "quien tuvo el promedio mas alto de 1ro A" -> TOP, cursoParalelo contiene 1ro
  3. "existe un profesor llamado Silvia" -> BUSCAR_PROFESOR, profesor=silvia
  4. "existe un estudiante llamado Juan" -> BUSCAR_ESTUDIANTE
  5. "que materias tiene asignada la profesora Silvia?" — documenta el comportamiento
     ACTUAL del regex MATERIAS_TIENE (no inventes un enum MATERIAS_PROFESOR)
  6. follow-up "Y en el segundo trimestre?" con contexto NOTAS
- No inventes APIs. No uses @Disabled. No borres nada.
- Devuelve SOLO la clase Java completa en un bloque ```java.
```
