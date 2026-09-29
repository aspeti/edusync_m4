package com.edusync.shared.ai.domain;

/**
 * Decisión del nodo GUARDRAIL_SALIDA (ADR-0021). Entrega siempre un texto
 * seguro; {@code ok=false} marca el paso de traza como fallido.
 */
public record ResultadoGuardrailSalida(boolean ok, String textoEntregable, String problema) {

    public ResultadoGuardrailSalida {
        textoEntregable = textoEntregable == null ? "" : textoEntregable;
        problema = problema == null ? "" : problema;
    }

    public static ResultadoGuardrailSalida ok(String texto) {
        return new ResultadoGuardrailSalida(true, texto, "");
    }

    public static ResultadoGuardrailSalida redactado(String texto, String problema) {
        return new ResultadoGuardrailSalida(false, texto, problema);
    }
}
