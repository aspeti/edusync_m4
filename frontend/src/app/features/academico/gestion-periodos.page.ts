import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { PeriodoEvaluacionResponse } from './periodo-evaluacion.model';
import { ApiBase } from '../../core/api/api-base';
import { AuthService } from '../../core/auth/auth.service';

/** Periodos de Gestión (DD-UC-015/034) — UI PrimeNG (ADR-0026/0027). Sin parámetros por periodo. */
@Component({
  selector: 'app-gestion-periodos-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, DialogModule, TagModule, MessageModule],
  template: `
    <div class="page">
      <p-button label="← Volver a Gestión Escolar" [link]="true" (onClick)="volver()" />

      @if (notFound()) {
        <p-message
          severity="error"
          text="La gestión escolar no existe o no pertenece a su institución."
          styleClass="block-msg"
        />
      } @else {
        <h2>Periodos de "{{ gestion()?.nombre || '…' }}"</h2>
        @if (gestion()) {
          <p class="meta">
            {{ gestion()!.fechaInicio }} — {{ gestion()!.fechaFin }} · estado {{ gestion()!.estado }}
          </p>
        }

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        <p-table
          [value]="periodos()"
          dataKey="id"
          [loading]="loading()"
          [tableStyle]="{ 'min-width': '40rem' }"
          styleClass="p-datatable-sm"
        >
          <ng-template #header>
            <tr>
              <th>#</th>
              <th>Nombre</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Estado</th>
              @if (esAdmin()) {
                <th>Acciones</th>
              }
            </tr>
          </ng-template>
          <ng-template #body let-periodo>
            <tr>
              <td>{{ periodo.orden }}</td>
              <td>{{ periodo.nombre }}</td>
              <td>{{ periodo.fechaInicio }}</td>
              <td>{{ periodo.fechaFin }}</td>
              <td>
                <p-tag [value]="periodo.estado" [severity]="estadoSeverity(periodo.estado)" />
              </td>
              @if (esAdmin()) {
                <td class="actions">
                  <p-button
                    label="Editar"
                    [link]="true"
                    size="small"
                    [disabled]="saving()"
                    (onClick)="editar(periodo)"
                  />
                  @if (periodo.estado !== 'ABIERTO') {
                    <p-button
                      label="Abrir"
                      [link]="true"
                      size="small"
                      [disabled]="saving()"
                      (onClick)="cambiarEstado(periodo, 'ABIERTO')"
                    />
                  } @else {
                    <p-button
                      label="Cerrar"
                      [link]="true"
                      size="small"
                      [disabled]="saving()"
                      (onClick)="cambiarEstado(periodo, 'CERRADO')"
                    />
                  }
                  @if (periodo.estado !== 'PENDIENTE') {
                    <p-button
                      label="Volver a pendiente"
                      [link]="true"
                      size="small"
                      [disabled]="saving()"
                      (onClick)="cambiarEstado(periodo, 'PENDIENTE')"
                    />
                  }
                  <p-button
                    label="Eliminar"
                    [link]="true"
                    severity="danger"
                    size="small"
                    [disabled]="saving()"
                    (onClick)="eliminar(periodo)"
                  />
                </td>
              }
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td [attr.colspan]="esAdmin() ? 6 : 5">Esta gestión todavía no tiene periodos.</td>
            </tr>
          </ng-template>
        </p-table>

        @if (esAdmin()) {
          <div class="box">
            <h3>Nuevo periodo</h3>
            <form class="inline-form" (ngSubmit)="onSubmit()">
              <label class="field">
                Nombre
                <input pInputText type="text" [(ngModel)]="nuevoNombre" name="nuevoNombre" required />
              </label>
              <label class="field">
                Inicio
                <input
                  pInputText
                  type="date"
                  [(ngModel)]="nuevaFechaInicio"
                  name="nuevaFechaInicio"
                  required
                />
              </label>
              <label class="field">
                Fin
                <input pInputText type="date" [(ngModel)]="nuevaFechaFin" name="nuevaFechaFin" required />
              </label>
              <p-button type="submit" [label]="saving() ? 'Guardando...' : 'Agregar'" [disabled]="saving()" />
            </form>
          </div>
        }
      }

      <p-dialog
        header="Editar periodo"
        [modal]="true"
        [visible]="editDialog() !== null"
        (visibleChange)="onEditVisible($event)"
        [style]="{ width: '24rem' }"
      >
        @if (editDialog(); as p) {
          <div class="form-stack">
            <label class="field">
              Nombre
              <input pInputText type="text" [(ngModel)]="editNombre" />
            </label>
            <label class="field">
              Fecha inicio
              <input pInputText type="date" [(ngModel)]="editFechaInicio" />
            </label>
            <label class="field">
              Fecha fin
              <input pInputText type="date" [(ngModel)]="editFechaFin" />
            </label>
          </div>
          @if (editError()) {
            <p-message severity="error" [text]="editError()!" styleClass="block-msg" />
          }
          <div class="dialog-actions">
            <p-button label="Cancelar" severity="secondary" [text]="true" (onClick)="cerrarEditDialog()" />
            <p-button
              [label]="editSaving() ? 'Guardando...' : 'Guardar'"
              [disabled]="editSaving()"
              (onClick)="confirmarEdicion()"
            />
          </div>
        }
      </p-dialog>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 900px;
        margin: 0 auto;
      }
      .meta {
        color: var(--color-text-secondary);
        font-size: var(--font-size-small);
      }
      .block-msg {
        display: block;
        margin: var(--space-md) 0;
      }
      .actions {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-xs);
      }
      .box {
        margin-top: var(--space-sm);
        padding: var(--space-md);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }
      .box h3 {
        margin-top: 0;
      }
      .inline-form {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
        align-items: end;
      }
      .form-stack {
        display: flex;
        flex-direction: column;
        gap: var(--space-sm);
        margin: var(--space-md) 0;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
        font-size: var(--font-size-small);
      }
      .dialog-actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--space-sm);
        margin-top: var(--space-md);
      }
    `,
  ],
})
export class GestionPeriodosPage implements OnInit {
  gestion = signal<GestionEscolarResponse | null>(null);
  periodos = signal<PeriodoEvaluacionResponse[]>([]);
  loading = signal(true);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  saving = signal(false);

