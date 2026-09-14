package com.edusync.shared.ai.domain;

import java.util.Map;

/**
 * La eleccion del modelo en un turno del bucle ReAct: que herramienta
 * invocar y con que argumentos. "id" es el identificador de tool-call
 * que el proveedor LLM usa para enlazar la observacion en el turno
 * siguiente (equivalente a ChatResponse.getToolCalls() en Spring AI).
 */
public record LlamadaHerramienta(
        String id,
        String nombreHerramienta,
        Map<String, Object> argumentos
) {
    public LlamadaHerramienta {
        argumentos = Map.copyOf(argumentos);
    }
}
