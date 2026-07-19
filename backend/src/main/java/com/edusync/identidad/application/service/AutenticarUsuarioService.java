package com.edusync.identidad.application.service;

import com.edusync.identidad.TenantConsultaPort;
import com.edusync.identidad.application.port.in.AutenticarUsuarioUseCase;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.TokenGeneradorPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.CredencialesInvalidasException;
import com.edusync.identidad.domain.TenantNoActivoException;
import com.edusync.identidad.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementa el login (parte de {@code FSD-UC-021}). Verifica credenciales antes que el
 * estado del tenant ({@code BR-014}): revelar si un Tenant esta suspendido/vencido a un
 * intento con credenciales invalidas filtraria informacion a un atacante.
 * {@code SYSADMIN} (sin {@code tenantId}) nunca pasa por esta verificacion.
 */
@Service
@RequiredArgsConstructor
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;
  private final PasswordHasherPort passwordHasherPort;
  private final TokenGeneradorPort tokenGeneradorPort;
  private final TenantConsultaPort tenantConsultaPort;

  @Override
  public TokenAcceso autenticar(String email, String password) {
    Usuario usuario = usuarioRepositoryPort.buscarPorEmail(email)
        .filter(Usuario::isActivo)
        .orElseThrow(CredencialesInvalidasException::new);

    if (!passwordHasherPort.coincide(password, usuario.getPasswordHash())) {
      throw new CredencialesInvalidasException();
    }

    if (usuario.getTenantId() != null && !tenantConsultaPort.estaActivo(usuario.getTenantId())) {
      throw new TenantNoActivoException();
    }

    return tokenGeneradorPort.generar(usuario);
  }
}
