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
@Table(name = "materia")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MateriaJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  public MateriaJpaEntity(UUID id, UUID tenantId, String nombre) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombre = nombre;
  }
}
