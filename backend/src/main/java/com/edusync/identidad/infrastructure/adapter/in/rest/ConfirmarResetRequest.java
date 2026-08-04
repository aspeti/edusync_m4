package com.edusync.identidad.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de entrada de {@code POST /api/v1/auth/restablecer-password/confirmar} (publico, DD-UC-005). */
public record ConfirmarResetRequest(
    @Schema(example = "b3f1c9de-...")
    @NotBlank(message = "token es obligatorio")
    String token,

    @Schema(example = "nueva-contrasena")
    @NotBlank(message = "passwordNuevo es obligatorio")
    @Size(min = 8, message = "passwordNuevo debe tener al menos 8 caracteres")
    String passwordNuevo) {
}
