package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.ActualizarEvaluacionCommand;
import com.edusync.academico.application.port.in.ActualizarEvaluacionUseCase;
import com.edusync.academico.application.port.in.AnularEvaluacionUseCase;
import com.edusync.academico.application.port.in.CrearEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearEvaluacionUseCase;
import com.edusync.academico.application.port.in.ListarCalificacionesUseCase;
import com.edusync.academico.application.port.in.ObtenerEvaluacionUseCase;
import com.edusync.academico.application.port.in.UpsertCalificacionesCommand;
import com.edusync.academico.application.port.in.UpsertCalificacionesUseCase;
import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
 * Adaptador REST de {@code FSD-UC-015}/{@code FSD-UC-016} ({@code DD-UC-017}/{@code DD-UC-018}).
 * {@code tenantId} y {@code actorId} salen del contexto / JWT, nunca del body.
 */
@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Tag(name = "Academico", description = "Evaluaciones y calificaciones (DD-UC-017/018)")
public class EvaluacionController {

  private final CrearEvaluacionUseCase crearEvaluacionUseCase;
  private final ObtenerEvaluacionUseCase obtenerEvaluacionUseCase;
  private final ActualizarEvaluacionUseCase actualizarEvaluacionUseCase;
  private final AnularEvaluacionUseCase anularEvaluacionUseCase;
  private final UpsertCalificacionesUseCase upsertCalificacionesUseCase;
  private final ListarCalificacionesUseCase listarCalificacionesUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
  @Operation(summary = "Crear una Evaluacion", description = "puntajeMaximo = seccion.nota (nunca del cliente).")
  @ApiResponse(responseCode = "201", description = "Evaluacion creada")
  @ApiResponse(responseCode = "404", description = "Materia/periodo/seccion no encontrados")
  @ApiResponse(responseCode = "409", description = "E_MATERIA_SIN_PROFESOR")
  @ApiResponse(responseCode = "422", description = "E_PERIODO_NO_ABIERTO / E_SECCION_NO_PERTENECE_A_GESTION")
  public ResponseEntity<EvaluacionResponse> crear(
      @Valid @RequestBody CrearEvaluacionRequest request, Authentication authentication) {
    Evaluacion evaluacion = crearEvaluacionUseCase.crear(new CrearEvaluacionCommand(
        tenantActual(),
        ActorSeguridad.id(authentication),
        ActorSeguridad.esAdmin(authentication),
        request.nombre(),
        request.materiaId(),
        request.periodoEvaluacionId(),
        request.seccionEvaluacionId(),
        request.fecha(),
        request.descripcion()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(evaluacion));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','PROFESOR')")
  @Operation(summary = "Obtener una Evaluacion por id")
  @ApiResponse(responseCode = "200", description = "Evaluacion")
  @ApiResponse(responseCode = "404", description = "E_EVALUACION_NO_ENCONTRADA")
  public ResponseEntity<EvaluacionResponse> obtener(@PathVariable UUID id, Authentication authentication) {
    Evaluacion evaluacion = obtenerEvaluacionUseCase.obtener(
        tenantActual(),
        id,
        ActorSeguridad.id(authentication),
        ActorSeguridad.veTodasLasMaterias(authentication));
    return ResponseEntity.ok(aResponse(evaluacion));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
  @Operation(summary = "Actualizar nombre, fecha o descripcion")
  @ApiResponse(responseCode = "200", description = "Evaluacion actualizada")
  @ApiResponse(responseCode = "404", description = "E_EVALUACION_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_PERIODO_NO_ABIERTO / E_EVALUACION_YA_ANULADA")
  public ResponseEntity<EvaluacionResponse> actualizar(
      @PathVariable UUID id,
      @RequestBody ActualizarEvaluacionRequest request,
      Authentication authentication) {
    Evaluacion evaluacion = actualizarEvaluacionUseCase.actualizar(new ActualizarEvaluacionCommand(
        tenantActual(),
        ActorSeguridad.id(authentication),
        ActorSeguridad.esAdmin(authentication),
        id,
        request.nombre(),
        request.fecha(),
        request.descripcion()));
    return ResponseEntity.ok(aResponse(evaluacion));
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
  @Operation(summary = "Anular una Evaluacion (baja logica)")
  @ApiResponse(responseCode = "200", description = "Evaluacion anulada")
  @ApiResponse(responseCode = "404", description = "E_EVALUACION_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_PERIODO_NO_ABIERTO / E_EVALUACION_YA_ANULADA")
  public ResponseEntity<EvaluacionResponse> anular(
      @PathVariable UUID id,
      @Valid @RequestBody CambiarEstadoEvaluacionRequest request,
      Authentication authentication) {
    Evaluacion evaluacion = anularEvaluacionUseCase.anular(
        tenantActual(), id, ActorSeguridad.id(authentication), ActorSeguridad.esAdmin(authentication));
    return ResponseEntity.ok(aResponse(evaluacion));
  }

  @PutMapping("/{id}/calificaciones")
  @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
  @Operation(summary = "Upsert batch de calificaciones (FSD-UC-016 / DD-UC-018)")
  @ApiResponse(responseCode = "200", description = "Calificaciones guardadas")
  @ApiResponse(responseCode = "404", description = "Evaluacion/materia no encontrada")
  @ApiResponse(responseCode = "409", description = "E_MATERIA_SIN_CURSO / E_MATERIA_SIN_PROFESOR")
  @ApiResponse(responseCode = "422", description = "Rango / no inscrito / periodo / no activa")
  public ResponseEntity<List<CalificacionResponse>> upsertCalificaciones(
      @PathVariable UUID id,
      @Valid @RequestBody UpsertCalificacionesRequest request,
      Authentication authentication) {
    List<UpsertCalificacionesCommand.Item> items =
        request.items().stream()
            .map(i -> new UpsertCalificacionesCommand.Item(i.estudianteId(), i.valor()))
            .toList();
    List<CalificacionEvaluacion> guardadas =
        upsertCalificacionesUseCase.upsert(
            new UpsertCalificacionesCommand(
                tenantActual(),
                ActorSeguridad.id(authentication),
                ActorSeguridad.esAdmin(authentication),
                id,
                items));
    return ResponseEntity.ok(
        guardadas.stream()
            .map(
                c ->
                    new CalificacionResponse(
                        c.getId().valor(),
                        c.getEvaluacionId().valor(),
                        c.getEstudianteId().valor(),
                        c.getValor()))
            .toList());
  }

  @GetMapping("/{id}/calificaciones")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','PROFESOR')")
  @Operation(summary = "Nomina + calificaciones de una evaluacion (DD-UC-018)")
  @ApiResponse(responseCode = "200", description = "Filas de nomina")
  @ApiResponse(responseCode = "404", description = "E_EVALUACION_NO_ENCONTRADA")
  public ResponseEntity<List<CalificacionFilaResponse>> listarCalificaciones(
      @PathVariable UUID id, Authentication authentication) {
    ListarCalificacionesUseCase.Resultado resultado =
        listarCalificacionesUseCase.listar(
            tenantActual(),
            id,
            ActorSeguridad.id(authentication),
            ActorSeguridad.veTodasLasMaterias(authentication));
    return ResponseEntity.ok(
        resultado.filas().stream()
            .map(
                f ->
                    new CalificacionFilaResponse(
                        f.estudiante().getId().valor(),
                        f.estudiante().getNombreCompleto(),
                        f.estudiante().getRude(),
                        f.calificacion() == null ? null : f.calificacion().getValor()))
            .toList());
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_EVALUACION_NO_ENCONTRADA",
          "E_MATERIA_NO_ENCONTRADA",
          "E_PERIODO_NO_ENCONTRADO",
          "E_SECCION_NO_ENCONTRADA" -> HttpStatus.NOT_FOUND;
      case "E_MATERIA_SIN_PROFESOR", "E_MATERIA_SIN_CURSO" -> HttpStatus.CONFLICT;
      case "E_PERIODO_NO_ABIERTO",
          "E_SECCION_NO_PERTENECE_A_GESTION",
          "E_EVALUACION_YA_ANULADA",
          "E_EVALUACION_NO_ACTIVA",
          "E_RANGO_INVALIDO",
          "E_ESTUDIANTE_NO_INSCRITO" -> HttpStatus.UNPROCESSABLE_CONTENT;
      default -> HttpStatus.CONFLICT;
    };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private EvaluacionResponse aResponse(Evaluacion evaluacion) {
    return new EvaluacionResponse(
        evaluacion.getId().valor(),
        evaluacion.getMateriaId().valor(),
        evaluacion.getPeriodoEvaluacionId().valor(),
        evaluacion.getSeccionEvaluacionId().valor(),
        evaluacion.getNombre(),
        evaluacion.getFecha(),
        evaluacion.getPuntajeMaximo(),
        evaluacion.getDescripcion(),
        evaluacion.getEstado().name());
  }
}
