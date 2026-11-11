import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { AdminCreadoResponse } from './tenant.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta Admin de Tenant paso 2 (DD-UC-004) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-tenant-admin-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, PasswordModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nuevo Tenant — Paso 2 de 2</h2>
      <p class="hint">Cree el primer administrador para el tenant <strong>{{ tenantId }}</strong>.</p>

      @if (successMsg()) {
        <p-message severity="success" [text]="successMsg()!" styleClass="block-msg" />
        <p-button label="Ir a la lista de Tenants" (onClick)="irALista()" />
      }

      @if (!successMsg()) {
        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        <form (ngSubmit)="onSubmit()" class="form">
          <label class="field">
            Nombre completo
            <input pInputText type="text" [(ngModel)]="nombreCompleto" name="nombreCompleto" required />
          </label>
          <label class="field">
            Email
            <input pInputText type="email" [(ngModel)]="email" name="email" required />
          </label>
          <label class="field">
            Contraseña inicial
            <p-password
              [(ngModel)]="password"
              name="password"
              [feedback]="false"
              [toggleMask]="true"
              styleClass="w-full"
              inputStyleClass="w-full"
            />
          </label>
          <div class="actions">
            <p-button type="button" label="Omitir" severity="secondary" [outlined]="true" (onClick)="irALista()" />
            <p-button
              type="submit"
              [label]="loading() ? 'Creando admin...' : 'Crear Admin'"
              [disabled]="loading()"
            />
          </div>
        </form>
      }
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
      :host ::ng-deep .w-full,
      :host ::ng-deep .p-password {
        width: 100%;
      }
    `,
  ],
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
