package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarEstadoEvaluacionRequest(
    @Schema(example = "ANULADA")
    @NotBlank(message = "estado es obligatorio")
    @Pattern(regexp = "ANULADA", message = "estado debe ser ANULADA")
    String estado) {}
