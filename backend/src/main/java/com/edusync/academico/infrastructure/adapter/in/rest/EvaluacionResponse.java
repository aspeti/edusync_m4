package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EvaluacionResponse(
    UUID id,
    UUID materiaId,
    UUID periodoEvaluacionId,
    UUID seccionEvaluacionId,
    String nombre,
    LocalDate fecha,
    BigDecimal puntajeMaximo,
    String descripcion,
    String estado) {}
