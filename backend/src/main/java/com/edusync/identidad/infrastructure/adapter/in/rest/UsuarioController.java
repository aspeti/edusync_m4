package com.edusync.identidad.infrastructure.adapter.in.rest;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.ActualizarRolesUsuarioUseCase;
import com.edusync.identidad.application.port.in.CambiarEstadoUsuarioUseCase;
import com.edusync.identidad.application.port.in.CrearUsuarioUseCase;
import com.edusync.identidad.application.port.in.IniciarRestablecimientoPasswordUseCase;
import com.edusync.identidad.application.port.in.ListarUsuariosUseCase;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.shared.exception.DomainException;
import com.edusync.shared.tenant.TenantContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
 * Adaptador REST del CRUD administrativo de Usuarios y Roles (DD-UC-005, cierra
 * {@code FSD-UC-021} — resto no cubierto por {@code AuthController}/{@code DD-UC-002}).
 * Todos los endpoints requieren rol {@code ADMIN} y operan exclusivamente sobre el tenant
 * del actor autenticado ({@link TenantContextProvider}, mismo patron mitigador de
 * {@code DD-UC-002} &sect;2): nunca se confia en un {@code tenantId} provisto por el
 * cliente.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "CRUD administrativo de Usuarios y Roles (DD-UC-005, solo ADMIN)")
public class UsuarioController {

  private final CrearUsuarioUseCase crearUsuarioUseCase;
  private final ListarUsuariosUseCase listarUsuariosUseCase;
  private final ActualizarRolesUsuarioUseCase actualizarRolesUsuarioUseCase;
  private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;
  private final IniciarRestablecimientoPasswordUseCase iniciarRestablecimientoPasswordUseCase;
  private final TenantContextProvider tenantContextProvider;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Crear un usuario del tenant", description = "Alta multi-rol (FSD-UC-021, pasos 1-2; ADR-0010).")
  @ApiResponse(responseCode = "201", description = "Usuario creado")
  @ApiResponse(responseCode = "422", description = "Roles vacios o rol incompatible (E_INVARIANTE_ROL_VIOLADA)")
  @ApiResponse(responseCode = "409", description = "Email ya registrado (E_EMAIL_EN_USO)")
  public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
    UUID tenantId = tenantActual();
    UsuarioId id = crearUsuarioUseCase.crear(new CrearUsuarioCommand(
        tenantId, request.nombreCompleto(), request.email(), request.passwordInicial(), request.roles()));
    UsuarioResponse response =
        new UsuarioResponse(id.valor(), request.nombreCompleto(), request.email(), request.roles(), true);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Listar usuarios del tenant", description = "Scoped al tenant del Admin autenticado (DD-UC-005 §1).")
  @ApiResponse(responseCode = "200", description = "Lista de usuarios")
  public ResponseEntity<List<UsuarioResponse>> listar() {
    List<UsuarioResponse> usuarios = listarUsuariosUseCase.listar(tenantActual()).stream()
        .map(this::aResponse)
        .toList();
    return ResponseEntity.ok(usuarios);
  }

  @PatchMapping("/{id}/roles")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Modificar los roles vigentes de un usuario", description = "FSD-UC-021, paso 3.")
  @ApiResponse(responseCode = "200", description = "Roles actualizados")
  @ApiResponse(responseCode = "422", description = "Roles vacios o rol incompatible (E_INVARIANTE_ROL_VIOLADA)")
  @ApiResponse(responseCode = "404", description = "Usuario inexistente o de otro tenant (E_USUARIO_NO_ENCONTRADO)")
  public ResponseEntity<UsuarioResponse> actualizarRoles(
      @PathVariable UUID id, @Valid @RequestBody ActualizarRolesRequest request) {
    Usuario usuario =
        actualizarRolesUsuarioUseCase.actualizarRoles(UsuarioId.de(id), tenantActual(), request.roles());
    return ResponseEntity.ok(aResponse(usuario));
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Activar o desactivar un usuario", description = "FSD-UC-021, paso 4.")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  @ApiResponse(responseCode = "404", description = "Usuario inexistente o de otro tenant (E_USUARIO_NO_ENCONTRADO)")
  public ResponseEntity<UsuarioResponse> cambiarEstado(
      @PathVariable UUID id, @Valid @RequestBody CambiarEstadoRequest request) {
    Usuario usuario = cambiarEstadoUsuarioUseCase.cambiarEstado(UsuarioId.de(id), tenantActual(), request.activo());
    return ResponseEntity.ok(aResponse(usuario));
  }

  @PostMapping("/{id}/restablecer-password")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Iniciar el restablecimiento de contrasena de un usuario",
      description = "FSD-UC-021, paso 5. Entrega real de notificacion pendiente (DD-UC-005 §1, placeholder log-only).")
  @ApiResponse(responseCode = "202", description = "Restablecimiento iniciado")
  @ApiResponse(responseCode = "404", description = "Usuario inexistente o de otro tenant (E_USUARIO_NO_ENCONTRADO)")
  public ResponseEntity<Void> iniciarRestablecimiento(@PathVariable UUID id) {
    iniciarRestablecimientoPasswordUseCase.iniciar(UsuarioId.de(id), tenantActual());
    return ResponseEntity.accepted().build();
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> alManejarErrorDeDominio(DomainException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
      case "E_USUARIO_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
      case "E_INVARIANTE_ROL_VIOLADA" -> HttpStatus.UNPROCESSABLE_CONTENT;
      case "E_EMAIL_EN_USO" -> HttpStatus.CONFLICT;
      default -> HttpStatus.CONFLICT;
    };
    return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  private UUID tenantActual() {
    return tenantContextProvider.tenantActual().orElseThrow();
  }

  private UsuarioResponse aResponse(Usuario usuario) {
    return new UsuarioResponse(
        usuario.getId().valor(),
        usuario.getNombreCompleto(),
        usuario.getEmail(),
        usuario.getRoles().stream().map(Rol::name).collect(Collectors.toUnmodifiableSet()),
        usuario.isActivo());
  }
}
