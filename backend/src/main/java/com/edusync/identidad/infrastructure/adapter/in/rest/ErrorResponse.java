package com.edusync.identidad.infrastructure.adapter.in.rest;

/** DTO de error uniforme para las respuestas 4xx del modulo {@code identidad}. */
public record ErrorResponse(String codigo, String mensaje) {
}
