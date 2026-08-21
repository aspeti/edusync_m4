package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CambiarEstadoPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.PeriodoNoSecuencialException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CambiarEstadoPeriodoEvaluacionService implements CambiarEstadoPeriodoEvaluacionUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;

  @Override
  @Transactional
  public PeriodoEvaluacion cambiarEstado(UUID tenantId, UUID periodoId, EstadoPeriodoEvaluacion nuevoEstado) {
    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);

    if (nuevoEstado == EstadoPeriodoEvaluacion.ABIERTO) {
      List<PeriodoEvaluacion> hermanos = periodoEvaluacionRepositoryPort.listarPorGestionYTenant(
          periodo.getGestionEscolarId(), tenantId);
      if (periodo.getOrden() > 1) {
        PeriodoEvaluacion predecesor = hermanos.stream()
            .filter(p -> p.getOrden() == periodo.getOrden() - 1)
            .findFirst()
            .orElseThrow(PeriodoNoSecuencialException::new);
        if (predecesor.getEstado() != EstadoPeriodoEvaluacion.CERRADO) {
          throw new PeriodoNoSecuencialException();
        }
      }
      boolean otroAbierto = hermanos.stream()
          .anyMatch(p -> p.getEstado() == EstadoPeriodoEvaluacion.ABIERTO && !p.getId().equals(periodo.getId()));
      if (otroAbierto) {
        throw new PeriodoNoSecuencialException();
      }
    }

    periodo.cambiarEstado(nuevoEstado);
    return periodoEvaluacionRepositoryPort.guardar(periodo);
  }
}
