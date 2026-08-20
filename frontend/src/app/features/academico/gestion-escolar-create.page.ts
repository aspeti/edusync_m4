import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Alta de una Gestión Escolar del tenant (DD-UC-009 §2).
 * POST /api/v1/gestiones-escolares → 201 GestionEscolarResponse (DD-UC-008).
 * Nace siempre en estado PLANIFICACION; no se pide estado en el formulario.
 */
@Component({
  selector: 'app-gestion-escolar-create-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 480px; margin: 2rem auto;">
      <h2>Nueva Gestión Escolar</h2>

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()">
        <div style="margin-bottom: 1rem;">
          <label>Nombre</label><br />
          <input
            type="text"
            [(ngModel)]="nombre"
            name="nombre"
            required
            placeholder="Ej: 2027"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1rem;">
          <label>Fecha de inicio</label><br />
          <input
            type="date"
            [(ngModel)]="fechaInicio"
            name="fechaInicio"
            required
            style="width: 100%; padding: 0.5rem; box-sizing: border-box;"
          />
        </div>
        <div style="margin-bottom: 1.5rem;">
          <label>Fecha de fin</label><br />
          <input
            type="date"
            [(ngModel)]="fechaFin"
            name="fechaFin"
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
            {{ loading() ? 'Creando...' : 'Crear Gestión Escolar' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class GestionEscolarCreatePage {
  nombre = '';
  fechaInicio = '';
  fechaFin = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

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
        next: () => {
          this.router.navigate(['/academico/gestiones-escolares']);
        },
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
