package com.edusync.plataforma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reloj simulado ({@code Clock} inyectado), como exige {@code DD-UC-003} &sect;6. */
class VencimientoSchedulerServiceTest {

  private static final Clock RELOJ_FIJO =
      Clock.fixed(Instant.parse("2026-07-19T00:00:00Z"), ZoneOffset.UTC);

  private TenantRepositoryPort tenantRepositoryPort;
  private VencimientoSchedulerService service;

  @BeforeEach
  void setUp() {
    tenantRepositoryPort = mock(TenantRepositoryPort.class);
    service = new VencimientoSchedulerService(tenantRepositoryPort, RELOJ_FIJO);
  }

  @Test
  void marcaVencidosLosTenantsConSuscripcionExpirada() {
    Tenant vencido = Tenant.reconstruir(
        TenantId.nueva(), "Colegio A", LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 30), EstadoTenant.ACTIVO);
    when(tenantRepositoryPort.listarPendientesDeVencer(LocalDate.of(2026, 7, 19)))
        .thenReturn(List.of(vencido));
    when(tenantRepositoryPort.guardar(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

    int marcados = service.marcarVencidos();

    assertThat(marcados).isEqualTo(1);
    assertThat(vencido.getEstado()).isEqualTo(EstadoTenant.VENCIDO);
    verify(tenantRepositoryPort, times(1)).guardar(vencido);
  }

  @Test
  void noMarcaNadaCuandoNoHayCandidatos() {
    when(tenantRepositoryPort.listarPendientesDeVencer(LocalDate.of(2026, 7, 19)))
        .thenReturn(List.of());

    int marcados = service.marcarVencidos();

    assertThat(marcados).isZero();
  }
}
