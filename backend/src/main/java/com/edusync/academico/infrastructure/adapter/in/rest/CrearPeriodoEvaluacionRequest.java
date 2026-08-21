package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CrearPeriodoEvaluacionRequest(
    @Schema(example = "Bimestre 1")
    @NotBlank(message = "nombre es obligatorio")
    String nombre,

    @NotNull(message = "fechaInicio es obligatoria")
    LocalDate fechaInicio,

    @NotNull(message = "fechaFin es obligatoria")
    LocalDate fechaFin) {
}
