import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

/**
 * Rutas de la SPA Angular 21 de EduSync (DD-UC-004 §2):
 * - /login                        → pública
 * - /plataforma/**                → authGuard + roleGuard(SYSADMIN)
 * - /home                         → authGuard (placeholder para otros roles)
 * - /                             → redirect a /login
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/layout/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: 'plataforma/tenants/nuevo',
        canActivate: [roleGuard],
        data: { role: 'SYSADMIN' },
        loadComponent: () =>
          import('./features/plataforma/tenant-create.page').then((m) => m.TenantCreatePage),
      },
      {
        path: 'plataforma/tenants/:id/admin',
        canActivate: [roleGuard],
        data: { role: 'SYSADMIN' },
        loadComponent: () =>
          import('./features/plataforma/tenant-admin-create.page').then((m) => m.TenantAdminCreatePage),
      },
      {
        path: 'plataforma/tenants',
        canActivate: [roleGuard],
        data: { role: 'SYSADMIN' },
        loadComponent: () =>
          import('./features/plataforma/tenants-list.page').then((m) => m.TenantsListPage),
      },
      {
        path: 'home',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/home/home.page').then((m) => m.HomePage),
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
