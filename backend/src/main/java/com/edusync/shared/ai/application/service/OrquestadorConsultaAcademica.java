package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.out.EjecutorHerramientaPort;
import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import com.edusync.shared.ai.domain.DefinicionHerramientaAgente;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.PasoTrazaAgente;
import com.edusync.shared.ai.domain.RespuestaAgente;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrquestadorConsultaAcademica {

    private final EjecutorHerramientaPort ejecutorHerramientaPort;
    private final CatalogoHerramientasAgente catalogo;
    private final FormateadorRespuestaAgente formateador;
    private final ObjectMapper objectMapper;

    public OrquestadorConsultaAcademica(
            EjecutorHerramientaPort ejecutorHerramientaPort,
            CatalogoHerramientasAgente catalogo,
            FormateadorRespuestaAgente formateador,
            ObjectMapper objectMapper) {
        this.ejecutorHerramientaPort = ejecutorHerramientaPort;
        this.catalogo = catalogo;
        this.formateador = formateador;
        this.objectMapper = objectMapper;
    }

    public RespuestaAgente ejecutar(
            IntencionConsultaAcademica intencion, ContextoConsultaAgente previo, String jwtUsuario) {
        List<String> tools = new ArrayList<>();
        List<PasoTrazaAgente> steps = new ArrayList<>();
        ContextoConsultaAgente ctx = previo == null ? ContextoConsultaAgente.vacio() : previo;

        MatchResuelto estudiante = resolverSiHay(
                "find_estudiante", intencion.estudiante(), jwtUsuario, tools, steps);
        if (estudiante != null && estudiante.ambiguo) {
            return respuesta(estudiante.json, "find_estudiante", tools, steps, ctx);
        }
        MatchResuelto materia = resolverSiHay("find_materia", intencion.materia(), jwtUsuario, tools, steps);
        if (materia != null && materia.ambiguo) {
            return respuesta(materia.json, "find_materia", tools, steps, ctx);
        }
        MatchResuelto periodo = resolverSiHay("find_periodo", intencion.periodo(), jwtUsuario, tools, steps);
        if (periodo != null && periodo.ambiguo) {
            return respuesta(periodo.json, "find_periodo", tools, steps, ctx);
        }
        MatchResuelto paralelo = resolverSiHay(
                "find_curso_paralelo", intencion.cursoParalelo(), jwtUsuario, tools, steps);
        if (paralelo != null && paralelo.ambiguo) {
            return respuesta(paralelo.json, "find_curso_paralelo", tools, steps, ctx);
        }
        MatchResuelto profesor = resolverSiHay("find_profesor", intencion.profesor(), jwtUsuario, tools, steps);
        if (profesor != null && profesor.ambiguo) {
            return respuesta(profesor.json, "find_profesor", tools, steps, ctx);
        }

        ctx = fusionar(ctx, estudiante, materia, periodo, paralelo, profesor, intencion.operacion().name());

        if (intencion.esBusquedaSimple()) {
            String json = profesor != null ? profesor.json : (estudiante != null ? estudiante.json : "{}");
            String tool = intencion.operacion() == IntencionConsultaAcademica.Operacion.BUSCAR_PROFESOR
                    ? "find_profesor"
                    : "find_estudiante";
            return respuesta(json, tool, tools, steps, ctx);
        }

        Map<String, Object> args = new HashMap<>();
        args.put("operacion", intencion.operacion().name());
        ponerSiHay(args, "estudianteId", first(estudiante, ctx.estudianteId()));
        ponerSiHay(args, "materiaId", first(materia, ctx.materiaId()));
        ponerSiHay(args, "periodoEvaluacionId", first(periodo, ctx.periodoId()));
        ponerSiHay(args, "paraleloId", first(paralelo, ctx.paraleloId()));
        ponerSiHay(args, "cursoId", paralelo != null && paralelo.cursoId != null ? paralelo.cursoId : ctx.cursoId());

        LlamadaHerramienta llamada = new LlamadaHerramienta("consulta", "consultar_academico", args);
        String observacion = ejecutorHerramientaPort.ejecutar(llamada, jwtUsuario);
        tools.add("consultar_academico");
        boolean exito = observacion == null || !observacion.contains("\"error\"");
        steps.add(new PasoTrazaAgente(steps.size() + 1, "consultar_academico", List.of("calificacion_evaluacion"), exito));
        DefinicionHerramientaAgente def = catalogo.porId("consultar_academico").orElseThrow();
        String texto = formateador.formatear(def, observacion);
        return new RespuestaAgente(
                texto,
                tools,
                0,
                RespuestaAgente.CAMINO_CONSULTA,
                RespuestaAgente.FUENTE_CATALOGO,
                RespuestaAgente.AGENTE_GENERAL,
                steps,
                false,
                ctx);
    }

    private MatchResuelto resolverSiHay(
            String toolId,
            String q,
            String jwt,
            List<String> tools,
            List<PasoTrazaAgente> steps) {
        if (q == null || q.isBlank()) {
            return null;
        }
        LlamadaHerramienta llamada = new LlamadaHerramienta("r", toolId, Map.of("q", q));
        String json = ejecutorHerramientaPort.ejecutar(llamada, jwt);
        tools.add(toolId);
        boolean exito = json == null || !json.contains("\"error\"");
        steps.add(new PasoTrazaAgente(steps.size() + 1, toolId, List.of(), exito));
        return parsear(json);
    }

    private MatchResuelto parsear(String json) {
        try {
            JsonNode raiz = objectMapper.readTree(json);
            String estado = raiz.path("estado").asString("");
            JsonNode matches = raiz.path("matches");
            String id = null;
            String etiqueta = null;
            String cursoId = null;
            String paraleloId = null;
            if (matches.isArray() && matches.size() > 0) {
                JsonNode m = matches.get(0);
                id = textoONulo(m.path("id"));
                etiqueta = textoONulo(m.path("etiqueta"));
                cursoId = textoONulo(m.path("cursoId"));
                paraleloId = textoONulo(m.path("paraleloId"));
            }
            boolean ambiguo = "AMBIGUO".equals(estado) || "NINGUNO".equals(estado);
            return new MatchResuelto(estado, id, etiqueta, cursoId, paraleloId, ambiguo, json);
        } catch (Exception e) {
            return new MatchResuelto("NINGUNO", null, null, null, null, true, json == null ? "{}" : json);
        }
    }

    private RespuestaAgente respuesta(
            String json,
            String toolId,
            List<String> tools,
            List<PasoTrazaAgente> steps,
            ContextoConsultaAgente ctx) {
        DefinicionHerramientaAgente def = catalogo.porId(toolId).orElseThrow();
        return new RespuestaAgente(
                formateador.formatear(def, json),
                tools,
                0,
                RespuestaAgente.CAMINO_CONSULTA,
                RespuestaAgente.FUENTE_CATALOGO,
                RespuestaAgente.AGENTE_GENERAL,
                steps,
                false,
                ctx);
    }

    private static ContextoConsultaAgente fusionar(
            ContextoConsultaAgente base,
            MatchResuelto estudiante,
            MatchResuelto materia,
            MatchResuelto periodo,
            MatchResuelto paralelo,
            MatchResuelto profesor,
            String operacion) {
        return new ContextoConsultaAgente(
                idO(estudiante, base.estudianteId()),
                etiquetaO(estudiante, base.estudianteEtiqueta()),
                paralelo != null && paralelo.cursoId != null ? paralelo.cursoId : base.cursoId(),
                paralelo != null && paralelo.id != null ? paralelo.id : base.paraleloId(),
                etiquetaO(paralelo, base.paraleloEtiqueta()),
                idO(materia, base.materiaId()),
                etiquetaO(materia, base.materiaEtiqueta()),
                idO(periodo, base.periodoId()),
                etiquetaO(periodo, base.periodoEtiqueta()),
                operacion);
    }

    private static String idO(MatchResuelto m, String fallback) {
        if (m != null && m.id != null && !m.ambiguo) {
            return m.id;
        }
        return fallback;
    }

    private static String etiquetaO(MatchResuelto m, String fallback) {
        if (m != null && m.etiqueta != null && !m.ambiguo) {
            return m.etiqueta;
        }
        return fallback;
    }

    private static String first(MatchResuelto m, String fallback) {
        if (m != null && m.id != null && !m.ambiguo) {
            return m.id;
        }
        return fallback;
    }

    private static String textoONulo(JsonNode nodo) {
        String valor = nodo.asString("");
        return valor.isBlank() ? null : valor;
    }

    private static void ponerSiHay(Map<String, Object> args, String clave, String valor) {
        if (valor != null && !valor.isBlank()) {
            args.put(clave, valor);
        }
    }

    private record MatchResuelto(
            String estado,
            String id,
            String etiqueta,
            String cursoId,
            String paraleloId,
            boolean ambiguo,
            String json) {}
}
