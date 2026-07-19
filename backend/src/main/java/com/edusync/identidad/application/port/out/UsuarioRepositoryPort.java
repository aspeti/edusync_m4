package com.edusync.identidad.application.port.out;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.domain.Usuario;
import java.util.Optional;

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

  Usuario guardar(Usuario usuario);
}
