package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;

/** Campos opcionales: los nulos conservan el valor actual. */
public record ActualizarSeccionEvaluacionRequest(String nombre, BigDecimal nota) {
}
