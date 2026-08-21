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
@Table(name = "asignacion_materia_curso")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsignacionMateriaCursoJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "materia_id", nullable = false)
  private UUID materiaId;

  @Column(name = "curso_id", nullable = false)
  private UUID cursoId;

  @Column(name = "paralelo_id", nullable = false)
  private UUID paraleloId;

  public AsignacionMateriaCursoJpaEntity(
      UUID id, UUID tenantId, UUID materiaId, UUID cursoId, UUID paraleloId) {
    this.id = id;
    this.tenantId = tenantId;
    this.materiaId = materiaId;
    this.cursoId = cursoId;
    this.paraleloId = paraleloId;
  }
}
