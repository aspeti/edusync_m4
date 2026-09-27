package com.edusync.academico.application.service;

import java.text.Normalizer;
import java.util.Locale;

/** Comparación de menciones en lenguaje natural vs. nombres persistidos (sin tildes). */
public final class NormalizacionTexto {

  private NormalizacionTexto() {}

  public static String normalizar(String texto) {
    if (texto == null) {
      return "";
    }
    String nfd = Normalizer.normalize(texto.toLowerCase(Locale.ROOT).trim(), Normalizer.Form.NFD);
    return nfd.replaceAll("\\p{M}+", "").replaceAll("[¿?¡!.,;:]+", " ").replaceAll("\\s+", " ").trim();
  }

  public static boolean contiene(String haystack, String needle) {
    String h = normalizar(haystack);
    String n = normalizar(needle);
    return !n.isBlank() && h.contains(n);
  }
}
