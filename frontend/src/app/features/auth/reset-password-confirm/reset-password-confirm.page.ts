import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiBase } from '../../../core/api/api-base';

/**
 * Pantalla pública (sin sesión) para completar el restablecimiento de
 * contraseña. POST /api/v1/auth/restablecer-password/confirmar (DD-UC-005).
 * DD-UC-006 §2.
 */
@Component({
  selector: 'app-reset-password-confirm-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div style="max-width: 400px; margin: 4rem auto; padding: 2rem; border: 1px solid #ddd; border-radius: 8px;">
      <h2 style="margin-bottom: 1rem; text-align: center;">Restablecer contraseña</h2>

      @if (successMsg()) {
        <div style="background: #e8f5e9; color: #2e7d32; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ successMsg() }}
        </div>
        <a routerLink="/login" style="display:block;text-align:center;">Ir a iniciar sesión</a>
      }

      @if (!successMsg()) {
        <p style="color: #555; font-size: 0.9rem;">
          Pegue el token recibido y elija una nueva contraseña.
        </p>

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        <form (ngSubmit)="onSubmit()">
          <div style="margin-bottom: 1rem;">
            <label>Token</label><br />
            <input
              type="text"
              [(ngModel)]="token"
              name="token"
              required
              style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
            />
          </div>
          <div style="margin-bottom: 1.5rem;">
            <label>Nueva contraseña</label><br />
            <input
              type="password"
              [(ngModel)]="passwordNuevo"
              name="passwordNuevo"
              required
              minlength="8"
              style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
            />
          </div>
          <button type="submit" [disabled]="loading()" style="width: 100%; padding: 0.75rem; cursor: pointer;">
            {{ loading() ? 'Confirmando...' : 'Confirmar' }}
          </button>
        </form>
      }
    </div>
  `,
})
export class ResetPasswordConfirmPage {
  token = '';
  passwordNuevo = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  successMsg = signal<string | null>(null);

  constructor(private http: HttpClient, private route: ActivatedRoute) {
    const tokenParam = this.route.snapshot.queryParamMap.get('token');
    if (tokenParam) {
      this.token = tokenParam;
    }
  }

  onSubmit(): void {
    if (!this.token || !this.passwordNuevo) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post(`${ApiBase.BASE}/auth/restablecer-password/confirmar`, {
        token: this.token,
        passwordNuevo: this.passwordNuevo,
      })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.successMsg.set('Contraseña actualizada correctamente.');
        },
        error: (err) => {
          this.loading.set(false);
          if (err.status === 410) {
            this.errorMsg.set('El enlace ya fue usado o expiró. Solicite uno nuevo al administrador.');
          } else if (err.status === 400) {
            this.errorMsg.set('Verifique el token y que la contraseña tenga al menos 8 caracteres.');
          } else {
            this.errorMsg.set('Error inesperado. Intente nuevamente.');
          }
        },
      });
  }
}
