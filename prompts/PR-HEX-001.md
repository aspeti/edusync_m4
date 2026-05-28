# PR-HEX-001 — Diseño de la arquitectura hexagonal del core EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-HEX-001` |
| Título | Diseño de la arquitectura hexagonal del core EduSync v0.1 |
| Artefacto origen | FSD + arquitectura funcional |
| ID origen | `FSD-UC-001..010`, `DA-01..DA-05` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 24/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Arquitecto Senior con experiencia profunda en arquitectura hexagonal
(Ports & Adapters), Domain-Driven Design y plataformas SaaS multitenant
en el stack EduSync (Java 21, Spring Boot 3.3, Spring Security 6,
Spring Data JPA, PostgreSQL 15, Angular 17).
```

### 1.2 Task
```text
Diseña la arquitectura hexagonal del core de EduSync identificando
puertos de entrada (casos de uso), puertos de salida (persistencia,
mensajeria, terceros), adaptadores correspondientes y Aggregate Roots
con sus invariantes verificables.
```

### 1.3 Context
```text
- Casos de uso criticos: FSD-UC-001..010 en docs/fsd/FSD_EduSync.md
- Entidades candidatas: modelo ER de 16 entidades en FSD §6.1
- Decisiones arquitectonicas: DA-01..DA-05 en docs/arquitectura_funcional_EduSync.md
- Reglas de negocio: BR-001..BR-012 en docs/fsd/FSD_EduSync.md §5
- Constitucion: 5 principios no negociables en docs/prd/PRD_EduSync.md
- Diseno previo: docs/LFSD-EduSync.md §2-§3 (estructura de paquetes)
- Stack autoritativo: Spring Boot 3.3, Spring Security 6, Spring Data JPA,
  Angular 17, PostgreSQL 15
```

### 1.4 Reasoning
```text
1. Identificar puertos de entrada (casos de uso) — uno por FSD-UC y
   por scheduler/listener; agrupar workflows complejos en sub-puertos.
2. Identificar puertos de salida (persistencia, mensajeria, terceros) —
   un puerto por agregado + DomainEventPublisher + SIEExportPort +
   KmsCipherPort + BoletinPdfPort + NotificacionPort + TenantContextProvider
   + ClockPort.
3. Asignar un adaptador concreto por cada puerto OUT (Spring Data JPA,
   Resilience4j, AWS SDK, PDFBox, Spring Events). Adaptadores IN incluyen
   REST Controllers + Schedulers + Listeners + Security Filters.
4. Determinar Aggregate Roots (8): GestionAcademica, PeriodoAcademico,
   Estudiante, Calificacion (append-only), Centralizador, ExportacionSIE,
   CorreccionRetroactiva, AuditLogEntry. Por cada AR: listar invariantes
   citando BR-NNN y DA-NN que justifican.
```

### 1.5 Stop condition
```text
Detente al entregar las 4 tablas requeridas (puertos IN, puertos OUT,
adaptadores in/out, Aggregate Roots con invariantes) y el archivo
docs/arquitectura_hexagonal_EduSync.md v0.1 persistido.
```

### 1.6 Output
```text
docs/arquitectura_hexagonal_EduSync.md v0.1 (283 lineas) con:
- Mapa hexagonal Mermaid + estructura de paquetes Java
- Tabla 1: 20 puertos IN (UC + scheduler + listener)
- Tabla 2: 16 puertos OUT (persistencia + mensajeria + terceros)
- Tabla 3: 32 adaptadores (15 IN + 17 OUT) con tecnologia y ubicacion
- Tabla 4: 8 Aggregate Roots con invariantes BR-001..BR-012 verificables
- Materializacion DA-01..DA-05 en hexagonal
- Catalogo de 4 eventos de dominio
- Checklist de implementacion para dev-agent
```

## 2. Invariantes del prompt

- `domain/` no importa Spring/JPA/AWS (DA-02).
- Cada AR tiene al menos una invariante que cita un BR-NNN específico.
- Cada puerto IN se mapea a un FSD-UC vigente; cero puertos huérfanos.
- Mermaid sin Unicode decorativo en labels.
- Sin secretos ni PII en el documento.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_PUERTO_SIN_UC` | Puerto IN sin FSD-UC asignado | Rechazar y completar trazabilidad |
| `E_AR_SIN_INVARIANTE` | Aggregate Root sin invariante BR-NNN | Rechazar |
| `E_DOMINIO_CON_SPRING` | domain/ con imports de Spring/JPA | Rechazar (DA-02) |
| `E_ADAPTER_SIN_PUERTO` | Adaptador sin puerto que implementa | Rechazar |

## 4. Guardrails

- MUST: validar que todos los puertos IN tienen UC correspondiente.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia`.
- MUST NOT: importar Spring/JPA en el paquete `domain/`.
- MUST NOT: almacenar PII en ningún artefacto generado.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| FSD | `FSD-UC-001..010` | PR-HEX-001 | `arch-agent` | `docs/arquitectura_hexagonal_EduSync.md` v0.1 |
| Arquitectura funcional | `DA-01..DA-05` | PR-HEX-001 | `arch-agent` | Materialización DAs en hexagonal |
| BRD | `BR-001..BR-012` | PR-HEX-001 | `arch-agent` | Invariantes de Aggregate Roots |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: FSD completo con 10 UCs, ER con 16 entidades, DA-01..DA-05.
- **Output esperado**: 20 puertos IN, 16 puertos OUT, 32 adaptadores, 8 ARs con invariantes BR.

### 6.2 Caso borde
- **Input**: FSD con un UC sin entidades definidas (UC-07 Boletines).
- **Output esperado**: el puerto IN de UC-07 se define con el conjunto mínimo de entidades inferibles.

### 6.3 Caso adversarial
- **Input**: propuesta de colocar lógica de negocio en un adaptador.
- **Comportamiento esperado**: rechazo con `E_DOMINIO_CON_SPRING`; la lógica se mueve al domain service.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 24/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
