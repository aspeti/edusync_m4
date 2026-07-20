package com.edusync.identidad.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT stateless (sin sesion de servidor, sin CSRF): el unico endpoint publico es el login;
 * todo lo demas requiere {@code Authorization: Bearer &lt;token&gt;} (DD-UC-002 &sect;2).
 *
 * <p>Swagger UI/OpenAPI ({@code /swagger-ui.html}, {@code /v3/api-docs/**}) tambien se
 * deja publico en los perfiles vigentes (`dev`/`test`) — ver la nota de pendiente de
 * revision en {@code OpenApiConfig} y {@code ADR-0012} &sect;3.
 *
 * <p>{@code @EnableMethodSecurity} habilita {@code @PreAuthorize("hasRole('SYSADMIN')")}
 * en {@code plataforma.TenantController} ({@code DD-UC-003}, {@code ADR-0010}).
 *
 * <p>Sin {@link UserDetailsService} propio, Boot registra un usuario in-memory por defecto
 * y {@code TestRestTemplate} envia Basic Auth automatico: las peticiones "sin token" llegan
 * autenticadas y {@code @PreAuthorize} responde 403 en lugar de 401. El bean vacio +
 * {@code HttpStatusEntryPoint(UNAUTHORIZED)} mantienen el contrato REST 401/403.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/login").permitAll()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            // Boot reenvia 4xx/5xx a /error; si exige auth, TestRestTemplate/clientes
            // ven 401 en lugar del 403 original de @PreAuthorize.
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Sustituye el {@code UserDetailsService} in-memory de Boot (usuario {@code user}/password
   * aleatorio). EduSync autentica solo por JWT; no hay usuarios de servlet-container.
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
