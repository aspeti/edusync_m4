package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "estudiante")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstudianteJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "rude", nullable = false)
  private String rude;

  @Column(name = "nombre_completo", nullable = false)
  private String nombreCompleto;

  @Column(name = "estado", nullable = false)
  private String estado;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "datos_personales", columnDefinition = "jsonb")
  private Map<String, String> datosPersonales;

  public EstudianteJpaEntity(
      UUID id,
      UUID tenantId,
      String rude,
      String nombreCompleto,
      String estado,
      Map<String, String> datosPersonales) {
    this.id = id;
    this.tenantId = tenantId;
    this.rude = rude;
    this.nombreCompleto = nombreCompleto;
    this.estado = estado;
    this.datosPersonales = datosPersonales;
  }
}
