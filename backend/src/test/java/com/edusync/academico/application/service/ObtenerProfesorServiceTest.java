package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.domain.ProfesorNoEncontradoException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObtenerProfesorServiceTest {

  private ProfesorConsultaPort profesorConsultaPort;
  private ObtenerProfesorService service;

  @BeforeEach
  void setUp() {
    profesorConsultaPort = mock(ProfesorConsultaPort.class);
    service = new ObtenerProfesorService(profesorConsultaPort);
  }

  @Test
  void devuelveProfesorInactivoConRol() {
    UUID tenantId = UUID.randomUUID();
    UUID profesorId = UUID.randomUUID();
    ProfesorResumen resumen = new ProfesorResumen(profesorId, "Ana Perez", false);
    when(profesorConsultaPort.buscarPorIdYTenant(profesorId, tenantId)).thenReturn(Optional.of(resumen));

    ProfesorResumen obtenido = service.obtener(tenantId, profesorId);

    assertThat(obtenido.activo()).isFalse();
    assertThat(obtenido.id()).isEqualTo(profesorId);
  }

  @Test
  void rechazaCon404CuandoNoExisteOEsDeOtroTenantOSinRol() {
    UUID tenantId = UUID.randomUUID();
    UUID profesorId = UUID.randomUUID();
    when(profesorConsultaPort.buscarPorIdYTenant(profesorId, tenantId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtener(tenantId, profesorId))
        .isInstanceOf(ProfesorNoEncontradoException.class)
        .satisfies(
            ex -> assertThat(((ProfesorNoEncontradoException) ex).getErrorCode()).isEqualTo("E_PROFESOR_NO_ENCONTRADO"));
  }
}
