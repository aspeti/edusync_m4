package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CambiarEstadoGestionEscolarServiceTest {

  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private CambiarEstadoGestionEscolarService service;

  @BeforeEach
  void setUp() {
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    service = new CambiarEstadoGestionEscolarService(gestionEscolarRepositoryPort);
  }

  @Test
  void cambiaElEstadoDeUnaGestionEscolarDelMismoTenant() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarId id = GestionEscolarId.nueva();
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        id, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30), EstadoGestionEscolar.PLANIFICACION);
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)).thenReturn(Optional.of(gestionEscolar));
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));

    GestionEscolar actualizada = service.cambiarEstado(id, tenantId, EstadoGestionEscolar.ACTIVA);

    assertThat(actualizada.getEstado()).isEqualTo(EstadoGestionEscolar.ACTIVA);
  }

  @Test
  void rechazaGestionEscolarDeOtroTenantConNotFound() {
    UUID tenantIdDeLaGestion = UUID.randomUUID();
    UUID tenantIdActor = UUID.randomUUID();
    GestionEscolarId id = GestionEscolarId.nueva();
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantIdActor)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.cambiarEstado(id, tenantIdActor, EstadoGestionEscolar.ACTIVA))
        .isInstanceOf(GestionEscolarNoEncontradaException.class);
  }

  /**
   * {@code DD-UC-019}: este endpoint es exclusivamente {@code ADMIN}; permite transicionar
   * a cualquier estado desde cualquier estado, incluida una gestion {@code CERRADA} (antes
   * terminal).
   */
  @Test
  void permiteCualquierTransicionIncluidaDesdeCerrada() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarId id = GestionEscolarId.nueva();
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        id, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30), EstadoGestionEscolar.CERRADA);
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)).thenReturn(Optional.of(gestionEscolar));
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));

    GestionEscolar actualizada = service.cambiarEstado(id, tenantId, EstadoGestionEscolar.ACTIVA);

    assertThat(actualizada.getEstado()).isEqualTo(EstadoGestionEscolar.ACTIVA);
  }

  /**
   * {@code DD-UC-021}: invariante "una sola gestion ACTIVA por tenant". Al activar una
   * gestion, si ya existe otra ACTIVA del mismo tenant, se cierra automaticamente.
   */
  @Test
  void activarUnaGestionCierraAutomaticamenteLaOtraActivaDelTenant() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarId idNueva = GestionEscolarId.nueva();
    GestionEscolarId idOtraActiva = GestionEscolarId.nueva();
    GestionEscolar nueva = GestionEscolar.reconstruir(
        idNueva, tenantId, "2028", LocalDate.of(2028, 2, 1), LocalDate.of(2028, 11, 30), EstadoGestionEscolar.PLANIFICACION);
    GestionEscolar otraActiva = GestionEscolar.reconstruir(
        idOtraActiva, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30), EstadoGestionEscolar.ACTIVA);
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(idNueva, tenantId)).thenReturn(Optional.of(nueva));
    when(gestionEscolarRepositoryPort.buscarActivaPorTenant(tenantId)).thenReturn(Optional.of(otraActiva));
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));

    GestionEscolar actualizada = service.cambiarEstado(idNueva, tenantId, EstadoGestionEscolar.ACTIVA);

    assertThat(actualizada.getEstado()).isEqualTo(EstadoGestionEscolar.ACTIVA);
    assertThat(otraActiva.getEstado()).isEqualTo(EstadoGestionEscolar.CERRADA);
    verify(gestionEscolarRepositoryPort, times(1)).guardar(otraActiva);
    verify(gestionEscolarRepositoryPort, times(1)).guardar(nueva);
  }

  /** {@code DD-UC-021}: re-activar la misma gestion (ya ACTIVA) no se cierra a si misma. */
  @Test
  void reactivarLaMismaGestionNoSeCierraASiMisma() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarId id = GestionEscolarId.nueva();
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        id, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30), EstadoGestionEscolar.ACTIVA);
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)).thenReturn(Optional.of(gestionEscolar));
    when(gestionEscolarRepositoryPort.buscarActivaPorTenant(tenantId)).thenReturn(Optional.of(gestionEscolar));
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));

    service.cambiarEstado(id, tenantId, EstadoGestionEscolar.ACTIVA);

    verify(gestionEscolarRepositoryPort, times(1)).guardar(any(GestionEscolar.class));
  }

  /** {@code DD-UC-021}: transicionar a un estado distinto de ACTIVA no dispara el auto-cierre. */
  @Test
  void transicionarACerradaNoConsultaOtraActiva() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarId id = GestionEscolarId.nueva();
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        id, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30), EstadoGestionEscolar.ACTIVA);
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)).thenReturn(Optional.of(gestionEscolar));
    when(gestionEscolarRepositoryPort.guardar(any(GestionEscolar.class))).thenAnswer(inv -> inv.getArgument(0));

    service.cambiarEstado(id, tenantId, EstadoGestionEscolar.CERRADA);

    verify(gestionEscolarRepositoryPort, never()).buscarActivaPorTenant(any());
  }
}
