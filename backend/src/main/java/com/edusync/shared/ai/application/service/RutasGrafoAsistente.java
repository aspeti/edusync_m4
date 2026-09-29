package com.edusync.shared.ai.application.service;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Aristas del grafo de oleada 1 (ADR-0021). Clasificador Java: no gasta tokens.
 * Orden: KEYWORD → CONSULTA → PROCESO → SALUDO → REACT/NINGUNO.
 */
public final class RutasGrafoAsistente {

    public static final String NODO_GUARDRAIL_ENTRADA = "guardrail_entrada";
    public static final String NODO_GUARDRAIL_SALIDA = "guardrail_salida";
    public static final String NODO_CLASIFICAR = "clasificar";
    public static final String NODO_SALUDO = "saludo";
    public static final String NODO_RECUPERAR = "recuperar";

    private static final Pattern SALUDO = Pattern.compile(
            "^(hola|buenas|buen[oa]s?\\s+(dias|días|tardes|noches)|hey|hello|buenas\\s+noches)\\b.*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public enum Nodo {
        BLOQUEAR,
        KEYWORD,
        CONSULTA,
        PROCESO,
        SALUDO,
        REACT,
        NINGUNO
    }

    private RutasGrafoAsistente() {
    }

    public static Nodo siguiente(
            boolean entradaOk,
            boolean hayKeyword,
            boolean hayConsulta,
            boolean hayProceso,
            boolean esSaludo,
            boolean llmHabilitado) {
        if (!entradaOk) {
            return Nodo.BLOQUEAR;
        }
        if (hayKeyword) {
            return Nodo.KEYWORD;
        }
        if (hayConsulta) {
            return Nodo.CONSULTA;
        }
        if (hayProceso) {
            return Nodo.PROCESO;
        }
        if (esSaludo) {
            return Nodo.SALUDO;
        }
        return llmHabilitado ? Nodo.REACT : Nodo.NINGUNO;
    }

    public static boolean esSaludo(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            return false;
        }
        String n = Normalizer.normalize(pregunta.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase();
        return SALUDO.matcher(n).matches();
    }
}
