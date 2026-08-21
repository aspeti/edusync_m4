package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asignacion_materia_profesor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsignacionMateriaProfesorJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "materia_id", nullable = false)
  private UUID materiaId;

  @Column(name = "profesor_id", nullable = false)
  private UUID profesorId;

  @Column(name = "curso_id", nullable = false)
  private UUID cursoId;

  @Column(name = "paralelo_id", nullable = false)
  private UUID paraleloId;

  public AsignacionMateriaProfesorJpaEntity(
      UUID id, UUID tenantId, UUID materiaId, UUID profesorId, UUID cursoId, UUID paraleloId) {
    this.id = id;
    this.tenantId = tenantId;
    this.materiaId = materiaId;
    this.profesorId = profesorId;
    this.cursoId = cursoId;
    this.paraleloId = paraleloId;
  }
}
