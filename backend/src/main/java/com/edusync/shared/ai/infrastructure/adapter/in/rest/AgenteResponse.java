package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import com.edusync.shared.ai.domain.RespuestaAgente;
import com.edusync.shared.ai.domain.PasoTrazaAgente;

import java.util.List;

public record AgenteResponse(
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
    public static AgenteResponse desde(RespuestaAgente r) {
        return new AgenteResponse(
                r.respuesta(),
                r.herramientasUsadas(),
                r.turnos(),
                r.camino(),
                r.fuente(),
                r.agente(),
                r.steps(),
                r.confirmacionRequerida(),
                r.contexto());
    }
}
