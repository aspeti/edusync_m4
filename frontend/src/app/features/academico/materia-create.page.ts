import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { MateriaResponse } from './materia.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta de Materia (DD-UC-012) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-materia-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nueva Materia</h2>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <form (ngSubmit)="onSubmit()" class="form">
        <label class="field" for="mat-nombre">
          Nombre
          <input
            pInputText
            id="mat-nombre"
            type="text"
            [(ngModel)]="nombre"
            name="nombre"
            required
            placeholder="Ej: Matemáticas"
          />
        </label>
        <div class="actions">
          <p-button type="button" label="Cancelar" severity="secondary" [outlined]="true" (onClick)="volver()" />
          <p-button
            type="submit"
            [label]="loading() ? 'Creando...' : 'Crear Materia'"
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
export class MateriaCreatePage {
  nombre = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  onSubmit(): void {
    if (!this.nombre.trim()) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http.post<MateriaResponse>(`${ApiBase.BASE}/materias`, { nombre: this.nombre }).subscribe({
      next: (creada) => this.router.navigate(['/academico/materias', creada.id]),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(
          err.status === 400
            ? 'Verifique los datos ingresados.'
            : 'Error al crear la Materia. Intente nuevamente.',
        );
      },
    });
  }

  volver(): void {
    this.router.navigate(['/academico/materias']);
  }
}
