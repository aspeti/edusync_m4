package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record ParametroPeriodoResponse(
    UUID id,
    UUID periodoEvaluacionId,
    UUID seccionEvaluacionId,
    BigDecimal rangoMin,
    BigDecimal rangoMax,
    String reglaCombinacion) {}
