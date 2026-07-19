package com.edusync.identidad.infrastructure;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.application.port.in.CrearUsuarioUseCase;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el primer usuario {@code SYSADMIN} al arrancar, unicamente si la tabla
 * {@code usuario} esta vacia (evita duplicar el seed en cada reinicio). Las credenciales
 * NUNCA se hardcodean: vienen de {@code edusync.seed.sysadmin.*} (env vars
 * {@code SYSADMIN_EMAIL}/{@code SYSADMIN_PASSWORD}); los valores por defecto en
 * {@code application.yml} son solo de desarrollo local, no un secreto real (AGENTS.md &sect;7).
 */
@Component
public class SysAdminSeeder implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SysAdminSeeder.class);

  private final UsuarioRepositoryPort usuarioRepositoryPort;
  private final CrearUsuarioUseCase crearUsuarioUseCase;
  private final String email;
  private final String password;

  public SysAdminSeeder(
      UsuarioRepositoryPort usuarioRepositoryPort,
      CrearUsuarioUseCase crearUsuarioUseCase,
      @Value("${edusync.seed.sysadmin.email}") String email,
      @Value("${edusync.seed.sysadmin.password}") String password) {
    this.usuarioRepositoryPort = usuarioRepositoryPort;
    this.crearUsuarioUseCase = crearUsuarioUseCase;
    this.email = email;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (usuarioRepositoryPort.contarTodos() > 0) {
      LOG.debug("Seed de SYSADMIN omitido: ya existen usuarios");
      return;
    }

    crearUsuarioUseCase.crear(
        new CrearUsuarioCommand(null, "SysAdmin Inicial", email, password, Set.of(Rol.SYSADMIN.name())));
    LOG.info("Usuario SYSADMIN inicial creado (email={})", email);
  }
}
