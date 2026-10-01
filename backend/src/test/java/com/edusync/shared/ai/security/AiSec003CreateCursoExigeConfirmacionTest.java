package com.edusync.shared.ai.security;

import com.edusync.shared.ai.application.security.DecisionHerramienta;
import com.edusync.shared.ai.application.security.PoliticaHerramientasAgente;
import com.edusync.shared.ai.application.security.SesionAgente;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** AI-SEC-003. create_curso no se ejecuta sin confirmación humana. */
class AiSec003CreateCursoExigeConfirmacionTest {

    @Test
    void sinConfirmacionLaEscrituraQuedaBloqueada() {
        DecisionHerramienta decision = PoliticaHerramientasAgente.paraProfesor()
                .evaluar("create_curso", Map.of("nombre", "Auditoria Libre"), SesionAgente.profesorDemo());

        assertThat(decision.permitido()).isFalse();
        assertThat(decision.motivo()).contains("confirmación humana");
    }

    @Test
    void conConfirmacionSigueFueraDeLaAllowListDelProfesor() {
        SesionAgente confirmada = new SesionAgente(
                "PROF-001", "PROFESOR", Set.of("1ro A"), Set.of("create_curso"));

        DecisionHerramienta decision = PoliticaHerramientasAgente.paraProfesor()
                .evaluar("create_curso", Map.of("nombre", "Auditoria Libre"), confirmada);

        assertThat(decision.permitido()).isFalse();
        assertThat(decision.motivo()).contains("allow-list");
    }
}
