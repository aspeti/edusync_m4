---
producto: "EduSync"
grupo: "G-EduSync"
diagrama: "c4_level3_domain_layer"
nivel: "C4 - Component (Level 3)"
contenedor: "domain-layer"
version: v0.1.0
fecha: "28/05/2026"
autor: "Rodrigo Aspeti"
estado: borrador
prompt: "PR-C4-004 (registrado en docs/PROMPT_MAPPING.md v1.5)"
skill: ".cursor/skills/c4-edusync/SKILL.md v0.3.0"
fuente_principal: "docs/arquitectura_hexagonal_EduSync.md v0.1"
fuentes_secundarias:
  - "docs/fsd/FSD_EduSync.md v1.0"
  - "docs/DTI.md v0.4 §5"
  - "docs/adr/0002-parametrizacion-reglas-normativas.md"
  - "docs/adr/0004-async-consolidacion-spring-events.md"
artefacto_mermaid: "docs/diagrams/c4_level3_domain_layer.mmd"
---

# C4 Level 3 - `domain-layer` (EduSync)

> Diagrama de componentes del núcleo hexagonal de EduSync. El `.mmd` asociado usa cabecera `C4Component` y este archivo es su espejo narrativo obligatorio (IG-09).

---

## 1. Alcance y frontera

El contenedor `domain-layer` representa el núcleo de negocio puro: puertos IN/OUT, servicios de dominio, Aggregate Roots, Value Objects y Domain Events. No contiene Spring, JPA, AWS SDK, SQL ni controllers.

Quedan fuera de esta frontera:

- `api-gateway`: filtros, controllers, DTOs y `AuditLogAspect`.
- `postgres-rls`: adapters de persistencia, RLS y mapeos JPA.
- `sie-adapter`: transporte HTTP, Resilience4j y WireMock.
- `event-bus`: implementación `SpringEventPublisherAdapter` y futuros SQS FIFO.
- `AWS KMS`: cifrado efectivo de PII mediante adaptador externo.

---

## 2. Componentes

| Componente | Tecnología | Responsabilidad | Trazabilidad |
|------------|------------|-----------------|--------------|
| `Input Ports` | Interfaces Java puras | Exponen los use cases FSD-UC-001, 002, 003, 004, 005 y 009 al borde web/scheduler/eventos. | FSD-UC-001..009 |
| `Output Ports` | Interfaces Java puras | Abstraen repositorios, eventos, SIE, KMS, reloj y tenant context. | DA-01, DA-04, DA-05 |
| `CalificacionDomainService` | Java 21 domain service | Valida docente, periodo, rangos, RUDE y emite `CalificacionRegistradaEvent`. | BR-001, BR-002, BR-004, BR-007 |
| `ConsolidacionDomainService` | Java 21 domain service | Unico dueño de `Math.floor()` y de centralizadores `PROVISIONAL`/`OFICIAL`. | BR-003, BR-008, BR-011 |
| `ExportacionDomainService` | Java 21 domain service | Orquesta exportación SIE e idempotencia por `(rude, periodo_id)`. | BR-004, NFR-011 |
| `CorreccionDomainService` | Java 21 domain service | Controla autorización jerárquica, ventana 1-72h y corrección append-only. | BR-005, BR-009, BR-010 |
| `PeriodoDomainService` | Java 21 domain service | Gestiona secuencia T1 -> T2 -> T3 e inmutabilidad de parámetros en `ABIERTO`. | BR-006, BR-007 |
| `Aggregate Roots` | Plain Java model | GestionAcademica, PeriodoAcademico, Estudiante, Calificacion, Centralizador, ExportacionSIE, CorreccionRetroactiva, AuditLogEntry. | DA-02 |
| `Value Objects` | Plain Java records/classes | Rude, TenantId, Dimension, ValorCalificacion, PuntajeTotal, Ventana, ClaveIdempotencia, Snapshot. | BR-004, BR-008 |
| `Domain Events` | Java records inmutables | CalificacionRegistradaEvent, MateriaCerradaEvent, CentralizadorOficialEvent, VentanaExpiradaEvent. | DA-04 |

