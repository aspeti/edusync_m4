---
name: poc-runner-edusync
description: >
  Toma una ficha de POC declarada en `docs/pocs/POC-NN-<slug>/README.md`
  (creada con `plantillas/POC_TEMPLATE.md` y registrada en
  `docs/DTI.md §12`) y bootstrapea un scaffold ejecutable reproducible:
  rama `poc/POC-NN-<slug>` desde `release/2.0.0`, proyecto Maven minimal
  con Spring Boot 3.3 / Testcontainers / WireMock segun corresponda,
  scripts de ejecucion, captura de metricas y veredicto pass/fail
  contra el criterio SMART de la ficha. Si la POC falla, propone diffs
  a ADR-0001 / ADR-0005, `docs/DTI.md §12` y `AGENTS.md`. Skill
  EduSync-flavored del generico `plantillas/poc-runner.md`.
allowed-tools:
  - read
  - edit
  - run-tests
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: poc-runner-edusync — Ejecutor reproducible de POCs EduSync

> Skill canonica del proyecto EduSync. Mantener copia identica en
> `.claude/skills/poc-runner-edusync/SKILL.md` y `.cursor/skills/poc-runner-edusync/SKILL.md`.

---

## 1. Cuándo activarlo (triggers)

- **DURANTE**: ejecucion time-boxed de una POC critica para la defensa final, entre el momento en que la ficha documental (`docs/pocs/POC-NN-<slug>/README.md`) esta completa y el momento en que el resultado entra al `docs/DTI.md §12`.
- **ARRANCA cuando** el usuario dice:
  - `"@poc-runner-edusync POC-01"`
  - `"@poc-runner-edusync POC-02"`
  - `"bootstrapea la POC-01 RLS"` / `"ejecuta la POC-02 Circuit Breaker SIE"`
- **NO ACTIVAR cuando**:
  - La ficha de la POC no existe o esta incompleta (correr primero el flujo de `plantillas/POC_TEMPLATE.md`).
  - El criterio SMART no es verificable por script (es prosa).
  - La POC ya cerro con veredicto pass/fail registrado en `docs/DTI.md §12`.

---

## 2. Entradas obligatorias

| # | Dato | Fuente | Estado actual EduSync |
|---|------|--------|------------------------|
| 1 | Ficha de la POC | `docs/pocs/POC-NN-<slug>/README.md` | POC-01 y POC-02 ya documentadas (v0.1) |
| 2 | Runbook | `docs/pocs/POC-NN-<slug>/runbook.md` | Pasos placeholder presentes |
| 3 | Indice de evidencias | `docs/pocs/POC-NN-<slug>/evidencia/README.md` | Estado "Pendiente de ejecucion" |
| 4 | ADR vinculado | `docs/adr/0001-multitenancy-rls-postgresql.md` (POC-01) o `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` (POC-02) | Aceptados |
| 5 | DTI §12 | `docs/DTI.md §12.1` o `§12.2` | Definidos; resultado pendiente |
| 6 | Stack | `AGENTS.md §1` | Java 21 + Spring Boot 3.3 + PostgreSQL 15 + Testcontainers + WireMock |

La ficha de la POC MUST contener (verificacion previa del runner):

- Hipotesis falsable (`§2` del README).
- Alcance time-boxed (`§4`).
- Criterio SMART verificable por script (`§3`): umbral numerico + comando o aserción.
- Umbral de fracaso explicito.
- Plan de ejecucion por dias (`§8`).

Si falta cualquier campo: `"E_FICHA_INCOMPLETA: la ficha POC-NN está incompleta; me falta <campo>. No bootstrapeo hasta completarla."`

---

## 3. Fuentes de verdad (orden de precedencia)

1. Ficha `docs/pocs/POC-NN-<slug>/README.md` — autoritativa para alcance, hipotesis y criterio SMART.
2. ADR vinculado (estado `Aceptada` > `Propuesta`).
3. `docs/DTI.md §12` — referencia para resultado declarado.
4. `AGENTS.md` — stack autoritativo, golden tests, guardrails PII, restricciones de ejecucion.
5. `docs/fsd/FSD_EduSync.md` (NFR-001..NFR-016) — umbrales numericos.
6. Convenciones del repo: GitFlow (ADR-0013), branch `release/2.0.0` como base de las POCs de la defensa final.

---

## 4. Procedimiento

### Paso 1 — Validar ficha
- Verificar que `docs/pocs/POC-NN-<slug>/README.md` existe.
- Confirmar que `§3 Criterio de éxito medible` tiene umbral numerico y comando verificable.
- Confirmar que la POC tiene `Estado: Propuesta` y `Resultado: Pendiente de ejecucion`.
- Si la ficha ya esta cerrada (`pass`/`fail`/`abandono`) → STOP, redirigir a `sync-doc-chain`.

