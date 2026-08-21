package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.ProfesorFiltro;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListarProfesoresServiceTest {

  private ProfesorConsultaPort profesorConsultaPort;
  private ListarProfesoresService service;

  @BeforeEach
  void setUp() {
    profesorConsultaPort = mock(ProfesorConsultaPort.class);
    service = new ListarProfesoresService(profesorConsultaPort);
  }

  @Test
  void delegaFiltroYPaginacionAlPuerto() {
    UUID tenantId = UUID.randomUUID();
    PageQuery pageQuery = PageQuery.of(0, 20);
    ProfesorResumen resumen = new ProfesorResumen(UUID.randomUUID(), "Ana Perez", true);
    when(profesorConsultaPort.listarDelTenant(tenantId, "ana", true, pageQuery))
        .thenReturn(PageResult.of(List.of(resumen), pageQuery, 1));

    PageResult<ProfesorResumen> resultado =
        service.listar(tenantId, new ProfesorFiltro("ana", true), pageQuery);

    assertThat(resultado.content()).containsExactly(resumen);
    verify(profesorConsultaPort).listarDelTenant(tenantId, "ana", true, pageQuery);
  }
}
