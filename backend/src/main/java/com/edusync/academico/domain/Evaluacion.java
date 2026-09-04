package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente ({@code DD-UC-017}, {@code FSD-UC-015}): una evaluacion en
 * ({@link Materia} × {@link PeriodoEvaluacion} × {@link SeccionEvaluacion}).
 * {@code puntajeMaximo} es un snapshot de {@code seccion.nota}; el cliente no lo envia.
 *
 * <p>Lombok solo {@code @Getter} (mismo criterio que {@link SeccionEvaluacion}).
 */
@Getter
public final class Evaluacion {

  static final int NOMBRE_MAX = 100;

  private final EvaluacionId id;
  private final UUID tenantId;
  private final MateriaId materiaId;
  private final PeriodoEvaluacionId periodoEvaluacionId;
  private final SeccionEvaluacionId seccionEvaluacionId;
  private final BigDecimal puntajeMaximo;
  private String nombre;
  private LocalDate fecha;
  private String descripcion;
  private EstadoEvaluacion estado;

  private Evaluacion(
      EvaluacionId id,
      UUID tenantId,
      MateriaId materiaId,
      PeriodoEvaluacionId periodoEvaluacionId,
      SeccionEvaluacionId seccionEvaluacionId,
      String nombre,
      LocalDate fecha,
      BigDecimal puntajeMaximo,
      String descripcion,
      EstadoEvaluacion estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.materiaId = materiaId;
    this.periodoEvaluacionId = periodoEvaluacionId;
    this.seccionEvaluacionId = seccionEvaluacionId;
    this.nombre = nombre;
    this.fecha = fecha;
    this.puntajeMaximo = puntajeMaximo;
    this.descripcion = descripcion;
    this.estado = estado;
  }

  public static Evaluacion crear(
      EvaluacionId id,
      UUID tenantId,
      MateriaId materiaId,
      PeriodoEvaluacionId periodoEvaluacionId,
      SeccionEvaluacionId seccionEvaluacionId,
      String nombre,
      LocalDate fecha,
      BigDecimal puntajeMaximo,
      String descripcion) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(materiaId, "materiaId no puede ser nulo");
    Objects.requireNonNull(periodoEvaluacionId, "periodoEvaluacionId no puede ser nulo");
    Objects.requireNonNull(seccionEvaluacionId, "seccionEvaluacionId no puede ser nulo");
    Objects.requireNonNull(fecha, "fecha no puede ser nula");
    return new Evaluacion(
        id,
        tenantId,
        materiaId,
        periodoEvaluacionId,
        seccionEvaluacionId,
        validarNombre(nombre),
        fecha,
        validarPuntajeMaximo(puntajeMaximo),
        normalizarDescripcion(descripcion),
        EstadoEvaluacion.ACTIVA);
  }

  public static Evaluacion reconstruir(
      EvaluacionId id,
      UUID tenantId,
      MateriaId materiaId,
      PeriodoEvaluacionId periodoEvaluacionId,
      SeccionEvaluacionId seccionEvaluacionId,
      String nombre,
      LocalDate fecha,
      BigDecimal puntajeMaximo,
      String descripcion,
      EstadoEvaluacion estado) {
    return new Evaluacion(
        id,
        tenantId,
        materiaId,
        periodoEvaluacionId,
        seccionEvaluacionId,
        nombre,
        fecha,
        puntajeMaximo,
        descripcion,
        estado);
  }

  public void actualizarDatos(String nombre, LocalDate fecha, String descripcion) {
    if (estado == EstadoEvaluacion.ANULADA) {
      throw new EvaluacionYaAnuladaException();
    }
    Objects.requireNonNull(fecha, "fecha no puede ser nula");
    this.nombre = validarNombre(nombre);
    this.fecha = fecha;
    this.descripcion = normalizarDescripcion(descripcion);
  }

  public void anular() {
    if (estado == EstadoEvaluacion.ANULADA) {
      throw new EvaluacionYaAnuladaException();
    }
    this.estado = EstadoEvaluacion.ANULADA;
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

  private static BigDecimal validarPuntajeMaximo(BigDecimal puntajeMaximo) {
    Objects.requireNonNull(puntajeMaximo, "puntajeMaximo no puede ser nulo");
    if (puntajeMaximo.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("puntajeMaximo debe ser > 0");
    }
    return puntajeMaximo.setScale(2, RoundingMode.HALF_UP);
  }

  private static String normalizarDescripcion(String descripcion) {
    if (descripcion == null) {
      return null;
    }
    String trimmed = descripcion.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
