package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.Paralelo;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Paralelo}. Implementado por
 * {@code ParaleloRepositoryAdapter} (JPA). Filtra explicitamente por {@code tenantId}, mismo
 * criterio de mitigacion que {@link CursoRepositoryPort}.
 */
public interface ParaleloRepositoryPort {

  Paralelo guardar(Paralelo paralelo);

  /** Sin paginar ({@code DD-UC-010} &sect;2): cardinalidad de paralelos por curso acotada. */
  List<Paralelo> listarPorCursoYTenant(CursoId cursoId, UUID tenantId);
}
