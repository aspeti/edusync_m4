import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import {
  AsignacionCursoResponse,
  AsignacionProfesorResponse,
  MateriaResponse,
  ProfesorResumenResponse,
} from './materia.model';
import { CursoResponse, ParaleloResponse } from './curso.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/** Detalle Materia + asignaciones (DD-UC-012) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-materia-detalle-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, SelectModule, MessageModule],
  template: `
    <div class="page">
      <p-button label="← Volver a Materias" [link]="true" (onClick)="volver()" />

      @if (notFound()) {
        <p-message severity="error" text="La Materia no existe o no pertenece a su institución." styleClass="block-msg" />
      } @else {
        <h2>{{ materia()?.nombre || 'Materia' }}</h2>
        @if (auth.hasRole('ADMIN')) {
          <p><p-button label="Evaluaciones" [link]="true" (onClick)="irEvaluaciones()" /></p>
        }

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        <section class="section">
          <h3>Asignaciones a Curso / Paralelo</h3>
          <p-table
            [value]="asignacionesCurso()"
            dataKey="id"
            [tableStyle]="{ 'min-width': '24rem' }"
            styleClass="p-datatable-sm"
          >
            <ng-template #header>
              <tr>
                <th>Curso</th>
                <th>Paralelo</th>
              </tr>
            </ng-template>
            <ng-template #body let-asig>
              <tr>
                <td>{{ asig.cursoNombre || nombreCurso(asig.cursoId) }}</td>
                <td>{{ asig.paraleloNombre || nombreParaleloAsignado(asig) }}</td>
              </tr>
            </ng-template>
            <ng-template #emptymessage>
              <tr>
                <td colspan="2">Esta Materia todavía no está asignada a ningún curso.</td>
              </tr>
            </ng-template>
          </p-table>

          <div class="box">
            <h4>Nueva asignación a curso</h4>
            <form class="inline-form" (ngSubmit)="crearAsignacionCurso()">
              <label class="field">
                Curso
                <p-select
                  [options]="cursos()"
                  [(ngModel)]="cursoSeleccionadoId"
                  name="cursoSeleccionadoId"
                  optionLabel="nombre"
                  optionValue="id"
                  placeholder="Seleccione..."
                  (ngModelChange)="onCursoSeleccionado($event)"
                  [showClear]="true"
                />
              </label>
              <label class="field">
                Paralelo
                <p-select
                  [options]="paralelosDelCurso()"
                  [(ngModel)]="paraleloSeleccionadoId"
                  name="paraleloSeleccionadoId"
                  optionLabel="nombre"
                  optionValue="id"
                  placeholder="Seleccione..."
                  [showClear]="true"
                />
              </label>
              <p-button
                type="submit"
                [label]="savingCurso() ? 'Asignando...' : 'Asignar'"
                [disabled]="savingCurso()"
              />
            </form>
          </div>
        </section>

        <section class="section">
          <h3>Asignaciones a Profesor</h3>
          <p-table
            [value]="asignacionesProfesor()"
            dataKey="id"
            [tableStyle]="{ 'min-width': '30rem' }"
            styleClass="p-datatable-sm"
          >
            <ng-template #header>
              <tr>
                <th>Profesor</th>
                <th>Curso</th>
                <th>Paralelo</th>
              </tr>
            </ng-template>
            <ng-template #body let-asig>
              <tr>
                <td>{{ nombreProfesor(asig.profesorId) }}</td>
                <td>{{ asig.cursoNombre || nombreCurso(asig.cursoId) }}</td>
                <td>{{ asig.paraleloNombre || nombreParaleloAsignado(asig) }}</td>
              </tr>
            </ng-template>
            <ng-template #emptymessage>
              <tr>
                <td colspan="3">Esta Materia todavía no tiene profesores asignados.</td>
              </tr>
            </ng-template>
          </p-table>

          <div class="box">
            <h4>Nueva asignación a profesor</h4>
            @if (asignacionesCurso().length === 0) {
              <p>Asigne primero un curso/paralelo para poder asignar un profesor.</p>
            } @else {
              <form class="inline-form" (ngSubmit)="crearAsignacionProfesor()">
                <label class="field">
                  Profesor
                  <p-select
                    [options]="profesores()"
                    [(ngModel)]="profesorSeleccionadoId"
                    name="profesorSeleccionadoId"
                    optionLabel="nombreCompleto"
                    optionValue="id"
                    placeholder="Seleccione..."
                    [showClear]="true"
                  />
                </label>
                <label class="field">
                  Curso / Paralelo
                  <p-select
                    [options]="opcionesAsignacionCurso"
                    [(ngModel)]="asignacionCursoSeleccionadaId"
                    name="asignacionCursoSeleccionadaId"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Seleccione..."
                    [showClear]="true"
                  />
                </label>
                <p-button
                  type="submit"
                  [label]="savingProfesor() ? 'Asignando...' : 'Asignar'"
                  [disabled]="savingProfesor()"
                />
              </form>
            }
          </div>
        </section>
      }
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 800px;
        margin: 0 auto;
      }
      .block-msg {
        display: block;
        margin: var(--space-md) 0;
      }
      .section {
        margin-bottom: var(--space-xl);
      }
      .box {
        margin-top: var(--space-md);
        padding: var(--space-md);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }
      .box h4 {
        margin-top: 0;
      }
      .inline-form {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
        align-items: end;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
        font-size: var(--font-size-small);
        min-width: 10rem;
      }
    `,
  ],
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

  cursoSeleccionadoId: string | null = null;
  paraleloSeleccionadoId: string | null = null;
  profesorSeleccionadoId: string | null = null;
  asignacionCursoSeleccionadaId: string | null = null;
  savingCurso = signal(false);
  savingProfesor = signal(false);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    protected auth: AuthService,
  ) {}

  get opcionesAsignacionCurso(): { label: string; value: string }[] {
    return this.asignacionesCurso().map((asig) => ({
      value: asig.id,
      label: `${asig.cursoNombre || this.nombreCurso(asig.cursoId)} — ${asig.paraleloNombre || this.nombreParaleloAsignado(asig)}`,
    }));
  }

  ngOnInit(): void {
    this.materiaId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargarDetalle();
    this.cargarCatalogos();
  }

  volver(): void {
    this.router.navigate(['/academico/materias']);
  }

  irEvaluaciones(): void {
    this.router.navigate(['/academico/materias', this.materiaId, 'evaluaciones']);
  }

  nombreCurso(cursoId: string): string {
    return this.cursos().find((c) => c.id === cursoId)?.nombre ?? cursoId;
  }

  nombreParaleloAsignado(asig: { paraleloId: string; paraleloNombre?: string | null }): string {
    return asig.paraleloNombre || this.paralelosPorId()[asig.paraleloId]?.nombre || asig.paraleloId;
  }

  nombreProfesor(profesorId: string): string {
    return this.profesores().find((p) => p.id === profesorId)?.nombreCompleto ?? profesorId;
  }

  onCursoSeleccionado(cursoId: string | null): void {
    this.paraleloSeleccionadoId = null;
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
          this.cursoSeleccionadoId = null;
          this.paraleloSeleccionadoId = null;
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
          this.profesorSeleccionadoId = null;
          this.asignacionCursoSeleccionadaId = null;
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
      .subscribe({ next: (lista) => this.asignacionesCurso.set(lista) });
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

  private registrarParalelos(paralelos: ParaleloResponse[]): void {
    this.paralelosPorId.update((actual) => {
      const siguiente = { ...actual };
      for (const paralelo of paralelos) siguiente[paralelo.id] = paralelo;
      return siguiente;
    });
  }

  private mensajeError(
    err: { status?: number; error?: { codigo?: string; mensaje?: string } },
    fallback: string,
  ): string {
    if (err.status === 409 && err.error?.codigo === 'E_MATERIA_SIN_CURSO') {
      return err.error.mensaje ?? 'Asigne primero el curso/paralelo a la materia.';
    }
    if (err.status === 404) {
      return err.error?.mensaje ?? 'El recurso no existe o no pertenece a su institución.';
    }
    return fallback;
  }
}
