package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionCursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CalificacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionProfesorRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearEstudianteRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearInscripcionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearMateriaRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.EstudianteResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.EvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.InscripcionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.MateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.MisAsignacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PeriodoEvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PlanillaMateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.SeccionEvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.UpsertCalificacionesRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.CrearUsuarioRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.UsuarioResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
 * Stop condition de {@code PR-IMPL-033} ({@code DD-UC-033} / {@code FSD-UC-022}):
 * mis asignaciones, planilla, null-clear y 404 cross-tenant / no asignado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class PlanillaProfesorIntegrationTest {

  @Container
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:15")
          .withDatabaseName("edusync_it")
          .withUsername("edusync")
          .withPassword("edusync_it_local");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private TestRestTemplate restTemplate;

  @Value("${edusync.seed.sysadmin.email}")
  private String sysAdminEmail;

  @Value("${edusync.seed.sysadmin.password}")
  private String sysAdminPassword;

  @Test
  void misAsignacionesPlanillaNullClearYAislamiento() {
    HttpHeaders adminA =
        crearTenantYAutenticarAdmin("Colegio Planilla A", "admin-planilla-a@colegio.edu.bo");
    HttpHeaders adminB =
        crearTenantYAutenticarAdmin("Colegio Planilla B", "admin-planilla-b@colegio.edu.bo");

    UUID gestionId = crearGestion(adminA);
    UUID cursoId = crearCurso(adminA, "Primero de Primaria");
    UUID paraleloId = crearParalelo(adminA, cursoId, "A");
    UUID profesorId = crearProfesor(adminA, "Prof Planilla", "profesor-planilla@colegio.edu.bo");
    HttpHeaders profesorHeaders = autenticarComo("profesor-planilla@colegio.edu.bo", "secreto123");

    UUID materiaId = crearMateria(adminA, "Matemáticas");
    asignarCursoYProfesor(adminA, materiaId, cursoId, paraleloId, profesorId);

    UUID estudianteId = crearEstudiante(adminA, "99887766", "Ana Rojas");
    crearInscripcion(adminA, estudianteId, gestionId, cursoId, paraleloId);

    List<PeriodoEvaluacionResponse> periodos = listarPeriodos(gestionId, adminA);
    UUID t1 = periodos.get(0).id();
    Map<String, SeccionEvaluacionResponse> secciones =
        listarSecciones(gestionId, adminA).stream()
            .collect(Collectors.toMap(SeccionEvaluacionResponse::nombre, Function.identity()));

    abrirPeriodo(adminA, gestionId, t1);
    UUID evalSaber =
        crearEval(adminA, "Quiz 1", materiaId, t1, secciones.get("Saber").id());

    ResponseEntity<List<MisAsignacionResponse>> mis =
        restTemplate.exchange(
            "/api/v1/profesores/me/asignaciones",
            HttpMethod.GET,
            new HttpEntity<>(profesorHeaders),
            new ParameterizedTypeReference<List<MisAsignacionResponse>>() {});
    assertThat(mis.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mis.getBody()).hasSize(1);
    assertThat(mis.getBody().get(0).cursoEtiqueta()).isEqualTo("Primero de Primaria A");
    assertThat(mis.getBody().get(0).materiaNombre()).isEqualTo("Matemáticas");

    ResponseEntity<PlanillaMateriaResponse> planilla =
        restTemplate.exchange(
            "/api/v1/materias/"
                + materiaId
                + "/planilla?cursoId="
                + cursoId
                + "&paraleloId="
                + paraleloId
                + "&periodoId="
                + t1,
            HttpMethod.GET,
            new HttpEntity<>(profesorHeaders),
            PlanillaMateriaResponse.class);
    assertThat(planilla.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(planilla.getBody().editable()).isTrue();
    assertThat(planilla.getBody().columnas()).extracting(PlanillaMateriaResponse.Columna::nombre)
        .containsExactly("Quiz 1");
    assertThat(planilla.getBody().filas()).hasSize(1);
    assertThat(planilla.getBody().filas().get(0).estudianteId()).isEqualTo(estudianteId);

    upsert(profesorHeaders, evalSaber, estudianteId, new BigDecimal("40"));
    ResponseEntity<PlanillaMateriaResponse> conNota =
        restTemplate.exchange(
            "/api/v1/materias/"
                + materiaId
                + "/planilla?cursoId="
                + cursoId
                + "&paraleloId="
                + paraleloId
                + "&periodoId="
                + t1,
            HttpMethod.GET,
            new HttpEntity<>(profesorHeaders),
            PlanillaMateriaResponse.class);
    assertThat(conNota.getBody().filas().get(0).notas()).containsKey(evalSaber);

    ResponseEntity<List<CalificacionResponse>> clear =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalSaber + "/calificaciones",
            HttpMethod.PUT,
            new HttpEntity<>(
                new UpsertCalificacionesRequest(
                    List.of(new UpsertCalificacionesRequest.Item(estudianteId, null))),
                profesorHeaders),
            new ParameterizedTypeReference<List<CalificacionResponse>>() {});
    assertThat(clear.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(clear.getBody()).isEmpty();

    ResponseEntity<PlanillaMateriaResponse> sinNota =
        restTemplate.exchange(
            "/api/v1/materias/"
                + materiaId
                + "/planilla?cursoId="
                + cursoId
                + "&paraleloId="
                + paraleloId
                + "&periodoId="
                + t1,
            HttpMethod.GET,
            new HttpEntity<>(profesorHeaders),
            PlanillaMateriaResponse.class);
    assertThat(sinNota.getBody().filas().get(0).notas()).doesNotContainKey(evalSaber);

    ResponseEntity<ErrorResponse> cross =
        restTemplate.exchange(
            "/api/v1/materias/"
                + materiaId
                + "/planilla?cursoId="
                + cursoId
                + "&paraleloId="
                + paraleloId
                + "&periodoId="
                + t1,
            HttpMethod.GET,
            new HttpEntity<>(adminB),
            ErrorResponse.class);
    assertThat(cross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    UUID otroProfesor =
        crearProfesor(adminA, "Otro Prof", "otro-prof-planilla@colegio.edu.bo");
    HttpHeaders otroHeaders = autenticarComo("otro-prof-planilla@colegio.edu.bo", "secreto123");
    ResponseEntity<ErrorResponse> noAsignado =
        restTemplate.exchange(
            "/api/v1/materias/"
                + materiaId
                + "/planilla?cursoId="
                + cursoId
                + "&paraleloId="
                + paraleloId
                + "&periodoId="
                + t1,
            HttpMethod.GET,
            new HttpEntity<>(otroHeaders),
            ErrorResponse.class);
    assertThat(noAsignado.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(otroProfesor).isNotNull();
  }

  private void upsert(HttpHeaders headers, UUID evalId, UUID estudianteId, BigDecimal valor) {
    ResponseEntity<List<CalificacionResponse>> resp =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalId + "/calificaciones",
            HttpMethod.PUT,
            new HttpEntity<>(
                new UpsertCalificacionesRequest(
                    List.of(new UpsertCalificacionesRequest.Item(estudianteId, valor))),
                headers),
            new ParameterizedTypeReference<List<CalificacionResponse>>() {});
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private UUID crearEval(
      HttpHeaders admin, String nombre, UUID materiaId, UUID periodoId, UUID seccionId) {
    return restTemplate
        .exchange(
            "/api/v1/evaluaciones",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearEvaluacionRequest(
                    nombre, materiaId, periodoId, seccionId, LocalDate.of(2026, 3, 10), null),
                admin),
            EvaluacionResponse.class)
        .getBody()
        .id();
  }

  private void abrirPeriodo(HttpHeaders admin, UUID gestionId, UUID periodoId) {
    ParametrosPeriodoDePrueba.configurarPorDefecto(restTemplate, admin, gestionId, periodoId);
    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), admin),
        PeriodoEvaluacionResponse.class);
  }

  private UUID crearEstudiante(HttpHeaders admin, String rude, String nombre) {
    return restTemplate
        .exchange(
            "/api/v1/estudiantes",
            HttpMethod.POST,
            new HttpEntity<>(new CrearEstudianteRequest(rude, nombre, null, null), admin),
            EstudianteResponse.class)
        .getBody()
        .id();
  }

  private void crearInscripcion(
      HttpHeaders admin, UUID estudianteId, UUID gestionId, UUID cursoId, UUID paraleloId) {
    restTemplate.exchange(
        "/api/v1/inscripciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearInscripcionRequest(
                estudianteId, gestionId, cursoId, paraleloId, LocalDate.of(2026, 2, 15)),
            admin),
        InscripcionResponse.class);
  }

  private UUID crearGestion(HttpHeaders admin) {
    return restTemplate
        .exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest(
                    "2026", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 11, 30)),
                admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();
  }

  private UUID crearCurso(HttpHeaders admin, String nombre) {
    return restTemplate
        .exchange(
            "/api/v1/cursos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearCursoRequest(nombre), admin),
            CursoResponse.class)
        .getBody()
        .id();
  }

  private UUID crearParalelo(HttpHeaders admin, UUID cursoId, String nombre) {
    return restTemplate
        .exchange(
            "/api/v1/cursos/" + cursoId + "/paralelos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearParaleloRequest(nombre), admin),
            ParaleloResponse.class)
        .getBody()
        .id();
  }

  private UUID crearProfesor(HttpHeaders admin, String nombre, String email) {
    return restTemplate
        .exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearUsuarioRequest(nombre, email, "secreto123", Set.of("PROFESOR")), admin),
            UsuarioResponse.class)
        .getBody()
        .id();
  }

  private UUID crearMateria(HttpHeaders admin, String nombre) {
    return restTemplate
        .exchange(
            "/api/v1/materias",
            HttpMethod.POST,
            new HttpEntity<>(new CrearMateriaRequest(nombre), admin),
            MateriaResponse.class)
        .getBody()
        .id();
  }

  private void asignarCursoYProfesor(
      HttpHeaders admin, UUID materiaId, UUID cursoId, UUID paraleloId, UUID profesorId) {
    restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-curso",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionCursoRequest(cursoId, paraleloId), admin),
        AsignacionCursoResponse.class);
    restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-profesor",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), admin),
        AsignacionProfesorResponse.class);
  }

  private List<SeccionEvaluacionResponse> listarSecciones(UUID gestionId, HttpHeaders headers) {
    return restTemplate
        .exchange(
            "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<SeccionEvaluacionResponse>>() {})
        .getBody();
  }

  private List<PeriodoEvaluacionResponse> listarPeriodos(UUID gestionId, HttpHeaders headers) {
    return restTemplate
        .exchange(
            "/api/v1/gestiones-escolares/" + gestionId + "/periodos",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<PeriodoEvaluacionResponse>>() {})
        .getBody();
  }

  private HttpHeaders crearTenantYAutenticarAdmin(String nombreTenant, String adminEmail) {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);
    var tenantId =
        restTemplate
            .exchange(
                "/api/v1/plataforma/tenants",
                HttpMethod.POST,
                new HttpEntity<>(
                    new RegistrarTenantRequest(
                        nombreTenant, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
                    sysAdminHeaders),
                TenantResponse.class)
            .getBody()
            .id();
    restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/admins",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearAdminTenantRequest("Admin " + nombreTenant, adminEmail, "secreto123"),
            sysAdminHeaders),
        AdminCreadoResponse.class);
    return autenticarComo(adminEmail, "secreto123");
  }

  private HttpHeaders autenticarComo(String email, String password) {
    ResponseEntity<LoginResponse> login =
        restTemplate.postForEntity(
            "/api/v1/auth/login", new LoginRequest(email, password), LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(login.getBody().accessToken());
    return headers;
  }
}
