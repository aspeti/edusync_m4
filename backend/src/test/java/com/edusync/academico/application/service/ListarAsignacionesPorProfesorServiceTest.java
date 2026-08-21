package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.AsignacionProfesorVista;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.AsignacionMateriaProfesorId;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ProfesorNoEncontradoException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListarAsignacionesPorProfesorServiceTest {

  private ProfesorConsultaPort profesorConsultaPort;
  private AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private MateriaRepositoryPort materiaRepositoryPort;
  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private ListarAsignacionesPorProfesorService service;

  @BeforeEach
  void setUp() {
    profesorConsultaPort = mock(ProfesorConsultaPort.class);
    asignacionMateriaProfesorRepositoryPort = mock(AsignacionMateriaProfesorRepositoryPort.class);
    materiaRepositoryPort = mock(MateriaRepositoryPort.class);
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    service =
        new ListarAsignacionesPorProfesorService(
            profesorConsultaPort,
            asignacionMateriaProfesorRepositoryPort,
            materiaRepositoryPort,
            cursoRepositoryPort,
            paraleloRepositoryPort);
  }

  @Test
  void listaAsignacionesEnriquecidasDeUnProfesorInactivo() {
    UUID tenantId = UUID.randomUUID();
    UUID profesorId = UUID.randomUUID();
    UUID materiaId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    UUID asignacionId = UUID.randomUUID();
    when(profesorConsultaPort.buscarPorIdYTenant(profesorId, tenantId))
        .thenReturn(Optional.of(new ProfesorResumen(profesorId, "Ana Perez", false)));
    when(asignacionMateriaProfesorRepositoryPort.listarPorProfesorYTenant(profesorId, tenantId))
        .thenReturn(
            List.of(
                AsignacionMateriaProfesor.reconstruir(
                    AsignacionMateriaProfesorId.de(asignacionId),
                    tenantId,
                    MateriaId.de(materiaId),
                    profesorId,
                    CursoId.de(cursoId),
                    ParaleloId.de(paraleloId))));
    when(materiaRepositoryPort.buscarPorIdYTenant(MateriaId.de(materiaId), tenantId))
        .thenReturn(Optional.of(Materia.reconstruir(MateriaId.de(materiaId), tenantId, "Matemáticas")));
    when(cursoRepositoryPort.buscarPorIdYTenant(CursoId.de(cursoId), tenantId))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(ParaleloId.de(paraleloId), tenantId))
        .thenReturn(Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(cursoId), "A")));

    List<AsignacionProfesorVista> vistas = service.listar(tenantId, profesorId);

    assertThat(vistas).hasSize(1);
    assertThat(vistas.getFirst().materiaNombre()).isEqualTo("Matemáticas");
    assertThat(vistas.getFirst().cursoNombre()).isEqualTo("Primero");
    assertThat(vistas.getFirst().paraleloNombre()).isEqualTo("A");
  }

  @Test
  void rechazaCon404CuandoElProfesorNoPerteneceAlTenantONoTieneRol() {
    UUID tenantId = UUID.randomUUID();
    UUID profesorId = UUID.randomUUID();
    when(profesorConsultaPort.buscarPorIdYTenant(profesorId, tenantId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.listar(tenantId, profesorId)).isInstanceOf(ProfesorNoEncontradoException.class);

    verify(asignacionMateriaProfesorRepositoryPort, never()).listarPorProfesorYTenant(any(), any());
  }
}
