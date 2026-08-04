package com.edusync.identidad.infrastructure.adapter.in.rest;

import java.util.Set;
import java.util.UUID;

/** DTO de salida del CRUD de Usuarios (DD-UC-005). Nunca expone {@code passwordHash}. */
public record UsuarioResponse(UUID id, String nombreCompleto, String email, Set<String> roles, boolean activo) {
}
