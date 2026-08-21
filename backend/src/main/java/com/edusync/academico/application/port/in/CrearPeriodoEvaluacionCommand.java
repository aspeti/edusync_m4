package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @param tenantId siempre de {@code TenantContextProvider}
 */
public record CrearPeriodoEvaluacionCommand(
    UUID tenantId, UUID gestionEscolarId, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
}
