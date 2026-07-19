package com.edusync.shared.web;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Normaliza los errores de validacion de Bean Validation ({@code @Valid} en DTOs de
 * {@code infrastructure/adapter/in/rest/}) al mismo formato {@code {codigo, mensaje}} que
 * ya usan los {@code ErrorResponse} especificos de cada modulo (ADR-0012). Vive en
 * {@code shared} (modulo {@code OPEN}) para aplicar a los controladores de cualquier
 * modulo sin duplicar este handler en cada uno.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> alManejarValidacionInvalida(MethodArgumentNotValidException ex) {
    String mensaje = ex.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining("; "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("E_VALIDACION", mensaje));
  }
}
