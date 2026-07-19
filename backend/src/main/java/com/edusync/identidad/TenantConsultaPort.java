package com.edusync.identidad;

import java.util.UUID;

/**
 * Puerto de salida (SPI) publico del modulo {@code identidad}: consulta minima que
 * {@code AutenticarUsuarioService} necesita de {@code plataforma} para aplicar
 * {@code BR-014} durante el login ({@code DD-UC-003} &sect;2).
 *
 * <p><strong>Nota de diseno (refinamiento respecto a {@code DD-UC-003} &sect;2):</strong>
 * el Design Doc describia este puerto como expuesto por {@code plataforma} (simetrico a
 * {@link UsuarioCreacionPort}); vive en cambio en la raiz de {@code identidad} porque
 * Spring Modulith rechaza ciclos de dependencia a nivel de modulo (verificacion
 * "no cycles"), y {@code plataforma} ya depende de {@code identidad} (via
 * {@link UsuarioCreacionPort}, consumido por {@code plataforma.CrearAdminTenantService}).
 * Con el puerto declarado aqui, {@code identidad} solo depende de su propia API (cero
 * referencias a {@code plataforma}); la implementacion real
 * ({@code plataforma.infrastructure.adapter.out.port.TenantConsultaPortImpl}) importa este
 * tipo desde la API publica de {@code identidad}, lo cual no anade una arista nueva al
 * grafo de modulos (la arista {@code plataforma -> identidad} ya existia). El resultado es
 * funcionalmente identico al diseno original (consulta sincronica, sin imports internos
 * entre modulos, "Open Host Service" — {@code ADR-0011}), solo cambia que modulo "hospeda"
 * la interfaz.
 */
public interface TenantConsultaPort {

  /** {@code true} si el Tenant existe y su estado es {@code ACTIVO}; {@code false} en cualquier otro caso. */
  boolean estaActivo(UUID tenantId);
}
