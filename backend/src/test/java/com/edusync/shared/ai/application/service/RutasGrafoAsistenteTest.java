package com.edusync.shared.ai.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RutasGrafoAsistenteTest {

    @Test
    void entradaFalsaSiempreBloquea() {
        assertThat(RutasGrafoAsistente.siguiente(false, true, true, true, true, true))
                .isEqualTo(RutasGrafoAsistente.Nodo.BLOQUEAR);
    }

    @Test
    void keywordGanaAConsultaYSaludo() {
        assertThat(RutasGrafoAsistente.siguiente(true, true, true, true, true, true))
                .isEqualTo(RutasGrafoAsistente.Nodo.KEYWORD);
    }

    @Test
    void consultaGanaASaludo() {
        assertThat(RutasGrafoAsistente.siguiente(true, false, true, true, true, true))
                .isEqualTo(RutasGrafoAsistente.Nodo.CONSULTA);
    }

    @Test
    void procesoGanaASaludo() {
        assertThat(RutasGrafoAsistente.siguiente(true, false, false, true, true, true))
                .isEqualTo(RutasGrafoAsistente.Nodo.PROCESO);
    }

    @Test
    void saludoSiNoHayKeywordNiConsultaNiProceso() {
        assertThat(RutasGrafoAsistente.siguiente(true, false, false, false, true, true))
                .isEqualTo(RutasGrafoAsistente.Nodo.SALUDO);
    }

    @Test
    void reactONingunoSegunLlm() {
        assertThat(RutasGrafoAsistente.siguiente(true, false, false, false, false, true))
                .isEqualTo(RutasGrafoAsistente.Nodo.REACT);
        assertThat(RutasGrafoAsistente.siguiente(true, false, false, false, false, false))
                .isEqualTo(RutasGrafoAsistente.Nodo.NINGUNO);
    }

    @Test
    void detectaSaludoConAcento() {
        assertThat(RutasGrafoAsistente.esSaludo("Buenos días")).isTrue();
        assertThat(RutasGrafoAsistente.esSaludo("lista los cursos")).isFalse();
    }
}
