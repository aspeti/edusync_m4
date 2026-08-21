package com.edusync.academico.application.port.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarMateriasUseCase} (patron reutilizable {@code DD-UC-007}).
 * Campo nulo = sin filtro. {@code tenantId} nunca es parte de este filtro.
 *
 * @param q coincide si {@code nombre} lo contiene (case-insensitive)
 */
public record MateriaFiltro(@Schema(description = "Coincide con nombre (contains, case-insensitive)") String q) {

  public static final MateriaFiltro VACIO = new MateriaFiltro(null);
}
