package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Paralelo}. Implementado por
 * {@code ParaleloRepositoryAdapter} (JPA). Filtra explicitamente por {@code tenantId}, mismo
 * criterio de mitigacion que {@link CursoRepositoryPort}.
 */
public interface ParaleloRepositoryPort {

  Paralelo guardar(Paralelo paralelo);

  /** Devuelve vacio si {@code id} no existe o pertenece a un tenant distinto de {@code tenantId}. */
  Optional<Paralelo> buscarPorIdYTenant(ParaleloId id, UUID tenantId);

  /** Sin paginar ({@code DD-UC-010} &sect;2): cardinalidad de paralelos por curso acotada. */
  List<Paralelo> listarPorCursoYTenant(CursoId cursoId, UUID tenantId);
}
