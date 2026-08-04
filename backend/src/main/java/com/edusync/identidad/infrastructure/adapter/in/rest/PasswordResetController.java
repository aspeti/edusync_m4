package com.edusync.identidad.infrastructure.adapter.in.rest;

import com.edusync.identidad.application.port.in.ConfirmarRestablecimientoPasswordUseCase;
import com.edusync.identidad.domain.TokenResetInvalidoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST publico del cierre del flujo de restablecimiento de contrasena
 * (DD-UC-005 &sect;1): el usuario no esta autenticado en este paso (por eso no vive en
 * {@code UsuarioController}, que exige rol {@code ADMIN}).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Confirmacion de restablecimiento de contrasena (DD-UC-005)")
public class PasswordResetController {

  private final ConfirmarRestablecimientoPasswordUseCase confirmarRestablecimientoPasswordUseCase;

  @PostMapping("/restablecer-password/confirmar")
  @Operation(
      summary = "Confirmar el restablecimiento de contrasena",
      description = "Publico; requiere un token vigente (FSD-UC-021, flujo alternativo A2).")
  @ApiResponse(responseCode = "200", description = "Contrasena actualizada")
  @ApiResponse(responseCode = "410", description = "Token usado o expirado (E_ENLACE_INVALIDO)")
  public ResponseEntity<Void> confirmar(@Valid @RequestBody ConfirmarResetRequest request) {
    confirmarRestablecimientoPasswordUseCase.confirmar(request.token(), request.passwordNuevo());
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler(TokenResetInvalidoException.class)
  public ResponseEntity<ErrorResponse> alManejarTokenInvalido(TokenResetInvalidoException ex) {
    return ResponseEntity.status(HttpStatus.GONE).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }
}
