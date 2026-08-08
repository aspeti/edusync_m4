import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

/**
 * Pantalla de login. Mapea errores HTTP a mensajes de negocio:
 * - 401 → credenciales inválidas
 * - 403 E_TENANT_NO_ACTIVO → tenant suspendido/vencido
 * DD-UC-004 §2, DD-UC-006 §2 (redirect ADMIN → /usuarios), FSD-UC-021 (login UI).
 *
 * Al entrar limpia cualquier JWT residual en sessionStorage para que un token
 * expirado/inválido no se reenvíe en el POST de login.
 */
@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 360px; margin: 4rem auto; padding: 2rem; border: 1px solid #ddd; border-radius: 8px;">
      <h2 style="margin-bottom: 1.5rem; text-align: center;">EduSync — Iniciar sesión</h2>

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()" autocomplete="on">
        <div style="margin-bottom: 1rem;">
          <label>Email</label><br />
          <input
            type="email"
            [(ngModel)]="email"
            name="email"
            required
            autocomplete="username"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1.5rem;">
          <label>Contraseña</label><br />
          <input
            type="password"
            [(ngModel)]="password"
            name="password"
            required
            autocomplete="current-password"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <button type="submit" [disabled]="loading()" style="width: 100%; padding: 0.75rem; cursor: pointer;">
          {{ loading() ? 'Ingresando...' : 'Ingresar' }}
        </button>
      </form>
    </div>
  `,
})
export class LoginPage implements OnInit {
  email = '';
  password = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.auth.logout();
  }

  onSubmit(): void {
    if (!this.email || !this.password) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        const roles = this.auth.roles();
        if (roles.includes('SYSADMIN')) {
          this.router.navigate(['/plataforma/tenants']);
        } else if (roles.includes('ADMIN')) {
          this.router.navigate(['/usuarios']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 401) {
          const codigo = err.error?.codigo;
          if (codigo === 'E_TOKEN_INVALIDO') {
            this.errorMsg.set('Sesión anterior inválida. Intente ingresar de nuevo.');
          } else {
            this.errorMsg.set('Credenciales inválidas. Verifique su email y contraseña.');
          }
        } else if (err.status === 403) {
          const codigo = err.error?.codigo;
          if (codigo === 'E_TENANT_NO_ACTIVO') {
            this.errorMsg.set('Su institución está suspendida o vencida. Contacte al administrador de la plataforma.');
          } else {
            this.errorMsg.set('Acceso denegado.');
          }
        } else {
          this.errorMsg.set('Error inesperado. Intente nuevamente.');
        }
      },
      complete: () => this.loading.set(false),
    });
  }
}
