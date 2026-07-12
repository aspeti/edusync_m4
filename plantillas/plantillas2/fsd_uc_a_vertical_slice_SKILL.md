---
name: fsd-uc-a-vertical-slice
description: >
  Implementación end-to-end desde un FSD-UC: genera controller, application
  service, dominio, repositorio, DTOs, mapeos y tests por cada criterio de
  aceptación Gherkin del UC. Aplica a sistemas universitarios (trámites,
  matrícula, calificaciones) en stack Spring Boot 3 + PostgreSQL. Requiere
  ID o cita del UC; NO redefine negocio.
allowed-tools:
  - read
  - edit
  - run-tests
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: FSD-UC → vertical slice (Spring Boot 3)

> Skill canónica del módulo. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/fsd-uc-a-vertical-slice/` o a
> `.claude/skills/fsd-uc-a-vertical-slice/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: implementación de una funcionalidad nueva o refactor de un caso de uso existente.
- ARRANCA cuando: el usuario indica `FSD-UC-NNN` o pega el bloque del UC y pide código.
- NO ACTIVAR cuando: el usuario aún está redactando el FSD o cuando solo quiere modelar datos (para eso existe el Skill `fsd-modelo-datos-a-jpa-flyway`).

## 2. Entradas obligatorias

`FSD-UC-NNN` con todos los siguientes campos del template del módulo:

- Trazabilidad a `PRD-REQ-…`.
- Actor principal.
- Precondiciones, disparador, flujo principal, flujos alternativos.
- Postcondiciones.
- `BR-…` aplicables (referenciadas en §5 del FSD).
- Datos de entrada y salida (referenciados en §6).
- Criterios de aceptación en formato Gherkin.

Si falta cualquiera, responder: "Necesito <X> del FSD antes de implementar." y listar los campos ausentes.

## 3. Fuentes de verdad (orden de precedencia)

1. UC del FSD citado.
2. `BR-…` invocadas y diccionario §6 del FSD.
3. `AGENTS.md` y ADRs del repositorio del grupo (stack, capas permitidas, guardrails).
4. Código existente del repo (convenciones de nombres, paquetes, transacciones).

## 4. Procedimiento

1. Verificar trazabilidad: ¿qué `PRD-REQ-…` y qué `BR-…` cubre el UC? Si no están listados, STOP.
2. Crear el slice en capas (arquitectura hexagonal por defecto):
   - `adapter/in/web/<UC>Controller` con endpoint REST que mapee el disparador del UC.
   - `application/<UC>UseCase` (puerto-in) con un método por flujo principal y, si aplica, por flujo alternativo.
   - `domain/<Agregado>` con invariantes derivadas de las `BR-…`.
   - `adapter/out/persistence/<Agregado>RepositoryAdapter` (puerto-out).
   - `dto/` (request/response) con la forma exacta de los datos in/out del FSD.
3. Transacción solo en `application/<UC>UseCase`; el dominio se mantiene puro (sin Spring, sin JPA).
4. Por cada AC Gherkin del UC, escribir un test:
   - Unit: para reglas de dominio puras.
   - Integración: con `@SpringBootTest` + Testcontainers PostgreSQL si toca BD.
   - Nombre del método de test = `ac<n>_<gherkinSlug>()` para que la trazabilidad sea evidente.
5. Manejar excepciones del FSD como tipos de dominio (`SaldoNoVigenteException`, `CupoAgotadoException`, `PrerequisitoNoCumplidoException`). Nunca `RuntimeException` desnudas.
6. Logging con `idTramite` o `idEstudiante` como MDC; nunca PII (carnet, email completo). Respetar `AGENTS.md §7`.

## 5. Salida esperada

- Lista de archivos creados / modificados.
- Tabla de trazabilidad obligatoria al cerrar el PR:

| FSD ID         | Archivo de implementación                                          | Test que lo verifica                                |
|----------------|--------------------------------------------------------------------|------------------------------------------------------|
| FSD-UC-002 AC1 | `src/main/java/.../inscripcion/InscribirEstudianteUseCase.java`    | `InscribirEstudianteUseCaseIT#ac1_estudianteAlDia` |
| FSD-UC-002 AC2 | `src/main/java/.../inscripcion/InscribirEstudianteUseCase.java`    | `InscribirEstudianteUseCaseIT#ac2_saldoVencido`    |
| BR-007         | `src/main/java/.../domain/Saldo.java`                              | `SaldoTest#bloqueaSiVencido`                       |

## 6. Verificación

- 100 % de los AC del UC tienen al menos un test verde.
- Ninguna validación de regla de negocio vive en el controller; está en dominio o aplicación.
- Nombres en código = nombres del diccionario §6 del FSD (sin "embellecimientos" libres).
- `mvn test` y `mvn verify` pasan localmente.

## 7. Anti-patrones del dominio universitario

- Validar saldo académico en el controller en vez del agregado `Estudiante` o servicio.
- Acoplar `Calificación` y `Pago` cuando el FSD los modela en bounded contexts distintos.
- Inventar estados en `Trámite` que el UC no documenta.
- Usar `RuntimeException("error")` en vez de excepción tipada del dominio.

## 8. Mini ejemplo de invocación

> "Implementa FSD-UC-002 'Inscripción a materias con verificación de prerequisitos' en `src/main/java/.../inscripcion/`. Stack y guardrails están en `AGENTS.md`. Usa el Skill `fsd-uc-a-vertical-slice`."

## 9. Modos de fallo conocidos

- El UC referencia un `BR-NNN` que no aparece en §5 → STOP, pedir aclaración o ADR.
- El AC Gherkin no es verificable sin datos faltantes → STOP, listar fixtures necesarios.
- El UC contradice el `AGENTS.md` (p. ej. obliga a saltar transacción) → STOP, abrir ADR.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 04/05/2026  | M.Sc. Edson Terceros   | versión inicial |
