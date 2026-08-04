package com.edusync.identidad.infrastructure.adapter.out.persistence;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.PasswordResetTokenRepositoryPort;
import com.edusync.identidad.domain.PasswordResetToken;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: traduce entre {@code identidad.domain.PasswordResetToken} y
 * {@code PasswordResetTokenJpaEntity}.
 */
@Component
@RequiredArgsConstructor
class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

  private final PasswordResetTokenJpaRepository jpaRepository;

  @Override
  public PasswordResetToken guardar(PasswordResetToken token) {
    PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity(
        token.getId(),
        token.getUsuarioId().valor(),
        token.getTokenHash(),
        token.getExpiraEn(),
        token.isUsado());
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public Optional<PasswordResetToken> buscarPorTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash).map(this::aDominio);
  }

  private PasswordResetToken aDominio(PasswordResetTokenJpaEntity entity) {
    return PasswordResetToken.reconstruir(
        entity.getId(),
        UsuarioId.de(entity.getUsuarioId()),
        entity.getTokenHash(),
        entity.getExpiraEn(),
        entity.isUsado());
  }
}
