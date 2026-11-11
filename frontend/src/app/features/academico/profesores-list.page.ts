import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule, TablePageEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { ProfesorResponse } from './profesor.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/** Lista de Profesores (DD-UC-014) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-profesores-list-page',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    SelectModule,
    TagModule,
    MessageModule,
  ],
  template: `
    <div class="page">
      <div class="toolbar">
        <h2 data-testid="profesores-heading">Profesores</h2>
      </div>

      @if (auth.hasRole('ADMIN')) {
        <p-message
          severity="info"
          text="El alta de un profesor se hace creando un usuario con rol PROFESOR."
          styleClass="block-msg"
        />
        <p class="hint">
          <p-button label="Crear usuario" [link]="true" (onClick)="irNuevoUsuario()" />
        </p>
      }

      <div class="filters">
        <input
          pInputText
          type="text"
          placeholder="Buscar por nombre o email..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          class="filter-q"
        />
        <p-select
          [options]="activoOptions"
          [(ngModel)]="filtroActivo"
          optionLabel="label"
          optionValue="value"
          placeholder="Todos"
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
        [value]="profesores()"
        dataKey="id"
        [lazy]="true"
        [paginator]="true"
        [rows]="tamanoPagina"
        [totalRecords]="totalElements()"
        [first]="page() * tamanoPagina"
        [loading]="loading()"
        (onPage)="onPage($event)"
        [tableStyle]="{ 'min-width': '30rem' }"
        styleClass="p-datatable-sm"
      >
        <ng-template #header>
          <tr>
            <th>Nombre</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-profesor>
          <tr>
            <td>{{ profesor.nombreCompleto }}</td>
            <td>
              <p-tag
                [value]="profesor.activo ? 'Activo' : 'Inactivo'"
                [severity]="profesor.activo ? 'success' : 'secondary'"
              />
            </td>
            <td>
              <p-button
                label="Ver asignaciones"
                [link]="true"
                size="small"
                (onClick)="irDetalle(profesor.id)"
              />
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="3">No hay profesores que coincidan con los filtros.</td>
          </tr>
        </ng-template>
      </p-table>
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
      }
      .hint {
        margin: calc(-1 * var(--space-sm)) 0 var(--space-md);
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
    `,
  ],
})
export class ProfesoresListPage implements OnInit {
  profesores = signal<ProfesorResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  filtroQ = '';
  filtroActivo: string | null = null;
  page = signal(0);
  totalElements = signal(0);
  readonly tamanoPagina = 20;
  readonly activoOptions = [
    { label: 'Activos', value: 'true' },
    { label: 'Inactivos', value: 'false' },
  ];

  constructor(
    private http: HttpClient,
    private router: Router,
    protected auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.cargarProfesores();
  }

  irNuevoUsuario(): void {
    this.router.navigate(['/usuarios/nuevo']);
  }

  irDetalle(id: string): void {
    this.router.navigate(['/academico/profesores', id]);
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarProfesores();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroActivo = null;
    this.page.set(0);
    this.cargarProfesores();
  }

  onPage(event: TablePageEvent): void {
    const rows = event.rows ?? this.tamanoPagina;
    this.page.set(Math.floor((event.first ?? 0) / rows));
    this.cargarProfesores();
  }

  cargarProfesores(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());
    if (this.filtroActivo) params = params.set('activo', this.filtroActivo);

    this.http.get<PageResponse<ProfesorResponse>>(`${ApiBase.BASE}/profesores`, { params }).subscribe({
      next: (respuesta) => {
        this.profesores.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los profesores.');
        this.loading.set(false);
      },
    });
  }
}