---

## 3. Tabla de trazabilidad obligatoria

| FSD-UC | Contenedor C4 | Componente nivel 3 | DA/BR/NFR aplicado |
|--------|---------------|--------------------|--------------------|
| FSD-UC-001 | `domain-layer` | `RegistrarCalificacionUseCase`, `CalificacionDomainService`, `Calificacion`, `Dimension`, `ValorCalificacion` | BR-001, BR-002, BR-004, BR-007 |
| FSD-UC-002 | `domain-layer` | `CerrarMateriaUseCase`, `MateriaCerradaEvent` | DA-04, completitud 100 % |
| FSD-UC-003 | `domain-layer` | `ConsolidarCentralizadorUseCase`, `ConsolidacionDomainService`, `Centralizador`, `PuntajeTotal` | BR-003, BR-008, BR-011 |
| FSD-UC-004 | `domain-layer` | `ExportarSIEUseCase`, `ExportacionDomainService`, `ExportacionSIE`, `ClaveIdempotencia`, `SIEExportPort` | BR-004, DA-05, NFR-011 |
| FSD-UC-005 | `domain-layer` | `GestionarCorreccionUseCase`, `CorreccionDomainService`, `CorreccionRetroactiva`, `Ventana` | BR-005, BR-009, BR-010 |
| FSD-UC-009 | `domain-layer` | `GestionarPeriodoUseCase`, `PeriodoDomainService`, `PeriodoAcademico`, `ParametroAcademico` | BR-006, BR-007 |

---

## 4. Reporte de validate

- [x] Cabecera Mermaid `C4Component`.
- [x] Todos los componentes tienen tecnología explícita.
- [x] Cada relación declara protocolo: `In-process`, `Port IN`, `Port OUT`, `JDBC/TLS`, `Spring Event`, `AWS KMS SDK/TLS`.
- [x] `Math.floor()` aparece únicamente en `ConsolidacionDomainService`.
- [x] El dominio no conoce Spring, JPA, AWS SDK ni SQL.
- [x] `audit_log` no se escribe desde el dominio; se expresa como intención por puerto OUT y lo materializa `AuditLogAspect` fuera de esta frontera.
- [x] RLS no vive en el dominio; se resuelve por `TenantContextProvider` + `postgres-rls`.
- [x] La tabla de trazabilidad cubre los FSD-UC críticos y FSD-UC-002 como evento de cierre.

### Gaps resueltos

| Gap | Resolución |
|-----|------------|
| FSD-UC-002 no estaba entre los 5 UC críticos del skill. | Se incluye porque `CerrarMateriaUseCase` produce `MateriaCerradaEvent`, evento necesario para FSD-UC-003 y DA-04. |
| `AuditLogEntry` aparece como Aggregate Root, pero la escritura real ocurre fuera del dominio. | Correcto: el dominio define la intención/contrato; `AuditLogAspect` y el adaptador JPA viven fuera del `domain-layer`. |

---

## 5. Convivencia con otros diagramas

| Archivo | Relación |
|---------|----------|
| `c4_level3_api_gateway.mmd` | Invoca los `Input Ports`; no ejecuta reglas de negocio. |
| `c4_level3_sie_adapter.mmd` | Implementa `SIEExportPort`; no contiene reglas de cálculo. |
| `deployment_aws.mmd` | Despliega `api-gateway + domain-layer` en ECS Fargate como monolito modular v1.0. |

---

## 6. Registro de cambios

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 28/05/2026 | Rodrigo Aspeti | Versión inicial generada por `c4-edusync` v0.3.0. Cubre puertos IN/OUT, 5 servicios de dominio, Aggregate Roots, Value Objects y Domain Events. | FSD v1.0, arquitectura_hexagonal v0.1, DTI v0.4, ADR-0002, ADR-0004 |
