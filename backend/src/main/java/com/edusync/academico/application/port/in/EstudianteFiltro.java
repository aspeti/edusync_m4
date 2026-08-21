package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.EstadoEstudiante;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarEstudiantesUseCase} (patron reutilizable {@code DD-UC-007}).
 * Campo nulo = sin filtro. {@code tenantId} nunca es parte de este filtro.
 *
 * @param q coincide si {@code nombreCompleto} lo contiene (contains, case-insensitive) o si
 *     {@code rude} es exacto (case-insensitive). No se loguea.
 * @param estado filtra por {@link EstadoEstudiante}
 */
public record EstudianteFiltro(
    @Schema(description = "Coincide con nombreCompleto (contains) o rude (exacto), case-insensitive")
        String q,
    @Schema(description = "Filtra por estado") EstadoEstudiante estado) {

  public static final EstudianteFiltro VACIO = new EstudianteFiltro(null, null);
}
