package com.edusync.shared.ai.infrastructure.adapter.out.ejecucion;

import com.edusync.shared.ai.application.port.out.DescubridorHerramientasPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.ParametroHerramienta;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EjecutorHerramientaHttpAdapterTest {

    private static final HerramientaLlm HERRAMIENTA = new HerramientaLlm(
            "obtenerNotasAlumno", "…", "GET", "/api/v1/notassie/alumnos/{id}/notas",
            List.of(new ParametroHerramienta("id", "string", true, ParametroHerramienta.Ubicacion.PATH)));

    @Test
    void propagaElJwtDelUsuario_noUnaCuentaTecnica() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8080/api/v1/notassie/alumnos/42/notas"))
                .andExpect(header("Authorization", "Bearer jwt-del-usuario-real"))
                .andRespond(withSuccess("{\"promedio\":8.5}", MediaType.APPLICATION_JSON));

        DescubridorHerramientasPort descubridor = mock(DescubridorHerramientasPort.class);
        when(descubridor.descubrir()).thenReturn(List.of(HERRAMIENTA));

        EjecutorHerramientaHttpAdapter adapter = new EjecutorHerramientaHttpAdapter(
                builder, new ObjectMapper(), descubridor, 8080);

        String resultado = adapter.ejecutar(
                new LlamadaHerramienta("tc1", "obtenerNotasAlumno", Map.of("id", "42")),
                "jwt-del-usuario-real");

        assertThat(resultado).contains("8.5");
        server.verify();
    }

    @Test
    void errorHttpSeConvierteEnJsonDeError_nuncaLanza() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8080/api/v1/notassie/alumnos/99/notas"))
                .andRespond(withServerError());

        DescubridorHerramientasPort descubridor = mock(DescubridorHerramientasPort.class);
        when(descubridor.descubrir()).thenReturn(List.of(HERRAMIENTA));

        EjecutorHerramientaHttpAdapter adapter = new EjecutorHerramientaHttpAdapter(
                builder, new ObjectMapper(), descubridor, 8080);

        String resultado = adapter.ejecutar(
                new LlamadaHerramienta("tc1", "obtenerNotasAlumno", Map.of("id", "99")),
                "jwt-del-usuario-real");

        assertThat(resultado).contains("error");
    }
}
