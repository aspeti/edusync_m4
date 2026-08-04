package com.edusync.identidad.application.service;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.ConfirmarRestablecimientoPasswordUseCase;
import com.edusync.identidad.application.port.in.IniciarRestablecimientoPasswordUseCase;
import com.edusync.identidad.application.port.out.NotificacionPort;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.PasswordResetTokenRepositoryPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.PasswordResetToken;
import com.edusync.identidad.domain.TokenResetInvalidoException;
import com.edusync.identidad.domain.Usuario;
import com.edusync.identidad.domain.UsuarioNoEncontradoException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicia y confirma el restablecimiento de contrasena (DD-UC-005 &sect;2). El token en
 * claro solo vive en memoria y en el parametro de {@link NotificacionPort}; nunca se
 * persiste ni se loguea (AGENTS.md &sect;7) — solo su hash SHA-256.
 */
@Service
@RequiredArgsConstructor
public class RestablecerPasswordService
    implements IniciarRestablecimientoPasswordUseCase, ConfirmarRestablecimientoPasswordUseCase {

  private static final Duration DURACION_TOKEN = Duration.ofHours(1);

  private final UsuarioRepositoryPort usuarioRepositoryPort;
  private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
  private final PasswordHasherPort passwordHasherPort;
  private final NotificacionPort notificacionPort;

  @Override
  @Transactional
  public void iniciar(UsuarioId usuarioId, UUID tenantIdActor) {
    Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId)
        .filter(u -> Objects.equals(u.getTenantId(), tenantIdActor))
        .orElseThrow(UsuarioNoEncontradoException::new);

    String tokenPlano = UUID.randomUUID().toString();
    PasswordResetToken token = PasswordResetToken.crear(
        usuario.getId(), sha256(tokenPlano), Instant.now().plus(DURACION_TOKEN));
    tokenRepositoryPort.guardar(token);

    notificacionPort.notificarRestablecimientoPassword(usuario.getId(), usuario.getEmail(), tokenPlano);
  }

  @Override
  @Transactional
  public void confirmar(String tokenPlano, String passwordNuevo) {
    PasswordResetToken token = tokenRepositoryPort.buscarPorTokenHash(sha256(tokenPlano))
        .orElseThrow(TokenResetInvalidoException::new);
    PasswordResetToken consumido = token.consumir();

    Usuario usuario = usuarioRepositoryPort.buscarPorId(token.getUsuarioId())
        .orElseThrow(UsuarioNoEncontradoException::new);
    Usuario actualizado = usuario.conPasswordHash(passwordHasherPort.hash(passwordNuevo));

    usuarioRepositoryPort.guardar(actualizado);
    tokenRepositoryPort.guardar(consumido);
  }

  private static String sha256(String valor) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 no disponible en esta JVM", ex);
    }
  }
}
