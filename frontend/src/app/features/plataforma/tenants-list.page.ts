import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TenantResponse } from './tenant.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Página de lista de Tenants — consola SysAdmin.
 * GET /api/v1/plataforma/tenants (DD-UC-004 §2).
 */
@Component({
  selector: 'app-tenants-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 900px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Tenants</h2>
        <a routerLink="/plataforma/tenants/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
          + Nuevo Tenant
        </a>
      </div>

      @if (loading()) {
        <p>Cargando tenants...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && tenants().length === 0 && !errorMsg()) {
        <p>No hay tenants registrados aún.</p>
      }

      @if (tenants().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Inicio suscripción</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Vencimiento</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (tenant of tenants(); track tenant.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ tenant.nombre }}</td>
                <td style="padding: 0.5rem;">
                  <span [style.color]="estadoColor(tenant.estado)">{{ tenant.estado }}</span>
                </td>
                <td style="padding: 0.5rem;">{{ tenant.fechaInicioSuscripcion }}</td>
                <td style="padding: 0.5rem;">{{ tenant.fechaVencimientoSuscripcion }}</td>
                <td style="padding: 0.5rem; display: flex; gap: 0.5rem;">
                  <a [routerLink]="['/plataforma/tenants', tenant.id, 'admin']"
                     style="color: #1e3a5f; text-decoration: underline; cursor: pointer;">
                    Crear admin
                  </a>
                  <button (click)="cambiarEstado(tenant)" style="cursor: pointer; font-size: 0.85rem;">
                    Cambiar estado
                  </button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }

      @if (estadoDialog()) {
        <div style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.4);display:flex;align-items:center;justify-content:center;">
          <div style="background:white;padding:2rem;border-radius:8px;min-width:320px;">
            <h3>Cambiar estado de "{{ estadoDialog()!.nombre }}"</h3>
            <div style="display:flex;flex-direction:column;gap:0.5rem;margin:1rem 0;">
              @for (op of estadoOpciones; track op) {
                <label style="cursor:pointer;">
                  <input type="radio" name="estado" [value]="op" [(ngModel)]="nuevoEstado" /> {{ op }}
                </label>
              }
            </div>
            @if (estadoError()) {
              <p style="color:#c62828;">{{ estadoError() }}</p>
            }
            <div style="display:flex;gap:0.5rem;justify-content:flex-end;">
              <button (click)="cerrarDialog()">Cancelar</button>
              <button (click)="confirmarEstado()" [disabled]="!nuevoEstado || estadoSaving()"
                      style="background:#1e3a5f;color:white;padding:0.4rem 1rem;cursor:pointer;">
                {{ estadoSaving() ? 'Guardando...' : 'Confirmar' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class TenantsListPage implements OnInit {
  tenants = signal<TenantResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  estadoDialog = signal<TenantResponse | null>(null);
  nuevoEstado = '';
  estadoError = signal<string | null>(null);
  estadoSaving = signal(false);

  readonly estadoOpciones = ['ACTIVO', 'SUSPENDIDO', 'VENCIDO'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarTenants();
  }

  cargarTenants(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.http.get<TenantResponse[]>(`${ApiBase.BASE}/plataforma/tenants`).subscribe({
      next: (data) => {
        this.tenants.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los tenants.');
        this.loading.set(false);
      },
    });
  }

  estadoColor(estado: string): string {
    return estado === 'ACTIVO' ? '#2e7d32' : estado === 'SUSPENDIDO' ? '#e65100' : '#757575';
  }

  cambiarEstado(tenant: TenantResponse): void {
    this.nuevoEstado = tenant.estado;
    this.estadoError.set(null);
    this.estadoDialog.set(tenant);
  }

  cerrarDialog(): void {
    this.estadoDialog.set(null);
    this.nuevoEstado = '';
  }

  confirmarEstado(): void {
    const tenant = this.estadoDialog();
    if (!tenant || !this.nuevoEstado) return;
    this.estadoSaving.set(true);
    this.estadoError.set(null);

    this.http
      .patch<TenantResponse>(`${ApiBase.BASE}/plataforma/tenants/${tenant.id}/estado`, {
        estado: this.nuevoEstado,
      })
      .subscribe({
        next: (updated) => {
          this.tenants.update((list) => list.map((t) => (t.id === updated.id ? updated : t)));
          this.estadoSaving.set(false);
          this.cerrarDialog();
        },
        error: () => {
          this.estadoError.set('Error al cambiar el estado.');
          this.estadoSaving.set(false);
        },
      });
  }
}
