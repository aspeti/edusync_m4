package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearParaleloCommand;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.Paralelo;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearParaleloServiceTest {

  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private CrearParaleloService service;

  @BeforeEach
  void setUp() {
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    service = new CrearParaleloService(cursoRepositoryPort, paraleloRepositoryPort);
  }

  @Test
  void creaUnParaleloCuandoElCursoPadreExisteEnElTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.guardar(any(Paralelo.class))).thenAnswer(inv -> inv.getArgument(0));

    Paralelo paralelo = service.crear(new CrearParaleloCommand(tenantId, cursoId, "A"));

    assertThat(paralelo.getNombre()).isEqualTo("A");
    assertThat(paralelo.getCursoId().valor()).isEqualTo(cursoId);
  }

  @Test
  void rechazaCuandoElCursoPadreNoExisteOEsDeOtroTenant() {
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.crear(new CrearParaleloCommand(UUID.randomUUID(), UUID.randomUUID(), "A")))
        .isInstanceOf(CursoNoEncontradoException.class)
        .satisfies(ex -> assertThat(((CursoNoEncontradoException) ex).getErrorCode())
            .isEqualTo("E_CURSO_NO_ENCONTRADO"));

    verify(paraleloRepositoryPort, never()).guardar(any());
  }
}
