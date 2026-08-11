package com.edusync.shared.ai.application.port.out;

import com.edusync.shared.ai.domain.UsuarioResumen;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: busca usuarios de un tenant cuyo nombre completo coincida (parcial, sin
 * distinguir mayusculas) con el texto buscado.
 *
 * <p>Declarado aqui (en {@code shared.ai}, modulo {@code OPEN}) e implementado por
 * {@code identidad} (dueno de los datos de {@code Usuario}) — misma direccion ya existente
 * {@code identidad -> shared} (p. ej. {@code TenantContextProvider}); {@code shared} nunca
 * importa de {@code identidad}, para no crear un ciclo que
 * {@code ApplicationModules.verify()} de Spring Modulith rechazaria (mismo criterio que
 * {@code identidad.TenantConsultaPort}, implementado por {@code plataforma}).
 */
public interface BuscarUsuarioPorNombrePort {

  List<UsuarioResumen> buscarPorNombre(UUID tenantId, String nombreBuscado);
}
