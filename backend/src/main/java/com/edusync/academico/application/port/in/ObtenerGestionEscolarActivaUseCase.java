package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.GestionEscolar;
import java.util.UUID;

/**
 * Puerto de entrada: resuelve la unica {@link GestionEscolar} en estado {@code ACTIVA} del
 * tenant ({@code DD-UC-021}, {@code GET /gestiones-escolares/activa}). A diferencia de
 * {@link ObtenerGestionEscolarUseCase} (exclusivo {@code ADMIN}, cualquier id/estado),
 * este puerto es el punto de entrada para {@code SECRETARIA}/{@code PROFESOR}/{@code ASESOR}:
 * nunca listan ni eligen una Gestion Escolar, solo consumen "la actual" de forma implicita.
 */
public interface ObtenerGestionEscolarActivaUseCase {

  /**
   * @throws com.edusync.academico.domain.GestionEscolarNoEncontradaException si el tenant no
   *     tiene ninguna gestion en estado {@code ACTIVA}
   */
  GestionEscolar obtener(UUID tenantId);
}
