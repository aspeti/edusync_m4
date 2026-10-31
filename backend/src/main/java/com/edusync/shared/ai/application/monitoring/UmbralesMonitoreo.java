package com.edusync.shared.ai.application.monitoring;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Umbrales versionados. Cambiarlos es un cambio de Git, no un ajuste oculto.
 */
public record UmbralesMonitoreo(
        double fidelidadMinima,
        int frasesMaximo,
        List<String> frases,
        int bloqueosMaximo,
        int ventanaMinutos,
        double feedbackMaximo,
        double fueraMaximo,
        long latenciaP95Maxima,
        int temaNuevoMinimo,
        List<String> bloqueaCi,
        String causaFidelidad,
        String accionFidelidad,
        String causaFrases,
        String accionFrases,
        String causaBloqueos,
        String accionBloqueos,
        String causaFeedback,
        String accionFeedback,
        String causaFuera,
        String accionFuera,
        String causaLatencia,
        String accionLatencia) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static UmbralesMonitoreo desdeClasspath() {
        try (InputStream in = UmbralesMonitoreo.class.getResourceAsStream("/ai/umbrales_monitoreo.json")) {
            if (in == null) {
                throw new IllegalStateException("No esta /ai/umbrales_monitoreo.json en el classpath");
            }
            return desde(MAPPER.readTree(in));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron leer los umbrales", ex);
        }
    }

    public static UmbralesMonitoreo desde(JsonNode raiz) {
        JsonNode fidelidad = raiz.path("fidelidad");
        JsonNode frases = raiz.path("frases_prohibidas");
        JsonNode bloqueos = raiz.path("bloqueos_por_usuario");
        JsonNode feedback = raiz.path("feedback_negativo");
        JsonNode fuera = raiz.path("fuera_de_alcance");
        JsonNode latencia = raiz.path("latencia_p95_ms");
        List<String> listaFrases = new ArrayList<>();
        frases.path("frases").forEach(n -> listaFrases.add(n.asString()));
        List<String> bloquea = new ArrayList<>();
        raiz.path("bloquea_ci").forEach(n -> bloquea.add(n.asString()));
        return new UmbralesMonitoreo(
                fidelidad.path("minimo").asDouble(0.85),
                frases.path("maximo").asInt(0),
                List.copyOf(listaFrases),
                bloqueos.path("maximo").asInt(3),
                bloqueos.path("ventana_minutos").asInt(10),
                feedback.path("maximo").asDouble(0.25),
                fuera.path("maximo").asDouble(0.20),
                latencia.path("maximo").asLong(8000),
                raiz.path("tema_nuevo_minimo").asInt(3),
                List.copyOf(bloquea),
                fidelidad.path("causa").asString(""),
                fidelidad.path("accion").asString(""),
                frases.path("causa").asString(""),
                frases.path("accion").asString(""),
                bloqueos.path("causa").asString(""),
                bloqueos.path("accion").asString(""),
                feedback.path("causa").asString(""),
                feedback.path("accion").asString(""),
                fuera.path("causa").asString(""),
                fuera.path("accion").asString(""),
                latencia.path("causa").asString(""),
                latencia.path("accion").asString(""));
    }

    public boolean bloquea(String severidad) {
        return bloqueaCi.contains(severidad);
    }
}
