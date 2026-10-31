package com.edusync.shared.ai.application.monitoring;

import java.util.Map;

public record ResumenOnline(
        int total,
        Map<String, Integer> porCamino,
        double fidelidadPromedio,
        int respuestasPuntuadas,
        int respuestasNoFieles,
        int frasesProhibidas,
        double feedbackNegativo,
        int calificadas,
        double fueraDeAlcance,
        double latenciaP95Ms,
        Double tokensPromedio,
        int bloqueos,
        Map<String, Integer> usuariosSobreLimite) {

    public ResumenOnline {
        porCamino = Map.copyOf(porCamino);
        usuariosSobreLimite = Map.copyOf(usuariosSobreLimite);
    }
}
