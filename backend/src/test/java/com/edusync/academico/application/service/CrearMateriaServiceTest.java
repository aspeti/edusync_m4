package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearMateriaCommand;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.Materia;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearMateriaServiceTest {

  private MateriaRepositoryPort materiaRepositoryPort;
  private CrearMateriaService service;

  @BeforeEach
  void setUp() {
    materiaRepositoryPort = mock(MateriaRepositoryPort.class);
    service = new CrearMateriaService(materiaRepositoryPort);
  }

  @Test
  void creaUnaMateria() {
    when(materiaRepositoryPort.guardar(any(Materia.class))).thenAnswer(inv -> inv.getArgument(0));

    Materia materia = service.crear(new CrearMateriaCommand(UUID.randomUUID(), "Matemáticas"));

    assertThat(materia.getNombre()).isEqualTo("Matemáticas");
  }
}
