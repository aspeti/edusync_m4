package com.edusync.academico.application.service;

import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SumaSeccionesInvalidaException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Invariantes de conjunto de secciones de una misma gestion ({@code DD-UC-016} &sect;2).
 *
 * <p><b>{@code DD-UC-019}</b>: el freeze sticky ({@code E_SECCIONES_INMUTABLES}, bloqueaba
 * POST/PUT/PATCH una vez que algun periodo dejaba de estar {@code PENDIENTE}, sin
 * levantarse nunca) se elimino: los tres escritores
 * ({@code Crear}/{@code Actualizar}/{@code Reemplazar} {@code SeccionEvaluacionService})
 * son exclusivamente {@code ADMIN}, y el requisito de negocio es que pueda editar la
 * plantilla en cualquier momento. Se conserva la exigencia de suma 100
 * ({@code exigirSumaCien}): es un requisito de integridad del motor de calculo
 * ({@code ADR-0013}), no una restriccion de flujo/edicion.
 */
final class SeccionEvaluacionPolitica {

  static final BigDecimal SUMA_REQUERIDA = new BigDecimal("100.00");

  private SeccionEvaluacionPolitica() {}

  static void exigirSumaCien(List<SeccionEvaluacion> secciones) {
    if (!sumaEsCien(secciones)) {
      throw new SumaSeccionesInvalidaException();
    }
  }

  static boolean sumaEsCien(List<SeccionEvaluacion> secciones) {
    if (secciones.isEmpty()) {
      return false;
    }
    BigDecimal suma = secciones.stream()
        .map(SeccionEvaluacion::getNota)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return suma.compareTo(SUMA_REQUERIDA) == 0;
  }
}
