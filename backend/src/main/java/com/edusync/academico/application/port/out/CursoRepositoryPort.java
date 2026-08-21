package com.edusync.academico.application.port.out;

import com.edusync.academico.application.port.in.CursoFiltro;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Curso}. Implementado por
 * {@code CursoRepositoryAdapter} (JPA).
 *
 * <p><strong>Mitigacion obligatoria (mismo patron que {@code GestionEscolarRepositoryPort},
 * {@code DD-UC-008} &sect;2):</strong> aunque {@code curso} ya aplica RLS por
 * {@code tenant_id} (sin el caso especial {@code SYSADMIN} de {@code usuario}), ambos
 * metodos filtran explicitamente por {@code tenantId} en la capa de aplicacion, sin
 * depender solo de RLS.
 */
public interface CursoRepositoryPort {

  /** Devuelve vacio si {@code id} no existe o pertenece a un tenant distinto de {@code tenantId}. */
  Optional<Curso> buscarPorIdYTenant(CursoId id, UUID tenantId);

  Curso guardar(Curso curso);

  /** Version paginada y filtrable, scoped al tenant (DD-UC-007), usada por {@code GET /api/v1/cursos}. */
  PageResult<Curso> listarPorTenant(UUID tenantId, CursoFiltro filtro, PageQuery pageQuery);
}
