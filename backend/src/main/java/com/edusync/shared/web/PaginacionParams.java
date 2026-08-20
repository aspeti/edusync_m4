package com.edusync.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Parametros de paginacion crudos tal como llegan de query params HTTP opcionales (patron
 * reutilizable, {@code DD-UC-007}). Vive en {@code shared.web} junto a {@link PageResponse}: es
 * el tipo "de borde" que cualquier controlador de listado {@code GetAll} puede bindear
 * directamente con {@code @ParameterObject} (springdoc-openapi) para mantener una firma de
 * metodo limpia, sin renunciar a la documentacion Swagger por campo.
 *
 * <p>Deliberadamente separado de {@code shared.PageQuery}: ambos campos aqui son nulos por
 * defecto (bindeable sin validar), mientras que {@code PageQuery} exige valores ya normalizados
 * (su constructor compacto rechaza rangos invalidos). La conversion se hace explicita en el
 * controlador via {@code PageQuery.of(paginacion.page(), paginacion.size())}.
 *
 * @param page indice de pagina, base 0 (si es nulo, {@code PageQuery.of} usa 0)
 * @param size tamano de pagina, 1-100 (si es nulo, {@code PageQuery.of} usa 20)
 */
public record PaginacionParams(
    @Schema(description = "Indice de pagina, base 0 (default 0)") Integer page,
    @Schema(description = "Tamano de pagina, 1-100 (default 20)") Integer size) {
}
