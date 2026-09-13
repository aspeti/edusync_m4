import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Página de lista de Gestiones Escolares.
 * GET /api/v1/gestiones-escolares (DD-UC-008 §2), con filtros y paginación
 * (DD-UC-007, patron reutilizable): `q` busca por nombre, `estado` es un filtro exacto.
 *
 * DD-UC-019: `ADMIN` ve y edita cualquier gestión (nombre/fechas/estado, sin
 * restricción de transición); `SECRETARIA`/`PROFESOR`/`ASESOR` solo ven la
 * gestión "actual" (backend fuerza `estado=ACTIVA`) y no tienen acciones de
 * escritura — ni el botón "+ Nueva Gestión", ni "Editar", ni "Cambiar estado".
 */
@Component({
  selector: 'app-gestiones-escolares-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 1000px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Gestión Escolar</h2>
        @if (esAdmin()) {
          <a routerLink="/academico/gestiones-escolares/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
            + Nueva Gestión Escolar
          </a>
        }
      </div>

      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-bottom: 1rem; background: #fafafa; padding: 0.75rem; border-radius: 4px;">
        <input
          type="text"
          placeholder="Buscar por nombre..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          style="padding: 0.4rem; flex: 1; min-width: 200px;"
        />
        @if (esAdmin()) {
          <select [(ngModel)]="filtroEstado" style="padding: 0.4rem;">
            <option value="">Todos los estados</option>
            @for (op of estadoOpciones; track op) {
              <option [value]="op">{{ op }}</option>
            }
          </select>
        }
        <button (click)="aplicarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Buscar</button>
        <button (click)="limpiarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Limpiar</button>
      </div>

      @if (!esAdmin()) {
        <p style="color: #666; font-size: 0.85rem; margin-top: -0.5rem;">
          Solo se muestra la gestión escolar actual (estado ACTIVA).
        </p>
      }

      @if (loading()) {
        <p>Cargando gestiones escolares...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && gestiones().length === 0 && !errorMsg()) {
        <p>No hay gestiones escolares que coincidan con los filtros.</p>
      }

      @if (gestiones().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Fecha inicio</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Fecha fin</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (gestion of gestiones(); track gestion.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ gestion.nombre }}</td>
                <td style="padding: 0.5rem;">
                  <span [style.color]="estadoColor(gestion.estado)">{{ gestion.estado }}</span>
                </td>
                <td style="padding: 0.5rem;">{{ gestion.fechaInicio }}</td>
                <td style="padding: 0.5rem;">{{ gestion.fechaFin }}</td>
                <td style="padding: 0.5rem;">
                  <a [routerLink]="['/academico/gestiones-escolares', gestion.id, 'periodos']" style="margin-right: 0.5rem; font-size: 0.85rem;">
                    Periodos
                  </a>
                  <a [routerLink]="['/academico/gestiones-escolares', gestion.id, 'secciones']" style="margin-right: 0.5rem; font-size: 0.85rem;">
                    Secciones
                  </a>
                  @if (esAdmin()) {
                    <button (click)="editar(gestion)" style="cursor: pointer; font-size: 0.85rem; margin-right: 0.5rem;">
                      Editar
                    </button>
                    <button (click)="cambiarEstado(gestion)" style="cursor: pointer; font-size: 0.85rem;">
                      Cambiar estado
                    </button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem;">
          <span style="color: #666; font-size: 0.9rem;">
            {{ totalElements() }} gestión(es) — página {{ page() + 1 }} de {{ totalPaginas() || 1 }}
          </span>
          <div style="display: flex; gap: 0.5rem;">
            <button (click)="irAPagina(page() - 1)" [disabled]="page() === 0" style="padding: 0.3rem 0.8rem; cursor: pointer;">
              ← Anterior
            </button>
            <button (click)="irAPagina(page() + 1)" [disabled]="page() + 1 >= totalPaginas()" style="padding: 0.3rem 0.8rem; cursor: pointer;">
              Siguiente →
            </button>
          </div>
        </div>
      }

      @if (estadoDialog()) {
        <div style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.4);display:flex;align-items:center;justify-content:center;">
          <div style="background:white;padding:2rem;border-radius:8px;min-width:320px;">
            <h3>Cambiar estado de "{{ estadoDialog()!.nombre }}"</h3>
            <p style="color:#666;font-size:0.85rem;">Estado actual: {{ estadoDialog()!.estado }}</p>
            <div style="display:flex;flex-direction:column;gap:0.5rem;margin:1rem 0;">
              @for (op of opcionesDialog(); track op) {
                <label style="cursor:pointer;">
                  <input type="radio" name="estado" [value]="op" [(ngModel)]="nuevoEstado" /> {{ op }}
                </label>
              }
            </div>
            @if (estadoError()) {
              <p style="color:#c62828;">{{ estadoError() }}</p>
            }
            <div style="display:flex;gap:0.5rem;justify-content:flex-end;">
              <button (click)="cerrarDialog()">Cancelar</button>
              <button (click)="confirmarEstado()" [disabled]="!nuevoEstado || estadoSaving()"
                      style="background:#1e3a5f;color:white;padding:0.4rem 1rem;cursor:pointer;">
                {{ estadoSaving() ? 'Guardando...' : 'Confirmar' }}
              </button>
            </div>
          </div>
        </div>
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
export class GestionesEscolaresListPage implements OnInit {
  gestiones = signal<GestionEscolarResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  estadoDialog = signal<GestionEscolarResponse | null>(null);
  opcionesDialog = signal<string[]>([]);
  nuevoEstado = '';
  estadoError = signal<string | null>(null);
  estadoSaving = signal(false);

  editDialog = signal<GestionEscolarResponse | null>(null);
  editNombre = '';
  editFechaInicio = '';
  editFechaFin = '';
  editError = signal<string | null>(null);
  editSaving = signal(false);

  // Filtros (DD-UC-007): `q` busca por nombre; `estado` es un filtro exacto (solo ADMIN,
  // DD-UC-019: para el resto de roles el backend fuerza estado=ACTIVA sin importar el filtro).
  filtroQ = '';
  filtroEstado = '';

  // Paginación (DD-UC-007).
  page = signal(0);
  totalElements = signal(0);
  totalPaginas = signal(0);
  private readonly tamanoPagina = 20;

  readonly estadoOpciones = ['PLANIFICACION', 'ACTIVA', 'CERRADA'];

  readonly esAdmin = computed(() => this.auth.hasRole('ADMIN'));

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit(): void {
    this.cargarGestiones();
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarGestiones();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroEstado = '';
    this.page.set(0);
    this.cargarGestiones();
  }

  irAPagina(nuevaPagina: number): void {
    if (nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.page.set(nuevaPagina);
    this.cargarGestiones();
  }

  cargarGestiones(): void {
    this.loading.set(true);
    this.errorMsg.set(null);

    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());
    if (this.filtroEstado) params = params.set('estado', this.filtroEstado);

    this.http.get<PageResponse<GestionEscolarResponse>>(`${ApiBase.BASE}/gestiones-escolares`, { params }).subscribe({
      next: (respuesta) => {
        this.gestiones.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
        this.totalPaginas.set(respuesta.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar las gestiones escolares.');
        this.loading.set(false);
      },
    });
  }

  estadoColor(estado: string): string {
    return estado === 'ACTIVA' ? '#2e7d32' : estado === 'PLANIFICACION' ? '#e65100' : '#757575';
  }

  /**
   * DD-UC-019: el endpoint `PATCH /estado` es exclusivamente ADMIN y ya no tiene
   * una máquina de estados restringida — se puede transicionar a cualquier
   * estado desde cualquier estado (incluida una gestión CERRADA, antes terminal).
   */
  transicionesValidas(estadoActual: string): string[] {
    return this.estadoOpciones.filter((op) => op !== estadoActual);
  }

  cambiarEstado(gestion: GestionEscolarResponse): void {
    this.opcionesDialog.set(this.transicionesValidas(gestion.estado));
    this.nuevoEstado = '';
    this.estadoError.set(null);
    this.estadoDialog.set(gestion);
  }

  cerrarDialog(): void {
    this.estadoDialog.set(null);
    this.nuevoEstado = '';
  }

  confirmarEstado(): void {
    const gestion = this.estadoDialog();
    if (!gestion || !this.nuevoEstado) return;
    this.estadoSaving.set(true);
    this.estadoError.set(null);

    this.http
      .patch<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/${gestion.id}/estado`, {
        estado: this.nuevoEstado,
      })
      .subscribe({
        next: (updated) => {
          this.gestiones.update((list) => list.map((g) => (g.id === updated.id ? updated : g)));
          this.estadoSaving.set(false);
          this.cerrarDialog();
        },
        error: () => {
          this.estadoError.set('Error al cambiar el estado.');
          this.estadoSaving.set(false);
        },
      });
  }

  editar(gestion: GestionEscolarResponse): void {
    this.editNombre = gestion.nombre;
    this.editFechaInicio = gestion.fechaInicio;
    this.editFechaFin = gestion.fechaFin;
    this.editError.set(null);
    this.editDialog.set(gestion);
  }

  cerrarEditDialog(): void {
    this.editDialog.set(null);
  }

  confirmarEdicion(): void {
    const gestion = this.editDialog();
    if (!gestion) return;
    this.editSaving.set(true);
    this.editError.set(null);

    this.http
      .patch<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/${gestion.id}`, {
        nombre: this.editNombre,
        fechaInicio: this.editFechaInicio,
        fechaFin: this.editFechaFin,
      })
      .subscribe({
        next: (updated) => {
          this.gestiones.update((list) => list.map((g) => (g.id === updated.id ? updated : g)));
          this.editSaving.set(false);
          this.cerrarEditDialog();
        },
        error: (err) => {
          const codigo = err?.error?.codigo as string | undefined;
          this.editError.set(
            codigo === 'E_FECHAS_INVALIDAS'
              ? 'La fecha de fin debe ser posterior a la de inicio.'
              : 'Error al guardar los cambios.'
          );
          this.editSaving.set(false);
        },
      });
  }
}
