package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/** DTO de salida de {@code POST /api/v1/ai/consultar-usuario}. Nunca incluye passwordHash. */
public record UsuarioResumenResponse(
    @Schema(example = "Roberto Fernandez") String nombreCompleto,
    @Schema(example = "roberto.fernandez@colegio.edu.bo") String email,
    @Schema(example = "[\"PROFESOR\"]") Set<String> roles,
    @Schema(example = "true") boolean activo) {}
