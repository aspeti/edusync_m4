package com.edusync.academico.infrastructure.adapter.in.rest;

import java.time.LocalDate;

/** Campos opcionales: los nulos conservan el valor actual. */
public record ActualizarPeriodoEvaluacionRequest(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
}
