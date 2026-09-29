package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase;
import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase.Comando;
import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase.Operacion;
import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase.Resultado;
import com.edusync.academico.application.port.in.ResolverEntidadAcademicaUseCase;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import com.edusync.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BFF de lectura del asistente ({@code ADR-0020}, {@code DD-UC-026}). IDs siempre del tenant JWT.
 */
@RestController
@RequestMapping("/api/v1/consultas-academicas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','PROFESOR')")
@Tag(name = "Academico", description = "Consultas parametrizables del asistente (DD-UC-026)")
public class ConsultaAcademicaController {

  private final ResolverEntidadAcademicaUseCase resolverEntidadAcademicaUseCase;
  private final ConsultarAcademicoUseCase consultarAcademicoUseCase;
  private final TenantContextProvider tenantContextProvider;

  @GetMapping("/entidades/estudiantes")
  @Operation(summary = "Resolver estudiantes por nombre (sin RUDE)")
  public ResponseEntity<ResolucionEntidadResponse> estudiantes(@RequestParam(required = false) String q) {
    return ResponseEntity.ok(aResponse(resolverEntidadAcademicaUseCase.resolver(tenantActual(), "ESTUDIANTE", q)));
  }

  @GetMapping("/entidades/materias")
  @Operation(summary = "Resolver materias por nombre")
  public ResponseEntity<ResolucionEntidadResponse> materias(@RequestParam(required = false) String q) {
    return ResponseEntity.ok(aResponse(resolverEntidadAcademicaUseCase.resolver(tenantActual(), "MATERIA", q)));
  }

  @GetMapping("/entidades/periodos")
  @Operation(summary = "Resolver periodos/trimestres de la gestión ACTIVA")
  public ResponseEntity<ResolucionEntidadResponse> periodos(@RequestParam(required = false) String q) {
    return ResponseEntity.ok(aResponse(resolverEntidadAcademicaUseCase.resolver(tenantActual(), "PERIODO", q)));
  }

  @GetMapping("/entidades/profesores")
  @Operation(summary = "Resolver profesores por nombre")
  public ResponseEntity<ResolucionEntidadResponse> profesores(@RequestParam(required = false) String q) {
    return ResponseEntity.ok(aResponse(resolverEntidadAcademicaUseCase.resolver(tenantActual(), "PROFESOR", q)));
  }

  @GetMapping("/entidades/curso-paralelo")
  @Operation(summary = "Resolver un curso+paralelo (ej. 1ro A)")
  public ResponseEntity<ResolucionEntidadResponse> cursoParalelo(@RequestParam(required = false) String q) {
    return ResponseEntity.ok(aResponse(resolverEntidadAcademicaUseCase.resolver(tenantActual(), "CURSO_PARALELO", q)));
  }

  @PostMapping("/consultar")
  @Operation(summary = "Consulta académica parametrizable (solo lectura)")
  public ResponseEntity<ConsultaAcademicaResponse> consultar(
      @Valid @RequestBody ConsultarAcademicoRequest request, Authentication authentication) {
    Resultado resultado =
        consultarAcademicoUseCase.consultar(
            tenantActual(),
            ActorSeguridad.id(authentication),
            ActorSeguridad.veTodasLasMaterias(authentication),
            new Comando(
                request.operacion(),
                request.estudianteId(),
                request.cursoId(),
                request.paraleloId(),
                request.materiaId(),
                request.periodoEvaluacionId(),
                request.gestionEscolarId(),
                request.umbral()));
    return ResponseEntity.ok(aResponse(resultado));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private static ResolucionEntidadResponse aResponse(ResolverEntidadAcademicaUseCase.Resultado r) {
    List<MatchEntidadResponse> matches =
        r.matches().stream()
            .map(m -> new MatchEntidadResponse(m.id(), m.etiqueta(), m.cursoId(), m.paraleloId()))
            .toList();
    return new ResolucionEntidadResponse(r.estado(), r.tipo(), matches);
  }

  private static ConsultaAcademicaResponse aResponse(Resultado r) {
    List<FilaConsultaResponse> filas =
        r.filas().stream()
            .map(f -> new FilaConsultaResponse(f.etiqueta(), f.valor(), f.detalle()))
            .toList();
    return new ConsultaAcademicaResponse(
        r.estado(), r.operacion(), r.preguntaAclaracion(), r.faltantes(), r.agregado(), filas);
  }

  public record ResolucionEntidadResponse(String estado, String tipo, List<MatchEntidadResponse> matches) {}

  public record MatchEntidadResponse(UUID id, String etiqueta, UUID cursoId, UUID paraleloId) {}

  public record ConsultarAcademicoRequest(
      @NotNull Operacion operacion,
      UUID estudianteId,
      UUID cursoId,
      UUID paraleloId,
      UUID materiaId,
      UUID periodoEvaluacionId,
      UUID gestionEscolarId,
      Integer umbral) {}

  public record ConsultaAcademicaResponse(
      String estado,
      String operacion,
      String preguntaAclaracion,
      List<String> faltantes,
      Integer agregado,
      List<FilaConsultaResponse> filas) {}

  public record FilaConsultaResponse(String etiqueta, Integer valor, String detalle) {}
}
