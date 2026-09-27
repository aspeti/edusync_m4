package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.out.AgenteLlmPort;
import com.edusync.shared.ai.application.port.out.AgenteLlmPort.ResultadoTurno;
import com.edusync.shared.ai.application.port.out.EjecutorHerramientaPort;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.HerramientaNoDisponibleException;
import com.edusync.shared.ai.domain.LimiteTurnosAlcanzadoException;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.RespuestaAgente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EjecutarConsultaAgenteServiceTest {

    @Mock AgenteLlmPort agenteLlmPort;
    @Mock EjecutorHerramientaPort ejecutorHerramientaPort;

    private EjecutarConsultaAgenteService service(int maxTurnos) {
        return service(maxTurnos, true, true);
    }

    private EjecutarConsultaAgenteService service(int maxTurnos, boolean agenteOn, boolean llmOn) {
        CatalogoHerramientasAgente catalogo = new CatalogoHerramientasAgente();
        ObjectMapper mapper = new ObjectMapper();
        FormateadorRespuestaAgente formatter = new FormateadorRespuestaAgente(mapper);
        return new EjecutarConsultaAgenteService(
                agenteLlmPort,
                ejecutorHerramientaPort,
                new EnrutadorPalabrasClaveAgente(catalogo),
                formatter,
                catalogo,
                new AnalizadorIntencionConsultaAcademica(),
                new OrquestadorConsultaAcademica(ejecutorHerramientaPort, catalogo, formatter, mapper),
                maxTurnos,
                "llama3.1:8b",
                true,
                agenteOn,
                llmOn);
    }

    @Test
    void casoFeliz_unaHerramienta_luegoRespuestaFinal() {
        LlamadaHerramienta llamada = new LlamadaHerramienta("tc1", "list_cursos", Map.of());
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deHerramienta(llamada))
                .thenReturn(ResultadoTurno.deRespuestaFinal("texto que se ignora"));
        when(ejecutorHerramientaPort.ejecutar(llamada, "jwt-usuario"))
                .thenReturn("{\"content\":[{\"nombre\":\"1ro A\"}],\"totalElements\":1}");

        RespuestaAgente respuesta = service(6).consultar("etapas del proceso académico", "jwt-usuario");

        assertThat(respuesta.herramientasUsadas()).containsExactly("list_cursos");
        assertThat(respuesta.turnos()).isEqualTo(2);
        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_LLM);
        assertThat(respuesta.respuesta()).contains("Hay 1 cursos");
        assertThat(respuesta.respuesta()).doesNotContain("texto que se ignora");
    }

    @Test
    void keyword_listaLosCursos_noInvocaLlm() {
        when(ejecutorHerramientaPort.ejecutar(any(), eq("jwt-usuario")))
                .thenReturn("{\"content\":[{\"nombre\":\"1ro A\"},{\"nombre\":\"2do B\"}],\"totalElements\":2}");

        RespuestaAgente respuesta = service(6).consultar("¿Cuántos cursos hay?", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_KEYWORD);
        assertThat(respuesta.turnos()).isEqualTo(0);
        assertThat(respuesta.fuente()).isEqualTo(RespuestaAgente.FUENTE_CATALOGO);
        assertThat(respuesta.herramientasUsadas()).containsExactly("list_cursos");
        assertThat(respuesta.respuesta()).contains("Hay 2 cursos");
        assertThat(respuesta.respuesta()).doesNotContain("rude");
        assertThat(respuesta.steps()).hasSize(1);
        assertThat(respuesta.steps().getFirst().tablasFuente()).containsExactly("curso");
        verifyNoInteractions(agenteLlmPort);
    }

    @Test
    void sinonimoSinKeyword_caeAlLlm() {
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deRespuestaFinal("Puedo listar cursos si lo pides así."));

        RespuestaAgente respuesta = service(6).consultar("etapas del proceso académico", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_LLM);
        assertThat(respuesta.turnos()).isEqualTo(1);
    }

    @Test
    void llmApagado_sinKeyword_ninguno() {
        RespuestaAgente respuesta = service(6, true, false).consultar("presupuesto 2027", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_NINGUNO);
        assertThat(respuesta.turnos()).isEqualTo(0);
        verifyNoInteractions(agenteLlmPort, ejecutorHerramientaPort);
    }

    @Test
    void casoBorde_sinHerramientas_respuestaDirecta() {
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deRespuestaFinal("Hola, ¿en qué puedo ayudarte?"));

        RespuestaAgente respuesta = service(6).consultar("hola", "jwt-usuario");

        assertThat(respuesta.turnos()).isEqualTo(1);
        assertThat(respuesta.herramientasUsadas()).isEmpty();
        verifyNoInteractions(ejecutorHerramientaPort);
    }

    @Test
    void casoBorde_limiteDeTurnosAlcanzado() {
        LlamadaHerramienta llamada = new LlamadaHerramienta("tc1", "list_cursos", Map.of());
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deHerramienta(llamada));
        when(ejecutorHerramientaPort.ejecutar(llamada, "jwt-usuario")).thenReturn("{}");

        assertThatThrownBy(() -> service(2).consultar("etapas del proceso académico", "jwt-usuario"))
                .isInstanceOf(LimiteTurnosAlcanzadoException.class);
    }

    @Test
    void casoAdversarial_herramientaNoDeclaradaEnCatalogo_noSeEjecuta() {
        LlamadaHerramienta llamadaInventada = new LlamadaHerramienta("tc1", "borrarAlumno", Map.of("id", "42"));
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deHerramienta(llamadaInventada));

        assertThatThrownBy(() -> service(6).consultar("etapas del proceso académico", "jwt-usuario"))
                .isInstanceOf(HerramientaNoDisponibleException.class);

        verifyNoInteractions(ejecutorHerramientaPort);
    }

    @Test
    void casoBorde_agenteDeshabilitado() {
        EjecutarConsultaAgenteService deshabilitado = service(6, false, true);

        assertThatThrownBy(() -> deshabilitado.consultar("¿cuántos cursos hay?", "jwt-usuario"))
                .isInstanceOf(AiDeshabilitadoException.class);

        verifyNoInteractions(agenteLlmPort, ejecutorHerramientaPort);
    }

    @Test
    void escrituraSinConfirmar_noLlamaAlEjecutor() {
        RespuestaAgente respuesta = service(6).consultar("crea el curso 1ro A", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_KEYWORD);
        assertThat(respuesta.confirmacionRequerida()).isTrue();
        assertThat(respuesta.respuesta()).contains("1ro a");
        assertThat(respuesta.herramientasUsadas()).containsExactly("create_curso");
        verifyNoInteractions(ejecutorHerramientaPort, agenteLlmPort);
    }

    @Test
    void escrituraConfirmada_ejecutaPost() {
        when(ejecutorHerramientaPort.ejecutar(any(), eq("jwt-usuario")))
                .thenReturn("{\"nombre\":\"1ro A\"}");

        RespuestaAgente respuesta = service(6).consultar("crea el curso 1ro A", "jwt-usuario", true);

        assertThat(respuesta.confirmacionRequerida()).isFalse();
        assertThat(respuesta.respuesta()).contains("Listo");
        assertThat(respuesta.respuesta()).contains("1ro A");
        org.mockito.Mockito.verify(ejecutorHerramientaPort).ejecutar(any(), eq("jwt-usuario"));
    }

    @Test
    void consultaNotasJuanNoInvocaLlm() {
        when(ejecutorHerramientaPort.ejecutar(any(), eq("jwt-usuario"))).thenAnswer(inv -> {
            LlamadaHerramienta llamada = inv.getArgument(0);
            return switch (llamada.nombreHerramienta()) {
                case "find_estudiante" ->
                        "{\"estado\":\"UNICO\",\"matches\":[{\"id\":\"11111111-1111-1111-1111-111111111111\",\"etiqueta\":\"Juan\"}]}";
                case "find_periodo" ->
                        "{\"estado\":\"UNICO\",\"matches\":[{\"id\":\"22222222-2222-2222-2222-222222222222\",\"etiqueta\":\"Trimestre 1\"}]}";
                case "consultar_academico" ->
                        "{\"estado\":\"OK\",\"operacion\":\"NOTAS\",\"filas\":[{\"etiqueta\":\"Juan\",\"valor\":80,\"detalle\":\"Matematica\"}]}";
                default -> "{}";
            };
        });

        RespuestaAgente respuesta = service(6).consultar("Notas de Juan del primer trimestre.", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_CONSULTA);
        assertThat(respuesta.turnos()).isEqualTo(0);
        assertThat(respuesta.herramientasUsadas()).contains("find_estudiante", "find_periodo", "consultar_academico");
        assertThat(respuesta.respuesta()).contains("Juan");
        verifyNoInteractions(agenteLlmPort);
    }

    @Test
    void consultaAmbiguoPideAclaracionSinElegir() {
        when(ejecutorHerramientaPort.ejecutar(any(), eq("jwt-usuario")))
                .thenReturn("{\"estado\":\"AMBIGUO\",\"matches\":[{\"etiqueta\":\"Juan Perez — 1ro A\"},{\"etiqueta\":\"Juan Ramirez — 2do B\"}]}");

        RespuestaAgente respuesta = service(6).consultar("Notas de Juan del primer trimestre.", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_CONSULTA);
        assertThat(respuesta.respuesta()).contains("varias coincidencias");
        assertThat(respuesta.respuesta()).contains("1ro A");
        assertThat(respuesta.respuesta()).contains("2do B");
        verifyNoInteractions(agenteLlmPort);
    }

    @Test
    void keywordFindProfesorExtraeQ() {
        when(ejecutorHerramientaPort.ejecutar(any(), eq("jwt-usuario")))
                .thenReturn("{\"estado\":\"UNICO\",\"tipo\":\"PROFESOR\",\"matches\":[{\"id\":\"a\",\"etiqueta\":\"Fabian\"}]}");

        RespuestaAgente respuesta = service(6).consultar("existe algun profesor con nombre fabian?", "jwt-usuario");

        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_KEYWORD);
        assertThat(respuesta.herramientasUsadas()).containsExactly("find_profesor");
        assertThat(respuesta.respuesta()).contains("Fabian");
        org.mockito.Mockito.verify(ejecutorHerramientaPort)
                .ejecutar(org.mockito.ArgumentMatchers.argThat(l ->
                        "find_profesor".equals(l.nombreHerramienta())
                                && "fabian".equals(String.valueOf(l.argumentos().get("q")))),
                        eq("jwt-usuario"));
    }
}