  nuevoNombre = '';
  nuevaFechaInicio = '';
  nuevaFechaFin = '';

  editDialog = signal<PeriodoEvaluacionResponse | null>(null);
  editNombre = '';
  editFechaInicio = '';
  editFechaFin = '';
  editError = signal<string | null>(null);
  editSaving = signal(false);

  readonly esAdmin = computed(() => this.auth.hasRole('ADMIN'));

  private gestionId = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.gestionId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargar();
  }

  volver(): void {
    this.router.navigate(['/academico/gestiones-escolares']);
  }

  estadoSeverity(estado: string): 'success' | 'warn' | 'secondary' {
    if (estado === 'ABIERTO') return 'success';
    if (estado === 'PENDIENTE') return 'warn';
    return 'secondary';
  }

  onEditVisible(visible: boolean): void {
    if (!visible) this.cerrarEditDialog();
  }

  cargar(): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.errorMsg.set(null);
    this.http.get<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}`).subscribe({
      next: (gestion) => {
        this.gestion.set(gestion);
        this.http
          .get<PeriodoEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/periodos`)
          .subscribe({
            next: (periodos) => {
              this.periodos.set(periodos);
              this.loading.set(false);
            },
            error: (err: HttpErrorResponse) => this.alErrorCarga(err),
          });
      },
      error: (err: HttpErrorResponse) => this.alErrorCarga(err),
    });
  }

  onSubmit(): void {
    this.saving.set(true);
    this.errorMsg.set(null);
    this.http
      .post<PeriodoEvaluacionResponse>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/periodos`, {
        nombre: this.nuevoNombre,
        fechaInicio: this.nuevaFechaInicio,
        fechaFin: this.nuevaFechaFin,
      })
      .subscribe({
        next: () => {
          this.nuevoNombre = '';
          this.nuevaFechaInicio = '';
          this.nuevaFechaFin = '';
          this.saving.set(false);
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(this.mensajeError(err));
          this.saving.set(false);
        },
      });
  }

  cambiarEstado(periodo: PeriodoEvaluacionResponse, estado: 'ABIERTO' | 'CERRADO' | 'PENDIENTE'): void {
    this.saving.set(true);
    this.errorMsg.set(null);
    this.http
      .patch<PeriodoEvaluacionResponse>(`${ApiBase.BASE}/periodos-evaluacion/${periodo.id}/estado`, { estado })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(this.mensajeError(err));
          this.saving.set(false);
        },
      });
  }

  eliminar(periodo: PeriodoEvaluacionResponse): void {
    this.saving.set(true);
    this.errorMsg.set(null);
    this.http.delete(`${ApiBase.BASE}/periodos-evaluacion/${periodo.id}`).subscribe({
      next: () => {
        this.saving.set(false);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(this.mensajeError(err));
        this.saving.set(false);
      },
    });
  }

  editar(periodo: PeriodoEvaluacionResponse): void {
    this.editNombre = periodo.nombre;
    this.editFechaInicio = periodo.fechaInicio;
    this.editFechaFin = periodo.fechaFin;
    this.editError.set(null);
    this.editDialog.set(periodo);
  }

  cerrarEditDialog(): void {
    this.editDialog.set(null);
  }

  confirmarEdicion(): void {
    const periodo = this.editDialog();
    if (!periodo) return;
    this.editSaving.set(true);
    this.editError.set(null);
    this.http
      .patch<PeriodoEvaluacionResponse>(`${ApiBase.BASE}/periodos-evaluacion/${periodo.id}`, {
        nombre: this.editNombre,
        fechaInicio: this.editFechaInicio,
        fechaFin: this.editFechaFin,
      })
      .subscribe({
        next: () => {
          this.editSaving.set(false);
          this.editDialog.set(null);
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.editError.set(this.mensajeError(err));
          this.editSaving.set(false);
        },
      });
  }

  private alErrorCarga(err: HttpErrorResponse): void {
    if (err.status === 404) {
      this.notFound.set(true);
    } else {
      this.errorMsg.set('Error al cargar los periodos.');
    }
    this.loading.set(false);
  }

  private mensajeError(err: HttpErrorResponse): string {
    const codigo = err.error?.codigo as string | undefined;
    if (codigo === 'E_PERIODOS_SOLAPADOS') {
      return 'Las fechas se solapan con otro periodo.';
    }
    if (codigo === 'E_PERIODO_UNICO') {
      return 'Debe quedar al menos un periodo.';
    }
    if (codigo === 'E_FECHAS_INVALIDAS') {
      return 'La fecha de fin debe ser posterior a la de inicio.';
    }
    if (codigo === 'E_RANGO_INVALIDO') {
      return 'Hay secciones con rango inválido. Revise Secciones de Evaluación.';
    }
    if (codigo === 'E_MATERIA_SIN_DOCENTE') {
      return 'Hay materias con curso asignado sin profesor.';
    }
    if (codigo === 'E_SUMA_SECCIONES_INVALIDA') {
      return 'La suma de las notas de las secciones debe ser 100.';
    }
    return 'No se pudo completar la operación.';
  }
}
