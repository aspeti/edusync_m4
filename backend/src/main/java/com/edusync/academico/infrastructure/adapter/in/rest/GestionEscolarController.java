package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.CambiarEstadoGestionEscolarUseCase;
import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.in.CrearGestionEscolarUseCase;
import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.application.port.in.ListarGestionesEscolaresUseCase;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import com.edusync.shared.web.PageResponse;
import com.edusync.shared.web.PaginacionParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * listado admite tambien {@code SECRETARIA} ({@code DD-UC-013}): el formulario de
 * inscripcion necesita el catalogo. Operan exclusivamente sobre el tenant del actor
 * autenticado ({@link TenantContextProvider}): nunca se confia en un {@code tenantId}
 * provisto por el cliente.
 */
@RestController
@RequestMapping("/api/v1/gestiones-escolares")
@RequiredArgsConstructor
@Tag(name = "Academico", description = "Gestion Escolar: alta, listado y ciclo de estado (DD-UC-008; GET tambien SECRETARIA desde DD-UC-013)")
public class GestionEscolarController {

  private final CrearGestionEscolarUseCase crearGestionEscolarUseCase;
  private final ListarGestionesEscolaresUseCase listarGestionesEscolaresUseCase;
  private final CambiarEstadoGestionEscolarUseCase cambiarEstadoGestionEscolarUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Crear una Gestion Escolar",
      description = "Nace en PLANIFICACION (FSD-UC-012, pasos 1-2).")
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
      case "E_GESTION_ESCOLAR_NO_ENCONTRADA" -> HttpStatus.NOT_FOUND;
      case "E_FECHAS_INVALIDAS", "E_ESTADO_INVALIDO" -> HttpStatus.UNPROCESSABLE_CONTENT;
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
}
