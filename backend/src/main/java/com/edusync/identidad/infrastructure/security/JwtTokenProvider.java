package com.edusync.identidad.infrastructure.security;

import com.edusync.identidad.application.port.in.TokenAcceso;
import com.edusync.identidad.application.port.out.TokenGeneradorPort;
import com.edusync.identidad.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Emision y validacion de JWT stateless HS256 (DD-UC-002 &sect;2, decisión: alternativa A
 * vs. RS256/sesion server-side). El secreto NUNCA se hardcodea: viene de
 * {@code edusync.security.jwt.secret} (env var {@code JWT_SECRET}).
 */
@Component
public class JwtTokenProvider implements TokenGeneradorPort {

  private static final String CLAIM_TENANT_ID = "tenantId";
  private static final String CLAIM_ROLES = "roles";
  private static final String TENANT_ID_AUSENTE = "";

  private final SecretKey key;
  private final long expirationSeconds;

  public JwtTokenProvider(
      @Value("${edusync.security.jwt.secret}") String secret,
      @Value("${edusync.security.jwt.expiration-seconds:28800}") long expirationSeconds) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
  }

  @Override
  public TokenAcceso generar(Usuario usuario) {
    Instant ahora = Instant.now();
    Instant expiracion = ahora.plusSeconds(expirationSeconds);
    String rolesCsv = usuario.getRoles().stream().map(Enum::name).collect(Collectors.joining(","));

    String token = Jwts.builder()
        .subject(usuario.getId().valor().toString())
        .claim(CLAIM_TENANT_ID, usuario.getTenantId() != null ? usuario.getTenantId().toString() : TENANT_ID_AUSENTE)
        .claim(CLAIM_ROLES, rolesCsv)
        .issuedAt(Date.from(ahora))
        .expiration(Date.from(expiracion))
        .signWith(key)
        .compact();

    return new TokenAcceso(token, expirationSeconds);
  }

  /**
   * @throws io.jsonwebtoken.JwtException si la firma es invalida o el token expiro
   */
  public ClaimsToken validar(String token) {
    Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    Claims claims = jws.getPayload();

    UUID userId = UUID.fromString(claims.getSubject());
    String tenantIdStr = claims.get(CLAIM_TENANT_ID, String.class);
    UUID tenantId = (tenantIdStr == null || tenantIdStr.isBlank()) ? null : UUID.fromString(tenantIdStr);
    Set<String> roles = Arrays.stream(claims.get(CLAIM_ROLES, String.class).split(","))
        .filter(rol -> !rol.isBlank())
        .collect(Collectors.toUnmodifiableSet());

    return new ClaimsToken(userId, tenantId, roles);
  }
}
