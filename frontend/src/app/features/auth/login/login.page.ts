import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../../core/auth/auth.service';

/**
 * Login UI (DD-UC-004/006/020) con PrimeNG + tokens ADR-0015/0026.
 * Contrato AuthService.login() sin cambio.
 */
@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [FormsModule, CardModule, InputTextModule, PasswordModule, ButtonModule, MessageModule],
  template: `
    <div class="login-page">
      <div class="login-page__container">
        <header class="login-page__brand">
          <div class="login-page__wordmark">EduSync</div>
          <div class="login-page__tagline">Portal académico</div>
        </header>

        <p-card styleClass="login-card">
          <h1 class="login-page__heading">Bienvenido de nuevo</h1>
          <p class="login-page__subheading">
            Ingresa tus credenciales institucionales para acceder a tu panel académico.
          </p>

          @if (errorMsg()) {
            <p-message
              severity="error"
              [text]="errorMsg()!"
              styleClass="block-msg"
              data-testid="login-error"
            />
          }

          <form (ngSubmit)="onSubmit()" autocomplete="on" class="login-form">
            <label class="field" for="login-email">
              Correo electrónico
              <input
                pInputText
                id="login-email"
                type="email"
                name="email"
                autocomplete="username"
                required
                [(ngModel)]="email"
              />
            </label>

            <label class="field" for="login-password">
              Contraseña
              <p-password
                inputId="login-password"
                name="password"
                [(ngModel)]="password"
                [feedback]="false"
                [toggleMask]="true"
                autocomplete="current-password"
                styleClass="w-full"
                inputStyleClass="w-full"
              />
            </label>

            <p-button
              type="submit"
              [label]="loading() ? 'Ingresando...' : 'Iniciar sesión'"
              [disabled]="loading()"
              styleClass="w-full"
            />
          </form>

          <div class="login-page__forgot">
            <button type="button" class="login-page__link" (click)="showForgotInfo.set(!showForgotInfo())">
              ¿Olvidaste tu contraseña?
            </button>
            @if (showForgotInfo()) {
              <p class="login-page__hint">Contacta a tu administrador institucional para restablecerla.</p>
            }
          </div>
        </p-card>

        <p class="login-page__signup">
          ¿Nuevo en la plataforma?
          <button type="button" class="login-page__link" (click)="showSignupInfo.set(!showSignupInfo())">
            Solicitar acceso institucional
          </button>
        </p>
        @if (showSignupInfo()) {
          <p class="login-page__hint login-page__hint--center">
            Contacta al equipo de EduSync para dar de alta tu institución.
          </p>
        }

        <footer class="login-page__footer">
          <p class="login-page__footer-brand">EduSync Academic.</p>
          <p class="login-page__footer-links">
            <span>Privacidad</span>
            <span aria-hidden="true">·</span>
            <span>Términos</span>
            <span aria-hidden="true">·</span>
            <span>Soporte</span>
            <span aria-hidden="true">·</span>
            <span>Contacto</span>
          </p>
        </footer>
      </div>
    </div>
  `,
  styles: [
    `
      .login-page {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--color-bg);
        padding: var(--space-lg);
        box-sizing: border-box;
        font-family: var(--font-family-base);
      }
      .login-page__container {
        width: 100%;
        max-width: 448px;
      }
      .login-page__brand {
        margin-bottom: var(--space-lg);
      }
      .login-page__wordmark {
        font-size: var(--font-size-h1);
        font-weight: 700;
        color: var(--color-primary);
      }
      .login-page__tagline {
        font-size: var(--font-size-label);
        font-weight: var(--font-weight-label);
        letter-spacing: var(--letter-spacing-label);
        text-transform: uppercase;
        color: var(--color-text-secondary);
        margin-top: var(--space-xs);
      }
      .login-page__heading {
        margin: 0 0 var(--space-xs);
        font-size: var(--font-size-h1);
        color: var(--color-text);
      }
      .login-page__subheading {
        margin: 0 0 var(--space-lg);
        font-size: var(--font-size-body);
        color: var(--color-text-secondary);
      }
      .login-form {
        display: flex;
        flex-direction: column;
        gap: var(--space-md);
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
        font-size: var(--font-size-label);
        font-weight: var(--font-weight-label);
        color: var(--color-text-secondary);
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .login-page__forgot {
        margin-top: var(--space-md);
        text-align: center;
      }
      .login-page__link {
        background: none;
        border: none;
        padding: 0;
        color: var(--color-primary);
        font-size: var(--font-size-small);
        font-family: var(--font-family-base);
        cursor: pointer;
        text-decoration: underline;
      }
      .login-page__hint {
        margin: var(--space-xs) 0 0;
        font-size: var(--font-size-small);
        color: var(--color-text-secondary);
      }
      .login-page__hint--center {
        text-align: center;
      }
      .login-page__signup {
        margin: var(--space-lg) 0 0;
        text-align: center;
        font-size: var(--font-size-small);
        color: var(--color-text-secondary);
      }
      .login-page__footer {
        margin-top: var(--space-xl);
        text-align: center;
        color: var(--color-text-secondary);
        font-size: var(--font-size-small);
      }
      .login-page__footer-brand {
        margin: 0 0 var(--space-xs);
      }
      .login-page__footer-links {
        margin: 0;
        display: flex;
        gap: var(--space-xs);
        justify-content: center;
        flex-wrap: wrap;
      }
      :host ::ng-deep .login-card {
        box-shadow: var(--shadow-card);
        border-radius: var(--radius-lg);
      }
      :host ::ng-deep .w-full {
        width: 100%;
      }
      :host ::ng-deep .p-password {
        width: 100%;
      }
    `,
  ],
})
export class LoginPage implements OnInit {
  email = '';
  password = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  showForgotInfo = signal(false);
  showSignupInfo = signal(false);

  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}

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
        } else if (roles.includes('PROFESOR')) {
          this.router.navigate(['/academico/mis-materias']);
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
            this.errorMsg.set(
              'Su institución está suspendida o vencida. Contacte al administrador de la plataforma.',
            );
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
