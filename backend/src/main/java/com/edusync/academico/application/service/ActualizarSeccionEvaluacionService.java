package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ActualizarSeccionEvaluacionCommand;
import com.edusync.academico.application.port.in.ActualizarSeccionEvaluacionUseCase;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import com.edusync.academico.domain.SeccionNoEncontradaException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActualizarSeccionEvaluacionService implements ActualizarSeccionEvaluacionUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;

  @Override
  @Transactional
  public SeccionEvaluacion actualizar(ActualizarSeccionEvaluacionCommand command) {
    SeccionEvaluacion seccion = seccionEvaluacionRepositoryPort
        .buscarPorIdYTenant(SeccionEvaluacionId.de(command.seccionId()), command.tenantId())
        .orElseThrow(SeccionNoEncontradaException::new);

    SeccionEvaluacionPolitica.exigirMutables(
        periodoEvaluacionRepositoryPort.listarPorGestionYTenant(
            seccion.getGestionEscolarId(), command.tenantId()));

    String nombre = command.nombre() != null ? command.nombre() : seccion.getNombre();
    BigDecimal nota = command.nota() != null ? command.nota() : seccion.getNota();
    seccion.actualizar(nombre, nota);

    List<SeccionEvaluacion> hermanas = seccionEvaluacionRepositoryPort.listarPorGestionYTenant(
        seccion.getGestionEscolarId(), command.tenantId());
    List<SeccionEvaluacion> resultantes = hermanas.stream()
        .map(s -> s.getId().equals(seccion.getId()) ? seccion : s)
        .toList();
    SeccionEvaluacionPolitica.exigirSumaCien(resultantes);
    return seccionEvaluacionRepositoryPort.guardar(seccion);
  }
}
