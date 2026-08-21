package com.edusync.academico;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida (SPI) publico del modulo {@code academico}: consulta minima que
 * {@code CrearAsignacionProfesorService} y el catalogo de la UI necesitan de
 * {@code identidad} para tratar a un {@code Usuario} con rol {@code PROFESOR} como
 * profesor asignable ({@code DD-UC-012} &sect;2, {@code FSD-UC-018}).
 *
 * <p>Vive en la raiz de {@code academico} (consumidor), no en {@code identidad}, porque
 * Spring Modulith rechaza ciclos: {@code academico} no debe importar {@code identidad}.
 * La implementacion real
 * ({@code identidad.infrastructure.adapter.out.port.ProfesorConsultaPortImpl}) importa este
 * tipo desde la API publica de {@code academico}, anadiendo la arista
 * {@code identidad -> academico} (no existia; no genera ciclo). Espejo del refinamiento de
 * {@link com.edusync.identidad.TenantConsultaPort} ({@code DD-UC-003}).
 *
 * <p>El contrato no incluye PII extra: {@link ProfesorResumen} solo expone {@code id} y
 * {@code nombreCompleto} para el {@code <select>} de la UI. Los implementadores
 * <strong>MUST NOT</strong> loguear {@code nombreCompleto} ({@code AGENTS.md} &sect;7).
 */
public interface ProfesorConsultaPort {

  /**
   * {@code true} si el usuario existe, pertenece a {@code tenantId}, esta activo y tiene el
   * rol {@code PROFESOR}; {@code false} en cualquier otro caso.
   */
  boolean esProfesorActivoDelTenant(UUID usuarioId, UUID tenantId);

  /** Profesores activos del tenant, listos para el catalogo de asignacion. */
  List<ProfesorResumen> listarActivosDelTenant(UUID tenantId);
}
