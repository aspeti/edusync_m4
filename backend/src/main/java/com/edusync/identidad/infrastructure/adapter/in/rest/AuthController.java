package com.edusync.identidad.infrastructure.adapter.in.rest;

import com.edusync.identidad.application.port.in.AutenticarUsuarioUseCase;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.domain.CredencialesInvalidasException;
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
public class AuthController {

  private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

  public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
    this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    TokenAcceso token = autenticarUsuarioUseCase.autenticar(request.email(), request.password());
    return ResponseEntity.ok(new LoginResponse(token.accessToken(), token.expiresInSeconds()));
  }

  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<ErrorResponse> alManejarCredencialesInvalidas(CredencialesInvalidasException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }
}
