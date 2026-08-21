package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListarParalelosServiceTest {

  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private ListarParalelosService service;

  @BeforeEach
  void setUp() {
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    service = new ListarParalelosService(cursoRepositoryPort, paraleloRepositoryPort);
  }

  @Test
  void listaLosParalelosCuandoElCursoExisteEnElTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    Paralelo paralelo = Paralelo.crear(ParaleloId.nueva(), tenantId, CursoId.de(cursoId), "A");
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.listarPorCursoYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(List.of(paralelo));

    List<Paralelo> resultado = service.listar(tenantId, cursoId);

    assertThat(resultado).containsExactly(paralelo);
  }

  @Test
  void rechazaCuandoElCursoNoExisteOEsDeOtroTenant() {
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.listar(UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(CursoNoEncontradoException.class);
  }
}