### Paso 2 — Crear branch
```
git checkout release/2.0.0
git pull origin release/2.0.0
git checkout -b poc/POC-NN-<slug>
```
Si `release/2.0.0` no existe aun, usar `release/1.0.1` (rama vigente del DTI) y dejar nota en `evidencia/log.md`.

### Paso 3 — Generar scaffold ejecutable
Crear bajo `docs/pocs/POC-NN-<slug>/`:

- `pom.xml` (Maven multi-module o standalone, Spring Boot 3.3 + JDK 21).
- `src/main/java/...` con codigo minimo de la POC.
- `src/test/java/...` con el test golden de validacion.
- `sql/` (POC-01) o `wiremock/mappings/` (POC-02).
- `scripts/run.sh` / `scripts/run.ps1` con comandos reproducibles por Claude Code.
- `Makefile` opcional con targets `make poc-NN-up`, `make poc-NN-run`, `make poc-NN-down`.

Reglas duras:
- `domain/` sin dependencias Spring/JPA (DA-02).
- Tablas nuevas DEBEN incluir `tenant_id NOT NULL` + politica RLS (DA-01).
- `SIEPayloadAdapter` (POC-02) DEBE usar solo RUDE como identificador (BR-004, NFR-004).
- Toda escritura inyecta entrada en `audit_log` (BR-010, DA-03) — incluso en POCs.

### Paso 4 — Ejecutar la POC
```
mvn -pl docs/pocs/POC-NN-<slug> -am clean test \
  -Dpoc.requests=<N> \
  -Dpoc.threads=<M> \
  -Dpoc.tenants=<UUIDs>
```
Capturar:
- `evidencia/test-output.txt` — stdout/stderr del `mvn test`.
- `evidencia/metrics.csv` — una fila por operacion (latencia, estado, tenant/idempotency key).
- `evidencia/log.md` — append con timestamp, comando, metrica final y veredicto.

### Paso 5 — Verificar contra criterio SMART
Por POC:

| POC | Verificacion script | Umbrales SMART |
|-----|---------------------|----------------|
| POC-01 (RLS) | `MultitenantTest.no_cross_tenant_data` + p95 calculado desde `metrics.csv` | 0 leaks cross-tenant en 1000 requests AND p95 INSERT/SELECT < 505 ms |
| POC-02 (CB SIE) | `CircuitBreakerTest` + `SIEPayloadTest.payload_uses_rude_only` + query SQL de duplicados | CB transita a OPEN con >= 60 % fail AND recovery < 15 min AND 0 duplicados `(rude, periodo_id)` |

El veredicto es `pass`, `fail` o `abandono` con metrica concreta. Nunca `parcial sin justificacion numerica`.

### Paso 6 — Registrar resultado
Append a `docs/pocs/POC-NN-<slug>/evidencia/log.md`:
```
## <timestamp ISO 8601>
- Comando: `<comando exacto>`
- Hipotesis: <texto>
- Criterio SMART: <umbral>
- Metrica observada: <valor>
- Veredicto: pass | fail | abandono
- Branch: poc/POC-NN-<slug>
- Commit: <SHA>
```

### Paso 7 — Si `fail` o cambia decision: proponer diffs
Generar un solo commit candidato con tres diffs propuestos (no aplicados automaticamente):
- `docs/adr/0001-multitenancy-rls-postgresql.md` (POC-01) o `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` (POC-02) — actualizar `§9 Historial` con la evidencia.
- `docs/DTI.md §12.1` o `§12.2` — cambiar `Resultado: Pendiente` → veredicto + enlace a `docs/pocs/POC-NN-<slug>/evidencia/`.
- `AGENTS.md` — solo si cambia el stack o se introduce un guardrail nuevo.

Si el ADR esta en estado `Aceptada` y la POC lo contradice: STOP, escalar al `arch-agent`. No actualizar ADRs `Aceptada` sin curacion humana (ver `E_ADR_ACCEPTED_CONFLICT`).

### Paso 8 — Cerrar la POC
En la ficha (`§9` y `§10` del `README.md` de la POC):
- Llenar tabla de metricas con valores reales.
- Marcar veredicto pass/fail/abandono.
- Marcar checklist de cierre.
- Invocar `sync-doc-chain` con disparador `docs/pocs/POC-NN-<slug>/README.md`.

---

## 5. Salida esperada

- Branch `poc/POC-NN-<slug>` con scaffold commiteado.
- `docs/pocs/POC-NN-<slug>/scripts/`, `src/`, `sql/` o `wiremock/` ejecutable.
- `docs/pocs/POC-NN-<slug>/evidencia/`: `metrics.csv`, `test-output.txt`, `log.md` y los archivos especificos de cada POC declarados en `evidencia/README.md`.
- Si `fail`: 3 diffs propuestos en un commit candidato (ADR + DTI + AGENTS).
- Tabla resumen al final del chat:

