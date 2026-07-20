import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { TenantResponse } from './tenant.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Paso 1 del wizard de alta: crea el Tenant.
 * POST /api/v1/plataforma/tenants → 201 TenantResponse.
 * DD-UC-004 §2 (alta en dos pasos separados — DD-UC-003 §2/§3).
 */
@Component({
  selector: 'app-tenant-create-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 480px; margin: 2rem auto;">
      <h2>Nuevo Tenant — Paso 1 de 2</h2>
      <p style="color: #555;">Complete los datos del tenant. En el paso 2 creará el primer ADMIN.</p>

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()">
        <div style="margin-bottom: 1rem;">
          <label>Nombre del tenant</label><br />
          <input
            type="text"
            [(ngModel)]="nombre"
            name="nombre"
            required
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1rem;">
          <label>Fecha inicio suscripción</label><br />
          <input
            type="date"
            [(ngModel)]="fechaInicio"
            name="fechaInicio"
            required
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1.5rem;">
          <label>Fecha vencimiento suscripción</label><br />
          <input
            type="date"
            [(ngModel)]="fechaVencimiento"
            name="fechaVencimiento"
            required
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <button type="button" (click)="volver()" style="flex: 1; padding: 0.75rem; cursor: pointer;">
            Cancelar
          </button>
          <button
            type="submit"
            [disabled]="loading()"
            style="flex: 2; padding: 0.75rem; background: #1e3a5f; color: white; cursor: pointer; border: none; border-radius: 4px;"
          >
            {{ loading() ? 'Creando...' : 'Crear Tenant →' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class TenantCreatePage {
  nombre = '';
  fechaInicio = '';
  fechaVencimiento = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit(): void {
    if (!this.nombre || !this.fechaInicio || !this.fechaVencimiento) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post<TenantResponse>(`${ApiBase.BASE}/plataforma/tenants`, {
        nombre: this.nombre,
        fechaInicioSuscripcion: this.fechaInicio,
        fechaVencimientoSuscripcion: this.fechaVencimiento,
      })
      .subscribe({
        next: (tenant) => {
          // Paso 2: ir a crear el admin del tenant recién creado
          this.router.navigate(['/plataforma/tenants', tenant.id, 'admin']);
        },
        error: (err) => {
          this.loading.set(false);
          const codigo = err.error?.codigo;
          if (codigo === 'E_SUSCRIPCION_INCOMPLETA') {
            this.errorMsg.set('La fecha de vencimiento de suscripción es obligatoria.');
          } else {
            this.errorMsg.set('Error al crear el tenant. Intente nuevamente.');
          }
        },
        complete: () => this.loading.set(false),
      });
  }

  volver(): void {
    this.router.navigate(['/plataforma/tenants']);
  }
}
