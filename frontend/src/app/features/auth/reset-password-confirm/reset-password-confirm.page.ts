import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ApiBase } from '../../../core/api/api-base';

/** Confirmación pública de reset password (DD-UC-006) — UI PrimeNG. */
@Component({
  selector: 'app-reset-password-confirm-page',
  standalone: true,
  imports: [FormsModule, CardModule, InputTextModule, PasswordModule, ButtonModule, MessageModule],
  template: `
    <div class="page">
      <p-card>
        <h2>Restablecer contraseña</h2>

        @if (successMsg()) {
          <p-message severity="success" [text]="successMsg()!" styleClass="block-msg" />
          <p-button label="Ir a iniciar sesión" (onClick)="irLogin()" styleClass="w-full" />
        }

        @if (!successMsg()) {
          <p class="hint">Pegue el token recibido y elija una nueva contraseña.</p>

          @if (errorMsg()) {
            <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
          }

          <form (ngSubmit)="onSubmit()" class="form">
            <label class="field" for="reset-token">
              Token
              <input pInputText id="reset-token" type="text" [(ngModel)]="token" name="token" required />
            </label>
            <label class="field" for="reset-password">
              Nueva contraseña
              <p-password
                inputId="reset-password"
                [(ngModel)]="passwordNuevo"
                name="passwordNuevo"
                [feedback]="false"
                [toggleMask]="true"
                styleClass="w-full"
                inputStyleClass="w-full"
              />
            </label>
            <p-button
              type="submit"
              [label]="loading() ? 'Confirmando...' : 'Confirmar'"
              [disabled]="loading()"
              styleClass="w-full"
            />
          </form>
        }
      </p-card>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 400px;
        margin: var(--space-xl) auto;
      }
      h2 {
        margin-top: 0;
        text-align: center;
      }
      .hint {
        color: var(--color-text-secondary);
        font-size: var(--font-size-small);
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .form {
        display: flex;
        flex-direction: column;
        gap: var(--space-md);
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
        font-size: var(--font-size-small);
      }
      :host ::ng-deep .w-full,
      :host ::ng-deep .p-password {
        width: 100%;
      }
    `,
  ],
})
export class ResetPasswordConfirmPage {
  token = '';
  passwordNuevo = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  successMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
  ) {
    const tokenParam = this.route.snapshot.queryParamMap.get('token');
    if (tokenParam) this.token = tokenParam;
  }

  irLogin(): void {
    this.router.navigate(['/login']);
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
