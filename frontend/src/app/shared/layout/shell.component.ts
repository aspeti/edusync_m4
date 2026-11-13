import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Shell post-login: menú por rol + outlet.
 * Migra el nav inline (DD-UC-004..024) a PrimeNG Menubar + tokens ADR-0015 / ADR-0026.
 * Visibilidad de enlaces sin cambio funcional respecto al shell previo.
 */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MenubarModule, ButtonModule],
  template: `
    <header class="shell-header">
      <p-menubar [model]="navItems" styleClass="shell-menubar">
        <ng-template #start>
          <a routerLink="/" class="brand">EduSync</a>
        </ng-template>
        <ng-template #end>
          <p-button
            type="button"
            label="Cerrar sesión"
            severity="secondary"
            [text]="true"
            styleClass="logout-btn"
            (onClick)="logout()"
          />
        </ng-template>
      </p-menubar>
    </header>
    <main class="shell-main">
      <router-outlet />
    </main>
  `,
  styles: [
    `
      .shell-header {
        background: var(--color-primary-600);
        color: var(--color-surface);
      }
      .brand {
        color: var(--color-surface);
        font-weight: 700;
        text-decoration: none;
        margin-right: var(--space-md);
        font-size: var(--font-size-body);
      }
      .shell-main {
        padding: var(--space-md);
        background: var(--color-bg);
        min-height: calc(100vh - 4rem);
      }
      :host ::ng-deep .shell-menubar {
        background: transparent;
        border: none;
        border-radius: 0;
        color: var(--color-surface);
      }
      :host ::ng-deep .shell-menubar .p-menubar-item-link {
        color: var(--color-surface);
      }
      :host ::ng-deep .logout-btn {
        color: var(--color-surface) !important;
      }
    `,
  ],
})
export class ShellComponent {
  /** Construido una vez al crear el shell (post-login); roles no cambian en sesión. */
  readonly navItems: MenuItem[];

  constructor(
    protected auth: AuthService,
    private router: Router,
  ) {
    this.navItems = this.buildNavItems();
  }

  private buildNavItems(): MenuItem[] {
    const links: MenuItem[] = [];
    if (this.auth.hasRole('SYSADMIN')) {
      links.push({ label: 'Tenants', routerLink: '/plataforma/tenants' });
    }
    if (this.auth.hasRole('ADMIN')) {
      links.push(
        { label: 'Usuarios', routerLink: '/usuarios' },
        { label: 'Cursos', routerLink: '/academico/cursos' },
        { label: 'Gestión Escolar', routerLink: '/academico/gestiones-escolares' },
      );
    }
    if (this.auth.hasRole('ADMIN') || this.auth.hasRole('SECRETARIA')) {
      links.push(
        { label: 'Materias', routerLink: '/academico/materias' },
        { label: 'Estudiantes', routerLink: '/academico/estudiantes' },
        { label: 'Profesores', routerLink: '/academico/profesores' },
      );
    }
    if (this.auth.hasRole('PROFESOR')) {
      links.push(
        { label: 'Mis cursos', routerLink: '/academico/mis-cursos' },
        { label: 'Mis materias', routerLink: '/academico/mis-materias' },
      );
    }
    if (
      this.auth.hasRole('ADMIN') ||
      this.auth.hasRole('SECRETARIA') ||
      this.auth.hasRole('PROFESOR')
    ) {
      links.push({ label: 'Asistente', routerLink: '/asistente' });
    }
    return links;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
