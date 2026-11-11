import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { EstudianteResponse, InscripcionResponse } from './estudiante.model';
import { CursoResponse, ParaleloResponse } from './curso.model';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/** Detalle Estudiante + inscripciones (DD-UC-013/021) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-estudiante-detalle-page',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    SelectModule,
    InputTextModule,
    MessageModule,
    TagModule,
  ],
  template: `
    <div class="page">
      <p-button label="← Volver a Estudiantes" [link]="true" (onClick)="volver()" />

      @if (notFound()) {
        <p-message
          severity="error"
          text="El Estudiante no existe o no pertenece a su institución."
          styleClass="block-msg"
        />
      } @else {
        <h2>{{ estudiante()?.nombreCompleto || 'Estudiante' }}</h2>
        @if (estudiante()) {
          <p class="meta">
            RUDE: {{ estudiante()!.rude }}
            <p-tag
              [value]="estudiante()!.estado"
              [severity]="estudiante()!.estado === 'ACTIVO' ? 'success' : 'secondary'"
            />
          </p>
        }

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        <h3>Inscripciones</h3>
        <p-table
          [value]="inscripciones()"
          dataKey="id"
          [tableStyle]="{ 'min-width': '40rem' }"
          styleClass="p-datatable-sm"
        >
          <ng-template #header>
            <tr>
              <th>Gestión</th>
              <th>Curso</th>
              <th>Paralelo</th>
              <th>Fecha</th>
              <th>Estado</th>
            </tr>
          </ng-template>
          <ng-template #body let-insc>
            <tr>
              <td>{{ insc.gestionNombre || nombreGestion(insc.gestionEscolarId) }}</td>
              <td>{{ insc.cursoNombre || nombreCurso(insc.cursoId) }}</td>
              <td>{{ insc.paraleloNombre || nombreParalelo(insc.paraleloId) }}</td>
              <td>{{ insc.fechaInscripcion }}</td>
              <td>{{ insc.estado }}</td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="5">Este estudiante todavía no tiene inscripciones.</td>
            </tr>
          </ng-template>
        </p-table>

        <div class="box">
          <h4>Nueva inscripción</h4>
          @if (!esAdmin() && gestionActivaNoEncontrada()) {
            <p-message
              severity="warn"
              text="No hay una gestión escolar activa en este momento. Contacta al administrador para inscribir estudiantes."
              styleClass="block-msg"
            />
          } @else {
            <form class="inline-form" (ngSubmit)="crearInscripcion()">
              @if (esAdmin()) {
                <label class="field">
                  Gestión escolar
                  <p-select
                    [options]="gestiones()"
                    [(ngModel)]="gestionSeleccionadaId"
                    name="gestionSeleccionadaId"
                    optionLabel="nombre"
                    optionValue="id"
                    placeholder="Seleccione..."
                    [showClear]="true"
                  />
                </label>
              } @else {
                <div class="field">
                  Gestión escolar actual
                  <strong>{{ nombreGestion(gestionSeleccionadaId || '') }}</strong>
                </div>
              }
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
              <label class="field">
                Fecha
                <input pInputText type="date" [(ngModel)]="fechaInscripcion" name="fechaInscripcion" required />
              </label>
              <p-button
                type="submit"
                [label]="saving() ? 'Inscribiendo...' : 'Inscribir'"
                [disabled]="saving()"
              />
            </form>
          }
        </div>
      }
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 800px;
        margin: 0 auto;
      }
      .meta {
        color: var(--color-text-secondary);
        display: flex;
        gap: var(--space-sm);
        align-items: center;
        flex-wrap: wrap;
      }
      .block-msg {
        display: block;
        margin: var(--space-md) 0;
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
        min-width: 9rem;
      }
    `,
  ],
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
  gestionActivaNoEncontrada = signal(false);

  gestionSeleccionadaId: string | null = null;
  cursoSeleccionadoId: string | null = null;
  paraleloSeleccionadoId: string | null = null;
  fechaInscripcion = '';
  saving = signal(false);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    protected auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.estudianteId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargarDetalle();
    this.cargarCatalogos();
  }

  volver(): void {
    this.router.navigate(['/academico/estudiantes']);
  }

  esAdmin(): boolean {
    return this.auth.hasRole('ADMIN');
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

  crearInscripcion(): void {
    if (
      !this.gestionSeleccionadaId ||
      !this.cursoSeleccionadoId ||
      !this.paraleloSeleccionadoId ||
      !this.fechaInscripcion
    ) {
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
          if (this.esAdmin()) this.gestionSeleccionadaId = null;
          this.cursoSeleccionadoId = null;
          this.paraleloSeleccionadoId = null;
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
      .subscribe({ next: (lista) => this.inscripciones.set(lista) });
  }

  private cargarCatalogos(): void {
    const params = new HttpParams().set('page', 0).set('size', 100);
    if (this.esAdmin()) {
      this.http
        .get<PageResponse<GestionEscolarResponse>>(`${ApiBase.BASE}/gestiones-escolares`, { params })
        .subscribe({ next: (respuesta) => this.gestiones.set(respuesta.content) });
    } else {
      this.http.get<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/activa`).subscribe({
        next: (activa) => {
          this.gestiones.set([activa]);
          this.gestionSeleccionadaId = activa.id;
        },
        error: (err) => {
          if (err.status === 404) this.gestionActivaNoEncontrada.set(true);
          else this.errorMsg.set('Error al cargar la gestión escolar activa.');
        },
      });
    }
    this.http.get<PageResponse<CursoResponse>>(`${ApiBase.BASE}/cursos`, { params }).subscribe({
      next: (respuesta) => this.cursos.set(respuesta.content),
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
    if (err.status === 409 && err.error?.codigo === 'E_INSCRIPCION_DUPLICADA') {
      return err.error.mensaje ?? 'El estudiante ya está inscrito en esa Gestión Escolar.';
    }
    if (err.status === 404) {
      return err.error?.mensaje ?? 'El recurso no existe o no pertenece a su institución.';
    }
    return fallback;
  }
}
