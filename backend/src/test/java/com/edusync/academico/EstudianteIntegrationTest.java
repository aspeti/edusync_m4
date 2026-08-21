package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearEstudianteRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearInscripcionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.EstudianteResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.InscripcionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import com.edusync.shared.web.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
 * Cubre el stop condition de {@code PR-IMPL-013} ({@code DD-UC-013} &sect;1.5): alta y
 * listado de Estudiantes, GET por id, historial, POST inscripciones, A1 409, rude
 * duplicado 409, y aislamiento cross-tenant 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class EstudianteIntegrationTest {

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
  void altaInscripcionesYAislamientoDeTenant() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Estudiantes A", "admin-est-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Estudiantes B", "admin-est-b@colegio.edu.bo");

    UUID gestion2026 = crearGestion(adminA, "2026", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 11, 30));
    UUID gestion2027 = crearGestion(adminA, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));
    UUID cursoId = crearCurso(adminA, "Primero de Primaria");
    UUID paraleloId = crearParalelo(adminA, cursoId, "A");

    ResponseEntity<EstudianteResponse> creado = restTemplate.exchange(
        "/api/v1/estudiantes",
        HttpMethod.POST,
        new HttpEntity<>(new CrearEstudianteRequest("12345678", "Ana Pérez", null, null), adminA),
        EstudianteResponse.class);
    assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(creado.getBody()).isNotNull();
    assertThat(creado.getBody().estado()).isEqualTo("ACTIVO");
    UUID estudianteId = creado.getBody().id();

    ResponseEntity<EstudianteResponse> detalle = restTemplate.exchange(
        "/api/v1/estudiantes/" + estudianteId,
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        EstudianteResponse.class);
    assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(detalle.getBody().nombreCompleto()).isEqualTo("Ana Pérez");

    ResponseEntity<PageResponse<EstudianteResponse>> lista = restTemplate.exchange(
        "/api/v1/estudiantes?q=12345678",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<PageResponse<EstudianteResponse>>() {});
    assertThat(lista.getBody().content()).extracting(EstudianteResponse::id).contains(estudianteId);

    ResponseEntity<ErrorResponse> rudeDuplicado = restTemplate.exchange(
        "/api/v1/estudiantes",
        HttpMethod.POST,
        new HttpEntity<>(new CrearEstudianteRequest("12345678", "Otra Persona", null, null), adminA),
        ErrorResponse.class);
    assertThat(rudeDuplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(rudeDuplicado.getBody().codigo()).isEqualTo("E_RUDE_DUPLICADO");
    assertThat(rudeDuplicado.getBody().mensaje()).doesNotContain("12345678");

    ResponseEntity<InscripcionResponse> inscripcion2026 = restTemplate.exchange(
        "/api/v1/inscripciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearInscripcionRequest(estudianteId, gestion2026, cursoId, paraleloId, LocalDate.of(2026, 2, 15)),
            adminA),
        InscripcionResponse.class);
    assertThat(inscripcion2026.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(inscripcion2026.getBody().estado()).isEqualTo("ACTIVA");

    ResponseEntity<ErrorResponse> duplicada = restTemplate.exchange(
        "/api/v1/inscripciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearInscripcionRequest(estudianteId, gestion2026, cursoId, paraleloId, LocalDate.of(2026, 3, 1)),
            adminA),
        ErrorResponse.class);
    assertThat(duplicada.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(duplicada.getBody().codigo()).isEqualTo("E_INSCRIPCION_DUPLICADA");

    ResponseEntity<InscripcionResponse> inscripcion2027 = restTemplate.exchange(
        "/api/v1/inscripciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearInscripcionRequest(estudianteId, gestion2027, cursoId, paraleloId, LocalDate.of(2027, 2, 15)),
            adminA),
        InscripcionResponse.class);
    assertThat(inscripcion2027.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    ResponseEntity<List<InscripcionResponse>> historial = restTemplate.exchange(
        "/api/v1/estudiantes/" + estudianteId + "/inscripciones",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<List<InscripcionResponse>>() {});
    assertThat(historial.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(historial.getBody()).hasSize(2);

    ResponseEntity<ErrorResponse> crossTenant = restTemplate.exchange(
        "/api/v1/estudiantes/" + estudianteId,
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        ErrorResponse.class);
    assertThat(crossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(crossTenant.getBody().codigo()).isEqualTo("E_ESTUDIANTE_NO_ENCONTRADO");

    ResponseEntity<ErrorResponse> inscripcionCruzada = restTemplate.exchange(
        "/api/v1/inscripciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearInscripcionRequest(estudianteId, gestion2026, cursoId, paraleloId, LocalDate.of(2026, 2, 15)),
            adminB),
        ErrorResponse.class);
    assertThat(inscripcionCruzada.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void estudiantesSinTokenDevuelve401() {
    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/estudiantes", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void estudiantesConRolSysAdminDevuelve403() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/estudiantes", HttpMethod.GET, new HttpEntity<>(sysAdminHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private UUID crearGestion(HttpHeaders admin, String nombre, LocalDate inicio, LocalDate fin) {
    return restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(new CrearGestionEscolarRequest(nombre, inicio, fin), admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();
  }

  private UUID crearCurso(HttpHeaders admin, String nombre) {
    return restTemplate.exchange(
            "/api/v1/cursos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearCursoRequest(nombre), admin),
            CursoResponse.class)
        .getBody()
        .id();
  }

  private UUID crearParalelo(HttpHeaders admin, UUID cursoId, String nombre) {
    return restTemplate.exchange(
            "/api/v1/cursos/" + cursoId + "/paralelos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearParaleloRequest(nombre), admin),
            ParaleloResponse.class)
        .getBody()
        .id();
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
