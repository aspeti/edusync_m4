package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.GuardarParametrosPeriodoUseCase.Item;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.ParametroPeriodoRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.AsignacionMateriaCursoId;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.AsignacionMateriaProfesorId;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaSinDocenteException;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ParametroPeriodo;
import com.edusync.academico.domain.ParametroPeriodoId;
import com.edusync.academico.domain.ParametroRangoInvalidoException;
import com.edusync.academico.domain.ParametrosIncompletosException;
import com.edusync.academico.domain.ParametrosInmutablesException;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.ReglaCombinacionNoSoportadaException;
import com.edusync.academico.domain.ReglaCombinacionPeriodo;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import com.edusync.academico.domain.SeccionNoEncontradaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParametroPeriodoServicesTest {

  private PeriodoEvaluacionRepositoryPort periodoPort;
  private SeccionEvaluacionRepositoryPort seccionPort;
  private ParametroPeriodoRepositoryPort parametroPort;
  private AsignacionMateriaCursoRepositoryPort cursoPort;
  private AsignacionMateriaProfesorRepositoryPort profesorPort;
  private GuardarParametrosPeriodoService guardarService;
  private CambiarEstadoPeriodoEvaluacionService cambiarEstadoService;

  private final UUID tenantId = UUID.randomUUID();
  private final GestionEscolarId gestionId = GestionEscolarId.nueva();

  @BeforeEach
  void setUp() {
    periodoPort = mock(PeriodoEvaluacionRepositoryPort.class);
    seccionPort = mock(SeccionEvaluacionRepositoryPort.class);
    parametroPort = mock(ParametroPeriodoRepositoryPort.class);
    cursoPort = mock(AsignacionMateriaCursoRepositoryPort.class);
    profesorPort = mock(AsignacionMateriaProfesorRepositoryPort.class);
    guardarService = new GuardarParametrosPeriodoService(periodoPort, seccionPort, parametroPort);
    cambiarEstadoService = new CambiarEstadoPeriodoEvaluacionService(
        periodoPort, seccionPort, parametroPort, cursoPort, profesorPort);
  }

  @Test
  void putConPeriodoAbiertoEsInmutable() {
    PeriodoEvaluacion abierto = periodo(EstadoPeriodoEvaluacion.ABIERTO);
    when(periodoPort.buscarPorIdYTenant(abierto.getId(), tenantId)).thenReturn(Optional.of(abierto));

    assertThatThrownBy(() -> guardarService.guardar(tenantId, abierto.getId().valor(), List.of()))
        .isInstanceOf(ParametrosInmutablesException.class);
    verify(parametroPort, never()).reemplazarPorPeriodo(any(), any(), any());
  }

  @Test
  void putSinTodasLasSeccionesEsIncompleto() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    SeccionEvaluacion ser = seccion("Ser", "5");
    SeccionEvaluacion saber = seccion("Saber", "95");
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser, saber));

    assertThatThrownBy(() -> guardarService.guardar(
            tenantId, pendiente.getId().valor(), List.of(item(ser, "5"))))
        .isInstanceOf(ParametrosIncompletosException.class);
  }

  @Test
  void putConSeccionAjenaEs404() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(seccion("Ser", "100")));

    assertThatThrownBy(() -> guardarService.guardar(
            tenantId,
            pendiente.getId().valor(),
            List.of(new Item(UUID.randomUUID(), BigDecimal.ZERO, new BigDecimal("10"), "PROMEDIO_SIMPLE"))))
        .isInstanceOf(SeccionNoEncontradaException.class);
  }

  @Test
  void rangoMaxMayorQueLaNotaRechaza() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    SeccionEvaluacion ser = seccion("Ser", "5");
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser));

    assertThatThrownBy(() -> guardarService.guardar(
            tenantId, pendiente.getId().valor(), List.of(item(ser, "6"))))
        .isInstanceOf(ParametroRangoInvalidoException.class);
  }

  @Test
  void reglaNoSoportadaRechaza() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    SeccionEvaluacion ser = seccion("Ser", "100");
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser));

    assertThatThrownBy(() -> guardarService.guardar(
            tenantId,
            pendiente.getId().valor(),
            List.of(new Item(ser.getId().valor(), BigDecimal.ZERO, new BigDecimal("100"), "MEJOR_N"))))
        .isInstanceOf(ReglaCombinacionNoSoportadaException.class);
  }

  @Test
  void abrirSinParametrosRechaza() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(seccion("Ser", "100")));
    when(parametroPort.listarPorPeriodoYTenant(pendiente.getId(), tenantId)).thenReturn(List.of());

    assertThatThrownBy(() -> cambiarEstadoService.cambiarEstado(
            tenantId, pendiente.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO))
        .isInstanceOf(ParametrosIncompletosException.class);
    verify(periodoPort, never()).guardar(any());
  }

  @Test
  void abrirConMateriaSinProfesorRechaza() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    SeccionEvaluacion ser = seccion("Ser", "100");
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser));
    when(parametroPort.listarPorPeriodoYTenant(pendiente.getId(), tenantId))
        .thenReturn(List.of(parametroDe(pendiente.getId(), ser)));
    MateriaId materiaId = MateriaId.nueva();
    when(cursoPort.listarPorTenant(tenantId)).thenReturn(List.of(asignacionCurso(materiaId)));
    when(profesorPort.listarPorTenant(tenantId)).thenReturn(List.of());

    assertThatThrownBy(() -> cambiarEstadoService.cambiarEstado(
            tenantId, pendiente.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO))
        .isInstanceOf(MateriaSinDocenteException.class);
  }

  @Test
  void materiaSinCursoNoBloqueaLaApertura() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    SeccionEvaluacion ser = seccion("Ser", "100");
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser));
    when(parametroPort.listarPorPeriodoYTenant(pendiente.getId(), tenantId))
        .thenReturn(List.of(parametroDe(pendiente.getId(), ser)));
    when(cursoPort.listarPorTenant(tenantId)).thenReturn(List.of());
    when(periodoPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

    PeriodoEvaluacion abierto = cambiarEstadoService.cambiarEstado(
        tenantId, pendiente.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO);

    assertThat(abierto.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.ABIERTO);
    verify(profesorPort, never()).listarPorTenant(any());
  }

  @Test
  void cerrarNoExigeParametros() {
    PeriodoEvaluacion abierto = periodo(EstadoPeriodoEvaluacion.ABIERTO);
    when(periodoPort.buscarPorIdYTenant(abierto.getId(), tenantId)).thenReturn(Optional.of(abierto));
    when(periodoPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

    PeriodoEvaluacion cerrado = cambiarEstadoService.cambiarEstado(
        tenantId, abierto.getId().valor(), EstadoPeriodoEvaluacion.CERRADO);

    assertThat(cerrado.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.CERRADO);
    verify(parametroPort, never()).listarPorPeriodoYTenant(any(), any());
  }

  @Test
  void abrirConParametrosYProfesorOk() {
    PeriodoEvaluacion pendiente = periodo(EstadoPeriodoEvaluacion.PENDIENTE);
    SeccionEvaluacion ser = seccion("Ser", "100");
    when(periodoPort.buscarPorIdYTenant(pendiente.getId(), tenantId)).thenReturn(Optional.of(pendiente));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser));
    when(parametroPort.listarPorPeriodoYTenant(pendiente.getId(), tenantId))
        .thenReturn(List.of(parametroDe(pendiente.getId(), ser)));
    MateriaId materiaId = MateriaId.nueva();
    when(cursoPort.listarPorTenant(tenantId)).thenReturn(List.of(asignacionCurso(materiaId)));
    when(profesorPort.listarPorTenant(tenantId)).thenReturn(List.of(asignacionProfesor(materiaId)));
    when(periodoPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

    PeriodoEvaluacion abierto = cambiarEstadoService.cambiarEstado(
        tenantId, pendiente.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO);

    assertThat(abierto.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.ABIERTO);
  }

  private Item item(SeccionEvaluacion seccion, String rangoMax) {
    return new Item(seccion.getId().valor(), BigDecimal.ZERO, new BigDecimal(rangoMax), "PROMEDIO_SIMPLE");
  }

  private ParametroPeriodo parametroDe(PeriodoEvaluacionId periodoId, SeccionEvaluacion seccion) {
    return ParametroPeriodo.crear(
        ParametroPeriodoId.nueva(),
        tenantId,
        periodoId,
        seccion.getId(),
        BigDecimal.ZERO,
        seccion.getNota(),
        ReglaCombinacionPeriodo.PROMEDIO_SIMPLE);
  }

  private AsignacionMateriaCurso asignacionCurso(MateriaId materiaId) {
    return AsignacionMateriaCurso.reconstruir(
        AsignacionMateriaCursoId.nueva(), tenantId, materiaId, CursoId.nueva(), ParaleloId.nueva());
  }

  private AsignacionMateriaProfesor asignacionProfesor(MateriaId materiaId) {
    return AsignacionMateriaProfesor.reconstruir(
        AsignacionMateriaProfesorId.nueva(),
        tenantId,
        materiaId,
        UUID.randomUUID(),
        CursoId.nueva(),
        ParaleloId.nueva());
  }

  private SeccionEvaluacion seccion(String nombre, String nota) {
    return SeccionEvaluacion.reconstruir(
        SeccionEvaluacionId.nueva(), tenantId, gestionId, nombre, 1, new BigDecimal(nota));
  }

  private PeriodoEvaluacion periodo(EstadoPeriodoEvaluacion estado) {
    return PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.nueva(),
        tenantId,
        gestionId,
        "T1",
        LocalDate.of(2027, 2, 1),
        LocalDate.of(2027, 5, 31),
        1,
        estado);
  }
}
