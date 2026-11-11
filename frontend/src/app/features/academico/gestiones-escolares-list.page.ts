import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule, TablePageEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { RadioButtonModule } from 'primeng/radiobutton';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Lista de Gestiones Escolares (DD-UC-008/007/019) — UI PrimeNG (ADR-0026).
 * Contratos HTTP y reglas ADMIN vs solo-lectura sin cambio.
 */
@Component({
  selector: 'app-gestiones-escolares-list-page',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    SelectModule,
    DialogModule,
    TagModule,
    MessageModule,
    RadioButtonModule,
  ],
  template: `
    <div class="page">
      <div class="toolbar">
        <h2 data-testid="gestiones-heading">Gestión Escolar</h2>
        @if (esAdmin()) {
          <p-button label="+ Nueva Gestión Escolar" (onClick)="irNuevo()" />
        }
      </div>

      <div class="filters">
        <input
          pInputText
          type="text"
          placeholder="Buscar por nombre..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          class="filter-q"
        />
        @if (esAdmin()) {
          <p-select
            [options]="estadoSelectOptions"
            [(ngModel)]="filtroEstado"
            optionLabel="label"
            optionValue="value"
            placeholder="Todos los estados"
            [showClear]="true"
            styleClass="filter-select"
          />
        }
        <p-button label="Buscar" (onClick)="aplicarFiltros()" />
        <p-button label="Limpiar" severity="secondary" [outlined]="true" (onClick)="limpiarFiltros()" />
      </div>

      @if (!esAdmin()) {
        <p class="hint">Solo se muestra la gestión escolar actual (estado ACTIVA).</p>
      }

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <p-table
        [value]="gestiones()"
        dataKey="id"
        [lazy]="true"
        [paginator]="true"
        [rows]="tamanoPagina"
        [totalRecords]="totalElements()"
        [first]="page() * tamanoPagina"
        [loading]="loading()"
        (onPage)="onPage($event)"
        [tableStyle]="{ 'min-width': '50rem' }"
        styleClass="p-datatable-sm"
      >
        <ng-template #header>
          <tr>
            <th>Nombre</th>
            <th>Estado</th>
            <th>Fecha inicio</th>
            <th>Fecha fin</th>
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-gestion>
          <tr>
            <td>{{ gestion.nombre }}</td>
            <td>
              <p-tag [value]="gestion.estado" [severity]="estadoSeverity(gestion.estado)" />
            </td>
            <td>{{ gestion.fechaInicio }}</td>
            <td>{{ gestion.fechaFin }}</td>
            <td class="actions">
              <p-button
                label="Periodos"
                [link]="true"
                size="small"
                (onClick)="irPeriodos(gestion.id)"
              />
              <p-button
                label="Secciones"
                [link]="true"
                size="small"
                (onClick)="irSecciones(gestion.id)"
              />
              @if (esAdmin()) {
                <p-button label="Editar" [link]="true" size="small" (onClick)="editar(gestion)" />
                <p-button
                  label="Cambiar estado"
                  [link]="true"
                  size="small"
                  (onClick)="cambiarEstado(gestion)"
                />
              }
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="5">No hay gestiones escolares que coincidan con los filtros.</td>
          </tr>
        </ng-template>
      </p-table>

      <p-dialog
        header="Cambiar estado"
        [modal]="true"
        [visible]="estadoDialog() !== null"
        (visibleChange)="onEstadoDialogVisible($event)"
        [style]="{ width: '24rem' }"
      >
        @if (estadoDialog(); as g) {
          <p>Gestión: <strong>{{ g.nombre }}</strong></p>
          <p class="hint">Estado actual: {{ g.estado }}</p>
          <div class="radio-list">
            @for (op of opcionesDialog(); track op) {
              <div class="radio-row">
                <p-radiobutton
                  name="estado"
                  [value]="op"
                  [(ngModel)]="nuevoEstado"
                  [inputId]="'estado-' + op"
                />
                <label [for]="'estado-' + op">{{ op }}</label>
              </div>
            }
          </div>
          @if (estadoError()) {
            <p-message severity="error" [text]="estadoError()!" styleClass="block-msg" />
          }
          <div class="dialog-actions">
            <p-button label="Cancelar" severity="secondary" [text]="true" (onClick)="cerrarDialog()" />
            <p-button
              [label]="estadoSaving() ? 'Guardando...' : 'Confirmar'"
              [disabled]="!nuevoEstado || estadoSaving()"
              (onClick)="confirmarEstado()"
            />
          </div>
        }
      </p-dialog>

      <p-dialog
        header="Editar gestión"
        [modal]="true"
        [visible]="editDialog() !== null"
        (visibleChange)="onEditDialogVisible($event)"
        [style]="{ width: '24rem' }"
      >
        @if (editDialog(); as g) {
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
        max-width: 1000px;
        margin: 0 auto;
      }
      .toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--space-md);
        gap: var(--space-sm);
      }
      .filters {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
        align-items: center;
        margin-bottom: var(--space-md);
        padding: var(--space-sm);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }
      .filter-q {
        flex: 1;
        min-width: 12rem;
      }
      :host ::ng-deep .filter-select {
        min-width: 10rem;
      }
      .hint {
        color: var(--color-text-secondary);
        font-size: var(--font-size-small);
        margin: 0 0 var(--space-md);
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .actions {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-xs);
      }
      .radio-list {
        display: flex;
        flex-direction: column;
        gap: var(--space-sm);
        margin: var(--space-md) 0;
      }
      .radio-row {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
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

  filtroQ = '';
  filtroEstado: string | null = null;

  page = signal(0);
  totalElements = signal(0);
  readonly tamanoPagina = 20;

  readonly estadoOpciones = ['PLANIFICACION', 'ACTIVA', 'CERRADA'];
  readonly estadoSelectOptions = this.estadoOpciones.map((e) => ({ label: e, value: e }));

  readonly esAdmin = computed(() => this.auth.hasRole('ADMIN'));

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cargarGestiones();
  }

  irNuevo(): void {
    this.router.navigate(['/academico/gestiones-escolares/nuevo']);
  }

  irPeriodos(id: string): void {
    this.router.navigate(['/academico/gestiones-escolares', id, 'periodos']);
  }

  irSecciones(id: string): void {
    this.router.navigate(['/academico/gestiones-escolares', id, 'secciones']);
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarGestiones();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroEstado = null;
    this.page.set(0);
    this.cargarGestiones();
  }

  onPage(event: TablePageEvent): void {
    const rows = event.rows ?? this.tamanoPagina;
    this.page.set(Math.floor((event.first ?? 0) / rows));
    this.cargarGestiones();
  }

  cargarGestiones(): void {
    this.loading.set(true);
    this.errorMsg.set(null);

    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());
    if (this.filtroEstado) params = params.set('estado', this.filtroEstado);

    this.http
      .get<PageResponse<GestionEscolarResponse>>(`${ApiBase.BASE}/gestiones-escolares`, { params })
      .subscribe({
        next: (respuesta) => {
          this.gestiones.set(respuesta.content);
          this.totalElements.set(respuesta.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.errorMsg.set('Error al cargar las gestiones escolares.');
          this.loading.set(false);
        },
      });
  }

  estadoSeverity(estado: string): 'success' | 'warn' | 'secondary' {
    if (estado === 'ACTIVA') return 'success';
    if (estado === 'PLANIFICACION') return 'warn';
    return 'secondary';
  }

  transicionesValidas(estadoActual: string): string[] {
    return this.estadoOpciones.filter((op) => op !== estadoActual);
  }

  cambiarEstado(gestion: GestionEscolarResponse): void {
    this.opcionesDialog.set(this.transicionesValidas(gestion.estado));
    this.nuevoEstado = '';
    this.estadoError.set(null);
    this.estadoDialog.set(gestion);
  }

  onEstadoDialogVisible(visible: boolean): void {
    if (!visible) this.cerrarDialog();
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

  onEditDialogVisible(visible: boolean): void {
    if (!visible) this.cerrarEditDialog();
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
              : 'Error al guardar los cambios.',
          );
          this.editSaving.set(false);
        },
      });
  }
}
