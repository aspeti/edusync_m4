import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Endpoints públicos: NUNCA enviar Bearer. Un JWT residual/expirado en
 * {@code sessionStorage} haría que {@code JwtAuthenticationFilter} responda
 * 401 {@code E_TOKEN_INVALIDO} antes de procesar el body del login.
 */
const PUBLIC_API_PATHS = [
  '/api/v1/auth/login',
  '/api/v1/auth/restablecer-password/confirmar',
];

/**
 * Interceptor funcional que añade el header {@code Authorization: Bearer <token>}
 * a las peticiones autenticadas. Omite rutas públicas de auth.
 * DD-UC-004 §2.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (PUBLIC_API_PATHS.some((path) => req.url.includes(path))) {
    return next(req);
  }

  const token = inject(AuthService).getToken();
  if (!token) return next(req);

  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });
  return next(authReq);
};
