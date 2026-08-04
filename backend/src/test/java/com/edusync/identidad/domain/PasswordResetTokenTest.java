package com.edusync.identidad.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edusync.identidad.UsuarioId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {

  @Test
  void consumirMarcaElTokenComoUsadoSinMutarElOriginal() {
    PasswordResetToken token =
        PasswordResetToken.crear(UsuarioId.nueva(), "hash", Instant.now().plus(1, ChronoUnit.HOURS));

    PasswordResetToken consumido = token.consumir();

    assertThat(consumido.isUsado()).isTrue();
    assertThat(token.isUsado()).isFalse();
  }

  @Test
  void consumirRechazaUnTokenYaUsado() {
    PasswordResetToken token =
        PasswordResetToken.crear(UsuarioId.nueva(), "hash", Instant.now().plus(1, ChronoUnit.HOURS));
    PasswordResetToken yaUsado = token.consumir();

    assertThatThrownBy(yaUsado::consumir).isInstanceOf(TokenResetInvalidoException.class);
  }

  @Test
  void consumirRechazaUnTokenExpirado() {
    PasswordResetToken expirado =
        PasswordResetToken.crear(UsuarioId.nueva(), "hash", Instant.now().minus(1, ChronoUnit.MINUTES));

    assertThatThrownBy(expirado::consumir).isInstanceOf(TokenResetInvalidoException.class);
  }
}
