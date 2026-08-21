package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearInscripcionCommand;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.EstadoEstudiante;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoEncontradoException;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.InscripcionDuplicadaException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ParaleloNoEncontradoException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearInscripcionServiceTest {

  private EstudianteRepositoryPort estudianteRepositoryPort;
  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private InscripcionRepositoryPort inscripcionRepositoryPort;
  private CrearInscripcionService service;

  @BeforeEach
  void setUp() {
    estudianteRepositoryPort = mock(EstudianteRepositoryPort.class);
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    inscripcionRepositoryPort = mock(InscripcionRepositoryPort.class);
    service =
        new CrearInscripcionService(
            estudianteRepositoryPort,
            gestionEscolarRepositoryPort,
            cursoRepositoryPort,
            paraleloRepositoryPort,
            inscripcionRepositoryPort);
  }

  @Test
  void creaCuandoLosPadresPertenecenAlTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID estudianteId = UUID.randomUUID();
    UUID gestionId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    stubPadresValidos(tenantId, estudianteId, gestionId, cursoId, paraleloId);
    when(inscripcionRepositoryPort.existePorEstudianteGestionYTenant(any(), any(), any())).thenReturn(false);
    when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

    Inscripcion inscripcion =
        service.crear(
            new CrearInscripcionCommand(
                tenantId, estudianteId, gestionId, cursoId, paraleloId, LocalDate.of(2026, 2, 1)));

    assertThat(inscripcion.getEstado().name()).isEqualTo("ACTIVA");
    assertThat(inscripcion.getEstudianteId().valor()).isEqualTo(estudianteId);
  }

  @Test
  void rechazaCon409CuandoYaExisteInscripcionEnLaMismaGestion() {
    UUID tenantId = UUID.randomUUID();
    UUID estudianteId = UUID.randomUUID();
    UUID gestionId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    stubPadresValidos(tenantId, estudianteId, gestionId, cursoId, paraleloId);
    when(inscripcionRepositoryPort.existePorEstudianteGestionYTenant(any(), any(), any())).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearInscripcionCommand(
                        tenantId, estudianteId, gestionId, cursoId, paraleloId, LocalDate.of(2026, 2, 1))))
        .isInstanceOf(InscripcionDuplicadaException.class)
        .satisfies(
            ex ->
                assertThat(((InscripcionDuplicadaException) ex).getErrorCode())
                    .isEqualTo("E_INSCRIPCION_DUPLICADA"));

    verify(inscripcionRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoElEstudianteNoExisteOEsDeOtroTenant() {
    when(estudianteRepositoryPort.buscarPorIdYTenant(any(EstudianteId.class), any(UUID.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearInscripcionCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 2, 1))))
        .isInstanceOf(EstudianteNoEncontradoException.class);

    verify(inscripcionRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoLaGestionNoExisteOEsDeOtroTenant() {
    UUID tenantId = UUID.randomUUID();
    when(estudianteRepositoryPort.buscarPorIdYTenant(any(EstudianteId.class), any(UUID.class)))
        .thenReturn(
            Optional.of(
                Estudiante.reconstruir(
                    EstudianteId.nueva(), tenantId, "12345678", "Ana Pérez", EstadoEstudiante.ACTIVO, Map.of())));
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(any(GestionEscolarId.class), any(UUID.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearInscripcionCommand(
                        tenantId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 2, 1))))
        .isInstanceOf(GestionEscolarNoEncontradaException.class);

    verify(inscripcionRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoElCursoNoExisteOEsDeOtroTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID estudianteId = UUID.randomUUID();
    UUID gestionId = UUID.randomUUID();
    stubEstudianteYGestion(tenantId, estudianteId, gestionId);
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearInscripcionCommand(
                        tenantId, estudianteId, gestionId, UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 2, 1))))
        .isInstanceOf(CursoNoEncontradoException.class);

    verify(inscripcionRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoElParaleloNoPerteneceAlCurso() {
    UUID tenantId = UUID.randomUUID();
    UUID estudianteId = UUID.randomUUID();
    UUID gestionId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID otroCursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    stubEstudianteYGestion(tenantId, estudianteId, gestionId);
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(any(ParaleloId.class), any(UUID.class)))
        .thenReturn(
            Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(otroCursoId), "A")));

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearInscripcionCommand(
                        tenantId, estudianteId, gestionId, cursoId, paraleloId, LocalDate.of(2026, 2, 1))))
        .isInstanceOf(ParaleloNoEncontradoException.class);

    verify(inscripcionRepositoryPort, never()).guardar(any());
  }

  private void stubPadresValidos(
      UUID tenantId, UUID estudianteId, UUID gestionId, UUID cursoId, UUID paraleloId) {
    stubEstudianteYGestion(tenantId, estudianteId, gestionId);
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(any(ParaleloId.class), any(UUID.class)))
        .thenReturn(Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(cursoId), "A")));
  }

  private void stubEstudianteYGestion(UUID tenantId, UUID estudianteId, UUID gestionId) {
    when(estudianteRepositoryPort.buscarPorIdYTenant(any(EstudianteId.class), any(UUID.class)))
        .thenReturn(
            Optional.of(
                Estudiante.reconstruir(
                    EstudianteId.de(estudianteId),
                    tenantId,
                    "12345678",
                    "Ana Pérez",
                    EstadoEstudiante.ACTIVO,
                    Map.of())));
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(any(GestionEscolarId.class), any(UUID.class)))
        .thenReturn(
            Optional.of(
                GestionEscolar.reconstruir(
                    GestionEscolarId.de(gestionId),
                    tenantId,
                    "2026",
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 11, 30),
                    EstadoGestionEscolar.PLANIFICACION)));
  }
}
