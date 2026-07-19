package com.edusync.shared.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion viva de la API REST (springdoc-openapi, ADR-0012). Declara el esquema de
 * seguridad Bearer JWT una sola vez (visible en Swagger UI como "Authorize"); cada
 * controlador se documenta con {@code @Tag}/{@code @Operation} en su propio modulo.
 *
 * <p><b>Pendiente de revision</b> (documentado en {@code ADR-0012} &sect;3): hoy Swagger
 * UI (`/swagger-ui.html`) y `/v3/api-docs/**` son publicos en todos los perfiles vigentes
 * (`dev`/`test`, ver {@code SecurityConfig}). Cuando exista un perfil de produccion real,
 * <b>MUST</b> revisarse si se restringe (autenticacion + rol {@code SYSADMIN}) o se
 * deshabilita con {@code springdoc.swagger-ui.enabled=false}.
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI edusyncOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("EduSync API")
            .description("Plataforma SaaS multi-tenant EduSync — monolito modular (ADR-0011)")
            .version("v0.1"))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(new io.swagger.v3.oas.models.Components()
            .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }
}
