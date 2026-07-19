package com.edusync.plataforma.infrastructure.adapter.out.port;

import com.edusync.identidad.TenantConsultaPort;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementacion real de {@code identidad.TenantConsultaPort} (Open Host Service para
 * {@code identidad}, {@code ADR-0011}). Vive en {@code plataforma.infrastructure} — no en
 * {@code identidad} — porque quien conoce el estado real de un Tenant es este modulo; ver
 * el Javadoc de {@code identidad.TenantConsultaPort} para el porque el puerto se declara
 * alli y no aqui (evitar un ciclo de modulos en Spring Modulith, {@code DD-UC-003}).
 */
@Component
@RequiredArgsConstructor
class TenantConsultaPortImpl implements TenantConsultaPort {

  private final TenantRepositoryPort tenantRepositoryPort;

  @Override
  public boolean estaActivo(UUID tenantId) {
    return tenantRepositoryPort.buscarPorId(TenantId.de(tenantId))
        .map(Tenant::estaActivo)
        .orElse(false);
  }
}
