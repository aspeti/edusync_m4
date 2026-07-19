# DTOs por capa hexagonal — EduSync

| Campo | Valor |
|-------|-------|
| **Producto** | EduSync |
| **Grupo** | G-EduSync |
| **Versión del documento** | v0.1 |
| **Fecha** | 24/05/2026 |
| **Autor** | Rodrigo Aspeti — Dev Lead / PM |
| **Estado** | Borrador |
| **Stack** | Java 21 · Spring Boot 3.3 · Jakarta Bean Validation 3.0 · Spring Security 6 · PostgreSQL 15 |
| **Insumos** | `docs/fsd/FSD_EduSync.md` v1.0 (§4.1 UC-001, §4.2 UC-003, §4.4 UC-005) · `docs/arquitectura_hexagonal_EduSync.md` v0.1 · `AGENTS.md` v0.2 |
| **Prompt aplicado** | `PR-DTO-001` (registrado en `docs/PROMPT_MAPPING.md` v0.9) |
| **Trazabilidad** | FSD-UC-001 · FSD-UC-003 · FSD-UC-005 · BR-001..BR-011 · DA-01..DA-03 |

---

## 0. Propósito

Documento de **diseño técnico** que define los **Data Transfer Objects (DTOs)** y **Domain Events** de los 3 casos de uso críticos de EduSync, respetando estrictamente la separación de capas hexagonales:

| Capa | Paquete | Tipo de artefacto |
|------|---------|-------------------|
| `infrastructure/adapter/in/web/dto/` | `bo.edusync.infrastructure.adapter.in.web.dto` | **Request DTO** y **Response DTO** (Java Records con Jakarta Bean Validation) |
| `application/<contexto>/` | `bo.edusync.application.<contexto>` | **Commands** (Java Records puros, sin Spring ni Jakarta) |
| `domain/model/<contexto>/event/` | `bo.edusync.domain.model.<contexto>.event` | **Domain Events** (Java Records inmutables, sin imports externos) |

Sirve de **contrato vinculante** entre `dev-agent` (implementación), `qa-agent` (golden tests) y `arch-agent` (revisión de invariantes hexagonales).

### Convenciones aplicadas (fuente: `AGENTS.md` §5)

- Todos los DTOs son **Java Records** (Java 21).
- Idioma del código: inglés (nombres de clase, campos, métodos).
- Validación **Bean Validation** en Request DTOs (`@NotNull`, `@Size`, `@DecimalMin`, `@DecimalMax`).
- **NUNCA** se exponen entidades JPA directamente por API.
- Enums permanecen en español dentro del dominio (`SER`, `SABER`, `HACER`, `DECIDIR`, `AUTOEVALUACION`) — espejo fiel del modelo normativo del SIE.
- Los **Commands** no contienen anotaciones Jakarta/Spring: son POJOs puros (cumple DA-02 — dominio aislado de frameworks).

---

## 1. FSD-UC-001 — Registro de calificación por dimensión

### 1.1 Request DTO

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import bo.edusync.domain.model.calificacion.Dimension;
import bo.edusync.domain.model.calificacion.TipoCalificacion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CalificacionRequestDTO(
        @NotNull UUID materiaId,
        @NotNull UUID periodoId,
        @NotBlank @Size(min = 10, max = 20) String rude,
        @NotNull Dimension dimension,
        @NotNull @Min(1) Integer indiceEvaluacion,
        @NotNull TipoCalificacion tipo,
        @NotNull @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal valor
) {
}
```

### 1.2 Command (application)

```java
package bo.edusync.application.calificacion;

import bo.edusync.domain.model.calificacion.Dimension;
import bo.edusync.domain.model.calificacion.TipoCalificacion;
import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarCalificacionCommand(
        UUID tenantId,
        UUID actorId,
        UUID materiaId,
        UUID periodoId,
        String rude,
        Dimension dimension,
        int indiceEvaluacion,
        TipoCalificacion tipo,
        BigDecimal valor
) {
}
```

### 1.3 Response DTO

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import bo.edusync.domain.model.consolidacion.EstadoCentralizador;
import java.time.Instant;
import java.util.UUID;

public record CalificacionResponseDTO(
        UUID calificacionId,
        PromedioProvisional promedioProvisional,
        Instant timestamp
) {
    public record PromedioProvisional(int valor, EstadoCentralizador estado) {
    }
}
```

