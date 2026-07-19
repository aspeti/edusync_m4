package com.edusync.identidad.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO de entrada de {@code POST /api/v1/auth/login} (AGENTS.md &sect;5: DTOs en infrastructure/web). */
public record LoginRequest(
    @Schema(example = "sysadmin@edusync.local")
    @NotBlank(message = "email es obligatorio")
    @Email(message = "email debe tener un formato valido")
    String email,

    @Schema(example = "secreto")
    @NotBlank(message = "password es obligatorio")
    String password) {
}
