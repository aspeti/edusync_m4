package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarEvaluacionesUseCase;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.SeccionEvaluacion;
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
public class ListarEvaluacionesService implements ListarEvaluacionesUseCase {

  private final MateriaAccesoService materiaAccesoService;
  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<Evaluacion> listar(
      UUID tenantId, UUID materiaId, UUID periodoId, UUID actorId, boolean veTodasLasMaterias) {
    Materia materia = materiaAccesoService.exigirMateria(tenantId, materiaId);
    materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);

    List<Evaluacion> evaluaciones = periodoId == null
        ? evaluacionRepositoryPort.listarPorMateriaYTenant(MateriaId.de(materiaId), tenantId)
        : evaluacionRepositoryPort.listarPorMateriaPeriodoYTenant(
            MateriaId.de(materiaId), PeriodoEvaluacionId.de(periodoId), tenantId);

    Map<UUID, SeccionEvaluacion> seccionesPorId = evaluaciones.stream()
        .map(Evaluacion::getSeccionEvaluacionId)
        .distinct()
        .map(id -> seccionEvaluacionRepositoryPort.buscarPorIdYTenant(id, tenantId).orElse(null))
        .filter(s -> s != null)
        .collect(Collectors.toMap(s -> s.getId().valor(), Function.identity()));

    return evaluaciones.stream()
        .sorted(Comparator
            .comparingInt((Evaluacion e) -> {
              SeccionEvaluacion seccion = seccionesPorId.get(e.getSeccionEvaluacionId().valor());
              return seccion == null ? Integer.MAX_VALUE : seccion.getOrden();
            })
            .thenComparing(Evaluacion::getFecha)
            .thenComparing(Evaluacion::getNombre))
        .toList();
  }
}
