---
name: ai-test-generator-edusync
description: >
  Genera pruebas unitarias, de integración y de contrato asistidas por un agente de IA
  (temperature=0) para un FSD-UC ya implementado de EduSync (Java 25 / Spring Boot 4.1.0),
  auditadas por una persona con un checklist de 3 preguntas antes de aceptarse, e
  integradas en la pirámide de pruebas (JUnit 5 + JaCoCo + Pitest) sin bajar el gate de
  cobertura (NFR-013) ni romper los golden tests. Activar cuando se pide "genera tests con
  IA para <clase/endpoint>", "audita los tests que generó el agente", o "sube la cobertura
  de <módulo>" citando un FSD-UC que ya tiene código en verde.
allowed-tools:
  - read
  - edit
  - run-tests
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: ai-test-generator-edusync — Generación y auditoría de tests asistidos por IA

> Formaliza para EduSync el ciclo enseñado en el laboratorio "Unit testing asistido por
> agentes de IA" (M7): *tests a mano → tests con agente (temperature=0) → auditoría humana
> obligatoria*, extendido a las tres capas de la pirámide (unitaria, integración, contrato).
> Copiar a `.claude/skills/ai-test-generator-edusync/` y a
> `.cursor/skills/ai-test-generator-edusync/` (mismo contenido).

---

## 1. Cuándo activarlo

- **DURANTE**: estabilización de un `FSD-UC` que ya tiene código en verde (`mvn test`
  pasando), antes de subir su cobertura o de declarar cerrado su `Design Doc`. También
  aplica al spike `shared.ai` (LLM/tool calling), para dejar evidencia de auditoría humana
  sobre tests que el propio agente ayudó a redactar.
- **ARRANCA cuando**: se pide generar o auditar tests con IA para una clase de `domain/` o
  `application/service/` ya implementada, o para un endpoint REST con al menos dos códigos
  de respuesta distintos (200/404/400, etc.).
- **NO ACTIVAR cuando**: el `FSD-UC` todavía no tiene código (usar `feature-design-doc`
  primero) o la instrucción pide saltarse, desactivar o comentar tests existentes —
  eso es un patrón prohibido de `AGENTS.md §11`.

---

## 2. Entradas obligatorias

| # | Dato | Descripción | Ejemplo |
|---|------|-------------|---------|
| 1 | **FSD-UC / módulo objetivo** | Debe figurar "completo" en `docs/product/DTP.md §A.3` | `FSD-UC-016` (Cálculo de Notas) |
| 2 | **Clase(s) + test(s) manual(es) existentes** | Contexto que el agente NO debe duplicar; si no existe ningún test manual, escribir uno antes de invocar al agente | `CalculoNotas` + `CalculoNotasTest` |
| 3 | **Endpoint REST asociado (si aplica)** | Con sus códigos de respuesta documentados | `PUT /evaluaciones/{id}/calificaciones` → 200/404/409 |
| 4 | **Umbral de auditoría** | Siempre el checklist de 3 preguntas de §4.5 — no es opcional | — |

Si falta el dato 2: responder *"Necesito al menos un test manual de referencia antes de
generar tests con el agente — sin eso no hay contexto de qué NO duplicar."*

---

## 3. Fuentes de verdad (leer en este orden)

1. La clase objetivo en `domain/` o `application/service/` + su test manual existente.
2. `docs/qa/matriz-fsd-uc-tests.md` — confirmar el hueco real (columna vacía o `PARCIAL`).
3. `docs/qa/README.md` — comandos y umbrales JaCoCo/Pitest vigentes (no inventar umbrales).
4. `AGENTS.md §5` (convenciones), `§6` (invariantes de dominio), `§8.2`/`§8.3` (guardrails y
   golden tests), `§10` (anatomía del prompt-contrato Role/Task/Context/Reasoning/Stop/
   Output/Invariants/Failure modes — usarla tal cual para el prompt del paso 4).
5. `docs/product/DTP.md §A.3` — confirmar que el `FSD-UC` objetivo está **completo**.

---

## 4. Procedimiento

