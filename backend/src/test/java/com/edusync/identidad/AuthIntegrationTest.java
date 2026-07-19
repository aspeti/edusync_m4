package com.edusync.identidad;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.identidad.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.shared.tenant.TenantContextProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Cubre el stop condition de {@code PR-IMPL-002} (a) y (c): login end-to-end contra un
 * PostgreSQL 15 real (Testcontainers) con las migraciones Flyway V1/V2 aplicadas y el
 * seed de {@code SYSADMIN} ya ejecutado, y verificacion de que
 * {@link TenantContextProvider} fija {@code app.current_tenant} en la conexion JDBC
 * activa (a traves de {@code TenantAwareDataSource}).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class AuthIntegrationTest {

  @Container
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15")
      .withDatabaseName("edusync_it")
      .withUsername("edusync")
      .withPassword("edusync_it_local");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private TenantContextProvider tenantContextProvider;

  @Autowired
  private DataSource dataSource;

  @Value("${edusync.seed.sysadmin.email}")
  private String sysAdminEmail;

  @Value("${edusync.seed.sysadmin.password}")
  private String sysAdminPassword;

  @Test
  void loginConCredencialesValidasDevuelveJwt() {
    ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest(sysAdminEmail, sysAdminPassword), LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().accessToken()).isNotBlank();
    assertThat(response.getBody().expiresIn()).isEqualTo(28800L);
  }

  @Test
  void loginConContrasenaIncorrectaDevuelve401() {
    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest(sysAdminEmail, "password-incorrecta"), ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_CREDENCIALES_INVALIDAS");
  }

  @Test
  void loginConEmailInexistenteDevuelve401() {
    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest("nadie@edusync.local", "x"), ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void tenantContextProviderFijaAppCurrentTenantEnLaConexionJdbc() throws Exception {
    UUID tenantId = UUID.randomUUID();
    tenantContextProvider.activar(tenantId);
    try {
      assertThat(valorSesionActual()).isEqualTo(tenantId.toString());
    } finally {
      tenantContextProvider.limpiar();
    }

    assertThat(valorSesionActual()).isEmpty();
  }

  private String valorSesionActual() throws Exception {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement("SELECT current_setting('app.current_tenant', true)");
        ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getString(1);
    }
  }
}
