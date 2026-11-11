import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule, TablePageEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { CheckboxModule } from 'primeng/checkbox';
import { UsuarioResponse } from './usuario.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Lista de Usuarios (DD-UC-006/007) — UI PrimeNG (ADR-0026): p-table lazy,
 * filtros, diálogo de roles. Contratos HTTP sin cambio.
 */
@Component({
  selector: 'app-usuarios-list-page',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    SelectModule,
    DialogModule,
    TagModule,
    MessageModule,
    CheckboxModule,
  ],
  template: `
    <div class="page">
      <div class="toolbar">
        <h2 data-testid="usuarios-heading">Usuarios</h2>
        <p-button label="+ Nuevo Usuario" (onClick)="irNuevo()" />
      </div>

      <div class="filters">
        <input
          pInputText
          type="text"
          placeholder="Buscar por nombre o email..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          class="filter-q"
        />
        <p-select
          [options]="rolOptions"
          [(ngModel)]="filtroRol"
          optionLabel="label"
          optionValue="value"
          placeholder="Todos los roles"
          [showClear]="true"
          styleClass="filter-select"
        />
        <p-select
          [options]="activoOptions"
          [(ngModel)]="filtroActivo"
          optionLabel="label"
          optionValue="value"
          placeholder="Todos los estados"
          [showClear]="true"
          styleClass="filter-select"
        />
        <p-button label="Buscar" (onClick)="aplicarFiltros()" />
        <p-button label="Limpiar" severity="secondary" [outlined]="true" (onClick)="limpiarFiltros()" />
      </div>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }
      @if (resetInfoMsg()) {
        <p-message severity="info" [text]="resetInfoMsg()!" styleClass="block-msg" />
      }

      <p-table
        [value]="usuarios()"
        dataKey="id"
        [lazy]="true"
        [paginator]="true"
        [rows]="tamanoPagina"
        [totalRecords]="totalElements()"
        [first]="page() * tamanoPagina"
        [loading]="loading()"
        (onPage)="onPage($event)"
        [tableStyle]="{ 'min-width': '50rem' }"
        styleClass="p-datatable-sm"
      >
        <ng-template #header>
          <tr>
            <th>Nombre</th>
            <th>Email</th>
            <th>Roles</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-usuario>
          <tr>
            <td>{{ usuario.nombreCompleto }}</td>
            <td>{{ usuario.email }}</td>
            <td>{{ usuario.roles.join(', ') }}</td>
            <td>
              <p-tag
                [value]="usuario.activo ? 'ACTIVO' : 'INACTIVO'"
                [severity]="usuario.activo ? 'success' : 'secondary'"
              />
            </td>
            <td class="actions">
              <p-button label="Editar roles" [link]="true" size="small" (onClick)="abrirRolesDialog(usuario)" />
              <p-button
                [label]="usuario.activo ? 'Desactivar' : 'Activar'"
                [link]="true"
                size="small"
                (onClick)="toggleEstado(usuario)"
              />
              <p-button
                [label]="reseteando() === usuario.id ? 'Enviando...' : 'Restablecer password'"
                [link]="true"
                size="small"
                [disabled]="reseteando() === usuario.id"
                (onClick)="iniciarReset(usuario)"
              />
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="5">No hay usuarios que coincidan con los filtros.</td>
          </tr>
        </ng-template>
      </p-table>

      <p-dialog
        header="Editar roles"
        [modal]="true"
        [visible]="rolesDialog() !== null"
        (visibleChange)="onRolesDialogVisible($event)"
        [style]="{ width: '24rem' }"
      >
        @if (rolesDialog(); as u) {
          <p>Usuario: <strong>{{ u.nombreCompleto }}</strong></p>
          <div class="roles-list">
            @for (rol of rolesDisponibles; track rol) {
              <div class="role-row">
                <p-checkbox
                  [binary]="true"
                  [ngModel]="rolesSeleccionados.includes(rol)"
                  (ngModelChange)="onRolCheck(rol, $event)"
                  [inputId]="'rol-' + rol"
                />
                <label [for]="'rol-' + rol">{{ rol }}</label>
              </div>
            }
          </div>
          @if (rolesError()) {
            <p-message severity="error" [text]="rolesError()!" styleClass="block-msg" />
          }
          <div class="dialog-actions">
            <p-button label="Cancelar" severity="secondary" [text]="true" (onClick)="cerrarRolesDialog()" />
            <p-button
              [label]="rolesSaving() ? 'Guardando...' : 'Guardar'"
              [disabled]="rolesSeleccionados.length === 0 || rolesSaving()"
              (onClick)="confirmarRoles()"
            />
          </div>
        }
      </p-dialog>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 1000px;
        margin: 0 auto;
      }
      .toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--space-md);
        gap: var(--space-sm);
      }
      .filters {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
        align-items: center;
        margin-bottom: var(--space-md);
        padding: var(--space-sm);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }
      .filter-q {
        flex: 1;
        min-width: 12rem;
      }
      :host ::ng-deep .filter-select {
        min-width: 10rem;
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .actions {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-xs);
      }
      .roles-list {
        display: flex;
        flex-direction: column;
        gap: var(--space-sm);
        margin: var(--space-md) 0;
      }
      .role-row {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
      }
      .dialog-actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--space-sm);
        margin-top: var(--space-md);
      }
    `,
  ],
})
export class UsuariosListPage implements OnInit {
  usuarios = signal<UsuarioResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  resetInfoMsg = signal<string | null>(null);
  reseteando = signal<string | null>(null);

