package com.edusync.shared.ai.domain;

/**
 * Se alcanzo edusync.ai.agente.max-turnos sin que el modelo diera una
 * respuesta final. Failure mode E_LIMITE_TURNOS (PR-IMPL-023).
 */
public class LimiteTurnosAlcanzadoException extends RuntimeException {
    public LimiteTurnosAlcanzadoException(int maxTurnos) {
        super("Limite de %d turnos alcanzado sin respuesta final del agente".formatted(maxTurnos));
    }
}
