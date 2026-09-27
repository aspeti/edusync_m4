package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarActivaUseCase;
import com.edusync.academico.application.port.in.ObtenerNotaProvisionalUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.CalculoNotas;
import com.edusync.academico.domain.EstadoInscripcion;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.shared.exception.DomainException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultarAcademicoService implements ConsultarAcademicoUseCase {

  static final int UMBRAL_DEFAULT = 51;

  private final ObtenerNotaProvisionalUseCase obtenerNotaProvisionalUseCase;
  private final ObtenerGestionEscolarActivaUseCase obtenerGestionEscolarActivaUseCase;
  private final EstudianteRepositoryPort estudianteRepositoryPort;
  private final MateriaRepositoryPort materiaRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;
  private final InscripcionRepositoryPort inscripcionRepositoryPort;
  private final AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  private final MateriaAccesoService materiaAccesoService;

  @Override
  @Transactional(readOnly = true)
  public Resultado consultar(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando) {
    if (comando == null || comando.operacion() == null) {
      return Resultado.falta("Indica qué quieres consultar (notas, promedio, nómina…).", List.of("operacion"));
    }
    Operacion op = comando.operacion();
    int umbral = comando.umbral() == null ? UMBRAL_DEFAULT : comando.umbral();
    return switch (op) {
      case NOMINA -> nomina(tenantId, comando);
      case MATERIAS_ESTUDIANTE -> materiasEstudiante(tenantId, comando);
      case NOTAS -> notas(tenantId, actorId, veTodasLasMaterias, comando);
      case PROMEDIO -> promedio(tenantId, actorId, veTodasLasMaterias, comando);
      case REPROBADOS -> reprobados(tenantId, actorId, veTodasLasMaterias, comando, umbral);
      case TOP -> top(tenantId, actorId, veTodasLasMaterias, comando);
    };
  }

  private Resultado nomina(UUID tenantId, Comando comando) {
    if (comando.paraleloId() == null) {
      return Resultado.falta("¿De qué curso y paralelo? Por ejemplo: 1ro A.", List.of("paraleloId"));
    }
    Optional<Paralelo> paralelo =
        paraleloRepositoryPort.buscarPorIdYTenant(ParaleloId.de(comando.paraleloId()), tenantId);
    if (paralelo.isEmpty()) {
      return Resultado.vacio(Operacion.NOMINA.name());
    }
    Optional<GestionEscolar> gestion = gestion(tenantId, comando.gestionEscolarId());
    if (gestion.isEmpty()) {
      return Resultado.falta("No hay una gestión escolar activa.", List.of("gestionEscolarId"));
    }
    List<Inscripcion> inscripciones =
        nominaParalelo(tenantId, gestion.get(), paralelo.get());
    List<EstudianteId> ids = inscripciones.stream().map(Inscripcion::getEstudianteId).toList();
    List<Estudiante> estudiantes = estudianteRepositoryPort.listarPorIdsYTenant(ids, tenantId);
    List<Fila> filas =
        estudiantes.stream()
            .sorted(Comparator.comparing(Estudiante::getNombreCompleto))
            .map(e -> new Fila(e.getNombreCompleto(), null, null))
            .toList();
    if (filas.isEmpty()) {
      return Resultado.vacio(Operacion.NOMINA.name());
    }
    return Resultado.ok(Operacion.NOMINA.name(), filas.size(), filas);
  }

  private Resultado materiasEstudiante(UUID tenantId, Comando comando) {
    if (comando.estudianteId() == null) {
      return Resultado.falta("¿De qué estudiante?", List.of("estudianteId"));
    }
    Optional<Estudiante> estudiante =
        estudianteRepositoryPort.buscarPorIdYTenant(EstudianteId.de(comando.estudianteId()), tenantId);
    if (estudiante.isEmpty()) {
      return Resultado.vacio(Operacion.MATERIAS_ESTUDIANTE.name());
    }
    Optional<GestionEscolar> gestion = gestion(tenantId, comando.gestionEscolarId());
    if (gestion.isEmpty()) {
      return Resultado.falta("No hay una gestión escolar activa.", List.of("gestionEscolarId"));
    }
    Optional<Inscripcion> inscripcion =
        inscripcionRepositoryPort
            .listarPorEstudianteYTenant(estudiante.get().getId(), tenantId)
            .stream()
            .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
            .filter(i -> i.getGestionEscolarId().equals(gestion.get().getId()))
            .findFirst();
    if (inscripcion.isEmpty()) {
      return Resultado.vacio(Operacion.MATERIAS_ESTUDIANTE.name());
    }
    List<AsignacionMateriaCurso> asignaciones =
        asignacionMateriaCursoRepositoryPort.listarPorCursoParaleloYTenant(
            inscripcion.get().getCursoId(), inscripcion.get().getParaleloId(), tenantId);
    List<Fila> filas = new ArrayList<>();
    for (AsignacionMateriaCurso a : asignaciones) {
      materiaRepositoryPort
          .buscarPorIdYTenant(a.getMateriaId(), tenantId)
          .ifPresent(m -> filas.add(new Fila(m.getNombre(), null, null)));
    }
    if (filas.isEmpty()) {
      return Resultado.vacio(Operacion.MATERIAS_ESTUDIANTE.name());
    }
    return Resultado.ok(Operacion.MATERIAS_ESTUDIANTE.name(), filas.size(), filas);
  }

  private Resultado notas(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando) {
    if (comando.estudianteId() == null) {
      return Resultado.falta("¿De qué estudiante?", List.of("estudianteId"));
    }
    Optional<Estudiante> estudiante =
        estudianteRepositoryPort.buscarPorIdYTenant(EstudianteId.de(comando.estudianteId()), tenantId);
    if (estudiante.isEmpty()) {
      return Resultado.vacio(Operacion.NOTAS.name());
    }
    UUID periodoId = comando.periodoEvaluacionId();
    if (periodoId == null) {
      Optional<PeriodoEvaluacion> abierto = periodoAbierto(tenantId, comando.gestionEscolarId());
      if (abierto.isEmpty()) {
        return Resultado.falta("¿De qué período o trimestre?", List.of("periodoEvaluacionId"));
      }
      periodoId = abierto.get().getId().valor();
    } else if (periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .isEmpty()) {
      return Resultado.vacio(Operacion.NOTAS.name());
    }
    List<Materia> materias = materiasObjetivo(tenantId, actorId, veTodasLasMaterias, comando, estudiante.get());
    if (materias.isEmpty()) {
      return Resultado.falta("¿En qué materia?", List.of("materiaId"));
    }
    List<Fila> filas = new ArrayList<>();
    for (Materia materia : materias) {
      Optional<Integer> nota =
          notaPeriodo(tenantId, actorId, veTodasLasMaterias, materia.getId().valor(),
              estudiante.get().getId().valor(), periodoId);
      nota.ifPresent(
          valor -> filas.add(new Fila(estudiante.get().getNombreCompleto(), valor, materia.getNombre())));
    }
    if (filas.isEmpty()) {
      return Resultado.vacio(Operacion.NOTAS.name());
    }
    return Resultado.ok(Operacion.NOTAS.name(), filas.size() == 1 ? filas.getFirst().valor() : null, filas);
  }

  private Resultado promedio(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando) {
    if (comando.periodoEvaluacionId() == null
        && periodoAbierto(tenantId, comando.gestionEscolarId()).isEmpty()) {
      return Resultado.falta("¿De qué período o trimestre?", List.of("periodoEvaluacionId"));
    }
    UUID periodoId =
        comando.periodoEvaluacionId() != null
            ? comando.periodoEvaluacionId()
            : periodoAbierto(tenantId, comando.gestionEscolarId()).orElseThrow().getId().valor();
    if (periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .isEmpty()) {
      return Resultado.vacio(Operacion.PROMEDIO.name());
    }
    if (comando.estudianteId() != null) {
      Resultado notas = notas(tenantId, actorId, veTodasLasMaterias,
          new Comando(Operacion.NOTAS, comando.estudianteId(), comando.cursoId(), comando.paraleloId(),
              comando.materiaId(), periodoId, comando.gestionEscolarId(), comando.umbral()));
      if (!Resultado.OK.equals(notas.estado()) || notas.filas().isEmpty()) {
        return notas.estado() == null ? Resultado.vacio(Operacion.PROMEDIO.name()) : notas;
      }
      int suma = notas.filas().stream().filter(f -> f.valor() != null).mapToInt(Fila::valor).sum();
      long n = notas.filas().stream().filter(f -> f.valor() != null).count();
      if (n == 0) {
        return Resultado.vacio(Operacion.PROMEDIO.name());
      }
      int promedio = (int) Math.round(suma / (double) n);
      return Resultado.ok(Operacion.PROMEDIO.name(), promedio, notas.filas());
    }
    if (comando.paraleloId() == null) {
      return Resultado.falta("¿De qué estudiante o curso/paralelo?", List.of("estudianteId", "paraleloId"));
    }
    List<Integer> valores = notasDelParalelo(tenantId, actorId, veTodasLasMaterias, comando, periodoId);
    if (valores.isEmpty()) {
      return Resultado.vacio(Operacion.PROMEDIO.name());
    }
    int promedio = (int) Math.round(valores.stream().mapToInt(i -> i).average().orElse(0));
    return Resultado.ok(
        Operacion.PROMEDIO.name(),
        promedio,
        List.of(new Fila("Promedio del grupo", promedio, valores.size() + " estudiantes")));
  }

  private Resultado reprobados(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando, int umbral) {
    if (comando.materiaId() == null) {
      return Resultado.falta("¿En qué materia?", List.of("materiaId"));
    }
    if (comando.periodoEvaluacionId() == null
        && periodoAbierto(tenantId, comando.gestionEscolarId()).isEmpty()) {
      return Resultado.falta("¿De qué período o trimestre?", List.of("periodoEvaluacionId"));
    }
    UUID periodoId =
        comando.periodoEvaluacionId() != null
            ? comando.periodoEvaluacionId()
            : periodoAbierto(tenantId, comando.gestionEscolarId()).orElseThrow().getId().valor();
    List<NotaEstudiante> notas = notasPorEstudiante(tenantId, actorId, veTodasLasMaterias, comando, periodoId);
    List<Fila> filas =
        notas.stream()
            .filter(n -> n.valor() < umbral)
            .map(n -> new Fila(n.nombre(), n.valor(), "por debajo de " + umbral))
            .toList();
    if (filas.isEmpty()) {
      return Resultado.vacio(Operacion.REPROBADOS.name());
    }
    return Resultado.ok(Operacion.REPROBADOS.name(), filas.size(), filas);
  }

  private Resultado top(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando) {
    if (comando.paraleloId() == null) {
      return Resultado.falta("¿De qué curso y paralelo?", List.of("paraleloId"));
    }
    if (comando.periodoEvaluacionId() == null
        && periodoAbierto(tenantId, comando.gestionEscolarId()).isEmpty()) {
      return Resultado.falta("¿De qué período o trimestre?", List.of("periodoEvaluacionId"));
    }
    UUID periodoId =
        comando.periodoEvaluacionId() != null
            ? comando.periodoEvaluacionId()
            : periodoAbierto(tenantId, comando.gestionEscolarId()).orElseThrow().getId().valor();
    List<NotaEstudiante> notas = notasPorEstudiante(tenantId, actorId, veTodasLasMaterias, comando, periodoId);
    Optional<NotaEstudiante> mejor =
        notas.stream().max(Comparator.comparingInt(NotaEstudiante::valor));
    if (mejor.isEmpty()) {
      return Resultado.vacio(Operacion.TOP.name());
    }
    NotaEstudiante n = mejor.get();
    return Resultado.ok(
        Operacion.TOP.name(),
        n.valor(),
        List.of(new Fila(n.nombre(), n.valor(), "promedio más alto")));
  }

  private List<NotaEstudiante> notasPorEstudiante(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando, UUID periodoId) {
    Optional<Paralelo> paralelo =
        comando.paraleloId() == null
            ? Optional.empty()
            : paraleloRepositoryPort.buscarPorIdYTenant(ParaleloId.de(comando.paraleloId()), tenantId);
    Optional<GestionEscolar> gestion = gestion(tenantId, comando.gestionEscolarId());
    if (paralelo.isEmpty() || gestion.isEmpty()) {
      return List.of();
    }
    List<Inscripcion> inscripciones = nominaParalelo(tenantId, gestion.get(), paralelo.get());
    List<Materia> materiasFiltro = materiasDeParalelo(tenantId, actorId, veTodasLasMaterias, comando, paralelo.get());
    List<NotaEstudiante> out = new ArrayList<>();
    for (Inscripcion inscripcion : inscripciones) {
      Optional<Estudiante> estudiante =
          estudianteRepositoryPort.buscarPorIdYTenant(inscripcion.getEstudianteId(), tenantId);
      if (estudiante.isEmpty()) {
        continue;
      }
      List<Integer> valores = new ArrayList<>();
      for (Materia materia : materiasFiltro) {
        notaPeriodo(
                tenantId,
                actorId,
                veTodasLasMaterias,
                materia.getId().valor(),
                estudiante.get().getId().valor(),
                periodoId)
            .ifPresent(valores::add);
      }
      if (valores.isEmpty()) {
        continue;
      }
      int promedio = (int) Math.round(valores.stream().mapToInt(i -> i).average().orElse(0));
      out.add(new NotaEstudiante(estudiante.get().getNombreCompleto(), promedio));
    }
    return out;
  }

  private List<Integer> notasDelParalelo(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando, UUID periodoId) {
    return notasPorEstudiante(tenantId, actorId, veTodasLasMaterias, comando, periodoId).stream()
        .map(NotaEstudiante::valor)
        .toList();
  }

  private List<Materia> materiasObjetivo(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando, Estudiante estudiante) {
    if (comando.materiaId() != null) {
      try {
        Materia materia = materiaAccesoService.exigirMateria(tenantId, comando.materiaId());
        materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);
        return List.of(materia);
      } catch (DomainException e) {
        return List.of();
      }
    }
    Optional<GestionEscolar> gestion = gestion(tenantId, comando.gestionEscolarId());
    if (gestion.isEmpty()) {
      return List.of();
    }
    return inscripcionRepositoryPort.listarPorEstudianteYTenant(estudiante.getId(), tenantId).stream()
        .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
        .filter(i -> i.getGestionEscolarId().equals(gestion.get().getId()))
        .findFirst()
        .map(i -> materiasDeParalelo(tenantId, actorId, veTodasLasMaterias, comando,
            paraleloRepositoryPort.buscarPorIdYTenant(i.getParaleloId(), tenantId).orElse(null)))
        .orElse(List.of());
  }

  private List<Materia> materiasDeParalelo(
      UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando, Paralelo paralelo) {
    if (paralelo == null) {
      return List.of();
    }
    if (comando.materiaId() != null) {
      try {
        Materia materia = materiaAccesoService.exigirMateria(tenantId, comando.materiaId());
        materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);
        return List.of(materia);
      } catch (DomainException e) {
        return List.of();
      }
    }
    List<Materia> materias = new ArrayList<>();
    for (AsignacionMateriaCurso a :
        asignacionMateriaCursoRepositoryPort.listarPorCursoParaleloYTenant(
            paralelo.getCursoId(), paralelo.getId(), tenantId)) {
      try {
        Materia materia = materiaAccesoService.exigirMateria(tenantId, a.getMateriaId().valor());
        materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);
        materias.add(materia);
      } catch (DomainException ignored) {
        // profesor sin acceso: se omite
      }
    }
    return materias;
  }

  private Optional<Integer> notaPeriodo(
      UUID tenantId,
      UUID actorId,
      boolean veTodasLasMaterias,
      UUID materiaId,
      UUID estudianteId,
      UUID periodoId) {
    try {
      CalculoNotas.NotaProvisional vista =
          obtenerNotaProvisionalUseCase.obtener(
              tenantId, materiaId, estudianteId, periodoId, actorId, veTodasLasMaterias);
      return Optional.ofNullable(vista.notaPeriodo());
    } catch (DomainException e) {
      return Optional.empty();
    }
  }

  private List<Inscripcion> nominaParalelo(UUID tenantId, GestionEscolar gestion, Paralelo paralelo) {
    return inscripcionRepositoryPort.listarActivasPorGestionYParesCursoParalelo(
        gestion.getId(),
        tenantId,
        Set.of(new InscripcionRepositoryPort.CursoParaleloPar(paralelo.getCursoId(), paralelo.getId())));
  }

  private Optional<GestionEscolar> gestion(UUID tenantId, UUID gestionEscolarId) {
    try {
      return Optional.of(obtenerGestionEscolarActivaUseCase.obtener(tenantId));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private Optional<PeriodoEvaluacion> periodoAbierto(UUID tenantId, UUID gestionEscolarId) {
    Optional<GestionEscolar> gestion = gestion(tenantId, gestionEscolarId);
    if (gestion.isEmpty()) {
      return Optional.empty();
    }
    List<PeriodoEvaluacion> abiertos =
        periodoEvaluacionRepositoryPort
            .listarPorGestionYTenant(gestion.get().getId(), tenantId)
            .stream()
            .filter(p -> p.getEstado() == com.edusync.academico.domain.EstadoPeriodoEvaluacion.ABIERTO)
            .toList();
    if (abiertos.size() != 1) {
      return Optional.empty();
    }
    return Optional.of(abiertos.getFirst());
  }

  private record NotaEstudiante(String nombre, int valor) {}
}
