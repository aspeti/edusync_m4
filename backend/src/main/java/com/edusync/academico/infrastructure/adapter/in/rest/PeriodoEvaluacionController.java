package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.ActualizarPeriodoEvaluacionCommand;
import com.edusync.academico.application.port.in.ActualizarPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.in.CambiarEstadoPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.in.EliminarPeriodoEvaluacionUseCase;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recurso propio de {@link PeriodoEvaluacion} ({@code FSD-UC-013}, {@code DD-UC-015}):
 * PATCH datos, DELETE y PATCH estado. El alta y el listado viven anidados en
 * {@link GestionEscolarController}.
 */
@RestController
@RequestMapping("/api/v1/periodos-evaluacion")
@RequiredArgsConstructor
@Tag(name = "Academico", description = "Periodos de Evaluacion (DD-UC-015 / FSD-UC-013)")
public class PeriodoEvaluacionController {

  private final ActualizarPeriodoEvaluacionUseCase actualizarPeriodoEvaluacionUseCase;
  private final EliminarPeriodoEvaluacionUseCase eliminarPeriodoEvaluacionUseCase;
  private final CambiarEstadoPeriodoEvaluacionUseCase cambiarEstadoPeriodoEvaluacionUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Actualizar nombre y fechas de un periodo")
  @ApiResponse(responseCode = "200", description = "Periodo actualizado")
  @ApiResponse(responseCode = "404", description = "E_PERIODO_NO_ENCONTRADO")
  @ApiResponse(responseCode = "422", description = "E_FECHAS_INVALIDAS / E_PERIODOS_SOLAPADOS / E_PERIODOS_INMUTABLES")
  public ResponseEntity<PeriodoEvaluacionResponse> actualizar(
      @PathVariable UUID id, @RequestBody ActualizarPeriodoEvaluacionRequest request) {
    PeriodoEvaluacion periodo = actualizarPeriodoEvaluacionUseCase.actualizar(
        new ActualizarPeriodoEvaluacionCommand(
            tenantActual(), id, request.nombre(), request.fechaInicio(), request.fechaFin()));
    return ResponseEntity.ok(aResponse(periodo));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Eliminar un periodo (solo si todos estan PENDIENTE y N>1)")
  @ApiResponse(responseCode = "204", description = "Eliminado")
  @ApiResponse(responseCode = "404", description = "E_PERIODO_NO_ENCONTRADO")
  @ApiResponse(responseCode = "422", description = "E_PERIODOS_INMUTABLES / E_PERIODO_UNICO")
  public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
    eliminarPeriodoEvaluacionUseCase.eliminar(tenantActual(), id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Abrir o cerrar un periodo (apertura secuencial)")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  @ApiResponse(responseCode = "404", description = "E_PERIODO_NO_ENCONTRADO")
  @ApiResponse(responseCode = "422", description = "E_PERIODO_NO_SECUENCIAL / E_ESTADO_INVALIDO")
  public ResponseEntity<PeriodoEvaluacionResponse> cambiarEstado(
      @PathVariable UUID id, @Valid @RequestBody CambiarEstadoPeriodoEvaluacionRequest request) {
    PeriodoEvaluacion periodo = cambiarEstadoPeriodoEvaluacionUseCase.cambiarEstado(
        tenantActual(), id, EstadoPeriodoEvaluacion.valueOf(request.estado()));
    return ResponseEntity.ok(aResponse(periodo));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_PERIODO_NO_ENCONTRADO", "E_GESTION_ESCOLAR_NO_ENCONTRADA" -> HttpStatus.NOT_FOUND;
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

  private PeriodoEvaluacionResponse aResponse(PeriodoEvaluacion periodo) {
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
