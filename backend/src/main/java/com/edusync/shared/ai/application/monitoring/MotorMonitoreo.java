package com.edusync.shared.ai.application.monitoring;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Evals en línea sobre un JSONL. No llama al modelo ni escribe en el dataset dorado.
 */
public final class MotorMonitoreo {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MotorMonitoreo() {
    }

    public static List<EventoInteraccion> cargar(Path archivo) throws IOException {
        List<EventoInteraccion> eventos = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo)) {
            if (!linea.isBlank()) {
                eventos.add(EventoInteraccion.desde(MAPPER.readTree(linea)));
            }
        }
        return eventos;
    }

    public static ResumenOnline resumir(List<EventoInteraccion> eventos, UmbralesMonitoreo umbrales) {
        Map<String, Integer> porCamino = new TreeMap<>();
        List<Double> fidelidades = new ArrayList<>();
        int noFieles = 0;
        int prohibidas = 0;
        int calificadas = 0;
        int negativas = 0;
        int fuera = 0;
        List<Long> latencias = new ArrayList<>();
        List<Integer> tokens = new ArrayList<>();
        Map<String, List<LocalDateTime>> bloqueos = new LinkedHashMap<>();

        for (EventoInteraccion evento : eventos) {
            porCamino.merge(evento.camino(), 1, Integer::sum);
            if (evento.fueraDeAlcance()) {
                fuera++;
            }
            if (!evento.textoOmitido() && evento.esGenerada() && !evento.respuesta().isBlank()) {
                double f = FidelidadHeuristica.puntaje(
                        evento.respuesta(), evento.contexto() + "\n" + evento.pregunta());
                fidelidades.add(f);
                if (f < 1.0) {
                    noFieles++;
                }
            }
            if (!evento.textoOmitido()
                    && FidelidadHeuristica.fraseProhibida(evento.respuesta(), umbrales.frases()) != null) {
                prohibidas++;
            }
            if (evento.feedback() != null) {
                calificadas++;
                if ("negativo".equalsIgnoreCase(evento.feedback())) {
                    negativas++;
                }
            }
            if (evento.esGenerada()) {
                latencias.add(evento.latenciaMs());
                if (evento.tokens() != null) {
                    tokens.add(evento.tokens());
                }
            }
            if (evento.bloqueada()) {
                bloqueos.computeIfAbsent(evento.usuario(), k -> new ArrayList<>())
                        .add(parsear(evento.ts()));
            }
        }

        Map<String, Integer> sobreLimite = new LinkedHashMap<>();
        int totalBloqueos = 0;
        for (Map.Entry<String, List<LocalDateTime>> entry : bloqueos.entrySet()) {
            totalBloqueos += entry.getValue().size();
            int rafaga = maxEnVentana(entry.getValue(), umbrales.ventanaMinutos());
            if (rafaga > umbrales.bloqueosMaximo()) {
                sobreLimite.put(entry.getKey(), rafaga);
            }
        }

        double fidelidad = fidelidades.isEmpty()
                ? 1.0
                : fidelidades.stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
        Double tokensMedios = tokens.isEmpty()
                ? null
                : tokens.stream().mapToInt(Integer::intValue).average().orElse(0);
        int total = eventos.size();
        return new ResumenOnline(
                total,
                porCamino,
                fidelidad,
                fidelidades.size(),
                noFieles,
                prohibidas,
                calificadas == 0 ? 0.0 : negativas / (double) calificadas,
                calificadas,
                total == 0 ? 0.0 : fuera / (double) total,
                percentil(latencias, 95),
                tokensMedios,
                totalBloqueos,
                sobreLimite);
    }

    public static List<AlertaMonitoreo> alertas(ResumenOnline resumen, UmbralesMonitoreo u) {
        List<AlertaMonitoreo> alertas = new ArrayList<>();
        if (resumen.frasesProhibidas() > u.frasesMaximo()) {
            alertas.add(new AlertaMonitoreo(
                    "CRITICAL",
                    "frases prohibidas",
                    resumen.frasesProhibidas() + " respuestas",
                    "maximo " + u.frasesMaximo(),
                    u.causaFrases(),
                    u.accionFrases()));
        }
        for (Map.Entry<String, Integer> usuario : resumen.usuariosSobreLimite().entrySet()) {
            alertas.add(new AlertaMonitoreo(
                    "CRITICAL",
                    "seguridad " + usuario.getKey(),
                    usuario.getValue() + " bloqueos en " + u.ventanaMinutos() + " min",
                    "maximo " + u.bloqueosMaximo(),
                    u.causaBloqueos(),
                    u.accionBloqueos()));
        }
        if (resumen.fidelidadPromedio() < u.fidelidadMinima()) {
            alertas.add(new AlertaMonitoreo(
                    "HIGH",
                    "fidelidad promedio",
                    String.format("%.2f", resumen.fidelidadPromedio()),
                    String.format("minimo %.2f", u.fidelidadMinima()),
                    u.causaFidelidad(),
                    u.accionFidelidad()));
        }
        if (resumen.calificadas() > 0 && resumen.feedbackNegativo() > u.feedbackMaximo()) {
            alertas.add(new AlertaMonitoreo(
                    "MEDIUM",
                    "feedback negativo",
                    String.format("%.0f%% de %d", resumen.feedbackNegativo() * 100, resumen.calificadas()),
                    String.format("maximo %.0f%%", u.feedbackMaximo() * 100),
                    u.causaFeedback(),
                    u.accionFeedback()));
        }
        if (resumen.fueraDeAlcance() > u.fueraMaximo()) {
            alertas.add(new AlertaMonitoreo(
                    "MEDIUM",
                    "fuera de alcance",
                    String.format("%.0f%%", resumen.fueraDeAlcance() * 100),
                    String.format("maximo %.0f%%", u.fueraMaximo() * 100),
                    u.causaFuera(),
                    u.accionFuera()));
        }
        if (resumen.latenciaP95Ms() > u.latenciaP95Maxima()) {
            alertas.add(new AlertaMonitoreo(
                    "MEDIUM",
                    "latencia p95",
                    String.format("%.0f ms", resumen.latenciaP95Ms()),
                    "maximo " + u.latenciaP95Maxima() + " ms",
                    u.causaLatencia(),
                    u.accionLatencia()));
        }
        return alertas;
    }

    public static int codigoSalida(List<AlertaMonitoreo> alertas, UmbralesMonitoreo umbrales) {
        boolean bloquea = alertas.stream().anyMatch(a -> umbrales.bloquea(a.severidad()));
        return bloquea ? 1 : 0;
    }

    public static List<Map<String, Object>> casosNuevos(List<EventoInteraccion> eventos, UmbralesMonitoreo umbrales) {
        Map<String, Map<String, Object>> candidatos = new LinkedHashMap<>();
        Map<String, Integer> fuera = new LinkedHashMap<>();
        for (EventoInteraccion evento : eventos) {
            if (evento.textoOmitido()) {
                continue;
            }
            String prohibida = FidelidadHeuristica.fraseProhibida(evento.respuesta(), umbrales.frases());
            if (prohibida != null) {
                agregar(candidatos, evento, "comportamiento_prohibido", "frase prohibida: " + prohibida, true);
            } else if (evento.esGenerada()) {
                double f = FidelidadHeuristica.puntaje(
                        evento.respuesta(), evento.contexto() + "\n" + evento.pregunta());
                if (f < 1.0) {
                    agregar(candidatos, evento, "respuesta_no_sustentada", "respuesta no fiel al contexto", false);
                }
            }
            if (evento.bloqueada()) {
                agregar(candidatos, evento, "ataque_nuevo", "guard bloqueo la interaccion", true);
            }
            if (evento.fueraDeAlcance()) {
                fuera.merge(evento.pregunta(), 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> tema : fuera.entrySet()) {
            if (tema.getValue() >= umbrales.temaNuevoMinimo()) {
                EventoInteraccion ejemplo = eventos.stream()
                        .filter(e -> e.pregunta().equals(tema.getKey()))
                        .findFirst()
                        .orElseThrow();
                Map<String, Object> fila = base(ejemplo, "tema_sin_cobertura",
                        "tema frecuente sin cobertura (" + tema.getValue() + ")", false);
                fila.put("veces", tema.getValue());
                candidatos.put("tema:" + tema.getKey(), fila);
            }
        }
        return List.copyOf(candidatos.values());
    }

    public static int maxEnVentana(List<LocalDateTime> tiempos, int minutos) {
        List<LocalDateTime> ordenados = new ArrayList<>(tiempos);
        ordenados.sort(Comparator.naturalOrder());
        int mejor = 0;
        int inicio = 0;
        for (int fin = 0; fin < ordenados.size(); fin++) {
            while (Duration.between(ordenados.get(inicio), ordenados.get(fin)).toMinutes() > minutos) {
                inicio++;
            }
            mejor = Math.max(mejor, fin - inicio + 1);
        }
        return mejor;
    }

    public static double percentil(List<Long> valores, int p) {
        if (valores.isEmpty()) {
            return 0.0;
        }
        List<Long> ordenados = new ArrayList<>(valores);
        ordenados.sort(Comparator.naturalOrder());
        int indice = (int) Math.round(p / 100.0 * (ordenados.size() - 1));
        indice = Math.min(ordenados.size() - 1, Math.max(0, indice));
        return ordenados.get(indice);
    }

    private static void agregar(
            Map<String, Map<String, Object>> candidatos,
            EventoInteraccion evento,
            String tipo,
            String motivo,
            boolean critico) {
        String clave = tipo + ":" + evento.pregunta();
        Map<String, Object> existente = candidatos.get(clave);
        if (existente == null) {
            Map<String, Object> fila = base(evento, tipo, motivo, critico);
            candidatos.put(clave, fila);
            return;
        }
        existente.put("veces", ((int) existente.get("veces")) + 1);
        if (critico && !Boolean.TRUE.equals(existente.get("critico"))) {
            existente.put("critico", true);
            existente.put("tipo", tipo);
            existente.put("motivo", motivo);
        }
    }

    private static Map<String, Object> base(
            EventoInteraccion evento, String tipo, String motivo, boolean critico) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("pregunta", evento.pregunta());
        fila.put("tipo", tipo);
        fila.put("motivo", motivo);
        fila.put("critico", critico);
        fila.put("veces", 1);
        fila.put("version", evento.version());
        fila.put("camino", evento.camino());
        fila.put("referencia", "PENDIENTE: la escribe una persona");
        fila.put("no_debe_contener", List.of());
        return fila;
    }

    private static LocalDateTime parsear(String ts) {
        if (ts == null || ts.isBlank()) {
            return LocalDateTime.MIN;
        }
        return LocalDateTime.parse(ts);
    }
}
