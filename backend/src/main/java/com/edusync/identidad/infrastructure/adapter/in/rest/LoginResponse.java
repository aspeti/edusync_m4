package com.edusync.identidad.infrastructure.adapter.in.rest;

/** DTO de salida de {@code POST /api/v1/auth/login}. */
public record LoginResponse(String accessToken, long expiresIn) {
}
