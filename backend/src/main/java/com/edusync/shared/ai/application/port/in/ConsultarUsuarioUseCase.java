package com.edusync.shared.ai.application.port.in;

import com.edusync.shared.ai.domain.UsuarioResumen;
import java.util.List;

/**
 * Puerto de entrada: responde una pregunta en texto libre sobre un usuario del tenant
 * (nombre buscado &rarr; coincidencias con nombre/email/roles/estado). Compone
 * {@link ExtraerConsultaUsuarioUseCase} (extraccion estructurada) con la busqueda real en
 * {@code identidad}, scoped al tenant del actor autenticado.
 */
public interface ConsultarUsuarioUseCase {

  /**
   * @return 0..N coincidencias (lista vacia si no hay ninguna)
   * @throws com.edusync.shared.ai.domain.AiDeshabilitadoException si {@code edusync.ai.enabled=false}
   * @throws com.edusync.shared.ai.domain.EsquemaLlmNoCumplidoException si el LLM no logra
   *     extraer un nombre valido tras varios intentos
   */
  List<UsuarioResumen> consultar(String texto);
}
