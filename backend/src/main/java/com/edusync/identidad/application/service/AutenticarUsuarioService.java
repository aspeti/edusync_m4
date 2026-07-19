package com.edusync.identidad.application.service;

import com.edusync.identidad.application.port.in.AutenticarUsuarioUseCase;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.TokenGeneradorPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.CredencialesInvalidasException;
import com.edusync.identidad.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementa el login (parte de {@code FSD-UC-021}). No verifica todavia el estado del
 * tenant ({@code BR-014}, suspendido/vencido): esa verificacion se anade en
 * {@code DD-UC-003}/{@code PR-IMPL-003}, cuando exista {@code TenantConsultaPort}.
 */
@Service
@RequiredArgsConstructor
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;
  private final PasswordHasherPort passwordHasherPort;
  private final TokenGeneradorPort tokenGeneradorPort;

  @Override
  public TokenAcceso autenticar(String email, String password) {
    Usuario usuario = usuarioRepositoryPort.buscarPorEmail(email)
        .filter(Usuario::isActivo)
        .orElseThrow(CredencialesInvalidasException::new);

    if (!passwordHasherPort.coincide(password, usuario.getPasswordHash())) {
      throw new CredencialesInvalidasException();
    }

    return tokenGeneradorPort.generar(usuario);
  }
}
