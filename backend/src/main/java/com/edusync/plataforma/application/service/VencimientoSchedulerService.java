package com.edusync.plataforma.application.service;

import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logica invocada por {@code VencimientoSchedulerJob} ({@code @Scheduled}, infrastructure)
 * ({@code FSD-UC-011}, paso 5). Separada del job para ser testeable sin arrancar el
 * scheduler de Spring, con un {@link Clock} inyectado ({@code DD-UC-003} &sect;6).
 */
@Service
@RequiredArgsConstructor
public class VencimientoSchedulerService {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final Clock clock;

  /** @return cuantos Tenants se marcaron {@code VENCIDO} en esta ejecucion */
  @Transactional
  public int marcarVencidos() {
    LocalDate hoy = LocalDate.now(clock);
    List<Tenant> candidatos = tenantRepositoryPort.listarPendientesDeVencer(hoy);

    int marcados = 0;
    for (Tenant tenant : candidatos) {
      tenant.marcarVencidoSiCorresponde(hoy);
      if (tenant.getEstado() == EstadoTenant.VENCIDO) {
        tenantRepositoryPort.guardar(tenant);
        marcados++;
      }
    }
    return marcados;
  }
}
