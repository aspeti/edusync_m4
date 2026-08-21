import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CursoResponse } from './curso.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Página de lista de Cursos — consola Admin de tenant.
 * GET /api/v1/cursos (DD-UC-010 §2), con filtro `q` y paginación (DD-UC-007,
 * patrón reutilizable). Sin `<select>` de estado: `Curso` no tiene estado
 * (DD-UC-010 §2, DD-UC-011 §2).
 */
@Component({
  selector: 'app-cursos-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 1000px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Cursos</h2>
        <a routerLink="/academico/cursos/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
          + Nuevo Curso
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
        <p>Cargando cursos...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && cursos().length === 0 && !errorMsg()) {
        <p>No hay cursos que coincidan con los filtros.</p>
      }

      @if (cursos().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (curso of cursos(); track curso.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ curso.nombre }}</td>
                <td style="padding: 0.5rem;">
                  <a [routerLink]="['/academico/cursos', curso.id, 'paralelos']" [queryParams]="{ nombre: curso.nombre }" style="font-size: 0.85rem;">
                    Ver paralelos
                  </a>
                </td>
              </tr>
            }
          </tbody>
        </table>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem;">
          <span style="color: #666; font-size: 0.9rem;">
            {{ totalElements() }} curso(s) — página {{ page() + 1 }} de {{ totalPaginas() || 1 }}
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
export class CursosListPage implements OnInit {
  cursos = signal<CursoResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  // Filtro (DD-UC-007): `q` busca por nombre. Sin filtro de estado (DD-UC-010 §2).
  filtroQ = '';

  // Paginación (DD-UC-007).
  page = signal(0);
  totalElements = signal(0);
  totalPaginas = signal(0);
  private readonly tamanoPagina = 20;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarCursos();
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarCursos();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.page.set(0);
    this.cargarCursos();
  }

  irAPagina(nuevaPagina: number): void {
    if (nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.page.set(nuevaPagina);
    this.cargarCursos();
  }

  cargarCursos(): void {
    this.loading.set(true);
    this.errorMsg.set(null);

    let params = new HttpParams().set('page', this.page()).set('size', this.tamanoPagina);
    if (this.filtroQ.trim()) params = params.set('q', this.filtroQ.trim());

    this.http.get<PageResponse<CursoResponse>>(`${ApiBase.BASE}/cursos`, { params }).subscribe({
      next: (respuesta) => {
        this.cursos.set(respuesta.content);
        this.totalElements.set(respuesta.totalElements);
        this.totalPaginas.set(respuesta.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los cursos.');
        this.loading.set(false);
      },
    });
  }
}
