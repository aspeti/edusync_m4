package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("agente")
public class AnalizadorIntencionConsultaAcademicaAgenteTest {

    private final AnalizadorIntencionConsultaAcademica analizador = new AnalizadorIntencionConsultaAcademica();

    @Test
    @Tag("agente")
    void caso1_cadenaVacia() {
        var i = analizador.analizar("", null);
        assertThat(i).isEmpty();
    }

    @Test
    @Tag("agente")
    void caso2_cadenaHola() {
        var i = analizador.analizar("hola", null);
        assertThat(i).isEmpty();
    }

    @Test
    @Tag("agente")
    void caso3_promedioMasAlto() {
        var i = analizador.analizar("quien tuvo el promedio mas alto de 1ro A", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.TOP);
        assertThat(i.cursoParalelo()).contains("1ro a");
    }

    @Test
    @Tag("agente")
    void caso4_buscarProfesor() {
        var i = analizador.analizar("existe un profesor llamado Silvia", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.BUSCAR_PROFESOR);
        assertThat(i.profesor()).isEqualTo("silvia");
    }

    @Test
    @Tag("agente")
    void caso5_buscarEstudiante() {
        var i = analizador.analizar("existe un estudiante llamado Juan", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.BUSCAR_ESTUDIANTE);
        assertThat(i.estudiante()).isEqualTo("juan");
    }

    @Test
    @Tag("agente")
    void caso6_materiasProfesor() {
        var i = analizador.analizar("que materias tiene asignada la profesora Silvia?", null).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.MATERIAS_ESTUDIANTE);
        assertThat(i.estudiante()).isEqualTo("silvia");
    }

    @Test
    @Tag("agente")
    void caso7_followUpPeriodo() {
        var ctx = ContextoConsultaAgente.vacio().conOperacion("NOTAS");
        var i = analizador.analizar("Y en el segundo trimestre?", ctx).orElseThrow();
        assertThat(i.operacion()).isEqualTo(IntencionConsultaAcademica.Operacion.NOTAS);
        assertThat(i.periodo()).contains("segundo trimestre");
    }
}
