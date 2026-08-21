package com.edusync.academico.infrastructure.adapter.in.rest;

import com.edusync.academico.domain.EstadoEstudiante;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** DTO de entrada de {@code POST /api/v1/estudiantes} ({@code FSD-UC-020}, paso 1). */
public record CrearEstudianteRequest(
    @Schema(example = "12345678901")
    @NotBlank(message = "rude es obligatorio")
    @Size(max = 20, message = "rude no puede superar 20 caracteres")
    String rude,

    @Schema(example = "Ana Pérez")
    @NotBlank(message = "nombreCompleto es obligatorio")
    @Size(max = 200, message = "nombreCompleto no puede superar 200 caracteres")
    String nombreCompleto,

    @Schema(example = "ACTIVO")
    EstadoEstudiante estado,

    Map<String, String> datosPersonales) {}
