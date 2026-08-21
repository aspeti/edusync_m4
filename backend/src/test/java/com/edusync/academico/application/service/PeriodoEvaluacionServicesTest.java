package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionCommand;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.PeriodoNoSecuencialException;
import com.edusync.academico.domain.PeriodoUnicoException;
import com.edusync.academico.domain.PeriodosInmutablesException;
import com.edusync.academico.domain.PeriodosSolapadosException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PeriodoEvaluacionServicesTest {

  private GestionEscolarRepositoryPort gestionPort;
  private PeriodoEvaluacionRepositoryPort periodoPort;
  private SeccionEvaluacionRepositoryPort seccionPort;
  private CrearPeriodoEvaluacionService crearService;
  private CambiarEstadoPeriodoEvaluacionService cambiarEstadoService;
  private EliminarPeriodoEvaluacionService eliminarService;

  private final UUID tenantId = UUID.randomUUID();
  private final GestionEscolarId gestionId = GestionEscolarId.nueva();

  @BeforeEach
  void setUp() {
    gestionPort = mock(GestionEscolarRepositoryPort.class);
    periodoPort = mock(PeriodoEvaluacionRepositoryPort.class);
    seccionPort = mock(SeccionEvaluacionRepositoryPort.class);
    crearService = new CrearPeriodoEvaluacionService(gestionPort, periodoPort);
    cambiarEstadoService = new CambiarEstadoPeriodoEvaluacionService(periodoPort, seccionPort);
    eliminarService = new EliminarPeriodoEvaluacionService(periodoPort);
  }

  @Test
  void crearRechazaSiGestionNoExiste() {
    when(gestionPort.buscarPorIdYTenant(any(), eq(tenantId))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> crearService.crear(new CrearPeriodoEvaluacionCommand(
            tenantId, gestionId.valor(), "T4", LocalDate.of(2027, 10, 1), LocalDate.of(2027, 11, 30))))
        .isInstanceOf(GestionEscolarNoEncontradaException.class);
  }

  @Test
  void crearRechazaSolape() {
    when(gestionPort.buscarPorIdYTenant(any(), eq(tenantId))).thenReturn(Optional.of(gestionStub()));
    PeriodoEvaluacion t1 = periodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 31), 1, EstadoPeriodoEvaluacion.PENDIENTE);
    when(periodoPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(t1));

    assertThatThrownBy(() -> crearService.crear(new CrearPeriodoEvaluacionCommand(
            tenantId, gestionId.valor(), "T2", LocalDate.of(2027, 5, 1), LocalDate.of(2027, 8, 31))))
        .isInstanceOf(PeriodosSolapadosException.class);
  }

  @Test
  void crearRechazaSiHayAbierto() {
    when(gestionPort.buscarPorIdYTenant(any(), eq(tenantId))).thenReturn(Optional.of(gestionStub()));
    PeriodoEvaluacion t1 = periodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 31), 1, EstadoPeriodoEvaluacion.ABIERTO);
    when(periodoPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(t1));

    assertThatThrownBy(() -> crearService.crear(new CrearPeriodoEvaluacionCommand(
            tenantId, gestionId.valor(), "T2", LocalDate.of(2027, 6, 1), LocalDate.of(2027, 8, 31))))
        .isInstanceOf(PeriodosInmutablesException.class);
  }

  @Test
  void abrirK2ConK1AbiertoEsNoSecuencial() {
    PeriodoEvaluacion t1 = periodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 31), 1, EstadoPeriodoEvaluacion.ABIERTO);
    PeriodoEvaluacion t2 = periodo("T2", LocalDate.of(2027, 6, 1), LocalDate.of(2027, 8, 31), 2, EstadoPeriodoEvaluacion.PENDIENTE);
    when(periodoPort.buscarPorIdYTenant(t2.getId(), tenantId)).thenReturn(Optional.of(t2));
    when(seccionPort.listarPorGestionYTenant(t2.getGestionEscolarId(), tenantId)).thenReturn(seccionesValidas());
    when(periodoPort.listarPorGestionYTenant(t2.getGestionEscolarId(), tenantId)).thenReturn(List.of(t1, t2));

    assertThatThrownBy(() ->
            cambiarEstadoService.cambiarEstado(tenantId, t2.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO))
        .isInstanceOf(PeriodoNoSecuencialException.class);
    verify(periodoPort, never()).guardar(any());
  }

  @Test
  void abrirK2ConK1CerradoOk() {
    PeriodoEvaluacion t1 = periodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 31), 1, EstadoPeriodoEvaluacion.CERRADO);
    PeriodoEvaluacion t2 = periodo("T2", LocalDate.of(2027, 6, 1), LocalDate.of(2027, 8, 31), 2, EstadoPeriodoEvaluacion.PENDIENTE);
    when(periodoPort.buscarPorIdYTenant(t2.getId(), tenantId)).thenReturn(Optional.of(t2));
    when(seccionPort.listarPorGestionYTenant(t2.getGestionEscolarId(), tenantId)).thenReturn(seccionesValidas());
    when(periodoPort.listarPorGestionYTenant(t2.getGestionEscolarId(), tenantId)).thenReturn(List.of(t1, t2));
    when(periodoPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

    PeriodoEvaluacion actualizado =
        cambiarEstadoService.cambiarEstado(tenantId, t2.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO);

    assertThat(actualizado.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.ABIERTO);
  }

  @Test
  void eliminarElUltimoRechaza() {
    PeriodoEvaluacion unico = periodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 31), 1, EstadoPeriodoEvaluacion.PENDIENTE);
    when(periodoPort.buscarPorIdYTenant(unico.getId(), tenantId)).thenReturn(Optional.of(unico));
    when(periodoPort.listarPorGestionYTenant(unico.getGestionEscolarId(), tenantId)).thenReturn(List.of(unico));

    assertThatThrownBy(() -> eliminarService.eliminar(tenantId, unico.getId().valor()))
        .isInstanceOf(PeriodoUnicoException.class);
  }

  @Test
  void cambiarEstadoDeOtroTenantEs404() {
    when(periodoPort.buscarPorIdYTenant(any(), eq(tenantId))).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
            cambiarEstadoService.cambiarEstado(tenantId, UUID.randomUUID(), EstadoPeriodoEvaluacion.ABIERTO))
        .isInstanceOf(PeriodoNoEncontradoException.class);
  }

  private GestionEscolar gestionStub() {
    return GestionEscolar.reconstruir(
        gestionId, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30),
        com.edusync.academico.domain.EstadoGestionEscolar.PLANIFICACION);
  }

  private PeriodoEvaluacion periodo(
      String nombre, LocalDate inicio, LocalDate fin, int orden, EstadoPeriodoEvaluacion estado) {
    return PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.nueva(), tenantId, gestionId, nombre, inicio, fin, orden, estado);
  }

  private List<com.edusync.academico.domain.SeccionEvaluacion> seccionesValidas() {
    return List.of(
        com.edusync.academico.domain.SeccionEvaluacion.reconstruir(
            com.edusync.academico.domain.SeccionEvaluacionId.nueva(),
            tenantId, gestionId, "Ser", 1, new java.math.BigDecimal("5")),
        com.edusync.academico.domain.SeccionEvaluacion.reconstruir(
            com.edusync.academico.domain.SeccionEvaluacionId.nueva(),
            tenantId, gestionId, "Saber", 2, new java.math.BigDecimal("45")),
        com.edusync.academico.domain.SeccionEvaluacion.reconstruir(
            com.edusync.academico.domain.SeccionEvaluacionId.nueva(),
            tenantId, gestionId, "Hacer", 3, new java.math.BigDecimal("40")),
        com.edusync.academico.domain.SeccionEvaluacion.reconstruir(
            com.edusync.academico.domain.SeccionEvaluacionId.nueva(),
            tenantId, gestionId, "Autoevaluación", 4, new java.math.BigDecimal("10")));
  }
}
