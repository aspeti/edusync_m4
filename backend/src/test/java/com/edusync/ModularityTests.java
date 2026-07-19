package com.edusync;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Prueba de arquitectura exigida por {@code ADR-0011}: verifica que los 5 modulos de
 * Spring Modulith ({@code plataforma}, {@code identidad}, {@code academico},
 * {@code notassie}, {@code shared}) no tienen ciclos ni accesos a paquetes internos de
 * otro modulo. No arranca el contexto de Spring (analisis estatico de bytecode);
 * no requiere base de datos.
 */
class ModularityTests {

  static final ApplicationModules MODULES = ApplicationModules.of(EduSyncApplication.class);

  @Test
  void verifiesModularStructure() {
    MODULES.verify();
  }

  @Test
  void exactlyFiveModulesAreDetected() {
    assertThat(MODULES.stream()).hasSize(5);
  }

  @ParameterizedTest
  @ValueSource(strings = {"plataforma", "identidad", "academico", "notassie", "shared"})
  void expectedModuleExists(String moduleName) {
    assertThat(MODULES.getModuleByName(moduleName)).isPresent();
  }
}
