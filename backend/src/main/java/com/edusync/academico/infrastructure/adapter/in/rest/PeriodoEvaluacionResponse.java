package com.edusync.academico.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoEvaluacionResponse(
    UUID id,
    UUID gestionEscolarId,
    String nombre,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    int orden,
    String estado) {
}
