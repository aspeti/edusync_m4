package com.edusync.identidad.application.port.out;

import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.domain.Usuario;

/**
 * Puerto de salida: emision de tokens de acceso. Implementado por
 * {@code JwtTokenProvider} (infrastructure/security). Mantiene la capa de aplicacion
 * desacoplada de la libreria JWT concreta (AGENTS.md &sect;5, arquitectura hexagonal).
 */
public interface TokenGeneradorPort {

  TokenAcceso generar(Usuario usuario);
}
