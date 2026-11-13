package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerPlanillaMateriaUseCase;
import com.edusync.academico.application.port.in.PlanillaMateriaVista;
import com.edusync.academico.application.port.in.PlanillaMateriaVista.Columna;
import com.edusync.academico.application.port.in.PlanillaMateriaVista.Fila;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.CalificacionEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort.CursoParaleloPar;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.CalculoNotas;
import com.edusync.academico.domain.CalculoNotas.SeccionCalculoInput;
import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.EstadoEvaluacion;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.PlanillaNoEncontradaException;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Composición de planilla ({@code DD-UC-033} / {@code FSD-UC-022}). */
@Service
@RequiredArgsConstructor
public class ObtenerPlanillaMateriaService implements ObtenerPlanillaMateriaUseCase {

  private final MateriaAccesoService materiaAccesoService;
  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;
  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final CalificacionEvaluacionRepositoryPort calificacionEvaluacionRepositoryPort;
  private final InscripcionRepositoryPort inscripcionRepositoryPort;
  private final EstudianteRepositoryPort estudianteRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public PlanillaMateriaVista obtener(
      UUID tenantId,
      UUID materiaId,
      UUID cursoId,
      UUID paraleloId,
      UUID periodoId,
      UUID actorId,
      boolean veTodasLasMaterias,
      boolean puedeEscribir) {
    Materia materia = materiaAccesoService.exigirMateria(tenantId, materiaId);
    materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);
    exigirTripleta(tenantId, materia, cursoId, paraleloId, actorId, veTodasLasMaterias);

    Curso curso =
        cursoRepositoryPort
            .buscarPorIdYTenant(CursoId.de(cursoId), tenantId)
            .orElseThrow(PlanillaNoEncontradaException::new);
    Paralelo paralelo =
        paraleloRepositoryPort
            .buscarPorIdYTenant(ParaleloId.de(paraleloId), tenantId)
            .orElseThrow(PlanillaNoEncontradaException::new);

    PeriodoEvaluacion periodo =
        periodoEvaluacionRepositoryPort
            .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
            .orElseThrow(PeriodoNoEncontradoException::new);

    List<SeccionEvaluacion> secciones =
        seccionEvaluacionRepositoryPort
            .listarPorGestionYTenant(periodo.getGestionEscolarId(), tenantId)
            .stream()
            .sorted(Comparator.comparingInt(SeccionEvaluacion::getOrden))
            .toList();
    Map<UUID, SeccionEvaluacion> seccionPorId =
        secciones.stream().collect(Collectors.toMap(s -> s.getId().valor(), s -> s));

    List<Evaluacion> activas =
        evaluacionRepositoryPort
            .listarPorMateriaPeriodoYTenant(materia.getId(), periodo.getId(), tenantId)
            .stream()
            .filter(e -> e.getEstado() == EstadoEvaluacion.ACTIVA)
            .sorted(
                Comparator.comparingInt(
                        (Evaluacion e) ->
                            seccionPorId.containsKey(e.getSeccionEvaluacionId().valor())
                                ? seccionPorId.get(e.getSeccionEvaluacionId().valor()).getOrden()
                                : Integer.MAX_VALUE)
                    .thenComparing(Evaluacion::getFecha)
                    .thenComparing(Evaluacion::getNombre, String.CASE_INSENSITIVE_ORDER))
            .toList();

    List<Columna> columnas =
        activas.stream()
            .map(
                e -> {
                  SeccionEvaluacion seccion = seccionPorId.get(e.getSeccionEvaluacionId().valor());
                  return new Columna(
                      e.getId().valor(),
                      e.getNombre(),
                      e.getSeccionEvaluacionId().valor(),
                      seccion == null ? null : seccion.getNombre(),
                      seccion == null ? 0 : seccion.getOrden(),
                      e.getPuntajeMaximo(),
                      e.getFecha());
                })
            .toList();

    Set<CursoParaleloPar> pares =
        Set.of(new CursoParaleloPar(CursoId.de(cursoId), ParaleloId.de(paraleloId)));
    List<Inscripcion> inscritos =
        inscripcionRepositoryPort.listarActivasPorGestionYParesCursoParalelo(
            periodo.getGestionEscolarId(), tenantId, pares);

