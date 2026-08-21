package com.edusync.academico.application.port.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarCursosUseCase} (patron reutilizable de listados
 * {@code GetAll}, {@code DD-UC-007}). Campo nulo = sin filtro. {@code tenantId} nunca es
 * parte de este filtro (viene siempre del contexto del actor autenticado,
 * {@code TenantContextProvider}, nunca del cliente).
 *
 * <p>A diferencia de {@code GestionEscolarFiltro}, solo tiene {@code q}: {@link
 * com.edusync.academico.domain.Curso} no tiene estado ({@code DD-UC-010} &sect;2).
 *
 * @param q coincide si {@code nombre} lo contiene (case-insensitive)
 */
public record CursoFiltro(@Schema(description = "Coincide con nombre (contains, case-insensitive)") String q) {

  public static final CursoFiltro VACIO = new CursoFiltro(null);
}
