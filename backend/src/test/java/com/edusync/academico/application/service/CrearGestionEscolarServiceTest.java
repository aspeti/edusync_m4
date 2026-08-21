package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.FechasInvalidasException;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearGestionEscolarServiceTest {

  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;
  private CrearGestionEscolarService service;

  @BeforeEach
  void setUp() {
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    periodoEvaluacionRepositoryPort = mock(PeriodoEvaluacionRepositoryPort.class);
    seccionEvaluacionRepositoryPort = mock(SeccionEvaluacionRepositoryPort.class);
    service = new CrearGestionEscolarService(
        gestionEscolarRepositoryPort, periodoEvaluacionRepositoryPort, seccionEvaluacionRepositoryPort);
  }

  @Test
  void creaUnaGestionEscolarEnPlanificacionYSiembraTresPeriodosYCuatroSecciones() {
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));
    when(periodoEvaluacionRepositoryPort.guardar(any(PeriodoEvaluacion.class))).thenAnswer(inv -> inv.getArgument(0));
    when(seccionEvaluacionRepositoryPort.guardar(any(SeccionEvaluacion.class))).thenAnswer(inv -> inv.getArgument(0));

    GestionEscolar gestionEscolar = service.crear(new CrearGestionEscolarCommand(
        UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)));

    assertThat(gestionEscolar.getNombre()).isEqualTo("2027");
    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.PLANIFICACION);
    verify(periodoEvaluacionRepositoryPort, times(3)).guardar(any(PeriodoEvaluacion.class));
    verify(seccionEvaluacionRepositoryPort, times(4)).guardar(any(SeccionEvaluacion.class));
  }

  @Test
  void rechazaFechasInvalidas() {
    assertThatThrownBy(() -> service.crear(new CrearGestionEscolarCommand(
            UUID.randomUUID(), "2027", LocalDate.of(2027, 11, 30), LocalDate.of(2027, 2, 1))))
        .isInstanceOf(FechasInvalidasException.class);
  }
}
