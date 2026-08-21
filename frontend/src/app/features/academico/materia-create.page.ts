import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { MateriaResponse } from './materia.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Alta de una Materia del tenant (DD-UC-012 §2).
 * POST /api/v1/materias → 201 MateriaResponse.
 */
@Component({
  selector: 'app-materia-create-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 480px; margin: 2rem auto;">
      <h2>Nueva Materia</h2>

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()">
        <div style="margin-bottom: 1.5rem;">
          <label>Nombre</label><br />
          <input
            type="text"
            [(ngModel)]="nombre"
            name="nombre"
            required
            placeholder="Ej: Matemáticas"
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
            {{ loading() ? 'Creando...' : 'Crear Materia' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class MateriaCreatePage {
  nombre = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit(): void {
    if (!this.nombre.trim()) return;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.http
      .post<MateriaResponse>(`${ApiBase.BASE}/materias`, {
        nombre: this.nombre,
      })
      .subscribe({
        next: (creada) => {
          this.router.navigate(['/academico/materias', creada.id]);
        },
        error: (err) => {
          this.loading.set(false);
          if (err.status === 400) {
            this.errorMsg.set('Verifique los datos ingresados.');
          } else {
            this.errorMsg.set('Error al crear la Materia. Intente nuevamente.');
          }
        },
      });
  }

  volver(): void {
    this.router.navigate(['/academico/materias']);
  }
}
