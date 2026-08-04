package com.edusync.identidad.domain;

import com.edusync.identidad.UsuarioId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Mini-agregado independiente del restablecimiento de contrasena (DD-UC-005 &sect;2): token
 * de un solo uso, con expiracion, separado del aggregate {@link Usuario} para no mezclar
 * estado transitorio de seguridad con el modelo de identidad. Solo se persiste el hash del
 * token (nunca el valor en claro, AGENTS.md &sect;7). POJO inmutable: {@link #consumir()}
 * devuelve una nueva instancia marcada como usada, igual que las mutaciones de {@link Usuario}.
 */
@Getter
public final class PasswordResetToken {

  private final UUID id;
  private final UsuarioId usuarioId;
  private final String tokenHash;
  private final Instant expiraEn;
  private final boolean usado;

  private PasswordResetToken(
      UUID id, UsuarioId usuarioId, String tokenHash, Instant expiraEn, boolean usado) {
    this.id = id;
    this.usuarioId = usuarioId;
    this.tokenHash = tokenHash;
    this.expiraEn = expiraEn;
    this.usado = usado;
  }

  public static PasswordResetToken crear(UsuarioId usuarioId, String tokenHash, Instant expiraEn) {
    Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
    Objects.requireNonNull(tokenHash, "tokenHash no puede ser nulo");
    Objects.requireNonNull(expiraEn, "expiraEn no puede ser nulo");
    return new PasswordResetToken(UUID.randomUUID(), usuarioId, tokenHash, expiraEn, false);
  }

  /** Reconstruye un {@link PasswordResetToken} ya persistido. */
  public static PasswordResetToken reconstruir(
      UUID id, UsuarioId usuarioId, String tokenHash, Instant expiraEn, boolean usado) {
    return new PasswordResetToken(id, usuarioId, tokenHash, expiraEn, usado);
  }

  /**
   * Valida que el token siga vigente (no usado, no expirado) y devuelve una nueva instancia
   * marcada como usada (un solo uso, {@code FSD-UC-021} flujo alternativo A2).
   *
   * @throws TokenResetInvalidoException si el token ya fue usado o esta expirado
   */
  public PasswordResetToken consumir() {
    if (usado || Instant.now().isAfter(expiraEn)) {
      throw new TokenResetInvalidoException();
    }
    return new PasswordResetToken(id, usuarioId, tokenHash, expiraEn, true);
  }
}
