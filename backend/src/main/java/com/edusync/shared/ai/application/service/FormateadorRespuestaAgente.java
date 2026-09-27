package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.DefinicionHerramientaAgente;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Unica fuente del texto de producto en el camino KEYWORD (DD-UC-025).
 * Nunca concatena el JSON crudo. No cita {@code rude} (NFR-007).
 */
@Component
public class FormateadorRespuestaAgente {

    private static final int MAX_NOMBRES = 8;
    private final ObjectMapper objectMapper;

    public FormateadorRespuestaAgente(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String formatear(DefinicionHerramientaAgente definicion, String jsonObservacion) {
        if (jsonObservacion == null || jsonObservacion.isBlank()) {
            return "No hay datos de " + definicion.etiqueta() + " para mostrar.";
        }
        JsonNode raiz;
        try {
            raiz = objectMapper.readTree(jsonObservacion);
        } catch (Exception e) {
            return "Consulta de " + definicion.etiqueta() + " completada.";
        }
        if (raiz == null || raiz.isNull() || raiz.isMissingNode()) {
            return "No hay datos de " + definicion.etiqueta() + " para mostrar.";
        }
        if (raiz.has("error") && !raiz.get("error").isNull()) {
            return "No pude consultar " + definicion.etiqueta()
                    + " (sin permiso o el servicio no respondio).";
        }
        if (raiz.path("confirmationRequired").asBoolean()) {
            String preview = raiz.path("preview").asString("");
            if (preview.isBlank()) {
                preview = "Se va a crear " + definicion.etiqueta() + ".";
            }
            return preview + "\nEscribi *confirmo* o pulsa Confirmar.";
        }

        String estado = raiz.path("estado").asString("");
        if (esEstadoConsulta(estado)) {
            return formatearEstado(definicion, raiz, estado);
        }

        JsonNode items = raiz.has("content") && raiz.get("content").isArray()
                ? raiz.get("content")
                : (raiz.isArray() ? raiz : null);
        JsonNode totalNode = raiz.get("totalElements");
        long total = (totalNode != null && totalNode.isNumber()) ? totalNode.asLong() : -1;

        if (items == null && raiz.isObject()) {
            String nombre = nombreDe(raiz);
            if (nombre != null && !nombre.isBlank()) {
                return "La " + definicion.etiqueta() + " es " + nombre + ".";
            }
            return "Consulta de " + definicion.etiqueta() + " completada.";
        }
        if (items == null || !items.isArray()) {
            return "Consulta de " + definicion.etiqueta() + " completada.";
        }
        int n = items.size();
        long mostradoComoTotal = total >= 0 ? total : n;
        if (mostradoComoTotal == 0) {
            return "No hay " + definicion.etiqueta() + " en tu institucion.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hay ").append(mostradoComoTotal).append(' ').append(definicion.etiqueta()).append('.');
        int limite = Math.min(n, MAX_NOMBRES);
        boolean hayNombres = false;
        for (int i = 0; i < limite; i++) {
            String nombre = nombreDe(items.get(i));
            if (nombre == null || nombre.isBlank()) {
                continue;
            }
            if (!hayNombres) {
                sb.append("\n");
                hayNombres = true;
            }
            sb.append("- ").append(nombre).append('\n');
        }
        if (n > MAX_NOMBRES) {
            sb.append("… y ").append(mostradoComoTotal - MAX_NOMBRES).append(" mas.");
        }
        return sb.toString().trim();
    }

    public String previewEscritura(DefinicionHerramientaAgente definicion, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "Para crear " + articulo(definicion.etiqueta()) + " indica el nombre, por ejemplo: crea "
                    + articulo(definicion.etiqueta()) + " 1ro A.";
        }
        return "Se creara " + articulo(definicion.etiqueta()) + " «" + nombre + "».\n"
                + "Escribi *confirmo* o pulsa Confirmar.";
    }

    public String escrituraCompletada(DefinicionHerramientaAgente definicion, String jsonObservacion) {
        JsonNode raiz;
        try {
            raiz = objectMapper.readTree(jsonObservacion);
        } catch (Exception e) {
            return "Se creo " + articulo(definicion.etiqueta()) + ".";
        }
        if (raiz != null && raiz.has("error") && !raiz.get("error").isNull()) {
            return "No pude crear " + articulo(definicion.etiqueta())
                    + " (sin permiso o el servicio no respondio).";
        }
        String nombre = nombreDe(raiz);
        if (nombre != null && !nombre.isBlank()) {
            return "Listo. Se creo " + articulo(definicion.etiqueta()) + " «" + nombre + "».";
        }
        return "Listo. Se creo " + articulo(definicion.etiqueta()) + ".";
    }

