package com.edusync.identidad;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.identidad.infrastructure.adapter.in.rest.ActualizarRolesRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.CambiarEstadoRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.ConfirmarResetRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.CrearUsuarioRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.UsuarioResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
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
 * Cubre el stop condition de {@code PR-IMPL-005} ({@code DD-UC-005} &sect;6): CRUD de
 * Usuarios y Roles de punta a punta, aislamiento de tenant (404 cross-tenant, no 403) y
 * el flujo alternativo A4 (roles vacio -&gt; 422).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class UsuarioIntegrationTest {

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
  void crudDeUsuariosDePuntaAPuntaConAislamientoDeTenant() {
    HttpHeaders adminTenantA = crearTenantYAutenticarAdmin("Colegio CRUD A", "admin-crud-a@colegio.edu.bo");
    HttpHeaders adminTenantB = crearTenantYAutenticarAdmin("Colegio CRUD B", "admin-crud-b@colegio.edu.bo");

    // Alta multi-rol.
    ResponseEntity<UsuarioResponse> creado = restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearUsuarioRequest("Marco Rios", "marco.rios@colegio.edu.bo", "secreto123", Set.of("ADMIN", "SECRETARIA")),
            adminTenantA),
        UsuarioResponse.class);
    assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(creado.getBody()).isNotNull();
    assertThat(creado.getBody().roles()).containsExactlyInAnyOrder("ADMIN", "SECRETARIA");
    var usuarioId = creado.getBody().id();

    // Listado scoped al tenant.
    ResponseEntity<List<UsuarioResponse>> lista = restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantA),
        new ParameterizedTypeReference<List<UsuarioResponse>>() {});
    assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lista.getBody()).extracting(UsuarioResponse::id).contains(usuarioId);

    // PATCH roles.
    ResponseEntity<UsuarioResponse> rolesActualizados = restTemplate.exchange(
        "/api/v1/usuarios/" + usuarioId + "/roles",
        HttpMethod.PATCH,
        new HttpEntity<>(new ActualizarRolesRequest(Set.of("PROFESOR")), adminTenantA),
        UsuarioResponse.class);
    assertThat(rolesActualizados.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(rolesActualizados.getBody()).isNotNull();
    assertThat(rolesActualizados.getBody().roles()).containsExactly("PROFESOR");

    // PATCH estado.
    ResponseEntity<UsuarioResponse> desactivado = restTemplate.exchange(
        "/api/v1/usuarios/" + usuarioId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoRequest(false), adminTenantA),
        UsuarioResponse.class);
    assertThat(desactivado.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(desactivado.getBody()).isNotNull();
    assertThat(desactivado.getBody().activo()).isFalse();

    // Aislamiento de tenant: el Admin de otro tenant no puede ver ni mutar este usuario (404, no 403).
    ResponseEntity<ErrorResponse> patchCrossTenant = restTemplate.exchange(
        "/api/v1/usuarios/" + usuarioId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoRequest(true), adminTenantB),
        ErrorResponse.class);
    assertThat(patchCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(patchCrossTenant.getBody()).isNotNull();
    assertThat(patchCrossTenant.getBody().codigo()).isEqualTo("E_USUARIO_NO_ENCONTRADO");

    ResponseEntity<List<UsuarioResponse>> listaTenantB = restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantB),
        new ParameterizedTypeReference<List<UsuarioResponse>>() {});
    assertThat(listaTenantB.getBody()).extracting(UsuarioResponse::id).doesNotContain(usuarioId);
  }

  @Test
  void crearUsuarioConRolesVaciosDevuelve400() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Roles Vacios", "admin-roles-vacios@colegio.edu.bo");

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearUsuarioRequest("X", "x-roles-vacios@colegio.edu.bo", "secreto123", Set.of()), adminHeaders),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void iniciarRestablecimientoDevuelve202YConfirmarConTokenInvalidoDevuelve410() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Reset", "admin-reset@colegio.edu.bo");
    ResponseEntity<UsuarioResponse> creado = restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearUsuarioRequest("Reset User", "reset-user@colegio.edu.bo", "secreto123", Set.of("SECRETARIA")),
            adminHeaders),
        UsuarioResponse.class);
    var usuarioId = creado.getBody().id();

    ResponseEntity<Void> iniciado = restTemplate.exchange(
        "/api/v1/usuarios/" + usuarioId + "/restablecer-password",
        HttpMethod.POST,
        new HttpEntity<>(adminHeaders),
        Void.class);
    assertThat(iniciado.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    ResponseEntity<ErrorResponse> confirmacionInvalida = restTemplate.postForEntity(
        "/api/v1/auth/restablecer-password/confirmar",
        new ConfirmarResetRequest("token-que-no-existe", "otra-contrasena"),
        ErrorResponse.class);
    assertThat(confirmacionInvalida.getStatusCode()).isEqualTo(HttpStatus.GONE);
    assertThat(confirmacionInvalida.getBody()).isNotNull();
    assertThat(confirmacionInvalida.getBody().codigo()).isEqualTo("E_ENLACE_INVALIDO");
  }

  private HttpHeaders crearTenantYAutenticarAdmin(String nombreTenant, String adminEmail) {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);
    var tenantId = restTemplate.exchange(
            "/api/v1/plataforma/tenants",
            HttpMethod.POST,
            new HttpEntity<>(
                new RegistrarTenantRequest(nombreTenant, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
                sysAdminHeaders),
            TenantResponse.class)
        .getBody()
        .id();
    restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/admins",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAdminTenantRequest("Admin " + nombreTenant, adminEmail, "secreto123"), sysAdminHeaders),
        AdminCreadoResponse.class);
    return autenticarComo(adminEmail, "secreto123");
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