    Map<UUID, Map<UUID, BigDecimal>> notasPorEstudiante = new HashMap<>();
    for (Evaluacion evaluacion : activas) {
      for (CalificacionEvaluacion cal :
          calificacionEvaluacionRepositoryPort.listarPorEvaluacionYTenant(
              evaluacion.getId(), tenantId)) {
        notasPorEstudiante
            .computeIfAbsent(cal.getEstudianteId().valor(), ignored -> new HashMap<>())
            .put(evaluacion.getId().valor(), cal.getValor());
      }
    }

    List<Fila> filas = new ArrayList<>();
    for (Inscripcion insc : inscritos) {
      Estudiante estudiante =
          estudianteRepositoryPort.buscarPorIdYTenant(insc.getEstudianteId(), tenantId).orElse(null);
      if (estudiante == null) {
        continue;
      }
      Map<UUID, BigDecimal> raw =
          notasPorEstudiante.getOrDefault(estudiante.getId().valor(), Map.of());
      Map<UUID, BigDecimal> notasVisibles = new LinkedHashMap<>();
      for (Evaluacion e : activas) {
        BigDecimal v = raw.get(e.getId().valor());
        if (v != null) {
          notasVisibles.put(e.getId().valor(), v);
        }
      }
      Integer notaPeriodo =
          CalculoNotas.calcularNotaPeriodo(armarSecciones(secciones, activas, raw));
      filas.add(
          new Fila(
              estudiante.getId().valor(),
              estudiante.getNombreCompleto(),
              estudiante.getRude(),
              Map.copyOf(notasVisibles),
              notaPeriodo));
    }
    filas.sort(Comparator.comparing(Fila::nombreCompleto, String.CASE_INSENSITIVE_ORDER));

    String cursoEtiqueta = curso.getNombre() + " " + paralelo.getNombre();
    boolean editable = puedeEscribir && periodo.getEstado() == EstadoPeriodoEvaluacion.ABIERTO;

    return new PlanillaMateriaVista(
        materia.getId().valor(),
        materia.getNombre(),
        curso.getId().valor(),
        curso.getNombre(),
        paralelo.getId().valor(),
        paralelo.getNombre(),
        cursoEtiqueta,
        periodo.getId().valor(),
        periodo.getNombre(),
        periodo.getEstado().name(),
        editable,
        columnas,
        List.copyOf(filas));
  }

  private void exigirTripleta(
      UUID tenantId,
      Materia materia,
      UUID cursoId,
      UUID paraleloId,
      UUID actorId,
      boolean veTodasLasMaterias) {
    List<AsignacionMateriaProfesor> asignaciones =
        asignacionMateriaProfesorRepositoryPort.listarPorMateriaYTenant(materia.getId(), tenantId);
    boolean tripleExiste =
        asignaciones.stream()
            .anyMatch(
                a ->
                    a.getCursoId().valor().equals(cursoId)
                        && a.getParaleloId().valor().equals(paraleloId));
    if (!tripleExiste) {
      throw new PlanillaNoEncontradaException();
    }
    if (!veTodasLasMaterias) {
      boolean propio =
          asignaciones.stream()
              .anyMatch(
                  a ->
                      a.getProfesorId().equals(actorId)
                          && a.getCursoId().valor().equals(cursoId)
                          && a.getParaleloId().valor().equals(paraleloId));
      if (!propio) {
        throw new PlanillaNoEncontradaException();
      }
    }
  }

  private List<SeccionCalculoInput> armarSecciones(
      List<SeccionEvaluacion> secciones,
      List<Evaluacion> activasPeriodo,
      Map<UUID, BigDecimal> valorPorEval) {
    Map<UUID, List<Evaluacion>> porSeccion =
        activasPeriodo.stream()
            .collect(Collectors.groupingBy(e -> e.getSeccionEvaluacionId().valor()));
    List<SeccionCalculoInput> inputs = new ArrayList<>();
    for (SeccionEvaluacion seccion : secciones) {
      List<BigDecimal> valores =
          porSeccion.getOrDefault(seccion.getId().valor(), List.of()).stream()
              .map(e -> valorPorEval.get(e.getId().valor()))
              .filter(v -> v != null)
              .toList();
      inputs.add(new SeccionCalculoInput(seccion.getId().valor(), seccion.getNombre(), valores));
    }
    return inputs;
  }
}
