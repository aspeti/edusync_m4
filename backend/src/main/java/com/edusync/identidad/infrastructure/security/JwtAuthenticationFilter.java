package com.edusync.identidad.infrastructure.security;

import com.edusync.shared.tenant.TenantContextProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida el JWT de cada request, puebla el {@code SecurityContext} (RBAC) y activa el
 * {@link TenantContextProvider} (RLS) con el {@code tenantId} del claim. Se limpia el
 * tenant en el {@code finally} porque Tomcat reutiliza hilos entre requests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final TenantContextProvider tenantContextProvider;

  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider, TenantContextProvider tenantContextProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.tenantContextProvider = tenantContextProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String header = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (header != null && header.startsWith(BEARER_PREFIX)) {
        String token = header.substring(BEARER_PREFIX.length());
        ClaimsToken claims = jwtTokenProvider.validar(token);
        tenantContextProvider.activar(claims.tenantId());
        SecurityContextHolder.getContext().setAuthentication(autenticacionDe(claims));
      }
      filterChain.doFilter(request, response);
    } catch (JwtException | IllegalArgumentException ex) {
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"codigo\":\"E_TOKEN_INVALIDO\"}");
    } finally {
      tenantContextProvider.limpiar();
    }
  }

  private UsernamePasswordAuthenticationToken autenticacionDe(ClaimsToken claims) {
    List<GrantedAuthority> authorities = claims.roles().stream()
        .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
        .collect(Collectors.toList());
    return new UsernamePasswordAuthenticationToken(claims.userId(), null, authorities);
  }
}
