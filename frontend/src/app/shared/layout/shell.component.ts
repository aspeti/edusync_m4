import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';

/**
 * Shell mínimo post-login: barra de navegación + router outlet.
 * DD-UC-004 §2 (dentro del alcance: layout shell mínimo).
 */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <nav style="padding: 0.75rem 1rem; background: #1e3a5f; color: white; display: flex; gap: 1rem; align-items: center;">
      <strong>EduSync</strong>
      <a routerLink="/plataforma/tenants" style="color: white; text-decoration: none;">Tenants</a>
      <span style="flex: 1"></span>
      <button (click)="logout()" style="cursor: pointer; padding: 0.25rem 0.75rem;">Cerrar sesión</button>
    </nav>
    <main style="padding: 1rem;">
      <router-outlet />
    </main>
  `,
})
export class ShellComponent {
  constructor(private auth: AuthService, private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
