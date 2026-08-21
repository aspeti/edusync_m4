package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.CambiarEstadoGestionEscolarUseCase;
import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.in.CrearGestionEscolarUseCase;
import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.application.port.in.ListarGestionesEscolaresUseCase;
import com.edusync.academico.application.port.in.ListarPeriodosEvaluacionUseCase;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarUseCase;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.shared.PageQuery;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import com.edusync.shared.web.PageResponse;
import com.edusync.shared.web.PaginacionParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico de {@code FSD-UC-012} (Gestion Escolar, {@code DD-UC-008}).
 * {@code POST} y {@code PATCH .../estado} requieren {@code ADMIN}. El {@code GET} de
 * listado y detalle admite tambien {@code SECRETARIA} ({@code DD-UC-013}/{@code DD-UC-015}).
 * Operan exclusivamente sobre el tenant del actor autenticado
 * ({@link TenantContextProvider}): nunca se confia en un {@code tenantId}
 * provisto por el cliente. Seed de 3 periodos al crear ({@code DD-UC-015}).
 */
@RestController
@RequestMapping("/api/v1/gestiones-escolares")
@RequiredArgsConstructor
@Tag(name = "Academico", description = "Gestion Escolar + Periodos anidados (DD-UC-008/015; GET tambien SECRETARIA)")
public class GestionEscolarController {

  private final CrearGestionEscolarUseCase crearGestionEscolarUseCase;
  private final ListarGestionesEscolaresUseCase listarGestionesEscolaresUseCase;
  private final ObtenerGestionEscolarUseCase obtenerGestionEscolarUseCase;
  private final CambiarEstadoGestionEscolarUseCase cambiarEstadoGestionEscolarUseCase;
  private final CrearPeriodoEvaluacionUseCase crearPeriodoEvaluacionUseCase;
  private final ListarPeriodosEvaluacionUseCase listarPeriodosEvaluacionUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Crear una Gestion Escolar",
      description = "Nace en PLANIFICACION y siembra 3 trimestres PENDIENTE (FSD-UC-012/013, ADR-0013).")
  @ApiResponse(responseCode = "201", description = "Gestion Escolar creada")
  @ApiResponse(responseCode = "422", description = "fechaFin no posterior a fechaInicio (E_FECHAS_INVALIDAS)")
  public ResponseEntity<GestionEscolarResponse> crear(@Valid @RequestBody CrearGestionEscolarRequest request) {
    GestionEscolar gestionEscolar = crearGestionEscolarUseCase.crear(new CrearGestionEscolarCommand(
        tenantActual(), request.nombre(), request.fechaInicio(), request.fechaFin()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(gestionEscolar));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
  @Operation(
      summary = "Listar Gestiones Escolares del tenant (filtrable y paginado)",
      description =
          "Scoped al tenant del actor autenticado (DD-UC-008 §2). Lectura tambien SECRETARIA "
              + "(DD-UC-013). Filtros y paginacion opcionales (DD-UC-007): sin query params, "
              + "page=0 y size=20 por defecto.")
  @ApiResponse(responseCode = "200", description = "Pagina de Gestiones Escolares")
  public ResponseEntity<PageResponse<GestionEscolarResponse>> listar(
      @ParameterObject GestionEscolarFiltro filtro, @ParameterObject PaginacionParams paginacion) {
    var resultado = listarGestionesEscolaresUseCase.listar(
        tenantActual(), filtro, PageQuery.of(paginacion.page(), paginacion.size()));
    return ResponseEntity.ok(PageResponse.from(resultado, this::aResponse));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
  @Operation(summary = "Obtener una Gestion Escolar por id", description = "DD-UC-015: evita query param de nombre.")
  @ApiResponse(responseCode = "200", description = "Gestion Escolar")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  public ResponseEntity<GestionEscolarResponse> obtener(@PathVariable UUID id) {
    GestionEscolar gestionEscolar =
        obtenerGestionEscolarUseCase.obtener(GestionEscolarId.de(id), tenantActual());
    return ResponseEntity.ok(aResponse(gestionEscolar));
  }

  @GetMapping("/{id}/periodos")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
  @Operation(summary = "Listar periodos de una Gestion Escolar", description = "Sin paginar, ordenados por orden.")
  @ApiResponse(responseCode = "200", description = "Lista de periodos")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  public ResponseEntity<List<PeriodoEvaluacionResponse>> listarPeriodos(@PathVariable UUID id) {
    List<PeriodoEvaluacionResponse> periodos = listarPeriodosEvaluacionUseCase.listar(tenantActual(), id).stream()
        .map(this::aPeriodoResponse)
        .toList();
    return ResponseEntity.ok(periodos);
  }

  @PostMapping("/{id}/periodos")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Agregar un periodo a una Gestion Escolar")
  @ApiResponse(responseCode = "201", description = "Periodo creado")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_FECHAS_INVALIDAS / E_PERIODOS_SOLAPADOS / E_PERIODOS_INMUTABLES")
  public ResponseEntity<PeriodoEvaluacionResponse> crearPeriodo(
      @PathVariable UUID id, @Valid @RequestBody CrearPeriodoEvaluacionRequest request) {
    PeriodoEvaluacion periodo = crearPeriodoEvaluacionUseCase.crear(new CrearPeriodoEvaluacionCommand(
        tenantActual(), id, request.nombre(), request.fechaInicio(), request.fechaFin()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aPeriodoResponse(periodo));
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Cambiar el estado de una Gestion Escolar",
      description = "PLANIFICACION/ACTIVA/CERRADA (FSD-UC-012, pasos 3-4).")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  @ApiResponse(responseCode = "404", description = "Gestion Escolar inexistente o de otro tenant (E_GESTION_ESCOLAR_NO_ENCONTRADA)")
  @ApiResponse(responseCode = "422", description = "Transicion de estado invalida (E_ESTADO_INVALIDO)")
  public ResponseEntity<GestionEscolarResponse> cambiarEstado(
      @PathVariable UUID id, @Valid @RequestBody CambiarEstadoGestionEscolarRequest request) {
    GestionEscolar gestionEscolar = cambiarEstadoGestionEscolarUseCase.cambiarEstado(
        GestionEscolarId.de(id), tenantActual(), EstadoGestionEscolar.valueOf(request.estado()));
    return ResponseEntity.ok(aResponse(gestionEscolar));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_GESTION_ESCOLAR_NO_ENCONTRADA", "E_PERIODO_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
      case "E_FECHAS_INVALIDAS",
          "E_ESTADO_INVALIDO",
          "E_PERIODOS_SOLAPADOS",
          "E_PERIODO_NO_SECUENCIAL",
          "E_PERIODOS_INMUTABLES",
          "E_PERIODO_UNICO" -> HttpStatus.UNPROCESSABLE_CONTENT;
      default -> HttpStatus.CONFLICT;
    };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private GestionEscolarResponse aResponse(GestionEscolar gestionEscolar) {
    return new GestionEscolarResponse(
        gestionEscolar.getId().valor(),
        gestionEscolar.getNombre(),
        gestionEscolar.getFechaInicio(),
        gestionEscolar.getFechaFin(),
        gestionEscolar.getEstado().name());
  }

  private PeriodoEvaluacionResponse aPeriodoResponse(PeriodoEvaluacion periodo) {
    return new PeriodoEvaluacionResponse(
        periodo.getId().valor(),
        periodo.getGestionEscolarId().valor(),
        periodo.getNombre(),
        periodo.getFechaInicio(),
        periodo.getFechaFin(),
        periodo.getOrden(),
        periodo.getEstado().name());
  }
}
