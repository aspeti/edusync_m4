package com.edusync.plataforma.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** DTO de entrada de {@code PATCH /api/v1/plataforma/tenants/{id}/estado} ({@code FSD-UC-011}, paso 4). */
public record CambiarEstadoTenantRequest(
    @Schema(example = "SUSPENDIDO")
    @NotBlank(message = "estado es obligatorio")
    @Pattern(regexp = "ACTIVO|SUSPENDIDO|VENCIDO", message = "estado debe ser ACTIVO, SUSPENDIDO o VENCIDO")
    String estado) {
}
