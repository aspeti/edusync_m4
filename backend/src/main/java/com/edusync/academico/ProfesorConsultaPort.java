package com.edusync.academico;

import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (SPI) publico del modulo {@code academico}: consulta de usuarios con rol
 * {@code PROFESOR} ({@code DD-UC-012} &sect;2, {@code DD-UC-014} &sect;2).
 *
 * <p>Vive en la raiz de {@code academico} (consumidor), no en {@code identidad}, porque
 * Spring Modulith rechaza ciclos: {@code academico} no debe importar {@code identidad}.
 * La implementacion real
 * ({@code identidad.infrastructure.adapter.out.port.ProfesorConsultaPortImpl}) importa este
 * tipo desde la API publica de {@code academico}, anadiendo la arista
 * {@code identidad -> academico} (ya existia desde {@code DD-UC-012}; no genera ciclo).
 *
 * <p>{@code listarDelTenant} toma {@code q}/{@code activo} primitivos — no
 * {@code academico.application.port.in.ProfesorFiltro} — para que {@code identidad} no
 * importe un paquete interno de {@code academico} (Modulith). El filtro REST vive en
 * {@code application.port.in} y el servicio de listado traduce los campos.
 *
 * <p>Los implementadores <strong>MUST NOT</strong> loguear {@code nombreCompleto} ni
 * {@code q} ({@code AGENTS.md} &sect;7).
 */
public interface ProfesorConsultaPort {

  /**
   * {@code true} si el usuario existe, pertenece a {@code tenantId}, esta activo y tiene el
   * rol {@code PROFESOR}; {@code false} en cualquier otro caso. Usado por la <em>escritura</em>
   * de asignaciones ({@code DD-UC-012}).
   */
  boolean esProfesorActivoDelTenant(UUID usuarioId, UUID tenantId);

  /** Profesores activos del tenant, listos para el catalogo de asignacion. */
  List<ProfesorResumen> listarActivosDelTenant(UUID tenantId);

  /**
   * Profesor del tenant con rol {@code PROFESOR}, este activo o no. Vacio si no existe, es de
   * otro tenant, o el usuario no tiene el rol ({@code DD-UC-014} &sect;2: 404, no 403).
   */
  Optional<ProfesorResumen> buscarPorIdYTenant(UUID usuarioId, UUID tenantId);

  /**
   * Listado paginado de usuarios con rol {@code PROFESOR} del tenant. {@code q} nulo = sin
   * filtro de texto; {@code activo} nulo = activos e inactivos.
   */
  PageResult<ProfesorResumen> listarDelTenant(UUID tenantId, String q, Boolean activo, PageQuery pageQuery);
}
