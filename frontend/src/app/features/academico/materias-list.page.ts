import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule, TablePageEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { MateriaResponse } from './materia.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/** Lista de Materias (DD-UC-012) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-materias-list-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, MessageModule],
  template: `
    <div class="page">
      <div class="toolbar">
        <h2 data-testid="materias-heading">Materias</h2>
        <p-button label="+ Nueva Materia" (onClick)="irNuevo()" />
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
        <p-button label="Buscar" (onClick)="aplicarFiltros()" />
        <p-button label="Limpiar" severity="secondary" [outlined]="true" (onClick)="limpiarFiltros()" />
      </div>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <p-table
        [value]="materias()"
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
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-materia>
          <tr>
            <td>{{ materia.nombre }}</td>
            <td>
              <p-button
                label="Ver asignaciones"
                [link]="true"
                size="small"
                (onClick)="irDetalle(materia.id)"
              />
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="2">No hay materias que coincidan con los filtros.</td>
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
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
    `,
  ],
})
export class MateriasListPage implements OnInit {
  materias = signal<MateriaResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  filtroQ = '';
  page = signal(0);
  totalElements = signal(0);
  readonly tamanoPagina = 20;

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cargarMaterias();
  }

  irNuevo(): void {
    this.router.navigate(['/academico/materias/nuevo']);
  }

  irDetalle(id: string): void {
    this.router.navigate(['/academico/materias', id]);
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarMaterias();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.page.set(0);
    this.cargarMaterias();
  }

  onPage(event: TablePageEvent): void {
    const rows = event.rows ?? this.tamanoPagina;
    this.page.set(Math.floor((event.first ?? 0) / rows));
    this.cargarMaterias();
  }

  cargarMaterias(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());

    this.http.get<PageResponse<MateriaResponse>>(`${ApiBase.BASE}/materias`, { params }).subscribe({
      next: (respuesta) => {
        this.materias.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar las materias.');
        this.loading.set(false);
      },
    });
  }
}
