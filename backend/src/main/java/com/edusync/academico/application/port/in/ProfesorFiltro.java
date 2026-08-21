package com.edusync.academico.application.port.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarProfesoresUseCase} (patron reutilizable {@code DD-UC-007}).
 * Campo nulo = sin filtro. {@code tenantId} nunca es parte de este filtro.
 *
 * <p>{@code q} se traduce al {@code q} de {@code UsuarioFiltro} (nombreCompleto <strong>o</strong>
 * email, case-insensitive) en {@code ProfesorConsultaPortImpl}; no se reimplementa Specification
 * en {@code academico}. No se loguea ({@code AGENTS.md} &sect;7).
 *
 * @param q coincide si {@code nombreCompleto} o {@code email} lo contienen (contains, case-insensitive)
 * @param activo filtra por estado activo/inactivo del {@code Usuario}
 */
public record ProfesorFiltro(
    @Schema(description = "Coincide con nombreCompleto o email (contains, case-insensitive)") String q,
    @Schema(description = "Filtra por estado activo/inactivo") Boolean activo) {

  public static final ProfesorFiltro VACIO = new ProfesorFiltro(null, null);
}
