package com.edusync.shared.ai.application.security;

import java.util.Set;

/**
 * Identidad que fija la aplicación. No sale del texto del usuario ni del modelo.
 */
public record SesionAgente(String usuarioId, String rol, Set<String> cursos, Set<String> confirmaciones) {

    public SesionAgente {
        cursos = Set.copyOf(cursos);
        confirmaciones = Set.copyOf(confirmaciones);
    }

    public static SesionAgente profesorDemo() {
        return new SesionAgente("PROF-001", "PROFESOR", Set.of("1ro A"), Set.of());
    }
}
