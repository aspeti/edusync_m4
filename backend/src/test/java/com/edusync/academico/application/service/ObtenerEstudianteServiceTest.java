package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.domain.EstadoEstudiante;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoEncontradoException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObtenerEstudianteServiceTest {

  private EstudianteRepositoryPort estudianteRepositoryPort;
  private ObtenerEstudianteService service;

  @BeforeEach
  void setUp() {
    estudianteRepositoryPort = mock(EstudianteRepositoryPort.class);
    service = new ObtenerEstudianteService(estudianteRepositoryPort);
  }

  @Test
  void devuelveElEstudianteDelTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID estudianteId = UUID.randomUUID();
    Estudiante estudiante =
        Estudiante.reconstruir(
            EstudianteId.de(estudianteId), tenantId, "12345678", "Ana Pérez", EstadoEstudiante.ACTIVO, Map.of());
    when(estudianteRepositoryPort.buscarPorIdYTenant(any(EstudianteId.class), any(UUID.class)))
        .thenReturn(Optional.of(estudiante));

    assertThat(service.obtener(tenantId, estudianteId).getNombreCompleto()).isEqualTo("Ana Pérez");
  }

  @Test
  void rechazaCuandoNoExisteOEsDeOtroTenant() {
    when(estudianteRepositoryPort.buscarPorIdYTenant(any(EstudianteId.class), any(UUID.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtener(UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(EstudianteNoEncontradoException.class);
  }
}
