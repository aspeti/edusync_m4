package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record ActualizarPeriodoEvaluacionCommand(
    UUID tenantId, UUID periodoId, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
}
