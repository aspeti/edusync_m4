import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EstudianteResponse, InscripcionResponse } from './estudiante.model';
import { CursoResponse, ParaleloResponse } from './curso.model';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Detalle de un Estudiante: GET /estudiantes/{id} para título (no query param),
 * historial de inscripciones y alta inline POST /inscripciones (DD-UC-013 §2).
 */
@Component({
  selector: 'app-estudiante-detalle-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 800px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a routerLink="/academico/estudiantes" style="font-size: 0.85rem;">← Volver a Estudiantes</a>
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          El Estudiante no existe o no pertenece a su institución.
        </div>
      } @else {
        <h2>{{ estudiante()?.nombreCompleto || 'Estudiante' }}</h2>
        @if (estudiante()) {
          <p style="color: #555; margin-top: 0;">RUDE: {{ estudiante()!.rude }} — {{ estudiante()!.estado }}</p>
        }

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        <section>
          <h3>Inscripciones</h3>
          @if (inscripciones().length === 0) {
            <p>Este estudiante todavía no tiene inscripciones.</p>
          } @else {
            <table style="width: 100%; border-collapse: collapse; margin-bottom: 1rem;">
              <thead>
                <tr style="background: #f5f5f5;">
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Gestión</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Curso</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Paralelo</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Fecha</th>
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
                </tr>
              </thead>
              <tbody>
                @for (insc of inscripciones(); track insc.id) {
                  <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 0.5rem;">{{ nombreGestion(insc.gestionEscolarId) }}</td>
                    <td style="padding: 0.5rem;">{{ nombreCurso(insc.cursoId) }}</td>
                    <td style="padding: 0.5rem;">{{ nombreParalelo(insc.paraleloId) }}</td>
                    <td style="padding: 0.5rem;">{{ insc.fechaInscripcion }}</td>
                    <td style="padding: 0.5rem;">{{ insc.estado }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }

          <div style="background: #fafafa; padding: 1rem; border-radius: 4px;">
            <h4 style="margin-top: 0;">Nueva inscripción</h4>
            <form (ngSubmit)="crearInscripcion()" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: end;">
              <label>
                Gestión escolar<br />
                <select [(ngModel)]="gestionSeleccionadaId" name="gestionSeleccionadaId" required style="padding: 0.4rem; min-width: 160px;">
                  <option value="">Seleccione...</option>
                  @for (gestion of gestiones(); track gestion.id) {
                    <option [value]="gestion.id">{{ gestion.nombre }}</option>
                  }
                </select>
              </label>
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
              <label>
                Fecha<br />
                <input type="date" [(ngModel)]="fechaInscripcion" name="fechaInscripcion" required style="padding: 0.4rem;" />
              </label>
              <button type="submit" [disabled]="saving()" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; border: none; border-radius: 4px; cursor: pointer;">
                {{ saving() ? 'Inscribiendo...' : 'Inscribir' }}
              </button>
            </form>
          </div>
        </section>
      }
    </div>
  `,
})
export class EstudianteDetallePage implements OnInit {
  estudianteId = '';
  estudiante = signal<EstudianteResponse | null>(null);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);

  inscripciones = signal<InscripcionResponse[]>([]);
  gestiones = signal<GestionEscolarResponse[]>([]);
  cursos = signal<CursoResponse[]>([]);
  paralelosDelCurso = signal<ParaleloResponse[]>([]);
  paralelosPorId = signal<Record<string, ParaleloResponse>>({});

  gestionSeleccionadaId = '';
  cursoSeleccionadoId = '';
  paraleloSeleccionadoId = '';
  fechaInscripcion = '';
  saving = signal(false);

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.estudianteId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargarDetalle();
    this.cargarCatalogos();
  }

  nombreGestion(gestionId: string): string {
    return this.gestiones().find((g) => g.id === gestionId)?.nombre ?? gestionId;
  }

  nombreCurso(cursoId: string): string {
    return this.cursos().find((c) => c.id === cursoId)?.nombre ?? cursoId;
  }

  nombreParalelo(paraleloId: string): string {
    return this.paralelosPorId()[paraleloId]?.nombre ?? paraleloId;
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

  crearInscripcion(): void {
    if (!this.gestionSeleccionadaId || !this.cursoSeleccionadoId || !this.paraleloSeleccionadoId || !this.fechaInscripcion) {
      return;
    }
    this.saving.set(true);
    this.errorMsg.set(null);
    this.http
      .post<InscripcionResponse>(`${ApiBase.BASE}/inscripciones`, {
        estudianteId: this.estudianteId,
        gestionEscolarId: this.gestionSeleccionadaId,
        cursoId: this.cursoSeleccionadoId,
        paraleloId: this.paraleloSeleccionadoId,
        fechaInscripcion: this.fechaInscripcion,
      })
      .subscribe({
        next: (nueva) => {
          this.inscripciones.update((lista) => [...lista, nueva]);
          this.gestionSeleccionadaId = '';
          this.cursoSeleccionadoId = '';
          this.paraleloSeleccionadoId = '';
          this.paralelosDelCurso.set([]);
          this.fechaInscripcion = '';
          this.saving.set(false);
        },
        error: (err) => {
          this.saving.set(false);
          this.errorMsg.set(this.mensajeError(err, 'Error al crear la inscripción.'));
        },
      });
  }

  private cargarDetalle(): void {
    this.http.get<EstudianteResponse>(`${ApiBase.BASE}/estudiantes/${this.estudianteId}`).subscribe({
      next: (estudiante) => this.estudiante.set(estudiante),
      error: (err) => {
        if (err.status === 404) this.notFound.set(true);
        else this.errorMsg.set('Error al cargar el estudiante.');
      },
    });
    this.http
      .get<InscripcionResponse[]>(`${ApiBase.BASE}/estudiantes/${this.estudianteId}/inscripciones`)
      .subscribe({
        next: (lista) => {
          this.inscripciones.set(lista);
          this.cargarParalelosDeInscripciones(lista);
        },
      });
  }

  private cargarCatalogos(): void {
    const params = new HttpParams().set('page', 0).set('size', 100);
    this.http.get<PageResponse<GestionEscolarResponse>>(`${ApiBase.BASE}/gestiones-escolares`, { params }).subscribe({
      next: (respuesta) => this.gestiones.set(respuesta.content),
    });
    this.http.get<PageResponse<CursoResponse>>(`${ApiBase.BASE}/cursos`, { params }).subscribe({
      next: (respuesta) => this.cursos.set(respuesta.content),
    });
  }

  private cargarParalelosDeInscripciones(inscripciones: InscripcionResponse[]): void {
    const cursoIds = [...new Set(inscripciones.map((i) => i.cursoId))];
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

  private mensajeError(
    err: { status?: number; error?: { codigo?: string; mensaje?: string } },
    fallback: string
  ): string {
    if (err.status === 409 && err.error?.codigo === 'E_INSCRIPCION_DUPLICADA') {
      return err.error.mensaje ?? 'El estudiante ya está inscrito en esa Gestión Escolar.';
    }
    if (err.status === 404) {
      return err.error?.mensaje ?? 'El recurso no existe o no pertenece a su institución.';
    }
    return fallback;
  }
}
