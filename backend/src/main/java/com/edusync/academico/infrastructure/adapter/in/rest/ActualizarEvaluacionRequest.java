package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** DTO de {@code PATCH /api/v1/evaluaciones/{id}}. Campos omitidos conservan el valor. */
public record ActualizarEvaluacionRequest(
    @Schema(example = "Prueba escrita 1 (recuperatorio)")
    @Size(max = 100, message = "nombre no puede superar 100 caracteres")
    String nombre,
    LocalDate fecha,
    String descripcion) {}