| Campo | Valor |
|-------|-------|
| POC | POC-NN — `<titulo>` |
| Hipotesis | `<1 linea>` |
| Criterio SMART | `<umbral>` |
| Metrica observada | `<valor>` |
| Time-box | `<gastado/maximo>` |
| Veredicto | `pass / fail / abandono` |
| Decision desbloqueada | `<texto>` |
| Branch | `poc/POC-NN-<slug>` |
| Commit | `<SHA>` |

---

## 6. Verificación (criterios de "bien hecho")

- [ ] Branch `poc/POC-NN-<slug>` existe; commits solo de la POC.
- [ ] README de la POC tiene comandos copiables sin `<placeholders>` sin definir.
- [ ] El scaffold se re-ejecuta de cero (sin estado oculto en la maquina del autor).
- [ ] El criterio SMART se verifica por **script** (no por inspeccion visual).
- [ ] `evidencia/metrics.csv`, `test-output.txt` y `log.md` commiteados.
- [ ] `MultitenantTest` (POC-01) o `CircuitBreakerTest` + `SIEPayloadTest` (POC-02) en verde.
- [ ] Si la POC fallo, los 3 diffs propuestos estan en el mismo commit candidato.
- [ ] Ningun log expone PII (RUDE, nombre, fecha_nacimiento) — NFR-003, NFR-007.

---

## 7. Anti-patrones específicos

| Anti-patron | Por que es un error | Mitigacion |
|-------------|---------------------|------------|
| POC infinita | Ignorar el time-box declarado en la ficha | Parar al alcanzar el time-box; registrar `abandono` con metrica parcial |
| POC sin criterio de abandono | Imposible declarar `fail` objetivo | Rechazar en Paso 1 si la ficha no lo declara |
| POC que se vuelve produccion | Codigo de POC migra a `release/*` | "La rama `poc/*` muere; los aprendizajes viajan al ADR, no el codigo" |
| Criterio difuso ("que sea rapido") | No es verificable por script | Rechazar; pedir umbral numerico + comando |
| POC no reproducible | Scripts solo corren en la maquina del autor | Exigir Testcontainers / WireMock estandar |
| PII real en payloads de prueba | Viola NFR-003 / Ley 164 Bolivia | Usar UUIDs y `RUDE-XXX` sinteticos siempre |
| Reescribir `Math.floor()` o RLS fuera de la POC | Viola BR-008 / DA-01 incluso en codigo experimental | Mantener invariantes de dominio en el codigo de la POC |
| Conflicto con ADR `Aceptada` sin escalado | Modificar ADR aceptado sin curacion humana | STOP, escalar al `arch-agent` |

---

## 8. Mini ejemplo de invocación

> "@poc-runner-edusync POC-01. Ficha: `docs/pocs/POC-01-rls-multitenancy/README.md`. Criterio SMART: 0 leaks cross-tenant en 1000 requests y p95 INSERT/SELECT < 505 ms. Time-box: 3 dias. Bootstrapea el scaffold, ejecuta, registra el log y dame el veredicto."

---

## 9. Modos de fallo conocidos

- **`E_FICHA_INCOMPLETA`** — ficha sin hipotesis / criterio SMART / time-box → STOP, pedir completar.
- **`E_CRITERIO_NO_EJECUTABLE`** — criterio en prosa, no por script → STOP, pedir reformular.
- **`E_NFR_INEXISTENTE`** — la ficha cita un NFR-NNN que no existe en el FSD → STOP, aclarar.
- **`E_POC_TIMEOUT_OOM`** — la POC no termina o agota memoria → registrar `abandono` con causa, proponer rediseño.
- **`E_ADR_ACCEPTED_CONFLICT`** — el resultado contradice un ADR en estado `Aceptada` → STOP, escalar al `arch-agent`, no actualizar el ADR.
- **`E_PII_EN_LOG`** — RUDE/nombre/fecha_nacimiento aparece en `metrics.csv` o `test-output.txt` → STOP, sanitizar antes de commitear.
- **`E_INVARIANT_VIOLATION`** — el codigo de la POC viola IG-01..IG-10 de `docs/PROMPT_MAPPING.md` → STOP, ajustar el scaffold.

---

## 10. Registro de cambios del Skill

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 28/05/2026 | Rodrigo Aspeti | Versión inicial — adaptación EduSync del skill genérico `plantillas/poc-runner.md`. Rutas canonizadas a `docs/pocs/POC-NN-<slug>/`; criterios SMART específicos por POC (POC-01 RLS multitenancy, POC-02 Circuit Breaker SIE); golden tests del proyecto (`MultitenantTest`, `CircuitBreakerTest`, `SIEPayloadTest`); branch base `release/2.0.0`; invariantes BR-004/BR-008/BR-010/DA-01 obligatorias en el scaffold. | POC_TEMPLATE.md, poc-runner.md, DTI v0.4, FSD v1.0, AGENTS v0.4, PROMPT_MAPPING v1.3, ADR-0001, ADR-0005 |