### 1.4 Domain Event

```java
package bo.edusync.domain.model.calificacion.event;

import bo.edusync.domain.model.calificacion.Dimension;
import java.time.Instant;
import java.util.UUID;

public record CalificacionRegistradaEvent(
        UUID tenantId,
        UUID calificacionId,
        UUID materiaId,
        UUID periodoId,
        String rude,
        Dimension dimension,
        int indiceEvaluacion,
        Instant occurredAt
) {
}
```

### 1.5 Enums de dominio compartidos

```java
package bo.edusync.domain.model.calificacion;

public enum Dimension {
    SER,
    SABER,
    HACER,
    DECIDIR,
    AUTOEVALUACION
}
```

```java
package bo.edusync.domain.model.calificacion;

public enum TipoCalificacion {
    REGULAR,
    AYUDA
}
```

### 1.6 Mapeo DTO ↔ Entidad `Calificacion`

| Campo DTO | Tipo Java | Campo Entidad | BR aplicada | Capa de validación |
|-----------|-----------|---------------|-------------|---------------------|
| `materiaId` | `UUID` | `Calificacion.materia_id` | BR-001 (RBAC `asignacion_docente`) | Jakarta `@NotNull` + Domain Service (`MateriaAsignacionRepository.verificarAsignacion`) |
| `periodoId` | `UUID` | `Calificacion.periodo_id` | BR-007 (parámetros inmutables si `ABIERTO`) | Jakarta `@NotNull` + Domain Service (`PeriodoAcademico.estado == ABIERTO`) |
| `rude` | `String` | `Calificacion.rude` (FK lógica a `Estudiante.rude`) | BR-004 (identidad solo por RUDE) | Jakarta `@NotBlank @Size(10,20)` + VO `Rude` (regex en dominio) |
| `dimension` | `Dimension` | `Calificacion.dimension` | BR-002 (rango paramétrico activo) | Jakarta `@NotNull` + Domain Service consulta `ParametroAcademico` |
| `indiceEvaluacion` | `Integer` | `Calificacion.indice_evaluacion` | BR-002 (rango) | Jakarta `@Min(1)` |
| `tipo` | `TipoCalificacion` | `Calificacion.tipo` | Regla A5 del FSD (AYUDA requiere REGULAR previa) | Jakarta `@NotNull` + Domain Service |
| `valor` | `BigDecimal` | `Calificacion.valor` | BR-002 (`valor ∈ [rango_min, rango_max]`) | Jakarta `@DecimalMin("0.00") @Digits(3,2)` + VO `ValorCalificacion` (rango paramétrico dinámico — no fijable en compile-time, validación en dominio) |
| (Command) `tenantId` | `UUID` | `Calificacion.tenant_id` (RLS) | DA-01 (multitenancy RLS) | `TenantContextProvider` (SecurityContext, no body) |
| (Command) `actorId` | `UUID` | `audit_log.actor_id` | BR-010 (audit en misma TX) | JWT claim (SecurityContext, no body) |
| (Response) `calificacionId` | `UUID` | `Calificacion.id` | — | Sistema |
| (Response) `promedioProvisional.valor` | `int` | derivado de `Centralizador` (PROVISIONAL) | BR-003 (`Math.floor()`), BR-008 | Domain Service `ConsolidacionDomainService.recalcularProvisional()` |
| (Response) `promedioProvisional.estado` | `EstadoCentralizador` | `Centralizador.estado` | BR-008 | Domain Service |
| (Response) `timestamp` | `Instant` | `Calificacion.timestamp_utc` | — | Sistema (`ClockPort.now()`) |

---

## 2. FSD-UC-003 — Consolidación algorítmica de centralizadores

> **Observación arquitectónica**: el motor de consolidación es asíncrono y se dispara por `MateriaCerradaEvent` (no por REST). La oficialización es la única acción REST IN del Director. La consulta del centralizador (GET) reutiliza el mismo Response DTO.

### 2.1 Request DTO (Director — Oficializar)

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OficializarCentralizadorRequestDTO(
        @NotNull UUID centralizadorId,
        @NotNull UUID periodoId
) {
}
```

### 2.2 Command (interno, derivado del evento)

```java
package bo.edusync.application.consolidacion;

