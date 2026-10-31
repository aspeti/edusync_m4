package com.edusync.shared.ai.infrastructure.adapter.out.monitoring;

import com.edusync.shared.ai.application.monitoring.RedactorBitacora;
import com.edusync.shared.ai.application.monitoring.RegistroInteraccionAgente;
import com.edusync.shared.ai.domain.PasoTrazaAgente;
import com.edusync.shared.ai.domain.RespuestaAgente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Escribe una línea JSON por interacción cuando el registro está encendido.
 * No incluye el JWT. El modo por defecto no guarda el texto de la pregunta ni de la respuesta.
 */
@Component
@ConditionalOnProperty(name = "edusync.ai.monitor.registro", havingValue = "true")
public class RegistroInteraccionJsonl implements RegistroInteraccionAgente {

    private static final Logger log = LoggerFactory.getLogger(RegistroInteraccionJsonl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Path archivo;
    private final String version;
    private final boolean textoCompleto;

    public RegistroInteraccionJsonl(
            @Value("${edusync.ai.monitor.archivo:logs/asistente.jsonl}") String archivo,
            @Value("${edusync.ai.version:dev}") String version,
            @Value("${edusync.ai.monitor.texto:minimizado}") String texto) {
        this.archivo = Path.of(archivo);
        this.version = version;
        this.textoCompleto = "laboratorio".equalsIgnoreCase(texto);
    }

    @Override
    public synchronized void registrar(String jwt, String pregunta, RespuestaAgente respuesta, long latenciaMs) {
        try {
            ObjectNode nodo = MAPPER.createObjectNode();
            nodo.put("ts", LocalDateTime.now().format(TS));
            nodo.put("usuario", RedactorBitacora.sujeto(jwt));
            nodo.put("version", version);
            nodo.put("texto_omitido", !textoCompleto);
            if (textoCompleto) {
                nodo.put("pregunta", RedactorBitacora.texto(pregunta));
                nodo.put("respuesta", RedactorBitacora.texto(respuesta.respuesta()));
            } else {
                nodo.put("pregunta_sha256", RedactorBitacora.sha256(pregunta == null ? "" : pregunta));
                nodo.put("respuesta_sha256", RedactorBitacora.sha256(respuesta.respuesta() == null ? "" : respuesta.respuesta()));
            }
            var fuentesNodo = nodo.putArray("fuentes");
            fuentes(respuesta).forEach(fuentesNodo::add);
            nodo.put("camino", respuesta.camino());
            if (respuesta.herramientasUsadas().isEmpty()) {
                nodo.putNull("herramienta");
            } else {
                nodo.put("herramienta", respuesta.herramientasUsadas().getFirst());
            }
            nodo.put("guard", guard(respuesta));
            nodo.put("latencia_ms", latenciaMs);
            nodo.putNull("tokens");
            nodo.putNull("feedback");
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            Files.writeString(
                    archivo,
                    MAPPER.writeValueAsString(nodo) + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ex) {
            log.warn("Bitacora del asistente no escrita: {}", ex.getClass().getSimpleName());
        }
    }

    private static List<String> fuentes(RespuestaAgente respuesta) {
        List<String> fuentes = new ArrayList<>();
        for (PasoTrazaAgente paso : respuesta.steps()) {
            fuentes.addAll(paso.tablasFuente());
        }
        if (fuentes.isEmpty() && respuesta.fuente() != null) {
            fuentes.add(respuesta.fuente());
        }
        return fuentes.stream().distinct().toList();
    }

    private static String guard(RespuestaAgente respuesta) {
        if (RespuestaAgente.CAMINO_BLOQUEADO.equals(respuesta.camino())) {
            return "bloqueado";
        }
        boolean redactado = respuesta.steps().stream()
                .anyMatch(p -> "guardrail_salida".equals(p.toolId()) && !p.exito());
        if (redactado) {
            return "redactado";
        }
        boolean rechazo = respuesta.steps().stream()
                .anyMatch(p -> !p.exito()
                        && !"guardrail_entrada".equals(p.toolId())
                        && !"guardrail_salida".equals(p.toolId()));
        if (rechazo) {
            return "rechazado";
        }
        return "permitido";
    }
}
