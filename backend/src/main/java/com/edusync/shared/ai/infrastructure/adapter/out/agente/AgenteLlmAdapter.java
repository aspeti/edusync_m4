package com.edusync.shared.ai.infrastructure.adapter.out.agente;

import com.edusync.shared.ai.application.port.out.AgenteLlmPort;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.LlamadaHerramienta;
import com.edusync.shared.ai.domain.MensajeAgente;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Adaptador de {@link AgenteLlmPort} sobre Spring AI 2.0. Construye {@link ToolCallback}
 * dinamicos a partir del catalogo descubierto y deja la ejecucion interna de tools
 * desactivada (ADR-0018): este adaptador solo devuelve la eleccion del modelo.
 *
 * <p>En Spring AI 2.0 no existe {@code internalToolExecutionEnabled}; el equivalente es
 * {@link AdvisorParams#toolCallingAdvisorAutoRegister(boolean) false}. {@code ToolCall}
 * vive en {@link AssistantMessage.ToolCall}, no en {@code chat.model}.
 */
@Component
public class AgenteLlmAdapter implements AgenteLlmPort {

  private static final Logger LOG = LoggerFactory.getLogger(AgenteLlmAdapter.class);

  private static final String SYSTEM_PROMPT =
      """
      Eres el asistente de EduSync. Respondes preguntas sobre datos
      academicos usando SOLO las herramientas de solo lectura
      disponibles. Encadena herramientas si hace falta (por ejemplo,
      resolver un nombre a un id antes de consultar sus notas).
      Cuando tengas suficiente informacion, responde en lenguaje
      natural sin volver a llamar herramientas. Nunca inventes datos
      que no vinieron de una herramienta.
      """;

  private final ChatClient agenteChatClient;
  private final ObjectMapper objectMapper;

  public AgenteLlmAdapter(
      @Qualifier("agenteChatClient") ChatClient agenteChatClient, ObjectMapper objectMapper) {
    this.agenteChatClient = agenteChatClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResultadoTurno decidirSiguientePaso(
      List<MensajeAgente> historial, List<HerramientaLlm> catalogo) {
    List<Message> mensajes = new ArrayList<>();
    mensajes.add(new SystemMessage(SYSTEM_PROMPT));
    mensajes.addAll(aMensajesSpringAi(historial));

    List<ToolCallback> herramientas = catalogo.stream().map(this::aToolCallback).toList();

    ChatResponse respuesta =
        agenteChatClient
            .prompt()
            .messages(mensajes)
            .toolCallbacks(herramientas)
            .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
            .call()
            .chatResponse();

    AssistantMessage.ToolCall elegida = primeraToolCall(respuesta);
    if (elegida != null) {
      Map<String, Object> argumentos = parsearArgumentos(elegida.arguments());
      return ResultadoTurno.deHerramienta(
          new LlamadaHerramienta(elegida.id(), elegida.name(), argumentos));
    }

    String texto =
        (respuesta != null
                && respuesta.getResult() != null
                && respuesta.getResult().getOutput() != null)
            ? respuesta.getResult().getOutput().getText()
            : "";
    return ResultadoTurno.deRespuestaFinal(texto == null ? "" : texto);
  }

  private List<Message> aMensajesSpringAi(List<MensajeAgente> historial) {
    List<Message> mensajes = new ArrayList<>();
    LlamadaHerramienta ultimaLlamada = null;
    for (MensajeAgente m : historial) {
      switch (m.rol()) {
        case USUARIO -> mensajes.add(new UserMessage(m.contenido()));
        case ASISTENTE -> {
          if (m.llamada() != null) {
            ultimaLlamada = m.llamada();
            mensajes.add(assistantConToolCall(m.llamada(), m.contenido()));
          } else {
            mensajes.add(new AssistantMessage(m.contenido() == null ? "" : m.contenido()));
          }
        }
        case HERRAMIENTA -> {
          String id = ultimaLlamada != null ? ultimaLlamada.id() : "tool";
          String nombre =
              ultimaLlamada != null ? ultimaLlamada.nombreHerramienta() : "herramienta";
          mensajes.add(
              ToolResponseMessage.builder()
                  .responses(
                      List.of(
                          new ToolResponseMessage.ToolResponse(
                              id, nombre, m.contenido() == null ? "{}" : m.contenido())))
                  .build());
        }
      }
    }
    return mensajes;
  }

  private AssistantMessage assistantConToolCall(LlamadaHerramienta llamada, String contenido) {
    String argumentosJson;
    try {
      argumentosJson = objectMapper.writeValueAsString(llamada.argumentos());
    } catch (Exception e) {
      argumentosJson = "{}";
    }
    return AssistantMessage.builder()
        .content(contenido == null ? "" : contenido)
        .toolCalls(
            List.of(
                new AssistantMessage.ToolCall(
                    llamada.id(), "function", llamada.nombreHerramienta(), argumentosJson)))
        .build();
  }

  private ToolCallback aToolCallback(HerramientaLlm herramienta) {
    ToolDefinition definicion =
        ToolDefinition.builder()
            .name(herramienta.nombre())
            .description(herramienta.descripcion())
            .inputSchema(esquemaJsonDe(herramienta))
            .build();

    return new ToolCallback() {
      @Override
      public ToolDefinition getToolDefinition() {
        return definicion;
      }

      @Override
      public String call(String toolInput) {
        throw new UnsupportedOperationException(
            "Ejecucion manual por EjecutorHerramientaPort — ver ADR-0018");
      }
    };
  }

  private String esquemaJsonDe(HerramientaLlm herramienta) {
    try {
      Map<String, Object> propiedades = new LinkedHashMap<>();
      List<String> requeridos = new ArrayList<>();
      for (var p : herramienta.parametros()) {
        propiedades.put(p.nombre(), Map.of("type", p.tipo()));
        if (p.requerido()) {
          requeridos.add(p.nombre());
        }
      }
      Map<String, Object> schema =
          Map.of("type", "object", "properties", propiedades, "required", requeridos);
      return objectMapper.writeValueAsString(schema);
    } catch (Exception e) {
      LOG.warn(
          "No se pudo serializar el esquema de '{}': {}",
          herramienta.nombre(),
          e.getClass().getSimpleName());
      return "{\"type\":\"object\"}";
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parsearArgumentos(String argumentosJson) {
    try {
      return objectMapper.readValue(argumentosJson, Map.class);
    } catch (Exception e) {
      LOG.warn("Argumentos de tool call no parseables como JSON: {}", e.getClass().getSimpleName());
      return Map.of();
    }
  }

  private static AssistantMessage.ToolCall primeraToolCall(ChatResponse respuesta) {
    if (respuesta == null || !respuesta.hasToolCalls() || respuesta.getResult() == null) {
      return null;
    }
    AssistantMessage output = respuesta.getResult().getOutput();
    if (output == null || !output.hasToolCalls()) {
      return null;
    }
    List<AssistantMessage.ToolCall> calls = output.getToolCalls();
    return calls == null || calls.isEmpty() ? null : calls.getFirst();
  }
}
