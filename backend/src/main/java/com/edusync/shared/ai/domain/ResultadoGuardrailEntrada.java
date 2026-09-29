package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Decisión del nodo GUARDRAIL_ENTRADA (ADR-0021). No es una excepción:
 * el grafo sigue por arista {@code ok=false} → BLOQUEADO.
 *
 * <p>{@code hallazgos} son etiquetas ({@code inyeccion}, {@code rude}, …), nunca
 * el valor detectado (NFR-007).
 */
public record ResultadoGuardrailEntrada(
        boolean ok,
        String preguntaLimpia,
        List<String> hallazgos,
        String motivo
) {
    public ResultadoGuardrailEntrada {
        hallazgos = hallazgos == null ? List.of() : List.copyOf(hallazgos);
        preguntaLimpia = preguntaLimpia == null ? "" : preguntaLimpia;
        motivo = motivo == null ? "" : motivo;
    }

    public static ResultadoGuardrailEntrada permitir(String preguntaLimpia, List<String> hallazgos) {
        return new ResultadoGuardrailEntrada(true, preguntaLimpia, hallazgos, "");
    }

    public static ResultadoGuardrailEntrada bloquear(String motivo, List<String> hallazgos) {
        return new ResultadoGuardrailEntrada(false, "", hallazgos, motivo);
    }
}
