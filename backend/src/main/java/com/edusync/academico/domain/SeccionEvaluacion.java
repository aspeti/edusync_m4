package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente de {@link GestionEscolar} ({@code DD-UC-016}/{@code DD-UC-034},
 * {@code FSD-UC-014}): plantilla de la gestion (no del periodo). {@code nota} es el peso /
 * escala del motor ({@code ADR-0013}); {@code rangoMin}/{@code rangoMax} delimitan la carga
 * de calificaciones ({@code ADR-0027}).
 *
 * <p>Lombok solo {@code @Getter} (mismo criterio que {@link PeriodoEvaluacion}).
 */
@Getter
public final class SeccionEvaluacion {

  static final int NOMBRE_MAX = 50;
  static final BigDecimal NOTA_MAX = new BigDecimal("100");

  private final SeccionEvaluacionId id;
  private final UUID tenantId;
  private final GestionEscolarId gestionEscolarId;
  private String nombre;
  private int orden;
  private BigDecimal nota;
  private BigDecimal rangoMin;
  private BigDecimal rangoMax;

  private SeccionEvaluacion(
      SeccionEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      int orden,
      BigDecimal nota,
      BigDecimal rangoMin,
      BigDecimal rangoMax) {
    this.id = id;
    this.tenantId = tenantId;
    this.gestionEscolarId = gestionEscolarId;
    this.nombre = nombre;
    this.orden = orden;
    this.nota = nota;
    this.rangoMin = rangoMin;
    this.rangoMax = rangoMax;
  }

  public static SeccionEvaluacion crear(
      SeccionEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      int orden,
      BigDecimal nota,
      BigDecimal rangoMin,
      BigDecimal rangoMax) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(gestionEscolarId, "gestionEscolarId no puede ser nulo");
    String nombreNormalizado = validarNombre(nombre);
    BigDecimal notaNormalizada = validarNota(nota);
    if (orden < 1) {
      throw new IllegalArgumentException("orden debe ser >= 1");
    }
    BigDecimal[] rangos = validarRangos(rangoMin, rangoMax, notaNormalizada);
    return new SeccionEvaluacion(
        id, tenantId, gestionEscolarId, nombreNormalizado, orden, notaNormalizada, rangos[0], rangos[1]);
  }

  public static SeccionEvaluacion reconstruir(
      SeccionEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      int orden,
      BigDecimal nota,
      BigDecimal rangoMin,
      BigDecimal rangoMax) {
    return new SeccionEvaluacion(
        id, tenantId, gestionEscolarId, nombre, orden, nota, rangoMin, rangoMax);
  }

  public void actualizar(String nombre, BigDecimal nota, BigDecimal rangoMin, BigDecimal rangoMax) {
    this.nombre = validarNombre(nombre);
    this.nota = validarNota(nota);
    BigDecimal[] rangos = validarRangos(rangoMin, rangoMax, this.nota);
    this.rangoMin = rangos[0];
    this.rangoMax = rangos[1];
  }

  private static String validarNombre(String nombre) {
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    String trimmed = nombre.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("nombre no puede estar en blanco");
    }
    if (trimmed.length() > NOMBRE_MAX) {
      throw new IllegalArgumentException("nombre no puede superar " + NOMBRE_MAX + " caracteres");
    }
    return trimmed;
  }

  private static BigDecimal validarNota(BigDecimal nota) {
    Objects.requireNonNull(nota, "nota no puede ser nula");
    if (nota.compareTo(BigDecimal.ZERO) <= 0 || nota.compareTo(NOTA_MAX) > 0) {
      throw new PesoInvalidoException();
    }
    return nota.setScale(2, RoundingMode.HALF_UP);
  }

  /** @return {@code [rangoMin, rangoMax]} normalizados */
  private static BigDecimal[] validarRangos(BigDecimal rangoMin, BigDecimal rangoMax, BigDecimal nota) {
    Objects.requireNonNull(rangoMin, "rangoMin no puede ser nulo");
    Objects.requireNonNull(rangoMax, "rangoMax no puede ser nulo");
    BigDecimal min = rangoMin.setScale(2, RoundingMode.HALF_UP);
    BigDecimal max = rangoMax.setScale(2, RoundingMode.HALF_UP);
    if (min.compareTo(BigDecimal.ZERO) < 0
        || max.compareTo(min) <= 0
        || max.compareTo(nota) > 0) {
      throw new ParametroRangoInvalidoException();
    }
    return new BigDecimal[] {min, max};
  }
}
