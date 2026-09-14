package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.in.EjecutarConsultaAgenteUseCase;
import com.edusync.shared.ai.application.port.out.AgenteLlmPort;
import com.edusync.shared.ai.application.port.out.AgenteLlmPort.ResultadoTurno;
import com.edusync.shared.ai.application.port.out.DescubridorHerramientasPort;
import com.edusync.shared.ai.application.port.out.EjecutorHerramientaPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.HerramientaNoDisponibleException;
import com.edusync.shared.ai.domain.LimiteTurnosAlcanzadoException;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.MensajeAgente;
import com.edusync.shared.ai.domain.RespuestaAgente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bucle ReAct del agente de tool calling. Controla los turnos a mano
 * (la ejecucion interna de tool calling de Spring AI permanece
 * desactivada en AgenteLlmAdapter) para poder reportar
 * herramientasUsadas/turnos/camino/fuente con exactitud, igual que
 * hace edusync-agente-llm (Python) — ver ADR-0018 y DD-UC-023.
 *
 * NO reemplaza ni reutiliza ChatConLlmService: es un caso de uso
 * completamente nuevo y aditivo dentro de shared.ai. Deliberadamente
 * NO depende de AiProperties (no se pudo leer su forma real en la
 * generacion de este slice — ver notas de entrega de PR-IMPL-023);
 * usa @Value directo sobre las mismas claves edusync.ai.agente.* para
 * no arriesgar tocar esa clase existente.
 */
@Service
public class EjecutarConsultaAgenteService implements EjecutarConsultaAgenteUseCase {

    private static final Logger log = LoggerFactory.getLogger(EjecutarConsultaAgenteService.class);

    private final AgenteLlmPort agenteLlmPort;
    private final DescubridorHerramientasPort descubridorHerramientasPort;
    private final EjecutorHerramientaPort ejecutorHerramientaPort;
    private final int maxTurnos;
    private final String modeloAgente;

    public EjecutarConsultaAgenteService(
            AgenteLlmPort agenteLlmPort,
            DescubridorHerramientasPort descubridorHerramientasPort,
            EjecutorHerramientaPort ejecutorHerramientaPort,
            @Value("${edusync.ai.agente.max-turnos:6}") int maxTurnos,
            @Value("${edusync.ai.agente.model:llama3.1:8b}") String modeloAgente) {
        this.agenteLlmPort = agenteLlmPort;
        this.descubridorHerramientasPort = descubridorHerramientasPort;
        this.ejecutorHerramientaPort = ejecutorHerramientaPort;
        this.maxTurnos = maxTurnos;
        this.modeloAgente = modeloAgente;
    }

    @Override
    public RespuestaAgente consultar(String pregunta, String jwtUsuario) {
        List<HerramientaLlm> catalogo = descubridorHerramientasPort.descubrir();
        Set<String> nombresValidos = catalogo.stream()
                .map(HerramientaLlm::nombre)
                .collect(Collectors.toSet());

        List<MensajeAgente> historial = new ArrayList<>();
        historial.add(MensajeAgente.pregunta(pregunta));
        List<String> herramientasUsadas = new ArrayList<>();

        for (int turno = 1; turno <= maxTurnos; turno++) {
            ResultadoTurno resultado = agenteLlmPort.decidirSiguientePaso(historial, catalogo);

            if (resultado.respuestaFinal().isPresent()) {
                log.debug("Agente resuelto en {} turno(s), {} herramienta(s) usada(s)",
                        turno, herramientasUsadas.size());
                return new RespuestaAgente(
                        resultado.respuestaFinal().get(),
                        herramientasUsadas,
                        turno,
                        RespuestaAgente.CAMINO_AGENTE,
                        modeloAgente);
            }

            LlamadaHerramienta llamada = resultado.llamada().orElseThrow(() ->
                    new IllegalStateException("AgenteLlmPort no devolvio ni llamada ni respuesta final"));

            if (!nombresValidos.contains(llamada.nombreHerramienta())) {
                // No se ejecuta nada: la herramienta no existe en el catalogo descubierto.
                throw new HerramientaNoDisponibleException(llamada.nombreHerramienta());
            }

            log.debug("Turno {}: ejecutando herramienta '{}'", turno, llamada.nombreHerramienta());
            historial.add(MensajeAgente.decisionHerramienta(llamada));

            String observacion = ejecutorHerramientaPort.ejecutar(llamada, jwtUsuario);
            historial.add(MensajeAgente.observacion(observacion));
            herramientasUsadas.add(llamada.nombreHerramienta());
        }

        throw new LimiteTurnosAlcanzadoException(maxTurnos);
    }
}
