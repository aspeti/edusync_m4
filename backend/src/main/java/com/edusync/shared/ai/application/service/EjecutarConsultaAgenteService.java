package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.in.EjecutarConsultaAgenteUseCase;
import com.edusync.shared.ai.application.port.out.AgenteLlmPort;
import com.edusync.shared.ai.application.port.out.AgenteLlmPort.ResultadoTurno;
import com.edusync.shared.ai.application.port.out.EjecutorHerramientaPort;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import com.edusync.shared.ai.domain.DefinicionHerramientaAgente;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.HerramientaNoDisponibleException;
import com.edusync.shared.ai.domain.LimiteTurnosAlcanzadoException;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.MensajeAgente;
import com.edusync.shared.ai.domain.PasoTrazaAgente;
import com.edusync.shared.ai.domain.RespuestaAgente;
import com.edusync.shared.ai.domain.TurnoHistorialAgente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orquesta KEYWORD, CONSULTA (analizador) y ReAct. ADR-0018 / 0019 / 0020.
 */
@Service
public class EjecutarConsultaAgenteService implements EjecutarConsultaAgenteUseCase {

    private static final Logger log = LoggerFactory.getLogger(EjecutarConsultaAgenteService.class);

    private final AgenteLlmPort agenteLlmPort;
    private final EjecutorHerramientaPort ejecutorHerramientaPort;
    private final EnrutadorPalabrasClaveAgente enrutadorPalabrasClave;
    private final FormateadorRespuestaAgente formateadorRespuesta;
    private final CatalogoHerramientasAgente catalogoHerramientas;
    private final AnalizadorIntencionConsultaAcademica analizadorIntencion;
    private final OrquestadorConsultaAcademica orquestadorConsulta;
    private final int maxTurnos;
    private final String modeloAgente;
    private final boolean aiEnabled;
    private final boolean agenteHabilitado;
    private final boolean llmHabilitado;

    public EjecutarConsultaAgenteService(
            AgenteLlmPort agenteLlmPort,
            EjecutorHerramientaPort ejecutorHerramientaPort,
            EnrutadorPalabrasClaveAgente enrutadorPalabrasClave,
            FormateadorRespuestaAgente formateadorRespuesta,
            CatalogoHerramientasAgente catalogoHerramientas,
            AnalizadorIntencionConsultaAcademica analizadorIntencion,
            OrquestadorConsultaAcademica orquestadorConsulta,
            @Value("${edusync.ai.agente.max-turnos:6}") int maxTurnos,
            @Value("${edusync.ai.agente.model:llama3.1:8b}") String modeloAgente,
            @Value("${edusync.ai.enabled:true}") boolean aiEnabled,
            @Value("${edusync.ai.agente.habilitado:true}") boolean agenteHabilitado,
            @Value("${edusync.ai.agente.llm-habilitado:true}") boolean llmHabilitado) {
        this.agenteLlmPort = agenteLlmPort;
        this.ejecutorHerramientaPort = ejecutorHerramientaPort;
        this.enrutadorPalabrasClave = enrutadorPalabrasClave;
        this.formateadorRespuesta = formateadorRespuesta;
        this.catalogoHerramientas = catalogoHerramientas;
        this.analizadorIntencion = analizadorIntencion;
        this.orquestadorConsulta = orquestadorConsulta;
        this.maxTurnos = maxTurnos;
        this.modeloAgente = modeloAgente;
        this.aiEnabled = aiEnabled;
        this.agenteHabilitado = agenteHabilitado;
        this.llmHabilitado = llmHabilitado;
    }

    @Override
    public RespuestaAgente consultar(String pregunta, String jwtUsuario, boolean confirmed) {
        return consultar(pregunta, jwtUsuario, confirmed, List.of(), null);
    }

