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
 * Test de regresion de seguridad OBLIGATORIO (ADR-0018 §4.2, PR-IMPL-023
 * failure mode E_TOOL_DE_ESCRITURA_DETECTADA): recorre el catalogo
 * descubierto contra un fixture de /v3/api-docs y verifica que ninguna
 * herramienta apunta a un prefijo excluido ni a un metodo de escritura
 * sin palabra de consulta explicita en el path.
 *
 * El fixture incluye deliberadamente: 1 GET de lectura valido bajo
 * /api/v1/**, 1 bajo /api/v1/auth/**, 1 bajo /api/v1/plataforma/**,
 * 1 bajo /api/v1/ai/**, y 1 PUT de escritura bajo /api/v1/**. Solo el
 * primero debe sobrevivir al filtro.
 */
class DescubridorHerramientasOpenApiAdapterTest {

    private static final Set<String> PREFIJOS_PROHIBIDOS = Set.of(
            "/api/v1/auth/", "/api/v1/plataforma/", "/api/v1/ai/");

    private static final String OPENAPI_FIXTURE = """
            {
              "paths": {
                "/api/v1/notassie/alumnos/{id}/promedio": {
                  "get": {
                    "operationId": "consultarPromedioAlumno",
                    "summary": "Consulta el promedio de un alumno",
                    "parameters": [
                      {"name": "id", "in": "path", "required": true, "schema": {"type": "string"}}
                    ]
                  }
                },
                "/api/v1/auth/login": {
                  "post": {"operationId": "login", "summary": "Login"}
                },
                "/api/v1/plataforma/salud": {
                  "get": {"operationId": "salud", "summary": "Healthcheck"}
                },
                "/api/v1/ai/chat": {
                  "post": {"operationId": "chat", "summary": "Chat v0"}
                },
                "/api/v1/notassie/calificaciones": {
                  "put": {"operationId": "actualizarCalificacion", "summary": "Actualiza una calificacion"}
                }
              }
            }
            """;

    @Test
    void ningunaHerramientaDescubiertaViolaElFiltroDeSeguridad() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8080/v3/api-docs"))
                .andRespond(withSuccess(OPENAPI_FIXTURE, org.springframework.http.MediaType.APPLICATION_JSON));

        DescubridorHerramientasOpenApiAdapter adapter =
                new DescubridorHerramientasOpenApiAdapter(builder, 8080, "/v3/api-docs");

        List<HerramientaLlm> catalogo = adapter.descubrir();

        // Solo la herramienta de lectura legitima debe sobrevivir.
        assertThat(catalogo).hasSize(1);
        assertThat(catalogo.get(0).nombre()).isEqualTo("consultarPromedioAlumno");

        for (HerramientaLlm h : catalogo) {
            assertThat(h.path()).startsWith("/api/v1/");
            for (String prohibido : PREFIJOS_PROHIBIDOS) {
                assertThat(h.path()).doesNotStartWith(prohibido);
            }
            if (!"GET".equalsIgnoreCase(h.metodoHttp())) {
                assertThat(h.metodoHttp()).isEqualToIgnoringCase("POST");
                assertThat(h.path().toLowerCase()).matches(".*(consultar|buscar|obtener|listar|search|query).*");
            }
        }
    }
}
