package com.edusync.shared.ai.infrastructure.adapter.out.descubrimiento;

import com.edusync.shared.ai.application.port.out.DescubridorHerramientasPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.ParametroHerramienta;
import com.edusync.shared.ai.domain.ParametroHerramienta.Ubicacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Descubre el catalogo de herramientas leyendo GET /v3/api-docs del
 * propio backend (springdoc-openapi ya expuesto, ver pom.xml). Aplica
 * EXACTAMENTE el mismo filtro que DescubridorTools en
 * edusync-agente-llm (Python): ver ADR-0018 seccion 3. El resultado se
 * cachea en memoria (el catalogo no cambia sin redeploy), igual que el
 * agente Python.
 */
@Component
public class DescubridorHerramientasOpenApiAdapter implements DescubridorHerramientasPort {

    private static final Logger log = LoggerFactory.getLogger(DescubridorHerramientasOpenApiAdapter.class);

    private static final String PREFIJO_PERMITIDO = "/api/v1/";
    private static final Set<String> PREFIJOS_EXCLUIDOS = Set.of(
            "/api/v1/auth/", "/api/v1/plataforma/", "/api/v1/ai/");
    private static final Set<String> PALABRAS_CLAVE_POST_SEGURO = Set.of(
            "consultar", "buscar", "obtener", "listar", "search", "query");

    private final RestClient restClient;
    private final String openApiPath;
    private final AtomicReference<List<HerramientaLlm>> cache = new AtomicReference<>();

    public DescubridorHerramientasOpenApiAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${server.port:8080}") int puertoServidor,
            @Value("${edusync.ai.agente.openapi-path:/v3/api-docs}") String openApiPath) {
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

    /**
     * Replica exacta de _es_endpoint_permitido
     * (EduSync_LLM/edusync-agente-llm/agente/descubridor_tools.py):
     * prefijo /api/v1/**, excluye auth/plataforma/ai, solo GET o POST
     * de consulta explicita. Cubierta por el test de regresion de
     * seguridad obligatorio (DescubridorHerramientasOpenApiAdapterTest).
     */
    private boolean esEndpointPermitido(String path, String metodoHttp) {
        if (!path.startsWith(PREFIJO_PERMITIDO)) {
            return false;
        }
        for (String excluido : PREFIJOS_EXCLUIDOS) {
            if (path.startsWith(excluido)) {
                return false;
            }
        }
        if ("GET".equals(metodoHttp)) {
            return true;
        }
        if ("POST".equals(metodoHttp)) {
            String pathMinuscula = path.toLowerCase();
            return PALABRAS_CLAVE_POST_SEGURO.stream().anyMatch(pathMinuscula::contains);
        }
        return false; // PUT/PATCH/DELETE nunca son herramientas del agente.
    }

    private HerramientaLlm aHerramienta(String path, String metodoHttp, JsonNode operacion) {
        String nombre = operacion.hasNonNull("operationId")
                ? operacion.get("operationId").asString()
                : (metodoHttp + "_" + path).replaceAll("[^a-zA-Z0-9]+", "_");
        String descripcion = operacion.hasNonNull("summary")
                ? operacion.get("summary").asString()
                : operacion.path("description").asString("Herramienta " + nombre);

        List<ParametroHerramienta> parametros = new ArrayList<>();
        for (JsonNode p : operacion.path("parameters")) {
            String pNombre = p.path("name").asString();
            String pUbicacion = p.path("in").asString("query"); // path|query
            String pTipo = p.path("schema").path("type").asString("string");
            boolean requerido = p.path("required").asBoolean(false);
            Ubicacion ubicacion = "path".equals(pUbicacion) ? Ubicacion.PATH : Ubicacion.QUERY;
            parametros.add(new ParametroHerramienta(pNombre, pTipo, requerido, ubicacion));
        }
        // Cuerpo de request (POST de consulta): se declara como un unico
        // parametro BODY generico; el detalle de mapeo lo hace
        // EjecutorHerramientaHttpAdapter.
        if (operacion.has("requestBody")) {
            parametros.add(new ParametroHerramienta("body", "object", false, Ubicacion.BODY));
        }

        return new HerramientaLlm(nombre, descripcion, metodoHttp, path, parametros);
    }
}
