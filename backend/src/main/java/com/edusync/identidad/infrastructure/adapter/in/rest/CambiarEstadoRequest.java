package com.edusync.identidad.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO de entrada de {@code PATCH /api/v1/usuarios/{id}/estado} (DD-UC-005). */
public record CambiarEstadoRequest(@Schema(example = "false") boolean activo) {
}
