import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Guard funcional de rol. Acepta `data.role` (string, rutas existentes) o
 * `data.roles` (string[], DD-UC-012): basta UN rol coincidente. La ampliación
 * es aditiva — las rutas con `data.role` no se tocan.
 * Redirige a `/login` si no autenticado, o a `/home` si el rol no coincide.
 */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);

  const requiredRoles: string[] | undefined = route.data['roles'];
  if (Array.isArray(requiredRoles) && requiredRoles.length > 0) {
    if (requiredRoles.some((role) => auth.hasRole(role))) return true;
    return router.createUrlTree(['/home']);
  }

  const requiredRole: string = route.data['role'];
  if (auth.hasRole(requiredRole)) return true;

  return router.createUrlTree(['/home']);
};
