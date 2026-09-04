package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/** DTO de {@code POST /api/v1/evaluaciones}. Sin {@code puntajeMaximo} ({@code DD-UC-017}). */
public record CrearEvaluacionRequest(
    @Schema(example = "Prueba escrita 1")
    @NotBlank(message = "nombre es obligatorio")
    @Size(max = 100, message = "nombre no puede superar 100 caracteres")
    String nombre,

    @NotNull(message = "materiaId es obligatorio")
    UUID materiaId,

    @NotNull(message = "periodoEvaluacionId es obligatorio")
    UUID periodoEvaluacionId,

    @NotNull(message = "seccionEvaluacionId es obligatorio")
    UUID seccionEvaluacionId,

    @NotNull(message = "fecha es obligatoria")
    LocalDate fecha,

    String descripcion) {}
