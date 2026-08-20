package com.edusync.shared;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado devuelto por puertos/casos de uso (patron reutilizable de listados
 * {@code GetAll}, {@code DD-UC-007}). Framework-free, distinto del DTO REST
 * {@link com.edusync.shared.web.PageResponse} que expone la misma forma por API (mismo
 * precedente de {@code RespuestaLlm} vs. {@code ChatResponse} en {@code shared.ai}): la capa
 * de aplicacion nunca depende de un tipo HTTP-specific.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

  public static <T> PageResult<T> of(List<T> content, PageQuery query, long totalElements) {
    int totalPaginas = query.size() == 0 ? 0 : (int) Math.ceil((double) totalElements / query.size());
    return new PageResult<>(content, query.page(), query.size(), totalElements, totalPaginas);
  }

  /** Traduce el contenido preservando la metadata de paginacion (usado por los controllers). */
  public <R> PageResult<R> map(Function<T, R> mapper) {
    return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
  }
}
