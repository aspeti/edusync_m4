package com.edusync.shared.ai.application.monitoring;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Fidelidad léxica sin juez LLM: una oración está sustentada si sus cifras
 * aparecen en el contexto y al menos la mitad de sus palabras de contenido
 * también. Las frases de gestión, sin cifras, no se castigan.
 */
public final class FidelidadHeuristica {

    private static final Pattern SEPARADOR = Pattern.compile("[.!?¡¿]+");
    private static final Pattern PALABRA = Pattern.compile("[a-z0-9:]+");
    private static final Pattern NUMERO = Pattern.compile("\\d+(?::\\d+)?");

    private static final List<String> GESTION = List.of(
            "no puedo",
            "no tengo informacion",
            "te derivo",
            "no reconozco esa consulta");

    private static final Set<String> VACIAS = Set.of(
            "a", "al", "como", "con", "de", "del", "el", "en", "es", "esa", "ese",
            "la", "las", "lo", "los", "mas", "me", "mi", "no", "para", "pero", "por",
            "que", "se", "si", "sin", "su", "sus", "te", "tu", "tus", "un", "una",
            "y", "ya", "puedes", "puedo", "debes", "tienes", "tiene");

    private FidelidadHeuristica() {
    }

    public static String normalizar(String texto) {
        String base = texto == null ? "" : texto;
        String sinTildes = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinTildes.toLowerCase();
    }

    public static double puntaje(String respuesta, String contexto) {
        String ctx = normalizar(contexto);
        Set<String> palabrasCtx = palabras(ctx);
        List<String> oraciones = SEPARADOR.splitAsStream(respuesta == null ? "" : respuesta)
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList();
        if (oraciones.isEmpty()) {
            return 0.0;
        }
        int respaldadas = 0;
        for (String oracion : oraciones) {
            String o = normalizar(oracion);
            List<String> numeros = NUMERO.matcher(o).results().map(m -> m.group()).toList();
            boolean gestion = GESTION.stream().anyMatch(o::contains);
            if (gestion && numeros.isEmpty()) {
                respaldadas++;
                continue;
            }
            List<String> contenido = palabras(o).stream()
                    .filter(p -> !VACIAS.contains(p) && p.length() > 2)
                    .toList();
            double proporcion = contenido.isEmpty()
                    ? 1.0
                    : contenido.stream().filter(palabrasCtx::contains).count() / (double) contenido.size();
            boolean cifrasOk = numeros.stream().allMatch(ctx::contains);
            if (cifrasOk && proporcion >= 0.5) {
                respaldadas++;
            }
        }
        return respaldadas / (double) oraciones.size();
    }

    public static String fraseProhibida(String respuesta, List<String> frases) {
        String normalizada = normalizar(respuesta);
        for (String frase : frases) {
            String clave = normalizar(frase);
            if (!clave.isEmpty() && normalizada.contains(clave)) {
                return clave;
            }
        }
        return null;
    }

    private static Set<String> palabras(String texto) {
        return PALABRA.matcher(texto).results().map(m -> m.group()).collect(java.util.stream.Collectors.toSet());
    }
}
