package com.edusync.shared.ai.application.monitoring;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Una interacción del asistente, ya leída del JSONL. No transporta JWT ni secretos.
 */
public record EventoInteraccion(
        String ts,
        String usuario,
        String version,
        String pregunta,
        String respuesta,
        List<String> fuentes,
        String contexto,
        String camino,
        String herramienta,
        String guard,
        long latenciaMs,
        Integer tokens,
        String feedback,
        boolean textoOmitido) {

    public EventoInteraccion {
        fuentes = fuentes == null ? List.of() : List.copyOf(fuentes);
        pregunta = pregunta == null ? "" : pregunta;
        respuesta = respuesta == null ? "" : respuesta;
        contexto = contexto == null ? "" : contexto;
        usuario = usuario == null ? "anonimo" : usuario;
        version = version == null ? "" : version;
        camino = camino == null ? "" : camino;
    }

    public static EventoInteraccion desde(JsonNode nodo) {
        List<String> fuentes = new ArrayList<>();
        JsonNode arr = nodo.path("fuentes");
        if (arr.isArray()) {
            arr.forEach(item -> fuentes.add(item.asString()));
        }
        Integer tokens = nodo.path("tokens").isNumber() ? nodo.path("tokens").asInt() : null;
        String feedback = textoONull(nodo, "feedback");
        String guard = textoONull(nodo, "guard");
        String herramienta = textoONull(nodo, "herramienta");
        return new EventoInteraccion(
                nodo.path("ts").asString(""),
                nodo.path("usuario").asString("anonimo"),
                nodo.path("version").asString(""),
                nodo.path("pregunta").asString(""),
                nodo.path("respuesta").asString(""),
                fuentes,
                nodo.path("contexto").asString(""),
                nodo.path("camino").asString(""),
                herramienta,
                guard,
                nodo.path("latencia_ms").asLong(0),
                tokens,
                feedback,
                nodo.path("texto_omitido").asBoolean(false));
    }

    public boolean esGenerada() {
        return "LLM".equals(camino) || "PROCESO".equals(camino);
    }

    public boolean fueraDeAlcance() {
        return "FUERA_DE_ALCANCE".equals(camino) || "NINGUNO".equals(camino);
    }

    public boolean bloqueada() {
        return "bloqueado".equals(guard) || "rechazado".equals(guard) || "redactado".equals(guard);
    }

    private static String textoONull(JsonNode nodo, String campo) {
        JsonNode valor = nodo.path(campo);
        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }
        String texto = valor.asString();
        return texto.isBlank() ? null : texto;
    }
}
