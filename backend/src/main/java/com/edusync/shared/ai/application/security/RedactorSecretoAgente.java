package com.edusync.shared.ai.application.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Última capa: redacta secretos conocidos en la salida.
 * No cubre paráfrasis. El secreto, además, no debe viajar en el contexto.
 */
public final class RedactorSecretoAgente {

    /** Valor sintético de la demo. No es una credencial. */
    public static final String SECRETO_DEMO = "EDUSYNC-DEMO-8472";

    private RedactorSecretoAgente() {
    }

    public static String redactar(String texto, List<String> secretos) {
        String limpio = texto == null ? "" : texto;
        for (String secreto : secretos) {
            if (secreto == null || secreto.isBlank()) {
                continue;
            }
            if (limpio.toLowerCase().contains(secreto.toLowerCase())) {
                limpio = limpio.replaceAll("(?i)" + Pattern.quote(secreto), "[REDACTADO]");
            }
        }
        return limpio;
    }

    public static List<String> secretosDemo() {
        return new ArrayList<>(List.of(SECRETO_DEMO));
    }
}
