package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente de {@link GestionEscolar} ({@code DD-UC-016}, {@code FSD-UC-014}):
 * una seccion de la plantilla de la gestion (no del periodo). {@code nota} es el peso en
 * puntos ({@code ADR-0013}); la suma 100 y el freeze se validan en aplicacion.
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

  private SeccionEvaluacion(
      SeccionEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      int orden,
      BigDecimal nota) {
    this.id = id;
    this.tenantId = tenantId;
    this.gestionEscolarId = gestionEscolarId;
    this.nombre = nombre;
    this.orden = orden;
    this.nota = nota;
  }

  public static SeccionEvaluacion crear(
      SeccionEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      int orden,
      BigDecimal nota) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(gestionEscolarId, "gestionEscolarId no puede ser nulo");
    String nombreNormalizado = validarNombre(nombre);
    BigDecimal notaNormalizada = validarNota(nota);
    if (orden < 1) {
      throw new IllegalArgumentException("orden debe ser >= 1");
    }
    return new SeccionEvaluacion(id, tenantId, gestionEscolarId, nombreNormalizado, orden, notaNormalizada);
  }

  public static SeccionEvaluacion reconstruir(
      SeccionEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      int orden,
      BigDecimal nota) {
    return new SeccionEvaluacion(id, tenantId, gestionEscolarId, nombre, orden, nota);
  }

  public void actualizar(String nombre, BigDecimal nota) {
    this.nombre = validarNombre(nombre);
    this.nota = validarNota(nota);
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
}
