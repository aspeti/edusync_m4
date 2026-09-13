package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CambiarEstadoPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code DD-UC-019}: la secuencialidad de apertura ({@code E_PERIODO_NO_SECUENCIAL} —
 * predecesor cerrado, un solo periodo {@code ABIERTO} a la vez) se elimino: este endpoint es
 * exclusivamente {@code ADMIN} y el requisito de negocio es que pueda abrir/cerrar cualquier
 * periodo en cualquier orden. Se conserva la exigencia de secciones sumando 100
 * ({@code E_SUMA_SECCIONES_INVALIDA}) al abrir un periodo: es un requisito de integridad del
 * motor de calculo ({@code ADR-0013}), no una restriccion de flujo/edicion.
 */
@Service
@RequiredArgsConstructor
public class CambiarEstadoPeriodoEvaluacionService implements CambiarEstadoPeriodoEvaluacionUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;

  @Override
  @Transactional
  public PeriodoEvaluacion cambiarEstado(UUID tenantId, UUID periodoId, EstadoPeriodoEvaluacion nuevoEstado) {
    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);

    if (nuevoEstado == EstadoPeriodoEvaluacion.ABIERTO) {
      SeccionEvaluacionPolitica.exigirSumaCien(
          seccionEvaluacionRepositoryPort.listarPorGestionYTenant(periodo.getGestionEscolarId(), tenantId));
    }

    periodo.cambiarEstado(nuevoEstado);
    return periodoEvaluacionRepositoryPort.guardar(periodo);
  }
}
