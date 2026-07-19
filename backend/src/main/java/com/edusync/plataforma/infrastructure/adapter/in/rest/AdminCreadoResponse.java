package com.edusync.plataforma.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida de {@code POST /api/v1/plataforma/tenants/{id}/admins}. */
public record AdminCreadoResponse(UUID usuarioId, String email) {
}
