package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** {@code DD-UC-021}: resuelve "la gestion actual" sin listar ni elegir. */
class ObtenerGestionEscolarActivaServiceTest {

  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private ObtenerGestionEscolarActivaService service;

  @BeforeEach
  void setUp() {
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    service = new ObtenerGestionEscolarActivaService(gestionEscolarRepositoryPort);
  }

  @Test
  void devuelveLaUnicaGestionActivaDelTenant() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolar activa = GestionEscolar.reconstruir(
        GestionEscolarId.nueva(),
        tenantId,
        "2027",
        LocalDate.of(2027, 2, 1),
        LocalDate.of(2027, 11, 30),
        com.edusync.academico.domain.EstadoGestionEscolar.ACTIVA);
    when(gestionEscolarRepositoryPort.buscarActivaPorTenant(tenantId)).thenReturn(Optional.of(activa));

    GestionEscolar resultado = service.obtener(tenantId);

    assertThat(resultado).isEqualTo(activa);
  }

  @Test
  void lanzaNoEncontradaCuandoNingunaGestionEstaActiva() {
    UUID tenantId = UUID.randomUUID();
    when(gestionEscolarRepositoryPort.buscarActivaPorTenant(tenantId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtener(tenantId))
        .isInstanceOf(GestionEscolarNoEncontradaException.class);
  }
}
