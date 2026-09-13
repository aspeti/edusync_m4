package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.ActualizarGestionEscolarCommand;
import com.edusync.academico.application.port.in.ActualizarGestionEscolarUseCase;
import com.edusync.academico.application.port.in.CambiarEstadoGestionEscolarUseCase;
import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.in.CrearGestionEscolarUseCase;
import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.in.CrearSeccionEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearSeccionEvaluacionUseCase;
import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.application.port.in.ListarGestionesEscolaresUseCase;
import com.edusync.academico.application.port.in.ListarPeriodosEvaluacionUseCase;
import com.edusync.academico.application.port.in.ListarSeccionesEvaluacionUseCase;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarActivaUseCase;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarUseCase;
import com.edusync.academico.application.port.in.ReemplazarSeccionesEvaluacionCommand;
import com.edusync.academico.application.port.in.ReemplazarSeccionesEvaluacionUseCase;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacion;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico de {@code FSD-UC-012} (Gestion Escolar, {@code DD-UC-008}).
 *
 * <p><strong>{@code DD-UC-021}:</strong> el modulo "Gestion Escolar" (listar/elegir
 * cualquier gestion por id, crear, editar, transicionar de estado) es exclusivo
 * {@code ADMIN}. {@code SECRETARIA}, {@code PROFESOR} y {@code ASESOR} nunca listan ni
 * eligen una Gestion Escolar: consumen "la gestion actual" de forma implicita via
 * {@code GET .../activa[, /periodos, /secciones]}, que resuelve server-side la unica
 * gestion en estado {@code ACTIVA} del tenant (404 {@code E_GESTION_ESCOLAR_NO_ENCONTRADA}
 * si no hay ninguna). La unicidad de la {@code ACTIVA} es un invariante de
 * {@code CambiarEstadoGestionEscolarService}: activar una gestion cierra automaticamente
 * cualquier otra que estuviera {@code ACTIVA}. Operan exclusivamente sobre el tenant del
 * actor autenticado ({@link TenantContextProvider}): nunca se confia en un {@code tenantId}
 * provisto por el cliente. Seed de 3 periodos y 4 secciones al crear
 * ({@code DD-UC-015}/{@code DD-UC-016}).
 */
@RestController
@RequestMapping("/api/v1/gestiones-escolares")
@RequiredArgsConstructor
@Tag(
    name = "Academico",
    description =
        "Gestion Escolar + Periodos/Secciones anidados (DD-UC-008/015/016/020); listar/elegir "
            + "por id es exclusivo ADMIN, el resto de roles usa .../activa")
public class GestionEscolarController {

