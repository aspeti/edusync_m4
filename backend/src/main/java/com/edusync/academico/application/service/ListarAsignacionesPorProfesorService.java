package com.edusync.academico.application.service;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.application.port.in.AsignacionProfesorVista;
import com.edusync.academico.application.port.in.ListarAsignacionesPorProfesorUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ProfesorNoEncontradoException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarAsignacionesPorProfesorService implements ListarAsignacionesPorProfesorUseCase {

  private final ProfesorConsultaPort profesorConsultaPort;
  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private final MateriaRepositoryPort materiaRepositoryPort;
  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<AsignacionProfesorVista> listar(UUID tenantId, UUID profesorId) {
    profesorConsultaPort
        .buscarPorIdYTenant(profesorId, tenantId)
        .orElseThrow(ProfesorNoEncontradoException::new);
    return asignacionMateriaProfesorRepositoryPort.listarPorProfesorYTenant(profesorId, tenantId).stream()
        .map(asignacion -> aVista(asignacion, tenantId))
        .toList();
  }

  private AsignacionProfesorVista aVista(AsignacionMateriaProfesor asignacion, UUID tenantId) {
    String materiaNombre =
        materiaRepositoryPort
            .buscarPorIdYTenant(asignacion.getMateriaId(), tenantId)
            .map(Materia::getNombre)
            .orElse(null);
    String cursoNombre =
        cursoRepositoryPort
            .buscarPorIdYTenant(asignacion.getCursoId(), tenantId)
            .map(Curso::getNombre)
            .orElse(null);
    String paraleloNombre =
        paraleloRepositoryPort
            .buscarPorIdYTenant(asignacion.getParaleloId(), tenantId)
            .map(Paralelo::getNombre)
            .orElse(null);
    return new AsignacionProfesorVista(
        asignacion.getId().valor(),
        asignacion.getMateriaId().valor(),
        materiaNombre,
        asignacion.getCursoId().valor(),
        cursoNombre,
        asignacion.getParaleloId().valor(),
        paraleloNombre);
  }
}
