package com.edusync.identidad.application.port.in;

import com.edusync.identidad.domain.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filtro opcional de {@link ListarUsuariosUseCase} (patron reutilizable de listados
 * {@code GetAll}, {@code DD-UC-007}). Todos los campos son nulos por defecto: campo nulo =
 * sin filtro. {@code tenantId} nunca es parte de este filtro (viene siempre del contexto del
 * actor autenticado, {@code TenantContextProvider}, nunca del cliente).
 *
 * <p>Se bindea directamente como {@code @ParameterObject} en {@code UsuarioController} (un query
 * param HTTP por componente del record). Las anotaciones {@code @Schema} son metadata pura de
 * documentacion (sin comportamiento en runtime, mismo criterio que Lombok en {@code ADR-0012}):
 * no acoplan este puerto de entrada a Spring MVC.
 *
 * @param q coincide si {@code nombreCompleto} o {@code email} lo contienen (case-insensitive)
 * @param activo filtra por estado activo/inactivo
 * @param rol filtra usuarios que tengan este rol entre los suyos
 */
public record UsuarioFiltro(
    @Schema(description = "Coincide con nombreCompleto o email (contains, case-insensitive)") String q,
    @Schema(description = "Filtra por estado activo/inactivo") Boolean activo,
    @Schema(description = "Filtra usuarios que tengan este rol") Rol rol) {

  public static final UsuarioFiltro VACIO = new UsuarioFiltro(null, null, null);
}
