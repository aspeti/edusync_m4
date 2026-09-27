package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.DefinicionHerramientaAgente;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class FormateadorRespuestaAgenteTest {

    private final FormateadorRespuestaAgente formateador = new FormateadorRespuestaAgente(new ObjectMapper());
    private final DefinicionHerramientaAgente cursos = new CatalogoHerramientasAgente().porId("list_cursos").orElseThrow();

    @Test
    void paginaConNombres() {
        String texto = formateador.formatear(cursos,
                "{\"content\":[{\"nombre\":\"1ro A\",\"rude\":\"999\"}],\"totalElements\":1}");
        assertThat(texto).contains("Hay 1 cursos");
        assertThat(texto).contains("1ro A");
        assertThat(texto).doesNotContain("999");
        assertThat(texto).doesNotContain("{");
    }

    @Test
    void errorHttpNoExponeJson() {
        String texto = formateador.formatear(cursos, "{\"error\":\"Forbidden\"}");
        assertThat(texto).contains("No pude consultar cursos");
        assertThat(texto).doesNotContain("Forbidden");
    }

    @Test
    void vacio() {
        String texto = formateador.formatear(cursos, "{\"content\":[],\"totalElements\":0}");
        assertThat(texto).contains("No hay cursos");
    }

    @Test
    void objetoUnicoGestionActiva() {
        DefinicionHerramientaAgente activa = new CatalogoHerramientasAgente()
                .porId("get_gestion_activa").orElseThrow();
        String texto = formateador.formatear(activa,
                "{\"nombre\":\"Gestion 2026\",\"estado\":\"ACTIVA\"}");
        assertThat(texto).contains("gestion activa");
        assertThat(texto).contains("Gestion 2026 (ACTIVA)");
        assertThat(texto).doesNotContain("{");
    }

    @Test
    void listaDePeriodosSinPaginacion() {
        DefinicionHerramientaAgente periodos = new CatalogoHerramientasAgente()
                .porId("list_periodos_gestion_activa").orElseThrow();
        String texto = formateador.formatear(periodos,
                "[{\"nombre\":\"Trimestre 1\",\"estado\":\"ABIERTO\"},{\"nombre\":\"Trimestre 2\",\"estado\":\"PENDIENTE\"}]");
        assertThat(texto).contains("Hay 2 periodos");
        assertThat(texto).contains("Trimestre 1 (ABIERTO)");
        assertThat(texto).doesNotContain("{");
    }

    @Test
    void catalogoSinPathsProhibidos() {
        CatalogoHerramientasAgente catalogo = new CatalogoHerramientasAgente();
        assertThat(catalogo.lecturas())
                .allSatisfy(d -> assertThat(CatalogoHerramientasAgente.pathProhibido(d.path())).isFalse());
        assertThat(CatalogoHerramientasAgente.pathProhibido("/api/v1/evaluaciones/1/calificaciones")).isTrue();
        assertThat(CatalogoHerramientasAgente.pathProhibido("/api/v1/auth/login")).isTrue();
    }

    @Test
    void ambiguoPideAclaracion() {
        DefinicionHerramientaAgente find = new CatalogoHerramientasAgente().porId("find_estudiante").orElseThrow();
        String texto = formateador.formatear(find,
                "{\"estado\":\"AMBIGUO\",\"matches\":[{\"etiqueta\":\"Juan Perez — 1ro A\"},{\"etiqueta\":\"Juan Ramirez — 2do B\"}]}");
        assertThat(texto).contains("varias coincidencias");
        assertThat(texto).contains("1ro A");
        assertThat(texto).contains("2do B");
        assertThat(texto).doesNotContain("{");
    }

    @Test
    void previewEscrituraPideConfirmacion() {
        DefinicionHerramientaAgente crear = new CatalogoHerramientasAgente().porId("create_curso").orElseThrow();
        String texto = formateador.previewEscritura(crear, "1ro A");
        assertThat(texto).contains("1ro A");
        assertThat(texto).contains("Confirmar");
        assertThat(texto).doesNotContain("{");
    }
}
