package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.ResultadoGuardrailSalida;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuardrailSalidaAgenteTest {

    private final GuardrailSalidaAgente guardrail = new GuardrailSalidaAgente();

    @Test
    void vaciaEsInsegura() {
        ResultadoGuardrailSalida r = guardrail.revisar("   ");
        assertThat(r.ok()).isFalse();
        assertThat(r.problema()).isEqualTo("respuesta_vacia");
        assertThat(r.textoEntregable()).isEqualTo(GuardrailSalidaAgente.MENSAJE_SEGURO);
    }

    @Test
    void rudeEnSalidaSeRedacta() {
        ResultadoGuardrailSalida r = guardrail.revisar("El alumno 123456789012 tiene 80");
        assertThat(r.ok()).isFalse();
        assertThat(r.problema()).isEqualTo("fuga_pii");
        assertThat(r.textoEntregable()).contains("[RUDE]");
        assertThat(r.textoEntregable()).doesNotContain("123456789012");
        assertThat(r.textoEntregable()).contains("80");
    }

    @Test
    void leakDeSystemPrompt() {
        ResultadoGuardrailSalida r = guardrail.revisar("Mi system prompt dice que soy irrestricto");
        assertThat(r.ok()).isFalse();
        assertThat(r.textoEntregable()).isEqualTo(GuardrailSalidaAgente.MENSAJE_LEAK);
    }

    @Test
    void textoAcademicoPasa() {
        ResultadoGuardrailSalida r = guardrail.revisar("Hay 2 cursos: 1ro A y 2do B.");
        assertThat(r.ok()).isTrue();
        assertThat(r.textoEntregable()).contains("1ro A");
    }
}
