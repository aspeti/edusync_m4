package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Contrato de salida de POST /api/v1/ai/agente. Misma forma que
 * RespuestaAgente en edusync-agente-llm (Python): respuesta,
 * herramientas usadas (solo nombres, nunca argumentos ni resultados —
 * NFR-007), turnos consumidos, camino de resolucion y fuente (el
 * modelo que decidio la respuesta final).
 */
public record RespuestaAgente(
        String respuesta,
        List<String> herramientasUsadas,
        int turnos,
        String camino,
        String fuente
) {
    public RespuestaAgente {
        herramientasUsadas = List.copyOf(herramientasUsadas);
    }

    public static final String CAMINO_AGENTE = "AGENTE";
}
