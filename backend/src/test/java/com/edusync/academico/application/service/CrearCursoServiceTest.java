package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearCursoCommand;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.domain.Curso;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearCursoServiceTest {

  private CursoRepositoryPort cursoRepositoryPort;
  private CrearCursoService service;

  @BeforeEach
  void setUp() {
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    service = new CrearCursoService(cursoRepositoryPort);
  }

  @Test
  void creaUnCurso() {
    when(cursoRepositoryPort.guardar(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

    Curso curso = service.crear(new CrearCursoCommand(UUID.randomUUID(), "Primero de Primaria"));

    assertThat(curso.getNombre()).isEqualTo("Primero de Primaria");
  }
}
