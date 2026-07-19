package com.edusync.shared.web;

/**
 * DTO de error uniforme para respuestas 4xx transversales (validacion de Bean Validation,
 * hoy; otros errores comunes en el futuro). Estructuralmente igual al {@code ErrorResponse}
 * especifico de cada modulo (p. ej. {@code identidad.infrastructure.adapter.in.rest}), pero
 * vive en {@code shared} porque lo consume {@link GlobalExceptionHandler}, que aplica a
 * cualquier controlador de cualquier modulo (ADR-0012).
 */
public record ErrorResponse(String codigo, String mensaje) {
}
