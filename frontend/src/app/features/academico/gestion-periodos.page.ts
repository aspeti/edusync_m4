import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { PeriodoEvaluacionResponse } from './periodo-evaluacion.model';
import { ApiBase } from '../../core/api/api-base';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Detalle de Periodos de una Gestion Escolar (DD-UC-015 §2, DD-UC-019): GET
 * gestion por id, lista de periodos, alta/edición/eliminación y abrir/cerrar.
 *
 * DD-UC-019: `ADMIN` puede editar nombre/fechas/estado de cualquier periodo en
 * cualquier momento, sin la secuencialidad de apertura ni el freeze de
 * inmutabilidad que existían antes (ambos exclusivos de este endpoint,
 * ADMIN-only). `SECRETARIA`/`PROFESOR`/`ASESOR` solo ven esta pantalla en modo
 * lectura (la gestión visible ya está acotada a la ACTIVA por el backend).
 */
@Component({
  selector: 'app-gestion-periodos-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 900px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a routerLink="/academico/gestiones-escolares" style="font-size: 0.85rem;">← Volver a Gestión Escolar</a>
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          La gestión escolar no existe o no pertenece a su institución.
        </div>
      } @else {
        <h2>Periodos de "{{ gestion()?.nombre || '…' }}"</h2>
        @if (gestion()) {
          <p style="color: #666; font-size: 0.9rem;">
            {{ gestion()!.fechaInicio }} — {{ gestion()!.fechaFin }}
            · estado {{ gestion()!.estado }}
          </p>
        }

        @if (loading()) {
          <p>Cargando periodos...</p>
        }

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        @if (!loading() && periodos().length === 0) {
          <p>Esta gestión todavía no tiene periodos.</p>
        }

        @if (periodos().length > 0) {
          <table style="width: 100%; border-collapse: collapse; margin-bottom: 1.5rem;">
            <thead>
              <tr style="background: #f5f5f5;">
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">#</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Inicio</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Fin</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
                @if (esAdmin()) {
                  <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
                }
              </tr>
            </thead>
            <tbody>
              @for (periodo of periodos(); track periodo.id) {
                <tr style="border-bottom: 1px solid #eee;">
                  <td style="padding: 0.5rem;">{{ periodo.orden }}</td>
                  <td style="padding: 0.5rem;">{{ periodo.nombre }}</td>
                  <td style="padding: 0.5rem;">{{ periodo.fechaInicio }}</td>
                  <td style="padding: 0.5rem;">{{ periodo.fechaFin }}</td>
                  <td style="padding: 0.5rem;">
                    <span [style.color]="estadoColor(periodo.estado)">{{ periodo.estado }}</span>
                  </td>
                  @if (esAdmin()) {
                    <td style="padding: 0.5rem;">
                      <button (click)="editar(periodo)" [disabled]="saving()" style="cursor: pointer; font-size: 0.85rem; margin-right: 0.4rem;">
                        Editar
                      </button>
                      @if (periodo.estado !== 'ABIERTO') {
                        <button (click)="cambiarEstado(periodo, 'ABIERTO')" [disabled]="saving()" style="cursor: pointer; font-size: 0.85rem; margin-right: 0.4rem;">
                          Abrir
                        </button>
                      } @else {
                        <button (click)="cambiarEstado(periodo, 'CERRADO')" [disabled]="saving()" style="cursor: pointer; font-size: 0.85rem; margin-right: 0.4rem;">
                          Cerrar
                        </button>
                      }
                      <button (click)="eliminar(periodo)" [disabled]="saving()" style="cursor: pointer; font-size: 0.85rem; color: #c62828;">
                        Eliminar
                      </button>
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        }

        @if (esAdmin()) {
          <div style="background: #fafafa; padding: 1rem; border-radius: 4px;">
            <h3 style="margin-top: 0;">Nuevo periodo</h3>
            <form (ngSubmit)="onSubmit()" style="display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: end;">
              <label style="display: flex; flex-direction: column; font-size: 0.85rem;">
                Nombre
                <input type="text" [(ngModel)]="nuevoNombre" name="nuevoNombre" required style="padding: 0.4rem;" />
              </label>
              <label style="display: flex; flex-direction: column; font-size: 0.85rem;">
                Inicio
                <input type="date" [(ngModel)]="nuevaFechaInicio" name="nuevaFechaInicio" required style="padding: 0.4rem;" />
              </label>
              <label style="display: flex; flex-direction: column; font-size: 0.85rem;">
                Fin
                <input type="date" [(ngModel)]="nuevaFechaFin" name="nuevaFechaFin" required style="padding: 0.4rem;" />
              </label>
              <button type="submit" [disabled]="saving()" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; border: none; cursor: pointer;">
                {{ saving() ? 'Guardando...' : 'Agregar' }}
              </button>
            </form>
          </div>
        }
      }

      @if (editDialog()) {
        <div style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.4);display:flex;align-items:center;justify-content:center;">
          <div style="background:white;padding:2rem;border-radius:8px;min-width:320px;">
            <h3>Editar "{{ editDialog()!.nombre }}"</h3>
            <div style="display:flex;flex-direction:column;gap:0.5rem;margin:1rem 0;">
              <label style="display:flex;flex-direction:column;font-size:0.85rem;">
                Nombre
                <input type="text" [(ngModel)]="editNombre" style="padding:0.4rem;" />
              </label>
              <label style="display:flex;flex-direction:column;font-size:0.85rem;">
                Fecha inicio
                <input type="date" [(ngModel)]="editFechaInicio" style="padding:0.4rem;" />
              </label>
              <label style="display:flex;flex-direction:column;font-size:0.85rem;">
                Fecha fin
                <input type="date" [(ngModel)]="editFechaFin" style="padding:0.4rem;" />
              </label>
            </div>
            @if (editError()) {
              <p style="color:#c62828;">{{ editError() }}</p>
            }
            <div style="display:flex;gap:0.5rem;justify-content:flex-end;">
              <button (click)="cerrarEditDialog()">Cancelar</button>
              <button (click)="confirmarEdicion()" [disabled]="editSaving()"
                      style="background:#1e3a5f;color:white;padding:0.4rem 1rem;cursor:pointer;">
                {{ editSaving() ? 'Guardando...' : 'Guardar' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
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
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.gestionId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargar();
  }

  estadoColor(estado: string): string {
    return estado === 'ABIERTO' ? '#2e7d32' : estado === 'PENDIENTE' ? '#e65100' : '#757575';
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

  cambiarEstado(periodo: PeriodoEvaluacionResponse, estado: 'ABIERTO' | 'CERRADO'): void {
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
    return 'No se pudo completar la operación.';
  }
}
