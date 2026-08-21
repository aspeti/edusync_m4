package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CrearSeccionEvaluacionRequest(
    @Schema(example = "Saber")
    @NotBlank(message = "nombre es obligatorio")
    String nombre,

    @Schema(example = "2")
    @NotNull(message = "orden es obligatorio")
    Integer orden,

    @Schema(example = "45.00")
    @NotNull(message = "nota es obligatoria")
    BigDecimal nota) {
}
