package com.edusync.shared.ai.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se alcanzo edusync.ai.agente.max-turnos sin que el modelo diera una
 * respuesta final. Failure mode E_LIMITE_TURNOS (PR-IMPL-023).
 */
public class LimiteTurnosAlcanzadoException extends DomainException {
    public LimiteTurnosAlcanzadoException(int maxTurnos) {
        super(
                "E_LIMITE_TURNOS",
                "Limite de %d turnos alcanzado sin respuesta final del agente".formatted(maxTurnos));
    }
}
