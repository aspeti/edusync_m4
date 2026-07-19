package com.edusync.shared.tenant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Decorador de {@link DataSource} que fija la variable de sesion PostgreSQL
 * {@code app.current_tenant} en cada conexion obtenida del pool, segun el
 * {@link TenantContext} activo en el hilo actual (ADR-0001).
 *
 * <p>Usa {@code set_config(...)} (funcion de PostgreSQL) en vez de concatenar el UUID en
 * un {@code SET app.current_tenant = '...'} literal: {@code set_config} acepta un bind
 * parameter via {@link PreparedStatement}, evitando cualquier riesgo de inyeccion SQL.
 * Un {@code tenant_id} ausente (SysAdmin o conexion sin peticion HTTP, p. ej. Flyway)
 * fija la variable a cadena vacia, lo cual la politica RLS interpreta como "sin tenant".
 */
public class TenantAwareDataSource extends DelegatingDataSource {

  private static final String SET_TENANT_SQL = "SELECT set_config('app.current_tenant', ?, false)";

  public TenantAwareDataSource(DataSource targetDataSource) {
    super(targetDataSource);
  }

  @Override
  public Connection getConnection() throws SQLException {
    Connection connection = super.getConnection();
    aplicarTenantActual(connection);
    return connection;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    Connection connection = super.getConnection(username, password);
    aplicarTenantActual(connection);
    return connection;
  }

  private void aplicarTenantActual(Connection connection) throws SQLException {
    Optional<UUID> tenantId = TenantContext.getCurrentTenantId();
    try (PreparedStatement statement = connection.prepareStatement(SET_TENANT_SQL)) {
      statement.setString(1, tenantId.map(UUID::toString).orElse(""));
      statement.execute();
    }
  }
}
