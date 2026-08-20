package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListarGestionesEscolaresServiceTest {

  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private ListarGestionesEscolaresService service;

  @BeforeEach
  void setUp() {
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    service = new ListarGestionesEscolaresService(gestionEscolarRepositoryPort);
  }

  @Test
  void delegaEnElRepositorioFiltrandoPorTenant() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), tenantId, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));
    PageQuery pageQuery = PageQuery.of(null, null);
    GestionEscolarFiltro filtro = GestionEscolarFiltro.VACIO;
    when(gestionEscolarRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery))
        .thenReturn(PageResult.of(List.of(gestionEscolar), pageQuery, 1));

    PageResult<GestionEscolar> resultado = service.listar(tenantId, filtro, pageQuery);

    assertThat(resultado.content()).containsExactly(gestionEscolar);
    assertThat(resultado.totalElements()).isEqualTo(1);
  }

  @Test
  void delegaFiltroYPaginacionSinModificarlos() {
    UUID tenantId = UUID.randomUUID();
    GestionEscolarFiltro filtro = new GestionEscolarFiltro("2027", EstadoGestionEscolar.ACTIVA);
    PageQuery pageQuery = new PageQuery(1, 5);
    when(gestionEscolarRepositoryPort.listarPorTenant(any(), any(), any()))
        .thenReturn(PageResult.of(List.of(), pageQuery, 0));

    service.listar(tenantId, filtro, pageQuery);

    verify(gestionEscolarRepositoryPort).listarPorTenant(tenantId, filtro, pageQuery);
  }
}
