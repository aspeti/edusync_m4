package com.edusync.plataforma.infrastructure.adapter.in.rest;

/** DTO de error uniforme para las respuestas 4xx del modulo {@code plataforma}. */
public record ErrorResponse(String codigo, String mensaje) {
}
