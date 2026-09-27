package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Contrato de salida de POST /api/v1/ai/agente.
 * {@code camino}: KEYWORD | CONSULTA | LLM | NINGUNO.
 */
public record RespuestaAgente(
        String respuesta,
        List<String> herramientasUsadas,
        int turnos,
        String camino,
        String fuente,
        String agente,
        List<PasoTrazaAgente> steps,
        boolean confirmacionRequerida,
        ContextoConsultaAgente contexto
) {
    public RespuestaAgente {
        herramientasUsadas = List.copyOf(herramientasUsadas);
        agente = agente == null || agente.isBlank() ? AGENTE_GENERAL : agente;
        steps = steps == null ? List.of() : List.copyOf(steps);
    }

    /** Alias historico: el camino ReAct ahora se reporta como {@link #CAMINO_LLM}. */
    public static final String CAMINO_AGENTE = "LLM";
    public static final String CAMINO_KEYWORD = "KEYWORD";
    public static final String CAMINO_CONSULTA = "CONSULTA";
    public static final String CAMINO_LLM = "LLM";
    public static final String CAMINO_NINGUNO = "NINGUNO";
    public static final String AGENTE_GENERAL = "general";
    public static final String FUENTE_CATALOGO = "catalogo";
    public static final String FUENTE_NINGUNO = "ninguno";

    public RespuestaAgente(
            String respuesta,
            List<String> herramientasUsadas,
            int turnos,
            String camino,
            String fuente) {
        this(respuesta, herramientasUsadas, turnos, camino, fuente, AGENTE_GENERAL, List.of(), false, null);
    }

    public RespuestaAgente(
            String respuesta,
            List<String> herramientasUsadas,
            int turnos,
            String camino,
            String fuente,
            String agente,
            List<PasoTrazaAgente> steps) {
        this(respuesta, herramientasUsadas, turnos, camino, fuente, agente, steps, false, null);
    }

    public RespuestaAgente(
            String respuesta,
            List<String> herramientasUsadas,
            int turnos,
            String camino,
            String fuente,
            String agente,
            List<PasoTrazaAgente> steps,
            boolean confirmacionRequerida) {
        this(respuesta, herramientasUsadas, turnos, camino, fuente, agente, steps, confirmacionRequerida, null);
    }
}
