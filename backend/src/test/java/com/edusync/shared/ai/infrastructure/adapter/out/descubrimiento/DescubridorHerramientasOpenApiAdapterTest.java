package com.edusync.shared.ai.infrastructure.adapter.out.descubrimiento;

import com.edusync.shared.ai.domain.HerramientaLlm;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Regresion de seguridad + allowlist academica (ADR-0018 / DD-UC-024):
 * el catalogo cubre el sistema (cursos, estudiantes, gestiones, …), no
 * solo usuarios, y excluye auth/plataforma/ai/escrituras/notas individuales.
 */
class DescubridorHerramientasOpenApiAdapterTest {

    private static final Set<String> PREFIJOS_PROHIBIDOS = Set.of(
            "/api/v1/auth/", "/api/v1/plataforma/", "/api/v1/ai/");

    private static final String OPENAPI_FIXTURE = """
            {
              "paths": {
                "/api/v1/estudiantes": {
                  "get": {
                    "operationId": "listarEstudiantes",
                    "summary": "Listar",
                    "parameters": [
                      {"name": "q", "in": "query", "required": false, "schema": {"type": "string"}}
                    ]
                  },
                  "post": {"operationId": "crearEstudiante", "summary": "Alta de estudiante"}
                },
                "/api/v1/cursos": {
                  "get": {
                    "operationId": "listarCursos",
                    "summary": "Lista cursos"
                  }
                },
                "/api/v1/evaluaciones/{id}/calificaciones": {
                  "get": {
                    "operationId": "listarCalificaciones",
                    "summary": "Notas por estudiante"
                  }
                },
                "/api/v1/notassie/alumnos/{id}/promedio": {
                  "get": {
                    "operationId": "consultarPromedioAlumno",
                    "summary": "Consulta el promedio de un alumno"
                  }
                },
                "/api/v1/auth/login": {
                  "post": {"operationId": "login", "summary": "Login"}
                },
                "/api/v1/plataforma/tenants": {
                  "get": {"operationId": "listarTenants", "summary": "Tenants"}
                },
                "/api/v1/ai/chat": {
                  "post": {"operationId": "chat", "summary": "Chat v0"}
                },
                "/api/v1/usuarios": {
                  "put": {"operationId": "reemplazarUsuario", "summary": "Reemplaza un usuario"}
                }
              }
            }
            """;

    @Test
    void catalogoAllowlistCubreAcademicoYExcluyeRiesgos() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8080/v3/api-docs"))
                .andRespond(withSuccess(OPENAPI_FIXTURE, org.springframework.http.MediaType.APPLICATION_JSON));

        DescubridorHerramientasOpenApiAdapter adapter =
                new DescubridorHerramientasOpenApiAdapter(builder, 8080, "/v3/api-docs");

        List<HerramientaLlm> catalogo = adapter.descubrir();

        assertThat(catalogo).extracting(HerramientaLlm::nombre)
                .containsExactlyInAnyOrder("listarEstudiantes", "listarCursos");

        HerramientaLlm estudiantes = catalogo.stream()
                .filter(h -> "listarEstudiantes".equals(h.nombre()))
                .findFirst()
                .orElseThrow();
        assertThat(estudiantes.descripcion()).containsIgnoringCase("estudiante");
        assertThat(estudiantes.descripcion()).doesNotContain("solo usuarios");

        for (HerramientaLlm h : catalogo) {
            assertThat(h.path()).startsWith("/api/v1/");
            for (String prohibido : PREFIJOS_PROHIBIDOS) {
                assertThat(h.path()).doesNotStartWith(prohibido);
            }
            assertThat(h.path().toLowerCase()).doesNotContain("/calificaciones");
            assertThat(DescubridorHerramientasOpenApiAdapter.estaEnAllowlist(h.path())).isTrue();
            if (!"GET".equalsIgnoreCase(h.metodoHttp())) {
                assertThat(h.metodoHttp()).isEqualToIgnoringCase("POST");
                assertThat(h.path().toLowerCase()).matches(".*(consultar|buscar|obtener|listar|search|query).*");
            }
        }
    }
}
