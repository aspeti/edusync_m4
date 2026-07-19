package com.edusync.plataforma.application.port.out;

import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: persistencia de {@link Tenant}. Implementado por {@code TenantRepositoryAdapter} (JPA). */
public interface TenantRepositoryPort {

  Optional<Tenant> buscarPorId(TenantId id);

  Tenant guardar(Tenant tenant);

  /**
   * Candidatos a marcar {@code VENCIDO} por el scheduler: Tenants que no estan ya
   * {@code VENCIDO} y cuya {@code fechaVencimientoSuscripcion} es anterior a
   * {@code fechaReferencia}. Optimizacion de consulta; la regla de negocio real la valida
   * {@link Tenant#marcarVencidoSiCorresponde}.
   */
  List<Tenant> listarPendientesDeVencer(LocalDate fechaReferencia);
}
