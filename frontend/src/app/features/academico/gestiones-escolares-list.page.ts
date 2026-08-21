import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Página de lista de Gestiones Escolares — consola Admin de tenant.
 * GET /api/v1/gestiones-escolares (DD-UC-008 §2), con filtros y paginación
 * (DD-UC-007, patron reutilizable): `q` busca por nombre, `estado` es un filtro exacto.
 *
 * A diferencia del dialogo de cambio de estado de Tenant (que ofrece las 3
 * opciones siempre porque cualquier transicion es valida alli), aqui el
 * dialogo solo ofrece las transiciones validas del estado actual
 * (transicionesValidas), reflejando la maquina de estados de
 * GestionEscolar.cambiarEstado() (DD-UC-009 §2/§3).
 */
@Component({
  selector: 'app-gestiones-escolares-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 1000px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Gestión Escolar</h2>
        <a routerLink="/academico/gestiones-escolares/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
          + Nueva Gestión Escolar
        </a>
      </div>

      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-bottom: 1rem; background: #fafafa; padding: 0.75rem; border-radius: 4px;">
        <input
          type="text"
          placeholder="Buscar por nombre..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          style="padding: 0.4rem; flex: 1; min-width: 200px;"
        />
        <select [(ngModel)]="filtroEstado" style="padding: 0.4rem;">
          <option value="">Todos los estados</option>
          @for (op of estadoOpciones; track op) {
            <option [value]="op">{{ op }}</option>
          }
        </select>
        <button (click)="aplicarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Buscar</button>
        <button (click)="limpiarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Limpiar</button>
      </div>

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
                  @if (transicionesValidas(gestion.estado).length > 0) {
                    <button (click)="cambiarEstado(gestion)" style="cursor: pointer; font-size: 0.85rem;">
                      Cambiar estado
                    </button>
                  } @else {
                    <span style="color: #999; font-size: 0.85rem;">Sin transiciones</span>
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

  // Filtros (DD-UC-007): `q` busca por nombre; `estado` es un filtro exacto.
  filtroQ = '';
  filtroEstado = '';

  // Paginación (DD-UC-007).
  page = signal(0);
  totalElements = signal(0);
  totalPaginas = signal(0);
  private readonly tamanoPagina = 20;

  readonly estadoOpciones = ['PLANIFICACION', 'ACTIVA', 'CERRADA'];

  constructor(private http: HttpClient) {}

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
   * Refleja la maquina de estados de GestionEscolar.cambiarEstado() (backend,
   * DD-UC-008): solo estas transiciones son validas. CERRADA no tiene salida
   * en este slice.
   */
  transicionesValidas(estadoActual: string): string[] {
    switch (estadoActual) {
      case 'PLANIFICACION':
        return ['ACTIVA'];
      case 'ACTIVA':
        return ['CERRADA', 'PLANIFICACION'];
      default:
        return [];
    }
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
}
