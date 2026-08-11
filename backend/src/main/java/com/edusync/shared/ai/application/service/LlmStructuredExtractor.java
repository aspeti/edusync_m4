package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.EjemploFewShot;
import com.edusync.shared.ai.domain.EsquemaLlmNoCumplidoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Extrae y valida una salida estructurada del LLM a partir de texto libre: genera &rarr;
 * valida contra un esquema (Bean Validation) &rarr; si falla, reintenta inyectando el error
 * como retroalimentacion &rarr; tras {@link #MAX_INTENTOS} falla de forma controlada
 * ({@link EsquemaLlmNoCumplidoException}). Reutilizable por cualquier esquema nuevo
 * (record + anotaciones {@code jakarta.validation}) sin tocar {@link LlmPort} ni sus
 * adaptadores.
 *
 * <p>No usa mensajes con roles (system/user/assistant): {@link LlmPort#completar(String)}
 * recibe un unico string porque el adaptador de Ollama usa {@code /api/generate}, que no
 * soporta roles (a diferencia de Open WebUI, que usa {@code /api/chat/completions} con
 * {@code messages}). El prompt de reintento se construye como texto plano creciente
 * (instrucciones + ejemplos few-shot + intento anterior + error de validacion), igual de
 * efectivo para este proposito y sin requerir cambiar el contrato del puerto ni el adaptador
 * de Ollama.
 */
@Component
public class LlmStructuredExtractor {

  private static final int MAX_INTENTOS = 3;
  private static final Logger LOG = LoggerFactory.getLogger(LlmStructuredExtractor.class);
  private static final Pattern BLOQUE_JSON = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

  private final LlmPort llmPort;
  private final ObjectMapper objectMapper;
  private final Validator validator;

  public LlmStructuredExtractor(LlmPort llmPort, ObjectMapper objectMapper, Validator validator) {
    this.llmPort = llmPort;
    this.objectMapper = objectMapper;
    this.validator = validator;
  }

  /**
   * @param instrucciones instrucciones de sistema (que extraer, formato esperado)
   * @param ejemplos ejemplos few-shot (entrada &rarr; salida JSON esperada)
   * @param textoUsuario texto libre del que se extrae el esquema
   * @param esquema clase destino (record con anotaciones {@code jakarta.validation})
   * @throws EsquemaLlmNoCumplidoException si el LLM no cumple el esquema tras
   *     {@value #MAX_INTENTOS} intentos
   */
  public <T> T extraer(
      String instrucciones, List<EjemploFewShot> ejemplos, String textoUsuario, Class<T> esquema) {
    StringBuilder prompt = new StringBuilder(construirPromptBase(instrucciones, ejemplos, textoUsuario));

    for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
      String crudo = limpiarJson(llmPort.completar(prompt.toString()).texto());
      LOG.debug(
          "Intento {}/{} de extraccion de {} ({} caracteres de respuesta)",
          intento, MAX_INTENTOS, esquema.getSimpleName(), crudo.length());

      try {
        T resultado = objectMapper.readValue(crudo, esquema);
        Set<ConstraintViolation<T>> violaciones = validator.validate(resultado);
        if (violaciones.isEmpty()) {
          return resultado;
        }
        prompt.append(feedbackReintento(crudo, describir(violaciones)));
      } catch (JacksonException ex) {
        prompt.append(feedbackReintento(crudo, "JSON invalido: " + ex.getMessage()));
      }
    }

    throw new EsquemaLlmNoCumplidoException(esquema.getSimpleName(), MAX_INTENTOS);
  }

  private String construirPromptBase(String instrucciones, List<EjemploFewShot> ejemplos, String textoUsuario) {
    StringBuilder sb = new StringBuilder(instrucciones).append("\n\n");
    for (EjemploFewShot ejemplo : ejemplos) {
      sb.append("Entrada: ").append(ejemplo.entrada()).append('\n')
          .append("Salida: ").append(ejemplo.salidaJson()).append("\n\n");
    }
    return sb.append("Entrada: ").append(textoUsuario).append('\n').append("Salida:").toString();
  }

  private String feedbackReintento(String respuestaPrevia, String error) {
    return "\n\nTu respuesta anterior fue: " + respuestaPrevia
        + "\nEsa respuesta no cumple el esquema esperado. Error: " + error
        + "\nCorrige y devuelve SOLO el JSON valido, sin texto adicional.\nSalida:";
  }

  private <T> String describir(Set<ConstraintViolation<T>> violaciones) {
    return violaciones.stream()
        .map(v -> v.getPropertyPath() + " " + v.getMessage())
        .collect(Collectors.joining("; "));
  }

  private String limpiarJson(String texto) {
    Matcher m = BLOQUE_JSON.matcher(texto);
    return m.find() ? m.group() : texto.trim();
  }
}