  filtroQ = '';
  filtroRol: string | null = null;
  filtroActivo: string | null = null;

  page = signal(0);
  totalElements = signal(0);
  readonly tamanoPagina = 20;

  rolesDialog = signal<UsuarioResponse | null>(null);
  rolesSeleccionados: string[] = [];
  rolesError = signal<string | null>(null);
  rolesSaving = signal(false);

  readonly rolesDisponibles = ['ADMIN', 'SECRETARIA', 'ASESOR', 'PROFESOR'];
  readonly rolOptions = this.rolesDisponibles.map((r) => ({ label: r, value: r }));
  readonly activoOptions = [
    { label: 'Activos', value: 'true' },
    { label: 'Inactivos', value: 'false' },
  ];

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  irNuevo(): void {
    this.router.navigate(['/usuarios/nuevo']);
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarUsuarios();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroRol = null;
    this.filtroActivo = null;
    this.page.set(0);
    this.cargarUsuarios();
  }

  onPage(event: TablePageEvent): void {
    const rows = event.rows ?? this.tamanoPagina;
    this.page.set(Math.floor((event.first ?? 0) / rows));
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.loading.set(true);
    this.errorMsg.set(null);

    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());
    if (this.filtroRol) params = params.set('rol', this.filtroRol);
    if (this.filtroActivo) params = params.set('activo', this.filtroActivo);

    this.http.get<PageResponse<UsuarioResponse>>(`${ApiBase.BASE}/usuarios`, { params }).subscribe({
      next: (respuesta) => {
        this.usuarios.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
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

  onRolesDialogVisible(visible: boolean): void {
    if (!visible) this.cerrarRolesDialog();
  }

  onRolCheck(rol: string, checked: boolean): void {
    this.rolesSeleccionados = checked
      ? [...this.rolesSeleccionados, rol]
      : this.rolesSeleccionados.filter((r) => r !== rol);
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
              : 'Error al actualizar los roles.',
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

  iniciarReset(usuario: UsuarioResponse): void {
    this.reseteando.set(usuario.id);
    this.resetInfoMsg.set(null);
    this.http.post(`${ApiBase.BASE}/usuarios/${usuario.id}/restablecer-password`, {}).subscribe({
      next: () => {
        this.resetInfoMsg.set(
          `Restablecimiento iniciado para "${usuario.nombreCompleto}". El envío de correo real aún no está implementado (ver DD-UC-005 §1); el token queda registrado en los logs del servidor para completar el flujo manualmente en /restablecer-password.`,
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
