package com.edusync.academico.application.port.out;

import com.edusync.academico.application.port.in.EstudianteFiltro;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Estudiante}. Filtra explicitamente por
 * {@code tenantId} en toda consulta (mitigacion RLS, {@code DD-UC-013} &sect;2).
 */
public interface EstudianteRepositoryPort {

  Optional<Estudiante> buscarPorIdYTenant(EstudianteId id, UUID tenantId);

  List<Estudiante> listarPorIdsYTenant(Collection<EstudianteId> ids, UUID tenantId);

  boolean existePorRudeYTenant(String rude, UUID tenantId);

  Estudiante guardar(Estudiante estudiante);

  PageResult<Estudiante> listarPorTenant(UUID tenantId, EstudianteFiltro filtro, PageQuery pageQuery);
}
