package com.edusync.identidad.application.port.in;

/**
 * Resultado de un login exitoso. DTO de aplicacion (no domain, no JPA): puede
 * reutilizarse directamente desde la capa REST sin violar AGENTS.md &sect;5
 * ("MUST NOT exponer entidades JPA ni clases de dominio directamente").
 */
public record TokenAcceso(String accessToken, long expiresInSeconds) {
}
