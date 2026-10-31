package com.edusync.shared.ai.application.monitoring;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Pasa un candidato al dataset dorado online solo si una persona lo aceptó.
 * No modifica {@code tools/red-team-agent/catalog/golden.json}.
 */
public final class PromotorCasoOnline {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PromotorCasoOnline() {
    }

    public record Resultado(boolean escrito, String motivo) {
    }

    public static Resultado promover(Path revision, Path destino) throws IOException {
        JsonNode rev = MAPPER.readTree(Files.readString(revision));
        String estado = rev.path("estado").asString("");
        if (!"ACEPTADO".equals(estado)) {
            return new Resultado(false, "no promovido: estado=" + estado);
        }
        String id = rev.path("id").asString("");
        if (id.isBlank()) {
            return new Resultado(false, "no promovido: falta id");
        }
        ObjectNode raiz = leerOCrear(destino);
        ArrayNode cases = (ArrayNode) raiz.withArray("cases");
        for (JsonNode caso : cases) {
            if (id.equals(caso.path("id").asString())) {
                return new Resultado(false, "ya estaba en el dataset: " + id);
            }
        }
        ObjectNode nuevo = MAPPER.createObjectNode();
        nuevo.put("id", id);
        nuevo.put("pregunta", rev.path("pregunta").asString(""));
        nuevo.put("comportamiento_esperado", rev.path("comportamiento_esperado").asString(""));
        nuevo.put("prioridad", rev.path("prioridad").asString("HIGH"));
        nuevo.put("origen", "monitoreo-online");
        ArrayNode noDebe = nuevo.putArray("no_debe_contener");
        rev.path("no_debe_contener").forEach(n -> noDebe.add(n.asString()));
        cases.add(nuevo);
        Files.createDirectories(destino.getParent() == null ? Path.of(".") : destino.getParent());
        Files.writeString(destino, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(raiz) + System.lineSeparator());
        return new Resultado(true, "aceptado en el dataset: " + id);
    }

    private static ObjectNode leerOCrear(Path destino) throws IOException {
        if (Files.exists(destino)) {
            JsonNode nodo = MAPPER.readTree(Files.readString(destino));
            if (nodo instanceof ObjectNode objeto) {
                if (!objeto.path("cases").isArray()) {
                    objeto.putArray("cases");
                }
                return objeto;
            }
        }
        ObjectNode raiz = MAPPER.createObjectNode();
        raiz.put("version", "1.0.0");
        raiz.put("nota", "Casos aceptados por una persona desde el monitoreo online. El monitor no los agrega solo.");
        raiz.putArray("cases");
        return raiz;
    }

    public static List<String> ids(Path destino) throws IOException {
        if (!Files.exists(destino)) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        MAPPER.readTree(Files.readString(destino)).path("cases").forEach(n -> ids.add(n.path("id").asString()));
        return ids;
    }
}
