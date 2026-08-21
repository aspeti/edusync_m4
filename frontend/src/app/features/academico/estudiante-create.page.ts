import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { EstudianteResponse } from './estudiante.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Alta de un Estudiante del tenant (DD-UC-013 §2).
 * POST /api/v1/estudiantes → 201 EstudianteResponse. Sin editor JSON de datosPersonales.
 */
@Component({
  selector: 'app-estudiante-create-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 480px; margin: 2rem auto;">
      <h2>Nuevo Estudiante</h2>

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()">
        <div style="margin-bottom: 1rem;">
          <label>RUDE</label><br />
          <input
            type="text"
            [(ngModel)]="rude"
            name="rude"
            required
            maxlength="20"
            placeholder="Código RUDE"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1rem;">
          <label>Nombre completo</label><br />
          <input
            type="text"
            [(ngModel)]="nombreCompleto"
            name="nombreCompleto"
            required
            placeholder="Ej: Ana Pérez"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1.5rem;">
          <label>Estado</label><br />
          <select [(ngModel)]="estado" name="estado" style="width: 100%; padding: 0.5rem;">
            <option value="ACTIVO">ACTIVO</option>
            <option value="INACTIVO">INACTIVO</option>
          </select>
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
            {{ loading() ? 'Creando...' : 'Crear Estudiante' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class EstudianteCreatePage {
  rude = '';
  nombreCompleto = '';
  estado: 'ACTIVO' | 'INACTIVO' = 'ACTIVO';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

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
        next: (creado) => {
          this.router.navigate(['/academico/estudiantes', creado.id]);
        },
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
