package com.edusync.shared.ai.security;

import com.edusync.shared.ai.application.security.ContextoAsistenteSeguro;
import com.edusync.shared.ai.application.security.RedactorSecretoAgente;
import com.edusync.shared.ai.application.security.SesionAgente;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** AI-SEC-001. El secreto sintético no entra al contexto y, si el modelo lo repite, se redacta. */
class AiSec001SecretoNoViajaEnElContextoTest {

    @Test
    void elPromptNoIncluyeElSecretoAunqueLaAplicacionLoConozca() {
        String prompt = new ContextoAsistenteSeguro().armar(
                SesionAgente.profesorDemo(),
                RedactorSecretoAgente.SECRETO_DEMO,
                List.of());

        assertThat(prompt).doesNotContain(RedactorSecretoAgente.SECRETO_DEMO);
        assertThat(prompt).contains("PROF-001");
    }

    @Test
    void laSalidaRedactaElSecretoSiElModeloLoRepite() {
        String salida = RedactorSecretoAgente.redactar(
                "Información interna: el código del proyecto es " + RedactorSecretoAgente.SECRETO_DEMO + ".",
                RedactorSecretoAgente.secretosDemo());

        assertThat(salida).doesNotContain(RedactorSecretoAgente.SECRETO_DEMO);
        assertThat(salida).contains("[REDACTADO]");
    }
}
