package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearEstudianteCommand;
import com.edusync.academico.application.port.in.CrearEstudianteUseCase;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.RudeDuplicadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa el alta de Estudiantes ({@code FSD-UC-020}, paso 1). */
@Service
@RequiredArgsConstructor
public class CrearEstudianteService implements CrearEstudianteUseCase {

  private final EstudianteRepositoryPort estudianteRepositoryPort;

  @Override
  @Transactional
  public Estudiante crear(CrearEstudianteCommand command) {
    if (estudianteRepositoryPort.existePorRudeYTenant(command.rude(), command.tenantId())) {
      throw new RudeDuplicadoException();
    }
    Estudiante estudiante =
        Estudiante.crear(
            EstudianteId.nueva(),
            command.tenantId(),
            command.rude(),
            command.nombreCompleto(),
            command.estado(),
            command.datosPersonales());
    return estudianteRepositoryPort.guardar(estudiante);
  }
}
