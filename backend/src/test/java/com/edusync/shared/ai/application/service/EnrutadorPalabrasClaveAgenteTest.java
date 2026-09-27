package com.edusync.shared.ai.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrutadorPalabrasClaveAgenteTest {

    private final EnrutadorPalabrasClaveAgente enrutador =
            new EnrutadorPalabrasClaveAgente(new CatalogoHerramientasAgente());

    @Test
    void matchConTildesYSignos() {
        assertThat(enrutador.resolver("¿Cuántos cursos hay?"))
                .isPresent()
                .get()
                .extracting("toolId")
                .isEqualTo("list_cursos");
    }

    @Test
    void estudiantesNoConfundeConUsuarios() {
        assertThat(enrutador.resolver("lista los estudiantes"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_estudiantes");
        assertThat(enrutador.resolver("lista los usuarios"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_usuarios");
    }

    @Test
    void gestionActivaNoUsaElListado() {
        assertThat(enrutador.resolver("¿Cuál es la gestión activa?"))
                .get()
                .extracting("toolId")
                .isEqualTo("get_gestion_activa");
        assertThat(enrutador.resolver("lista las gestiones"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_gestiones_escolares");
    }

    @Test
    void periodosSeccionesYMisMaterias() {
        assertThat(enrutador.resolver("lista los periodos"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_periodos_gestion_activa");
        assertThat(enrutador.resolver("lista las secciones"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_secciones_gestion_activa");
        assertThat(enrutador.resolver("mis materias"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_mis_materias");
        assertThat(enrutador.resolver("lista las materias"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_materias");
    }

    @Test
    void crearCursoExtraeNombreYNoConfundeConListar() {
        assertThat(enrutador.resolver("crea el curso 1ro A"))
                .get()
                .extracting("toolId")
                .isEqualTo("create_curso");
        assertThat(enrutador.resolverConArgumentos("crea el curso 1ro A"))
                .isPresent()
                .hasValueSatisfying(m -> assertThat(m.argumentos()).containsEntry("nombre", "1ro a"));
        assertThat(enrutador.resolver("lista los cursos"))
                .get()
                .extracting("toolId")
                .isEqualTo("list_cursos");
    }

    @Test
    void existeProfesorExtraeNombre() {
        assertThat(enrutador.resolver("existe algun profesor con nombre fabian?"))
                .get()
                .extracting("toolId")
                .isEqualTo("find_profesor");
        assertThat(enrutador.resolverConArgumentos("existe algun profesor con nombre fabian?"))
                .isPresent()
                .hasValueSatisfying(m -> assertThat(m.argumentos()).containsEntry("q", "fabian"));
    }
}
