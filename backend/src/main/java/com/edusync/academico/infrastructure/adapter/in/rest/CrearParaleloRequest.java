package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** DTO de entrada de {@code POST /api/v1/cursos/{id}/paralelos} ({@code FSD-UC-017}, paso 2). */
public record CrearParaleloRequest(
    @Schema(example = "A")
    @NotBlank(message = "nombre es obligatorio")
    String nombre) {
}
