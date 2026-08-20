package com.edusync.plataforma.application.port.in;

import com.edusync.plataforma.domain.EstadoTenant;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarTenantsUseCase} (patron reutilizable de listados
 * {@code GetAll}, {@code DD-UC-007}). Campo nulo = sin filtro.
 *
 * <p>Se bindea directamente como {@code @ParameterObject} en {@code TenantController} (un query
 * param HTTP por componente del record); ver {@code UsuarioFiltro} para el razonamiento
 * completo sobre las anotaciones {@code @Schema} en un puerto de entrada.
 *
 * @param q coincide si {@code nombre} lo contiene (case-insensitive)
 * @param estado filtra por {@link EstadoTenant}
 */
public record TenantFiltro(
    @Schema(description = "Coincide con nombre (contains, case-insensitive)") String q,
    @Schema(description = "Filtra por estado") EstadoTenant estado) {

  public static final TenantFiltro VACIO = new TenantFiltro(null, null);
}
