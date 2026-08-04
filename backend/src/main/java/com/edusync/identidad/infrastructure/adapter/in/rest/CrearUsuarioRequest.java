package com.edusync.identidad.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** DTO de entrada de {@code POST /api/v1/usuarios} (DD-UC-005). */
public record CrearUsuarioRequest(
    @Schema(example = "Marco Rios")
    @NotBlank(message = "nombreCompleto es obligatorio")
    String nombreCompleto,

    @Schema(example = "marco.rios@colegio.edu.bo")
    @NotBlank(message = "email es obligatorio")
    @Email(message = "email debe tener un formato valido")
    String email,

    @Schema(example = "secreto-inicial")
    @NotBlank(message = "passwordInicial es obligatorio")
    @Size(min = 8, message = "passwordInicial debe tener al menos 8 caracteres")
    String passwordInicial,

    @Schema(example = "[\"ADMIN\", \"SECRETARIA\"]")
    @NotEmpty(message = "roles no puede estar vacio")
    Set<String> roles) {
}
