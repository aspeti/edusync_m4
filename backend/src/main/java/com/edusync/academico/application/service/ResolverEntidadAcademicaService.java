package com.edusync.academico.application.service;

import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.CursoFiltro;
import com.edusync.academico.application.port.in.EstudianteFiltro;
import com.edusync.academico.application.port.in.ListarCursosUseCase;
import com.edusync.academico.application.port.in.ListarEstudiantesUseCase;
import com.edusync.academico.application.port.in.ListarMateriasUseCase;
import com.edusync.academico.application.port.in.ListarParalelosUseCase;
import com.edusync.academico.application.port.in.ListarPeriodosEvaluacionUseCase;
import com.edusync.academico.application.port.in.ListarProfesoresUseCase;
import com.edusync.academico.application.port.in.MateriaFiltro;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarActivaUseCase;
import com.edusync.academico.application.port.in.ProfesorFiltro;
import com.edusync.academico.application.port.in.ResolverEntidadAcademicaUseCase;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.shared.PageQuery;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResolverEntidadAcademicaService implements ResolverEntidadAcademicaUseCase {

  private static final PageQuery PAGINA = PageQuery.of(0, 100);
  private static final Pattern CURSO_PARALELO =
      Pattern.compile("^(.+?)\\s+([a-z0-9]{1,3})$");

  private final ListarEstudiantesUseCase listarEstudiantesUseCase;
  private final ListarMateriasUseCase listarMateriasUseCase;
  private final ListarPeriodosEvaluacionUseCase listarPeriodosEvaluacionUseCase;
  private final ListarProfesoresUseCase listarProfesoresUseCase;
  private final ListarCursosUseCase listarCursosUseCase;
  private final ListarParalelosUseCase listarParalelosUseCase;
  private final ObtenerGestionEscolarActivaUseCase obtenerGestionEscolarActivaUseCase;
  private final InscripcionRepositoryPort inscripcionRepositoryPort;
  private final EtiquetasAcademicas etiquetasAcademicas;

  @Override
  @Transactional(readOnly = true)
  public Resultado resolver(UUID tenantId, String tipo, String q) {
    String tipoNorm = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
    String consulta = q == null ? "" : q.trim();
    return switch (tipoNorm) {
      case "ESTUDIANTE" -> Resultado.de("ESTUDIANTE", estudiantes(tenantId, consulta));
      case "MATERIA" -> Resultado.de("MATERIA", materias(tenantId, consulta));
      case "PERIODO" -> Resultado.de("PERIODO", periodos(tenantId, consulta));
      case "PROFESOR" -> Resultado.de("PROFESOR", profesores(tenantId, consulta));
      case "CURSO_PARALELO" -> Resultado.de("CURSO_PARALELO", cursosParalelos(tenantId, consulta));
      default -> Resultado.de(tipoNorm.isBlank() ? "DESCONOCIDO" : tipoNorm, List.of());
    };
  }

  private List<Match> estudiantes(UUID tenantId, String q) {
    List<Estudiante> candidatos = listarEstudiantesUseCase
        .listar(tenantId, new EstudianteFiltro(q.isBlank() ? null : q, null), PAGINA)
        .content();
    if (candidatos.isEmpty() && !q.isBlank()) {
      candidatos = listarEstudiantesUseCase
          .listar(tenantId, EstudianteFiltro.VACIO, PAGINA)
          .content()
          .stream()
          .filter(e -> NormalizacionTexto.contiene(e.getNombreCompleto(), q))
          .toList();
    } else if (!q.isBlank()) {
      candidatos = candidatos.stream()
          .filter(e -> NormalizacionTexto.contiene(e.getNombreCompleto(), q))
          .toList();
    }
    List<Match> matches = new ArrayList<>();
    for (Estudiante e : candidatos) {
      String extra = etiquetaInscripcion(tenantId, e);
      String etiqueta = extra == null || extra.isBlank() ? e.getNombreCompleto() : extra;
      matches.add(new Match(e.getId().valor(), etiqueta, null, null));
    }
    return matches;
  }

  private String etiquetaInscripcion(UUID tenantId, Estudiante estudiante) {
    List<Inscripcion> inscripciones =
        inscripcionRepositoryPort.listarPorEstudianteYTenant(estudiante.getId(), tenantId);
    if (inscripciones.isEmpty()) {
      return estudiante.getNombreCompleto();
    }
    Inscripcion ultima = inscripciones.getLast();
    String curso = etiquetasAcademicas.nombreCurso(tenantId, ultima.getCursoId().valor());
    String paralelo = etiquetasAcademicas.nombreParalelo(tenantId, ultima.getParaleloId().valor());
    String lugar = joinNombre(curso, paralelo);
    return lugar.isBlank()
        ? estudiante.getNombreCompleto()
        : estudiante.getNombreCompleto() + " — " + lugar;
  }

  private List<Match> materias(UUID tenantId, String q) {
    List<Materia> lista = listarMateriasUseCase
        .listar(tenantId, new MateriaFiltro(q.isBlank() ? null : q), PAGINA)
        .content();
    if (lista.isEmpty() && !q.isBlank()) {
      lista = listarMateriasUseCase.listar(tenantId, MateriaFiltro.VACIO, PAGINA).content().stream()
          .filter(m -> NormalizacionTexto.contiene(m.getNombre(), q))
          .toList();
    } else if (!q.isBlank()) {
      lista = lista.stream().filter(m -> NormalizacionTexto.contiene(m.getNombre(), q)).toList();
    }
    return lista.stream()
        .map(m -> new Match(m.getId().valor(), m.getNombre(), null, null))
        .toList();
  }

  private List<Match> periodos(UUID tenantId, String q) {
    GestionEscolar activa;
    try {
      activa = obtenerGestionEscolarActivaUseCase.obtener(tenantId);
    } catch (RuntimeException e) {
      return List.of();
    }
    List<PeriodoEvaluacion> periodos =
        listarPeriodosEvaluacionUseCase.listar(tenantId, activa.getId().valor());
    return periodos.stream()
        .filter(p -> coincidePeriodo(p, q))
        .map(p -> new Match(p.getId().valor(), p.getNombre(), null, null))
        .toList();
  }

  static boolean coincidePeriodo(PeriodoEvaluacion periodo, String q) {
    if (q == null || q.isBlank()) {
      return periodo.getEstado() == EstadoPeriodoEvaluacion.ABIERTO;
    }
    String nq = NormalizacionTexto.normalizar(q);
    if (NormalizacionTexto.contiene(periodo.getNombre(), q)
        || NormalizacionTexto.contiene(q, periodo.getNombre())) {
      return true;
    }
    int orden = periodo.getOrden();
    if (orden == 1 && nq.matches(".*(primer|1er|1ero|trimestre 1|t1|1\\b).*")) {
      return true;
    }
    if (orden == 2 && nq.matches(".*(segund|2do|2o |trimestre 2|t2|2\\b).*")) {
      return true;
    }
    if (orden == 3 && nq.matches(".*(tercer|3er|3o |trimestre 3|t3|3\\b).*")) {
      return true;
    }
    if ((nq.contains("este trimestre") || nq.contains("este periodo") || nq.equals("este"))
        && periodo.getEstado() == EstadoPeriodoEvaluacion.ABIERTO) {
      return true;
    }
    return false;
  }

  private List<Match> profesores(UUID tenantId, String q) {
    List<ProfesorResumen> lista = listarProfesoresUseCase
        .listar(tenantId, new ProfesorFiltro(q.isBlank() ? null : q, null), PAGINA)
        .content();
    if (lista.isEmpty() && !q.isBlank()) {
      lista = listarProfesoresUseCase
          .listar(tenantId, ProfesorFiltro.VACIO, PAGINA)
          .content()
          .stream()
          .filter(p -> NormalizacionTexto.contiene(p.nombreCompleto(), q))
          .toList();
    } else if (!q.isBlank()) {
      lista = lista.stream()
          .filter(p -> NormalizacionTexto.contiene(p.nombreCompleto(), q))
          .toList();
    }
    return lista.stream()
        .map(p -> new Match(p.id(), p.nombreCompleto(), null, null))
        .toList();
  }

  private List<Match> cursosParalelos(UUID tenantId, String q) {
    if (q.isBlank()) {
      return List.of();
    }
    String nq = NormalizacionTexto.normalizar(q);
    Matcher matcher = CURSO_PARALELO.matcher(nq);
    String cursoQ = nq;
    String paraleloQ = null;
    if (matcher.matches()) {
      cursoQ = matcher.group(1).trim();
      paraleloQ = matcher.group(2).trim();
    }
    final String cursoQuery = cursoQ;
    final String paraleloQuery = paraleloQ;
    List<Curso> cursos = listarCursosUseCase
        .listar(tenantId, new CursoFiltro(cursoQuery), PAGINA)
        .content();
    if (cursos.isEmpty()) {
      cursos = listarCursosUseCase.listar(tenantId, CursoFiltro.VACIO, PAGINA).content().stream()
          .filter(c -> NormalizacionTexto.contiene(c.getNombre(), cursoQuery))
          .toList();
    }
    Map<UUID, Match> unicos = new LinkedHashMap<>();
    for (Curso curso : cursos) {
      for (Paralelo paralelo : listarParalelosUseCase.listar(tenantId, curso.getId().valor())) {
        String etiqueta = joinNombre(curso.getNombre(), paralelo.getNombre());
        boolean okParalelo =
            paraleloQuery == null
                || NormalizacionTexto.normalizar(paralelo.getNombre()).equals(paraleloQuery)
                || NormalizacionTexto.contiene(paralelo.getNombre(), paraleloQuery);
        boolean okEtiqueta = NormalizacionTexto.contiene(etiqueta, q);
        if (okParalelo && (paraleloQuery != null || okEtiqueta || NormalizacionTexto.contiene(curso.getNombre(), q))) {
          unicos.put(
              paralelo.getId().valor(),
              new Match(
                  paralelo.getId().valor(),
                  etiqueta,
                  curso.getId().valor(),
                  paralelo.getId().valor()));
        }
      }
    }
    return List.copyOf(unicos.values());
  }

  private static String joinNombre(String curso, String paralelo) {
    if (curso == null || curso.isBlank()) {
      return paralelo == null ? "" : paralelo;
    }
    if (paralelo == null || paralelo.isBlank()) {
      return curso;
    }
    return curso + " " + paralelo;
  }
}
