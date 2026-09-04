package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

/** Respuesta corta de un upsert (sin PII de nómina). */
public record CalificacionResponse(
    UUID id, UUID evaluacionId, UUID estudianteId, BigDecimal valor) {}
