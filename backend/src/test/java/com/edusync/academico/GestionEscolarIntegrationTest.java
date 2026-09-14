package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PeriodoEvaluacionResponse;
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
 * Cubre el stop condition de {@code PR-IMPL-008} ({@code DD-UC-008} &sect;6): alta,
 * listado con filtros/paginacion, ciclo de estado de {@code GestionEscolar} y aislamiento
 * de tenant (404 cross-tenant, no 403).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class GestionEscolarIntegrationTest {

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
  void crudDeGestionEscolarDePuntaAPuntaConAislamientoDeTenant() {
    HttpHeaders adminTenantA = crearTenantYAutenticarAdmin("Colegio Academico A", "admin-academico-a@colegio.edu.bo");
    HttpHeaders adminTenantB = crearTenantYAutenticarAdmin("Colegio Academico B", "admin-academico-b@colegio.edu.bo");

    ResponseEntity<GestionEscolarResponse> creada = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
            adminTenantA),
        GestionEscolarResponse.class);
    assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(creada.getBody()).isNotNull();
    assertThat(creada.getBody().estado()).isEqualTo("PLANIFICACION");
    var gestionId = creada.getBody().id();

    ResponseEntity<PageResponse<GestionEscolarResponse>> lista = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantA),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lista.getBody()).isNotNull();
    assertThat(lista.getBody().content()).extracting(GestionEscolarResponse::id).contains(gestionId);
    assertThat(lista.getBody().page()).isZero();
    assertThat(lista.getBody().size()).isEqualTo(20);

    ResponseEntity<GestionEscolarResponse> activada = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("ACTIVA"), adminTenantA),
        GestionEscolarResponse.class);
    assertThat(activada.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(activada.getBody()).isNotNull();
    assertThat(activada.getBody().estado()).isEqualTo("ACTIVA");

    ResponseEntity<ErrorResponse> patchCrossTenant = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("CERRADA"), adminTenantB),
        ErrorResponse.class);
    assertThat(patchCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(patchCrossTenant.getBody()).isNotNull();
    assertThat(patchCrossTenant.getBody().codigo()).isEqualTo("E_GESTION_ESCOLAR_NO_ENCONTRADA");

    ResponseEntity<PageResponse<GestionEscolarResponse>> listaTenantB = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantB),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(listaTenantB.getBody()).isNotNull();
    assertThat(listaTenantB.getBody().content()).extracting(GestionEscolarResponse::id).doesNotContain(gestionId);
  }

  @Test
  void rechazaFechaFinNoPosteriorAFechaInicioCon422() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Fechas Invalidas", "admin-fechas@colegio.edu.bo");

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 11, 30), LocalDate.of(2027, 2, 1)),
            adminHeaders),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_FECHAS_INVALIDAS");
  }

  /**
   * {@code DD-UC-019}: el endpoint es exclusivamente {@code ADMIN} y ya no tiene una
   * maquina de estados restringida; una transicion directa {@code PLANIFICACION}
   * &rarr; {@code CERRADA} (antes rechazada con 422 {@code E_ESTADO_INVALIDO}) ahora se
   * acepta.
   */
  @Test
  void permiteTransicionDirectaDePlanificacionACerrada() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Transicion Directa", "admin-transicion@colegio.edu.bo");
    var gestionId = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                adminHeaders),
            GestionEscolarResponse.class)
        .getBody()
        .id();

    ResponseEntity<GestionEscolarResponse> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("CERRADA"), adminHeaders),
        GestionEscolarResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().estado()).isEqualTo("CERRADA");
  }

  @Test
  void listarGestionesEscolaresConFiltroQYEstadoYPaginacion() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Academico Filtros", "admin-academico-filtros@colegio.edu.bo");

    restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("Gestion Norte", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
            adminHeaders),
        GestionEscolarResponse.class);
    var idActiva = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("Gestion Sur", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                adminHeaders),
            GestionEscolarResponse.class)
        .getBody()
        .id();
    restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + idActiva + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("ACTIVA"), adminHeaders),
        GestionEscolarResponse.class);

    ResponseEntity<PageResponse<GestionEscolarResponse>> porNombre = restTemplate.exchange(
        "/api/v1/gestiones-escolares?q=norte",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(porNombre.getBody()).isNotNull();
    assertThat(porNombre.getBody().content()).extracting(GestionEscolarResponse::nombre)
        .containsExactly("Gestion Norte");

    ResponseEntity<PageResponse<GestionEscolarResponse>> porEstado = restTemplate.exchange(
        "/api/v1/gestiones-escolares?estado=ACTIVA",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(porEstado.getBody()).isNotNull();
    assertThat(porEstado.getBody().content()).extracting(GestionEscolarResponse::id).contains(idActiva);
    assertThat(porEstado.getBody().content()).allSatisfy(g -> assertThat(g.estado()).isEqualTo("ACTIVA"));

    ResponseEntity<PageResponse<GestionEscolarResponse>> paginado = restTemplate.exchange(
        "/api/v1/gestiones-escolares?page=0&size=1",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(paginado.getBody()).isNotNull();
    assertThat(paginado.getBody().content()).hasSize(1);
    assertThat(paginado.getBody().totalElements()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void gestionesEscolaresSinTokenDevuelve401() {
    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void gestionesEscolaresConRolSysAdminDevuelve403() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(sysAdminHeaders),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  /**
   * {@code DD-UC-021}: invariante "una sola gestion ACTIVA por tenant" a nivel HTTP. Al
   * activar una segunda gestion del mismo tenant, la primera se cierra automaticamente en
   * la misma transaccion (sin que el actor tenga que cerrarla explicitamente).
   */
  @Test
  void activarSegundaGestionCierraAutomaticamenteLaPrimeraDelTenant() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Auto Cierre", "admin-auto-cierre@colegio.edu.bo");

    UUID gestion1 = crearGestion(adminHeaders, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));
    activarGestion(adminHeaders, gestion1);

    UUID gestion2 = crearGestion(adminHeaders, "2028", LocalDate.of(2028, 2, 1), LocalDate.of(2028, 11, 30));
    activarGestion(adminHeaders, gestion2);

    ResponseEntity<GestionEscolarResponse> detalleGestion1 = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestion1,
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        GestionEscolarResponse.class);
    assertThat(detalleGestion1.getBody()).isNotNull();
    assertThat(detalleGestion1.getBody().estado()).isEqualTo("CERRADA");

    ResponseEntity<GestionEscolarResponse> detalleGestion2 = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestion2,
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        GestionEscolarResponse.class);
    assertThat(detalleGestion2.getBody()).isNotNull();
    assertThat(detalleGestion2.getBody().estado()).isEqualTo("ACTIVA");
  }

  /**
   * {@code DD-UC-021}: {@code PROFESOR} nunca lista ni elige una Gestion Escolar
   * ({@code GET /gestiones-escolares} y {@code GET /{id}} son exclusivos {@code ADMIN}, 403
   * para el resto de roles); en su lugar consume "la gestion actual" de forma implicita via
   * {@code GET /gestiones-escolares/activa} (404 si el tenant aun no tiene ninguna
   * {@code ACTIVA}, 200 con la misma gestion que activo el {@code ADMIN} en caso contrario).
   */
  @Test
  void profesorNoListaNiEligeYSoloVeLaGestionActivaViaEndpointActiva() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Rol Profesor", "admin-rol-profesor@colegio.edu.bo");
    restTemplate.exchange(
        "/api/v1/usuarios",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearUsuarioRequest("Profesor Rol", "profesor-rol-gestion@colegio.edu.bo", "secreto123", Set.of("PROFESOR")),
            adminHeaders),
        UsuarioResponse.class);
    HttpHeaders profesorHeaders = autenticarComo("profesor-rol-gestion@colegio.edu.bo", "secreto123");

    ResponseEntity<String> listaProhibida = restTemplate.exchange(
        "/api/v1/gestiones-escolares", HttpMethod.GET, new HttpEntity<>(profesorHeaders), String.class);
    assertThat(listaProhibida.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    ResponseEntity<ErrorResponse> sinActiva = restTemplate.exchange(
        "/api/v1/gestiones-escolares/activa",
        HttpMethod.GET,
        new HttpEntity<>(profesorHeaders),
        ErrorResponse.class);
    assertThat(sinActiva.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(sinActiva.getBody()).isNotNull();
    assertThat(sinActiva.getBody().codigo()).isEqualTo("E_GESTION_ESCOLAR_NO_ENCONTRADA");

    UUID gestionId = crearGestion(adminHeaders, "2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30));

    ResponseEntity<String> detalleProhibido = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId,
        HttpMethod.GET,
        new HttpEntity<>(profesorHeaders),
        String.class);
    assertThat(detalleProhibido.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    activarGestion(adminHeaders, gestionId);

    ResponseEntity<GestionEscolarResponse> activa = restTemplate.exchange(
        "/api/v1/gestiones-escolares/activa",
        HttpMethod.GET,
        new HttpEntity<>(profesorHeaders),
        GestionEscolarResponse.class);
    assertThat(activa.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(activa.getBody()).isNotNull();
    assertThat(activa.getBody().id()).isEqualTo(gestionId);
    assertThat(activa.getBody().estado()).isEqualTo("ACTIVA");

    ResponseEntity<List<PeriodoEvaluacionResponse>> periodosDeActiva = restTemplate.exchange(
        "/api/v1/gestiones-escolares/activa/periodos",
        HttpMethod.GET,
        new HttpEntity<>(profesorHeaders),
        new ParameterizedTypeReference<List<PeriodoEvaluacionResponse>>() {});
    assertThat(periodosDeActiva.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(periodosDeActiva.getBody()).isNotNull().hasSize(3);
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

  private void activarGestion(HttpHeaders admin, UUID gestionId) {
    restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("ACTIVA"), admin),
        GestionEscolarResponse.class);
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
