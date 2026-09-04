package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarCalificacionesUseCase;
import com.edusync.academico.application.port.out.CalificacionEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.EvaluacionNoEncontradaException;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarCalificacionesService implements ListarCalificacionesUseCase {

  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final CalificacionEvaluacionRepositoryPort calificacionEvaluacionRepositoryPort;
  private final EstudianteRepositoryPort estudianteRepositoryPort;
  private final MateriaAccesoService materiaAccesoService;
  private final NominaMateriaService nominaMateriaService;

  @Override
  @Transactional(readOnly = true)
  public Resultado listar(
      UUID tenantId, UUID evaluacionId, UUID actorId, boolean veTodasLasMaterias) {
    Evaluacion evaluacion =
        evaluacionRepositoryPort
            .buscarPorIdYTenant(EvaluacionId.de(evaluacionId), tenantId)
            .orElseThrow(EvaluacionNoEncontradaException::new);

    Materia materia =
        materiaAccesoService.exigirMateria(tenantId, evaluacion.getMateriaId().valor());
    materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);

    PeriodoEvaluacion periodo =
        periodoEvaluacionRepositoryPort
            .buscarPorIdYTenant(evaluacion.getPeriodoEvaluacionId(), tenantId)
            .orElseThrow(PeriodoNoEncontradoException::new);

    List<Inscripcion> nomina =
        nominaMateriaService.listarNomina(
            materia.getId(), periodo.getGestionEscolarId(), tenantId);

    List<EstudianteId> ids =
        nomina.stream().map(Inscripcion::getEstudianteId).distinct().toList();
    Map<UUID, Estudiante> estudiantes =
        estudianteRepositoryPort.listarPorIdsYTenant(ids, tenantId).stream()
            .collect(Collectors.toMap(e -> e.getId().valor(), Function.identity()));

    Map<UUID, CalificacionEvaluacion> porEstudiante =
        calificacionEvaluacionRepositoryPort
            .listarPorEvaluacionYTenant(evaluacion.getId(), tenantId)
            .stream()
            .collect(
                Collectors.toMap(c -> c.getEstudianteId().valor(), Function.identity(), (a, b) -> a));

    List<Fila> filas =
        ids.stream()
            .map(EstudianteId::valor)
            .filter(estudiantes::containsKey)
            .map(id -> new Fila(estudiantes.get(id), porEstudiante.get(id)))
            .sorted(Comparator.comparing(f -> f.estudiante().getNombreCompleto()))
            .toList();
    return new Resultado(filas);
  }
}
