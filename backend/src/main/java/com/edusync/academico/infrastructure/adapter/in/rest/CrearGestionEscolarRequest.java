package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO de entrada de {@code POST /api/v1/gestiones-escolares} ({@code FSD-UC-012}, pasos 1-2).
 *
 * <p>{@code fechaInicio}/{@code fechaFin} son obligatorias a nivel de Bean Validation
 * (HTTP 400 si faltan); la validacion de que {@code fechaFin} sea posterior a
 * {@code fechaInicio} es un caso de negocio con codigo propio ({@code E_FECHAS_INVALIDAS},
 * HTTP 422, flujo alternativo A1), no un error de validacion generico.
 */
public record CrearGestionEscolarRequest(
    @Schema(example = "2027")
    @NotBlank(message = "nombre es obligatorio")
    String nombre,

    @Schema(example = "2027-02-01")
    @NotNull(message = "fechaInicio es obligatoria")
    LocalDate fechaInicio,

    @Schema(example = "2027-11-30")
    @NotNull(message = "fechaFin es obligatoria")
    LocalDate fechaFin) {
}
