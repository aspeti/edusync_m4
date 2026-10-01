package com.edusync.shared.ai.security;

import com.edusync.shared.ai.application.security.DecisionHerramienta;
import com.edusync.shared.ai.application.security.PoliticaHerramientasAgente;
import com.edusync.shared.ai.application.security.SesionAgente;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** AI-SEC-004. consultar_academico no acepta un umbral por encima de 100. */
class AiSec004UmbralSobreLimiteSeBloqueaTest {

    private final PoliticaHerramientasAgente politica = PoliticaHerramientasAgente.paraProfesor();
    private final SesionAgente sesion = SesionAgente.profesorDemo();

    @Test
    void umbral999SeBloquea() {
        DecisionHerramienta decision = politica.evaluar(
                "consultar_academico",
                Map.of("operacion", "REPROBADOS", "umbral", 999),
                sesion);

        assertThat(decision.permitido()).isFalse();
        assertThat(decision.motivo()).contains("supera el límite automático");
    }

    @Test
    void umbral100SiguePermitido() {
        DecisionHerramienta decision = politica.evaluar(
                "consultar_academico",
                Map.of("operacion", "REPROBADOS", "umbral", 100),
                sesion);

        assertThat(decision.permitido()).isTrue();
    }
}
