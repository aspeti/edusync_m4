package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CrearAsignacionCursoCommand;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ParaleloNoEncontradoException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearAsignacionCursoServiceTest {

  private MateriaRepositoryPort materiaRepositoryPort;
  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  private CrearAsignacionCursoService service;

  @BeforeEach
  void setUp() {
    materiaRepositoryPort = mock(MateriaRepositoryPort.class);
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    asignacionMateriaCursoRepositoryPort = mock(AsignacionMateriaCursoRepositoryPort.class);
    service =
        new CrearAsignacionCursoService(
            materiaRepositoryPort, cursoRepositoryPort, paraleloRepositoryPort, asignacionMateriaCursoRepositoryPort);
  }

  @Test
  void creaCuandoMateriaCursoYParaleloPertenecenAlTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID materiaId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.of(Materia.reconstruir(MateriaId.de(materiaId), tenantId, "Matemáticas")));
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(any(ParaleloId.class), any(UUID.class)))
        .thenReturn(Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(cursoId), "A")));
    when(asignacionMateriaCursoRepositoryPort.guardar(any(AsignacionMateriaCurso.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    AsignacionMateriaCurso asignacion =
        service.crear(new CrearAsignacionCursoCommand(tenantId, materiaId, cursoId, paraleloId));

    assertThat(asignacion.getCursoId().valor()).isEqualTo(cursoId);
    assertThat(asignacion.getParaleloId().valor()).isEqualTo(paraleloId);
  }

  @Test
  void rechazaCuandoLaMateriaNoExisteOEsDeOtroTenant() {
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearAsignacionCursoCommand(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
        .isInstanceOf(MateriaNoEncontradaException.class);

    verify(asignacionMateriaCursoRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoElCursoNoExisteOEsDeOtroTenant() {
    UUID tenantId = UUID.randomUUID();
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.of(Materia.reconstruir(MateriaId.nueva(), tenantId, "Matemáticas")));
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearAsignacionCursoCommand(
                        tenantId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
        .isInstanceOf(CursoNoEncontradoException.class);

    verify(asignacionMateriaCursoRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoElParaleloNoPerteneceAlCurso() {
    UUID tenantId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID otroCursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.of(Materia.reconstruir(MateriaId.nueva(), tenantId, "Matemáticas")));
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(any(ParaleloId.class), any(UUID.class)))
        .thenReturn(
            Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(otroCursoId), "A")));

    assertThatThrownBy(
            () -> service.crear(new CrearAsignacionCursoCommand(tenantId, UUID.randomUUID(), cursoId, paraleloId)))
        .isInstanceOf(ParaleloNoEncontradoException.class);

    verify(asignacionMateriaCursoRepositoryPort, never()).guardar(any());
  }
}
