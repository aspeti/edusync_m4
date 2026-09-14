package com.edusync.shared.ai.application.port.in;

import com.edusync.shared.ai.domain.RespuestaAgente;

/**
 * Puerto de entrada del agente de tool calling multipaso.
 * Ver ADR-0018 (Alternativa C) y DD-UC-023 seccion 2.
 */
public interface EjecutarConsultaAgenteUseCase {

    /**
     * @param pregunta   pregunta en lenguaje natural del usuario.
     * @param jwtUsuario JWT del usuario autenticado que hizo la pregunta;
     *                   se propaga tal cual a cada herramienta ejecutada
     *                   (ADR-0018 seccion 3: "Identidad de ejecucion").
     *                   NUNCA se sustituye por una cuenta tecnica.
     */
    RespuestaAgente consultar(String pregunta, String jwtUsuario);
}