  private final CrearGestionEscolarUseCase crearGestionEscolarUseCase;
  private final ListarGestionesEscolaresUseCase listarGestionesEscolaresUseCase;
  private final ObtenerGestionEscolarUseCase obtenerGestionEscolarUseCase;
  private final ObtenerGestionEscolarActivaUseCase obtenerGestionEscolarActivaUseCase;
  private final ActualizarGestionEscolarUseCase actualizarGestionEscolarUseCase;
  private final CambiarEstadoGestionEscolarUseCase cambiarEstadoGestionEscolarUseCase;
  private final CrearPeriodoEvaluacionUseCase crearPeriodoEvaluacionUseCase;
  private final ListarPeriodosEvaluacionUseCase listarPeriodosEvaluacionUseCase;
  private final ListarSeccionesEvaluacionUseCase listarSeccionesEvaluacionUseCase;
  private final ReemplazarSeccionesEvaluacionUseCase reemplazarSeccionesEvaluacionUseCase;
  private final CrearSeccionEvaluacionUseCase crearSeccionEvaluacionUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Crear una Gestion Escolar",
      description = "Nace en PLANIFICACION y siembra 3 trimestres PENDIENTE y 4 secciones (FSD-UC-012/013/014, ADR-0013).")
  @ApiResponse(responseCode = "201", description = "Gestion Escolar creada")
  @ApiResponse(responseCode = "422", description = "fechaFin no posterior a fechaInicio (E_FECHAS_INVALIDAS)")
  public ResponseEntity<GestionEscolarResponse> crear(@Valid @RequestBody CrearGestionEscolarRequest request) {
    GestionEscolar gestionEscolar = crearGestionEscolarUseCase.crear(new CrearGestionEscolarCommand(
        tenantActual(), request.nombre(), request.fechaInicio(), request.fechaFin()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(gestionEscolar));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Listar Gestiones Escolares del tenant (filtrable y paginado)",
      description =
          "Scoped al tenant del actor autenticado (DD-UC-008 §2). Exclusivo ADMIN "
              + "(DD-UC-021): el resto de roles nunca lista ni elige una Gestion Escolar, "
              + "usa GET .../activa. Filtros y paginacion opcionales (DD-UC-007): sin query "
              + "params, page=0 y size=20 por defecto.")
  @ApiResponse(responseCode = "200", description = "Pagina de Gestiones Escolares")
  public ResponseEntity<PageResponse<GestionEscolarResponse>> listar(
      @ParameterObject GestionEscolarFiltro filtro, @ParameterObject PaginacionParams paginacion) {
    var resultado = listarGestionesEscolaresUseCase.listar(
        tenantActual(), filtro, PageQuery.of(paginacion.page(), paginacion.size()));
    return ResponseEntity.ok(PageResponse.from(resultado, this::aResponse));
  }

  @GetMapping("/activa")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','PROFESOR','ASESOR')")
  @Operation(
      summary = "Obtener la Gestion Escolar actual (unica ACTIVA del tenant)",
      description =
          "DD-UC-021: punto de entrada para SECRETARIA/PROFESOR/ASESOR, que nunca listan ni "
              + "eligen una Gestion Escolar por id. Declarado antes de /{id}.")
  @ApiResponse(responseCode = "200", description = "Gestion Escolar ACTIVA")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA (ninguna gestion ACTIVA)")
  public ResponseEntity<GestionEscolarResponse> obtenerActiva() {
    GestionEscolar gestionEscolar = obtenerGestionEscolarActivaUseCase.obtener(tenantActual());
    return ResponseEntity.ok(aResponse(gestionEscolar));
  }

  @GetMapping("/activa/periodos")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','PROFESOR','ASESOR')")
  @Operation(summary = "Listar periodos de la Gestion Escolar actual", description = "DD-UC-021. Sin paginar.")
  @ApiResponse(responseCode = "200", description = "Lista de periodos")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA (ninguna gestion ACTIVA)")
  public ResponseEntity<List<PeriodoEvaluacionResponse>> listarPeriodosDeActiva() {
    GestionEscolar activa = obtenerGestionEscolarActivaUseCase.obtener(tenantActual());
    List<PeriodoEvaluacionResponse> periodos = listarPeriodosEvaluacionUseCase
        .listar(tenantActual(), activa.getId().valor())
        .stream()
        .map(this::aPeriodoResponse)
        .toList();
    return ResponseEntity.ok(periodos);
  }

  @GetMapping("/activa/secciones")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','PROFESOR','ASESOR')")
  @Operation(summary = "Listar secciones de la Gestion Escolar actual", description = "DD-UC-021. Sin paginar.")
  @ApiResponse(responseCode = "200", description = "Lista de secciones")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA (ninguna gestion ACTIVA)")
  public ResponseEntity<List<SeccionEvaluacionResponse>> listarSeccionesDeActiva() {
    GestionEscolar activa = obtenerGestionEscolarActivaUseCase.obtener(tenantActual());
    List<SeccionEvaluacionResponse> secciones = listarSeccionesEvaluacionUseCase
        .listar(tenantActual(), activa.getId().valor())
        .stream()
        .map(this::aSeccionResponse)
        .toList();
    return ResponseEntity.ok(secciones);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Obtener una Gestion Escolar por id", description = "DD-UC-015/020: exclusivo ADMIN.")
  @ApiResponse(responseCode = "200", description = "Gestion Escolar")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  public ResponseEntity<GestionEscolarResponse> obtener(@PathVariable UUID id) {
    GestionEscolar gestionEscolar = obtenerGestionEscolarUseCase.obtener(GestionEscolarId.de(id), tenantActual());
    return ResponseEntity.ok(aResponse(gestionEscolar));
  }

  @GetMapping("/{id}/periodos")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Listar periodos de una Gestion Escolar",
      description = "DD-UC-021: exclusivo ADMIN. Sin paginar, ordenados por orden.")
  @ApiResponse(responseCode = "200", description = "Lista de periodos")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  public ResponseEntity<List<PeriodoEvaluacionResponse>> listarPeriodos(@PathVariable UUID id) {
    List<PeriodoEvaluacionResponse> periodos = listarPeriodosEvaluacionUseCase
        .listar(tenantActual(), id)
        .stream()
        .map(this::aPeriodoResponse)
        .toList();
    return ResponseEntity.ok(periodos);
  }

  @PostMapping("/{id}/periodos")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Agregar un periodo a una Gestion Escolar")
  @ApiResponse(responseCode = "201", description = "Periodo creado")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_FECHAS_INVALIDAS / E_PERIODOS_SOLAPADOS")
  public ResponseEntity<PeriodoEvaluacionResponse> crearPeriodo(
      @PathVariable UUID id, @Valid @RequestBody CrearPeriodoEvaluacionRequest request) {
    PeriodoEvaluacion periodo = crearPeriodoEvaluacionUseCase.crear(new CrearPeriodoEvaluacionCommand(
        tenantActual(), id, request.nombre(), request.fechaInicio(), request.fechaFin()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aPeriodoResponse(periodo));
  }

  @GetMapping("/{id}/secciones")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Listar secciones de una Gestion Escolar",
      description = "DD-UC-021: exclusivo ADMIN. Sin paginar, ordenadas por orden.")
  @ApiResponse(responseCode = "200", description = "Lista de secciones")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  public ResponseEntity<List<SeccionEvaluacionResponse>> listarSecciones(@PathVariable UUID id) {
    List<SeccionEvaluacionResponse> secciones = listarSeccionesEvaluacionUseCase
        .listar(tenantActual(), id)
        .stream()
        .map(this::aSeccionResponse)
        .toList();
    return ResponseEntity.ok(secciones);
  }

  @PutMapping("/{id}/secciones")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Reemplazar la plantilla de secciones (operacion canonica, Σ nota = 100)")
  @ApiResponse(responseCode = "200", description = "Plantilla reemplazada")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_PESO_INVALIDO / E_SUMA_SECCIONES_INVALIDA")
  public ResponseEntity<List<SeccionEvaluacionResponse>> reemplazarSecciones(
      @PathVariable UUID id, @Valid @RequestBody ReemplazarSeccionesEvaluacionRequest request) {
    List<ReemplazarSeccionesEvaluacionCommand.Item> items = request.secciones().stream()
        .map(item -> new ReemplazarSeccionesEvaluacionCommand.Item(item.nombre(), item.nota()))
        .toList();
    List<SeccionEvaluacionResponse> secciones = reemplazarSeccionesEvaluacionUseCase
        .reemplazar(new ReemplazarSeccionesEvaluacionCommand(tenantActual(), id, items))
        .stream()
        .map(this::aSeccionResponse)
        .toList();
    return ResponseEntity.ok(secciones);
  }

  @PostMapping("/{id}/secciones")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Agregar una seccion a una Gestion Escolar (la suma resultante debe ser 100)")
  @ApiResponse(responseCode = "201", description = "Seccion creada")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_PESO_INVALIDO / E_SUMA_SECCIONES_INVALIDA")
  public ResponseEntity<SeccionEvaluacionResponse> crearSeccion(
      @PathVariable UUID id, @Valid @RequestBody CrearSeccionEvaluacionRequest request) {
    SeccionEvaluacion seccion = crearSeccionEvaluacionUseCase.crear(new CrearSeccionEvaluacionCommand(
        tenantActual(), id, request.nombre(), request.orden(), request.nota()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aSeccionResponse(seccion));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Actualizar nombre y/o fechas de una Gestion Escolar",
      description =
          "DD-UC-019: campos nulos conservan el valor actual. Sin restriccion de estado "
              + "(ADMIN puede editar en cualquier momento, PLANIFICACION/ACTIVA/CERRADA).")
  @ApiResponse(responseCode = "200", description = "Gestion Escolar actualizada")
  @ApiResponse(responseCode = "404", description = "E_GESTION_ESCOLAR_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "fechaFin no posterior a fechaInicio (E_FECHAS_INVALIDAS)")
  public ResponseEntity<GestionEscolarResponse> actualizar(
      @PathVariable UUID id, @RequestBody ActualizarGestionEscolarRequest request) {
    GestionEscolar gestionEscolar = actualizarGestionEscolarUseCase.actualizar(new ActualizarGestionEscolarCommand(
        tenantActual(), id, request.nombre(), request.fechaInicio(), request.fechaFin()));
    return ResponseEntity.ok(aResponse(gestionEscolar));
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Cambiar el estado de una Gestion Escolar",
      description =
          "PLANIFICACION/ACTIVA/CERRADA (FSD-UC-012, pasos 3-4). DD-UC-019: ADMIN puede "
              + "transicionar a cualquier estado desde cualquier estado, sin maquina de "
              + "estados restringida.")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  @ApiResponse(responseCode = "404", description = "Gestion Escolar inexistente o de otro tenant (E_GESTION_ESCOLAR_NO_ENCONTRADA)")
  public ResponseEntity<GestionEscolarResponse> cambiarEstado(
      @PathVariable UUID id, @Valid @RequestBody CambiarEstadoGestionEscolarRequest request) {
    GestionEscolar gestionEscolar = cambiarEstadoGestionEscolarUseCase.cambiarEstado(
        GestionEscolarId.de(id), tenantActual(), EstadoGestionEscolar.valueOf(request.estado()));
    return ResponseEntity.ok(aResponse(gestionEscolar));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_GESTION_ESCOLAR_NO_ENCONTRADA", "E_PERIODO_NO_ENCONTRADO", "E_SECCION_NO_ENCONTRADA" ->
          HttpStatus.NOT_FOUND;
      case "E_FECHAS_INVALIDAS",
          "E_PERIODOS_SOLAPADOS",
          "E_PERIODO_UNICO",
          "E_PESO_INVALIDO",
          "E_SUMA_SECCIONES_INVALIDA" -> HttpStatus.UNPROCESSABLE_CONTENT;
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

  private SeccionEvaluacionResponse aSeccionResponse(SeccionEvaluacion seccion) {
    return new SeccionEvaluacionResponse(
        seccion.getId().valor(),
        seccion.getGestionEscolarId().valor(),
        seccion.getNombre(),
        seccion.getOrden(),
        seccion.getNota());
  }
}
