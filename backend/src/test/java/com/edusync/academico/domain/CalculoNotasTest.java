package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Caso canónico {@code ADR-0013} / {@code DD-UC-018}: Saber 35+40 → 37.50;
 * +Ser5/Hacer40/AE10 → periodo 93; N=3 → gestión 31. Sin {@code floor()}.
 */
class CalculoNotasTest {

  @Test
  void casoCanonicoSaber3750Periodo93Gestion31() {
    UUID ser = UUID.randomUUID();
    UUID saber = UUID.randomUUID();
    UUID hacer = UUID.randomUUID();
    UUID ae = UUID.randomUUID();

    List<CalculoNotas.SeccionCalculoInput> secciones =
        List.of(
            new CalculoNotas.SeccionCalculoInput(ser, "Ser", List.of(new BigDecimal("5"))),
            new CalculoNotas.SeccionCalculoInput(
                saber, "Saber", List.of(new BigDecimal("35"), new BigDecimal("40"))),
            new CalculoNotas.SeccionCalculoInput(hacer, "Hacer", List.of(new BigDecimal("40"))),
            new CalculoNotas.SeccionCalculoInput(ae, "Autoevaluación", List.of(new BigDecimal("10"))));

    // T1=93, T2=0, T3=0 → promedio gestión 31
    CalculoNotas.NotaProvisional nota =
        CalculoNotas.calcular(secciones, Arrays.asList(93, null, null));

    assertThat(nota.secciones()).hasSize(4);
    assertThat(nota.secciones().get(1).notaSeccion()).isEqualByComparingTo("37.50");
    assertThat(nota.secciones().get(1).estado()).isEqualTo(EstadoSeccionNota.COMPLETO);
    assertThat(nota.notaPeriodo()).isEqualTo(93);
    assertThat(nota.promedioGestion()).isEqualTo(31);
    assertThat(nota.estado()).isEqualTo("PROVISIONAL");
  }

  @Test
  void seccionSinNotasEsIncompletaYNoSumaAlPeriodo() {
    UUID saber = UUID.randomUUID();
    UUID ser = UUID.randomUUID();
    List<CalculoNotas.SeccionCalculoInput> secciones =
        List.of(
            new CalculoNotas.SeccionCalculoInput(ser, "Ser", List.of()),
            new CalculoNotas.SeccionCalculoInput(saber, "Saber", List.of(new BigDecimal("40"))));

    Integer notaPeriodo = CalculoNotas.calcularNotaPeriodo(secciones);
    assertThat(notaPeriodo).isEqualTo(40);

    CalculoNotas.NotaProvisional vista = CalculoNotas.calcular(secciones, List.of(40, 0, 0));
    assertThat(vista.secciones().get(0).estado()).isEqualTo(EstadoSeccionNota.INCOMPLETO);
    assertThat(vista.secciones().get(0).notaSeccion()).isNull();
  }

  @Test
  void redondeoHalfUpNoFloor() {
    // 64.5 → 65 con HALF_UP; floor sería 64
    assertThat(
            CalculoNotas.calcularNotaPeriodo(
                List.of(
                    new CalculoNotas.SeccionCalculoInput(
                        UUID.randomUUID(), "X", List.of(new BigDecimal("64.5"))))))
        .isEqualTo(65);
  }
}
