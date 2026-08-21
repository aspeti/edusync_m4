package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.AsignacionProfesorVista;
import com.edusync.academico.application.port.in.ListarAsignacionesPorProfesorUseCase;
import com.edusync.academico.application.port.in.ListarProfesoresUseCase;
import com.edusync.academico.application.port.in.ObtenerProfesorUseCase;
import com.edusync.academico.application.port.in.ProfesorFiltro;
import com.edusync.shared.PageQuery;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import com.edusync.shared.web.PageResponse;
import com.edusync.shared.web.PaginacionParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico de {@code FSD-UC-019} (Profesores, {@code DD-UC-014}). Solo lectura:
 * el alta permanece en {@code FSD-UC-021} y las escrituras de asignacion en {@code FSD-UC-018}.
 * Todos los endpoints requieren {@code ADMIN} o {@code SECRETARIA} y operan exclusivamente
 * sobre el tenant del actor autenticado ({@link TenantContextProvider}).
 */
@RestController
@RequestMapping("/api/v1/profesores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
@Tag(name = "Academico", description = "Consulta de Profesores y asignaciones (DD-UC-014, ADMIN o SECRETARIA)")
public class ProfesorController {

  private final ListarProfesoresUseCase listarProfesoresUseCase;
  private final ObtenerProfesorUseCase obtenerProfesorUseCase;
  private final ListarAsignacionesPorProfesorUseCase listarAsignacionesPorProfesorUseCase;
  private final TenantContextProvider tenantContextProvider;

  @GetMapping
  @Operation(summary = "Listar profesores del tenant (filtrable y paginado)")
  @ApiResponse(responseCode = "200", description = "Pagina de profesores")
  public ResponseEntity<PageResponse<ProfesorResponse>> listar(
      @ParameterObject ProfesorFiltro filtro, @ParameterObject PaginacionParams paginacion) {
    var resultado =
        listarProfesoresUseCase.listar(tenantActual(), filtro, PageQuery.of(paginacion.page(), paginacion.size()));
    return ResponseEntity.ok(PageResponse.from(resultado, this::aResponse));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener un profesor por id")
  @ApiResponse(responseCode = "200", description = "Profesor")
  @ApiResponse(responseCode = "404", description = "Profesor inexistente, sin rol o de otro tenant (E_PROFESOR_NO_ENCONTRADO)")
  public ResponseEntity<ProfesorResponse> obtener(@PathVariable UUID id) {
    return ResponseEntity.ok(aResponse(obtenerProfesorUseCase.obtener(tenantActual(), id)));
  }

  @GetMapping("/{id}/asignaciones")
  @Operation(summary = "Listar asignaciones Materia/Curso/Paralelo de un profesor")
  @ApiResponse(responseCode = "200", description = "Lista de asignaciones enriquecida")
  @ApiResponse(responseCode = "404", description = "Profesor inexistente, sin rol o de otro tenant (E_PROFESOR_NO_ENCONTRADO)")
  public ResponseEntity<List<AsignacionProfesorVistaResponse>> listarAsignaciones(@PathVariable UUID id) {
    List<AsignacionProfesorVistaResponse> asignaciones =
        listarAsignacionesPorProfesorUseCase.listar(tenantActual(), id).stream().map(this::aResponse).toList();
    return ResponseEntity.ok(asignaciones);
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status =
        "E_PROFESOR_NO_ENCONTRADO".equals(ex.getErrorCode()) ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private ProfesorResponse aResponse(ProfesorResumen profesor) {
    return new ProfesorResponse(profesor.id(), profesor.nombreCompleto(), profesor.activo());
  }

  private AsignacionProfesorVistaResponse aResponse(AsignacionProfesorVista vista) {
    return new AsignacionProfesorVistaResponse(
        vista.id(),
        vista.materiaId(),
        vista.materiaNombre(),
        vista.cursoId(),
        vista.cursoNombre(),
        vista.paraleloId(),
        vista.paraleloNombre());
  }
}
