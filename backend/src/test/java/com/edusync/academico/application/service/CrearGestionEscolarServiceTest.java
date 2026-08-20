package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.FechasInvalidasException;
import com.edusync.academico.domain.GestionEscolar;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearGestionEscolarServiceTest {

  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private CrearGestionEscolarService service;

  @BeforeEach
  void setUp() {
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    service = new CrearGestionEscolarService(gestionEscolarRepositoryPort);
  }

  @Test
  void creaUnaGestionEscolarEnPlanificacion() {
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));

    GestionEscolar gestionEscolar = service.crear(new CrearGestionEscolarCommand(
        UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)));

    assertThat(gestionEscolar.getNombre()).isEqualTo("2027");
    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.PLANIFICACION);
  }

  @Test
  void rechazaFechasInvalidas() {
    assertThatThrownBy(() -> service.crear(new CrearGestionEscolarCommand(
            UUID.randomUUID(), "2027", LocalDate.of(2027, 11, 30), LocalDate.of(2027, 2, 1))))
        .isInstanceOf(FechasInvalidasException.class);
  }
}