1. **Mapeo de fronteras.** Identificar qué colabora la clase/servicio objetivo (repos
   `out-port`, reloj, llamada a modelo LLM si aplica) — esas fronteras son las que llevan
   stub/mock; el dominio real nunca se sustituye.
2. **Cobertura actual.** `mvn -Punit verify` → JaCoCo; anotar el % de la clase/paquete y
   compararlo contra el gate vigente en `docs/qa/README.md` (80 % suite completa / 70 %
   sin Docker).
3. **Detección de duplicados.** Pedir al agente que liste, dentro del archivo de test
   existente, casos con misma función + mismo input + misma aserción; auditar cada
   hallazgo a mano y descartar falsos positivos (se parecen pero prueban lógica distinta).
4. **Propuesta HITL (obligatoria antes de escribir).** Correr el CLI en modo dry-run
   (`java GenerarTest.java --proponer ...` o sin `--generar`): revisar Target, escenarios
   propuestos y archivo de salida. Solo tras revisión humana: `--generar --aprobar-alcance
   --sesion <id>` (`temperature = 0`). Anatomía de `AGENTS.md §10`; el prompt **MUST NOT**
   pedir nada de `AGENTS.md §11`. Sesión en `docs/qa/ai-test-sessions/`. La IA **MUST NOT**
   auto-aprobar (usar `--decidir --veredicto APPROVE|REJECT|MODIFIED` después de ejecutar).
5. **Auditoría de 3 preguntas**, por cada test nuevo, antes de aceptarlo:
   - ¿Se pone en rojo si rompo la función (`return` fijo, lógica invertida)?
   - ¿El `assert` dice lo que el código **debe** hacer, o es una foto de lo que hizo hoy
     (ej. igualdad estricta sobre un flotante en vez de `Offset`/tolerancia)?
   - ¿El nombre del método describe el comportamiento (no `test_caso_3`)?
   Lo que no pasa las tres se corrige o se borra antes de integrarlo — nunca se acepta
   "porque quedó en verde".
6. **Duplicados confirmados.** No se borra el archivo: se anota `@Disabled("Duplicado de
   <testDeReferencia>")` con el motivo explícito.
7. **Integración.** Elegir un flujo real punta a punta (ej. `PUT
   /evaluaciones/{id}/calificaciones` → `UpsertCalificacionesService` → `CalculoNotas` →
   repositorio) y pedir al menos dos tests que lo recorran completo, incluyendo el camino
   de error de dominio (`E_PERIODO_NO_MODIFICABLE`, `E_RANGO_INVALIDO`).
8. **Contrato.** Para cada respuesta JSON del endpoint, generar un test que valide
   presencia y tipo de cada clave del DTO de `infrastructure/.../rest/*Response.java` —
   nunca el valor exacto si puede variar. Usar `com.networknt:json-schema-validator`
   (agregar a `pom.xml` si falta) o, sin dependencia nueva, aserciones Jackson `JsonNode`
   contra el `Map<String,Class<?>>` esperado.
9. **Re-medición.** `mvn -Punit verify` de nuevo; registrar el delta de cobertura y
   actualizar `docs/qa/matriz-fsd-uc-tests.md` (`PARCIAL`→`OK`) y, si cambia el número
   global, el baseline de `docs/qa/README.md`.

---

## 5. Salida esperada

| Artefacto | Archivo | Contenido |
|-----------|---------|-----------|
| Tests unitarios nuevos | `backend/src/test/.../application/service/<Servicio>Test.java` | Casos límite/negativos auditados, marcados `@Disabled` los duplicados |
| Tests de integración | `backend/src/test/.../<Flujo>IntegrationTest.java` | Camino feliz + camino de error del flujo elegido |
| Tests de contrato | `backend/src/test/.../<Endpoint>ContractTest.java` | Validación de forma/tipo del JSON de respuesta |
| Registro de auditoría | `docs/qa/matriz-fsd-uc-tests.md` (fila actualizada) | Estado `PARCIAL`→`OK`, cobertura antes/después |

---

## 6. Verificación (criterios de "bien hecho")

