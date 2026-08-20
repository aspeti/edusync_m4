package com.edusync.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

  @Test
  void ofCalculaTotalPagesRedondeandoHaciaArriba() {
    PageResult<String> resultado = PageResult.of(List.of("a", "b"), new PageQuery(0, 2), 5);

    assertThat(resultado.totalPages()).isEqualTo(3);
    assertThat(resultado.totalElements()).isEqualTo(5);
    assertThat(resultado.page()).isZero();
    assertThat(resultado.size()).isEqualTo(2);
  }

  @Test
  void mapTraduceElContenidoPreservandoLaMetadata() {
    PageResult<Integer> original = PageResult.of(List.of(1, 2, 3), new PageQuery(1, 3), 10);

    PageResult<String> traducido = original.map(n -> "n" + n);

    assertThat(traducido.content()).containsExactly("n1", "n2", "n3");
    assertThat(traducido.page()).isEqualTo(1);
    assertThat(traducido.size()).isEqualTo(3);
    assertThat(traducido.totalElements()).isEqualTo(10);
    assertThat(traducido.totalPages()).isEqualTo(original.totalPages());
  }
}
