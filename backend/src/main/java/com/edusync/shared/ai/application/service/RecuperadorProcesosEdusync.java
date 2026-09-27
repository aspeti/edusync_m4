package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.ResultadoRecuperacionProceso;
import com.edusync.shared.ai.domain.ResultadoRecuperacionProceso.FragmentoProceso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RAG léxico sobre procesos EduSync (DD-UC-028). Sin embeddings externos.
 */
@Component
public class RecuperadorProcesosEdusync {

    private static final Logger log = LoggerFactory.getLogger(RecuperadorProcesosEdusync.class);
    private static final String CLASSPATH_DIR = "/ai/base_conocimiento/";
    private static final List<String> ARCHIVOS = List.of(
            "alcance_asistente.md",
            "calculo_notas.md",
            "periodos_secciones.md",
            "roles_tenants.md",
            "estudiantes_cursos.md",
            "exportacion_sie.md");
    private static final Set<String> STOP = Set.of(
            "el", "la", "los", "las", "un", "una", "de", "del", "en", "y", "o", "a", "que",
            "se", "por", "con", "para", "es", "al", "lo", "su", "sus", "como");
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern PREGUNTA_PROCESO = Pattern.compile(
            "(como|cómo|que es|qué es|quien puede|quién puede|\\brude\\b|consolid"
                    + "|exportacion|exportación|\\bsie\\b|periodo cerrado|floor"
                    + "|calculo de notas|cálculo de notas)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final List<FragmentoProceso> corpus;

    public RecuperadorProcesosEdusync() {
        this.corpus = cargarCorpus();
    }

    public boolean esPreguntaDeProceso(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            return false;
        }
        String n = normalizar(pregunta);
        return PREGUNTA_PROCESO.matcher(n).find();
    }

    public ResultadoRecuperacionProceso recuperar(String pregunta, int k) {
        Set<String> qTokens = tokens(pregunta);
        if (qTokens.isEmpty() || corpus.isEmpty()) {
            return new ResultadoRecuperacionProceso(List.of(), 0);
        }
        List<FragmentoProceso> scored = new ArrayList<>();
        for (FragmentoProceso frag : corpus) {
            int score = 0;
            Set<String> docTokens = tokens(frag.texto() + " " + frag.fuente());
            for (String q : qTokens) {
                if (docTokens.contains(q) || prefijoComun(q, docTokens)) {
                    score++;
                }
            }
            if (score > 0) {
                scored.add(new FragmentoProceso(frag.fuente(), frag.texto(), score));
            }
        }
        scored.sort(Comparator.comparingInt(FragmentoProceso::puntuacion).reversed());
        int top = Math.max(1, k);
        List<FragmentoProceso> best = scored.stream().limit(top).toList();
        int max = best.isEmpty() ? 0 : best.getFirst().puntuacion();
        return new ResultadoRecuperacionProceso(best, max);
    }

    public String formatear(ResultadoRecuperacionProceso resultado) {
        if (!resultado.hayMatch()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Según los procesos de EduSync:\n");
        Set<String> fuentes = new LinkedHashSet<>();
        for (FragmentoProceso f : resultado.fragmentos()) {
            if (f.puntuacion() < ResultadoRecuperacionProceso.RecuperacionUmbral.MINIMO) {
                continue;
            }
            sb.append('\n').append(f.texto().trim()).append('\n');
            fuentes.add(f.fuente());
        }
        if (fuentes.isEmpty() && !resultado.fragmentos().isEmpty()) {
            FragmentoProceso f = resultado.fragmentos().getFirst();
            sb.append('\n').append(f.texto().trim()).append('\n');
            fuentes.add(f.fuente());
        }
        sb.append("\nFuente: ").append(String.join(", ", fuentes));
        return sb.toString().trim();
    }

    private List<FragmentoProceso> cargarCorpus() {
        List<FragmentoProceso> frags = new ArrayList<>();
        for (String archivo : ARCHIVOS) {
            String texto = leer(archivo);
            if (texto.isBlank()) {
                continue;
            }
            for (String parrafo : texto.split("\\n\\s*\\n")) {
                String limpio = parrafo.replace('#', ' ').trim();
                if (limpio.length() < 40) {
                    continue;
                }
                frags.add(new FragmentoProceso(archivo, limpio, 0));
            }
        }
        log.debug("Corpus procesos EduSync fragmentos={}", frags.size());
        return List.copyOf(frags);
    }

    private String leer(String archivo) {
        try (InputStream in = RecuperadorProcesosEdusync.class.getResourceAsStream(CLASSPATH_DIR + archivo)) {
            if (in == null) {
                log.warn("Corpus ausente en classpath: {}", archivo);
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("No se pudo leer corpus {}", archivo);
            return "";
        }
    }

    static Set<String> tokens(String texto) {
        Set<String> out = new LinkedHashSet<>();
        String n = normalizar(texto);
        var m = TOKEN.matcher(n);
        while (m.find()) {
            String t = m.group();
            if (t.length() < 4 || STOP.contains(t)) {
                continue;
            }
            out.add(t);
        }
        return out;
    }

    private static boolean prefijoComun(String q, Set<String> docTokens) {
        if (q.length() < 5) {
            return false;
        }
        String pref = q.substring(0, 5);
        for (String t : docTokens) {
            if (t.length() >= 5 && t.startsWith(pref)) {
                return true;
            }
        }
        return false;
    }

    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    /** Visible para tests: lista de archivos del corpus institucional. */
    List<String> archivosCargados() {
        return ARCHIVOS;
    }
}
