package com.edusync.identidad.infrastructure.adapter.in.rest;

import com.edusync.identidad.application.port.in.AutenticarUsuarioUseCase;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.domain.CredencialesInvalidasException;
import com.edusync.identidad.domain.TenantNoActivoException;
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

/** Adaptador REST publico del login (FSD-UC-021, parcial). */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Login stateless con JWT (DD-UC-002)")
public class AuthController {

  private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

  @PostMapping("/login")
  @Operation(
      summary = "Iniciar sesion",
      description = "Unico endpoint publico del modulo identidad; el resto exige Bearer JWT.")
  @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve el JWT")
  @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
  @ApiResponse(responseCode = "403", description = "Tenant suspendido o vencido (BR-014)")
  @ApiResponse(responseCode = "400", description = "Validacion de entrada fallida")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenAcceso token = autenticarUsuarioUseCase.autenticar(request.email(), request.password());
    return ResponseEntity.ok(new LoginResponse(token.accessToken(), token.expiresInSeconds()));
  }

  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<ErrorResponse> alManejarCredencialesInvalidas(CredencialesInvalidasException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  @ExceptionHandler(TenantNoActivoException.class)
  public ResponseEntity<ErrorResponse> alManejarTenantNoActivo(TenantNoActivoException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }
}
