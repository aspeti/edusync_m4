package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** DTO de entrada de {@code PATCH /api/v1/gestiones-escolares/{id}/estado} ({@code FSD-UC-012}, pasos 3-4). */
public record CambiarEstadoGestionEscolarRequest(
    @Schema(example = "ACTIVA")
    @NotBlank(message = "estado es obligatorio")
    @Pattern(regexp = "PLANIFICACION|ACTIVA|CERRADA", message = "estado debe ser PLANIFICACION, ACTIVA o CERRADA")
    String estado) {
}
