package com.edusync.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PageQueryTest {

  @Test
  void ofConNulosDevuelveValoresPorDefecto() {
    PageQuery query = PageQuery.of(null, null);

    assertThat(query.page()).isZero();
    assertThat(query.size()).isEqualTo(20);
  }

  @Test
  void ofClampeaSizeAlMaximo() {
    PageQuery query = PageQuery.of(0, 1000);

    assertThat(query.size()).isEqualTo(100);
  }

  @Test
  void ofClampeaSizeAlMinimo() {
    PageQuery query = PageQuery.of(0, -5);

    assertThat(query.size()).isEqualTo(1);
  }

  @Test
  void ofNormalizaPageNegativoACero() {
    PageQuery query = PageQuery.of(-3, 10);

    assertThat(query.page()).isZero();
  }

  @Test
  void constructorRechazaPageNegativo() {
    assertThatThrownBy(() -> new PageQuery(-1, 10)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructorRechazaSizeFueraDeRango() {
    assertThatThrownBy(() -> new PageQuery(0, 0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PageQuery(0, 101)).isInstanceOf(IllegalArgumentException.class);
  }
}
