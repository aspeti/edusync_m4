package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.CrearEstudianteCommand;
import com.edusync.academico.application.port.in.CrearEstudianteUseCase;
import com.edusync.academico.application.port.in.EstudianteFiltro;
import com.edusync.academico.application.port.in.ListarEstudiantesUseCase;
import com.edusync.academico.application.port.in.ListarInscripcionesEstudianteUseCase;
import com.edusync.academico.application.port.in.ObtenerEstudianteUseCase;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.Inscripcion;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico de {@code FSD-UC-020} (Estudiantes, {@code DD-UC-013}). Todos los
 * endpoints requieren {@code ADMIN} o {@code SECRETARIA} y operan exclusivamente sobre el
 * tenant del actor autenticado ({@link TenantContextProvider}): nunca se confia en un
 * {@code tenantId} provisto por el cliente.
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
@Tag(name = "Academico", description = "Estudiantes e historial de inscripciones (DD-UC-013, ADMIN o SECRETARIA)")
public class EstudianteController {

  private final CrearEstudianteUseCase crearEstudianteUseCase;
  private final ListarEstudiantesUseCase listarEstudiantesUseCase;
  private final ObtenerEstudianteUseCase obtenerEstudianteUseCase;
  private final ListarInscripcionesEstudianteUseCase listarInscripcionesEstudianteUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @Operation(summary = "Crear un Estudiante", description = "FSD-UC-020, paso 1. rude obligatorio unico por tenant.")
  @ApiResponse(responseCode = "201", description = "Estudiante creado")
  @ApiResponse(responseCode = "409", description = "RUDE duplicado en el tenant (E_RUDE_DUPLICADO)")
  public ResponseEntity<EstudianteResponse> crear(@Valid @RequestBody CrearEstudianteRequest request) {
    Estudiante estudiante =
        crearEstudianteUseCase.crear(
            new CrearEstudianteCommand(
                tenantActual(),
                request.rude(),
                request.nombreCompleto(),
                request.estado(),
                request.datosPersonales()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(estudiante));
  }

  @GetMapping
  @Operation(summary = "Listar Estudiantes del tenant (filtrable y paginado)")
  @ApiResponse(responseCode = "200", description = "Pagina de Estudiantes")
  public ResponseEntity<PageResponse<EstudianteResponse>> listar(
      @ParameterObject EstudianteFiltro filtro, @ParameterObject PaginacionParams paginacion) {
    var resultado =
        listarEstudiantesUseCase.listar(tenantActual(), filtro, PageQuery.of(paginacion.page(), paginacion.size()));
    return ResponseEntity.ok(PageResponse.from(resultado, this::aResponse));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener un Estudiante por id")
  @ApiResponse(responseCode = "200", description = "Estudiante")
  @ApiResponse(responseCode = "404", description = "Estudiante inexistente o de otro tenant (E_ESTUDIANTE_NO_ENCONTRADO)")
  public ResponseEntity<EstudianteResponse> obtener(@PathVariable UUID id) {
    return ResponseEntity.ok(aResponse(obtenerEstudianteUseCase.obtener(tenantActual(), id)));
  }

  @GetMapping("/{id}/inscripciones")
  @Operation(summary = "Listar inscripciones (historial) de un Estudiante")
  @ApiResponse(responseCode = "200", description = "Lista de inscripciones")
  @ApiResponse(responseCode = "404", description = "Estudiante inexistente o de otro tenant (E_ESTUDIANTE_NO_ENCONTRADO)")
  public ResponseEntity<List<InscripcionResponse>> listarInscripciones(@PathVariable UUID id) {
    List<InscripcionResponse> inscripciones =
        listarInscripcionesEstudianteUseCase.listar(tenantActual(), id).stream().map(this::aResponse).toList();
    return ResponseEntity.ok(inscripciones);
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status =
        switch (ex.getErrorCode()) {
          case "E_ESTUDIANTE_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
          default -> HttpStatus.CONFLICT;
        };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private EstudianteResponse aResponse(Estudiante estudiante) {
    return new EstudianteResponse(
        estudiante.getId().valor(),
        estudiante.getRude(),
        estudiante.getNombreCompleto(),
        estudiante.getEstado().name(),
        estudiante.getDatosPersonales());
  }

  private InscripcionResponse aResponse(Inscripcion inscripcion) {
    return new InscripcionResponse(
        inscripcion.getId().valor(),
        inscripcion.getEstudianteId().valor(),
        inscripcion.getGestionEscolarId().valor(),
        inscripcion.getCursoId().valor(),
        inscripcion.getParaleloId().valor(),
        inscripcion.getFechaInscripcion(),
        inscripcion.getEstado().name());
  }
}
