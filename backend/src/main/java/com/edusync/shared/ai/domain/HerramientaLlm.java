package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Una herramienta de solo lectura descubierta desde GET /v3/api-docs,
 * ya filtrada segun las reglas de seguridad de ADR-0018 seccion 3
 * (identicas a DescubridorTools de la referencia Python
 * EduSync_LLM/edusync-agente-llm/agente/descubridor_tools.py).
 */
public record HerramientaLlm(
        String nombre,
        String descripcion,
        String metodoHttp,
        String path,
        List<ParametroHerramienta> parametros
) {
    public HerramientaLlm {
        parametros = List.copyOf(parametros);
    }
}