    @Override
    public RespuestaAgente consultar(
            String pregunta,
            String jwtUsuario,
            boolean confirmed,
            List<TurnoHistorialAgente> history,
            ContextoConsultaAgente contexto) {
        if (!aiEnabled || !agenteHabilitado) {
            throw new AiDeshabilitadoException();
        }

        Optional<EnrutadorPalabrasClaveAgente.Match> keyword = enrutadorPalabrasClave.resolverConArgumentos(pregunta);
        if (keyword.isPresent()) {
            return resolverPorKeyword(keyword.get(), jwtUsuario, confirmed);
        }
        Optional<IntencionConsultaAcademica> intencion = analizadorIntencion.analizar(pregunta, contexto);
        if (intencion.isPresent()) {
            return orquestadorConsulta.ejecutar(intencion.get(), contexto, jwtUsuario);
        }
        if (!llmHabilitado) {
            log.debug("Sin match KEYWORD/CONSULTA y LLM apagado");
            return new RespuestaAgente(
                    "No reconozco esa consulta en el catalogo y el modelo esta apagado. "
                            + "Prueba una frase como «lista los cursos» o «notas de Juan del primer trimestre».",
                    List.of(),
                    0,
                    RespuestaAgente.CAMINO_NINGUNO,
                    RespuestaAgente.FUENTE_NINGUNO);
        }
        return resolverPorLlm(pregunta, jwtUsuario, history == null ? List.of() : history);
    }

    private RespuestaAgente resolverPorKeyword(
            EnrutadorPalabrasClaveAgente.Match match, String jwtUsuario, boolean confirmed) {
        DefinicionHerramientaAgente definicion = match.definicion();
        if (definicion.escritura()) {
            return resolverEscrituraKeyword(definicion, match.argumentos(), jwtUsuario, confirmed);
        }
        LlamadaHerramienta llamada = new LlamadaHerramienta("keyword", definicion.toolId(), match.argumentos());
        String observacion = ejecutorHerramientaPort.ejecutar(llamada, jwtUsuario);
        boolean exito = observacion == null || !observacion.contains("\"error\"");
        String texto = formateadorRespuesta.formatear(definicion, observacion);
        PasoTrazaAgente paso = new PasoTrazaAgente(1, definicion.toolId(), definicion.tablasFuente(), exito);
        log.debug("Camino KEYWORD tool={}", definicion.toolId());
        return new RespuestaAgente(
                texto,
                List.of(definicion.toolId()),
                0,
                RespuestaAgente.CAMINO_KEYWORD,
                RespuestaAgente.FUENTE_CATALOGO,
                RespuestaAgente.AGENTE_GENERAL,
                List.of(paso),
                false);
    }

    private RespuestaAgente resolverEscrituraKeyword(
            DefinicionHerramientaAgente definicion,
            Map<String, Object> argumentos,
            String jwtUsuario,
            boolean confirmed) {
        String nombre = argumentos.get("nombre") == null ? "" : argumentos.get("nombre").toString();
        if (nombre.isBlank()) {
            return new RespuestaAgente(
                    formateadorRespuesta.previewEscritura(definicion, ""),
                    List.of(definicion.toolId()),
                    0,
                    RespuestaAgente.CAMINO_KEYWORD,
                    RespuestaAgente.FUENTE_CATALOGO,
                    RespuestaAgente.AGENTE_GENERAL,
                    List.of(new PasoTrazaAgente(1, definicion.toolId(), definicion.tablasFuente(), false)),
                    false);
        }
        if (!confirmed) {
            log.debug("Camino KEYWORD write preview tool={}", definicion.toolId());
            return new RespuestaAgente(
                    formateadorRespuesta.previewEscritura(definicion, nombre),
                    List.of(definicion.toolId()),
                    0,
                    RespuestaAgente.CAMINO_KEYWORD,
                    RespuestaAgente.FUENTE_CATALOGO,
                    RespuestaAgente.AGENTE_GENERAL,
                    List.of(new PasoTrazaAgente(1, definicion.toolId(), definicion.tablasFuente(), true)),
                    true);
        }
        Map<String, Object> args = new java.util.HashMap<>(argumentos);
        args.put("confirmed", true);
        LlamadaHerramienta llamada = new LlamadaHerramienta("keyword", definicion.toolId(), args);
        String observacion = ejecutorHerramientaPort.ejecutar(llamada, jwtUsuario);
        boolean exito = observacion == null || !observacion.contains("\"error\"");
        String texto = formateadorRespuesta.escrituraCompletada(definicion, observacion);
        return new RespuestaAgente(
                texto,
                List.of(definicion.toolId()),
                0,
                RespuestaAgente.CAMINO_KEYWORD,
                RespuestaAgente.FUENTE_CATALOGO,
                RespuestaAgente.AGENTE_GENERAL,
                List.of(new PasoTrazaAgente(1, definicion.toolId(), definicion.tablasFuente(), exito)),
                false);
    }

