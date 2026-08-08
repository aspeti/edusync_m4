import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UsuarioResponse } from './usuario.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Página de lista de Usuarios — consola Admin del tenant.
 * GET /api/v1/usuarios (DD-UC-006 §2). Mismo patrón sin design system de
 * TenantsListPage (DD-UC-004).
 */
@Component({
  selector: 'app-usuarios-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 900px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Usuarios</h2>
        <a routerLink="/usuarios/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
          + Nuevo Usuario
        </a>
      </div>

      @if (loading()) {
        <p>Cargando usuarios...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (resetInfoMsg()) {
        <div style="background: #e3f2fd; color: #1565c0; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ resetInfoMsg() }}
        </div>
      }

      @if (!loading() && usuarios().length === 0 && !errorMsg()) {
        <p>No hay usuarios registrados aún.</p>
      }

      @if (usuarios().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Email</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Roles</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (usuario of usuarios(); track usuario.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ usuario.nombreCompleto }}</td>
                <td style="padding: 0.5rem;">{{ usuario.email }}</td>
                <td style="padding: 0.5rem;">{{ usuario.roles.join(', ') }}</td>
                <td style="padding: 0.5rem;">
                  <span [style.color]="usuario.activo ? '#2e7d32' : '#757575'">
                    {{ usuario.activo ? 'ACTIVO' : 'INACTIVO' }}
                  </span>
                </td>
                <td style="padding: 0.5rem; display: flex; gap: 0.5rem; flex-wrap: wrap;">
                  <button (click)="abrirRolesDialog(usuario)" style="cursor: pointer; font-size: 0.85rem;">
                    Editar roles
                  </button>
                  <button (click)="toggleEstado(usuario)" style="cursor: pointer; font-size: 0.85rem;">
                    {{ usuario.activo ? 'Desactivar' : 'Activar' }}
                  </button>
                  <button (click)="iniciarReset(usuario)" [disabled]="reseteando() === usuario.id" style="cursor: pointer; font-size: 0.85rem;">
                    {{ reseteando() === usuario.id ? 'Enviando...' : 'Restablecer password' }}
                  </button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }

      @if (rolesDialog()) {
        <div style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.4);display:flex;align-items:center;justify-content:center;">
          <div style="background:white;padding:2rem;border-radius:8px;min-width:320px;">
            <h3>Editar roles de "{{ rolesDialog()!.nombreCompleto }}"</h3>
            <div style="display:flex;flex-direction:column;gap:0.5rem;margin:1rem 0;">
              @for (rol of rolesDisponibles; track rol) {
                <label style="cursor:pointer;">
                  <input
                    type="checkbox"
                    [checked]="rolesSeleccionados.includes(rol)"
                    (change)="toggleRolSeleccionado(rol)"
                  />
                  {{ rol }}
                </label>
              }
            </div>
            @if (rolesError()) {
              <p style="color:#c62828;">{{ rolesError() }}</p>
            }
            <div style="display:flex;gap:0.5rem;justify-content:flex-end;">
              <button (click)="cerrarRolesDialog()">Cancelar</button>
              <button
                (click)="confirmarRoles()"
                [disabled]="rolesSeleccionados.length === 0 || rolesSaving()"
                style="background:#1e3a5f;color:white;padding:0.4rem 1rem;cursor:pointer;"
              >
                {{ rolesSaving() ? 'Guardando...' : 'Guardar' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class UsuariosListPage implements OnInit {
  usuarios = signal<UsuarioResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  resetInfoMsg = signal<string | null>(null);
  reseteando = signal<string | null>(null);

  rolesDialog = signal<UsuarioResponse | null>(null);
  rolesSeleccionados: string[] = [];
  rolesError = signal<string | null>(null);
  rolesSaving = signal(false);

  /** SYSADMIN nunca es una opción aquí (ADR-0010): este formulario solo gestiona roles de tenant. */
  readonly rolesDisponibles = ['ADMIN', 'SECRETARIA', 'ASESOR', 'PROFESOR'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.http.get<UsuarioResponse[]>(`${ApiBase.BASE}/usuarios`).subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los usuarios.');
        this.loading.set(false);
      },
    });
  }

  abrirRolesDialog(usuario: UsuarioResponse): void {
    this.rolesSeleccionados = [...usuario.roles];
    this.rolesError.set(null);
    this.rolesDialog.set(usuario);
  }

  toggleRolSeleccionado(rol: string): void {
    this.rolesSeleccionados = this.rolesSeleccionados.includes(rol)
      ? this.rolesSeleccionados.filter((r) => r !== rol)
      : [...this.rolesSeleccionados, rol];
  }

  cerrarRolesDialog(): void {
    this.rolesDialog.set(null);
    this.rolesSeleccionados = [];
  }

  confirmarRoles(): void {
    const usuario = this.rolesDialog();
    if (!usuario || this.rolesSeleccionados.length === 0) return;
    this.rolesSaving.set(true);
    this.rolesError.set(null);

    this.http
      .patch<UsuarioResponse>(`${ApiBase.BASE}/usuarios/${usuario.id}/roles`, {
        roles: this.rolesSeleccionados,
      })
      .subscribe({
        next: (updated) => {
          this.usuarios.update((list) => list.map((u) => (u.id === updated.id ? updated : u)));
          this.rolesSaving.set(false);
          this.cerrarRolesDialog();
        },
        error: (err) => {
          this.rolesSaving.set(false);
          this.rolesError.set(
            err.error?.codigo === 'E_INVARIANTE_ROL_VIOLADA'
              ? 'Combinación de roles no permitida.'
              : 'Error al actualizar los roles.'
          );
        },
      });
  }

  toggleEstado(usuario: UsuarioResponse): void {
    this.http
      .patch<UsuarioResponse>(`${ApiBase.BASE}/usuarios/${usuario.id}/estado`, {
        activo: !usuario.activo,
      })
      .subscribe({
        next: (updated) => {
          this.usuarios.update((list) => list.map((u) => (u.id === updated.id ? updated : u)));
        },
        error: () => {
          this.errorMsg.set('Error al cambiar el estado del usuario.');
        },
      });
  }

  /**
   * Restablecimiento de contraseña (DD-UC-005 §1, placeholder log-only): la entrega real
   * de email no está implementada todavía, así que el mensaje es transparente sobre esa
   * limitación en vez de simular un envío que no ocurre (DD-UC-006 §2/§3).
   */
  iniciarReset(usuario: UsuarioResponse): void {
    this.reseteando.set(usuario.id);
    this.resetInfoMsg.set(null);
    this.http.post(`${ApiBase.BASE}/usuarios/${usuario.id}/restablecer-password`, {}).subscribe({
      next: () => {
        this.resetInfoMsg.set(
          `Restablecimiento iniciado para "${usuario.nombreCompleto}". El envío de correo real aún no está implementado (ver DD-UC-005 §1); el token queda registrado en los logs del servidor para completar el flujo manualmente en /restablecer-password.`
        );
        this.reseteando.set(null);
      },
      error: () => {
        this.errorMsg.set('Error al iniciar el restablecimiento de contraseña.');
        this.reseteando.set(null);
      },
    });
  }
}
