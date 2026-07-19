package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.DomainEmailEnUsoException;
import com.edusync.identidad.domain.InvarianteRolException;
import com.edusync.identidad.domain.Usuario;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearUsuarioServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private PasswordHasherPort passwordHasherPort;
  private CrearUsuarioService service;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    passwordHasherPort = mock(PasswordHasherPort.class);
    service = new CrearUsuarioService(usuarioRepositoryPort, passwordHasherPort);
  }

  @Test
  void creaSysAdminSinTenant() {
    when(usuarioRepositoryPort.existePorEmail("sysadmin@edusync.local")).thenReturn(false);
    when(passwordHasherPort.hash("secreto")).thenReturn("hash-bcrypt");
    when(usuarioRepositoryPort.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    UsuarioId id = service.crear(
        new CrearUsuarioCommand(null, "SysAdmin", "sysadmin@edusync.local", "secreto", Set.of("SYSADMIN")));

    assertThat(id).isNotNull();
    verify(usuarioRepositoryPort).guardar(any(Usuario.class));
  }

  @Test
  void rechazaEmailDuplicado() {
    when(usuarioRepositoryPort.existePorEmail("dup@edusync.local")).thenReturn(true);

    assertThatThrownBy(() -> service.crear(
        new CrearUsuarioCommand(null, "X", "dup@edusync.local", "secreto", Set.of("SYSADMIN"))))
        .isInstanceOf(DomainEmailEnUsoException.class);
  }

  @Test
  void propagaInvarianteRolViolada() {
    when(usuarioRepositoryPort.existePorEmail("admin@colegio.edu.bo")).thenReturn(false);
    when(passwordHasherPort.hash("secreto")).thenReturn("hash-bcrypt");

    assertThatThrownBy(() -> service.crear(
        new CrearUsuarioCommand(null, "Admin", "admin@colegio.edu.bo", "secreto", Set.of("ADMIN"))))
        .isInstanceOf(InvarianteRolException.class);
  }
}
