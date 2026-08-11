package com.edusync.shared.ai.domain;

/** @param texto contenido generado @param modelo p. ej. {@code llama3.1:latest} */
public record RespuestaLlm(String texto, String modelo) {}
