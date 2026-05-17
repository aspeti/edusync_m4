# Referencia C4 — EduSync (ejemplos Mermaid listos para usar)

> Copiar el bloque correspondiente, pegarlo en `docs/diagrams/c4_level<N>.mmd`
> y ajustar las relaciones según el nivel solicitado.

---

## Nivel 1 — System Context

```
C4Context
  title EduSync — Contexto del sistema (Level 1)

  Person(director, "Director", "Jeanneth — gestiona periodos,\nautoriza correcciones retroactivas")
  Person(docente, "Docente", "Marcela — carga calificaciones\npor dimensión (Ser/Saber/Hacer/Decidir)")
  Person(secretaria, "Secretaria", "Wendy — exporta datos al SIE\n y gestiona nominas")

  System(edusync, "EduSync", "Plataforma SaaS B2B multitenant\nJava 21 / Spring Boot 3.3 / PostgreSQL 15\nCentraliza calificaciones y genera centralizadores\nsincronizados con el SIE boliviano")

  System_Ext(sie, "SIE — Ministerio de Educacion Bolivia", "Recibe exportacion de calificaciones\nidentificadas por RUDE (codigo unico estudiantil)")
  System_Ext(kms, "AWS KMS", "Cifrado en reposo de PII\n(RUDE, nombre, fecha de nacimiento)")

  Rel(director, edusync, "Configura periodos, habilita permisos,\nautoriza correcciones", "HTTPS")
  Rel(docente, edusync, "Carga calificaciones por dimension,\nsolicita correcciones retroactivas", "HTTPS")
  Rel(secretaria, edusync, "Exporta centralizadores al SIE,\ngestiona nominas de estudiantes", "HTTPS")
  Rel(edusync, sie, "Envia calificaciones por RUDE\n(POST /registro)", "HTTPS/REST")
  Rel(edusync, kms, "Cifra/descifra PII en reposo", "AWS SDK/TLS")
```

---

## Nivel 2 — Containers

```
C4Container
  title EduSync — Contenedores (Level 2)

  Person(director, "Director", "Gestiona periodos y autorizaciones")
  Person(docente, "Docente", "Carga calificaciones")
  Person(secretaria, "Secretaria", "Exporta al SIE")

  System_Ext(sie, "SIE Ministerio de Educacion", "Receptor de exportaciones academicas")
  System_Ext(kms, "AWS KMS", "Cifrado PII en reposo")

  System_Boundary(edusync, "EduSync") {
    Container(spa, "Angular SPA", "Angular 17, TypeScript", "Interfaz reactiva por rol\n(DIRECTOR / DOCENTE / SECRETARIA)")
    Container(api, "API Gateway", "Spring Boot 3.3, Java 21\nAWS ECS Fargate", "REST API con JWT, RBAC y RLS injection\nPuerto 443")
    Container(domain, "Domain Layer", "Java 21, arquitectura hexagonal\nsin dependencias de Spring", "Logica de negocio pura:\ncalificaciones, consolidacion, correcciones")
    ContainerDb(db, "PostgreSQL", "PostgreSQL 15, AWS RDS Multi-AZ\nRow-Level Security por tenant_id", "Persistencia principal:\ncalificaciones, centralizadores, audit_log")
    Container(queue, "Event Bus", "Spring Events + AWS SQS", "Consolidacion asincrona post-cierre\n(DA-04) — idempotencia garantizada")
    Container(sieadapter, "SIE Adapter", "Java 21, Resilience4j\nHTTPS/REST", "Circuit breaker + retry backoff\npara exportacion al SIE (DA-05)")
    Container(scheduler, "Scheduler", "Spring Scheduler @Scheduled", "VentanaExpiracionScheduler (BR-009)\nSIERetryScheduler (DA-05)")
  }

  Rel(director, spa, "Gestiona periodos y permisos", "HTTPS")
  Rel(docente, spa, "Carga calificaciones", "HTTPS")
  Rel(secretaria, spa, "Exporta al SIE", "HTTPS")
  Rel(spa, api, "Llamadas REST autenticadas", "HTTPS/REST, JWT")
  Rel(api, domain, "Invoca casos de uso", "In-process / Ports")
  Rel(domain, db, "Lee y escribe (RLS activo)", "JDBC/TLS, SET LOCAL app.tenant_id")
  Rel(domain, queue, "Publica CalificacionRegistradaEvent\nMateriaCerradaEvent", "Spring Event / SQS")
  Rel(queue, domain, "ConsolidarCentralizadorUseCase", "Spring Event async")
  Rel(sieadapter, sie, "POST /registro/{rude}", "HTTPS/REST, retry + circuit breaker")
  Rel(domain, sieadapter, "Delega exportacion", "In-process / Port")
  Rel(scheduler, domain, "Revoca ventanas expiradas\nreintenta exportaciones fallidas", "In-process")
  Rel(db, kms, "Cifrado columnas PII", "AWS KMS SDK/TLS")
```

