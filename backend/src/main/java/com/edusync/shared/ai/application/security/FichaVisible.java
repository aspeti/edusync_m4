package com.edusync.shared.ai.application.security;

/** Ficha sintética usada para decidir qué datos entran al contexto. */
public record FichaVisible(String id, String correo, String marcador, String curso) {
}
