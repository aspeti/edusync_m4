package com.edusync.academico.application.port.in;

import java.util.List;
import java.util.UUID;

/** Resuelve una mención en lenguaje natural a entidades del tenant (ADR-0020). */
public interface ResolverEntidadAcademicaUseCase {

  Resultado resolver(UUID tenantId, String tipo, String q);

  enum Tipo {
    ESTUDIANTE,
    MATERIA,
    PERIODO,
    PROFESOR,
    CURSO_PARALELO
  }

  record Match(UUID id, String etiqueta, UUID cursoId, UUID paraleloId) {}

  record Resultado(String estado, String tipo, List<Match> matches) {
    public static final String UNICO = "UNICO";
    public static final String AMBIGUO = "AMBIGUO";
    public static final String NINGUNO = "NINGUNO";

    public Resultado {
      matches = matches == null ? List.of() : List.copyOf(matches);
    }

    public static Resultado de(String tipo, List<Match> matches) {
      List<Match> lista = matches == null ? List.of() : matches;
      String estado =
          lista.isEmpty() ? NINGUNO : (lista.size() == 1 ? UNICO : AMBIGUO);
      return new Resultado(estado, tipo, lista);
    }
  }
}
