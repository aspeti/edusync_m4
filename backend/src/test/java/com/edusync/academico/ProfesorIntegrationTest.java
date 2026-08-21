package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionCursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorVistaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionProfesorRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearMateriaRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.MateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ProfesorResumenResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.CambiarEstadoRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.CrearUsuarioRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.UsuarioResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import com.edusync.shared.web.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Cubre el stop condition de {@code PR-IMPL-014} ({@code DD-UC-014} &sect;1.5): listado y
 * detalle de profesores, asignaciones enriquecidas, 404 cross-tenant / sin rol, profesor
 * inactivo 200, y el catalogo {@code GET /materias/profesores-disponibles} sigue
 * {@code {id, nombreCompleto}}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class ProfesorIntegrationTest {

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
  void listaDetalleAsignacionesYAislamientoDeTenant() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Profesores A", "admin-profesores-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Profesores B", "admin-profesores-b@colegio.edu.bo");

    UUID cursoId = crearCurso(adminA, "Primero de Primaria");
    UUID paraleloId = crearParalelo(adminA, cursoId, "A");
    UUID profesorId = crearProfesor(adminA, "Ana Perez", "ana.perez@colegio.edu.bo");
    UUID materiaId = crearMateria(adminA, "Matemáticas");
    asignarCursoYProfesor(adminA, materiaId, cursoId, paraleloId, profesorId);

    ResponseEntity<PageResponse<ProfesorResponse>> lista = restTemplate.exchange(
        "/api/v1/profesores",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<PageResponse<ProfesorResponse>>() {});
    assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lista.getBody().content()).extracting(ProfesorResponse::id).contains(profesorId);
    assertThat(lista.getBody().content()).extracting(ProfesorResponse::nombreCompleto).contains("Ana Perez");

    ResponseEntity<PageResponse<ProfesorResponse>> porNombre = restTemplate.exchange(
        "/api/v1/profesores?q=ana",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<PageResponse<ProfesorResponse>>() {});
    assertThat(porNombre.getBody().content()).extracting(ProfesorResponse::id).containsExactly(profesorId);

    ResponseEntity<ProfesorResponse> detalle = restTemplate.exchange(
        "/api/v1/profesores/" + profesorId,
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        ProfesorResponse.class);
    assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(detalle.getBody().activo()).isTrue();

    ResponseEntity<List<AsignacionProfesorVistaResponse>> asignaciones = restTemplate.exchange(
        "/api/v1/profesores/" + profesorId + "/asignaciones",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<List<AsignacionProfesorVistaResponse>>() {});
    assertThat(asignaciones.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(asignaciones.getBody()).hasSize(1);
    AsignacionProfesorVistaResponse vista = asignaciones.getBody().getFirst();
    assertThat(vista.materiaNombre()).isEqualTo("Matemáticas");
    assertThat(vista.cursoNombre()).isEqualTo("Primero de Primaria");
    assertThat(vista.paraleloNombre()).isEqualTo("A");
    assertThat(vista.cursoId()).isEqualTo(cursoId);

    ResponseEntity<List<Map<String, Object>>> catalogo = restTemplate.exchange(
        "/api/v1/materias/profesores-disponibles",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    assertThat(catalogo.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(catalogo.getBody()).isNotEmpty();
    assertThat(catalogo.getBody().getFirst().keySet()).containsExactlyInAnyOrder("id", "nombreCompleto");

    ResponseEntity<List<ProfesorResumenResponse>> catalogoTipado = restTemplate.exchange(
        "/api/v1/materias/profesores-disponibles",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<List<ProfesorResumenResponse>>() {});
    assertThat(catalogoTipado.getBody()).extracting(ProfesorResumenResponse::id).contains(profesorId);

    ResponseEntity<ErrorResponse> detalleCrossTenant = restTemplate.exchange(
        "/api/v1/profesores/" + profesorId,
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        ErrorResponse.class);
    assertThat(detalleCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(detalleCrossTenant.getBody().codigo()).isEqualTo("E_PROFESOR_NO_ENCONTRADO");

    ResponseEntity<ErrorResponse> asignacionesCrossTenant = restTemplate.exchange(
        "/api/v1/profesores/" + profesorId + "/asignaciones",
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        ErrorResponse.class);
    assertThat(asignacionesCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(asignacionesCrossTenant.getBody().codigo()).isEqualTo("E_PROFESOR_NO_ENCONTRADO");

    ResponseEntity<PageResponse<ProfesorResponse>> listaTenantB = restTemplate.exchange(
        "/api/v1/profesores",
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        new ParameterizedTypeReference<PageResponse<ProfesorResponse>>() {});
    assertThat(listaTenantB.getBody().content()).extracting(ProfesorResponse::id).doesNotContain(profesorId);
  }

  @Test
  void profesorInactivoConRolDevuelve200EnAsignaciones() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Profesor Inactivo", "admin-profesor-inactivo@colegio.edu.bo");
    UUID cursoId = crearCurso(admin, "Primero");
    UUID paraleloId = crearParalelo(admin, cursoId, "A");
    UUID profesorId = crearProfesor(admin, "Luis Soto", "luis.soto@colegio.edu.bo");
    UUID materiaId = crearMateria(admin, "Lenguaje");
    asignarCursoYProfesor(admin, materiaId, cursoId, paraleloId, profesorId);

    ResponseEntity<UsuarioResponse> desactivado = restTemplate.exchange(
        "/api/v1/usuarios/" + profesorId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoRequest(false), admin),
        UsuarioResponse.class);
    assertThat(desactivado.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(desactivado.getBody().activo()).isFalse();

    ResponseEntity<ProfesorResponse> detalle = restTemplate.exchange(
        "/api/v1/profesores/" + profesorId,
        HttpMethod.GET,
        new HttpEntity<>(admin),
        ProfesorResponse.class);
    assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(detalle.getBody().activo()).isFalse();

    ResponseEntity<List<AsignacionProfesorVistaResponse>> asignaciones = restTemplate.exchange(
        "/api/v1/profesores/" + profesorId + "/asignaciones",
        HttpMethod.GET,
        new HttpEntity<>(admin),
        new ParameterizedTypeReference<List<AsignacionProfesorVistaResponse>>() {});
    assertThat(asignaciones.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(asignaciones.getBody()).hasSize(1);

    ResponseEntity<ErrorResponse> escrituraNueva = restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-profesor",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), admin),
        ErrorResponse.class);
    assertThat(escrituraNueva.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(escrituraNueva.getBody().codigo()).isEqualTo("E_PROFESOR_NO_ENCONTRADO");
  }

  @Test
  void usuarioSinRolProfesorDevuelve404() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Sin Rol", "admin-sin-rol-profesor@colegio.edu.bo");
    UUID secretariaId = restTemplate.exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearUsuarioRequest("Marta Secretaria", "marta.sec@colegio.edu.bo", "secreto123", Set.of("SECRETARIA")),
                admin),
            UsuarioResponse.class)
        .getBody()
        .id();

    ResponseEntity<ErrorResponse> detalle = restTemplate.exchange(
        "/api/v1/profesores/" + secretariaId,
        HttpMethod.GET,
        new HttpEntity<>(admin),
        ErrorResponse.class);
    assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(detalle.getBody().codigo()).isEqualTo("E_PROFESOR_NO_ENCONTRADO");
  }

  @Test
  void secretariaPuedeListarProfesores() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Secretaria Profesores", "admin-sec-profesores@colegio.edu.bo");
    UUID profesorId = crearProfesor(admin, "Carlos Rojas", "carlos.rojas@colegio.edu.bo");
    restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearUsuarioRequest("Lucia Secretaria", "lucia.sec@colegio.edu.bo", "secreto123", Set.of("SECRETARIA")),
            admin),
        UsuarioResponse.class);
    HttpHeaders secretaria = autenticarComo("lucia.sec@colegio.edu.bo", "secreto123");

    ResponseEntity<PageResponse<ProfesorResponse>> lista = restTemplate.exchange(
        "/api/v1/profesores",
        HttpMethod.GET,
        new HttpEntity<>(secretaria),
        new ParameterizedTypeReference<PageResponse<ProfesorResponse>>() {});
    assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lista.getBody().content()).extracting(ProfesorResponse::id).contains(profesorId);
  }

  @Test
  void profesoresSinTokenDevuelve401() {
    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/profesores", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void profesoresConRolSysAdminDevuelve403() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/profesores", HttpMethod.GET, new HttpEntity<>(sysAdminHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private void asignarCursoYProfesor(
      HttpHeaders admin, UUID materiaId, UUID cursoId, UUID paraleloId, UUID profesorId) {
    ResponseEntity<AsignacionCursoResponse> asignacionCurso = restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-curso",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionCursoRequest(cursoId, paraleloId), admin),
        AsignacionCursoResponse.class);
    assertThat(asignacionCurso.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    ResponseEntity<AsignacionProfesorResponse> asignacionProfesor = restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-profesor",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), admin),
        AsignacionProfesorResponse.class);
    assertThat(asignacionProfesor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  private UUID crearMateria(HttpHeaders admin, String nombre) {
    return restTemplate.exchange(
            "/api/v1/materias",
            HttpMethod.POST,
            new HttpEntity<>(new CrearMateriaRequest(nombre), admin),
            MateriaResponse.class)
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

  private UUID crearProfesor(HttpHeaders admin, String nombre, String email) {
    return restTemplate.exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(new CrearUsuarioRequest(nombre, email, "secreto123", Set.of("PROFESOR")), admin),
            UsuarioResponse.class)
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
    ResponseEntity<LoginResponse> login =
        restTemplate.postForEntity("/api/v1/auth/login", new LoginRequest(email, password), LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(login.getBody().accessToken());
    return headers;
  }
}
