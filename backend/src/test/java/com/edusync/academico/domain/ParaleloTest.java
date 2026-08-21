package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Alta de {@link Paralelo} ({@code FSD-UC-017}, {@code DD-UC-010}). */
class ParaleloTest {

  @Test
  void creaUnParaleloReferenciandoSuCursoPadre() {
    CursoId cursoId = CursoId.nueva();
    UUID tenantId = UUID.randomUUID();

    Paralelo paralelo = Paralelo.crear(ParaleloId.nueva(), tenantId, cursoId, "A");

    assertThat(paralelo.getNombre()).isEqualTo("A");
    assertThat(paralelo.getCursoId()).isEqualTo(cursoId);
    assertThat(paralelo.getTenantId()).isEqualTo(tenantId);
  }
}
