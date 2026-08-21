package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.application.port.in.CrearAsignacionProfesorCommand;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaSinCursoException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ProfesorNoEncontradoException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearAsignacionProfesorServiceTest {

  private MateriaRepositoryPort materiaRepositoryPort;
  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  private AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private ProfesorConsultaPort profesorConsultaPort;
  private CrearAsignacionProfesorService service;

  @BeforeEach
  void setUp() {
    materiaRepositoryPort = mock(MateriaRepositoryPort.class);
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    asignacionMateriaCursoRepositoryPort = mock(AsignacionMateriaCursoRepositoryPort.class);
    asignacionMateriaProfesorRepositoryPort = mock(AsignacionMateriaProfesorRepositoryPort.class);
    profesorConsultaPort = mock(ProfesorConsultaPort.class);
    service =
        new CrearAsignacionProfesorService(
            materiaRepositoryPort,
            cursoRepositoryPort,
            paraleloRepositoryPort,
            asignacionMateriaCursoRepositoryPort,
            asignacionMateriaProfesorRepositoryPort,
            profesorConsultaPort);
  }

  @Test
  void creaCuandoHayAsignacionCursoPreviaYElProfesorEsActivoDelTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID materiaId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    UUID profesorId = UUID.randomUUID();
    stubPadresValidos(tenantId, materiaId, cursoId, paraleloId);
    when(asignacionMateriaCursoRepositoryPort.existePorMateriaCursoParaleloYTenant(
            any(MateriaId.class), any(CursoId.class), any(ParaleloId.class), any(UUID.class)))
        .thenReturn(true);
    when(profesorConsultaPort.esProfesorActivoDelTenant(profesorId, tenantId)).thenReturn(true);
    when(asignacionMateriaProfesorRepositoryPort.guardar(any(AsignacionMateriaProfesor.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    AsignacionMateriaProfesor asignacion =
        service.crear(new CrearAsignacionProfesorCommand(tenantId, materiaId, profesorId, cursoId, paraleloId));

    assertThat(asignacion.getProfesorId()).isEqualTo(profesorId);
  }

  @Test
  void rechazaCon409CuandoFaltaAsignacionCursoPrevia() {
    UUID tenantId = UUID.randomUUID();
    UUID materiaId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    stubPadresValidos(tenantId, materiaId, cursoId, paraleloId);
    when(asignacionMateriaCursoRepositoryPort.existePorMateriaCursoParaleloYTenant(
            any(MateriaId.class), any(CursoId.class), any(ParaleloId.class), any(UUID.class)))
        .thenReturn(false);

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearAsignacionProfesorCommand(
                        tenantId, materiaId, UUID.randomUUID(), cursoId, paraleloId)))
        .isInstanceOf(MateriaSinCursoException.class)
        .satisfies(ex -> assertThat(((MateriaSinCursoException) ex).getErrorCode()).isEqualTo("E_MATERIA_SIN_CURSO"));

    verify(asignacionMateriaProfesorRepositoryPort, never()).guardar(any());
  }

  @Test
  void rechazaCuandoElUsuarioNoEsProfesorActivoDelTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID materiaId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    UUID profesorId = UUID.randomUUID();
    stubPadresValidos(tenantId, materiaId, cursoId, paraleloId);
    when(asignacionMateriaCursoRepositoryPort.existePorMateriaCursoParaleloYTenant(
            any(MateriaId.class), any(CursoId.class), any(ParaleloId.class), any(UUID.class)))
        .thenReturn(true);
    when(profesorConsultaPort.esProfesorActivoDelTenant(profesorId, tenantId)).thenReturn(false);

    assertThatThrownBy(
            () ->
                service.crear(
                    new CrearAsignacionProfesorCommand(tenantId, materiaId, profesorId, cursoId, paraleloId)))
        .isInstanceOf(ProfesorNoEncontradoException.class);

    verify(asignacionMateriaProfesorRepositoryPort, never()).guardar(any());
  }

  private void stubPadresValidos(UUID tenantId, UUID materiaId, UUID cursoId, UUID paraleloId) {
    when(materiaRepositoryPort.buscarPorIdYTenant(any(MateriaId.class), any(UUID.class)))
        .thenReturn(Optional.of(Materia.reconstruir(MateriaId.de(materiaId), tenantId, "Matemáticas")));
    when(cursoRepositoryPort.buscarPorIdYTenant(any(CursoId.class), any(UUID.class)))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(any(ParaleloId.class), any(UUID.class)))
        .thenReturn(Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(cursoId), "A")));
  }
}
