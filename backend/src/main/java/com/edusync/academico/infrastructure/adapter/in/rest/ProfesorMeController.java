package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.application.port.in.ListarMisAsignacionesUseCase;
import com.edusync.academico.application.port.in.MisAsignacionVista;
import com.edusync.shared.tenant.TenantContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Landing del Profesor ({@code DD-UC-033} / {@code FSD-UC-022}):
 * {@code GET /profesores/me/asignaciones} declarado en controlador propio para no capturar
 * {@code me} como UUID en {@link ProfesorController}.
 */
@RestController
@RequestMapping("/api/v1/profesores/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
@Tag(name = "Academico", description = "Asignaciones del actor autenticado (DD-UC-033)")
public class ProfesorMeController {

  private final ListarMisAsignacionesUseCase listarMisAsignacionesUseCase;
  private final TenantContextProvider tenantContextProvider;

  @GetMapping("/asignaciones")
  @Operation(summary = "Asignaciones del Profesor autenticado (landing Mis cursos)")
  @ApiResponse(responseCode = "200", description = "Lista de asignaciones (puede estar vacía)")
  public ResponseEntity<List<MisAsignacionResponse>> listarAsignaciones(Authentication authentication) {
    UUID actorId = ActorSeguridad.id(authentication);
    List<MisAsignacionResponse> asignaciones =
        listarMisAsignacionesUseCase.listar(tenantActual(), actorId).stream()
            .map(this::aResponse)
            .toList();
    return ResponseEntity.ok(asignaciones);
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private MisAsignacionResponse aResponse(MisAsignacionVista vista) {
    return new MisAsignacionResponse(
        vista.asignacionId(),
        vista.materiaId(),
        vista.materiaNombre(),
        vista.cursoId(),
        vista.cursoNombre(),
        vista.paraleloId(),
        vista.paraleloNombre(),
        vista.cursoEtiqueta());
  }
}
