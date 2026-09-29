package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.GuardarParametrosPeriodoUseCase;
import com.edusync.academico.application.port.out.ParametroPeriodoRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.ParametroPeriodo;
import com.edusync.academico.domain.ParametroPeriodoId;
import com.edusync.academico.domain.ParametroRangoInvalidoException;
import com.edusync.academico.domain.ParametrosIncompletosException;
import com.edusync.academico.domain.ParametrosInmutablesException;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.ReglaCombinacionPeriodo;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import com.edusync.academico.domain.SeccionNoEncontradaException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reemplazo atomico de {@link ParametroPeriodo} ({@code DD-UC-029}). */
@Service
@RequiredArgsConstructor
public class GuardarParametrosPeriodoService implements GuardarParametrosPeriodoUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;
  private final ParametroPeriodoRepositoryPort parametroPeriodoRepositoryPort;

  @Override
  @Transactional
  public List<ParametroPeriodo> guardar(UUID tenantId, UUID periodoId, List<Item> parametros) {
    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);
    if (periodo.getEstado() != EstadoPeriodoEvaluacion.PENDIENTE) {
      throw new ParametrosInmutablesException();
    }

    List<SeccionEvaluacion> secciones = seccionEvaluacionRepositoryPort.listarPorGestionYTenant(
        periodo.getGestionEscolarId(), tenantId);
    Map<UUID, SeccionEvaluacion> porId = secciones.stream()
        .collect(Collectors.toMap(seccion -> seccion.getId().valor(), Function.identity()));

    Set<UUID> vistos = new HashSet<>();
    List<ParametroPeriodo> nuevos = parametros.stream().map(item -> {
      SeccionEvaluacion seccion = porId.get(item.seccionEvaluacionId());
      if (seccion == null) {
        throw new SeccionNoEncontradaException();
      }
      if (!vistos.add(item.seccionEvaluacionId())) {
        throw new ParametrosIncompletosException();
      }
      ParametroPeriodo creado = ParametroPeriodo.crear(
          ParametroPeriodoId.nueva(),
          tenantId,
          periodo.getId(),
          SeccionEvaluacionId.de(item.seccionEvaluacionId()),
          item.rangoMin(),
          item.rangoMax(),
          ReglaCombinacionPeriodo.parse(item.reglaCombinacion()));
      if (creado.getRangoMax().compareTo(seccion.getNota()) > 0) {
        throw new ParametroRangoInvalidoException();
      }
      return creado;
    }).toList();

    if (vistos.size() != porId.size()) {
      throw new ParametrosIncompletosException();
    }
    return parametroPeriodoRepositoryPort.reemplazarPorPeriodo(periodo.getId(), tenantId, nuevos);
  }
}