    private RespuestaAgente resolverPorLlm(
            String pregunta, String jwtUsuario, List<TurnoHistorialAgente> history) {
        List<HerramientaLlm> catalogo = catalogoHerramientas.herramientasLlm();
        Set<String> nombresValidos = catalogo.stream()
                .map(HerramientaLlm::nombre)
                .collect(Collectors.toSet());

        List<MensajeAgente> historial = new ArrayList<>();
        for (TurnoHistorialAgente turno : history) {
            if (turno == null || turno.content() == null || turno.content().isBlank()) {
                continue;
            }
            if ("assistant".equalsIgnoreCase(turno.role())) {
                historial.add(MensajeAgente.respuestaFinal(turno.content()));
            } else {
                historial.add(MensajeAgente.pregunta(turno.content()));
            }
        }
        historial.add(MensajeAgente.pregunta(pregunta));
        List<String> herramientasUsadas = new ArrayList<>();
        List<PasoTrazaAgente> steps = new ArrayList<>();
        String ultimaObservacion = null;
        String ultimaTool = null;

        for (int turno = 1; turno <= maxTurnos; turno++) {
            ResultadoTurno resultado = agenteLlmPort.decidirSiguientePaso(historial, catalogo);

            if (resultado.respuestaFinal().isPresent()) {
                log.debug("Agente LLM resuelto en {} turno(s)", turno);
                String texto;
                if (ultimaTool != null && ultimaObservacion != null) {
                    final String toolId = ultimaTool;
                    final String observacion = ultimaObservacion;
                    texto = catalogoHerramientas.porId(toolId)
                            .map(d -> formateadorRespuesta.formatear(d, observacion))
                            .orElseGet(() -> formateadorRespuesta.formatear(
                                    catalogoHerramientas.porId("consultar_academico").orElseThrow(),
                                    observacion));
                } else {
                    texto = "No pude resolverlo con las herramientas disponibles. "
                            + "Prueba a nombrar al estudiante, la materia o el curso.";
                }
                return new RespuestaAgente(
                        texto,
                        herramientasUsadas,
                        turno,
                        RespuestaAgente.CAMINO_LLM,
                        modeloAgente,
                        RespuestaAgente.AGENTE_GENERAL,
                        steps);
            }

            LlamadaHerramienta llamada = resultado.llamada().orElseThrow(() ->
                    new IllegalStateException("AgenteLlmPort no devolvio ni llamada ni respuesta final"));

            if (!nombresValidos.contains(llamada.nombreHerramienta())) {
                throw new HerramientaNoDisponibleException(llamada.nombreHerramienta());
            }

            historial.add(MensajeAgente.decisionHerramienta(llamada));
            String observacion = ejecutorHerramientaPort.ejecutar(llamada, jwtUsuario);
            historial.add(MensajeAgente.observacion(observacion));
            herramientasUsadas.add(llamada.nombreHerramienta());
            ultimaObservacion = observacion;
            ultimaTool = llamada.nombreHerramienta();
            boolean exito = observacion == null || !observacion.contains("\"error\"");
            DefinicionHerramientaAgente def = catalogoHerramientas.porId(llamada.nombreHerramienta()).orElse(null);
            steps.add(new PasoTrazaAgente(
                    steps.size() + 1,
                    llamada.nombreHerramienta(),
                    def == null ? List.of() : def.tablasFuente(),
                    exito));
        }

        throw new LimiteTurnosAlcanzadoException(maxTurnos);
    }
}
