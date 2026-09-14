package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import com.edusync.shared.ai.domain.RespuestaAgente;

import java.util.List;

public record AgenteResponse(
        String respuesta,
        List<String> herramientasUsadas,
        int turnos,
        String camino,
        String fuente
) {
    public static AgenteResponse desde(RespuestaAgente r) {
        return new AgenteResponse(r.respuesta(), r.herramientasUsadas(), r.turnos(), r.camino(), r.fuente());
    }
}
