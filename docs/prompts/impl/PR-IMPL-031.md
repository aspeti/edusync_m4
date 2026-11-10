# PR-IMPL-031 — Exportación de la nota final de periodo al SIE

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-031` |
| Título | Módulo `notassie`: exportación asíncrona e idempotente de la nota final de periodo |
| Artefacto origen | `docs/design/DD-UC-031.md`, `docs/adr/0025-sie-recibe-nota-final-de-periodo-y-retiro-de-floor.md` |
| ID origen | `DD-UC-031`, `ADR-0025`, `ADR-0005`, `ADR-0013`, `FSD-UC-004`, `FSD-UC-003`, `FSD-UC-016`, `NFR-007` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aprobado (prompt)** — ejecutar solo cuando `ADR-0025` esté *Aceptada* |

> No ejecutar este prompt como parte del diseño. La implementación es un turno aparte.

## 1. Task

Implementar `docs/design/DD-UC-031.md` §2: cálculo de notas finales por lote en `academico`, módulo `notassie` (dominio, aplicación, persistencia `V14`, API, evento tras commit, scheduler de reintentos, `SieClientPort` con Resilience4j) y consola Angular mínima.

## 2. Stop condition

`mvn test` en verde, incluidos `ModularityTests` (7/7) y los casos del §6 del design doc. `ng build` en verde. Ningún test llama a un SIE real. Ningún log INFO contiene `rude`, nota ni cuerpo de respuesta del SIE.

## 3. Anatomía

### Role

Eres el servicio de dominio de exportación al SIE de EduSync (Java 25, Spring Boot 4.1.0, arquitectura hexagonal, Spring Modulith). Tu responsabilidad es enviar al SIE la nota final de periodo de cada estudiante sin duplicar envíos ni perder avance ante fallos.

### Task

Implementa en este orden, con commit lógico entre pasos si el humano lo pide:

1. `CalculoNotas.notaPeriodoSiCompleto` + prueba (no modificar `calcularNotaPeriodo`).
2. `academico.NotasFinalesPeriodoConsultaPort` (raíz de `academico`) + servicio por lote. Extraer `armarSecciones` a un colaborador compartido con `ObtenerNotaProvisionalService`. Añadir a los repositorios solo lo que falte (consulta de calificaciones por varias evaluaciones, estudiantes por ids), verificando primero qué existe.
3. `plataforma.TenantActivosPort` (raíz de `plataforma`) con su implementación.
4. `notassie.domain`: `ExportacionSie`, `RegistroExportacion`, `EstadoExportacion`, `EstadoRegistroExportacion`, `RudeValidador`.
5. `notassie.application`: puertos y servicios del §2.4 del design doc. `@EnableAsync` + `TaskExecutor` acotado en `EduSyncApplication`/configuración.
6. `notassie.infrastructure`: JPA + `V14__notassie_exportacion_sie.sql` (RLS `FORCE`), `ExportacionSieController`, `SieHttpClientAdapter` (Resilience4j programático), `SieSimuladoAdapter`, `SieRetryScheduler`, propiedades `edusync.sie.*`.
7. Frontend: `features/notassie/exportacion-sie.page.ts`, ruta, enlace en el shell. Si `PR-IMPL-032` ya se ejecutó (`ADR-0026` Aceptada), esta pantalla **MUST** usar PrimeNG (mismo tema EduSync). Si no, se construye con el patrón actual y se migra después.
8. Corregir las dos frases del corpus que reservan `floor()` al SIE en `docs/ai/base_conocimiento/` y `backend/src/main/resources/ai/base_conocimiento/` (`exportacion_sie.md`, `calculo_notas.md`), y ajustar los tests o evals que las citen.
9. Actualizar `notassie/package-info.java` (hoy habla de `floor` y de `FSD-UC-001/003/004/005/009`).

### Context

- Documentos: `DD-UC-031`, `ADR-0025`, `ADR-0005`, `ADR-0013`, `ADR-0014`, `ADR-0011`, `docs/product/FSD.md` §4.2/§4.3/§4.6, `docs/product/DTP.md`.
- Stack: Java 25 LTS, Spring Boot 4.1.0, PostgreSQL 15, Flyway (siguiente versión `V14`), Angular 21.
- Dependencia nueva autorizada por `ADR-0025` §3.6: `io.github.resilience4j:resilience4j-circuitbreaker` (módulo núcleo, sin starter). Antes de fijar la versión, verificar compatibilidad con Java 25 y Spring Boot 4.1.0 y anotar la versión elegida en el `pom.xml`. Ninguna otra dependencia nueva. WireMock no se añade: se usa un `SieClientPort` falso.
- Restricciones activas:
  - `round` HALF_UP en todo cálculo. Sin `floor()`.
  - La nota la calcula solo `CalculoNotas`. Ni SQL, ni adaptadores, ni frontend.
  - Payload al SIE: `rude`, materia, periodo y nota. Sin nombre ni posición de lista.
  - `TenantContext` es `ThreadLocal`: el hilo asíncrono y el scheduler lo fijan y lo limpian en `finally`.
  - Todas las tablas con `tenant_id NOT NULL` + RLS `FORCE`. `tenant_id` nunca viene del cliente.
  - Modo SIE por defecto `desactivado` (`503 E_SIE_NO_CONFIGURADO`). `simulado` solo en `application-dev.yml`.
  - Secretos solo por variables de entorno (`SIE_BASE_URL`, `SIE_API_TOKEN`). `.env.example` los documenta sin valores.
  - Sin PII, notas ni cuerpo del SIE en logs INFO o superiores.
  - Sin editar `docs/baseline/**` ni migraciones aplicadas (`V1`..`V13`).
  - Dominio de `notassie` con Lombok solo `@Getter` (misma postura que `academico`).
- No implementar: inmutabilidad, `append-only`, `AutorizacionCorreccion`, `audit_log`, centralizador, promedio anual, boletines, notificaciones, cierre de materia.

### Reasoning

1. Verificar en el código qué puertos y métodos de repositorio existen antes de añadir ninguno.
2. Definir los tests del §6 del design doc antes del código de cada capa.
3. Mantener `domain/` sin Spring ni JPA. Resilience4j solo en `infrastructure`.
4. Un registro `ENVIADO` es terminal: ninguna ruta de código puede reenviarlo ni modificarlo.
5. Cada resultado de envío se persiste en su propia transacción para conservar el avance.
6. Comprobar con `ModularityTests` que no hay ciclos (`notassie → academico`, `notassie → plataforma`).

### Output

Código en `backend/src/main/java/com/edusync/{academico,plataforma,notassie}`, `V14__notassie_exportacion_sie.sql`, `application.yml`/`application-dev.yml`, `.env.example`, tests JUnit 5, `frontend/src/app/features/notassie/**`, rutas y shell. Al cerrar: `dtp-sync`, actualización de `DD-UC-031` (DoD) y de este prompt a «Ejecutado».

### Invariants

- Un solo motor de cálculo. `CalculoNotas` no cambia su comportamiento existente.
- Solo un periodo `CERRADO` es exportable (`422 E_PERIODO_NO_CERRADO`).
- La clave de idempotencia es `estudiante (rude) + materia + periodo`. `ENVIADO` nunca se reenvía.
- Aislamiento entre tenants: un `id` ajeno devuelve `404`.
- Todo envío al SIE pasa por circuit breaker con timeout de 30 s.

### Failure modes

- `E_FLOOR_REINTRODUCIDO`: aparece `floor()` o `Math.floor` en cálculo de notas. Rechazar.
- `E_CALCULO_FUERA_DOMINIO`: la nota se calcula fuera de `CalculoNotas`. Rechazar.
- `E_PAYLOAD_CON_PII`: el payload o un log lleva nombre, posición de lista, nota o RUDE en INFO. Rechazar.
- `E_REENVIO_DE_ENVIADO`: un registro `ENVIADO` se envía o se modifica. Rechazar.
- `E_TENANT_CONTEXT_PERDIDO`: el hilo asíncrono o el scheduler consultan sin fijar `TenantContext`. Rechazar.
- `E_SIE_SIMULADO_EN_DEFECTO`: el modo `simulado` queda activo fuera de `dev`. Rechazar.
- `E_DEPENDENCIA_NUEVA`: se añade algo distinto de `resilience4j-circuitbreaker`. Rechazar.
- `E_RLS_FALTANTE`: tabla nueva sin `tenant_id` o sin política RLS. Rechazar la migración.
- `E_BASELINE_TOCADO`: revertir.

## 4. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 10/11/2026 | G-EduSync | Prompt aprobado, ejecución pendiente |
