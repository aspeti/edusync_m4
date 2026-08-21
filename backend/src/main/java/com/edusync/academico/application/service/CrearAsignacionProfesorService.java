package com.edusync.academico.application.service;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.application.port.in.CrearAsignacionProfesorCommand;
import com.edusync.academico.application.port.in.CrearAsignacionProfesorUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.AsignacionMateriaProfesorId;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import com.edusync.academico.domain.MateriaSinCursoException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ParaleloNoEncontradoException;
import com.edusync.academico.domain.ProfesorNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa la asignacion Materia → Profesor ({@code FSD-UC-018}, paso 3). Exige una
 * {@code AsignacionMateriaCurso} previa para el mismo {@code (cursoId, paraleloId)} (A1:
 * {@code 409 E_MATERIA_SIN_CURSO}) y que el usuario sea profesor activo del tenant.
 */
@Service
@RequiredArgsConstructor
public class CrearAsignacionProfesorService implements CrearAsignacionProfesorUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;
  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;
  private final AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private final ProfesorConsultaPort profesorConsultaPort;

  @Override
  @Transactional
  public AsignacionMateriaProfesor crear(CrearAsignacionProfesorCommand command) {
    MateriaId materiaId = MateriaId.de(command.materiaId());
    CursoId cursoId = CursoId.de(command.cursoId());
    ParaleloId paraleloId = ParaleloId.de(command.paraleloId());

    materiaRepositoryPort
        .buscarPorIdYTenant(materiaId, command.tenantId())
        .orElseThrow(MateriaNoEncontradaException::new);
    cursoRepositoryPort
        .buscarPorIdYTenant(cursoId, command.tenantId())
        .orElseThrow(CursoNoEncontradoException::new);
    Paralelo paralelo =
        paraleloRepositoryPort
            .buscarPorIdYTenant(paraleloId, command.tenantId())
            .orElseThrow(ParaleloNoEncontradoException::new);
    if (!paralelo.getCursoId().equals(cursoId)) {
      throw new ParaleloNoEncontradoException();
    }
    if (!asignacionMateriaCursoRepositoryPort.existePorMateriaCursoParaleloYTenant(
        materiaId, cursoId, paraleloId, command.tenantId())) {
      throw new MateriaSinCursoException();
    }
    if (!profesorConsultaPort.esProfesorActivoDelTenant(command.profesorId(), command.tenantId())) {
      throw new ProfesorNoEncontradoException();
    }

    AsignacionMateriaProfesor asignacion =
        AsignacionMateriaProfesor.crear(
            AsignacionMateriaProfesorId.nueva(),
            command.tenantId(),
            materiaId,
            command.profesorId(),
            cursoId,
            paraleloId);
    return asignacionMateriaProfesorRepositoryPort.guardar(asignacion);
  }
}
