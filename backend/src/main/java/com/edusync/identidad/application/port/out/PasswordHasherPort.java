package com.edusync.identidad.application.port.out;

/**
 * Puerto de salida: hashing/verificacion de contrasenas. Implementado por
 * {@code BCryptPasswordHasher} (infrastructure/security). Evita que la capa de
 * aplicacion dependa directamente de {@code org.springframework.security.crypto.*}.
 */
public interface PasswordHasherPort {

  String hash(String passwordPlano);

  boolean coincide(String passwordPlano, String passwordHash);
}
