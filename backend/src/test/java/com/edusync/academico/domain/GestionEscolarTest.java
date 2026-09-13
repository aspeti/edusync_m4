package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Ciclo de vida de {@link GestionEscolar} ({@code FSD-UC-012}, {@code DD-UC-008}/{@code DD-UC-019}). */
class GestionEscolarTest {

  @Test
  void creaGestionEscolarEnPlanificacionConFechasValidas() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.PLANIFICACION);
    assertThat(gestionEscolar.getNombre()).isEqualTo("2027");
  }

  @Test
  void rechazaFechaFinNoPosteriorAFechaInicio() {
    assertThatThrownBy(() -> GestionEscolar.crear(
            GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 1)))
        .isInstanceOf(FechasInvalidasException.class)
        .satisfies(ex -> assertThat(((FechasInvalidasException) ex).getErrorCode())
            .isEqualTo("E_FECHAS_INVALIDAS"));
  }

  @Test
  void rechazaFechaFinAnteriorAFechaInicio() {
    assertThatThrownBy(() -> GestionEscolar.crear(
            GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 1, 1)))
        .isInstanceOf(FechasInvalidasException.class);
  }

  @Test
  void transicionaDePlanificacionAActiva() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.ACTIVA);

    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.ACTIVA);
  }

  @Test
  void transicionaDeActivaACerrada() {
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30),
        EstadoGestionEscolar.ACTIVA);

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.CERRADA);

    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.CERRADA);
  }

  @Test
  void transicionaDeActivaAPlanificacionParaReabrir() {
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30),
        EstadoGestionEscolar.ACTIVA);

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.PLANIFICACION);

    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.PLANIFICACION);
  }

  /**
   * {@code DD-UC-019}: el endpoint que invoca {@code cambiarEstado} es exclusivamente
   * {@code ADMIN}; el requisito de negocio es que pueda transicionar a cualquier estado
   * desde cualquier estado, incluida una gestion {@code CERRADA} (antes terminal).
   */
  @Test
  void permiteCualquierTransicionIncluidaDesdeCerrada() {
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30),
        EstadoGestionEscolar.CERRADA);

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.ACTIVA);
    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.ACTIVA);

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.PLANIFICACION);
    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.PLANIFICACION);

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.CERRADA);
    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.CERRADA);
  }

  @Test
  void permiteTransicionDirectaDePlanificacionACerrada() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    gestionEscolar.cambiarEstado(EstadoGestionEscolar.CERRADA);

    assertThat(gestionEscolar.getEstado()).isEqualTo(EstadoGestionEscolar.CERRADA);
  }

  @Test
  void actualizarDatosConCamposParcialesConservaElResto() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    gestionEscolar.actualizarDatos("2027 renombrada", null, null);

    assertThat(gestionEscolar.getNombre()).isEqualTo("2027 renombrada");
    assertThat(gestionEscolar.getFechaInicio()).isEqualTo(LocalDate.of(2027, 2, 1));
    assertThat(gestionEscolar.getFechaFin()).isEqualTo(LocalDate.of(2027, 11, 30));
  }

  @Test
  void actualizarDatosConTodosLosCampos() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    gestionEscolar.actualizarDatos("2028", LocalDate.of(2028, 1, 1), LocalDate.of(2028, 12, 1));

    assertThat(gestionEscolar.getNombre()).isEqualTo("2028");
    assertThat(gestionEscolar.getFechaInicio()).isEqualTo(LocalDate.of(2028, 1, 1));
    assertThat(gestionEscolar.getFechaFin()).isEqualTo(LocalDate.of(2028, 12, 1));
  }

  @Test
  void actualizarDatosRechazaFechaFinNoPosteriorAFechaInicio() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    assertThatThrownBy(() -> gestionEscolar.actualizarDatos(null, LocalDate.of(2027, 12, 1), null))
        .isInstanceOf(FechasInvalidasException.class);
  }
}
