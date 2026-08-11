package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.in.ExtraerConsultaUsuarioUseCase;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.ConsultaUsuarioDTO;
import com.edusync.shared.ai.domain.EjemploFewShot;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Primer consumidor de {@link LlmStructuredExtractor}: extrae solo el nombre buscado por
 * quien pregunta (nunca email, RUDE, tenant ni roles — AGENTS.md &sect;7). El texto de
 * entrada es la propia pregunta de quien consulta, no un registro almacenado.
 */
@Service
public class ExtraerConsultaUsuarioService implements ExtraerConsultaUsuarioUseCase {

  private static final String INSTRUCCIONES =
      """
      Extrae UNICAMENTE el nombre de la persona que el usuario busca en su mensaje.
      Devuelve SOLO un JSON valido con exactamente este campo: nombreBuscado (string).
      No inventes datos que no esten en el texto (ni email, ni RUDE, ni otro dato).
      Si no se menciona un nombre, deja nombreBuscado como un string vacio.
      No incluyas explicaciones ni texto adicional.
      """;

  private static final List<EjemploFewShot> EJEMPLOS =
      List.of(
          new EjemploFewShot(
              "Hola, no recuerdo el correo de un profesor que se llama Roberto Fernandez, "
                  + "¿me pueden confirmar si existe en el sistema?",
              "{\"nombreBuscado\": \"Roberto Fernandez\"}"),
          new EjemploFewShot(
              "Necesito saber si Maria Elena Rojas esta registrada, se me olvido su email",
              "{\"nombreBuscado\": \"Maria Elena Rojas\"}"));

  private final LlmStructuredExtractor extractor;
  private final boolean enabled;

  public ExtraerConsultaUsuarioService(
      LlmStructuredExtractor extractor, @Value("${edusync.ai.enabled:false}") boolean enabled) {
    this.extractor = extractor;
    this.enabled = enabled;
  }

  @Override
  public ConsultaUsuarioDTO extraer(String texto) {
    if (!enabled) {
      throw new AiDeshabilitadoException();
    }
    return extractor.extraer(INSTRUCCIONES, EJEMPLOS, texto, ConsultaUsuarioDTO.class);
  }
}
