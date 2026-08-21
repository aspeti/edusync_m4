package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** DTO de entrada de {@code POST /api/v1/cursos} ({@code FSD-UC-017}, paso 1). */
public record CrearCursoRequest(
    @Schema(example = "Primero de Primaria")
    @NotBlank(message = "nombre es obligatorio")
    String nombre) {
}
