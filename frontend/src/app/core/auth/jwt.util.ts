/**
 * Decodifica el payload de un JWT sin verificar la firma.
 * La autorización real se aplica siempre en el backend (Spring Security).
 * Referencia: DD-UC-004 §2.
 */
export interface JwtPayload {
  sub: string;
  roles: string[];
  tenantId?: string;
  exp: number;
  iat: number;
}

/**
 * El backend emite {@code roles} como CSV ({@code "ADMIN,SECRETARIA"},
 * {@code JwtTokenProvider}); algunos tests usan array JSON. Normaliza ambos a
 * {@code string[]} para que {@code Array.includes} no caiga en el includes de
 * String (p. ej. {@code "SYSADMIN".includes("ADMIN") === true}).
 */
export function normalizeRoles(raw: unknown): string[] {
  if (Array.isArray(raw)) {
    return raw.map(String).map((r) => r.trim()).filter((r) => r.length > 0);
  }
  if (typeof raw === 'string') {
    return raw
      .split(',')
      .map((r) => r.trim())
      .filter((r) => r.length > 0);
  }
  return [];
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    // Padding base64url → base64 estándar
    const padded = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = atob(padded);
    const parsed = JSON.parse(decoded) as Omit<JwtPayload, 'roles'> & { roles?: unknown };
    return {
      ...parsed,
      roles: normalizeRoles(parsed.roles),
    };
  } catch {
    return null;
  }
}

export function isTokenExpired(payload: JwtPayload): boolean {
  return Date.now() >= payload.exp * 1000;
}
