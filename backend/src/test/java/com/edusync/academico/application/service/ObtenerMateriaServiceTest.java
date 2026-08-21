package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObtenerMateriaServiceTest {

  private MateriaRepositoryPort materiaRepositoryPort;
  private ObtenerMateriaService service;

  @BeforeEach
  void setUp() {
    materiaRepositoryPort = mock(MateriaRepositoryPort.class);
    service = new ObtenerMateriaService(materiaRepositoryPort);
  }

  @Test
  void devuelveLaMateriaDelTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID materiaId = UUID.randomUUID();
    Materia materia = Materia.reconstruir(MateriaId.de(materiaId), tenantId, "Matemáticas");
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.of(materia));

    assertThat(service.obtener(tenantId, materiaId).getNombre()).isEqualTo("Matemáticas");
  }

  @Test
  void rechazaCuandoNoExisteOEsDeOtroTenant() {
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtener(UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(MateriaNoEncontradaException.class);
  }
}
