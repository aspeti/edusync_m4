package com.edusync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del monolito modular EduSync (Spring Modulith, module-first, ADR-0011).
 *
 * <p>Vive en la raiz del paquete base {@code com.edusync} para que Spring Modulith detecte
 * cada subpaquete directo ({@code plataforma}, {@code identidad}, {@code academico},
 * {@code notassie}, {@code shared}) como un modulo de aplicacion independiente.
 */
@SpringBootApplication
public class EduSyncApplication {

  public static void main(String[] args) {
    SpringApplication.run(EduSyncApplication.class, args);
  }
}
