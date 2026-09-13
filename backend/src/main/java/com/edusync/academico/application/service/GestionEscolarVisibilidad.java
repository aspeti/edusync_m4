package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;

/**
 * Regla de visibilidad de {@code DD-UC-019}: {@code ADMIN} ve y edita cualquier
 * {@link GestionEscolar} sin restriccion; {@code SECRETARIA}, {@code PROFESOR} y
 * {@code ASESOR} solo pueden ver la gestion {@code ACTIVA} ("gestion actual"). El resto de
 * gestiones se ocultan devolviendo {@code 404} (mismo patron 404-no-403 usado en el resto
 * del proyecto, p. ej. aislamiento cross-tenant), nunca {@code 403}.
 */
final class GestionEscolarVisibilidad {

  private GestionEscolarVisibilidad() {}

  /**
   * @throws GestionEscolarNoEncontradaException si {@code actorVeTodas} es {@code false} y
   *     {@code gestionEscolar} no esta {@code ACTIVA}
   */
  static void exigirVisible(GestionEscolar gestionEscolar, boolean actorVeTodas) {
    if (!actorVeTodas && gestionEscolar.getEstado() != EstadoGestionEscolar.ACTIVA) {
      throw new GestionEscolarNoEncontradaException();
    }
  }

  /** Si {@code actorVeTodas} es {@code false}, fuerza {@code estado=ACTIVA} ignorando el filtro recibido. */
  static GestionEscolarFiltro filtroEfectivo(GestionEscolarFiltro filtro, boolean actorVeTodas) {
    if (actorVeTodas) {
      return filtro;
    }
    return new GestionEscolarFiltro(filtro.q(), EstadoGestionEscolar.ACTIVA);
  }
}
