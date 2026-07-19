package com.edusync.identidad.infrastructure.adapter.in.rest;

/** DTO de entrada de {@code POST /api/v1/auth/login} (AGENTS.md &sect;5: DTOs en infrastructure/web). */
public record LoginRequest(String email, String password) {
}