import java.util.UUID;

public record ConsolidarCentralizadorCommand(
        UUID tenantId,
        UUID materiaId,
        UUID cursoId,
        UUID periodoId,
        boolean intentarOficializar
) {
}
```

### 2.3 Response DTO

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import bo.edusync.domain.model.consolidacion.EstadoCentralizador;
import java.util.List;
import java.util.UUID;

public record CentralizadorResponseDTO(
        UUID centralizadorId,
        UUID cursoId,
        UUID periodoId,
        EstadoCentralizador estado,
        List<PromedioEstudianteDTO> promedios,
        Integer promedioAnual
) {
    public record PromedioEstudianteDTO(
            String rude,
            int puntaje,
            boolean aprobado
    ) {
    }
}
```

> **Invariante**: `promedioAnual` es `Integer` (boxed nullable). `null` = "EN CURSO" (algún periodo no `CERRADO`); cualquier valor entero = promedio oficial (`floor`). Esto permite al frontend distinguir 0 de N/A.

### 2.4 Domain Events

**Evento de entrada (disparador del motor)**:

```java
package bo.edusync.domain.model.consolidacion.event;

import java.time.Instant;
import java.util.UUID;

public record MateriaCerradaEvent(
        UUID tenantId,
        UUID materiaId,
        UUID cursoId,
        UUID periodoId,
        Instant occurredAt
) {
}
```

**Evento de salida (cuando el Director oficializa)**:

```java
package bo.edusync.domain.model.consolidacion.event;

import java.time.Instant;
import java.util.UUID;

public record CentralizadorOficialEvent(
        UUID tenantId,
        UUID centralizadorId,
        UUID cursoId,
        UUID periodoId,
        Instant occurredAt
) {
}
```

### 2.5 Enum de dominio

```java
package bo.edusync.domain.model.consolidacion;

public enum EstadoCentralizador {
    PROVISIONAL,
    OFICIAL,
    ERROR
}
```

### 2.6 Mapeo DTO ↔ Entidad `Centralizador`

| Campo DTO | Tipo Java | Campo Entidad | BR aplicada | Capa de validación |
|-----------|-----------|---------------|-------------|---------------------|
| (Request) `centralizadorId` | `UUID` | `Centralizador.id` | — | Jakarta `@NotNull` |
| (Request) `periodoId` | `UUID` | `Centralizador.periodo_id` | BR-011 (anual con 3 periodos CERRADO) | Jakarta `@NotNull` + Domain Service `verificar100PorCientoMateriasCerradas()` |
| (Response) `centralizadorId` | `UUID` | `Centralizador.id` | — | Sistema |
| (Response) `cursoId` | `UUID` | `Centralizador.curso_id` | — | Sistema |
| (Response) `periodoId` | `UUID` | `Centralizador.periodo_id` | — | Sistema |
| (Response) `estado` | `EstadoCentralizador` | `Centralizador.estado` | BR-008 (transición exclusiva del dominio) | Domain Service (no SQL ni front) |
| (Response) `promedios[].rude` | `String` | `CentralizadorRegistro.rude` | BR-004 (identidad por RUDE) | Domain Service |
| (Response) `promedios[].puntaje` | `int` | `CentralizadorRegistro.puntaje_total` | BR-003 (`Math.floor()`), BR-008 | Domain Service `ConsolidacionDomainService` aplica `floor()` — **único lugar permitido** |
| (Response) `promedios[].aprobado` | `boolean` | (derivado) | BR-008 | Domain Service compara contra umbral paramétrico |
| (Response) `promedioAnual` | `Integer` (nullable) | (derivado de `floor((T1+T2+T3)/3)`) | BR-011 (solo con 3 periodos `CERRADO`) | Domain Service retorna `null` si algún trimestre no está cerrado |
| (Command) `tenantId` | `UUID` | RLS | DA-01 | Evento entrante / SecurityContext |
| (Command) `intentarOficializar` | `boolean` | — | BR-011 | Flag puro de aplicación (no se persiste) |

---

## 3. FSD-UC-005 — Autorización jerárquica de modificación retroactiva

