import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EstudianteResponse } from './estudiante.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';

/**
 * Lista de Estudiantes — consola Admin/Secretaria (DD-UC-013).
 * GET /api/v1/estudiantes con filtro `q`/`estado` y paginación (DD-UC-007).
 */
@Component({
  selector: 'app-estudiantes-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 1000px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Estudiantes</h2>
        <a routerLink="/academico/estudiantes/nuevo" style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; text-decoration: none; border-radius: 4px;">
          + Nuevo Estudiante
        </a>
      </div>

      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-bottom: 1rem; background: #fafafa; padding: 0.75rem; border-radius: 4px;">
        <input
          type="text"
          placeholder="Buscar por nombre o RUDE..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          style="padding: 0.4rem; flex: 1; min-width: 200px;"
        />
        <select [(ngModel)]="filtroEstado" style="padding: 0.4rem;">
          <option value="">Todos los estados</option>
          <option value="ACTIVO">ACTIVO</option>
          <option value="INACTIVO">INACTIVO</option>
        </select>
        <button (click)="aplicarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Buscar</button>
        <button (click)="limpiarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Limpiar</button>
      </div>

      @if (loading()) {
        <p>Cargando estudiantes...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && estudiantes().length === 0 && !errorMsg()) {
        <p>No hay estudiantes que coincidan con los filtros.</p>
      }

      @if (estudiantes().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">RUDE</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (estudiante of estudiantes(); track estudiante.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ estudiante.nombreCompleto }}</td>
                <td style="padding: 0.5rem;">{{ estudiante.rude }}</td>
                <td style="padding: 0.5rem;">{{ estudiante.estado }}</td>
                <td style="padding: 0.5rem;">
                  <a [routerLink]="['/academico/estudiantes', estudiante.id]" style="font-size: 0.85rem;">
                    Ver historial
                  </a>
                </td>
              </tr>
            }
          </tbody>
        </table>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem;">
          <span style="color: #666; font-size: 0.9rem;">
            {{ totalElements() }} estudiante(s) — página {{ page() + 1 }} de {{ totalPaginas() || 1 }}
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
export class EstudiantesListPage implements OnInit {
  estudiantes = signal<EstudianteResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  filtroQ = '';
  filtroEstado = '';
  page = signal(0);
  totalElements = signal(0);
  totalPaginas = signal(0);
  private readonly tamanoPagina = 20;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarEstudiantes();
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarEstudiantes();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroEstado = '';
    this.page.set(0);
    this.cargarEstudiantes();
  }

  irAPagina(nuevaPagina: number): void {
    if (nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.page.set(nuevaPagina);
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
        this.totalPaginas.set(respuesta.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los estudiantes.');
        this.loading.set(false);
      },
    });
  }
}
