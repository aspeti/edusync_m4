package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CambiarEstadoPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code DD-UC-019}/{@code DD-UC-034}: apertura sin secuencialidad; al abrir exige Σ
 * secciones = 100, rangos validos ({@code ADR-0027}) y cobertura docente.
 */
@Service
@RequiredArgsConstructor
public class CambiarEstadoPeriodoEvaluacionService implements CambiarEstadoPeriodoEvaluacionUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;
  private final AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;

  @Override
  @Transactional
  public PeriodoEvaluacion cambiarEstado(UUID tenantId, UUID periodoId, EstadoPeriodoEvaluacion nuevoEstado) {
    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);

    if (nuevoEstado == EstadoPeriodoEvaluacion.ABIERTO) {
      List<SeccionEvaluacion> secciones = seccionEvaluacionRepositoryPort.listarPorGestionYTenant(
          periodo.getGestionEscolarId(), tenantId);
      SeccionEvaluacionPolitica.exigirSumaCien(secciones);
      AperturaPeriodoPolitica.exigirRangosSecciones(secciones);
      AperturaPeriodoPolitica.exigirCoberturaDocente(
          asignacionMateriaCursoRepositoryPort, asignacionMateriaProfesorRepositoryPort, tenantId);
    }

    periodo.cambiarEstado(nuevoEstado);
    return periodoEvaluacionRepositoryPort.guardar(periodo);
  }
}
