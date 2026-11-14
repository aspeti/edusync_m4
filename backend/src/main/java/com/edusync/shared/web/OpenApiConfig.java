package com.edusync.shared.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
            .description(
                "Plataforma SaaS multi-tenant de gestión académica EduSync — monolito modular"
                    + " (ADR-0011). Contrato generado desde el código con springdoc y versionado"
                    + " en openapi.json (release 8.2.0).")
            .version("8.2.0")
            .contact(new Contact().name("G-EduSync").url("https://github.com/aspeti/edusync_m4"))
            .license(new License().name("Uso académico — Maestría en Ingeniería de Sistemas")))
        // URL fija: sin esto springdoc usa el host/puerto de la petición y el export no es
        // determinista (drift falso en CI).
        .servers(List.of(new Server().url("http://localhost:8080").description("Desarrollo local")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(new io.swagger.v3.oas.models.Components()
            .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                // Sin .name(): "name" solo es válido para apiKey; en type=http rompe el esquema OAS.
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }

  /**
   * Cada controlador declara {@code @Tag(name = "Academico", ...)} y springdoc emite una entrada
   * por clase, lo que duplica nombres (regla {@code openapi-tags-uniqueness}). Se deja una sola
   * entrada por nombre; si había descripciones distintas se omite la descripción.
   */
  @Bean
  public OpenApiCustomizer tagsUnicos() {
    return openApi -> {
      if (openApi.getTags() == null) {
        return;
      }
      Map<String, Integer> repeticiones = new LinkedHashMap<>();
      Map<String, Tag> primeras = new LinkedHashMap<>();
      for (Tag tag : openApi.getTags()) {
        repeticiones.merge(tag.getName(), 1, Integer::sum);
        primeras.putIfAbsent(tag.getName(), tag);
      }
      List<Tag> unicas = new ArrayList<>();
      primeras.forEach(
          (nombre, tag) -> {
            if (repeticiones.get(nombre) > 1) {
              unicas.add(new Tag().name(nombre));
            } else {
              unicas.add(tag);
            }
          });
      openApi.setTags(unicas);
    };
  }
}
