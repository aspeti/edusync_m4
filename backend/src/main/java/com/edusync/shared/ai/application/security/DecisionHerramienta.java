package com.edusync.shared.ai.application.security;

/** Resultado de la política antes de ejecutar una herramienta. */
public record DecisionHerramienta(boolean permitido, String motivo) {

    public static DecisionHerramienta permitir() {
        return new DecisionHerramienta(true, "ok");
    }

    public static DecisionHerramienta bloquear(String motivo) {
        return new DecisionHerramienta(false, motivo);
    }
}
