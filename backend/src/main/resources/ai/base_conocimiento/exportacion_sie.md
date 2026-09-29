# Exportación SIE y perfil Bolivia

El Perfil Bolivia SIE (`FSD-UC-003`, `FSD-UC-004`, `FSD-UC-009`) es el cumplimiento ministerial: centralizadores y exportación al sistema estatal.

Reglas que el asistente debe respetar al explicar el proceso:

- La única clave de identidad estudiantil en payloads SIE es el **RUDE**. Nunca nombre ni posición de lista.
- El truncado oficial de promedios SIE es `floor()` en `ConsolidacionDomainService`, no `Math.round()`. Eso no se aplica al motor genérico de `FSD-UC-016`.
- Correcciones retroactivas son append-only (nuevo registro, el original no se pisa) con autorización del Director y ventana de expiración (BR-005, BR-009).
- Toda escritura de negocio deja `audit_log` inmutable en la misma transacción (BR-010).
- La integración SIE usa circuit breaker, timeout y reintentos (DA-05). Si el SIE no está disponible, el proceso se reintenta; no se inventa el acuse.

Este perfil convive con el modelo genérico SaaS: no reemplaza cursos, materias ni el cálculo HALF_UP de `CalculoNotas`.
