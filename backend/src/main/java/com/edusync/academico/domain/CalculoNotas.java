package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Motor puro de dominio ({@code DD-UC-018}, {@code ADR-0013} §3.4 / {@code BR-020}).
 * Modelo genérico: promedio simple + {@code round} HALF_UP. MUST NOT usar {@code floor()}.
 *
 * <p>Caso canónico: Saber 35+40 → 37.50; +Ser5/Hacer40/AE10 → periodo 93; N=3 → gestión 31.
 */
public final class CalculoNotas {

  private CalculoNotas() {}

  /**
   * Calcula la vista provisional de un estudiante en una materia para un periodo,
   * más el promedio de gestión sobre N periodos.
   *
   * @param seccionesPeriodo secciones de la gestión con los valores (solo evals ACTIVA
   *     con nota) del periodo solicitado
   * @param notasPeriodoCompletas tamaño = N; cada elemento es la nota del periodo
   *     (null → 0 en el promedio de gestión)
   */
  public static NotaProvisional calcular(
      List<SeccionCalculoInput> seccionesPeriodo, List<Integer> notasPeriodoCompletas) {
    Objects.requireNonNull(seccionesPeriodo, "seccionesPeriodo no puede ser nulo");
    Objects.requireNonNull(notasPeriodoCompletas, "notasPeriodoCompletas no puede ser nulo");
    if (notasPeriodoCompletas.isEmpty()) {
      throw new IllegalArgumentException("N periodos debe ser >= 1");
    }

    List<NotaSeccion> notasSeccion = new ArrayList<>();
    BigDecimal sumaCompletas = BigDecimal.ZERO;
    int completas = 0;
    for (SeccionCalculoInput seccion : seccionesPeriodo) {
      NotaSeccion nota = calcularSeccion(seccion);
      notasSeccion.add(nota);
      if (nota.estado() == EstadoSeccionNota.COMPLETO) {
        sumaCompletas = sumaCompletas.add(nota.notaSeccion());
        completas++;
      }
    }

    Integer notaPeriodo = null;
    if (completas > 0) {
      notaPeriodo = sumaCompletas.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    int n = notasPeriodoCompletas.size();
    BigDecimal sumaGestion = BigDecimal.ZERO;
    for (Integer nota : notasPeriodoCompletas) {
      sumaGestion = sumaGestion.add(BigDecimal.valueOf(nota == null ? 0 : nota));
    }
    int promedioGestion =
        sumaGestion
            .divide(BigDecimal.valueOf(n), 0, RoundingMode.HALF_UP)
            .intValueExact();

    return new NotaProvisional(notasSeccion, notaPeriodo, promedioGestion, "PROVISIONAL");
  }

  /** Nota de un periodo a partir de sus secciones (reutilizable para N periodos). */
  public static Integer calcularNotaPeriodo(List<SeccionCalculoInput> secciones) {
    Objects.requireNonNull(secciones, "secciones no puede ser nulo");
    BigDecimal sumaCompletas = BigDecimal.ZERO;
    int completas = 0;
    for (SeccionCalculoInput seccion : secciones) {
      NotaSeccion nota = calcularSeccion(seccion);
      if (nota.estado() == EstadoSeccionNota.COMPLETO) {
        sumaCompletas = sumaCompletas.add(nota.notaSeccion());
        completas++;
      }
    }
    if (completas == 0) {
      return null;
    }
    return sumaCompletas.setScale(0, RoundingMode.HALF_UP).intValueExact();
  }

  private static NotaSeccion calcularSeccion(SeccionCalculoInput seccion) {
    List<BigDecimal> valores = seccion.valores();
    if (valores == null || valores.isEmpty()) {
      return new NotaSeccion(
          seccion.seccionId(), seccion.nombre(), EstadoSeccionNota.INCOMPLETO, null);
    }
    BigDecimal suma = BigDecimal.ZERO;
    for (BigDecimal v : valores) {
      suma = suma.add(v);
    }
    BigDecimal promedio =
        suma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    return new NotaSeccion(
        seccion.seccionId(), seccion.nombre(), EstadoSeccionNota.COMPLETO, promedio);
  }

  /** Entrada por sección: valores solo de evaluaciones ACTIVA con nota (n≥1 → COMPLETO). */
  public record SeccionCalculoInput(
      UUID seccionId, String nombre, List<BigDecimal> valores) {

    public SeccionCalculoInput {
      Objects.requireNonNull(seccionId, "seccionId no puede ser nulo");
      Objects.requireNonNull(nombre, "nombre no puede ser nulo");
      valores = valores == null ? List.of() : List.copyOf(valores);
    }
  }

  public record NotaSeccion(
      UUID seccionId, String nombre, EstadoSeccionNota estado, BigDecimal notaSeccion) {}

  public record NotaProvisional(
      List<NotaSeccion> secciones,
      Integer notaPeriodo,
      int promedioGestion,
      String estado) {}
}
