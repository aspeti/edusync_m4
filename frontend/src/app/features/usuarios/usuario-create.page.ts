import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageModule } from 'primeng/message';
import { UsuarioResponse } from './usuario.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta de Usuario (DD-UC-006) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-usuario-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, PasswordModule, CheckboxModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nuevo Usuario</h2>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <form (ngSubmit)="onSubmit()" class="form">
        <label class="field" for="usr-nombre">
          Nombre completo
          <input pInputText id="usr-nombre" type="text" [(ngModel)]="nombreCompleto" name="nombreCompleto" required />
        </label>
        <label class="field" for="usr-email">
          Email
          <input pInputText id="usr-email" type="email" [(ngModel)]="email" name="email" required />
        </label>
        <label class="field" for="usr-password">
          Contraseña inicial
          <p-password
            inputId="usr-password"
            [(ngModel)]="passwordInicial"
            name="passwordInicial"
            [feedback]="false"
            [toggleMask]="true"
            styleClass="w-full"
            inputStyleClass="w-full"
          />
        </label>
        <div class="field">
          <span>Roles</span>
          <div class="roles">
            @for (rol of rolesDisponibles; track rol) {
              <div class="role-row">
                <p-checkbox
                  [binary]="true"
                  [ngModel]="roles.includes(rol)"
                  (ngModelChange)="onRolCheck(rol, $event)"
                  [name]="'rol-' + rol"
                  [inputId]="'rol-' + rol"
                />
                <label [for]="'rol-' + rol">{{ rol }}</label>
              </div>
            }
          </div>
        </div>
        <div class="actions">
          <p-button type="button" label="Cancelar" severity="secondary" [outlined]="true" (onClick)="volver()" />
          <p-button
            type="submit"
            [label]="loading() ? 'Creando...' : 'Crear Usuario'"
            [disabled]="loading() || roles.length === 0"
          />
        </div>
      </form>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 480px;
        margin: var(--space-lg) auto;
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
      .roles {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
      }
      .role-row {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
      }
      .actions {
        display: flex;
        gap: var(--space-sm);
      }
      :host ::ng-deep .w-full,
      :host ::ng-deep .p-password {
        width: 100%;
      }
    `,
  ],
})
export class UsuarioCreatePage {
  nombreCompleto = '';
  email = '';
  passwordInicial = '';
  roles: string[] = [];
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  readonly rolesDisponibles = ['ADMIN', 'SECRETARIA', 'ASESOR', 'PROFESOR'];

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  onRolCheck(rol: string, checked: boolean): void {
    this.roles = checked ? [...this.roles, rol] : this.roles.filter((r) => r !== rol);
  }

  onSubmit(): void {
    if (!this.nombreCompleto || !this.email || !this.passwordInicial || this.roles.length === 0) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post<UsuarioResponse>(`${ApiBase.BASE}/usuarios`, {
        nombreCompleto: this.nombreCompleto,
        email: this.email,
        passwordInicial: this.passwordInicial,
        roles: this.roles,
      })
      .subscribe({
        next: () => this.router.navigate(['/usuarios']),
        error: (err) => {
          this.loading.set(false);
          const codigo = err.error?.codigo;
          if (codigo === 'E_EMAIL_EN_USO') {
            this.errorMsg.set('El email ya está registrado en el sistema.');
          } else if (codigo === 'E_INVARIANTE_ROL_VIOLADA') {
            this.errorMsg.set('Combinación de roles no permitida.');
          } else if (err.status === 400) {
            this.errorMsg.set('Verifique los datos ingresados (email, contraseña mínima de 8 caracteres).');
          } else {
            this.errorMsg.set('Error al crear el usuario. Intente nuevamente.');
          }
        },
      });
  }

  volver(): void {
    this.router.navigate(['/usuarios']);
  }
}
