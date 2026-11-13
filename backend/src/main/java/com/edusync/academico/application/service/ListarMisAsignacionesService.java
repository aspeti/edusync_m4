package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarMisAsignacionesUseCase;
import com.edusync.academico.application.port.in.MisAsignacionVista;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.Paralelo;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarMisAsignacionesService implements ListarMisAsignacionesUseCase {

  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private final MateriaRepositoryPort materiaRepositoryPort;
  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<MisAsignacionVista> listar(UUID tenantId, UUID profesorId) {
    return asignacionMateriaProfesorRepositoryPort.listarPorProfesorYTenant(profesorId, tenantId)
        .stream()
        .map(asignacion -> aVista(asignacion, tenantId))
        .sorted(
            Comparator.comparing(MisAsignacionVista::cursoNombre, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(MisAsignacionVista::paraleloNombre, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(MisAsignacionVista::materiaNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
        .toList();
  }

  private MisAsignacionVista aVista(AsignacionMateriaProfesor asignacion, UUID tenantId) {
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
    String cursoEtiqueta =
        (cursoNombre == null ? "" : cursoNombre)
            + (paraleloNombre == null || paraleloNombre.isBlank() ? "" : " " + paraleloNombre);
    return new MisAsignacionVista(
        asignacion.getId().valor(),
        asignacion.getMateriaId().valor(),
        materiaNombre,
        asignacion.getCursoId().valor(),
        cursoNombre,
        asignacion.getParaleloId().valor(),
        paraleloNombre,
        cursoEtiqueta.trim());
  }
}
