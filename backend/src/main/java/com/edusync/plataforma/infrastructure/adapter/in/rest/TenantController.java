package com.edusync.plataforma.infrastructure.adapter.in.rest;

import com.edusync.identidad.UsuarioId;
import com.edusync.plataforma.application.port.in.CambiarEstadoTenantUseCase;
import com.edusync.plataforma.application.port.in.CrearAdminTenantCommand;
import com.edusync.plataforma.application.port.in.CrearAdminTenantUseCase;
import com.edusync.plataforma.application.port.in.RegistrarTenantCommand;
import com.edusync.plataforma.application.port.in.RegistrarTenantUseCase;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import com.edusync.shared.exception.DomainException;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico de {@code FSD-UC-011} (Gestion de Tenants y Suscripciones,
 * {@code DD-UC-003}). Todos los endpoints requieren rol {@code SYSADMIN} ({@code ADR-0010}).
 *
 * <p>Alta de tenant y alta de admin son endpoints deliberadamente separados
 * ({@code DD-UC-003} &sect;2/&sect;3, alternativa C): nunca combinar en una sola llamada.
 */
@RestController
@RequestMapping("/api/v1/plataforma/tenants")
@RequiredArgsConstructor
@Tag(name = "Plataforma", description = "Alta y gestion de Tenants y Suscripciones (DD-UC-003, solo SYSADMIN)")
public class TenantController {

  private final RegistrarTenantUseCase registrarTenantUseCase;
  private final CambiarEstadoTenantUseCase cambiarEstadoTenantUseCase;
  private final CrearAdminTenantUseCase crearAdminTenantUseCase;

  @PostMapping
  @PreAuthorize("hasRole('SYSADMIN')")
  @Operation(
      summary = "Registrar un nuevo Tenant",
      description = "Crea el Tenant en estado ACTIVO (FSD-UC-011, paso 1).")
  @ApiResponse(responseCode = "201", description = "Tenant creado")
  @ApiResponse(responseCode = "422", description = "fechaVencimientoSuscripcion ausente (E_SUSCRIPCION_INCOMPLETA)")
  public ResponseEntity<TenantResponse> registrar(@Valid @RequestBody RegistrarTenantRequest request) {
    Tenant tenant = registrarTenantUseCase.registrar(new RegistrarTenantCommand(
        request.nombre(), request.fechaInicioSuscripcion(), request.fechaVencimientoSuscripcion()));
    return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(tenant));
  }

  @PostMapping("/{id}/admins")
  @PreAuthorize("hasRole('SYSADMIN')")
  @Operation(
      summary = "Crear el primer ADMIN de un Tenant",
      description = "Llamada separada de POST /tenants (FSD-UC-011, paso 3; DD-UC-003 §3, alternativa C).")
  @ApiResponse(responseCode = "201", description = "Usuario ADMIN creado")
  @ApiResponse(responseCode = "404", description = "Tenant inexistente (E_TENANT_NO_ENCONTRADO)")
  @ApiResponse(responseCode = "409", description = "Email ya registrado (E_EMAIL_EN_USO)")
  public ResponseEntity<AdminCreadoResponse> crearAdmin(
      @PathVariable UUID id, @Valid @RequestBody CrearAdminTenantRequest request) {
    UsuarioId usuarioId = crearAdminTenantUseCase.crearAdmin(new CrearAdminTenantCommand(
        TenantId.de(id), request.nombreCompleto(), request.email(), request.password()));
    return ResponseEntity.status(HttpStatus.CREATED).body(new AdminCreadoResponse(usuarioId.valor(), request.email()));
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('SYSADMIN')")
  @Operation(
      summary = "Cambiar el estado de un Tenant",
      description = "ACTIVO/SUSPENDIDO/VENCIDO manual (FSD-UC-011, paso 4).")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  @ApiResponse(responseCode = "404", description = "Tenant inexistente (E_TENANT_NO_ENCONTRADO)")
  public ResponseEntity<TenantResponse> cambiarEstado(
      @PathVariable UUID id, @Valid @RequestBody CambiarEstadoTenantRequest request) {
    Tenant tenant = cambiarEstadoTenantUseCase.cambiarEstado(
        TenantId.de(id), EstadoTenant.valueOf(request.estado()));
    return ResponseEntity.ok(aResponse(tenant));
  }

  /**
   * Cubre tanto las excepciones propias de {@code plataforma.domain} como las que
   * bubblean desde {@code identidad.UsuarioCreacionPort} (p. ej. {@code E_EMAIL_EN_USO}):
   * ambas extienden {@code shared.exception.DomainException} (tipo publico de
   * {@code shared}), asi que capturarla aqui NO requiere importar clases internas de
   * {@code identidad.domain} (que violaria {@code ADR-0011}).
   */
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_TENANT_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
      case "E_SUSCRIPCION_INCOMPLETA", "E_INVARIANTE_ROL_VIOLADA" -> HttpStatus.UNPROCESSABLE_CONTENT;
      case "E_EMAIL_EN_USO" -> HttpStatus.CONFLICT;
      default -> HttpStatus.CONFLICT;
    };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private TenantResponse aResponse(Tenant tenant) {
    return new TenantResponse(
        tenant.getId().valor(),
        tenant.getNombre(),
        tenant.getFechaInicioSuscripcion(),
        tenant.getFechaVencimientoSuscripcion(),
        tenant.getEstado().name());
  }
}
