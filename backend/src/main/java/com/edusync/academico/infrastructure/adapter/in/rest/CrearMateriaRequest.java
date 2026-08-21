package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** DTO de entrada de {@code POST /api/v1/materias} ({@code FSD-UC-018}, paso 1). */
public record CrearMateriaRequest(
    @Schema(example = "Matemáticas")
    @NotBlank(message = "nombre es obligatorio")
    String nombre) {}
