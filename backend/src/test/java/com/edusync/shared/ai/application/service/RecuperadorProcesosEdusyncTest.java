package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.ResultadoRecuperacionProceso;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecuperadorProcesosEdusyncTest {

    private final RecuperadorProcesosEdusync recuperador = new RecuperadorProcesosEdusync();

    @Test
    void corpusIncluyeProcesosEdusync() {
        assertThat(recuperador.archivosCargados())
                .contains("calculo_notas.md", "alcance_asistente.md", "exportacion_sie.md");
    }

    @Test
    void preguntaDeProcesoDetectaComoYRude() {
        assertThat(recuperador.esPreguntaDeProceso("Cómo se calculan las notas")).isTrue();
        assertThat(recuperador.esPreguntaDeProceso("qué es el RUDE")).isTrue();
        assertThat(recuperador.esPreguntaDeProceso("lista los cursos")).isFalse();
        assertThat(recuperador.esPreguntaDeProceso("etapas del proceso académico")).isFalse();
    }

    @Test
    void recuperarCalculoCitaRoundYFuente() {
        ResultadoRecuperacionProceso r = recuperador.recuperar("cómo se calculan las notas", 2);
        assertThat(r.hayMatch()).isTrue();
        String texto = recuperador.formatear(r);
        assertThat(texto).containsIgnoringCase("round");
        assertThat(texto).contains("Fuente:");
        assertThat(texto).contains("calculo_notas.md");
    }
}
