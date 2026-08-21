package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.CrearInscripcionCommand;
import com.edusync.academico.application.port.in.CrearInscripcionUseCase;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico de {@code FSD-UC-020} (Inscripciones, {@code DD-UC-013}). POST
 * top-level segun el FSD; el historial se consulta anidado en {@link EstudianteController}.
 */
@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
@Tag(name = "Academico", description = "Inscripciones (DD-UC-013, ADMIN o SECRETARIA)")
public class InscripcionController {

  private final CrearInscripcionUseCase crearInscripcionUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @Operation(
      summary = "Crear una Inscripcion",
      description = "FSD-UC-020, pasos 2-3. Nace ACTIVA. A1: 409 E_INSCRIPCION_DUPLICADA.")
  @ApiResponse(responseCode = "201", description = "Inscripcion creada")
  @ApiResponse(responseCode = "404", description = "Padre inexistente o de otro tenant")
  @ApiResponse(responseCode = "409", description = "Estudiante ya inscrito en esa Gestion Escolar")
  public ResponseEntity<InscripcionResponse> crear(@Valid @RequestBody CrearInscripcionRequest request) {
    Inscripcion inscripcion =
        crearInscripcionUseCase.crear(
            new CrearInscripcionCommand(
                tenantActual(),
                request.estudianteId(),
                request.gestionEscolarId(),
                request.cursoId(),
                request.paraleloId(),
                request.fechaInscripcion()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(inscripcion));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status =
        switch (ex.getErrorCode()) {
          case "E_ESTUDIANTE_NO_ENCONTRADO",
              "E_GESTION_ESCOLAR_NO_ENCONTRADA",
              "E_CURSO_NO_ENCONTRADO",
              "E_PARALELO_NO_ENCONTRADO" ->
              HttpStatus.NOT_FOUND;
          default -> HttpStatus.CONFLICT;
        };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
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