> **Observación**: este UC tiene dos sub-flujos REST: (a) Docente solicita corrección, (b) Director autoriza. Se modelan ambos Request DTOs y ambos Commands. El Response DTO principal es el de autorización (que abre la ventana temporal).

### 3.1 Request DTOs

**Sub-flujo (a) — Docente solicita corrección**:

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import bo.edusync.domain.model.calificacion.Dimension;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SolicitudCorreccionRequestDTO(
        @NotNull UUID materiaId,
        @NotBlank @Size(min = 10, max = 20) String rude,
        @NotNull Dimension dimension,
        @NotNull @Min(1) Integer indiceEvaluacion,
        @NotBlank @Size(min = 20, max = 2000) String justificacion
) {
}
```

**Sub-flujo (b) — Director autoriza**:

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import bo.edusync.domain.model.correccion.AlcanceCorreccion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AutorizacionCorreccionRequestDTO(
        @NotNull UUID solicitudId,
        @NotNull AlcanceCorreccion alcance,
        @Min(1) @Max(72) Integer duracionHoras
) {
}
```

### 3.2 Commands

```java
package bo.edusync.application.correccion;

import bo.edusync.domain.model.calificacion.Dimension;
import java.util.UUID;

public record SolicitarCorreccionCommand(
        UUID tenantId,
        UUID actorId,
        UUID materiaId,
        String rude,
        Dimension dimension,
        int indiceEvaluacion,
        String justificacion
) {
}
```

```java
package bo.edusync.application.correccion;

import bo.edusync.domain.model.correccion.AlcanceCorreccion;
import java.util.UUID;

public record AutorizarCorreccionCommand(
        UUID tenantId,
        UUID actorId,
        UUID solicitudId,
        AlcanceCorreccion alcance,
        int duracionHoras
) {
}
```

### 3.3 Response DTO

```java
package bo.edusync.infrastructure.adapter.in.web.dto;

import bo.edusync.domain.model.correccion.AlcanceCorreccion;
import bo.edusync.domain.model.correccion.EstadoAutorizacion;
import java.time.Instant;
import java.util.UUID;

public record AutorizacionCorreccionResponseDTO(
        UUID autorizacionId,
        Instant ventanaFin,
        AlcanceCorreccion alcance,
        EstadoAutorizacion estado
) {
}
```

### 3.4 Domain Events

```java
package bo.edusync.domain.model.correccion.event;

import bo.edusync.domain.model.correccion.AlcanceCorreccion;
import java.time.Instant;
import java.util.UUID;

public record AutorizacionEmitidaEvent(
        UUID tenantId,
        UUID autorizacionId,
        UUID solicitudId,
        AlcanceCorreccion alcance,
        Instant ventanaInicio,
        Instant ventanaFin,
        Instant occurredAt
) {
}
```

```java
package bo.edusync.domain.model.correccion.event;

import java.time.Instant;
import java.util.UUID;

public record VentanaExpiradaEvent(
        UUID tenantId,
        UUID autorizacionId,
        Instant occurredAt
) {
}
```

### 3.5 Enums de dominio

```java
package bo.edusync.domain.model.correccion;

public enum AlcanceCorreccion {
    ESTUDIANTE_ESPECIFICO,
    CURSO_COMPLETO
}
```

```java
package bo.edusync.domain.model.correccion;

public enum EstadoAutorizacion {
    ACTIVA,
    EXPIRADA,
    COMPLETADA
}
```

### 3.6 Mapeo DTO ↔ Entidades `SolicitudCorreccion` + `AutorizacionCorreccion`

