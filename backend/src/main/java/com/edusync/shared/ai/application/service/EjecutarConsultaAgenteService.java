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
import com.edusync.shared.ai.domain.ResultadoGuardrailEntrada;
import com.edusync.shared.ai.domain.ResultadoGuardrailSalida;
import com.edusync.shared.ai.domain.ResultadoRecuperacionProceso;
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
 * Orquesta el grafo de oleada 1 (ADR-0021): guardrails + KEYWORD / CONSULTA / ReAct.
 */
@Service
public class EjecutarConsultaAgenteService implements EjecutarConsultaAgenteUseCase {

    private static final Logger log = LoggerFactory.getLogger(EjecutarConsultaAgenteService.class);

    static final String RESPUESTA_SALUDO =
            "Hola. Puedo ayudarte con cursos, materias, estudiantes, notas y promedios "
                    + "de tu institucion. Prueba «lista los cursos» o «notas de Juan del primer trimestre».";

    private final AgenteLlmPort agenteLlmPort;
    private final EjecutorHerramientaPort ejecutorHerramientaPort;
    private final EnrutadorPalabrasClaveAgente enrutadorPalabrasClave;
    private final FormateadorRespuestaAgente formateadorRespuesta;
    private final CatalogoHerramientasAgente catalogoHerramientas;
    private final AnalizadorIntencionConsultaAcademica analizadorIntencion;
    private final OrquestadorConsultaAcademica orquestadorConsulta;
    private final GuardrailEntradaAgente guardrailEntrada;
    private final GuardrailSalidaAgente guardrailSalida;
    private final RecuperadorProcesosEdusync recuperadorProcesos;
    private final int maxTurnos;
    private final String modeloAgente;
    private final boolean aiEnabled;
    private final boolean agenteHabilitado;
    private final boolean llmHabilitado;
    private final PoliticaAlcanceAgente politicaAlcance = new PoliticaAlcanceAgente();

    public EjecutarConsultaAgenteService(
            AgenteLlmPort agenteLlmPort,
            EjecutorHerramientaPort ejecutorHerramientaPort,
            EnrutadorPalabrasClaveAgente enrutadorPalabrasClave,
            FormateadorRespuestaAgente formateadorRespuesta,
            CatalogoHerramientasAgente catalogoHerramientas,
            AnalizadorIntencionConsultaAcademica analizadorIntencion,
            OrquestadorConsultaAcademica orquestadorConsulta,
            GuardrailEntradaAgente guardrailEntrada,
            GuardrailSalidaAgente guardrailSalida,
            RecuperadorProcesosEdusync recuperadorProcesos,
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
        this.guardrailEntrada = guardrailEntrada;
        this.guardrailSalida = guardrailSalida;
        this.recuperadorProcesos = recuperadorProcesos;
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

        ResultadoGuardrailEntrada entrada = guardrailEntrada.revisar(pregunta);
        if (!entrada.ok()) {
            log.debug("Camino BLOQUEADO nodo={}", RutasGrafoAsistente.NODO_GUARDRAIL_ENTRADA);
            RespuestaAgente bloqueada = new RespuestaAgente(
                    entrada.motivo(),
                    List.of(),
                    0,
                    RespuestaAgente.CAMINO_BLOQUEADO,
                    RespuestaAgente.FUENTE_GUARDRAIL,
                    RespuestaAgente.AGENTE_GENERAL,
                    List.of(),
                    false);
            return aplicarGrafo(bloqueada, entrada, guardrailSalida.revisar(entrada.motivo()));
        }

        String preguntaLimpia = entrada.preguntaLimpia();
        Optional<EnrutadorPalabrasClaveAgente.Match> keyword =
                enrutadorPalabrasClave.resolverConArgumentos(preguntaLimpia);
        Optional<IntencionConsultaAcademica> intencion = keyword.isPresent()
                ? Optional.empty()
                : analizadorIntencion.analizar(preguntaLimpia, contexto);
        ResultadoRecuperacionProceso recuperado = new ResultadoRecuperacionProceso(List.of(), 0);
        boolean hayProceso = false;
        if (keyword.isEmpty() && intencion.isEmpty() && recuperadorProcesos.esPreguntaDeProceso(preguntaLimpia)) {
            recuperado = recuperadorProcesos.recuperar(preguntaLimpia, 2);
            hayProceso = recuperado.hayMatch();
        }
        RutasGrafoAsistente.Nodo nodo = RutasGrafoAsistente.siguiente(
                true,
                keyword.isPresent(),
                intencion.isPresent(),
                hayProceso,
                RutasGrafoAsistente.esSaludo(preguntaLimpia),
                llmHabilitado);
        log.debug("Grafo clasificar nodo={}", nodo);

        final ResultadoRecuperacionProceso corpus = recuperado;
        RespuestaAgente inner = switch (nodo) {
            case BLOQUEAR -> throw new IllegalStateException("BLOQUEAR ya se resolvio en guardrail de entrada");
            case KEYWORD -> resolverPorKeyword(keyword.orElseThrow(), jwtUsuario, confirmed);
            case CONSULTA -> orquestadorConsulta.ejecutar(intencion.orElseThrow(), contexto, jwtUsuario);
            case PROCESO -> respuestaProceso(corpus);
            case SALUDO -> respuestaSaludo();
            case REACT -> resolverPorLlm(
                    preguntaLimpia, jwtUsuario, history == null ? List.of() : history);
            case NINGUNO -> respuestaNinguno();
        };
        return aplicarGrafo(inner, entrada, guardrailSalida.revisar(inner.respuesta()));
    }

