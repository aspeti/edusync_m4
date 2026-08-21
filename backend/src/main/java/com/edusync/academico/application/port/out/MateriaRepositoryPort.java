package com.edusync.academico.application.port.out;

import com.edusync.academico.application.port.in.MateriaFiltro;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Materia}. Filtra explicitamente por
 * {@code tenantId} en toda consulta (mitigacion RLS, {@code DD-UC-012} &sect;2).
 */
public interface MateriaRepositoryPort {

  Optional<Materia> buscarPorIdYTenant(MateriaId id, UUID tenantId);

  Materia guardar(Materia materia);

  PageResult<Materia> listarPorTenant(UUID tenantId, MateriaFiltro filtro, PageQuery pageQuery);
}
