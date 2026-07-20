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

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    // Padding base64url → base64 estándar
    const padded = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = atob(padded);
    return JSON.parse(decoded) as JwtPayload;
  } catch {
    return null;
  }
}

export function isTokenExpired(payload: JwtPayload): boolean {
  return Date.now() >= payload.exp * 1000;
}
