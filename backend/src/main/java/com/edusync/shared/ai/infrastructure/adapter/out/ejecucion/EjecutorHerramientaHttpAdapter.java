package com.edusync.shared.ai.infrastructure.adapter.out.ejecucion;

import com.edusync.shared.ai.application.port.out.DescubridorHerramientasPort;
import com.edusync.shared.ai.application.port.out.EjecutorHerramientaPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Ejecuta una LlamadaHerramienta por HTTP loopback contra los propios
 * controladores REST del backend, propagando el JWT del usuario que
 * hizo la pregunta (ADR-0018: "Identidad de ejecucion" — nunca una
 * cuenta tecnica fija). Nunca lanza: cualquier fallo se devuelve como
 * {"error": "..."} en el String de retorno, igual que
 * EjecutorTools.ejecutar en la referencia Python
 * (EduSync_LLM/edusync-agente-llm/agente/ejecutor_tools.py).
 *
 * MUST NOT loguear argumentos completos ni el cuerpo de la respuesta
 * (NFR-007) — solo nombre de herramienta y tipo de excepcion.
 */
@Component
public class EjecutorHerramientaHttpAdapter implements EjecutorHerramientaPort {

    private static final Logger log = LoggerFactory.getLogger(EjecutorHerramientaHttpAdapter.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final DescubridorHerramientasPort descubridorHerramientasPort;

    public EjecutorHerramientaHttpAdapter(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            DescubridorHerramientasPort descubridorHerramientasPort,
            @Value("${server.port:8080}") int puertoServidor) {
        this.restClient = restClientBuilder.baseUrl("http://localhost:" + puertoServidor).build();
        this.objectMapper = objectMapper;
        this.descubridorHerramientasPort = descubridorHerramientasPort;
    }

    @Override
    public String ejecutar(LlamadaHerramienta llamada, String jwtUsuario) {
        try {
            HerramientaLlm herramienta = descubridorHerramientasPort.descubrir().stream()
                    .filter(h -> h.nombre().equals(llamada.nombreHerramienta()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Herramienta desaparecio del catalogo entre el descubrimiento y la ejecucion: "
                                    + llamada.nombreHerramienta()));

            String pathResuelto = herramienta.path();
            Map<String, Object> argumentosRestantes = new HashMap<>(llamada.argumentos());
            Map<String, Object> queryParams = new HashMap<>();
            Map<String, Object> bodyParams = new HashMap<>();

            for (var parametro : herramienta.parametros()) {
                Object valor = argumentosRestantes.remove(parametro.nombre());
                if (valor == null) {
                    continue; // argumento no declarado por el modelo: se ignora, nunca se inventa.
                }
                switch (parametro.ubicacion()) {
                    case PATH -> pathResuelto = pathResuelto.replace("{" + parametro.nombre() + "}", valor.toString());
                    case QUERY -> queryParams.put(parametro.nombre(), valor);
                    case BODY -> bodyParams.put(parametro.nombre(), valor);
                }
            }
            // Cualquier argumento del modelo no declarado en el esquema real
            // queda en argumentosRestantes y se descarta aqui a proposito
            // (invariante: nunca reenviar un argumento no declarado).

            UriComponentsBuilder uri = UriComponentsBuilder.fromPath(pathResuelto);
            queryParams.forEach(uri::queryParam);

            RestClient.RequestBodySpec request = restClient
                    .method(HttpMethod.valueOf(herramienta.metodoHttp()))
                    .uri(uri.build().toUriString())
                    .header("Authorization", "Bearer " + jwtUsuario);

            String resultado;
            if ("POST".equalsIgnoreCase(herramienta.metodoHttp()) && !bodyParams.isEmpty()) {
                resultado = request.body(bodyParams).retrieve().body(String.class);
            } else {
                resultado = request.retrieve().body(String.class);
            }
            return resultado != null ? resultado : "{}";

        } catch (Exception e) {
            // MUST NOT propagar: el bucle del agente debe poder continuar con
            // una observacion de error (failure mode E_EDUSYNC_NO_DISPONIBLE).
            log.debug("Fallo ejecutando herramienta '{}': {}",
                    llamada.nombreHerramienta(), e.getClass().getSimpleName());
            return errorJson(e);
        }
    }

    private String errorJson(Exception e) {
        try {
            return objectMapper.writeValueAsString(Map.of("error", e.getClass().getSimpleName()));
        } catch (Exception ignored) {
            return "{\"error\":\"desconocido\"}";
        }
    }
}
