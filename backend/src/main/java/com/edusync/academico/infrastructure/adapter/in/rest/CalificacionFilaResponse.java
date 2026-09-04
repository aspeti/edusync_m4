package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record CalificacionFilaResponse(
    UUID estudianteId, String nombreCompleto, String rude, BigDecimal valor) {}
