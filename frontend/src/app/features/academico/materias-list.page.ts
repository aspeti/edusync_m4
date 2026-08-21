import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MateriaResponse } from './materia.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Lista de Materias — consola Admin/Secretaria (DD-UC-012).
 * GET /api/v1/materias con filtro `q` y paginación (DD-UC-007).
 */
@Component({
  selector: 'app-materias-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 1000px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Materias</h2>
        <a routerLink="/academico/materias/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
          + Nueva Materia
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
        <button (click)="aplicarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Buscar</button>
        <button (click)="limpiarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Limpiar</button>
      </div>

      @if (loading()) {
        <p>Cargando materias...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && materias().length === 0 && !errorMsg()) {
        <p>No hay materias que coincidan con los filtros.</p>
      }

      @if (materias().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (materia of materias(); track materia.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ materia.nombre }}</td>
                <td style="padding: 0.5rem;">
                  <a [routerLink]="['/academico/materias', materia.id]" style="font-size: 0.85rem;">
                    Ver asignaciones
                  </a>
                </td>
              </tr>
            }
          </tbody>
        </table>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem;">
          <span style="color: #666; font-size: 0.9rem;">
            {{ totalElements() }} materia(s) — página {{ page() + 1 }} de {{ totalPaginas() || 1 }}
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
    </div>
  `,
})
export class MateriasListPage implements OnInit {
  materias = signal<MateriaResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  filtroQ = '';
  page = signal(0);
  totalElements = signal(0);
  totalPaginas = signal(0);
  private readonly tamanoPagina = 20;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarMaterias();
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

  irAPagina(nuevaPagina: number): void {
    if (nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.page.set(nuevaPagina);
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
        this.totalPaginas.set(respuesta.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar las materias.');
        this.loading.set(false);
      },
    });
  }
}
