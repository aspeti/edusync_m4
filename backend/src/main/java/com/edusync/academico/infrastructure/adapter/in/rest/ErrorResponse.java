package com.edusync.academico.infrastructure.adapter.in.rest;

/** DTO de error uniforme para las respuestas 4xx del modulo {@code academico}. */
public record ErrorResponse(String codigo, String mensaje) {
}
