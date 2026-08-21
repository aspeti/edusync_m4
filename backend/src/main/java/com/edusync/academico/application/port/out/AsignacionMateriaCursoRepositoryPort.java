package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link AsignacionMateriaCurso}. Filtra explicitamente
 * por {@code tenantId}.
 */
public interface AsignacionMateriaCursoRepositoryPort {

  AsignacionMateriaCurso guardar(AsignacionMateriaCurso asignacion);

  List<AsignacionMateriaCurso> listarPorMateriaYTenant(MateriaId materiaId, UUID tenantId);

  boolean existePorMateriaCursoParaleloYTenant(
      MateriaId materiaId, CursoId cursoId, ParaleloId paraleloId, UUID tenantId);
}
