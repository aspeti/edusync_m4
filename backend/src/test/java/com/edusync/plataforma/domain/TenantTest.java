package com.edusync.plataforma.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Ciclo de suscripcion de {@link Tenant} (BR-013/BR-014, {@code FSD-UC-011}). */
class TenantTest {

  @Test
  void creaTenantActivoConFechasValidas() {
    Tenant tenant = Tenant.crear(
        TenantId.nueva(), "Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    assertThat(tenant.getEstado()).isEqualTo(EstadoTenant.ACTIVO);
    assertThat(tenant.estaActivo()).isTrue();
  }

  @Test
  void rechazaRegistroSinFechaDeVencimiento() {
    assertThatThrownBy(() -> Tenant.crear(TenantId.nueva(), "Colegio Ejemplo", LocalDate.of(2026, 1, 1), null))
        .isInstanceOf(SuscripcionIncompletaException.class)
        .satisfies(ex -> assertThat(((SuscripcionIncompletaException) ex).getErrorCode())
            .isEqualTo("E_SUSCRIPCION_INCOMPLETA"));
  }

  @Test
  void cambiarEstadoActualizaElEstado() {
    Tenant tenant = Tenant.crear(
        TenantId.nueva(), "Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    tenant.cambiarEstado(EstadoTenant.SUSPENDIDO);

    assertThat(tenant.getEstado()).isEqualTo(EstadoTenant.SUSPENDIDO);
    assertThat(tenant.estaActivo()).isFalse();
  }

  @Test
  void marcarVencidoSiCorresponde_transicionaSoloSiLaFechaYaPaso() {
    Tenant tenant = Tenant.reconstruir(
        TenantId.nueva(), "Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), EstadoTenant.ACTIVO);

    tenant.marcarVencidoSiCorresponde(LocalDate.of(2026, 6, 29));
    assertThat(tenant.getEstado()).isEqualTo(EstadoTenant.ACTIVO);

    tenant.marcarVencidoSiCorresponde(LocalDate.of(2026, 7, 1));
    assertThat(tenant.getEstado()).isEqualTo(EstadoTenant.VENCIDO);
  }

  @Test
  void marcarVencidoSiCorresponde_esIdempotente() {
    Tenant tenant = Tenant.reconstruir(
        TenantId.nueva(), "Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), EstadoTenant.VENCIDO);

    tenant.marcarVencidoSiCorresponde(LocalDate.of(2026, 7, 1));

    assertThat(tenant.getEstado()).isEqualTo(EstadoTenant.VENCIDO);
  }
}
