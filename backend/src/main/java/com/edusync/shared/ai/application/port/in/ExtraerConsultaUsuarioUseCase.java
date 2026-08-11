package com.edusync.shared.ai.application.port.in;

import com.edusync.shared.ai.domain.ConsultaUsuarioDTO;

/**
 * Puerto de entrada: extrae el nombre buscado de una solicitud en texto libre ("no recuerdo
 * el correo de un usuario llamado X..."). Spike sin endpoint de negocio todavia: no consulta
 * el modulo {@code identidad}, solo demuestra la capacidad de extraccion estructurada.
 */
public interface ExtraerConsultaUsuarioUseCase {

  /**
   * @throws com.edusync.shared.ai.domain.AiDeshabilitadoException si {@code edusync.ai.enabled=false}
   * @throws com.edusync.shared.ai.domain.EsquemaLlmNoCumplidoException si el LLM no logra
   *     extraer un nombre valido tras varios intentos
   */
  ConsultaUsuarioDTO extraer(String texto);
}
