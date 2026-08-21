package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.CrearAsignacionCursoCommand;
import com.edusync.academico.application.port.in.CrearAsignacionCursoUseCase;
import com.edusync.academico.application.port.in.CrearAsignacionProfesorCommand;
import com.edusync.academico.application.port.in.CrearAsignacionProfesorUseCase;
import com.edusync.academico.application.port.in.CrearMateriaCommand;
import com.edusync.academico.application.port.in.CrearMateriaUseCase;
import com.edusync.academico.application.port.in.ListarAsignacionesCursoUseCase;
import com.edusync.academico.application.port.in.ListarAsignacionesProfesorUseCase;
import com.edusync.academico.application.port.in.ListarMateriasUseCase;
import com.edusync.academico.application.port.in.ListarProfesoresDisponiblesUseCase;
import com.edusync.academico.application.port.in.MateriaFiltro;
import com.edusync.academico.application.port.in.ObtenerMateriaUseCase;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.Materia;
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
 * Adaptador REST publico de {@code FSD-UC-018} (Materias, {@code DD-UC-012}). Todos los
 * endpoints requieren {@code ADMIN} o {@code SECRETARIA} y operan exclusivamente sobre el
 * tenant del actor autenticado ({@link TenantContextProvider}): nunca se confia en un
 * {@code tenantId} provisto por el cliente.
 */
@RestController
@RequestMapping("/api/v1/materias")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
@Tag(name = "Academico", description = "Materias y asignaciones (DD-UC-012, ADMIN o SECRETARIA)")
public class MateriaController {

