package com.edusync.shared.ai.domain;

import java.util.Set;

/**
 * DTO minimo de un usuario encontrado por nombre (consulta en lenguaje natural). Nunca
 * incluye {@code passwordHash} ni {@code tenantId} — solo los campos seguros de mostrar
 * como resultado de una busqueda (AGENTS.md &sect;7).
 */
public record UsuarioResumen(String nombreCompleto, String email, Set<String> roles, boolean activo) {}
