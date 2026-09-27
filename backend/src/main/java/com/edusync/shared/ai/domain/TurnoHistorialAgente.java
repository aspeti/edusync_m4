package com.edusync.shared.ai.domain;

/** Turno user/assistant enviado por la UI (no incluye tool). */
public record TurnoHistorialAgente(String role, String content) {}
