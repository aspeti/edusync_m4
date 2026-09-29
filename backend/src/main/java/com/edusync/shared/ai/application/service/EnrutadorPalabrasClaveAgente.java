package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.DefinicionHerramientaAgente;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Camino KEYWORD (DD-UC-025 §2.5): primera coincidencia de frase gana;
 * 0 turnos de LLM. Sin fuzzy ni embeddings.
 */
@Component
public class EnrutadorPalabrasClaveAgente {

    private final CatalogoHerramientasAgente catalogo;

    public EnrutadorPalabrasClaveAgente(CatalogoHerramientasAgente catalogo) {
        this.catalogo = catalogo;
    }

    public Optional<DefinicionHerramientaAgente> resolver(String pregunta) {
        return resolverConArgumentos(pregunta).map(Match::definicion);
    }

    public Optional<Match> resolverConArgumentos(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            return Optional.empty();
        }
        String normalizada = normalizar(pregunta);
        return catalogo.todas().stream()
                .flatMap(def -> def.frasesKeyword().stream()
                        .map(frase -> new FraseCandidata(def, normalizar(frase))))
                .filter(c -> !c.frase().isBlank())
                .sorted(Comparator.comparingInt((FraseCandidata c) -> c.frase().length()).reversed())
                .filter(c -> normalizada.contains(c.frase()))
                .map(c -> new Match(c.def(), extraerArgumentos(c.def(), normalizada, c.frase())))
                .findFirst();
    }

    static Map<String, Object> extraerArgumentos(
            DefinicionHerramientaAgente definicion, String preguntaNormalizada, String frase) {
        String nombreOq = extraerSufijo(preguntaNormalizada, frase);
        nombreOq = nombreOq.replaceFirst("^con nombre\\s+", "");
        nombreOq = nombreOq.replaceFirst("^llamad[oa]\\s+", "");
        nombreOq = nombreOq.replaceFirst("^nombre\\s+", "");
        if (definicion.escritura()) {
            if (nombreOq.isBlank()) {
                return Map.of();
            }
            return Map.of("nombre", nombreOq);
        }
        boolean tieneQ = definicion.parametros().stream().anyMatch(p -> "q".equals(p.nombre()));
        if (tieneQ && !nombreOq.isBlank()) {
            return Map.of("q", nombreOq);
        }
        return Map.of();
    }

    static String extraerSufijo(String preguntaNormalizada, String frase) {
        int idx = preguntaNormalizada.indexOf(frase);
        if (idx < 0) {
            return "";
        }
        String resto = preguntaNormalizada.substring(idx + frase.length()).trim();
        resto = resto.replaceFirst("^llamad[oa]\\s+", "");
        resto = resto.replace("\"", "").trim();
        return resto;
    }

    static String normalizar(String texto) {
        String nfd = Normalizer.normalize(texto.toLowerCase(Locale.ROOT).trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "").replaceAll("[¿?¡!.,;:]+", " ").replaceAll("\\s+", " ").trim();
    }

    public record Match(DefinicionHerramientaAgente definicion, Map<String, Object> argumentos) {}

    private record FraseCandidata(DefinicionHerramientaAgente def, String frase) {}
}
