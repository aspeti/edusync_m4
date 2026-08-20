package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.EstadoGestionEscolar;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarGestionesEscolaresUseCase} (patron reutilizable de
 * listados {@code GetAll}, {@code DD-UC-007}). Campo nulo = sin filtro. {@code tenantId}
 * nunca es parte de este filtro (viene siempre del contexto del actor autenticado,
 * {@code TenantContextProvider}, nunca del cliente).
 *
 * @param q coincide si {@code nombre} lo contiene (case-insensitive)
 * @param estado filtra por {@link EstadoGestionEscolar}
 */
public record GestionEscolarFiltro(
    @Schema(description = "Coincide con nombre (contains, case-insensitive)") String q,
    @Schema(description = "Filtra por estado") EstadoGestionEscolar estado) {

  public static final GestionEscolarFiltro VACIO = new GestionEscolarFiltro(null, null);
}