---

## Nivel 3 — Componentes: `api-gateway`

```
C4Component
  title EduSync — Componentes de API Gateway (Level 3)

  Person(docente, "Docente", "Carga calificaciones")
  Person(director, "Director", "Gestiona periodos")
  Person(secretaria, "Secretaria", "Exporta al SIE")

  ContainerDb(db, "PostgreSQL 15 (RLS)", "Persistencia principal")
  Container(domain, "Domain Layer", "Logica de negocio hexagonal")

  System_Boundary(api, "API Gateway — Spring Boot 3.3") {
    Component(jwt, "JwtAuthFilter", "Spring Security 6\nOncePerRequestFilter", "Valida JWT, extrae rol y tenant_id\n(NFR-007, NFR-008)")
    Component(tenant, "TenantContextInjector", "Hibernate Interceptor\nHibernate EmptyInterceptor", "Ejecuta SET LOCAL app.tenant_id\nantes de cada TX (DA-01)")
    Component(calCtrl, "CalificacionController", "Spring MVC @RestController", "POST /api/v1/calificaciones\n(FSD-UC-001)")
    Component(centCtrl, "CentralizadorController", "Spring MVC @RestController", "GET /api/v1/centralizadores\n(FSD-UC-003)")
    Component(expCtrl, "ExportacionController", "Spring MVC @RestController", "POST /api/v1/exportaciones/sie\n(FSD-UC-004)")
    Component(corrCtrl, "CorreccionController", "Spring MVC @RestController", "POST/PUT /api/v1/correcciones\n(FSD-UC-005)")
    Component(perCtrl, "PeriodoController", "Spring MVC @RestController", "POST /api/v1/periodos\n(FSD-UC-009)")
    Component(audit, "AuditLogAspect", "Spring AOP @Around\n@Auditable", "Registra toda escritura en audit_log\nen la misma TX (DA-03, BR-010)")
    Component(errHandler, "GlobalExceptionHandler", "Spring @RestControllerAdvice", "Mapea DomainException a HTTP status\ny ErrorResponseDTO estandar")
  }

  Rel(docente, jwt, "POST /calificaciones", "HTTPS/REST, Bearer JWT")
  Rel(director, jwt, "POST /periodos, PUT /correcciones", "HTTPS/REST, Bearer JWT")
  Rel(secretaria, jwt, "POST /exportaciones/sie", "HTTPS/REST, Bearer JWT")
  Rel(jwt, tenant, "Pasa tenant_id validado", "In-process")
  Rel(tenant, db, "SET LOCAL app.tenant_id = ?", "JDBC/TLS antes de TX")
  Rel(calCtrl, domain, "RegistrarCalificacionUseCase", "In-process / Port")
  Rel(centCtrl, domain, "ObtenerCentralizadorUseCase", "In-process / Port")
  Rel(expCtrl, domain, "ExportarSIEUseCase", "In-process / Port")
  Rel(corrCtrl, domain, "GestionarCorreccionUseCase", "In-process / Port")
  Rel(perCtrl, domain, "CrearPeriodoUseCase", "In-process / Port")
  Rel(audit, db, "INSERT audit_log (append-only)", "JDBC/TLS en misma TX")
```