    private String formatearEstado(DefinicionHerramientaAgente definicion, JsonNode raiz, String estado) {
        if ("AMBIGUO".equals(estado)) {
            StringBuilder sb = new StringBuilder("Hay varias coincidencias. ¿A cuál te refieres?\n");
            JsonNode matches = raiz.path("matches");
            if (matches.isArray()) {
                for (JsonNode m : matches) {
                    String et = m.path("etiqueta").asString("");
                    if (!et.isBlank()) {
                        sb.append("- ").append(et).append('\n');
                    }
                }
            }
            return sb.toString().trim();
        }
        if ("NINGUNO".equals(estado) || "SIN_RESULTADOS".equals(estado)) {
            return "No encontré " + definicion.etiqueta() + " con esos datos en tu institución.";
        }
        if ("FALTA_CONTEXTO".equals(estado)) {
            String pregunta = raiz.path("preguntaAclaracion").asString("");
            return pregunta.isBlank() ? "Falta información para completar la consulta." : pregunta;
        }
        JsonNode filas = raiz.path("filas");
        JsonNode matches = raiz.path("matches");
        JsonNode agregado = raiz.get("agregado");
        StringBuilder sb = new StringBuilder();
        if (agregado != null && agregado.isNumber()) {
            String op = raiz.path("operacion").asString("");
            if ("PROMEDIO".equals(op)) {
                sb.append("El promedio es ").append(agregado.asInt()).append('.');
            } else if ("TOP".equals(op)) {
                sb.append("El más alto es ").append(agregado.asInt()).append('.');
            } else if ("REPROBADOS".equals(op) || "NOMINA".equals(op) || "MATERIAS_ESTUDIANTE".equals(op)) {
                sb.append("Hay ").append(agregado.asInt()).append('.');
            } else {
                sb.append("Resultado: ").append(agregado.asInt()).append('.');
            }
        }
        JsonNode lista = filas.isArray() && filas.size() > 0 ? filas : matches;
        if (lista.isArray() && lista.size() > 0) {
            if (sb.length() > 0) {
                sb.append('\n');
            } else if ("UNICO".equals(estado) || "OK".equals(estado)) {
                sb.append("Sí.");
                sb.append('\n');
            }
            int limite = Math.min(lista.size(), MAX_NOMBRES);
            for (int i = 0; i < limite; i++) {
                JsonNode item = lista.get(i);
                String etiqueta = item.path("etiqueta").asString("");
                if (etiqueta.isBlank()) {
                    etiqueta = nombreDe(item);
                }
                if (etiqueta == null || etiqueta.isBlank()) {
                    continue;
                }
                Integer valor = item.get("valor") != null && item.get("valor").isNumber()
                        ? item.get("valor").asInt() : null;
                String detalle = item.path("detalle").asString("");
                sb.append("- ").append(etiqueta);
                if (valor != null) {
                    sb.append(": ").append(valor);
                }
                if (!detalle.isBlank() && valor != null) {
                    sb.append(" (").append(detalle).append(')');
                } else if (!detalle.isBlank() && valor == null) {
                    sb.append(" — ").append(detalle);
                }
                sb.append('\n');
            }
        }
        String texto = sb.toString().trim();
        return texto.isBlank() ? "Consulta de " + definicion.etiqueta() + " completada." : texto;
    }

    private static boolean esEstadoConsulta(String estado) {
        return "UNICO".equals(estado)
                || "AMBIGUO".equals(estado)
                || "NINGUNO".equals(estado)
                || "OK".equals(estado)
                || "SIN_RESULTADOS".equals(estado)
                || "FALTA_CONTEXTO".equals(estado);
    }

    private static String articulo(String etiqueta) {
        if (etiqueta == null || etiqueta.isBlank()) {
            return "el registro";
        }
        if (etiqueta.startsWith("el ") || etiqueta.startsWith("la ") || etiqueta.startsWith("un ")) {
            return etiqueta;
        }
        return "el " + etiqueta;
    }

    private static String nombreDe(JsonNode item) {
        if (item == null || !item.isObject()) {
            return null;
        }
        String nombre = null;
        for (String campo : new String[] {"nombre", "nombreCompleto", "email"}) {
            if (item.hasNonNull(campo)) {
                nombre = item.get(campo).asString("");
                break;
            }
        }
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        if (item.hasNonNull("estado")) {
            String estado = item.get("estado").asString("");
            if (!estado.isBlank()) {
                return nombre + " (" + estado + ")";
            }
        }
        return nombre;
    }
}