  private final CrearMateriaUseCase crearMateriaUseCase;
  private final ListarMateriasUseCase listarMateriasUseCase;
  private final ObtenerMateriaUseCase obtenerMateriaUseCase;
  private final CrearAsignacionCursoUseCase crearAsignacionCursoUseCase;
  private final ListarAsignacionesCursoUseCase listarAsignacionesCursoUseCase;
  private final CrearAsignacionProfesorUseCase crearAsignacionProfesorUseCase;
  private final ListarAsignacionesProfesorUseCase listarAsignacionesProfesorUseCase;
  private final ListarProfesoresDisponiblesUseCase listarProfesoresDisponiblesUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @Operation(summary = "Crear una Materia", description = "FSD-UC-018, paso 1.")
  @ApiResponse(responseCode = "201", description = "Materia creada")
  public ResponseEntity<MateriaResponse> crear(@Valid @RequestBody CrearMateriaRequest request) {
    Materia materia = crearMateriaUseCase.crear(new CrearMateriaCommand(tenantActual(), request.nombre()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(materia));
  }

  @GetMapping
  @Operation(summary = "Listar Materias del tenant (filtrable y paginado)")
  @ApiResponse(responseCode = "200", description = "Pagina de Materias")
  public ResponseEntity<PageResponse<MateriaResponse>> listar(
      @ParameterObject MateriaFiltro filtro, @ParameterObject PaginacionParams paginacion) {
    var resultado =
        listarMateriasUseCase.listar(tenantActual(), filtro, PageQuery.of(paginacion.page(), paginacion.size()));
    return ResponseEntity.ok(PageResponse.from(resultado, this::aResponse));
  }

  @GetMapping("/profesores-disponibles")
  @Operation(summary = "Catalogo de profesores activos del tenant")
  @ApiResponse(responseCode = "200", description = "Lista de profesores")
  public ResponseEntity<List<ProfesorResumenResponse>> listarProfesoresDisponibles() {
    List<ProfesorResumenResponse> profesores =
        listarProfesoresDisponiblesUseCase.listar(tenantActual()).stream().map(this::aResponse).toList();
    return ResponseEntity.ok(profesores);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener una Materia por id")
  @ApiResponse(responseCode = "200", description = "Materia")
  @ApiResponse(responseCode = "404", description = "Materia inexistente o de otro tenant (E_MATERIA_NO_ENCONTRADA)")
  public ResponseEntity<MateriaResponse> obtener(@PathVariable UUID id) {
    return ResponseEntity.ok(aResponse(obtenerMateriaUseCase.obtener(tenantActual(), id)));
  }

  @PostMapping("/{id}/asignaciones-curso")
  @Operation(summary = "Asignar la Materia a un Curso/Paralelo", description = "FSD-UC-018, paso 2.")
  @ApiResponse(responseCode = "201", description = "Asignacion creada")
  public ResponseEntity<AsignacionCursoResponse> crearAsignacionCurso(
      @PathVariable UUID id, @Valid @RequestBody CrearAsignacionCursoRequest request) {
    AsignacionMateriaCurso asignacion =
        crearAsignacionCursoUseCase.crear(
            new CrearAsignacionCursoCommand(tenantActual(), id, request.cursoId(), request.paraleloId()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(asignacion));
  }

  @GetMapping("/{id}/asignaciones-curso")
  @Operation(summary = "Listar asignaciones Curso/Paralelo de una Materia")
  public ResponseEntity<List<AsignacionCursoResponse>> listarAsignacionesCurso(@PathVariable UUID id) {
    List<AsignacionCursoResponse> asignaciones =
        listarAsignacionesCursoUseCase.listar(tenantActual(), id).stream().map(this::aResponse).toList();
    return ResponseEntity.ok(asignaciones);
  }

  @PostMapping("/{id}/asignaciones-profesor")
  @Operation(summary = "Asignar un Profesor a la Materia", description = "FSD-UC-018, paso 3. A1: 409 E_MATERIA_SIN_CURSO.")
  @ApiResponse(responseCode = "201", description = "Asignacion creada")
  @ApiResponse(responseCode = "409", description = "Falta asignacion curso previa (E_MATERIA_SIN_CURSO)")
  public ResponseEntity<AsignacionProfesorResponse> crearAsignacionProfesor(
      @PathVariable UUID id, @Valid @RequestBody CrearAsignacionProfesorRequest request) {
    AsignacionMateriaProfesor asignacion =
        crearAsignacionProfesorUseCase.crear(
            new CrearAsignacionProfesorCommand(
                tenantActual(), id, request.profesorId(), request.cursoId(), request.paraleloId()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(asignacion));
  }

  @GetMapping("/{id}/asignaciones-profesor")
  @Operation(summary = "Listar asignaciones Profesor de una Materia")
  public ResponseEntity<List<AsignacionProfesorResponse>> listarAsignacionesProfesor(@PathVariable UUID id) {
    List<AsignacionProfesorResponse> asignaciones =
        listarAsignacionesProfesorUseCase.listar(tenantActual(), id).stream().map(this::aResponse).toList();
    return ResponseEntity.ok(asignaciones);
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status =
        switch (ex.getErrorCode()) {
          case "E_MATERIA_NO_ENCONTRADA",
              "E_CURSO_NO_ENCONTRADO",
              "E_PARALELO_NO_ENCONTRADO",
              "E_PROFESOR_NO_ENCONTRADO" ->
              HttpStatus.NOT_FOUND;
          default -> HttpStatus.CONFLICT;
        };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private MateriaResponse aResponse(Materia materia) {
    return new MateriaResponse(materia.getId().valor(), materia.getNombre());
  }

  private AsignacionCursoResponse aResponse(AsignacionMateriaCurso asignacion) {
    return new AsignacionCursoResponse(
        asignacion.getId().valor(),
        asignacion.getMateriaId().valor(),
        asignacion.getCursoId().valor(),
        asignacion.getParaleloId().valor());
  }

  private AsignacionProfesorResponse aResponse(AsignacionMateriaProfesor asignacion) {
    return new AsignacionProfesorResponse(
        asignacion.getId().valor(),
        asignacion.getMateriaId().valor(),
        asignacion.getProfesorId(),
        asignacion.getCursoId().valor(),
        asignacion.getParaleloId().valor());
  }

  private ProfesorResumenResponse aResponse(ProfesorResumen profesor) {
    return new ProfesorResumenResponse(profesor.id(), profesor.nombreCompleto());
  }
}
