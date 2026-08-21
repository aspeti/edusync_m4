import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AsignacionProfesorVistaResponse, ProfesorResponse } from './profesor.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Detalle de un Profesor: GET /profesores/{id} para título (no query param) y
 * tabla de asignaciones de solo lectura (DD-UC-014 §2). Sin alta inline.
 */
@Component({
  selector: 'app-profesor-detalle-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div style="max-width: 800px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a routerLink="/academico/profesores" style="font-size: 0.85rem;">← Volver a Profesores</a>
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          El Profesor no existe o no pertenece a su institución.
        </div>
      } @else {
        <h2>{{ profesor()?.nombreCompleto || 'Profesor' }}</h2>
        @if (profesor()) {
          <p style="color: #555; margin-top: 0;">
            <span
              style="display: inline-block; padding: 0.15rem 0.5rem; border-radius: 4px; font-size: 0.85rem;"
              [style.background]="profesor()!.activo ? '#e8f5e9' : '#eeeeee'"
            >
              {{ profesor()!.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </p>
        }

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        <section>
          <h3>Asignaciones</h3>
          @if (asignaciones().length === 0 && !errorMsg()) {
            <p>Este profesor todavía no tiene materias asignadas.</p>
          } @else if (asignaciones().length > 0) {
            <table style="width: 100%; border-collapse: collapse; margin-bottom: 1rem;">
              <thead>
                <tr style="background: #f5f5f5;">
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Materia</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Curso</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Paralelo</th>
                </tr>
              </thead>
              <tbody>
                @for (asignacion of asignaciones(); track asignacion.id) {
                  <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 0.5rem;">{{ asignacion.materiaNombre || asignacion.materiaId }}</td>
                    <td style="padding: 0.5rem;">{{ asignacion.cursoNombre || asignacion.cursoId }}</td>
                    <td style="padding: 0.5rem;">{{ asignacion.paraleloNombre || asignacion.paraleloId }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </section>
      }
    </div>
  `,
})
export class ProfesorDetallePage implements OnInit {
  profesorId = '';
  profesor = signal<ProfesorResponse | null>(null);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  asignaciones = signal<AsignacionProfesorVistaResponse[]>([]);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.profesorId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargarDetalle();
  }

  private cargarDetalle(): void {
    this.http.get<ProfesorResponse>(`${ApiBase.BASE}/profesores/${this.profesorId}`).subscribe({
      next: (profesor) => this.profesor.set(profesor),
      error: (err) => {
        if (err.status === 404) this.notFound.set(true);
        else this.errorMsg.set('Error al cargar el profesor.');
      },
    });
    this.http
      .get<AsignacionProfesorVistaResponse[]>(`${ApiBase.BASE}/profesores/${this.profesorId}/asignaciones`)
      .subscribe({
        next: (lista) => this.asignaciones.set(lista),
        error: (err) => {
          if (err.status === 404) this.notFound.set(true);
          else this.errorMsg.set('Error al cargar las asignaciones.');
        },
      });
  }
}
