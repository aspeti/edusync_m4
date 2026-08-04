package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.NotificacionPort;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.PasswordResetTokenRepositoryPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.PasswordResetToken;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.TokenResetInvalidoException;
import com.edusync.identidad.domain.Usuario;
import com.edusync.identidad.domain.UsuarioNoEncontradoException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RestablecerPasswordServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private PasswordResetTokenRepositoryPort tokenRepositoryPort;
  private PasswordHasherPort passwordHasherPort;
  private NotificacionPort notificacionPort;
  private RestablecerPasswordService service;

  private final Map<String, PasswordResetToken> tokensPorHash = new HashMap<>();

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    tokenRepositoryPort = mock(PasswordResetTokenRepositoryPort.class);
    passwordHasherPort = mock(PasswordHasherPort.class);
    notificacionPort = mock(NotificacionPort.class);
    service = new RestablecerPasswordService(
        usuarioRepositoryPort, tokenRepositoryPort, passwordHasherPort, notificacionPort);

    tokensPorHash.clear();
    when(tokenRepositoryPort.guardar(any(PasswordResetToken.class))).thenAnswer(inv -> {
      PasswordResetToken token = inv.getArgument(0);
      tokensPorHash.put(token.getTokenHash(), token);
      return token;
    });
    when(tokenRepositoryPort.buscarPorTokenHash(anyString()))
        .thenAnswer(inv -> Optional.ofNullable(tokensPorHash.get((String) inv.getArgument(0))));
  }

  @Test
  void iniciarYConfirmarActualizanElPasswordDeUnSoloUso() {
    UUID tenantId = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario = Usuario.crear(
        usuarioId, tenantId, "X", "x@x.com", "hash-viejo", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
    when(usuarioRepositoryPort.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    when(passwordHasherPort.hash("nueva-contrasena")).thenReturn("hash-nuevo");

    service.iniciar(usuarioId, tenantId);

    ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificacionPort).notificarRestablecimientoPassword(eq(usuarioId), eq("x@x.com"), tokenCaptor.capture());
    String tokenPlano = tokenCaptor.getValue();
    assertThat(tokenPlano).isNotBlank();

    service.confirmar(tokenPlano, "nueva-contrasena");

    ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepositoryPort, org.mockito.Mockito.times(1)).guardar(usuarioCaptor.capture());
    assertThat(usuarioCaptor.getValue().getPasswordHash()).isEqualTo("hash-nuevo");

    // Reutilizar el mismo token (un solo uso, FSD-UC-021 A2) debe fallar.
    assertThatThrownBy(() -> service.confirmar(tokenPlano, "otra-contrasena"))
        .isInstanceOf(TokenResetInvalidoException.class);
  }

  @Test
  void confirmarConTokenDesconocidoLanzaTokenResetInvalido() {
    assertThatThrownBy(() -> service.confirmar("token-inexistente", "nueva-contrasena"))
        .isInstanceOf(TokenResetInvalidoException.class);
  }

  @Test
  void confirmarConTokenExpiradoLanzaTokenResetInvalido() {
    UsuarioId usuarioId = UsuarioId.nueva();
    PasswordResetToken expirado =
        PasswordResetToken.crear(usuarioId, "hash-irrelevante", Instant.now().minus(1, ChronoUnit.MINUTES));
    when(tokenRepositoryPort.buscarPorTokenHash(anyString())).thenReturn(Optional.of(expirado));

    assertThatThrownBy(() -> service.confirmar("token-plano-cualquiera", "nueva-contrasena"))
        .isInstanceOf(TokenResetInvalidoException.class);
  }

  @Test
  void iniciarRechazaUsuarioDeOtroTenantConNotFound() {
    UUID tenantIdDelUsuario = UUID.randomUUID();
    UUID tenantIdActor = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario = Usuario.crear(
        usuarioId, tenantIdDelUsuario, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> service.iniciar(usuarioId, tenantIdActor))
        .isInstanceOf(UsuarioNoEncontradoException.class);
  }
}
