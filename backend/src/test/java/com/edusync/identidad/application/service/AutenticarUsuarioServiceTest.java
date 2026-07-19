package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.TokenGeneradorPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.CredencialesInvalidasException;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticarUsuarioServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private PasswordHasherPort passwordHasherPort;
  private TokenGeneradorPort tokenGeneradorPort;
  private AutenticarUsuarioService service;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    passwordHasherPort = mock(PasswordHasherPort.class);
    tokenGeneradorPort = mock(TokenGeneradorPort.class);
    service = new AutenticarUsuarioService(usuarioRepositoryPort, passwordHasherPort, tokenGeneradorPort);
  }

  @Test
  void autenticaCredencialesValidas() {
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), null, "SysAdmin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), true);
    when(usuarioRepositoryPort.buscarPorEmail("sysadmin@edusync.local")).thenReturn(Optional.of(usuario));
    when(passwordHasherPort.coincide("secreto", "hash")).thenReturn(true);
    TokenAcceso esperado = new TokenAcceso("jwt-simulado", 28800L);
    when(tokenGeneradorPort.generar(usuario)).thenReturn(esperado);

    TokenAcceso resultado = service.autenticar("sysadmin@edusync.local", "secreto");

    assertThat(resultado).isEqualTo(esperado);
  }

  @Test
  void rechazaEmailInexistente() {
    when(usuarioRepositoryPort.buscarPorEmail("nadie@edusync.local")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.autenticar("nadie@edusync.local", "x"))
        .isInstanceOf(CredencialesInvalidasException.class);
  }

  @Test
  void rechazaContrasenaIncorrecta() {
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), null, "SysAdmin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), true);
    when(usuarioRepositoryPort.buscarPorEmail("sysadmin@edusync.local")).thenReturn(Optional.of(usuario));
    when(passwordHasherPort.coincide("mala", "hash")).thenReturn(false);

    assertThatThrownBy(() -> service.autenticar("sysadmin@edusync.local", "mala"))
        .isInstanceOf(CredencialesInvalidasException.class);
  }

  @Test
  void rechazaUsuarioInactivo() {
    Usuario inactivo = Usuario.crear(
        UsuarioId.nueva(), null, "SysAdmin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), false);
    when(usuarioRepositoryPort.buscarPorEmail("sysadmin@edusync.local")).thenReturn(Optional.of(inactivo));

    assertThatThrownBy(() -> service.autenticar("sysadmin@edusync.local", "secreto"))
        .isInstanceOf(CredencialesInvalidasException.class);
  }
}
