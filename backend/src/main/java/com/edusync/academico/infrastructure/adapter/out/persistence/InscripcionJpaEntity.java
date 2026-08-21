package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inscripcion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InscripcionJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "estudiante_id", nullable = false)
  private UUID estudianteId;

  @Column(name = "gestion_escolar_id", nullable = false)
  private UUID gestionEscolarId;

  @Column(name = "curso_id", nullable = false)
  private UUID cursoId;

  @Column(name = "paralelo_id", nullable = false)
  private UUID paraleloId;

  @Column(name = "fecha_inscripcion", nullable = false)
  private LocalDate fechaInscripcion;

  @Column(name = "estado", nullable = false)
  private String estado;

  public InscripcionJpaEntity(
      UUID id,
      UUID tenantId,
      UUID estudianteId,
      UUID gestionEscolarId,
      UUID cursoId,
      UUID paraleloId,
      LocalDate fechaInscripcion,
      String estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.estudianteId = estudianteId;
    this.gestionEscolarId = gestionEscolarId;
    this.cursoId = cursoId;
    this.paraleloId = paraleloId;
    this.fechaInscripcion = fechaInscripcion;
    this.estado = estado;
  }
}
