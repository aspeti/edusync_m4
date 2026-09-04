package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerNotaProvisionalUseCase;
import com.edusync.academico.application.port.out.CalificacionEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.CalculoNotas;
import com.edusync.academico.domain.CalculoNotas.SeccionCalculoInput;
import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.EstadoEvaluacion;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoEncontradoException;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerNotaProvisionalService implements ObtenerNotaProvisionalUseCase {

  private final MateriaAccesoService materiaAccesoService;
  private final NominaMateriaService nominaMateriaService;
  private final EstudianteRepositoryPort estudianteRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;
  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final CalificacionEvaluacionRepositoryPort calificacionEvaluacionRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public CalculoNotas.NotaProvisional obtener(
      UUID tenantId,
      UUID materiaId,
      UUID estudianteId,
      UUID periodoId,
      UUID actorId,
      boolean veTodasLasMaterias) {
    Materia materia = materiaAccesoService.exigirMateria(tenantId, materiaId);
    materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);

    estudianteRepositoryPort
        .buscarPorIdYTenant(EstudianteId.de(estudianteId), tenantId)
        .orElseThrow(EstudianteNoEncontradoException::new);

    PeriodoEvaluacion periodoSolicitado =
        periodoEvaluacionRepositoryPort
            .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
            .orElseThrow(PeriodoNoEncontradoException::new);

    nominaMateriaService.exigirEnNomina(
        materia.getId(),
        periodoSolicitado.getGestionEscolarId(),
        tenantId,
        EstudianteId.de(estudianteId));

    List<SeccionEvaluacion> secciones =
        seccionEvaluacionRepositoryPort
            .listarPorGestionYTenant(periodoSolicitado.getGestionEscolarId(), tenantId)
            .stream()
            .sorted(Comparator.comparingInt(SeccionEvaluacion::getOrden))
            .toList();

    List<PeriodoEvaluacion> periodos =
        periodoEvaluacionRepositoryPort
            .listarPorGestionYTenant(periodoSolicitado.getGestionEscolarId(), tenantId)
            .stream()
            .sorted(Comparator.comparingInt(PeriodoEvaluacion::getOrden))
            .toList();

    List<Integer> notasPeriodo = new ArrayList<>();
    List<SeccionCalculoInput> seccionesPeriodoSolicitado = List.of();
    for (PeriodoEvaluacion periodo : periodos) {
      List<SeccionCalculoInput> inputs =
          armarSecciones(
              materia.getId(),
              periodo.getId(),
              EstudianteId.de(estudianteId),
              tenantId,
              secciones);
      Integer notaPeriodo = CalculoNotas.calcularNotaPeriodo(inputs);
      notasPeriodo.add(notaPeriodo);
      if (periodo.getId().equals(periodoSolicitado.getId())) {
        seccionesPeriodoSolicitado = inputs;
      }
    }

    return CalculoNotas.calcular(seccionesPeriodoSolicitado, notasPeriodo);
  }

  private List<SeccionCalculoInput> armarSecciones(
      com.edusync.academico.domain.MateriaId materiaId,
      PeriodoEvaluacionId periodoId,
      EstudianteId estudianteId,
      UUID tenantId,
      List<SeccionEvaluacion> secciones) {
    List<Evaluacion> activas =
        evaluacionRepositoryPort.listarPorMateriaPeriodoYTenant(materiaId, periodoId, tenantId)
            .stream()
            .filter(e -> e.getEstado() == EstadoEvaluacion.ACTIVA)
            .toList();

    Map<UUID, List<Evaluacion>> porSeccion =
        activas.stream()
            .collect(Collectors.groupingBy(e -> e.getSeccionEvaluacionId().valor()));

    List<CalificacionEvaluacion> calificaciones =
        calificacionEvaluacionRepositoryPort.listarPorEvaluacionesEstudianteYTenant(
            activas.stream().map(Evaluacion::getId).toList(), estudianteId, tenantId);
    Map<UUID, BigDecimal> valorPorEval =
        calificaciones.stream()
            .collect(
                Collectors.toMap(
                    c -> c.getEvaluacionId().valor(), CalificacionEvaluacion::getValor, (a, b) -> a));

    List<SeccionCalculoInput> inputs = new ArrayList<>();
    for (SeccionEvaluacion seccion : secciones) {
      List<Evaluacion> evalsSeccion =
          porSeccion.getOrDefault(seccion.getId().valor(), List.of());
      List<BigDecimal> valores =
          evalsSeccion.stream()
              .map(e -> valorPorEval.get(e.getId().valor()))
              .filter(v -> v != null)
              .toList();
      inputs.add(new SeccionCalculoInput(seccion.getId().valor(), seccion.getNombre(), valores));
    }
    return inputs;
  }
}
