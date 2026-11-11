import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta de Gestión Escolar (DD-UC-009) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-gestion-escolar-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nueva Gestión Escolar</h2>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <form (ngSubmit)="onSubmit()" class="form">
        <label class="field" for="ges-nombre">
          Nombre
          <input pInputText id="ges-nombre" type="text" [(ngModel)]="nombre" name="nombre" required placeholder="Ej: 2027" />
        </label>
        <label class="field" for="ges-inicio">
          Fecha de inicio
          <input pInputText id="ges-inicio" type="date" [(ngModel)]="fechaInicio" name="fechaInicio" required />
        </label>
        <label class="field" for="ges-fin">
          Fecha de fin
          <input pInputText id="ges-fin" type="date" [(ngModel)]="fechaFin" name="fechaFin" required />
        </label>
        <div class="actions">
          <p-button type="button" label="Cancelar" severity="secondary" [outlined]="true" (onClick)="volver()" />
          <p-button
            type="submit"
            [label]="loading() ? 'Creando...' : 'Crear Gestión Escolar'"
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
export class GestionEscolarCreatePage {
  nombre = '';
  fechaInicio = '';
  fechaFin = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  onSubmit(): void {
    if (!this.nombre || !this.fechaInicio || !this.fechaFin) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares`, {
        nombre: this.nombre,
        fechaInicio: this.fechaInicio,
        fechaFin: this.fechaFin,
      })
      .subscribe({
        next: () => this.router.navigate(['/academico/gestiones-escolares']),
        error: (err) => {
          this.loading.set(false);
          const codigo = err.error?.codigo;
          if (codigo === 'E_FECHAS_INVALIDAS') {
            this.errorMsg.set('La fecha de fin debe ser posterior a la fecha de inicio.');
          } else if (err.status === 400) {
            this.errorMsg.set('Verifique los datos ingresados.');
          } else {
            this.errorMsg.set('Error al crear la Gestión Escolar. Intente nuevamente.');
          }
        },
      });
  }

  volver(): void {
    this.router.navigate(['/academico/gestiones-escolares']);
  }
}
