import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { EvaluacionResponse } from './evaluacion.model';
import { MateriaResponse } from './materia.model';
import {
  CalificacionFilaResponse,
  NotaProvisionalResponse,
} from './calificacion.model';
import { ApiBase } from '../../core/api/api-base';
import {
  EstadoCelda,
  calcularCambios,
  estadoDeCelda,
  etiquetaEstado,
  mensajeEstadoCelda,
} from './calificaciones-grid.util';

/**
 * Matriz de calificaciones de una evaluación (DD-UC-018 / DD-UC-032 / FSD-UC-016):
 * nómina × celda de nota con p-table; Guardar → PUT solo de lo modificado.
 */
@Component({
  selector: 'app-evaluacion-calificaciones-page',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    TableModule,
    InputNumberModule,
    ButtonModule,
    MessageModule,
  ],
  template: `
    <div class="page">
      <div class="back">
        <a [routerLink]="['/academico/materias', materiaId, 'evaluaciones']">← Volver a Evaluaciones</a>
      </div>

      @if (notFound()) {
        <p-message severity="error" text="La evaluación no existe o no está asignada a su usuario." />
      } @else {
        <h2>Calificaciones — {{ evaluacion()?.nombre || '…' }}</h2>
        <p class="meta">
          Materia: {{ materia()?.nombre || '…' }} · Puntaje máximo: {{ evaluacion()?.puntajeMaximo }}
        </p>

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }
        @if (okMsg()) {
          <p-message severity="success" [text]="okMsg()!" styleClass="block-msg" />
        }

        @if (filas().length === 0) {
          <p>No hay estudiantes inscritos en los cursos asignados a esta materia.</p>
        } @else {
          <p class="counter">{{ cambiosPendientes() }} cambios sin guardar</p>

          <p-table
            [value]="filas()"
            dataKey="estudianteId"
            [tableStyle]="{ 'min-width': '40rem' }"
            styleClass="p-datatable-sm"
          >
            <ng-template #header>
              <tr>
                <th>Estudiante</th>
                <th>RUDE</th>
                <th>Nota [0 – {{ evaluacion()?.puntajeMaximo }}]</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template #body let-fila let-rowIndex="rowIndex">
              <tr [class.fila-modificada]="estadoDe(fila) === 'MODIFICADA'" [class.fila-invalida]="estadoDe(fila) === 'INVALIDA'">
                <td>{{ fila.nombreCompleto }}</td>
                <td>{{ fila.rude }}</td>
                <td>
                  <p-inputnumber
                    [ngModel]="edits[fila.estudianteId]"
                    (ngModelChange)="onNotaChange(fila.estudianteId, $event)"
                    [name]="'nota-' + fila.estudianteId"
                    [min]="0"
                    [max]="evaluacion()?.puntajeMaximo ?? 0"
                    [maxFractionDigits]="2"
                    [minFractionDigits]="0"
                    [useGrouping]="false"
                    locale="es-BO"
                    inputStyleClass="nota-input"
                    (onKeyDown)="onNotaKeydown($event, rowIndex)"
                    [invalid]="estadoDe(fila) === 'INVALIDA'"
                  />
                  @if (mensajeDe(fila); as msg) {
                    <small class="celda-error">{{ msg }}</small>
                  }
                </td>
                <td>{{ etiquetaDe(fila) }}</td>
                <td>
                  <p-button
                    type="button"
                    label="Ver provisional"
                    [link]="true"
                    size="small"
                    (onClick)="verProvisional(fila.estudianteId)"
                  />
                </td>
              </tr>
            </ng-template>
          </p-table>

          <p-button
            type="button"
            label="{{ saving() ? 'Guardando…' : 'Guardar calificaciones' }}"
            [disabled]="!puedeGuardar() || saving()"
            (onClick)="guardar()"
            styleClass="guardar-btn"
          />
        }

        @if (provisional()) {
          <section class="provisional">
            <h3>Resumen provisional</h3>
            <p>
              Nota periodo:
              <strong>{{ provisional()!.notaPeriodo ?? '—' }}</strong>
              · Promedio gestión:
              <strong>{{ provisional()!.promedioGestion }}</strong>
              ({{ provisional()!.estado }})
            </p>
            <p-table [value]="provisional()!.secciones" [tableStyle]="{ 'min-width': '20rem' }" styleClass="p-datatable-sm">
              <ng-template #header>
                <tr>
                  <th>Sección</th>
                  <th>Estado</th>
                  <th>Nota</th>
                </tr>
              </ng-template>
              <ng-template #body let-s>
                <tr>
                  <td>{{ s.nombre }}</td>
                  <td>{{ s.estado }}</td>
                  <td>{{ s.notaSeccion ?? '—' }}</td>
                </tr>
              </ng-template>
            </p-table>
          </section>
        }
      }
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 960px;
        margin: 0 auto;
      }
      .back {
        margin-bottom: var(--space-md);
        font-size: var(--font-size-small);
      }
      .meta {
        color: var(--color-text-secondary);
        margin-top: 0;
      }
      .counter {
        font-size: var(--font-size-small);
        color: var(--color-text-secondary);
        margin-bottom: var(--space-sm);
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .fila-modificada {
        background: var(--color-accent);
      }
      .fila-invalida {
        background: var(--color-danger-bg);
      }
      .celda-error {
        display: block;
        color: var(--color-danger-text);
        font-size: var(--font-size-small);
        margin-top: var(--space-xs);
      }
      :host ::ng-deep .nota-input {
        width: 6.5rem;
      }
      .guardar-btn {
        margin: var(--space-lg) 0;
      }
      .provisional {
        background: var(--color-bg);
        padding: var(--space-md);
        border-radius: var(--radius-sm);
      }
      .provisional h3 {
        margin-top: 0;
      }
    `,
  ],
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
  private editsVersion = signal(0);

  private maxPuntaje = computed(() => this.evaluacion()?.puntajeMaximo ?? 0);

  private resumenCambios = computed(() => {
    this.editsVersion();
    return calcularCambios(this.filas(), this.edits, this.maxPuntaje());
  });

  cambiosPendientes = computed(() => this.resumenCambios().items.length);

  puedeGuardar = computed(
    () => this.resumenCambios().items.length > 0 && this.resumenCambios().invalidos.length === 0
  );

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

  onNotaChange(estudianteId: string, valor: number | null): void {
    this.edits[estudianteId] = valor;
    this.editsVersion.update((v) => v + 1);
  }

  onNotaKeydown(event: Event, rowIndex: number): void {
    const ke = event as KeyboardEvent;
    if (ke.key !== 'Enter') {
      return;
    }
    ke.preventDefault();
    const next = rowIndex + 1;
    if (next >= this.filas().length) {
      return;
    }
    const inputs = document.querySelectorAll<HTMLInputElement>('.nota-input');
    const target = inputs[next];
    if (target) {
      target.focus();
      target.select();
    }
  }

  estadoDe(fila: CalificacionFilaResponse): EstadoCelda {
    this.editsVersion();
    return estadoDeCelda(fila.valor, this.edits[fila.estudianteId] ?? null, this.maxPuntaje());
  }

  etiquetaDe(fila: CalificacionFilaResponse): string {
    return etiquetaEstado(this.estadoDe(fila));
  }

  mensajeDe(fila: CalificacionFilaResponse): string | null {
    this.editsVersion();
    return mensajeEstadoCelda(fila.valor, this.edits[fila.estudianteId] ?? null, this.maxPuntaje());
  }

  guardar(): void {
    this.errorMsg.set(null);
    this.okMsg.set(null);
    const { items, invalidos } = calcularCambios(this.filas(), this.edits, this.maxPuntaje());
    if (invalidos.length > 0) {
      this.errorMsg.set('Corrija las notas inválidas antes de guardar.');
      return;
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
          this.editsVersion.update((v) => v + 1);
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
