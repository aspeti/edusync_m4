package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MateriaTest {

  @Test
  void creaUnaMateriaConNombre() {
    Materia materia = Materia.crear(MateriaId.nueva(), UUID.randomUUID(), "Matemáticas");

    assertThat(materia.getNombre()).isEqualTo("Matemáticas");
  }

  @Test
  void dosMateriasConMismoIdSonIguales() {
    MateriaId id = MateriaId.nueva();
    UUID tenantId = UUID.randomUUID();

    Materia a = Materia.reconstruir(id, tenantId, "A");
    Materia b = Materia.reconstruir(id, tenantId, "A");

    assertThat(a.getId()).isEqualTo(b.getId());
  }
}