| Campo DTO | Tipo Java | Campo Entidad | BR aplicada | Capa de validación |
|-----------|-----------|---------------|-------------|---------------------|
| (Solicitud) `materiaId` | `UUID` | `SolicitudCorreccion.materia_id` | BR-001 (RBAC) | Jakarta `@NotNull` + Domain Service `MateriaAsignacionRepository.verificar()` |
| (Solicitud) `rude` | `String` | `SolicitudCorreccion.rude` | BR-004 | Jakarta `@NotBlank @Size(10,20)` + VO `Rude` |
| (Solicitud) `dimension` | `Dimension` | (referencia para auditoría) | BR-002 | Jakarta `@NotNull` |
| (Solicitud) `indiceEvaluacion` | `Integer` | (referencia) | BR-002 | Jakarta `@Min(1)` |
| (Solicitud) `justificacion` | `String` | `SolicitudCorreccion.justificacion` | Regla institucional (≥ 20 chars) | Jakarta `@NotBlank @Size(20,2000)` |
| (Autorización) `solicitudId` | `UUID` | `AutorizacionCorreccion.solicitud_id` | — | Jakarta `@NotNull` |
| (Autorización) `alcance` | `AlcanceCorreccion` | `AutorizacionCorreccion.alcance` | — | Jakarta `@NotNull` |
| (Autorización) `duracionHoras` | `Integer` | derivado → `ventana_fin` | BR-009 (`1 ≤ duracion ≤ 72`, default 24) | Jakarta `@Min(1) @Max(72)` + Domain Service aplica default si `null` |
| (Response) `autorizacionId` | `UUID` | `AutorizacionCorreccion.id` | — | Sistema |
| (Response) `ventanaFin` | `Instant` | `AutorizacionCorreccion.ventana_fin` | BR-009 | Domain Service: `ClockPort.now().plus(duracionHoras)` |
| (Response) `alcance` | `AlcanceCorreccion` | `AutorizacionCorreccion.alcance` | — | Sistema |
| (Response) `estado` | `EstadoAutorizacion` | `AutorizacionCorreccion.estado` | BR-009 (revocación automática) | Domain Service (`ACTIVA` al emitir, `EXPIRADA` por scheduler) |
| (Command) `tenantId` | `UUID` | `tenant_id` (RLS) | DA-01 | SecurityContext (no body) |
| (Command) `actorId` | `UUID` | `audit_log.actor_id` | BR-010 | SecurityContext (no body) |
| Calificación versionada al aplicar la corrección | — | `Calificacion.registro_padre_id` | BR-005 (append-only) | Domain Service `AplicarCorreccionUseCase` inserta nueva fila con FK al original |

---

## 4. Verificación contra invariantes hexagonales

| Invariante | Verificación |
|-----------|--------------|
| `domain/` sin imports de Spring/Jakarta (DA-02) | Los 5 Records de `domain/model/*/event/` y los 5 enums solo importan `java.*` |
| `application/` sin Spring/Jakarta | Los 4 Commands son `record` puros con tipos `java.*` y referencias al propio dominio |
| `rude` nunca en path/query (BR-004 + NFR-007 PII) | Aparece exclusivamente en el body (`CalificacionRequestDTO`, `SolicitudCorreccionRequestDTO`, `CentralizadorResponseDTO.PromedioEstudianteDTO`) |
| `valor` con `@DecimalMin`/`@Digits` + validación dinámica de rango | Jakarta valida `>= 0.00` y `@Digits(3,2)`; el rango paramétrico (`rango_max` por dimensión activa) se valida en el VO `ValorCalificacion` del dominio (BR-002) |
| Response DTOs no exponen `tenant_id` ni `actor_id` | Ningún Response DTO contiene esos campos; viven solo en `*Command` |
| `promedioAnual` es `Integer` nullable | `CentralizadorResponseDTO.promedioAnual` es `Integer` (boxed) — `null` = "EN CURSO" (BR-011) |
| Cero cálculos (floor/promedio) en DTOs | Ningún Record realiza `floor()` ni cálculos; `promedios[].puntaje` viene pre-calculado por `ConsolidacionDomainService` (BR-003 + BR-008) |
| Cero entidades JPA expuestas | Todos los Records usan tipos primitivos / VOs / UUIDs; ninguno referencia `@Entity` |

---

## 5. Inventario consolidado

| UC | Request DTO | Command | Response DTO | Domain Events |
|----|-------------|---------|--------------|---------------|
| FSD-UC-001 | `CalificacionRequestDTO` | `RegistrarCalificacionCommand` | `CalificacionResponseDTO` | `CalificacionRegistradaEvent` |
| FSD-UC-003 | `OficializarCentralizadorRequestDTO` | `ConsolidarCentralizadorCommand` | `CentralizadorResponseDTO` | `MateriaCerradaEvent` (in), `CentralizadorOficialEvent` (out) |
| FSD-UC-005 | `SolicitudCorreccionRequestDTO`, `AutorizacionCorreccionRequestDTO` | `SolicitarCorreccionCommand`, `AutorizarCorreccionCommand` | `AutorizacionCorreccionResponseDTO` | `AutorizacionEmitidaEvent`, `VentanaExpiradaEvent` |

