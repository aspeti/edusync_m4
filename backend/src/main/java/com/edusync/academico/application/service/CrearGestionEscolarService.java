package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.in.CrearGestionEscolarUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alta de Gestiones Escolares ({@code FSD-UC-012}) con seed de 3 trimestres
 * ({@code ADR-0013}, {@code DD-UC-015}) en la misma transaccion.
 */
@Service
@RequiredArgsConstructor
public class CrearGestionEscolarService implements CrearGestionEscolarUseCase {

  static final int PERIODOS_SEED = 3;

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;

  @Override
  @Transactional
  public GestionEscolar crear(CrearGestionEscolarCommand command) {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), command.tenantId(), command.nombre(), command.fechaInicio(), command.fechaFin());
    GestionEscolar persistida = gestionEscolarRepositoryPort.guardar(gestionEscolar);
    sembrarTrimestres(persistida);
    return persistida;
  }

  private void sembrarTrimestres(GestionEscolar gestionEscolar) {
    LocalDate inicio = gestionEscolar.getFechaInicio();
    LocalDate fin = gestionEscolar.getFechaFin();
    long daysBetween = ChronoUnit.DAYS.between(inicio, fin);
    long part = Math.max(1, daysBetween / PERIODOS_SEED);
    LocalDate cursor = inicio;
    for (int orden = 1; orden <= PERIODOS_SEED; orden++) {
      LocalDate start = cursor;
      LocalDate end = (orden == PERIODOS_SEED) ? fin : start.plusDays(part).minusDays(1);
      if (!end.isAfter(start)) {
        end = start.plusDays(1);
      }
      if (orden < PERIODOS_SEED && !end.isBefore(fin)) {
        end = fin.minusDays(PERIODOS_SEED - orden);
        if (!end.isAfter(start)) {
          end = start.plusDays(1);
        }
      }
      PeriodoEvaluacion periodo = PeriodoEvaluacion.crear(
          PeriodoEvaluacionId.nueva(),
          gestionEscolar.getTenantId(),
          gestionEscolar.getId(),
          "Trimestre " + orden,
          start,
          end,
          orden);
      periodoEvaluacionRepositoryPort.guardar(periodo);
      cursor = end.plusDays(1);
    }
  }
}
