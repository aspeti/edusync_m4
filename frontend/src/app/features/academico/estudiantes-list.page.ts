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
import { EstudianteResponse } from './estudiante.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/** Lista de Estudiantes (DD-UC-013) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-estudiantes-list-page',
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
        <h2 data-testid="estudiantes-heading">Estudiantes</h2>
        <p-button label="+ Nuevo Estudiante" (onClick)="irNuevo()" />
      </div>

      <div class="filters">
        <input
          pInputText
          type="text"
          placeholder="Buscar por nombre o RUDE..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          class="filter-q"
        />
        <p-select
          [options]="estadoOptions"
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
        [value]="estudiantes()"
        dataKey="id"
        [lazy]="true"
        [paginator]="true"
        [rows]="tamanoPagina"
        [totalRecords]="totalElements()"
        [first]="page() * tamanoPagina"
        [loading]="loading()"
        (onPage)="onPage($event)"
        [tableStyle]="{ 'min-width': '40rem' }"
        styleClass="p-datatable-sm"
      >
        <ng-template #header>
          <tr>
            <th>Nombre</th>
            <th>RUDE</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-estudiante>
          <tr>
            <td>{{ estudiante.nombreCompleto }}</td>
            <td>{{ estudiante.rude }}</td>
            <td>
              <p-tag
                [value]="estudiante.estado"
                [severity]="estudiante.estado === 'ACTIVO' ? 'success' : 'secondary'"
              />
            </td>
            <td>
              <p-button
                label="Ver historial"
                [link]="true"
                size="small"
                (onClick)="irDetalle(estudiante.id)"
              />
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="4">No hay estudiantes que coincidan con los filtros.</td>
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
    `,
  ],
})
export class EstudiantesListPage implements OnInit {
  estudiantes = signal<EstudianteResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  filtroQ = '';
  filtroEstado: string | null = null;
  page = signal(0);
  totalElements = signal(0);
  readonly tamanoPagina = 20;
  readonly estadoOptions = [
    { label: 'ACTIVO', value: 'ACTIVO' },
    { label: 'INACTIVO', value: 'INACTIVO' },
  ];

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cargarEstudiantes();
  }

  irNuevo(): void {
    this.router.navigate(['/academico/estudiantes/nuevo']);
  }

  irDetalle(id: string): void {
    this.router.navigate(['/academico/estudiantes', id]);
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarEstudiantes();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroEstado = null;
    this.page.set(0);
    this.cargarEstudiantes();
  }

  onPage(event: TablePageEvent): void {
    const rows = event.rows ?? this.tamanoPagina;
    this.page.set(Math.floor((event.first ?? 0) / rows));
    this.cargarEstudiantes();
  }

  cargarEstudiantes(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());
    if (this.filtroEstado) params = params.set('estado', this.filtroEstado);

    this.http.get<PageResponse<EstudianteResponse>>(`${ApiBase.BASE}/estudiantes`, { params }).subscribe({
      next: (respuesta) => {
        this.estudiantes.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los estudiantes.');
        this.loading.set(false);
      },
    });
  }
}