    private RespuestaAgente respuestaProceso(ResultadoRecuperacionProceso recuperado) {
        String texto = recuperadorProcesos.formatear(recuperado);
        List<String> fuentes = recuperado.fragmentos().stream()
                .map(ResultadoRecuperacionProceso.FragmentoProceso::fuente)
                .distinct()
                .toList();
        return new RespuestaAgente(
                texto,
                List.of(),
                0,
                RespuestaAgente.CAMINO_PROCESO,
                RespuestaAgente.FUENTE_CORPUS,
                RespuestaAgente.AGENTE_GENERAL,
                List.of(new PasoTrazaAgente(1, RutasGrafoAsistente.NODO_RECUPERAR, fuentes, true)),
                false);
    }

    private static RespuestaAgente respuestaSaludo() {
        return new RespuestaAgente(
                RESPUESTA_SALUDO,
                List.of(),
                0,
                RespuestaAgente.CAMINO_SALUDO,
                RespuestaAgente.FUENTE_CATALOGO,
                RespuestaAgente.AGENTE_GENERAL,
                List.of(new PasoTrazaAgente(1, RutasGrafoAsistente.NODO_SALUDO, List.of(), true)),
                false);
    }

    private static RespuestaAgente respuestaNinguno() {
        return new RespuestaAgente(
                "No reconozco esa consulta en el catalogo y el modelo esta apagado. "
                        + "Prueba una frase como «lista los cursos» o «notas de Juan del primer trimestre».",
                List.of(),
                0,
                RespuestaAgente.CAMINO_NINGUNO,
                RespuestaAgente.FUENTE_NINGUNO);
    }

    private static RespuestaAgente aplicarGrafo(
            RespuestaAgente inner,
            ResultadoGuardrailEntrada entrada,
            ResultadoGuardrailSalida salida) {
        List<PasoTrazaAgente> steps = new ArrayList<>();
        steps.add(new PasoTrazaAgente(1, RutasGrafoAsistente.NODO_GUARDRAIL_ENTRADA, List.of(), entrada.ok()));
        for (PasoTrazaAgente p : inner.steps()) {
            steps.add(new PasoTrazaAgente(steps.size() + 1, p.toolId(), p.tablasFuente(), p.exito()));
        }
        steps.add(new PasoTrazaAgente(
                steps.size() + 1, RutasGrafoAsistente.NODO_GUARDRAIL_SALIDA, List.of(), salida.ok()));
        return new RespuestaAgente(
                salida.textoEntregable(),
                inner.herramientasUsadas(),
                inner.turnos(),
                inner.camino(),
                inner.fuente(),
                inner.agente(),
                steps,
                inner.confirmacionRequerida(),
                inner.contexto());
    }

    private RespuestaAgente resolverPorKeyword(
            EnrutadorPalabrasClaveAgente.Match match, String jwtUsuario, boolean confirmed) {
        DefinicionHerramientaAgente definicion = match.definicion();
        if (definicion.escritura()) {
            return resolverEscrituraKeyword(definicion, match.argumentos(), jwtUsuario, confirmed);
        }
        if (!politicaAlcance.puedeEjecutar(definicion.toolId())) {
            log.debug("Camino KEYWORD tool={} rechazada por rol", definicion.toolId());
            return new RespuestaAgente(
                    "No puedo listar las cuentas de la institucion con este rol.",
                    List.of(),
                    0,
                    RespuestaAgente.CAMINO_KEYWORD,
                    RespuestaAgente.FUENTE_CATALOGO,
                    RespuestaAgente.AGENTE_GENERAL,
                    List.of(new PasoTrazaAgente(1, definicion.toolId(), definicion.tablasFuente(), false)),
                    false);
        }
        LlamadaHerramienta llamada = politicaAlcance.sanear(
                new LlamadaHerramienta("keyword", definicion.toolId(), match.argumentos()));
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
            if (!politicaAlcance.puedeEjecutar(llamada.nombreHerramienta())) {
                historial.add(MensajeAgente.observacion("{\"error\":\"sin permiso\"}"));
                continue;
            }
            LlamadaHerramienta saneada = politicaAlcance.sanear(llamada);
            String observacion = ejecutorHerramientaPort.ejecutar(saneada, jwtUsuario);
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
