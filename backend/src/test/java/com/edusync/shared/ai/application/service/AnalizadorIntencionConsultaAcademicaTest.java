package com.edusync.shared.ai.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalizadorIntencionConsultaAcademicaTest {

    private final AnalizadorIntencionConsultaAcademica analizador = new AnalizadorIntencionConsultaAcademica();

    @Test
    void caso1_notasJuanPrimerTrimestre() {
        var i = analizador.analizar("Notas de Juan del primer trimestre.", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.NOTAS);
        assertThat(i.estudiante()).isEqualTo("juan");
        assertThat(i.periodo()).contains("primer trimestre");
        assertThat(i.materia()).isNull();
    }

    @Test
    void caso2_notasJuanMatematica() {
        var i = analizador.analizar("Notas de Juan en Matemática.", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.NOTAS);
        assertThat(i.estudiante()).isEqualTo("juan");
        assertThat(i.materia()).isEqualTo("matematica");
    }

    @Test
    void caso3_promedio1roASegundoTrimestre() {
        var i = analizador.analizar("¿Cuál es el promedio de 1ro A durante el segundo trimestre?", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.PROMEDIO);
        assertThat(i.cursoParalelo()).isEqualTo("1ro a");
        assertThat(i.periodo()).contains("segundo trimestre");
    }

    @Test
    void caso4_reprobaronMatematica() {
        var i = analizador.analizar("¿Qué alumnos reprobaron Matemática?", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.REPROBADOS);
        assertThat(i.materia()).isEqualTo("matematica");
    }

    @Test
    void caso5_nominaYMaterias() {
        var nomina = analizador.analizar("¿Qué alumnos están en 1ro A?", null).orElseThrow();
        assertThat(nomina.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.NOMINA);
        assertThat(nomina.cursoParalelo()).isEqualTo("1ro a");
        var materias = analizador.analizar("¿Qué materias tiene Juan?", null).orElseThrow();
        assertThat(materias.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.MATERIAS_ESTUDIANTE);
        assertThat(materias.estudiante()).isEqualTo("juan");
    }

    @Test
    void caso6_followUpPeriodoUsaContexto() {
        var ctx = com.edusync.shared.ai.domain.ContextoConsultaAgente.vacio().conOperacion("NOTAS");
        var i = analizador.analizar("Del primer trimestre.", ctx).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.NOTAS);
        assertThat(i.periodo()).contains("primer trimestre");
    }

    @Test
    void caso7_followUpMateriaUsaContexto() {
        var ctx = com.edusync.shared.ai.domain.ContextoConsultaAgente.vacio().conOperacion("NOTAS");
        var i = analizador.analizar("¿Y en Matemática?", ctx).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.NOTAS);
        assertThat(i.materia()).isEqualTo("matematica");
        assertThat(i.periodo()).isNull();
    }
}
