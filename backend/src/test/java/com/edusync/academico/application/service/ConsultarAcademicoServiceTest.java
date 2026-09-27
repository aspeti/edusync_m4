package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase;
import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase.Comando;
import com.edusync.academico.application.port.in.ConsultarAcademicoUseCase.Operacion;
import com.edusync.academico.application.port.in.ObtenerGestionEscolarActivaUseCase;
import com.edusync.academico.application.port.in.ObtenerNotaProvisionalUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstudianteId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarAcademicoServiceTest {

  @Mock ObtenerNotaProvisionalUseCase obtenerNotaProvisionalUseCase;
  @Mock ObtenerGestionEscolarActivaUseCase obtenerGestionEscolarActivaUseCase;
  @Mock EstudianteRepositoryPort estudianteRepositoryPort;
  @Mock MateriaRepositoryPort materiaRepositoryPort;
  @Mock PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  @Mock ParaleloRepositoryPort paraleloRepositoryPort;
  @Mock InscripcionRepositoryPort inscripcionRepositoryPort;
  @Mock AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  @Mock MateriaAccesoService materiaAccesoService;

  @InjectMocks ConsultarAcademicoService service;

  @Test
  void uuidDeOtroTenantNoDevuelveNotas() {
    UUID tenantId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    UUID estudianteAjeno = UUID.randomUUID();
    when(estudianteRepositoryPort.buscarPorIdYTenant(EstudianteId.de(estudianteAjeno), tenantId))
        .thenReturn(Optional.empty());

    ConsultarAcademicoUseCase.Resultado r =
        service.consultar(
            tenantId,
            actorId,
            true,
            new Comando(
                Operacion.NOTAS,
                estudianteAjeno,
                null,
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null));

    assertThat(r.estado()).isEqualTo(ConsultarAcademicoUseCase.Resultado.SIN_RESULTADOS);
    assertThat(r.filas()).isEmpty();
  }

  @Test
  void sinEstudiantePideAclaracion() {
    ConsultarAcademicoUseCase.Resultado r =
        service.consultar(
            UUID.randomUUID(),
            UUID.randomUUID(),
            true,
            new Comando(Operacion.NOTAS, null, null, null, null, null, null, null));

    assertThat(r.estado()).isEqualTo(ConsultarAcademicoUseCase.Resultado.FALTA_CONTEXTO);
    assertThat(r.faltantes()).contains("estudianteId");
  }
}
