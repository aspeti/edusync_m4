import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { EstudianteResponse } from './estudiante.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta de Estudiante (DD-UC-013) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-estudiante-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, SelectModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nuevo Estudiante</h2>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <form (ngSubmit)="onSubmit()" class="form">
        <label class="field" for="est-rude">
          RUDE
          <input
            pInputText
            id="est-rude"
            type="text"
            [(ngModel)]="rude"
            name="rude"
            required
            maxlength="20"
            placeholder="Código RUDE"
          />
        </label>
        <label class="field" for="est-nombre">
          Nombre completo
          <input
            pInputText
            id="est-nombre"
            type="text"
            [(ngModel)]="nombreCompleto"
            name="nombreCompleto"
            required
            placeholder="Ej: Ana Pérez"
          />
        </label>
        <label class="field" for="est-estado">
          Estado
          <p-select
            inputId="est-estado"
            [options]="estadoOptions"
            [(ngModel)]="estado"
            name="estado"
            optionLabel="label"
            optionValue="value"
          />
        </label>
        <div class="actions">
          <p-button type="button" label="Cancelar" severity="secondary" [outlined]="true" (onClick)="volver()" />
          <p-button
            type="submit"
            [label]="loading() ? 'Creando...' : 'Crear Estudiante'"
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
export class EstudianteCreatePage {
  rude = '';
  nombreCompleto = '';
  estado: 'ACTIVO' | 'INACTIVO' = 'ACTIVO';
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  readonly estadoOptions = [
    { label: 'ACTIVO', value: 'ACTIVO' },
    { label: 'INACTIVO', value: 'INACTIVO' },
  ];

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  onSubmit(): void {
    if (!this.rude.trim() || !this.nombreCompleto.trim()) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post<EstudianteResponse>(`${ApiBase.BASE}/estudiantes`, {
        rude: this.rude.trim(),
        nombreCompleto: this.nombreCompleto.trim(),
        estado: this.estado,
      })
      .subscribe({
        next: (creado) => this.router.navigate(['/academico/estudiantes', creado.id]),
        error: (err) => {
          this.loading.set(false);
          if (err.status === 409 && err.error?.codigo === 'E_RUDE_DUPLICADO') {
            this.errorMsg.set(err.error.mensaje ?? 'Ya existe un estudiante con ese código RUDE.');
          } else if (err.status === 400) {
            this.errorMsg.set('Verifique los datos ingresados.');
          } else {
            this.errorMsg.set('Error al crear el Estudiante. Intente nuevamente.');
          }
        },
      });
  }

  volver(): void {
    this.router.navigate(['/academico/estudiantes']);
  }
}
