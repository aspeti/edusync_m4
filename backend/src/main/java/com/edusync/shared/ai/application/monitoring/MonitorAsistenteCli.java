package com.edusync.shared.ai.application.monitoring;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CLI del monitor. Código 1 solo si {@code alertas} encuentra CRITICAL o HIGH.
 *
 * <pre>
 *   resumen|alertas|latencia|casos-nuevos &lt;trafico.jsonl&gt;
 *   promover &lt;revision.json&gt; &lt;destino.json&gt;
 * </pre>
 */
public final class MonitorAsistenteCli {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MonitorAsistenteCli() {
    }

    public static void main(String[] args) {
        System.exit(ejecutar(args));
    }

    public static int ejecutar(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Uso: resumen|alertas|latencia|casos-nuevos <jsonl> | promover <revision.json> <destino.json>");
                return 1;
            }
            String comando = args[0];
            if ("promover".equals(comando)) {
                if (args.length < 3) {
                    System.out.println("Uso: promover <revision.json> <destino.json>");
                    return 1;
                }
                PromotorCasoOnline.Resultado resultado =
                        PromotorCasoOnline.promover(Path.of(args[1]), Path.of(args[2]));
                System.out.println(resultado.motivo());
                return 0;
            }
            if (args.length < 2) {
                System.out.println("Falta el archivo de trafico.");
                return 1;
            }
            Path archivo = Path.of(args[1]);
            UmbralesMonitoreo umbrales = UmbralesMonitoreo.desdeClasspath();
            List<EventoInteraccion> eventos = MotorMonitoreo.cargar(archivo);
            return switch (comando) {
                case "resumen", "summary" -> resumen(archivo, eventos, umbrales);
                case "alertas", "alerts" -> alertas(archivo, eventos, umbrales);
                case "latencia", "latency", "tiempo" -> latencia(archivo, eventos);
                case "casos-nuevos", "new-cases" -> casos(archivo, eventos, umbrales);
                default -> {
                    System.out.println("Comando desconocido: " + comando);
                    yield 1;
                }
            };
        } catch (IOException ex) {
            System.out.println("No se pudo leer el trafico: " + ex.getClass().getSimpleName());
            return 1;
        }
    }

    private static int resumen(Path archivo, List<EventoInteraccion> eventos, UmbralesMonitoreo u) {
        ResumenOnline r = MotorMonitoreo.resumir(eventos, u);
        List<AlertaMonitoreo> alertas = MotorMonitoreo.alertas(r, u);
        System.out.println("Resumen · " + archivo.getFileName() + " · " + r.total() + " interacciones");
        System.out.println("  caminos: " + r.porCamino());
        linea("fidelidad promedio", String.format("%.2f", r.fidelidadPromedio()), r.fidelidadPromedio() >= u.fidelidadMinima());
        linea("frases prohibidas", Integer.toString(r.frasesProhibidas()), r.frasesProhibidas() <= u.frasesMaximo());
        linea("feedback negativo", String.format("%.0f%%", r.feedbackNegativo() * 100),
                r.calificadas() == 0 || r.feedbackNegativo() <= u.feedbackMaximo());
        linea("fuera de alcance", String.format("%.0f%%", r.fueraDeAlcance() * 100), r.fueraDeAlcance() <= u.fueraMaximo());
        linea("latencia p95", String.format("%.0f ms", r.latenciaP95Ms()), r.latenciaP95Ms() <= u.latenciaP95Maxima());
        String tokens = r.tokensPromedio() == null ? "sin dato" : String.format("%.0f", r.tokensPromedio());
        System.out.println("  tokens promedio (solo si el proveedor los informa): " + tokens);
        linea("usuarios sobre el limite", Integer.toString(r.usuariosSobreLimite().size()), r.usuariosSobreLimite().isEmpty());
        System.out.println(MotorMonitoreo.codigoSalida(alertas, u) == 0 ? "ESTADO: SIN ALERTAS QUE BLOQUEEN" : "ESTADO: HAY ALERTAS QUE BLOQUEAN");
        return 0;
    }

    private static int alertas(Path archivo, List<EventoInteraccion> eventos, UmbralesMonitoreo u) throws IOException {
        ResumenOnline r = MotorMonitoreo.resumir(eventos, u);
        List<AlertaMonitoreo> alertas = MotorMonitoreo.alertas(r, u);
        Path salida = archivo.getParent() == null
                ? Path.of("resultados")
                : archivo.getParent().resolveSibling("resultados");
        Files.createDirectories(salida);
        Path informe = salida.resolve("informe_" + nombre(archivo) + ".md");
        List<String> lineas = new ArrayList<>();
        lineas.add("# Informe de monitoreo · " + archivo.getFileName());
        lineas.add("");
        lineas.add("Interacciones: " + r.total());
        lineas.add("");
        lineas.add("| Severidad | Metrica | Observado | Umbral | Causa | Accion |");
        lineas.add("|---|---|---|---|---|---|");
        if (alertas.isEmpty()) {
            lineas.add("| — | sin alertas | — | — | — | — |");
        }
        for (AlertaMonitoreo a : alertas) {
            lineas.add("| " + a.severidad() + " | " + a.metrica() + " | " + a.observado()
                    + " | " + a.umbral() + " | " + a.causa() + " | " + a.accion() + " |");
            System.out.println("[" + a.severidad() + "] " + a.metrica() + ": " + a.observado()
                    + " (" + a.umbral() + ")");
            System.out.println("  causa: " + a.causa());
            System.out.println("  accion: " + a.accion());
        }
        int codigo = MotorMonitoreo.codigoSalida(alertas, u);
        lineas.add("");
        lineas.add(codigo == 0 ? "Codigo de salida: 0" : "Codigo de salida: 1");
        Files.write(informe, lineas);
        System.out.println(codigo == 0 ? "ESTADO: SIN ALERTAS QUE BLOQUEEN" : "ESTADO: " + alertas.size() + " ALERTAS");
        System.out.println("Informe: " + informe);
        return codigo;
    }

    private static int latencia(Path archivo, List<EventoInteraccion> eventos) {
        Map<String, List<EventoInteraccion>> porHora = new TreeMap<>();
        for (EventoInteraccion evento : eventos) {
            String hora = evento.ts().length() >= 13 ? evento.ts().substring(11, 13) + ":00" : "??:00";
            porHora.computeIfAbsent(hora, k -> new ArrayList<>()).add(evento);
        }
        System.out.println("Linea de tiempo · " + archivo.getFileName());
        System.out.println("hora  version  n  fidelidad  prohibidas  fuera  p95ms  bloqueos");
        UmbralesMonitoreo u = UmbralesMonitoreo.desdeClasspath();
        for (Map.Entry<String, List<EventoInteraccion>> entry : porHora.entrySet()) {
            List<EventoInteraccion> grupo = entry.getValue();
            ResumenOnline r = MotorMonitoreo.resumir(grupo, u);
            String versiones = grupo.stream().map(EventoInteraccion::version).distinct().sorted()
                    .reduce((a, b) -> a + "/" + b).orElse("");
            System.out.printf("%s  %-8s %2d  %8.2f  %10d  %5.0f%%  %6.0f  %d%n",
                    entry.getKey(), versiones, grupo.size(), r.fidelidadPromedio(), r.frasesProhibidas(),
                    r.fueraDeAlcance() * 100, r.latenciaP95Ms(), r.bloqueos());
        }
        return 0;
    }

    private static int casos(Path archivo, List<EventoInteraccion> eventos, UmbralesMonitoreo u) throws IOException {
        List<Map<String, Object>> candidatos = MotorMonitoreo.casosNuevos(eventos, u);
        Path salida = archivo.getParent() == null
                ? Path.of("resultados")
                : archivo.getParent().resolveSibling("resultados");
        Files.createDirectories(salida);
        Path destino = salida.resolve("candidatos_dataset.jsonl");
        List<String> lineas = new ArrayList<>();
        int i = 1;
        for (Map<String, Object> candidato : candidatos) {
            candidato.put("id", String.format("ONLINE-%03d", i++));
            lineas.add(MAPPER.writeValueAsString(candidato));
            System.out.println(candidato.get("id") + "  " + candidato.get("tipo") + "  " + candidato.get("motivo"));
        }
        Files.write(destino, lineas);
        System.out.println("Candidatos: " + candidatos.size() + " en " + destino);
        System.out.println("Una persona revisa y, si acepta, se promueve. No entran solos al dataset dorado.");
        return 0;
    }

    private static void linea(String nombre, String valor, boolean ok) {
        System.out.println("  " + nombre + ": " + valor + "  " + (ok ? "OK" : "ALERTA"));
    }

    private static String nombre(Path archivo) {
        String nombre = archivo.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? nombre : nombre.substring(0, punto);
    }
}
