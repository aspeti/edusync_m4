package com.edusync.academico.domain;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate del modulo {@code academico} ({@code DD-UC-013}, {@code FSD-UC-020}): identidad
 * de un estudiante del tenant, independiente de su matricula. Las inscripciones viven en el
 * aggregate {@link Inscripcion}, no como coleccion embebida ({@code BR-023}).
 *
 * <p>{@code rude} es obligatorio y unico por tenant ({@code BR-004}/{@code RB-01}). El valor
 * no se interpola en mensajes ni logs. Inmutable (constructor privado + factory {@link #crear}),
 * Lombok solo {@code @Getter}.
 */
@Getter
public final class Estudiante {

  private final EstudianteId id;
  private final UUID tenantId;
  private final String rude;
  private final String nombreCompleto;
  private final EstadoEstudiante estado;
  private final Map<String, String> datosPersonales;

  private Estudiante(
      EstudianteId id,
      UUID tenantId,
      String rude,
      String nombreCompleto,
      EstadoEstudiante estado,
      Map<String, String> datosPersonales) {
    this.id = id;
    this.tenantId = tenantId;
    this.rude = rude;
    this.nombreCompleto = nombreCompleto;
    this.estado = estado;
    this.datosPersonales = datosPersonales;
  }

  /**
   * Factory de alta ({@code FSD-UC-020}, paso 1). Si {@code estado} es nulo, nace {@code ACTIVO}.
   * {@code datosPersonales} nulo o vacio se persiste como mapa vacio inmutable.
   */
  public static Estudiante crear(
      EstudianteId id,
      UUID tenantId,
      String rude,
      String nombreCompleto,
      EstadoEstudiante estado,
      Map<String, String> datosPersonales) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(rude, "rude no puede ser nulo");
    Objects.requireNonNull(nombreCompleto, "nombreCompleto no puede ser nulo");
    EstadoEstudiante estadoEfectivo = estado == null ? EstadoEstudiante.ACTIVO : estado;
    Map<String, String> datos =
        datosPersonales == null || datosPersonales.isEmpty() ? Map.of() : Map.copyOf(datosPersonales);
    return new Estudiante(id, tenantId, rude, nombreCompleto, estadoEfectivo, datos);
  }

  /** Reconstruye un {@link Estudiante} ya persistido (sin repetir las validaciones de alta). */
  public static Estudiante reconstruir(
      EstudianteId id,
      UUID tenantId,
      String rude,
      String nombreCompleto,
      EstadoEstudiante estado,
      Map<String, String> datosPersonales) {
    Map<String, String> datos =
        datosPersonales == null || datosPersonales.isEmpty() ? Map.of() : Map.copyOf(datosPersonales);
    return new Estudiante(id, tenantId, rude, nombreCompleto, estado, datos);
  }
}
