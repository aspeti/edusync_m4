package com.edusync.identidad.application.port.out;

import com.edusync.identidad.domain.PasswordResetToken;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de {@link PasswordResetToken}. Implementado por
 * {@code PasswordResetTokenRepositoryAdapter} (JPA).
 */
public interface PasswordResetTokenRepositoryPort {

  PasswordResetToken guardar(PasswordResetToken token);

  Optional<PasswordResetToken> buscarPorTokenHash(String tokenHash);
}
