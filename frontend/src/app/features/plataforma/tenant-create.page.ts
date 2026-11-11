import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { TenantResponse } from './tenant.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta Tenant paso 1 (DD-UC-004) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-tenant-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nuevo Tenant — Paso 1 de 2</h2>
      <p class="hint">Complete los datos del tenant. En el paso 2 creará el primer ADMIN.</p>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <form (ngSubmit)="onSubmit()" class="form">
        <label class="field" for="tenant-nombre">
          Nombre del tenant
          <input pInputText id="tenant-nombre" type="text" [(ngModel)]="nombre" name="nombre" required />
        </label>
        <label class="field" for="tenant-inicio">
          Fecha inicio suscripción
          <input pInputText id="tenant-inicio" type="date" [(ngModel)]="fechaInicio" name="fechaInicio" required />
        </label>
        <label class="field" for="tenant-fin">
          Fecha vencimiento suscripción
          <input
            pInputText
            id="tenant-fin"
            type="date"
            [(ngModel)]="fechaVencimiento"
            name="fechaVencimiento"
            required
          />
        </label>
        <div class="actions">
          <p-button type="button" label="Cancelar" severity="secondary" [outlined]="true" (onClick)="volver()" />
          <p-button
            type="submit"
            [label]="loading() ? 'Creando...' : 'Crear Tenant →'"
            [disabled]="loading()"
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
      .hint {
        color: var(--color-text-secondary);
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
      .actions {
        display: flex;
        gap: var(--space-sm);
      }
    `,
  ],
})
export class TenantCreatePage {
  nombre = '';
  fechaInicio = '';
  fechaVencimiento = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

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
        next: (tenant) => this.router.navigate(['/plataforma/tenants', tenant.id, 'admin']),
        error: (err) => {
          this.loading.set(false);
          const codigo = err.error?.codigo;
          this.errorMsg.set(
            codigo === 'E_SUSCRIPCION_INCOMPLETA'
              ? 'La fecha de vencimiento de suscripción es obligatoria.'
              : 'Error al crear el tenant. Intente nuevamente.',
          );
        },
        complete: () => this.loading.set(false),
      });
  }

  volver(): void {
    this.router.navigate(['/plataforma/tenants']);
  }
}
