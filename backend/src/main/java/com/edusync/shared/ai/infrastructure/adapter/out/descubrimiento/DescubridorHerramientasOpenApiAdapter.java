package com.edusync.shared.ai.infrastructure.adapter.out.descubrimiento;

import com.edusync.shared.ai.application.port.out.DescubridorHerramientasPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.ParametroHerramienta;
import com.edusync.shared.ai.domain.ParametroHerramienta.Ubicacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Descubre el catalogo de herramientas leyendo GET /v3/api-docs.
 *
 * <p>Estrategia hibrida (DD-UC-024): OpenAPI dinamico + <strong>allowlist</strong>
 * de prefijos academicos/identidad (el sistema en general, no solo usuarios)
 * + enrichment de descripciones para el LLM. Sigue excluyendo auth/plataforma/ai,
 * escrituras y paths de calificaciones individuales (NFR-007).
 */
@Component
public class DescubridorHerramientasOpenApiAdapter implements DescubridorHerramientasPort {

    private static final Logger log = LoggerFactory.getLogger(DescubridorHerramientasOpenApiAdapter.class);

    private static final String PREFIJO_PERMITIDO = "/api/v1/";
    private static final Set<String> PREFIJOS_EXCLUIDOS = Set.of(
            "/api/v1/auth/", "/api/v1/plataforma/", "/api/v1/ai/");
    /**
     * Dominio consultable por el asistente: academico + usuarios del tenant.
     * Fuera: notassie, plataforma, auth, ai, y cualquier GET no listado.
     */
    static final List<String> PREFIJOS_ALLOWLIST = List.of(
            "/api/v1/gestiones-escolares",
            "/api/v1/cursos",
            "/api/v1/materias",
            "/api/v1/estudiantes",
            "/api/v1/profesores",
            "/api/v1/evaluaciones",
            "/api/v1/usuarios",
            "/api/v1/periodos-evaluacion",
            "/api/v1/secciones-evaluacion");
    private static final Set<String> FRAGMENTOS_PII_PROHIBIDOS = Set.of(
            "/calificaciones", "/nota-provisional");
    private static final Set<String> PALABRAS_CLAVE_POST_SEGURO = Set.of(
            "consultar", "buscar", "obtener", "listar", "search", "query");

    private final RestClient restClient;
    private final String openApiPath;
    private final AtomicReference<List<HerramientaLlm>> cache = new AtomicReference<>();

    @Autowired
    public DescubridorHerramientasOpenApiAdapter(
            @Value("${server.port:8080}") int puertoServidor,
            @Value("${edusync.ai.agente.openapi-path:/v3/api-docs}") String openApiPath) {
        this(RestClient.builder(), puertoServidor, openApiPath);
    }

    DescubridorHerramientasOpenApiAdapter(
            RestClient.Builder restClientBuilder, int puertoServidor, String openApiPath) {
        this.restClient = restClientBuilder.baseUrl("http://localhost:" + puertoServidor).build();
        this.openApiPath = openApiPath;
    }

    @Override
    public List<HerramientaLlm> descubrir() {
        List<HerramientaLlm> cacheado = cache.get();
        if (cacheado != null) {
            return cacheado;
        }
        List<HerramientaLlm> descubiertas = leerYFiltrar();
        cache.set(descubiertas);
        return descubiertas;
    }

    private List<HerramientaLlm> leerYFiltrar() {
        JsonNode openApi = restClient.get()
                .uri(openApiPath)
                .retrieve()
                .body(JsonNode.class);

        List<HerramientaLlm> herramientas = new ArrayList<>();
        if (openApi == null || !openApi.has("paths")) {
            log.warn("GET {} no devolvio 'paths'; catalogo de herramientas vacio", openApiPath);
            return herramientas;
        }

        JsonNode paths = openApi.get("paths");
        for (Map.Entry<String, JsonNode> pathEntry : paths.properties()) {
            String path = pathEntry.getKey();
            JsonNode operaciones = pathEntry.getValue();

            for (Map.Entry<String, JsonNode> opEntry : operaciones.properties()) {
                String metodo = opEntry.getKey().toUpperCase();
                JsonNode operacion = opEntry.getValue();

                if (!esEndpointPermitido(path, metodo)) {
                    continue;
                }
                herramientas.add(aHerramienta(path, metodo, operacion));
            }
        }
        log.info("Catalogo de herramientas del agente: {} endpoint(s) descubiertos", herramientas.size());
        return herramientas;
    }

    boolean esEndpointPermitido(String path, String metodoHttp) {
        if (!path.startsWith(PREFIJO_PERMITIDO)) {
            return false;
        }
        for (String excluido : PREFIJOS_EXCLUIDOS) {
            if (path.startsWith(excluido)) {
                return false;
            }
        }
        if (!estaEnAllowlist(path)) {
            return false;
        }
        String pathMinuscula = path.toLowerCase();
        for (String fragmento : FRAGMENTOS_PII_PROHIBIDOS) {
            if (pathMinuscula.contains(fragmento)) {
                return false;
            }
        }
        if ("GET".equals(metodoHttp)) {
            return true;
        }
        if ("POST".equals(metodoHttp)) {
            return PALABRAS_CLAVE_POST_SEGURO.stream().anyMatch(pathMinuscula::contains);
        }
        return false;
    }

    static boolean estaEnAllowlist(String path) {
        for (String prefijo : PREFIJOS_ALLOWLIST) {
            if (path.equals(prefijo) || path.startsWith(prefijo + "/")) {
                return true;
            }
        }
        return false;
    }

    private HerramientaLlm aHerramienta(String path, String metodoHttp, JsonNode operacion) {
        String nombre = operacion.hasNonNull("operationId")
                ? operacion.get("operationId").asString()
                : (metodoHttp + "_" + path).replaceAll("[^a-zA-Z0-9]+", "_");
        String summary = operacion.hasNonNull("summary")
                ? operacion.get("summary").asString()
                : operacion.path("description").asString("");
        String descripcion = DescripcionHerramientaAgente.enriquecer(path, metodoHttp, summary, nombre);

        List<ParametroHerramienta> parametros = new ArrayList<>();
        for (JsonNode p : operacion.path("parameters")) {
            String pNombre = p.path("name").asString();
            String pUbicacion = p.path("in").asString("query");
            String pTipo = p.path("schema").path("type").asString("string");
            boolean requerido = p.path("required").asBoolean(false);
            Ubicacion ubicacion = "path".equals(pUbicacion) ? Ubicacion.PATH : Ubicacion.QUERY;
            parametros.add(new ParametroHerramienta(pNombre, pTipo, requerido, ubicacion));
        }
        if (operacion.has("requestBody")) {
            parametros.add(new ParametroHerramienta("body", "object", false, Ubicacion.BODY));
        }

        return new HerramientaLlm(nombre, descripcion, metodoHttp, path, parametros);
    }
}
