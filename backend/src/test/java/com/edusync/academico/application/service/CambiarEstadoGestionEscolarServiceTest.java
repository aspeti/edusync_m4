package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.EstadoGestionEscolarInvalidoException;
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

  @Test
  void rechazaTransicionInvalida() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarId id = GestionEscolarId.nueva();
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        id, tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30), EstadoGestionEscolar.CERRADA);
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)).thenReturn(Optional.of(gestionEscolar));

    assertThatThrownBy(() -> service.cambiarEstado(id, tenantId, EstadoGestionEscolar.ACTIVA))
        .isInstanceOf(EstadoGestionEscolarInvalidoException.class);
  }
}
