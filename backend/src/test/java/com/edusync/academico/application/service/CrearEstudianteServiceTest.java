package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearEstudianteCommand;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.domain.EstadoEstudiante;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.RudeDuplicadoException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearEstudianteServiceTest {

  private EstudianteRepositoryPort estudianteRepositoryPort;
  private CrearEstudianteService service;

  @BeforeEach
  void setUp() {
    estudianteRepositoryPort = mock(EstudianteRepositoryPort.class);
    service = new CrearEstudianteService(estudianteRepositoryPort);
  }

  @Test
  void creaUnEstudianteCuandoElRudeEsUnicoEnElTenant() {
    when(estudianteRepositoryPort.existePorRudeYTenant(any(), any())).thenReturn(false);
    when(estudianteRepositoryPort.guardar(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));

    Estudiante estudiante =
        service.crear(
            new CrearEstudianteCommand(
                UUID.randomUUID(), "12345678", "Ana Pérez", EstadoEstudiante.ACTIVO, null));

    assertThat(estudiante.getEstado()).isEqualTo(EstadoEstudiante.ACTIVO);
    assertThat(estudiante.getNombreCompleto()).isEqualTo("Ana Pérez");
  }

  @Test
  void rechazaCon409CuandoElRudeYaExisteEnElTenant() {
    when(estudianteRepositoryPort.existePorRudeYTenant(any(), any())).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearEstudianteCommand(
                        UUID.randomUUID(), "12345678", "Ana Pérez", EstadoEstudiante.ACTIVO, null)))
        .isInstanceOf(RudeDuplicadoException.class)
        .satisfies(
            ex -> {
              assertThat(((RudeDuplicadoException) ex).getErrorCode()).isEqualTo("E_RUDE_DUPLICADO");
              assertThat(ex.getMessage()).doesNotContain("12345678");
            });

    verify(estudianteRepositoryPort, never()).guardar(any());
  }
}