---

## Nivel 3 — Componentes: `domain-layer`

```
C4Component
  title EduSync — Componentes de Domain Layer (Level 3)

  Container(api, "API Gateway", "Invoca casos de uso via puertos")
  ContainerDb(db, "PostgreSQL 15 (RLS)", "Persistencia principal")
  Container(queue, "Event Bus", "Spring Events / SQS")
  Container(sieadapter, "SIE Adapter", "Adaptador externo")

  System_Boundary(domain, "Domain Layer — arquitectura hexagonal (sin deps Spring)") {
    Component(calSvc, "CalificacionDomainService", "Java 21 — Dominio puro", "Valida rango parametrico por dimension\nEmite CalificacionRegistradaEvent (FSD-UC-001, BR-002)")
    Component(consSvc, "ConsolidacionDomainService", "Java 21 — Dominio puro", "UNICO responsable de Math.floor()\ny calculo de promedios (BR-008, DA-04)")
    Component(expSvc, "ExportacionDomainService", "Java 21 — Dominio puro", "Mapea RUDE, verifica idempotencia\nestados EN_PROGRESO/COMPLETADO (FSD-UC-004)")
    Component(corrSvc, "CorreccionDomainService", "Java 21 — Dominio puro", "Gestiona ventana_fin, append-only\nAutorizacionCorreccion (BR-005, BR-009)")
    Component(perSvc, "PeriodoDomainService", "Java 21 — Dominio puro", "Secuencia PENDIENTE→CONFIGURADO→ABIERTO→CERRADO\n(FSD-UC-009, BR-006)")
    Component(calEntity, "CalificacionEntity @Immutable", "Hibernate @Immutable\n@SQLRestriction(deleted=false)", "Registro inmutable — toda correccion\ncrea nuevo registro (BR-005, DA-03)")
    Component(auditEntity, "AuditLogEntity @Immutable", "Hibernate @Immutable\nPostgreSQL RULE no_update_audit", "Tabla inalterable — sin UPDATE/DELETE\npor ningun path de codigo (DA-03, BR-010)")
  }

  Rel(api, calSvc, "RegistrarCalificacionUseCase", "In-process / Port")
  Rel(api, consSvc, "ConsolidarCentralizadorUseCase", "In-process / Port")
  Rel(api, expSvc, "ExportarSIEUseCase", "In-process / Port")
  Rel(api, corrSvc, "GestionarCorreccionUseCase", "In-process / Port")
  Rel(api, perSvc, "CrearPeriodoUseCase", "In-process / Port")
  Rel(calSvc, calEntity, "Persiste via CalificacionRepositoryPort", "JDBC/TLS, RLS activo")
  Rel(calSvc, queue, "Publica CalificacionRegistradaEvent", "Spring Event")
  Rel(queue, consSvc, "Escucha MateriaCerradaEvent", "Spring Event @Async")
  Rel(consSvc, db, "UPSERT centralizador (PROVISIONAL/OFICIAL)", "JDBC/TLS")
  Rel(expSvc, sieadapter, "Delega POST /registro/{rude}", "In-process / Port")
  Rel(calSvc, auditEntity, "INSERT audit_log via AuditLogAspect", "JDBC/TLS en misma TX")
```

---

## Checklist rápido antes de guardar el .mmd

- [ ] Cabecera `C4Context` / `C4Container` / `C4Component` coincide con el nivel.
- [ ] Toda tecnología explícita (no "DB", sí "PostgreSQL 15").
- [ ] Todo protocolo en `Rel` (no "usa", sí "HTTPS/REST" o "JDBC/TLS").
- [ ] `Math.floor()` solo en `ConsolidacionDomainService`.
- [ ] `audit_log` solo escrito desde `AuditLogAspect`.
- [ ] Mermaid renderiza en [mermaid.live](https://mermaid.live) sin errores.
- [ ] Sin caracteres Unicode decorativos en labels (IG-10 del PROMPT_MAPPING).
