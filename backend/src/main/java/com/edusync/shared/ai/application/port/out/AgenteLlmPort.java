package com.edusync.shared.ai.application.port.out;

import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.MensajeAgente;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia el proveedor LLM del camino agente. Distinto
 * de LlmPort (chat v0, ADR-0017): este puerto decide una ACCION
 * (llamar una herramienta o responder), nunca solo redacta texto.
 *
 * La ejecucion interna de tool calling de Spring AI permanece
 * DESACTIVADA en el adaptador de este puerto (ADR-0018): el metodo
 * decidirSiguientePaso SOLO devuelve la eleccion del modelo, nunca
 * ejecuta la herramienta — la ejecucion la hace
 * EjecutorHerramientaPort, orquestada por EjecutarConsultaAgenteService.
 */
public interface AgenteLlmPort {

    /**
     * @param historial turnos previos de la conversacion interna.
     * @param catalogo  herramientas disponibles en este turno.
     * @return la llamada a herramienta elegida por el modelo, o la
     *         respuesta final si el modelo ya tiene suficiente
     *         informacion. Nunca ambas a la vez.
     */
    ResultadoTurno decidirSiguientePaso(List<MensajeAgente> historial, List<HerramientaLlm> catalogo);

    /**
     * Resultado de un turno del bucle ReAct.
     */
    record ResultadoTurno(Optional<LlamadaHerramienta> llamada, Optional<String> respuestaFinal) {

        public ResultadoTurno {
            if (llamada.isPresent() == respuestaFinal.isPresent()) {
                throw new IllegalArgumentException(
                        "ResultadoTurno debe tener exactamente una de llamada/respuestaFinal");
            }
        }

        public static ResultadoTurno deHerramienta(LlamadaHerramienta llamada) {
            return new ResultadoTurno(Optional.of(llamada), Optional.empty());
        }

        public static ResultadoTurno deRespuestaFinal(String texto) {
            return new ResultadoTurno(Optional.empty(), Optional.of(texto));
        }
    }
}
