import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminCreadoResponse } from './tenant.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Paso 2 del wizard de alta: crea el primer ADMIN del Tenant.
 * POST /api/v1/plataforma/tenants/{id}/admins → 201 AdminCreadoResponse.
 * Llamada deliberadamente separada de la creación del tenant (DD-UC-003 §3, DD-UC-004 §2).
 */
@Component({
  selector: 'app-tenant-admin-create-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 480px; margin: 2rem auto;">
      <h2>Nuevo Tenant — Paso 2 de 2</h2>
      <p style="color: #555;">Cree el primer administrador para el tenant <strong>{{ tenantId }}</strong>.</p>

      @if (successMsg()) {
        <div style="background: #e8f5e9; color: #2e7d32; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ successMsg() }}
          <br />
          <button (click)="irALista()" style="margin-top: 0.5rem; cursor: pointer;">
            Ir a la lista de Tenants
          </button>
        </div>
      }

      @if (!successMsg()) {
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
          <div style="margin-bottom: 1.5rem;">
            <label>Contraseña inicial</label><br />
            <input
              type="password"
              [(ngModel)]="password"
              name="password"
              required
              minlength="8"
              style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
            />
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <button type="button" (click)="irALista()" style="flex: 1; padding: 0.75rem; cursor: pointer;">
              Omitir
            </button>
            <button
              type="submit"
              [disabled]="loading()"
              style="flex: 2; padding: 0.75rem; background: #1e3a5f; color: white; cursor: pointer; border: none; border-radius: 4px;"
            >
              {{ loading() ? 'Creando admin...' : 'Crear Admin' }}
            </button>
          </div>
        </form>
      }
    </div>
  `,
})
export class TenantAdminCreatePage implements OnInit {
  tenantId = '';
  nombreCompleto = '';
  email = '';
  password = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  successMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.tenantId = this.route.snapshot.paramMap.get('id') ?? '';
  }

  onSubmit(): void {
    if (!this.nombreCompleto || !this.email || !this.password) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post<AdminCreadoResponse>(`${ApiBase.BASE}/plataforma/tenants/${this.tenantId}/admins`, {
        nombreCompleto: this.nombreCompleto,
        email: this.email,
        password: this.password,
      })
      .subscribe({
        next: (admin) => {
          this.successMsg.set(`Admin "${admin.email}" creado exitosamente.`);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          const codigo = err.error?.codigo;
          if (codigo === 'E_EMAIL_EN_USO') {
            this.errorMsg.set('El email ya está registrado en el sistema.');
          } else if (err.status === 404) {
            this.errorMsg.set('Tenant no encontrado.');
          } else {
            this.errorMsg.set('Error al crear el admin. Intente nuevamente.');
          }
        },
      });
  }

  irALista(): void {
    this.router.navigate(['/plataforma/tenants']);
  }
}
