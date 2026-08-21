package com.edusync.academico.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param tenantId siempre de {@code TenantContextProvider}
 */
public record CrearSeccionEvaluacionCommand(
    UUID tenantId, UUID gestionEscolarId, String nombre, int orden, BigDecimal nota) {
}
