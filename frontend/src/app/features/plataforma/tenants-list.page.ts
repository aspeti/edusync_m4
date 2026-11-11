import { Component, OnInit, signal } from '@angular/core';
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
import { TenantResponse } from './tenant.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Lista de Tenants (DD-UC-004/007) — UI PrimeNG (ADR-0026).
 * Contratos HTTP sin cambio.
 */
@Component({
  selector: 'app-tenants-list-page',
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
        <h2 data-testid="tenants-heading">Tenants</h2>
        <p-button label="+ Nuevo Tenant" (onClick)="irNuevo()" />
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
        <p-select
          [options]="estadoSelectOptions"
          [(ngModel)]="filtroEstado"
          optionLabel="label"
          optionValue="value"
          placeholder="Todos los estados"
          [showClear]="true"
          styleClass="filter-select"
        />
        <p-button label="Buscar" (onClick)="aplicarFiltros()" />
        <p-button label="Limpiar" severity="secondary" [outlined]="true" (onClick)="limpiarFiltros()" />
      </div>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <p-table
        [value]="tenants()"
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
            <th>Inicio suscripción</th>
            <th>Vencimiento</th>
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-tenant>
          <tr>
            <td>{{ tenant.nombre }}</td>
            <td>
              <p-tag [value]="tenant.estado" [severity]="estadoSeverity(tenant.estado)" />
            </td>
            <td>{{ tenant.fechaInicioSuscripcion }}</td>
            <td>{{ tenant.fechaVencimientoSuscripcion }}</td>
            <td class="actions">
              <p-button
                label="Crear admin"
                [link]="true"
                size="small"
                (onClick)="irAdmin(tenant.id)"
              />
              <p-button label="Cambiar estado" [link]="true" size="small" (onClick)="cambiarEstado(tenant)" />
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="5">No hay tenants que coincidan con los filtros.</td>
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
        @if (estadoDialog(); as t) {
          <p>Tenant: <strong>{{ t.nombre }}</strong></p>
          <div class="estado-list">
            @for (op of estadoOpciones; track op) {
              <div class="estado-row">
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
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .actions {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-xs);
        align-items: center;
      }
      .estado-list {
        display: flex;
        flex-direction: column;
        gap: var(--space-sm);
        margin: var(--space-md) 0;
      }
      .estado-row {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
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
export class TenantsListPage implements OnInit {
  tenants = signal<TenantResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  estadoDialog = signal<TenantResponse | null>(null);
  nuevoEstado = '';
  estadoError = signal<string | null>(null);
  estadoSaving = signal(false);

  filtroQ = '';
  filtroEstado: string | null = null;

  page = signal(0);
  totalElements = signal(0);
  readonly tamanoPagina = 20;

  readonly estadoOpciones = ['ACTIVO', 'SUSPENDIDO', 'VENCIDO'];
  readonly estadoSelectOptions = this.estadoOpciones.map((e) => ({ label: e, value: e }));

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cargarTenants();
  }

  irNuevo(): void {
    this.router.navigate(['/plataforma/tenants/nuevo']);
  }

  irAdmin(tenantId: string): void {
    this.router.navigate(['/plataforma/tenants', tenantId, 'admin']);
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarTenants();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroEstado = null;
    this.page.set(0);
    this.cargarTenants();
  }

  onPage(event: TablePageEvent): void {
    const rows = event.rows ?? this.tamanoPagina;
    this.page.set(Math.floor((event.first ?? 0) / rows));
    this.cargarTenants();
  }

  cargarTenants(): void {
    this.loading.set(true);
    this.errorMsg.set(null);

    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());
    if (this.filtroEstado) params = params.set('estado', this.filtroEstado);

    this.http.get<PageResponse<TenantResponse>>(`${ApiBase.BASE}/plataforma/tenants`, { params }).subscribe({
      next: (respuesta) => {
        this.tenants.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los tenants.');
        this.loading.set(false);
      },
    });
  }

  estadoSeverity(estado: string): 'success' | 'warn' | 'secondary' {
    if (estado === 'ACTIVO') return 'success';
    if (estado === 'SUSPENDIDO') return 'warn';
    return 'secondary';
  }

  cambiarEstado(tenant: TenantResponse): void {
    this.nuevoEstado = tenant.estado;
    this.estadoError.set(null);
    this.estadoDialog.set(tenant);
  }

  onEstadoDialogVisible(visible: boolean): void {
    if (!visible) this.cerrarDialog();
  }

  cerrarDialog(): void {
    this.estadoDialog.set(null);
    this.nuevoEstado = '';
  }

  confirmarEstado(): void {
    const tenant = this.estadoDialog();
    if (!tenant || !this.nuevoEstado) return;
    this.estadoSaving.set(true);
    this.estadoError.set(null);

    this.http
      .patch<TenantResponse>(`${ApiBase.BASE}/plataforma/tenants/${tenant.id}/estado`, {
        estado: this.nuevoEstado,
      })
      .subscribe({
        next: (updated) => {
          this.tenants.update((list) => list.map((t) => (t.id === updated.id ? updated : t)));
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
