package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.ListarCursosUseCase;
import com.edusync.academico.application.port.in.ListarEstudiantesUseCase;
import com.edusync.academico.application.port.in.ListarMateriasUseCase;
import com.edusync.academico.application.port.in.ListarParalelosUseCase;
import com.edusync.academico.application.port.in.ListarPeriodosEvaluacionUseCase;
import com.edusync.academico.application.port.in.ListarProfesoresUseCase;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarActivaUseCase;
import com.edusync.academico.application.port.in.ProfesorFiltro;
import com.edusync.academico.application.port.in.ResolverEntidadAcademicaUseCase;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.domain.EstadoEstudiante;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolverEntidadAcademicaServiceTest {

  @Mock ListarEstudiantesUseCase listarEstudiantesUseCase;
  @Mock ListarMateriasUseCase listarMateriasUseCase;
  @Mock ListarPeriodosEvaluacionUseCase listarPeriodosEvaluacionUseCase;
  @Mock ListarProfesoresUseCase listarProfesoresUseCase;
  @Mock ListarCursosUseCase listarCursosUseCase;
  @Mock ListarParalelosUseCase listarParalelosUseCase;
  @Mock ObtenerGestionEscolarActivaUseCase obtenerGestionEscolarActivaUseCase;
  @Mock InscripcionRepositoryPort inscripcionRepositoryPort;
  @Mock EtiquetasAcademicas etiquetasAcademicas;

  private ResolverEntidadAcademicaService service;
  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    service = new ResolverEntidadAcademicaService(
        listarEstudiantesUseCase,
        listarMateriasUseCase,
        listarPeriodosEvaluacionUseCase,
        listarProfesoresUseCase,
        listarCursosUseCase,
        listarParalelosUseCase,
        obtenerGestionEscolarActivaUseCase,
        inscripcionRepositoryPort,
        etiquetasAcademicas);
  }

  @Test
  void dosJuan_ambiguo() {
    Estudiante a = estudiante("Juan Perez");
    Estudiante b = estudiante("Juan Ramirez");
    when(listarEstudiantesUseCase.listar(eq(tenantId), any(), any()))
        .thenReturn(PageResult.of(List.of(a, b), PageQuery.of(0, 100), 2));
    when(inscripcionRepositoryPort.listarPorEstudianteYTenant(any(), eq(tenantId))).thenReturn(List.of());

    ResolverEntidadAcademicaUseCase.Resultado r = service.resolver(tenantId, "ESTUDIANTE", "Juan");

    assertThat(r.estado()).isEqualTo(ResolverEntidadAcademicaUseCase.Resultado.AMBIGUO);
    assertThat(r.matches()).hasSize(2);
    assertThat(r.matches()).allSatisfy(m -> assertThat(m.etiqueta()).doesNotContain("rude"));
  }

  @Test
  void profesorConTilde_matcheaFabian() {
    ProfesorResumen fabian = new ProfesorResumen(UUID.randomUUID(), "Fabián López", true);
    when(listarProfesoresUseCase.listar(eq(tenantId), eq(new ProfesorFiltro("fabian", null)), any()))
        .thenReturn(PageResult.of(List.of(), PageQuery.of(0, 100), 0));
    when(listarProfesoresUseCase.listar(eq(tenantId), eq(ProfesorFiltro.VACIO), any()))
        .thenReturn(PageResult.of(List.of(fabian), PageQuery.of(0, 100), 1));

    ResolverEntidadAcademicaUseCase.Resultado r = service.resolver(tenantId, "PROFESOR", "fabian");

    assertThat(r.estado()).isEqualTo(ResolverEntidadAcademicaUseCase.Resultado.UNICO);
    assertThat(r.matches().getFirst().etiqueta()).isEqualTo("Fabián López");
  }

  @Test
  void primerTrimestre_resuelveOrden1DeGestionActiva() {
    UUID gestionId = UUID.randomUUID();
    GestionEscolar activa = GestionEscolar.reconstruir(
        GestionEscolarId.de(gestionId),
        tenantId,
        "2026",
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 11, 30),
        EstadoGestionEscolar.ACTIVA);
    PeriodoEvaluacion t1 = PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.de(UUID.randomUUID()),
        tenantId,
        GestionEscolarId.de(gestionId),
        "Trimestre 1",
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 5, 1),
        1,
        EstadoPeriodoEvaluacion.ABIERTO);
    PeriodoEvaluacion t2 = PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.de(UUID.randomUUID()),
        tenantId,
        GestionEscolarId.de(gestionId),
        "Trimestre 2",
        LocalDate.of(2026, 5, 2),
        LocalDate.of(2026, 8, 1),
        2,
        EstadoPeriodoEvaluacion.PENDIENTE);
    when(obtenerGestionEscolarActivaUseCase.obtener(tenantId)).thenReturn(activa);
    when(listarPeriodosEvaluacionUseCase.listar(tenantId, gestionId)).thenReturn(List.of(t1, t2));

    ResolverEntidadAcademicaUseCase.Resultado r = service.resolver(tenantId, "PERIODO", "primer trimestre");

    assertThat(r.estado()).isEqualTo(ResolverEntidadAcademicaUseCase.Resultado.UNICO);
    assertThat(r.matches().getFirst().id()).isEqualTo(t1.getId().valor());
  }

  private Estudiante estudiante(String nombre) {
    return Estudiante.reconstruir(
        EstudianteId.de(UUID.randomUUID()),
        tenantId,
        "0000001",
        nombre,
        EstadoEstudiante.ACTIVO,
        Map.of());
  }
}
