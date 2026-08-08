import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { UsuarioResponse } from './usuario.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Alta de un usuario del tenant (DD-UC-006 §2).
 * POST /api/v1/usuarios → 201 UsuarioResponse (DD-UC-005).
 * No pide curso/paralelo para ASESOR: el backend no valida esa referencia
 * todavía (DD-UC-005 §1, E_ASESOR_SIN_CURSO diferido).
 */
@Component({
  selector: 'app-usuario-create-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 480px; margin: 2rem auto;">
      <h2>Nuevo Usuario</h2>

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()">
        <div style="margin-bottom: 1rem;">
          <label>Nombre completo</label><br />
          <input
            type="text"
            [(ngModel)]="nombreCompleto"
            name="nombreCompleto"
            required
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1rem;">
          <label>Email</label><br />
          <input
            type="email"
            [(ngModel)]="email"
            name="email"
            required
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1rem;">
          <label>Contraseña inicial</label><br />
          <input
            type="password"
            [(ngModel)]="passwordInicial"
            name="passwordInicial"
            required
            minlength="8"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1.5rem;">
          <label>Roles</label>
          <div style="display:flex;flex-direction:column;gap:0.35rem;margin-top:0.35rem;">
            @for (rol of rolesDisponibles; track rol) {
              <label style="cursor:pointer;">
                <input type="checkbox" [checked]="roles.includes(rol)" (change)="toggleRol(rol)" />
                {{ rol }}
              </label>
            }
          </div>
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <button type="button" (click)="volver()" style="flex: 1; padding: 0.75rem; cursor: pointer;">
            Cancelar
          </button>
          <button
            type="submit"
            [disabled]="loading() || roles.length === 0"
            style="flex: 2; padding: 0.75rem; background: #1e3a5f; color: white; cursor: pointer; border: none; border-radius: 4px;"
          >
            {{ loading() ? 'Creando...' : 'Crear Usuario' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class UsuarioCreatePage {
  nombreCompleto = '';
  email = '';
  passwordInicial = '';
  roles: string[] = [];
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  /** SYSADMIN nunca es una opción aquí (ADR-0010): esta pantalla solo crea usuarios de tenant. */
  readonly rolesDisponibles = ['ADMIN', 'SECRETARIA', 'ASESOR', 'PROFESOR'];

  constructor(private http: HttpClient, private router: Router) {}

  toggleRol(rol: string): void {
    this.roles = this.roles.includes(rol) ? this.roles.filter((r) => r !== rol) : [...this.roles, rol];
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
        next: () => {
          this.router.navigate(['/usuarios']);
        },
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
