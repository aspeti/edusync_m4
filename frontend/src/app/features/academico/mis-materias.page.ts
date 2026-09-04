import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MateriaResponse } from './materia.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Primera consola del rol PROFESOR: materias asignadas al JWT (DD-UC-017).
 * GET /api/v1/materias/mias.
 */
@Component({
  selector: 'app-mis-materias-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div style="max-width: 800px; margin: 0 auto;">
      <h2>Mis materias</h2>

      @if (loading()) {
        <p>Cargando materias...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && materias().length === 0 && !errorMsg()) {
        <p>No tiene materias asignadas.</p>
      }

      @if (materias().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Materia</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (materia of materias(); track materia.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ materia.nombre }}</td>
                <td style="padding: 0.5rem;">
                  <a [routerLink]="['/academico/materias', materia.id, 'evaluaciones']" style="font-size: 0.85rem;">
                    Evaluaciones
                  </a>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
})
export class MisMateriasPage implements OnInit {
  materias = signal<MateriaResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<MateriaResponse[]>(`${ApiBase.BASE}/materias/mias`).subscribe({
      next: (lista) => {
        this.materias.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMsg.set('Error al cargar sus materias.');
      },
    });
  }
}
