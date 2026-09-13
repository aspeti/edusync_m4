package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/** Campos {@code null} conservan el valor actual (mismo patron que {@code ActualizarPeriodoEvaluacionCommand}). */
public record ActualizarGestionEscolarCommand(
    UUID tenantId, UUID gestionId, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
}
