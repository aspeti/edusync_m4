package com.edusync.shared.ai.application.monitoring;

import com.edusync.shared.ai.domain.RespuestaAgente;

/**
 * Bitácora de una interacción. La implementación por defecto no escribe nada.
 */
public interface RegistroInteraccionAgente {

    void registrar(String jwt, String pregunta, RespuestaAgente respuesta, long latenciaMs);

    RegistroInteraccionAgente NULO = (jwt, pregunta, respuesta, latenciaMs) -> {
    };
}
