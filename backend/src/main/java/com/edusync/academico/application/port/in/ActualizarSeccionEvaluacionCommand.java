package com.edusync.academico.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/** Campos opcionales: los nulos conservan el valor actual. */
public record ActualizarSeccionEvaluacionCommand(
    UUID tenantId, UUID seccionId, String nombre, BigDecimal nota) {
}
