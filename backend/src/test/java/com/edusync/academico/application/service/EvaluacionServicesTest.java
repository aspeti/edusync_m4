package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearEvaluacionCommand;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.AsignacionMateriaProfesorId;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import com.edusync.academico.domain.MateriaSinProfesorException;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoAbiertoException;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import com.edusync.academico.domain.SeccionNoPerteneceAGestionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EvaluacionServicesTest {

  private MateriaRepositoryPort materiaPort;
  private AsignacionMateriaProfesorRepositoryPort asignacionPort;
  private PeriodoEvaluacionRepositoryPort periodoPort;
  private SeccionEvaluacionRepositoryPort seccionPort;
  private EvaluacionRepositoryPort evaluacionPort;
  private CrearEvaluacionService crearService;

  private final UUID tenantId = UUID.randomUUID();
  private final UUID actorAdmin = UUID.randomUUID();
  private final UUID actorProfesor = UUID.randomUUID();
  private final UUID otroProfesor = UUID.randomUUID();
  private final MateriaId materiaId = MateriaId.nueva();
  private final GestionEscolarId gestionId = GestionEscolarId.nueva();
  private final PeriodoEvaluacionId periodoId = PeriodoEvaluacionId.nueva();
  private final SeccionEvaluacionId seccionId = SeccionEvaluacionId.nueva();

  @BeforeEach
  void setUp() {
    materiaPort = mock(MateriaRepositoryPort.class);
    asignacionPort = mock(AsignacionMateriaProfesorRepositoryPort.class);
    periodoPort = mock(PeriodoEvaluacionRepositoryPort.class);
    seccionPort = mock(SeccionEvaluacionRepositoryPort.class);
    evaluacionPort = mock(EvaluacionRepositoryPort.class);
    MateriaAccesoService acceso = new MateriaAccesoService(materiaPort, asignacionPort);
    crearService = new CrearEvaluacionService(acceso, periodoPort, seccionPort, evaluacionPort);
    when(evaluacionPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void postFelizCopiaPuntajeMaximoDeSaber() {
    stubMateriaConProfesor(actorProfesor);
    stubPeriodo(EstadoPeriodoEvaluacion.ABIERTO);
    stubSeccion(gestionId, new BigDecimal("45"));

    Evaluacion creada = crearService.crear(comando(actorAdmin, true));

    assertThat(creada.getPuntajeMaximo()).isEqualByComparingTo("45.00");
    assertThat(creada.getNombre()).isEqualTo("Prueba escrita 1");
    ArgumentCaptor<Evaluacion> captor = ArgumentCaptor.forClass(Evaluacion.class);
    verify(evaluacionPort).guardar(captor.capture());
    assertThat(captor.getValue().getPuntajeMaximo()).isEqualByComparingTo("45.00");
  }

  @Test
  void materiaSinProfesorRechazaCon409() {
    when(materiaPort.buscarPorIdYTenant(materiaId, tenantId)).thenReturn(Optional.of(materia()));
    when(asignacionPort.listarPorMateriaYTenant(materiaId, tenantId)).thenReturn(List.of());

    assertThatThrownBy(() -> crearService.crear(comando(actorAdmin, true)))
        .isInstanceOf(MateriaSinProfesorException.class)
        .satisfies(ex -> assertThat(((MateriaSinProfesorException) ex).getErrorCode())
            .isEqualTo("E_MATERIA_SIN_PROFESOR"));
    verify(evaluacionPort, never()).guardar(any());
  }

  @Test
  void periodoPendienteRechaza() {
    stubMateriaConProfesor(actorProfesor);
    stubPeriodo(EstadoPeriodoEvaluacion.PENDIENTE);
    stubSeccion(gestionId, new BigDecimal("45"));

    assertThatThrownBy(() -> crearService.crear(comando(actorAdmin, true)))
        .isInstanceOf(PeriodoNoAbiertoException.class);
  }

  @Test
  void periodoCerradoRechaza() {
    stubMateriaConProfesor(actorProfesor);
    PeriodoEvaluacion periodo = periodoPendiente();
    periodo.cambiarEstado(EstadoPeriodoEvaluacion.ABIERTO);
    periodo.cambiarEstado(EstadoPeriodoEvaluacion.CERRADO);
    when(periodoPort.buscarPorIdYTenant(periodoId, tenantId)).thenReturn(Optional.of(periodo));
    stubSeccion(gestionId, new BigDecimal("45"));

    assertThatThrownBy(() -> crearService.crear(comando(actorAdmin, true)))
        .isInstanceOf(PeriodoNoAbiertoException.class);
  }

  @Test
  void seccionDeOtraGestionRechaza() {
    stubMateriaConProfesor(actorProfesor);
    stubPeriodo(EstadoPeriodoEvaluacion.ABIERTO);
    stubSeccion(GestionEscolarId.nueva(), new BigDecimal("45"));

    assertThatThrownBy(() -> crearService.crear(comando(actorAdmin, true)))
        .isInstanceOf(SeccionNoPerteneceAGestionException.class);
  }

  @Test
  void profesorNoAsignadoObtiene404DeMateria() {
    stubMateriaConProfesor(otroProfesor);
    stubPeriodo(EstadoPeriodoEvaluacion.ABIERTO);
    stubSeccion(gestionId, new BigDecimal("45"));

    assertThatThrownBy(() -> crearService.crear(comando(actorProfesor, false)))
        .isInstanceOf(MateriaNoEncontradaException.class);
    verify(evaluacionPort, never()).guardar(any());
  }

  @Test
  void adminEnMateriaConProfesorOk() {
    stubMateriaConProfesor(actorProfesor);
    stubPeriodo(EstadoPeriodoEvaluacion.ABIERTO);
    stubSeccion(gestionId, new BigDecimal("45"));

    Evaluacion creada = crearService.crear(comando(actorAdmin, true));
    assertThat(creada.getNombre()).isEqualTo("Prueba escrita 1");
  }

  @Test
  void materiaDeOtroTenant404() {
    when(materiaPort.buscarPorIdYTenant(materiaId, tenantId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> crearService.crear(comando(actorAdmin, true)))
        .isInstanceOf(MateriaNoEncontradaException.class);
  }

  private CrearEvaluacionCommand comando(UUID actorId, boolean actorEsAdmin) {
    return new CrearEvaluacionCommand(
        tenantId,
        actorId,
        actorEsAdmin,
        "Prueba escrita 1",
        materiaId.valor(),
        periodoId.valor(),
        seccionId.valor(),
        LocalDate.of(2026, 3, 10),
        null);
  }

  private void stubMateriaConProfesor(UUID profesorId) {
    when(materiaPort.buscarPorIdYTenant(materiaId, tenantId)).thenReturn(Optional.of(materia()));
    when(asignacionPort.listarPorMateriaYTenant(materiaId, tenantId))
        .thenReturn(List.of(asignacion(profesorId)));
  }

  private void stubPeriodo(EstadoPeriodoEvaluacion estado) {
    PeriodoEvaluacion periodo = periodoPendiente();
    if (estado == EstadoPeriodoEvaluacion.ABIERTO) {
      periodo.cambiarEstado(EstadoPeriodoEvaluacion.ABIERTO);
    }
    when(periodoPort.buscarPorIdYTenant(periodoId, tenantId)).thenReturn(Optional.of(periodo));
  }

  private void stubSeccion(GestionEscolarId gestion, BigDecimal nota) {
    when(seccionPort.buscarPorIdYTenant(seccionId, tenantId))
        .thenReturn(Optional.of(SeccionEvaluacion.crear(
            seccionId, tenantId, gestion, "Saber", 2, nota)));
  }

  private Materia materia() {
    return Materia.crear(materiaId, tenantId, "Matemáticas");
  }

  private PeriodoEvaluacion periodoPendiente() {
    return PeriodoEvaluacion.crear(
        periodoId,
        tenantId,
        gestionId,
        "1er Trimestre",
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 5, 31),
        1);
  }

  private AsignacionMateriaProfesor asignacion(UUID profesorId) {
    return AsignacionMateriaProfesor.crear(
        AsignacionMateriaProfesorId.nueva(),
        tenantId,
        materiaId,
        profesorId,
        CursoId.nueva(),
        ParaleloId.nueva());
  }
}
