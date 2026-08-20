package com.edusync.shared.web;

import com.edusync.shared.PageResult;
import java.util.List;
import java.util.function.Function;

/**
 * Envelope REST comun para cualquier listado {@code GetAll} paginado (patron reutilizable,
 * {@code DD-UC-007}). Vive en {@code shared.web} junto a {@link ErrorResponse} (mismo
 * precedente: DTO transversal usado por controladores de cualquier modulo, no exclusivo de
 * uno). Nunca expone entidades JPA ni clases de dominio directamente (AGENTS.md &sect;5): el
 * contenido siempre es el {@code *Response} especifico de cada modulo.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

  public static <D, T> PageResponse<T> from(PageResult<D> resultado, Function<D, T> mapper) {
    return new PageResponse<>(
        resultado.content().stream().map(mapper).toList(),
        resultado.page(),
        resultado.size(),
        resultado.totalElements(),
        resultado.totalPages());
  }
}
