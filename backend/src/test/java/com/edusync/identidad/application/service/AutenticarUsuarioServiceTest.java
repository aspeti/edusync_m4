package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.edusync.identidad.TenantConsultaPort;
import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.application.port.out.PasswordHasherPort;
import com.edusync.identidad.application.port.out.TokenGeneradorPort;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.CredencialesInvalidasException;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.TenantNoActivoException;
import com.edusync.identidad.domain.Usuario;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticarUsuarioServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private PasswordHasherPort passwordHasherPort;
  private TokenGeneradorPort tokenGeneradorPort;
  private TenantConsultaPort tenantConsultaPort;
  private AutenticarUsuarioService service;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    passwordHasherPort = mock(PasswordHasherPort.class);
    tokenGeneradorPort = mock(TokenGeneradorPort.class);
    tenantConsultaPort = mock(TenantConsultaPort.class);
    service = new AutenticarUsuarioService(
        usuarioRepositoryPort, passwordHasherPort, tokenGeneradorPort, tenantConsultaPort);
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
  void sysAdminNuncaConsultaElEstadoDeUnTenant() {
    Usuario sysAdmin = Usuario.crear(
        UsuarioId.nueva(), null, "SysAdmin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), true);
    when(usuarioRepositoryPort.buscarPorEmail("sysadmin@edusync.local")).thenReturn(Optional.of(sysAdmin));
    when(passwordHasherPort.coincide("secreto", "hash")).thenReturn(true);
    when(tokenGeneradorPort.generar(sysAdmin)).thenReturn(new TokenAcceso("jwt-simulado", 28800L));

    service.autenticar("sysadmin@edusync.local", "secreto");

    verifyNoInteractions(tenantConsultaPort);
  }

  @Test
  void rechazaLoginDeUsuarioDeTenantSuspendidoOVencido() {
    UUID tenantId = UUID.randomUUID();
    Usuario admin = Usuario.crear(
        UsuarioId.nueva(), tenantId, "Admin", "admin@colegio.edu.bo", "hash", Set.of(Rol.ADMIN), true);
    when(usuarioRepositoryPort.buscarPorEmail("admin@colegio.edu.bo")).thenReturn(Optional.of(admin));
    when(passwordHasherPort.coincide("secreto", "hash")).thenReturn(true);
    when(tenantConsultaPort.estaActivo(tenantId)).thenReturn(false);

    assertThatThrownBy(() -> service.autenticar("admin@colegio.edu.bo", "secreto"))
        .isInstanceOf(TenantNoActivoException.class)
        .satisfies(ex -> assertThat(((TenantNoActivoException) ex).getErrorCode()).isEqualTo("E_TENANT_NO_ACTIVO"));
  }

  @Test
  void permiteLoginDeUsuarioDeTenantActivo() {
    UUID tenantId = UUID.randomUUID();
    Usuario admin = Usuario.crear(
        UsuarioId.nueva(), tenantId, "Admin", "admin@colegio.edu.bo", "hash", Set.of(Rol.ADMIN), true);
    when(usuarioRepositoryPort.buscarPorEmail("admin@colegio.edu.bo")).thenReturn(Optional.of(admin));
    when(passwordHasherPort.coincide("secreto", "hash")).thenReturn(true);
    when(tenantConsultaPort.estaActivo(tenantId)).thenReturn(true);
    TokenAcceso esperado = new TokenAcceso("jwt-simulado", 28800L);
    when(tokenGeneradorPort.generar(admin)).thenReturn(esperado);

    TokenAcceso resultado = service.autenticar("admin@colegio.edu.bo", "secreto");

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
