package com.edusync.identidad.application.port.out;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.domain.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Usuario}. Implementado por
 * {@code UsuarioRepositoryAdapter} (JPA).
 *
 * <p><strong>Mitigacion obligatoria (DD-UC-002 &sect;2):</strong> la tabla {@code usuario}
 * usa la politica RLS {@code tenant_isolation ... OR tenant_id IS NULL}, que por si sola
 * NO oculta las filas {@code SYSADMIN} de un Admin de tenant autenticado. Cualquier metodo
 * nuevo que liste/busque usuarios para un actor de tenant (a partir de {@code DD-UC-004})
 * MUST filtrar explicitamente por {@code tenantId} en la consulta, sin depender solo de
 * RLS. {@link #buscarPorEmail(String)} es la unica excepcion intencional: es el propio
 * flujo de login, ocurre antes de que exista un tenant activo en el contexto.
 */
public interface UsuarioRepositoryPort {

  Optional<Usuario> buscarPorEmail(String email);

  Optional<Usuario> buscarPorId(UsuarioId id);

  boolean existePorEmail(String email);

  long contarTodos();

  /**
   * Lista los usuarios de un tenant (DD-UC-005). A diferencia de {@link #buscarPorEmail},
   * este metodo filtra explicitamente por {@code tenantId} en la capa de aplicacion (mismo
   * patron mitigador documentado arriba): nunca debe depender solo de la politica RLS.
   */
  List<Usuario> listarPorTenant(UUID tenantId);

  Usuario guardar(Usuario usuario);
}
