package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code seccion_evaluacion} ({@code V10} + {@code V14} rangos).
 * Nunca se expone por API.
 */
@Entity
@Table(name = "seccion_evaluacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeccionEvaluacionJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "gestion_escolar_id", nullable = false)
  private UUID gestionEscolarId;

  @Column(name = "nombre", nullable = false, length = 50)
  private String nombre;

  @Column(name = "orden", nullable = false)
  private int orden;

  @Column(name = "nota", nullable = false, precision = 5, scale = 2)
  private BigDecimal nota;

  @Column(name = "rango_min", nullable = false, precision = 5, scale = 2)
  private BigDecimal rangoMin;

  @Column(name = "rango_max", nullable = false, precision = 5, scale = 2)
  private BigDecimal rangoMax;

  public SeccionEvaluacionJpaEntity(
      UUID id,
      UUID tenantId,
      UUID gestionEscolarId,
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
}
