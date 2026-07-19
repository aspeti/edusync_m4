package com.edusync.identidad.infrastructure.security;

import com.edusync.identidad.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador de {@link PasswordHasherPort} sobre BCrypt (Spring Security Crypto). */
@Component
class BCryptPasswordHasher implements PasswordHasherPort {

  private final PasswordEncoder passwordEncoder;

  BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public String hash(String passwordPlano) {
    return passwordEncoder.encode(passwordPlano);
  }

  @Override
  public boolean coincide(String passwordPlano, String passwordHash) {
    return passwordEncoder.matches(passwordPlano, passwordHash);
  }
}
