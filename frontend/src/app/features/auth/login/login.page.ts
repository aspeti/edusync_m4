import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CardComponent } from '../../../shared/ui/card/card.component';
import { FormFieldComponent } from '../../../shared/ui/form-field/form-field.component';
import { ButtonComponent } from '../../../shared/ui/button/button.component';

/**
 * Pantalla de login. Mapea errores HTTP a mensajes de negocio:
 * - 401 → credenciales inválidas
 * - 403 E_TENANT_NO_ACTIVO → tenant suspendido/vencido
 * DD-UC-004 §2, DD-UC-006 §2 (redirect ADMIN → /usuarios), FSD-UC-021 (login UI).
 *
 * Al entrar limpia cualquier JWT residual en sessionStorage para que un token
 * expirado/inválido no se reenvíe en el POST de login.
 *
 * Rediseño visual DD-UC-020 (ADR-0015, Design System EduSync — PR-IMPL-020):
 * adopta `app-card`/`app-form-field`/`app-button` de `shared/ui/` y los
 * tokens de `styles/_tokens.scss` siguiendo el prototipo Figma (nodo
 * 399:20447). El submit sigue invocando exactamente el mismo
 * `AuthService.login()` de antes; no se tocó `core/auth/` ni el contrato
 * `POST /api/v1/auth/login`. "¿Olvidaste tu contraseña?" y "Solicitar
 * acceso institucional" son informativos (DD-UC-020 §1, fuera de alcance
 * su backend: no hay flujo de reset autoiniciado por email ni autoregistro
 * de tenants todavía).
 */
@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [FormsModule, CardComponent, FormFieldComponent, ButtonComponent],
  template: `
    <div class="login-page">
      <div class="login-page__container">
        <header class="login-page__brand">
          <div class="login-page__wordmark">EduSync</div>
          <div class="login-page__tagline">Portal académico</div>
        </header>

        <app-card>
          <h1 class="login-page__heading">Bienvenido de nuevo</h1>
          <p class="login-page__subheading">
            Ingresa tus credenciales institucionales para acceder a tu panel académico.
          </p>

          @if (errorMsg()) {
            <div class="login-page__error">{{ errorMsg() }}</div>
          }

          <form (ngSubmit)="onSubmit()" autocomplete="on">
            <app-form-field
              label="Correo electrónico"
              type="email"
              name="email"
              autocomplete="username"
              [required]="true"
              [value]="email"
              (valueChange)="email = $event"
            >
              <svg
                app-form-field-icon
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="5" width="18" height="14" rx="2" />
                <path d="M3 7l9 6 9-6" />
              </svg>
            </app-form-field>

            <app-form-field
              label="Contraseña"
              type="password"
              name="password"
              autocomplete="current-password"
              [required]="true"
              [value]="password"
              (valueChange)="password = $event"
            >
              <svg
                app-form-field-icon
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="5" y="11" width="14" height="9" rx="2" />
                <path d="M8 11V7a4 4 0 0 1 8 0v4" />
              </svg>
            </app-form-field>

            <app-button type="submit" [fullWidth]="true" [disabled]="loading()">
              {{ loading() ? 'Ingresando...' : 'Iniciar sesión' }}
            </app-button>
          </form>

          <div class="login-page__forgot">
            <button type="button" class="login-page__link" (click)="showForgotInfo.set(!showForgotInfo())">
              ¿Olvidaste tu contraseña?
            </button>
            @if (showForgotInfo()) {
              <p class="login-page__hint">
                Contacta a tu administrador institucional para restablecerla.
              </p>
            }
          </div>
        </app-card>

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

      .login-page__error {
        background: var(--color-danger-bg);
        color: var(--color-danger-text);
        padding: var(--space-sm);
        border-radius: var(--radius-sm);
        margin-bottom: var(--space-md);
        font-size: var(--font-size-small);
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
