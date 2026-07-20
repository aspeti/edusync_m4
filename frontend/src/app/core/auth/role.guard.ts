import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Guard funcional de rol: recibe el rol requerido como dato de ruta.
 * Redirige a {@code /login} si no autenticado, o a {@code /home} si el rol no coincide.
 * DD-UC-004 §2.
 */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);

  const requiredRole: string = route.data['role'];
  if (auth.hasRole(requiredRole)) return true;

  return router.createUrlTree(['/home']);
};
