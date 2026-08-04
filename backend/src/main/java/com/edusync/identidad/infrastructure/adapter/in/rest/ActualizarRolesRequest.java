package com.edusync.identidad.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/** DTO de entrada de {@code PATCH /api/v1/usuarios/{id}/roles} (DD-UC-005). */
public record ActualizarRolesRequest(
    @Schema(example = "[\"ADMIN\", \"SECRETARIA\"]")
    @NotEmpty(message = "roles no puede estar vacio")
    Set<String> roles) {
}
