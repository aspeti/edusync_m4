package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record SeccionEvaluacionResponse(
    UUID id, UUID gestionEscolarId, String nombre, int orden, BigDecimal nota) {
}
