import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { CursoResponse } from './curso.model';
import { ApiBase } from '../../core/api/api-base';

/** Alta de Curso (DD-UC-011) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-curso-create-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, MessageModule],
  template: `
    <div class="page">
      <h2>Nuevo Curso</h2>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <form (ngSubmit)="onSubmit()" class="form">
        <label class="field" for="curso-nombre">
          Nombre
          <input
            pInputText
            id="curso-nombre"
            type="text"
            [(ngModel)]="nombre"
            name="nombre"
            required
            placeholder="Ej: Primero de Primaria"
          />
        </label>
        <div class="actions">
          <p-button type="button" label="Cancelar" severity="secondary" [outlined]="true" (onClick)="volver()" />
          <p-button
            type="submit"
            [label]="loading() ? 'Creando...' : 'Crear Curso'"
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
export class CursoCreatePage {
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

    this.http.post<CursoResponse>(`${ApiBase.BASE}/cursos`, { nombre: this.nombre }).subscribe({
      next: () => this.router.navigate(['/academico/cursos']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(
          err.status === 400
            ? 'Verifique los datos ingresados.'
            : 'Error al crear el Curso. Intente nuevamente.',
        );
      },
    });
  }

  volver(): void {
    this.router.navigate(['/academico/cursos']);
  }
}
