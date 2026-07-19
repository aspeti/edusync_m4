---
producto: "EduSync"
grupo: "G-EduSync"
diagrama: "c4_level3_sie_adapter"
nivel: "C4 - Component (Level 3)"
contenedor: "sie-adapter"
version: v0.1.0
fecha: "28/05/2026"
autor: "Rodrigo Aspeti"
estado: borrador
prompt: "PR-C4-005 (registrado en docs/PROMPT_MAPPING.md v1.5)"
skill: ".cursor/skills/c4-edusync/SKILL.md v0.3.0"
fuente_principal: "docs/adr/0005-resiliencia-integracion-sie-resilience4j.md"
fuentes_secundarias:
  - "docs/fsd/FSD_EduSync.md v1.0 §4.3"
  - "docs/DTI.md v0.4 §6.1"
  - "docs/pocs/POC-02-circuit-breaker-sie/README.md v0.1"
artefacto_mermaid: "docs/diagrams/c4_level3_sie_adapter.mmd"
---

# C4 Level 3 - `sie-adapter` (EduSync)

> Diagrama de componentes del adaptador de integración con el SIE ministerial. El `.mmd` usa cabecera `C4Component`; este `.md` es su espejo narrativo obligatorio (IG-09).

---

## 1. Alcance y frontera

El contenedor `sie-adapter` implementa el puerto de salida `SIEExportPort` definido por el `domain-layer`. Su objetivo es aislar el protocolo externo del SIE, aplicar resiliencia con Resilience4j, evitar duplicados mediante estado por registro, y publicar métricas operativas.

Quedan fuera de esta frontera:

- `domain-layer`: decide cuándo exportar y qué registros son elegibles.
- `api-gateway`: recibe la solicitud de Secretaría.
- `scheduler`: dispara reintentos cada 5 minutos.
- `postgres-rls`: persiste `exportacion_sie_estado` con RLS.
- `SIE`: sistema externo sin SLA garantizado.

---

## 2. Componentes

| Componente | Tecnología | Responsabilidad | Trazabilidad |
|------------|------------|-----------------|--------------|
| `SIEHttpClientAdapter` | Spring RestClient + Resilience4j | Implementa `SIEExportPort`; ejecuta llamadas HTTPS/REST al SIE. | DA-05, FSD-UC-004 |
| `SIEPayloadMapper` | Java mapper | Convierte `Centralizador OFICIAL` a payload RUDE-only. | BR-004, NFR-004 |
| `IdempotencyGuard` | Java service | Consulta estado por `(rude, periodo_id)` antes de enviar; evita duplicados. | NFR-011 |
| `ExportacionEstadoWriter` | Spring Data JPA adapter | Escribe estados `PENDIENTE`, `ENVIADO`, `FALLIDO`, `EXCLUIDO_*` con RLS. | ADR-0005, ADR-0001 |
| `SIECircuitBreakerConfig` | Resilience4j config | Centraliza `failureRateThreshold`, timeout, retry y backoff. | NFR-012 |
| `SIEErrorMapper` | Java mapper | Normaliza errores HTTP/red a estados de exportación sin filtrar PII. | NFR-003 |
| `SIEMetricsPublisher` | Micrometer / CloudWatch | Publica métricas de éxito, fallo, circuit state y retries. | ADR-0005 |
| `SIEWireMockStub` | WireMock | Simula timeout, 5xx, fallo parcial y recuperación en tests. | POC-02 |

---

## 3. Tabla de trazabilidad obligatoria

| FSD-UC | Contenedor C4 | Componente nivel 3 | DA/BR/NFR aplicado |
|--------|---------------|--------------------|--------------------|
| FSD-UC-004 | `sie-adapter` | `SIEHttpClientAdapter` | DA-05, NFR-012 |
| FSD-UC-004 | `sie-adapter` | `SIEPayloadMapper` | BR-004, NFR-004 |
| FSD-UC-004 | `sie-adapter` | `IdempotencyGuard` | NFR-011 |
| FSD-UC-004 | `sie-adapter` | `ExportacionEstadoWriter` | ADR-0005, DA-01 |
| FSD-UC-004 | `sie-adapter` | `SIEErrorMapper`, `SIEMetricsPublisher` | NFR-003, ADR-0005 |
| POC-02 | `sie-adapter` | `SIEWireMockStub`, `SIECircuitBreakerConfig` | Validación pendiente con WireMock |

---

## 4. Reporte de validate

- [x] Cabecera Mermaid `C4Component`.
- [x] Cada componente tiene tecnología explícita.
- [x] Cada relación declara protocolo: `HTTPS/REST`, `JDBC/TLS`, `AWS SDK/TLS`, `Micrometer`, `HTTP local/test`.
- [x] `Math.floor()` no aparece en el `sie-adapter`; el cálculo vive en `ConsolidacionDomainService`.
- [x] El payload SIE usa RUDE como única clave visible; no incluye nombre, apellido, fecha de nacimiento ni posición de lista.
- [x] La idempotencia por `(rude, periodo_id)` se modela antes de cada envío.
- [x] Circuit breaker, timeout, retry y scheduler se trazan contra ADR-0005.
- [x] WireMock queda modelado como test adapter, no como componente productivo.

### Gaps resueltos

| Gap | Resolución |
|-----|------------|
| El SIE no garantiza idempotencia. | `IdempotencyGuard` + `exportacion_sie_estado` evitan reenvíos de registros ya marcados `ENVIADO`. |
| Fallo parcial al registro 47/80. | `ExportacionEstadoWriter` persiste estado por registro; `SIERetryScheduler` reintenta solo `PENDIENTE`/`FALLIDO`. |
| POC-02 aún no ejecutada. | Se referencia como validación pendiente; no se declara veredicto ni métrica real. |

---

## 5. Convivencia con otros diagramas

| Archivo | Relación |
|---------|----------|
| `c4_level3_api_gateway.mmd` | `ExportacionController` inicia el flujo. |
| `c4_level3_domain_layer.mmd` | `ExportacionDomainService` decide elegibilidad y usa `SIEExportPort`. |
| `deployment_aws.mmd` | Muestra Secrets Manager, CloudWatch, ECS y RDS como despliegue cloud. |

---

## 6. Registro de cambios

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 28/05/2026 | Rodrigo Aspeti | Versión inicial generada por `c4-edusync` v0.3.0. Cubre SIEHttpClientAdapter, payload RUDE-only, idempotencia, estado por registro, circuit breaker, métricas y WireMock. | ADR-0005, FSD v1.0, DTI v0.4, POC-02 v0.1 |
