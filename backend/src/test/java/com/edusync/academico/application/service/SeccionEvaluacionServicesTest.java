package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.ActualizarSeccionEvaluacionCommand;
import com.edusync.academico.application.port.in.ReemplazarSeccionesEvaluacionCommand;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import com.edusync.academico.domain.SeccionNoEncontradaException;
import com.edusync.academico.domain.SumaSeccionesInvalidaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeccionEvaluacionServicesTest {

  private GestionEscolarRepositoryPort gestionPort;
  private PeriodoEvaluacionRepositoryPort periodoPort;
  private SeccionEvaluacionRepositoryPort seccionPort;
  private ReemplazarSeccionesEvaluacionService reemplazarService;
  private ActualizarSeccionEvaluacionService actualizarService;
  private CambiarEstadoPeriodoEvaluacionService cambiarEstadoService;

  private final UUID tenantId = UUID.randomUUID();
  private final GestionEscolarId gestionId = GestionEscolarId.nueva();

  @BeforeEach
  void setUp() {
    gestionPort = mock(GestionEscolarRepositoryPort.class);
    periodoPort = mock(PeriodoEvaluacionRepositoryPort.class);
    seccionPort = mock(SeccionEvaluacionRepositoryPort.class);
    reemplazarService = new ReemplazarSeccionesEvaluacionService(gestionPort, seccionPort);
    actualizarService = new ActualizarSeccionEvaluacionService(seccionPort);
    cambiarEstadoService = new CambiarEstadoPeriodoEvaluacionService(
        periodoPort,
        seccionPort,
        mock(AsignacionMateriaCursoRepositoryPort.class),
        mock(AsignacionMateriaProfesorRepositoryPort.class));
  }

  @Test
  void putRebalanceaPlantillaConSumaCien() {
    when(gestionPort.buscarPorIdYTenant(gestionId, tenantId)).thenReturn(Optional.of(gestionStub()));
    when(seccionPort.reemplazarPlantilla(eq(gestionId), eq(tenantId), any())).thenAnswer(inv -> inv.getArgument(2));

    List<SeccionEvaluacion> resultado = reemplazarService.reemplazar(put60y40());

    assertThat(resultado).hasSize(2);
    assertThat(resultado.get(0).getOrden()).isEqualTo(1);
    assertThat(resultado.get(1).getOrden()).isEqualTo(2);
    assertThat(resultado.get(0).getRangoMax()).isEqualByComparingTo("60.00");
  }

  @Test
  void putConSuma99Rechaza() {
    when(gestionPort.buscarPorIdYTenant(gestionId, tenantId)).thenReturn(Optional.of(gestionStub()));

    assertThatThrownBy(() -> reemplazarService.reemplazar(new ReemplazarSeccionesEvaluacionCommand(
            tenantId,
            gestionId.valor(),
            List.of(
                item("A", "50"),
                item("B", "49")))))
        .isInstanceOf(SumaSeccionesInvalidaException.class);
    verify(seccionPort, never()).reemplazarPlantilla(any(), any(), any());
  }

  @Test
  void putArrayVacioRechaza() {
    when(gestionPort.buscarPorIdYTenant(gestionId, tenantId)).thenReturn(Optional.of(gestionStub()));

    assertThatThrownBy(() -> reemplazarService.reemplazar(
            new ReemplazarSeccionesEvaluacionCommand(tenantId, gestionId.valor(), List.of())))
        .isInstanceOf(SumaSeccionesInvalidaException.class);
  }

  @Test
  void patchQueRompeLaSumaRechaza() {
    SeccionEvaluacion saber = seccion("Saber", 2, "45");
    SeccionEvaluacion ser = seccion("Ser", 1, "5");
    when(seccionPort.buscarPorIdYTenant(saber.getId(), tenantId)).thenReturn(Optional.of(saber));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of(ser, saber));

    assertThatThrownBy(() -> actualizarService.actualizar(new ActualizarSeccionEvaluacionCommand(
            tenantId,
            saber.getId().valor(),
            null,
            new BigDecimal("40"),
            BigDecimal.ZERO,
            new BigDecimal("40"))))
        .isInstanceOf(SumaSeccionesInvalidaException.class);
    verify(seccionPort, never()).guardar(any());
  }

  @Test
  void putYaNoConsultaElEstadoDeLosPeriodosHermanos() {
    when(gestionPort.buscarPorIdYTenant(gestionId, tenantId)).thenReturn(Optional.of(gestionStub()));
    when(seccionPort.reemplazarPlantilla(eq(gestionId), eq(tenantId), any())).thenAnswer(inv -> inv.getArgument(2));

    List<SeccionEvaluacion> resultado = reemplazarService.reemplazar(put60y40());

    assertThat(resultado).hasSize(2);
    verify(periodoPort, never()).listarPorGestionYTenant(any(), any());
  }

  @Test
  void abrirPeriodoSinSeccionesRechaza() {
    PeriodoEvaluacion t1 = periodoPendiente(1);
    when(periodoPort.buscarPorIdYTenant(t1.getId(), tenantId)).thenReturn(Optional.of(t1));
    when(seccionPort.listarPorGestionYTenant(gestionId, tenantId)).thenReturn(List.of());

    assertThatThrownBy(() ->
            cambiarEstadoService.cambiarEstado(tenantId, t1.getId().valor(), EstadoPeriodoEvaluacion.ABIERTO))
        .isInstanceOf(SumaSeccionesInvalidaException.class);
    verify(periodoPort, never()).guardar(any());
  }

  @Test
  void putDeOtroTenantEs404() {
    when(gestionPort.buscarPorIdYTenant(any(), eq(tenantId))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reemplazarService.reemplazar(put60y40()))
        .isInstanceOf(GestionEscolarNoEncontradaException.class);
  }

  @Test
  void patchDeOtroTenantEs404() {
    when(seccionPort.buscarPorIdYTenant(any(), eq(tenantId))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> actualizarService.actualizar(
            new ActualizarSeccionEvaluacionCommand(tenantId, UUID.randomUUID(), "X", null, null, null)))
        .isInstanceOf(SeccionNoEncontradaException.class);
  }

  private ReemplazarSeccionesEvaluacionCommand put60y40() {
    return new ReemplazarSeccionesEvaluacionCommand(
        tenantId, gestionId.valor(), List.of(item("A", "60"), item("B", "40")));
  }

  private ReemplazarSeccionesEvaluacionCommand.Item item(String nombre, String nota) {
    BigDecimal n = new BigDecimal(nota);
    return new ReemplazarSeccionesEvaluacionCommand.Item(nombre, n, BigDecimal.ZERO, n);
  }

  private GestionEscolar gestionStub() {
    return GestionEscolar.reconstruir(
        gestionId, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30),
        EstadoGestionEscolar.PLANIFICACION);
  }

  private PeriodoEvaluacion periodoPendiente(int orden) {
    return PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.nueva(),
        tenantId,
        gestionId,
        "Trimestre " + orden,
        LocalDate.of(2027, 2, 1),
        LocalDate.of(2027, 5, 1),
        orden,
        EstadoPeriodoEvaluacion.PENDIENTE);
  }

  private SeccionEvaluacion seccion(String nombre, int orden, String nota) {
    BigDecimal n = new BigDecimal(nota);
    return SeccionEvaluacion.reconstruir(
        SeccionEvaluacionId.nueva(), tenantId, gestionId, nombre, orden, n, BigDecimal.ZERO, n);
  }
}
