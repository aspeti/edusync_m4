package com.edusync.identidad.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import io.jsonwebtoken.security.SignatureException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String SECRET = "unit-test-secret-key-at-least-32-characters-long";

  private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 28800L);

  @Test
  void generaYValidaTokenParaUsuarioDeTenant() {
    UUID tenantId = UUID.randomUUID();
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), tenantId, "Admin", "admin@colegio.edu.bo", "hash", Set.of(Rol.ADMIN), true);

    TokenAcceso token = provider.generar(usuario);
    assertThat(token.accessToken()).isNotBlank();
    assertThat(token.expiresInSeconds()).isEqualTo(28800L);

    ClaimsToken claims = provider.validar(token.accessToken());
    assertThat(claims.userId()).isEqualTo(usuario.id().valor());
    assertThat(claims.tenantId()).isEqualTo(tenantId);
    assertThat(claims.roles()).containsExactly("ADMIN");
  }

  @Test
  void generaTokenSinTenantParaSysAdmin() {
    Usuario sysAdmin = Usuario.crear(
        UsuarioId.nueva(), null, "SysAdmin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), true);

    TokenAcceso token = provider.generar(sysAdmin);
    ClaimsToken claims = provider.validar(token.accessToken());

    assertThat(claims.tenantId()).isNull();
    assertThat(claims.roles()).containsExactly("SYSADMIN");
  }

  @Test
  void rechazaTokenFirmadoConOtroSecreto() {
    JwtTokenProvider otroProvider = new JwtTokenProvider("otro-secreto-distinto-de-al-menos-32-caracteres", 28800L);
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), null, "SysAdmin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), true);
    String token = otroProvider.generar(usuario).accessToken();

    assertThatThrownBy(() -> provider.validar(token)).isInstanceOf(SignatureException.class);
  }
}
