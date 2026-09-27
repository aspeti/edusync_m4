# Cómo se calculan las notas en EduSync

Cómo se calculan las notas: el motor de dominio `CalculoNotas` aplica `round` HALF_UP. Nunca se calcula el promedio en el frontend ni en SQL. Este es el modelo genérico (`FSD-UC-016`, `ADR-0013`, BR-020).

## Qué notas se pueden registrar

Cada calificación de una evaluación debe estar en el rango `[0, puntajeMaximo]`. El `puntajeMaximo` es la nota de la sección (el profesor no lo inventa). Si el valor queda fuera de rango, el sistema rechaza la escritura con `E_RANGO_INVALIDO`.

Solo se puede cargar o corregir una nota cuando el periodo está ABIERTO y la evaluación está ACTIVA. El profesor no altera la nómina de estudiantes (BR-001).

## Promedio de sección

El promedio de sección es la media de las evaluaciones ACTIVA que ya tienen nota. Se aplica `round` a 2 decimales HALF_UP. Así se calculan las notas de cada sección.

Si una sección no tiene ninguna evaluación con nota, queda INCOMPLETO. El sistema no inventa un 0 para rellenar el vacío.

## Nota de periodo

La nota de periodo suma las secciones completas de esa materia en el periodo. El resultado se redondea a entero con `round` HALF_UP. Así se calculan las notas del trimestre o periodo.

Una sección INCOMPLETO no entra en esa suma. No se usa `floor()` en este cálculo.

## Promedio de gestión

El promedio de gestión es la media de los N periodos de la gestión escolar. También se redondea a entero con `round` HALF_UP.

Si faltan periodos, el promedio igual se muestra y queda marcado PROVISIONAL. Un periodo sin nota cuenta como 0 en esa media; no se oculta el dato parcial.

## Floor y perfil SIE

En el modelo genérico de EduSync no se usa `floor()` para calcular notas. El `floor()` queda reservado al Perfil Bolivia SIE (`FSD-UC-003`), cuando se consolidan promedios para la exportación ministerial.