**Totales**:

- 4 Request DTOs (Java Records con Jakarta Bean Validation)
- 4 Commands (Java Records puros)
- 3 Response DTOs (Java Records)
- 5 Domain Events (3 mandatorios + 2 derivados de BR-009)
- 5 enums de dominio compartidos (`Dimension`, `TipoCalificacion`, `EstadoCentralizador`, `AlcanceCorreccion`, `EstadoAutorizacion`)
- 3 tablas DTO ↔ Entidad con BR y capa de validación

---

## 6. Checklist de implementación (`dev-agent`)

- [ ] Crear paquetes `bo.edusync.infrastructure.adapter.in.web.dto`, `bo.edusync.application.{calificacion,consolidacion,correccion}` y `bo.edusync.domain.model.{calificacion,consolidacion,correccion}.event`.
- [ ] Implementar los 5 enums de dominio (`Dimension`, `TipoCalificacion`, `EstadoCentralizador`, `AlcanceCorreccion`, `EstadoAutorizacion`).
- [ ] Implementar los 4 Request DTOs con Jakarta Bean Validation tal cual aparece en §§1.1, 2.1, 3.1.
- [ ] Implementar los 4 Commands sin imports de `org.springframework.*` ni `jakarta.*`.
- [ ] Implementar los 3 Response DTOs sin exponer `tenant_id` ni `actor_id`.
- [ ] Implementar los 5 Domain Events como Records inmutables en `domain/`.
- [ ] Implementar el VO `ValorCalificacion` con validación dinámica del rango paramétrico (BR-002).
- [ ] Implementar el VO `Rude` con regex `^[A-Z0-9]{10,20}$` (BR-004).
- [ ] Configurar `ControllerAdvice` que mapee `MethodArgumentNotValidException` → HTTP 400 (formato uniforme).
- [ ] Validar arquitectura con ArchUnit: `domain/.._.shouldOnlyDependOnClassesThat(.. java..`).
- [ ] Cubrir cada Record con golden test (`FloorTest`, `RangoDinamicoTest`, `VentanaTest`, `MultitenantTest`).

---

## 7. Trazabilidad

| Referencia origen | Artefacto consumido | Sección |
|--------------------|---------------------|---------|
| FSD-UC-001 (Datos entrada/salida, BR-001/002/004/007) | `docs/fsd/FSD_EduSync.md` §4.1 | §1 |
| FSD-UC-003 (Disparador, Payload salida, BR-003/008/011) | `docs/fsd/FSD_EduSync.md` §4.2 | §2 |
| FSD-UC-005 (Solicitud, Autorización, BR-005/009/010) | `docs/fsd/FSD_EduSync.md` §4.4 | §3 |
| Estructura de paquetes hexagonal | `docs/arquitectura_hexagonal_EduSync.md` §1.1 | §0 |
| Convenciones de DTOs y Records | `AGENTS.md` §5 | §0 |
| Invariantes de dominio (BR-001..BR-012) | `docs/fsd/FSD_EduSync.md` §5 | §§1.6, 2.6, 3.6 |
| Materialización RLS multitenant (DA-01) | `docs/arquitectura_funcional_EduSync.md` §DA-01 | §§1.6, 2.6, 3.6 |
| Aislamiento del dominio (DA-02) | `docs/arquitectura_funcional_EduSync.md` §DA-02 | §0, §4 |
| Append-only y audit_log (DA-03) | `docs/arquitectura_funcional_EduSync.md` §DA-03 | §3.6 |

---

## 8. Registro de cambios

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| v0.1 | 24/05/2026 | Rodrigo Aspeti | Creación inicial — 4 Request DTOs, 4 Commands, 3 Response DTOs, 5 Domain Events, 5 enums de dominio, 3 tablas de mapeo DTO ↔ Entidad, checklist de implementación y trazabilidad completa a FSD-UC-001 / UC-003 / UC-005 y BR-001..BR-011. Generado a partir del prompt `PR-DTO-001`. |
