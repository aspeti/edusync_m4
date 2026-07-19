package com.edusync.plataforma;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.identidad.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CambiarEstadoTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Cubre el stop condition de {@code PR-IMPL-003} de punta a punta ({@code DD-UC-003}
 * &sect;6): alta de Tenant + admin en dos llamadas REST separadas, y bloqueo de login
 * ({@code BR-014}, {@code E_TENANT_NO_ACTIVO}) cuando el tenant pasa a {@code SUSPENDIDO}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class TenantIntegrationTest {

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

  @Value("${edusync.seed.sysadmin.email}")
  private String sysAdminEmail;

  @Value("${edusync.seed.sysadmin.password}")
  private String sysAdminPassword;

  @Test
  void altaDeTenantYAdminEnDosLlamadasSeparadasYBloqueoDeLoginPorSuspension() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<TenantResponse> tenantResponse = restTemplate.exchange(
        "/api/v1/plataforma/tenants",
        HttpMethod.POST,
        new HttpEntity<>(
            new RegistrarTenantRequest("Colegio Integration Test", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
            sysAdminHeaders),
        TenantResponse.class);
    assertThat(tenantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(tenantResponse.getBody()).isNotNull();
    assertThat(tenantResponse.getBody().estado()).isEqualTo("ACTIVO");
    var tenantId = tenantResponse.getBody().id();

    String adminEmail = "admin-it@colegio.edu.bo";
    String adminPassword = "secreto123";
    ResponseEntity<AdminCreadoResponse> adminResponse = restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/admins",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearAdminTenantRequest("Admin Integration Test", adminEmail, adminPassword), sysAdminHeaders),
        AdminCreadoResponse.class);
    assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(adminResponse.getBody()).isNotNull();
    assertThat(adminResponse.getBody().email()).isEqualTo(adminEmail);

    ResponseEntity<LoginResponse> loginActivo = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest(adminEmail, adminPassword), LoginResponse.class);
    assertThat(loginActivo.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<TenantResponse> suspenderResponse = restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoTenantRequest("SUSPENDIDO"), sysAdminHeaders),
        TenantResponse.class);
    assertThat(suspenderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(suspenderResponse.getBody()).isNotNull();
    assertThat(suspenderResponse.getBody().estado()).isEqualTo("SUSPENDIDO");

    ResponseEntity<ErrorResponse> loginBloqueado = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest(adminEmail, adminPassword), ErrorResponse.class);
    assertThat(loginBloqueado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(loginBloqueado.getBody()).isNotNull();
    assertThat(loginBloqueado.getBody().codigo()).isEqualTo("E_TENANT_NO_ACTIVO");
  }

  @Test
  void registrarTenantSinFechaDeVencimientoDevuelve422() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/plataforma/tenants",
        HttpMethod.POST,
        new HttpEntity<>(
            new RegistrarTenantRequest("Colegio Sin Vencimiento", LocalDate.of(2026, 1, 1), null), sysAdminHeaders),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_SUSCRIPCION_INCOMPLETA");
  }

  @Test
  void endpointsDePlataformaRechazanUsuarioSinRolSysAdmin() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);
    var tenantId = restTemplate.exchange(
            "/api/v1/plataforma/tenants",
            HttpMethod.POST,
            new HttpEntity<>(
                new RegistrarTenantRequest("Colegio Rbac", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
                sysAdminHeaders),
            TenantResponse.class)
        .getBody()
        .id();
    restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/admins",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAdminTenantRequest("Admin Rbac", "admin-rbac@colegio.edu.bo", "secreto123"), sysAdminHeaders),
        AdminCreadoResponse.class);
    HttpHeaders adminHeaders = autenticarComo("admin-rbac@colegio.edu.bo", "secreto123");

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/plataforma/tenants",
        HttpMethod.POST,
        new HttpEntity<>(
            new RegistrarTenantRequest("Colegio No Autorizado", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
            adminHeaders),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private HttpHeaders autenticarComo(String email, String password) {
    ResponseEntity<LoginResponse> login = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest(email, password), LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(login.getBody().accessToken());
    return headers;
  }
}
