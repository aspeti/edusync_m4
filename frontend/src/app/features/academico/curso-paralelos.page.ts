import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ParaleloResponse } from './curso.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Vista de detalle de un Curso: lista sus Paralelos y permite crear uno nuevo
 * inline (DD-UC-011 §2). GET/POST /api/v1/cursos/{id}/paralelos (DD-UC-010 §2,
 * sin paginar — cardinalidad acotada).
 *
 * El nombre del Curso llega como query param `nombre` desde `CursosListPage`
 * (no hay `GET /cursos/{id}` en el backend, DD-UC-010 §2); si falta (navegación
 * directa/recarga), se muestra un encabezado genérico — el `404
 * E_CURSO_NO_ENCONTRADO` del backend sigue siendo la única validación real de
 * existencia del Curso.
 */
@Component({
  selector: 'app-curso-paralelos-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 700px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a routerLink="/academico/cursos" style="font-size: 0.85rem;">← Volver a Cursos</a>
      </div>

      <h2>Paralelos de "{{ nombreCurso || 'este Curso' }}"</h2>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          El Curso no existe o no pertenece a su institución.
        </div>
      } @else {
        @if (loading()) {
          <p>Cargando paralelos...</p>
        }

        @if (!loading() && paralelos().length === 0) {
          <p>Este Curso todavía no tiene paralelos.</p>
        }

        @if (paralelos().length > 0) {
          <table style="width: 100%; border-collapse: collapse; margin-bottom: 1.5rem;">
            <thead>
              <tr style="background: #f5f5f5;">
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              </tr>
            </thead>
            <tbody>
              @for (paralelo of paralelos(); track paralelo.id) {
                <tr style="border-bottom: 1px solid #eee;">
                  <td style="padding: 0.5rem;">{{ paralelo.nombre }}</td>
                </tr>
              }
            </tbody>
          </table>
        }

        <div style="background: #fafafa; padding: 1rem; border-radius: 4px;">
          <h3 style="margin-top: 0;">Nuevo Paralelo</h3>

          @if (errorMsg()) {
            <div style="background: #fdecea; color: #c62828; padding: 0.5rem; border-radius: 4px; margin-bottom: 0.75rem;">
              {{ errorMsg() }}
            </div>
          }

          <form (ngSubmit)="onSubmit()" style="display: flex; gap: 0.5rem;">
            <input
              type="text"
              [(ngModel)]="nombreNuevoParalelo"
              name="nombreNuevoParalelo"
              required
              placeholder="Ej: A"
              style="flex: 1; padding: 0.5rem;"
            />
            <button
              type="submit"
              [disabled]="saving()"
              style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; cursor: pointer; border: none; border-radius: 4px;"
            >
              {{ saving() ? 'Creando...' : 'Crear' }}
            </button>
          </form>
        </div>
      }
    </div>
  `,
})
export class CursoParalelosPage implements OnInit {
  cursoId = '';
  nombreCurso = '';
  paralelos = signal<ParaleloResponse[]>([]);
  loading = signal(true);
  notFound = signal(false);

  nombreNuevoParalelo = '';
  saving = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cursoId = this.route.snapshot.paramMap.get('id') ?? '';
    this.nombreCurso = this.route.snapshot.queryParamMap.get('nombre') ?? '';
    this.cargarParalelos();
  }

  cargarParalelos(): void {
    this.loading.set(true);
    this.notFound.set(false);

    this.http.get<ParaleloResponse[]>(`${ApiBase.BASE}/cursos/${this.cursoId}/paralelos`).subscribe({
      next: (paralelos) => {
        this.paralelos.set(paralelos);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 404) {
          this.notFound.set(true);
        } else {
          this.errorMsg.set('Error al cargar los paralelos.');
        }
      },
    });
  }

  onSubmit(): void {
    if (!this.nombreNuevoParalelo.trim()) return;
    this.saving.set(true);
    this.errorMsg.set(null);

    this.http
      .post<ParaleloResponse>(`${ApiBase.BASE}/cursos/${this.cursoId}/paralelos`, {
        nombre: this.nombreNuevoParalelo,
      })
      .subscribe({
        next: (nuevo) => {
          this.paralelos.update((lista) => [...lista, nuevo]);
          this.nombreNuevoParalelo = '';
          this.saving.set(false);
        },
        error: (err) => {
          this.saving.set(false);
          if (err.status === 404) {
            this.notFound.set(true);
          } else if (err.status === 400) {
            this.errorMsg.set('Verifique el nombre ingresado.');
          } else {
            this.errorMsg.set('Error al crear el Paralelo. Intente nuevamente.');
          }
        },
      });
  }
}
