import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  AsignacionCursoResponse,
  AsignacionProfesorResponse,
  MateriaResponse,
  ProfesorResumenResponse,
} from './materia.model';
import { CursoResponse, ParaleloResponse } from './curso.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Detalle de una Materia: GET /materias/{id} para el título (no query param),
 * asignaciones curso/paralelo y profesor con alta inline (DD-UC-012 §2).
 * El formulario de profesor solo ofrece combinaciones ya asignadas a curso
 * para no disparar 409 E_MATERIA_SIN_CURSO.
 */
@Component({
  selector: 'app-materia-detalle-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 800px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a routerLink="/academico/materias" style="font-size: 0.85rem;">← Volver a Materias</a>
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          La Materia no existe o no pertenece a su institución.
        </div>
      } @else {
        <h2>{{ materia()?.nombre || 'Materia' }}</h2>

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        <section style="margin-bottom: 2rem;">
          <h3>Asignaciones a Curso / Paralelo</h3>
          @if (asignacionesCurso().length === 0) {
            <p>Esta Materia todavía no está asignada a ningún curso.</p>
          } @else {
            <table style="width: 100%; border-collapse: collapse; margin-bottom: 1rem;">
              <thead>
                <tr style="background: #f5f5f5;">
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Curso</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Paralelo</th>
                </tr>
              </thead>
              <tbody>
                @for (asig of asignacionesCurso(); track asig.id) {
                  <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 0.5rem;">{{ nombreCurso(asig.cursoId) }}</td>
                    <td style="padding: 0.5rem;">{{ nombreParaleloAsignado(asig) }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }

          <div style="background: #fafafa; padding: 1rem; border-radius: 4px;">
            <h4 style="margin-top: 0;">Nueva asignación a curso</h4>
            <form (ngSubmit)="crearAsignacionCurso()" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: end;">
              <label>
                Curso<br />
                <select [(ngModel)]="cursoSeleccionadoId" name="cursoSeleccionadoId" (ngModelChange)="onCursoSeleccionado($event)" required style="padding: 0.4rem; min-width: 180px;">
                  <option value="">Seleccione...</option>
                  @for (curso of cursos(); track curso.id) {
                    <option [value]="curso.id">{{ curso.nombre }}</option>
                  }
                </select>
              </label>
              <label>
                Paralelo<br />
                <select [(ngModel)]="paraleloSeleccionadoId" name="paraleloSeleccionadoId" required style="padding: 0.4rem; min-width: 120px;">
                  <option value="">Seleccione...</option>
                  @for (paralelo of paralelosDelCurso(); track paralelo.id) {
                    <option [value]="paralelo.id">{{ paralelo.nombre }}</option>
                  }
                </select>
              </label>
              <button type="submit" [disabled]="savingCurso()" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; border: none; border-radius: 4px; cursor: pointer;">
                {{ savingCurso() ? 'Asignando...' : 'Asignar' }}
              </button>
            </form>
          </div>
        </section>

        <section>
          <h3>Asignaciones a Profesor</h3>
          @if (asignacionesProfesor().length === 0) {
            <p>Esta Materia todavía no tiene profesores asignados.</p>
          } @else {
            <table style="width: 100%; border-collapse: collapse; margin-bottom: 1rem;">
              <thead>
                <tr style="background: #f5f5f5;">
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Profesor</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Curso</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Paralelo</th>
                </tr>
              </thead>
              <tbody>
                @for (asig of asignacionesProfesor(); track asig.id) {
                  <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 0.5rem;">{{ nombreProfesor(asig.profesorId) }}</td>
                    <td style="padding: 0.5rem;">{{ nombreCurso(asig.cursoId) }}</td>
                    <td style="padding: 0.5rem;">{{ nombreParaleloAsignado(asig) }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }

          <div style="background: #fafafa; padding: 1rem; border-radius: 4px;">
            <h4 style="margin-top: 0;">Nueva asignación a profesor</h4>
            @if (asignacionesCurso().length === 0) {
              <p style="margin: 0;">Asigne primero un curso/paralelo para poder asignar un profesor.</p>
            } @else {
              <form (ngSubmit)="crearAsignacionProfesor()" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: end;">
                <label>
                  Profesor<br />
                  <select [(ngModel)]="profesorSeleccionadoId" name="profesorSeleccionadoId" required style="padding: 0.4rem; min-width: 180px;">
                    <option value="">Seleccione...</option>
                    @for (profesor of profesores(); track profesor.id) {
                      <option [value]="profesor.id">{{ profesor.nombreCompleto }}</option>
                    }
                  </select>
                </label>
                <label>
                  Curso / Paralelo<br />
                  <select [(ngModel)]="asignacionCursoSeleccionadaId" name="asignacionCursoSeleccionadaId" required style="padding: 0.4rem; min-width: 200px;">
                    <option value="">Seleccione...</option>
                    @for (asig of asignacionesCurso(); track asig.id) {
                      <option [value]="asig.id">{{ nombreCurso(asig.cursoId) }} — {{ nombreParaleloAsignado(asig) }}</option>
                    }
                  </select>
                </label>
                <button type="submit" [disabled]="savingProfesor()" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; border: none; border-radius: 4px; cursor: pointer;">
                  {{ savingProfesor() ? 'Asignando...' : 'Asignar' }}
                </button>
              </form>
            }
          </div>
        </section>
      }
    </div>
  `,
})
export class MateriaDetallePage implements OnInit {
  materiaId = '';
  materia = signal<MateriaResponse | null>(null);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);

  asignacionesCurso = signal<AsignacionCursoResponse[]>([]);
  asignacionesProfesor = signal<AsignacionProfesorResponse[]>([]);
  cursos = signal<CursoResponse[]>([]);
  paralelosDelCurso = signal<ParaleloResponse[]>([]);
  profesores = signal<ProfesorResumenResponse[]>([]);
  paralelosPorId = signal<Record<string, ParaleloResponse>>({});

  cursoSeleccionadoId = '';
  paraleloSeleccionadoId = '';
  profesorSeleccionadoId = '';
  asignacionCursoSeleccionadaId = '';
  savingCurso = signal(false);
  savingProfesor = signal(false);

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.materiaId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargarDetalle();
    this.cargarCatalogos();
  }

  nombreCurso(cursoId: string): string {
    return this.cursos().find((c) => c.id === cursoId)?.nombre ?? cursoId;
  }

  nombreParaleloAsignado(asig: { paraleloId: string }): string {
    return this.paralelosPorId()[asig.paraleloId]?.nombre ?? asig.paraleloId;
  }

  nombreProfesor(profesorId: string): string {
    return this.profesores().find((p) => p.id === profesorId)?.nombreCompleto ?? profesorId;
  }

  onCursoSeleccionado(cursoId: string): void {
    this.paraleloSeleccionadoId = '';
    this.paralelosDelCurso.set([]);
    if (!cursoId) return;
    this.http.get<ParaleloResponse[]>(`${ApiBase.BASE}/cursos/${cursoId}/paralelos`).subscribe({
      next: (paralelos) => {
        this.paralelosDelCurso.set(paralelos);
        this.registrarParalelos(paralelos);
      },
    });
  }

  crearAsignacionCurso(): void {
    if (!this.cursoSeleccionadoId || !this.paraleloSeleccionadoId) return;
    this.savingCurso.set(true);
    this.errorMsg.set(null);
    this.http
      .post<AsignacionCursoResponse>(`${ApiBase.BASE}/materias/${this.materiaId}/asignaciones-curso`, {
        cursoId: this.cursoSeleccionadoId,
        paraleloId: this.paraleloSeleccionadoId,
      })
      .subscribe({
        next: (nueva) => {
          this.asignacionesCurso.update((lista) => [...lista, nueva]);
          this.cursoSeleccionadoId = '';
          this.paraleloSeleccionadoId = '';
          this.paralelosDelCurso.set([]);
          this.savingCurso.set(false);
        },
        error: (err) => {
          this.savingCurso.set(false);
          this.errorMsg.set(this.mensajeError(err, 'Error al asignar el curso.'));
        },
      });
  }

  crearAsignacionProfesor(): void {
    const asignacion = this.asignacionesCurso().find((a) => a.id === this.asignacionCursoSeleccionadaId);
    if (!this.profesorSeleccionadoId || !asignacion) return;
    this.savingProfesor.set(true);
    this.errorMsg.set(null);
    this.http
      .post<AsignacionProfesorResponse>(`${ApiBase.BASE}/materias/${this.materiaId}/asignaciones-profesor`, {
        profesorId: this.profesorSeleccionadoId,
        cursoId: asignacion.cursoId,
        paraleloId: asignacion.paraleloId,
      })
      .subscribe({
        next: (nueva) => {
          this.asignacionesProfesor.update((lista) => [...lista, nueva]);
          this.profesorSeleccionadoId = '';
          this.asignacionCursoSeleccionadaId = '';
          this.savingProfesor.set(false);
        },
        error: (err) => {
          this.savingProfesor.set(false);
          this.errorMsg.set(this.mensajeError(err, 'Error al asignar el profesor.'));
        },
      });
  }

  private cargarDetalle(): void {
    this.http.get<MateriaResponse>(`${ApiBase.BASE}/materias/${this.materiaId}`).subscribe({
      next: (materia) => this.materia.set(materia),
      error: (err) => {
        if (err.status === 404) this.notFound.set(true);
        else this.errorMsg.set('Error al cargar la materia.');
      },
    });
    this.http
      .get<AsignacionCursoResponse[]>(`${ApiBase.BASE}/materias/${this.materiaId}/asignaciones-curso`)
      .subscribe({
        next: (lista) => {
          this.asignacionesCurso.set(lista);
          this.cargarParalelosDeAsignaciones(lista);
        },
      });
    this.http
      .get<AsignacionProfesorResponse[]>(`${ApiBase.BASE}/materias/${this.materiaId}/asignaciones-profesor`)
      .subscribe({ next: (lista) => this.asignacionesProfesor.set(lista) });
  }

  private cargarCatalogos(): void {
    const params = new HttpParams().set('page', 0).set('size', 100);
    this.http.get<PageResponse<CursoResponse>>(`${ApiBase.BASE}/cursos`, { params }).subscribe({
      next: (respuesta) => this.cursos.set(respuesta.content),
    });
    this.http.get<ProfesorResumenResponse[]>(`${ApiBase.BASE}/materias/profesores-disponibles`).subscribe({
      next: (lista) => this.profesores.set(lista),
    });
  }

  private cargarParalelosDeAsignaciones(asignaciones: AsignacionCursoResponse[]): void {
    const cursoIds = [...new Set(asignaciones.map((a) => a.cursoId))];
    for (const cursoId of cursoIds) {
      this.http.get<ParaleloResponse[]>(`${ApiBase.BASE}/cursos/${cursoId}/paralelos`).subscribe({
        next: (paralelos) => this.registrarParalelos(paralelos),
      });
    }
  }

  private registrarParalelos(paralelos: ParaleloResponse[]): void {
    this.paralelosPorId.update((actual) => {
      const siguiente = { ...actual };
      for (const paralelo of paralelos) siguiente[paralelo.id] = paralelo;
      return siguiente;
    });
  }

  private mensajeError(err: { status?: number; error?: { codigo?: string; mensaje?: string } }, fallback: string): string {
    if (err.status === 409 && err.error?.codigo === 'E_MATERIA_SIN_CURSO') {
      return err.error.mensaje ?? 'Asigne primero el curso/paralelo a la materia.';
    }
    if (err.status === 404) {
      return err.error?.mensaje ?? 'El recurso no existe o no pertenece a su institución.';
    }
    return fallback;
  }
}
