package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Ciclo de vida de {@link GestionEscolar} ({@code FSD-UC-012}, {@code DD-UC-008}). */
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

  @Test
  void rechazaTransicionDesdeCerrada() {
    GestionEscolar gestionEscolar = GestionEscolar.reconstruir(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30),
        EstadoGestionEscolar.CERRADA);

    assertThatThrownBy(() -> gestionEscolar.cambiarEstado(EstadoGestionEscolar.ACTIVA))
        .isInstanceOf(EstadoGestionEscolarInvalidoException.class)
        .satisfies(ex -> assertThat(((EstadoGestionEscolarInvalidoException) ex).getErrorCode())
            .isEqualTo("E_ESTADO_INVALIDO"));

    assertThatThrownBy(() -> gestionEscolar.cambiarEstado(EstadoGestionEscolar.PLANIFICACION))
        .isInstanceOf(EstadoGestionEscolarInvalidoException.class);
  }

  @Test
  void rechazaTransicionDePlanificacionACerrada() {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), UUID.randomUUID(), "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    assertThatThrownBy(() -> gestionEscolar.cambiarEstado(EstadoGestionEscolar.CERRADA))
        .isInstanceOf(EstadoGestionEscolarInvalidoException.class);
  }
}
