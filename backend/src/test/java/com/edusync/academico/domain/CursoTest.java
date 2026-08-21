package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Alta de {@link Curso} ({@code FSD-UC-017}, {@code DD-UC-010}). */
class CursoTest {

  @Test
  void creaUnCursoConNombre() {
    Curso curso = Curso.crear(CursoId.nueva(), UUID.randomUUID(), "Primero de Primaria");

    assertThat(curso.getNombre()).isEqualTo("Primero de Primaria");
  }

  @Test
  void dosCursosConMismoIdSonIguales() {
    CursoId id = CursoId.nueva();
    UUID tenantId = UUID.randomUUID();

    Curso a = Curso.reconstruir(id, tenantId, "A");
    Curso b = Curso.reconstruir(id, tenantId, "A");

    assertThat(a.getId()).isEqualTo(b.getId());
  }
}
