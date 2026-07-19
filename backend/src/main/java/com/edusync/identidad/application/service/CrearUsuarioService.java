package com.edusync.identidad.application.service;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.CrearUsuarioUseCase;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.DomainEmailEnUsoException;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa la creacion de usuarios (uso interno en este Design Doc: seed del primer
 * {@code SYSADMIN}; expuesto a otros modulos via {@code UsuarioCreacionPort}, no
 * directamente). Traduce los nombres de rol ({@code String}) del comando publico al enum
 * de dominio {@code Rol}, manteniendo ese enum interno al modulo.
 */
@Service
public class CrearUsuarioService implements CrearUsuarioUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;
  private final PasswordHasherPort passwordHasherPort;

  public CrearUsuarioService(
      UsuarioRepositoryPort usuarioRepositoryPort, PasswordHasherPort passwordHasherPort) {
    this.usuarioRepositoryPort = usuarioRepositoryPort;
    this.passwordHasherPort = passwordHasherPort;
  }

  @Override
  @Transactional
  public UsuarioId crear(CrearUsuarioCommand command) {
    if (usuarioRepositoryPort.existePorEmail(command.email())) {
      throw new DomainEmailEnUsoException(command.email());
    }

    Set<Rol> roles = command.roles().stream()
        .map(nombre -> Rol.valueOf(nombre.trim().toUpperCase()))
        .collect(Collectors.toUnmodifiableSet());

    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(),
        command.tenantId(),
        command.nombreCompleto(),
        command.email(),
        passwordHasherPort.hash(command.passwordPlano()),
        roles,
        true);

    Usuario guardado = usuarioRepositoryPort.guardar(usuario);
    return guardado.id();
  }
}
