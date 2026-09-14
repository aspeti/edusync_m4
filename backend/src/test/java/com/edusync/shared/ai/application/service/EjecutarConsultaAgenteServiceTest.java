package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.out.AgenteLlmPort;
import com.edusync.shared.ai.application.port.out.AgenteLlmPort.ResultadoTurno;
import com.edusync.shared.ai.application.port.out.DescubridorHerramientasPort;
import com.edusync.shared.ai.application.port.out.EjecutorHerramientaPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.HerramientaNoDisponibleException;
import com.edusync.shared.ai.domain.LimiteTurnosAlcanzadoException;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.RespuestaAgente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas del bucle ReAct. Ver PR-IMPL-023 §6 (caso feliz / borde /
 * adversarial) — estos tests cubren exactamente esos tres casos.
 */
@ExtendWith(MockitoExtension.class)
class EjecutarConsultaAgenteServiceTest {

    @Mock AgenteLlmPort agenteLlmPort;
    @Mock DescubridorHerramientasPort descubridorHerramientasPort;
    @Mock EjecutorHerramientaPort ejecutorHerramientaPort;

    private static final HerramientaLlm HERRAMIENTA = new HerramientaLlm(
            "obtenerNotasAlumno", "Obtiene notas de un alumno por id", "GET",
            "/api/v1/notassie/alumnos/{id}/notas", List.of());

    private EjecutarConsultaAgenteService service(int maxTurnos) {
        return new EjecutarConsultaAgenteService(
                agenteLlmPort, descubridorHerramientasPort, ejecutorHerramientaPort,
                maxTurnos, "llama3.1:8b");
    }

    @Test
    void casoFeliz_unaHerramienta_luegoRespuestaFinal() {
        when(descubridorHerramientasPort.descubrir()).thenReturn(List.of(HERRAMIENTA));
        LlamadaHerramienta llamada = new LlamadaHerramienta("tc1", "obtenerNotasAlumno", Map.of("id", "42"));
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deHerramienta(llamada))
                .thenReturn(ResultadoTurno.deRespuestaFinal("El promedio es 8.5"));
        when(ejecutorHerramientaPort.ejecutar(llamada, "jwt-usuario")).thenReturn("{\"promedio\":8.5}");

        RespuestaAgente respuesta = service(6).consultar("¿cuál es el promedio del alumno 42?", "jwt-usuario");

        assertThat(respuesta.respuesta()).isEqualTo("El promedio es 8.5");
        assertThat(respuesta.herramientasUsadas()).containsExactly("obtenerNotasAlumno");
        assertThat(respuesta.turnos()).isEqualTo(2);
        assertThat(respuesta.camino()).isEqualTo(RespuestaAgente.CAMINO_AGENTE);
        assertThat(respuesta.fuente()).isEqualTo("llama3.1:8b");
    }

    @Test
    void casoBorde_sinHerramientas_respuestaDirecta() {
        when(descubridorHerramientasPort.descubrir()).thenReturn(List.of());
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deRespuestaFinal("Hola, ¿en qué puedo ayudarte?"));

        RespuestaAgente respuesta = service(6).consultar("hola", "jwt-usuario");

        assertThat(respuesta.turnos()).isEqualTo(1);
        assertThat(respuesta.herramientasUsadas()).isEmpty();
        verifyNoInteractions(ejecutorHerramientaPort);
    }

    @Test
    void casoBorde_limiteDeTurnosAlcanzado() {
        when(descubridorHerramientasPort.descubrir()).thenReturn(List.of(HERRAMIENTA));
        LlamadaHerramienta llamada = new LlamadaHerramienta("tc1", "obtenerNotasAlumno", Map.of("id", "42"));
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deHerramienta(llamada));
        when(ejecutorHerramientaPort.ejecutar(llamada, "jwt-usuario")).thenReturn("{}");

        assertThatThrownBy(() -> service(2).consultar("pregunta que nunca se resuelve", "jwt-usuario"))
                .isInstanceOf(LimiteTurnosAlcanzadoException.class);
    }

    @Test
    void casoAdversarial_herramientaNoDeclaradaEnCatalogo_noSeEjecuta() {
        when(descubridorHerramientasPort.descubrir()).thenReturn(List.of(HERRAMIENTA));
        LlamadaHerramienta llamadaInventada = new LlamadaHerramienta("tc1", "borrarAlumno", Map.of("id", "42"));
        when(agenteLlmPort.decidirSiguientePaso(anyList(), anyList()))
                .thenReturn(ResultadoTurno.deHerramienta(llamadaInventada));

        assertThatThrownBy(() -> service(6).consultar("borra al alumno 42", "jwt-usuario"))
                .isInstanceOf(HerramientaNoDisponibleException.class);

        verifyNoInteractions(ejecutorHerramientaPort);
    }
}
