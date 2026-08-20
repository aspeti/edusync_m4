package com.edusync.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResponseTest {

  @Test
  void fromMapeaContenidoYPreservaMetadataDePaginacion() {
    PageResult<Integer> resultado = PageResult.of(List.of(1, 2), new PageQuery(0, 2), 4);

    PageResponse<String> response = PageResponse.from(resultado, n -> "valor-" + n);

    assertThat(response.content()).containsExactly("valor-1", "valor-2");
    assertThat(response.page()).isZero();
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.totalElements()).isEqualTo(4);
    assertThat(response.totalPages()).isEqualTo(2);
  }
}
