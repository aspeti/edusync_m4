package com.edusync.identidad.infrastructure.security;

import com.edusync.identidad.application.port.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador de {@link PasswordHasherPort} sobre BCrypt (Spring Security Crypto). */
@Component
@RequiredArgsConstructor
class BCryptPasswordHasher implements PasswordHasherPort {

  private final PasswordEncoder passwordEncoder;

  @Override
  public String hash(String passwordPlano) {
    return passwordEncoder.encode(passwordPlano);
  }

  @Override
  public boolean coincide(String passwordPlano, String passwordHash) {
    return passwordEncoder.matches(passwordPlano, passwordHash);
  }
}