- [ ] `mvn test` / `mvn -Punit verify` en verde, incluye `ModularityTests` 7/7.
- [ ] Cobertura `domain/`+`application/` no baja del umbral vigente en `docs/qa/README.md`.
- [ ] Golden tests (`FloorTest`, `SIEPayloadTest`, `VentanaTest`, `MultitenantTest`) intactos
      si el módulo tocado los ejecuta.
- [ ] Cada test nuevo pasó las 3 preguntas de auditoría de §4.5 — dejar constancia (comentario
      o fila en la matriz), no solo "quedó en verde".
- [ ] Ningún test duplicado fue borrado sin `@Disabled(reason=...)`.
- [ ] `docs/qa/matriz-fsd-uc-tests.md` actualizada para el `FSD-UC` tocado.
- [ ] `mvn checkstyle:check` sin warnings **nuevos** (el gap preexistente de `sun_checks`
      está documentado aparte, no lo introduce este skill).

---

## 7. Anti-patrones

| Anti-patrón | Por qué es un error | Mitigación |
|-------------|---------------------|------------|
| Aceptar un test generado sin pasar las 3 preguntas | Rompe el principio "auditoría humana obligatoria" del laboratorio M7 | §4.5 es un paso bloqueante, no opcional |
| `assert valor == 0.3333333333333333` (igualdad estricta de flotante) | Es una foto del redondeo, no del comportamiento esperado | Usar `Offset`/tolerancia o comparar contra la fracción exacta |
| Borrar un test duplicado en vez de `@Disabled` | Pierde trazabilidad de qué se descartó y por qué | §4.6: siempre `@Disabled(reason=...)` |
| Test de contrato que valida el texto exacto de una respuesta LLM (`shared.ai`) | El contenido es probabilístico; el test se rompe con cada corrida | Validar solo forma/tipo del JSON, nunca el contenido generado |
| Calcular el promedio/redondeo dentro del propio test en vez de contra el motor real | Viola `AGENTS.md §6` (el cálculo vive solo en el dominio) | El test siempre llama al motor real (`CalculoNotas`), nunca reimplementa la fórmula |

---

## 8. Mini ejemplo de invocación

> "Usa el skill `ai-test-generator-edusync` sobre `academico.application.service.
> UpsertCalificacionesService` y `ObtenerNotaProvisionalService` (`FSD-UC-016`, ya
> completo) — no tienen test de `application` dedicado, solo cobertura vía dominio +
> integración (`docs/qa/matriz-fsd-uc-tests.md`, gap priorizado #3)."

---

## 9. Modos de fallo conocidos

- **`E_SIN_TEST_MANUAL`** — no existe ningún test manual de referencia → escribir uno antes
  de invocar al agente; STOP hasta tenerlo.
- **`E_FSD_UC_NO_COMPLETO`** — el `FSD-UC` objetivo sigue "pendiente" en `DTP.md §A.3` →
  usar `feature-design-doc` primero, este skill no aplica todavía.
- **`E_AUDITORIA_INCOMPLETA`** — un test generado se integra sin que conste que pasó las 3
  preguntas → bloquear el commit, mismo criterio que un golden test roto.
- **`E_CONTRATO_SOBRE_PROBABILISTICO`** — un test de contrato asertando contenido textual
  generado por el LLM en vez de forma/tipo → reescribir antes de aceptarlo.

---

## 10. Registro de cambios del Skill

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 14/09/2026 | Rodrigo Aspeti (vía Claude) | Versión inicial; piloto planeado sobre `CalculoNotas`/`CalificacionEvaluacion` (`FSD-UC-016`) | `AGENTS.md` v0.50, `docs/product/DTP.md` v1.42, `docs/qa/README.md` (baseline 2026-09-10), `docs/qa/matriz-fsd-uc-tests.md` (13/09/2026), laboratorio `LabX_M7_unit` (README + `PROMPT_AGENTE.md`), documento de proyecto "El objetivo central de este laboratorio" |
| 0.2.0 | 14/09/2026 | Rodrigo Aspeti (vía Auto) | Fase 1 HITL en CLI: `--proponer` (default), `--generar --aprobar-alcance`, `--ejecutar-tests`, `--decidir`; sesiones en `docs/qa/ai-test-sessions/` | `tools/ai-test-generator-edusync/GenerarTest.java`, `docs/qa/README.md` |
