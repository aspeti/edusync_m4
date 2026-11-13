package com.edusync.academico.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Composición de planilla ({@code DD-UC-033} / {@code FSD-UC-022}). */
public record PlanillaMateriaVista(
    UUID materiaId,
    String materiaNombre,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre,
    String cursoEtiqueta,
    UUID periodoId,
    String periodoNombre,
    String periodoEstado,
    boolean editable,
    List<Columna> columnas,
    List<Fila> filas) {

  public record Columna(
      UUID evaluacionId,
      String nombre,
      UUID seccionEvaluacionId,
      String seccionNombre,
      int seccionOrden,
      BigDecimal puntajeMaximo,
      LocalDate fecha) {}

  public record Fila(
      UUID estudianteId,
      String nombreCompleto,
      String rude,
      Map<UUID, BigDecimal> notas,
      Integer notaPeriodo) {}
}
