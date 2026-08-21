package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.CrearCursoCommand;
import com.edusync.academico.application.port.in.CrearCursoUseCase;
import com.edusync.academico.application.port.in.CrearParaleloCommand;
import com.edusync.academico.application.port.in.CrearParaleloUseCase;
import com.edusync.academico.application.port.in.CursoFiltro;
import com.edusync.academico.application.port.in.ListarCursosUseCase;
import com.edusync.academico.application.port.in.ListarParalelosUseCase;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.Paralelo;
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
 * Adaptador REST publico de {@code FSD-UC-017} (Cursos y Paralelos, {@code DD-UC-010}). Todos
 * los {@code POST} requieren rol {@code ADMIN}; los {@code GET} tambien admiten
 * {@code SECRETARIA} ({@code DD-UC-012}: el formulario de asignacion de Materias necesita
 * esos listados). Operan exclusivamente sobre el tenant del actor autenticado
 * ({@link TenantContextProvider}): nunca se confia en un {@code tenantId} provisto por el
 * cliente.
 */
@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor
@Tag(name = "Academico", description = "Cursos y Paralelos: alta (ADMIN) y listado (ADMIN o SECRETARIA)")
public class CursoController {

  private final CrearCursoUseCase crearCursoUseCase;
  private final ListarCursosUseCase listarCursosUseCase;
  private final CrearParaleloUseCase crearParaleloUseCase;
  private final ListarParalelosUseCase listarParalelosUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Crear un Curso", description = "FSD-UC-017, paso 1.")
  @ApiResponse(responseCode = "201", description = "Curso creado")
  public ResponseEntity<CursoResponse> crear(@Valid @RequestBody CrearCursoRequest request) {
    Curso curso = crearCursoUseCase.crear(new CrearCursoCommand(tenantActual(), request.nombre()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(curso));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
  @Operation(
      summary = "Listar Cursos del tenant (filtrable y paginado)",
      description =
          "Scoped al tenant del Admin autenticado (DD-UC-010 §2). Filtros y paginacion "
              + "opcionales (DD-UC-007): sin query params, page=0 y size=20 por defecto.")
  @ApiResponse(responseCode = "200", description = "Pagina de Cursos")
  public ResponseEntity<PageResponse<CursoResponse>> listar(
      @ParameterObject CursoFiltro filtro, @ParameterObject PaginacionParams paginacion) {
    var resultado =
        listarCursosUseCase.listar(tenantActual(), filtro, PageQuery.of(paginacion.page(), paginacion.size()));
    return ResponseEntity.ok(PageResponse.from(resultado, this::aResponse));
  }

  @PostMapping("/{id}/paralelos")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Crear un Paralelo dentro de un Curso", description = "FSD-UC-017, paso 2.")
  @ApiResponse(responseCode = "201", description = "Paralelo creado")
  @ApiResponse(responseCode = "404", description = "Curso inexistente o de otro tenant (E_CURSO_NO_ENCONTRADO)")
  public ResponseEntity<ParaleloResponse> crearParalelo(
      @PathVariable UUID id, @Valid @RequestBody CrearParaleloRequest request) {
    Paralelo paralelo =
        crearParaleloUseCase.crear(new CrearParaleloCommand(tenantActual(), id, request.nombre()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(paralelo));
  }

  @GetMapping("/{id}/paralelos")
  @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
  @Operation(
      summary = "Listar los Paralelos de un Curso",
      description = "Sin paginar (DD-UC-010 §2: cardinalidad acotada).")
  @ApiResponse(responseCode = "200", description = "Lista de Paralelos")
  @ApiResponse(responseCode = "404", description = "Curso inexistente o de otro tenant (E_CURSO_NO_ENCONTRADO)")
  public ResponseEntity<List<ParaleloResponse>> listarParalelos(@PathVariable UUID id) {
    List<ParaleloResponse> paralelos =
        listarParalelosUseCase.listar(tenantActual(), id).stream().map(this::aResponse).toList();
    return ResponseEntity.ok(paralelos);
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_CURSO_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
      default -> HttpStatus.CONFLICT;
    };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private CursoResponse aResponse(Curso curso) {
    return new CursoResponse(curso.getId().valor(), curso.getNombre());
  }

  private ParaleloResponse aResponse(Paralelo paralelo) {
    return new ParaleloResponse(paralelo.getId().valor(), paralelo.getCursoId().valor(), paralelo.getNombre());
  }
}
