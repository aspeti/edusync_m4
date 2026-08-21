import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

/**
 * Rutas de la SPA Angular 21 de EduSync (DD-UC-004 §2 + DD-UC-006 §2 + DD-UC-009 §2
 * + DD-UC-011 §2):
 * - /login                        → pública
 * - /restablecer-password         → pública (sin sesión, DD-UC-006)
 * - /plataforma/**                → authGuard + roleGuard(SYSADMIN)
 * - /usuarios/**                  → authGuard + roleGuard(ADMIN)
 * - /academico/gestiones-escolares/** → authGuard + roleGuard(ADMIN) (DD-UC-009)
 * - /academico/cursos/**          → authGuard + roleGuard(ADMIN) (DD-UC-011)
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
    path: 'restablecer-password',
    loadComponent: () =>
      import('./features/auth/reset-password-confirm/reset-password-confirm.page').then(
        (m) => m.ResetPasswordConfirmPage
      ),
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
        path: 'usuarios/nuevo',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/usuarios/usuario-create.page').then((m) => m.UsuarioCreatePage),
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/usuarios/usuarios-list.page').then((m) => m.UsuariosListPage),
      },
      {
        path: 'academico/gestiones-escolares/nuevo',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/academico/gestion-escolar-create.page').then(
            (m) => m.GestionEscolarCreatePage
          ),
      },
      {
        path: 'academico/gestiones-escolares',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/academico/gestiones-escolares-list.page').then(
            (m) => m.GestionesEscolaresListPage
          ),
      },
      {
        path: 'academico/cursos/nuevo',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/academico/curso-create.page').then((m) => m.CursoCreatePage),
      },
      {
        path: 'academico/cursos/:id/paralelos',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/academico/curso-paralelos.page').then((m) => m.CursoParalelosPage),
      },
      {
        path: 'academico/cursos',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        loadComponent: () =>
          import('./features/academico/cursos-list.page').then((m) => m.CursosListPage),
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
