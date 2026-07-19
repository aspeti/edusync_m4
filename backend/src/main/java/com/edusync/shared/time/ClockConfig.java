package com.edusync.shared.time;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expone un {@link Clock} inyectable en vez de llamadas estaticas a {@code LocalDate.now()}
 * dispersas por el codigo, para que los jobs con logica sensible a la fecha (p. ej.
 * {@code plataforma.VencimientoSchedulerService}, {@code DD-UC-003} &sect;6) sean
 * testeables con un reloj simulado.
 */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
