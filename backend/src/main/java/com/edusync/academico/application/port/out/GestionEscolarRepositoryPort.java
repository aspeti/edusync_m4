package com.edusync.academico.application.port.out;

import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link GestionEscolar}. Implementado por
 * {@code GestionEscolarRepositoryAdapter} (JPA).
 *
 * <p><strong>Mitigacion obligatoria (mismo patron que {@code identidad}/{@code plataforma},
 * DD-UC-002 &sect;2 / DD-UC-005 &sect;2):</strong> aunque la tabla {@code gestion_escolar}
 * ya aplica RLS por {@code tenant_id} (sin el caso especial {@code SYSADMIN} de
 * {@code usuario}, toda Gestion Escolar pertenece a un tenant), ambos metodos filtran
 * explicitamente por {@code tenantId} en la capa de aplicacion, sin depender solo de RLS.
 */
public interface GestionEscolarRepositoryPort {

  /** Devuelve vacio si {@code id} no existe o pertenece a un tenant distinto de {@code tenantId}. */
  Optional<GestionEscolar> buscarPorIdYTenant(GestionEscolarId id, UUID tenantId);

  GestionEscolar guardar(GestionEscolar gestionEscolar);

  /** Version paginada y filtrable, scoped al tenant (DD-UC-007), usada por {@code GET /api/v1/gestiones-escolares}. */
  PageResult<GestionEscolar> listarPorTenant(UUID tenantId, GestionEscolarFiltro filtro, PageQuery pageQuery);
}
