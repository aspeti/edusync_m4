package com.edusync.shared.ai.security;

import com.edusync.shared.ai.application.security.ContextoAsistenteSeguro;
import com.edusync.shared.ai.application.security.FichaVisible;
import com.edusync.shared.ai.application.security.RedactorSecretoAgente;
import com.edusync.shared.ai.application.security.SesionAgente;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** AI-SEC-005. La ficha de otro curso no entra al contexto del profesor autenticado. */
class AiSec005FichaAjenaNoEstaEnElContextoTest {

    @Test
    void est003NoApareceYEst001Si() {
        FichaVisible propia = new FichaVisible("EST-001", "luis.demo@ejemplo.test", "FICHA-EST-001", "1ro A");
        FichaVisible ajena = new FichaVisible("EST-003", "carla.prueba@ejemplo.test", "FICHA-EST-003", "2do B");

        String contexto = new ContextoAsistenteSeguro().armar(
                SesionAgente.profesorDemo(),
                RedactorSecretoAgente.SECRETO_DEMO,
                List.of(propia, ajena));

        assertThat(contexto).contains("luis.demo@ejemplo.test");
        assertThat(contexto).doesNotContain("carla.prueba@ejemplo.test");
        assertThat(contexto).doesNotContain("FICHA-EST-003");
    }
}
