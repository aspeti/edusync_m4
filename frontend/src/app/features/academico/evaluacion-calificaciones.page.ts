import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EvaluacionResponse } from './evaluacion.model';
import { MateriaResponse } from './materia.model';
import {
  CalificacionFilaResponse,
  NotaProvisionalResponse,
  UpsertCalificacionesRequest,
} from './calificacion.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Matriz de calificaciones de una evaluación (DD-UC-018 / FSD-UC-016):
 * nómina × celda de nota; Guardar → PUT lote; resumen PROVISIONAL.
 */
@Component({
  selector: 'app-evaluacion-calificaciones-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 960px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a [routerLink]="['/academico/materias', materiaId, 'evaluaciones']" style="font-size: 0.85rem;">
          ← Volver a Evaluaciones
        </a>
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          La evaluación no existe o no está asignada a su usuario.
        </div>
      } @else {
        <h2>Calificaciones — {{ evaluacion()?.nombre || '…' }}</h2>
        <p style="color: #555; margin-top: 0;">
          Materia: {{ materia()?.nombre || '…' }} · Puntaje máximo: {{ evaluacion()?.puntajeMaximo }}
        </p>

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }
        @if (okMsg()) {
          <div style="background: #e8f5e9; color: #2e7d32; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ okMsg() }}
          </div>
        }

        @if (filas().length === 0) {
          <p>No hay estudiantes inscritos en los cursos asignados a esta materia.</p>
        } @else {
          <table style="width: 100%; border-collapse: collapse; margin-bottom: 1rem;">
            <thead>
              <tr style="background: #f5f5f5;">
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estudiante</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">RUDE</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nota</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;"></th>
              </tr>
            </thead>
            <tbody>
              @for (fila of filas(); track fila.estudianteId) {
                <tr style="border-bottom: 1px solid #eee;">
                  <td style="padding: 0.5rem;">{{ fila.nombreCompleto }}</td>
                  <td style="padding: 0.5rem;">{{ fila.rude }}</td>
                  <td style="padding: 0.5rem;">
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      [attr.max]="evaluacion()?.puntajeMaximo"
                      [(ngModel)]="edits[fila.estudianteId]"
                      [name]="'nota-' + fila.estudianteId"
                      style="padding: 0.35rem; width: 6rem;"
                    />
                  </td>
                  <td style="padding: 0.5rem;">
                    <button type="button" (click)="verProvisional(fila.estudianteId)" style="cursor: pointer; font-size: 0.85rem;">
                      Ver provisional
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>

          <button
            type="button"
            (click)="guardar()"
            [disabled]="saving()"
            style="padding: 0.5rem 1.25rem; cursor: pointer; margin-bottom: 1.5rem;"
          >
            {{ saving() ? 'Guardando…' : 'Guardar calificaciones' }}
          </button>
        }

        @if (provisional()) {
          <section style="background: #fafafa; padding: 1rem; border-radius: 4px;">
            <h3 style="margin-top: 0;">Resumen provisional</h3>
            <p>
              Nota periodo:
              <strong>{{ provisional()!.notaPeriodo ?? '—' }}</strong>
              · Promedio gestión:
              <strong>{{ provisional()!.promedioGestion }}</strong>
              ({{ provisional()!.estado }})
            </p>
            <table style="width: 100%; border-collapse: collapse;">
              <thead>
                <tr style="background: #f0f0f0;">
                  <th style="padding: 0.4rem; text-align: left;">Sección</th>
                  <th style="padding: 0.4rem; text-align: left;">Estado</th>
                  <th style="padding: 0.4rem; text-align: left;">Nota</th>
                </tr>
              </thead>
              <tbody>
                @for (s of provisional()!.secciones; track s.seccionId) {
                  <tr>
                    <td style="padding: 0.4rem;">{{ s.nombre }}</td>
                    <td style="padding: 0.4rem;">{{ s.estado }}</td>
                    <td style="padding: 0.4rem;">{{ s.notaSeccion ?? '—' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </section>
        }
      }
    </div>
  `,
})
export class EvaluacionCalificacionesPage implements OnInit {
  materiaId = '';
  evaluacionId = '';
  materia = signal<MateriaResponse | null>(null);
  evaluacion = signal<EvaluacionResponse | null>(null);
  filas = signal<CalificacionFilaResponse[]>([]);
  provisional = signal<NotaProvisionalResponse | null>(null);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  okMsg = signal<string | null>(null);
  saving = signal(false);
  edits: Record<string, number | null> = {};

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.materiaId = this.route.snapshot.paramMap.get('id') ?? '';
    this.evaluacionId = this.route.snapshot.paramMap.get('evaluacionId') ?? '';

    this.http.get<MateriaResponse>(`${ApiBase.BASE}/materias/${this.materiaId}`).subscribe({
      next: (m) => this.materia.set(m),
      error: () => this.notFound.set(true),
    });

    this.http.get<EvaluacionResponse>(`${ApiBase.BASE}/evaluaciones/${this.evaluacionId}`).subscribe({
      next: (e) => {
        this.evaluacion.set(e);
        this.cargarNomina();
      },
      error: () => this.notFound.set(true),
    });
  }

  guardar(): void {
    this.errorMsg.set(null);
    this.okMsg.set(null);
    const max = this.evaluacion()?.puntajeMaximo ?? 0;
    const items: UpsertCalificacionesRequest['items'] = [];
    for (const fila of this.filas()) {
      const raw = this.edits[fila.estudianteId];
      if (raw === null || raw === undefined || (raw as unknown) === '') {
        continue;
      }
      const valor = Number(raw);
      if (Number.isNaN(valor) || valor < 0 || valor > max) {
        this.errorMsg.set(
          `Nota inválida para ${fila.nombreCompleto}: debe estar en [0, ${max}].`
        );
        return;
      }
      items.push({ estudianteId: fila.estudianteId, valor });
    }
    if (items.length === 0) {
      this.errorMsg.set('Ingrese al menos una nota para guardar.');
      return;
    }
    this.saving.set(true);
    this.http
      .put(`${ApiBase.BASE}/evaluaciones/${this.evaluacionId}/calificaciones`, { items })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.okMsg.set('Calificaciones guardadas.');
          this.cargarNomina();
          if (items[0]) {
            this.verProvisional(items[0].estudianteId);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          this.errorMsg.set(this.mensajeError(err));
        },
      });
  }

  verProvisional(estudianteId: string): void {
    const periodoId = this.evaluacion()?.periodoEvaluacionId;
    if (!periodoId) {
      return;
    }
    this.http
      .get<NotaProvisionalResponse>(
        `${ApiBase.BASE}/materias/${this.materiaId}/estudiantes/${estudianteId}/nota-provisional`,
        { params: { periodoId } }
      )
      .subscribe({
        next: (n) => this.provisional.set(n),
        error: (err: HttpErrorResponse) => this.errorMsg.set(this.mensajeError(err)),
      });
  }

  private cargarNomina(): void {
    this.http
      .get<CalificacionFilaResponse[]>(
        `${ApiBase.BASE}/evaluaciones/${this.evaluacionId}/calificaciones`
      )
      .subscribe({
        next: (filas) => {
          this.filas.set(filas);
          const next: Record<string, number | null> = {};
          for (const f of filas) {
            next[f.estudianteId] = f.valor;
          }
          this.edits = next;
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 404) {
            this.notFound.set(true);
          } else {
            this.errorMsg.set(this.mensajeError(err));
          }
        },
      });
  }

  private mensajeError(err: HttpErrorResponse): string {
    const body = err.error as { codigo?: string; mensaje?: string } | null;
    if (body?.mensaje) {
      return body.mensaje;
    }
    return 'No se pudo completar la operación.';
  }
}
