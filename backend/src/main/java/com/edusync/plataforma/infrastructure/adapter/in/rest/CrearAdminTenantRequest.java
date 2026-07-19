package com.edusync.plataforma.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de entrada de {@code POST /api/v1/plataforma/tenants/{id}/admins} ({@code FSD-UC-011}, paso 3). */
public record CrearAdminTenantRequest(
    @Schema(example = "Admin del Colegio")
    @NotBlank(message = "nombreCompleto es obligatorio")
    String nombreCompleto,

    @Schema(example = "admin@colegio.edu.bo")
    @NotBlank(message = "email es obligatorio")
    @Email(message = "email debe tener un formato valido")
    String email,

    @Schema(example = "secreto123")
    @NotBlank(message = "password es obligatorio")
    @Size(min = 8, message = "password debe tener al menos 8 caracteres")
    String password) {
}
