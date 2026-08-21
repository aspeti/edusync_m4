package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.ActualizarSeccionEvaluacionCommand;
import com.edusync.academico.application.port.in.ActualizarSeccionEvaluacionUseCase;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recurso propio de {@link SeccionEvaluacion} ({@code FSD-UC-014}, {@code DD-UC-016}):
 * PATCH de nombre/nota. El alta, listado y reemplazo viven anidados en
 * {@link GestionEscolarController}.
 */
@RestController
@RequestMapping("/api/v1/secciones-evaluacion")
@RequiredArgsConstructor
@Tag(name = "Academico", description = "Secciones de Evaluacion (DD-UC-016 / FSD-UC-014)")
public class SeccionEvaluacionController {

  private final ActualizarSeccionEvaluacionUseCase actualizarSeccionEvaluacionUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Actualizar nombre y/o nota de una seccion")
  @ApiResponse(responseCode = "200", description = "Seccion actualizada")
  @ApiResponse(responseCode = "404", description = "E_SECCION_NO_ENCONTRADA")
  @ApiResponse(responseCode = "422", description = "E_PESO_INVALIDO / E_SUMA_SECCIONES_INVALIDA / E_SECCIONES_INMUTABLES")
  public ResponseEntity<SeccionEvaluacionResponse> actualizar(
      @PathVariable UUID id, @RequestBody ActualizarSeccionEvaluacionRequest request) {
    SeccionEvaluacion seccion = actualizarSeccionEvaluacionUseCase.actualizar(
        new ActualizarSeccionEvaluacionCommand(
            tenantActual(), id, request.nombre(), request.nota()));
    return ResponseEntity.ok(aSeccionResponse(seccion));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_SECCION_NO_ENCONTRADA", "E_GESTION_ESCOLAR_NO_ENCONTRADA" -> HttpStatus.NOT_FOUND;
      case "E_PESO_INVALIDO", "E_SUMA_SECCIONES_INVALIDA", "E_SECCIONES_INMUTABLES" ->
          HttpStatus.UNPROCESSABLE_CONTENT;
      default -> HttpStatus.CONFLICT;
    };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
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
