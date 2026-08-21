package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarEstadoPeriodoEvaluacionRequest(
    @Schema(example = "ABIERTO")
    @NotBlank(message = "estado es obligatorio")
    @Pattern(regexp = "PENDIENTE|ABIERTO|CERRADO", message = "estado debe ser PENDIENTE, ABIERTO o CERRADO")
    String estado) {
}
