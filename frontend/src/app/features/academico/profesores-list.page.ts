import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProfesorResponse } from './profesor.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Lista de Profesores — consola Admin/Secretaria (DD-UC-014).
 * GET /api/v1/profesores con filtro `q`/`activo` y paginación (DD-UC-007).
 * Sin página /nuevo: el alta es FSD-UC-021 (solo ADMIN ve el enlace a Usuarios).
 */
@Component({
  selector: 'app-profesores-list-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 1000px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2>Profesores</h2>
      </div>

      @if (auth.hasRole('ADMIN')) {
        <p style="background: #f5f5f5; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          El alta de un profesor se hace creando un usuario con rol PROFESOR.
          <a routerLink="/usuarios/nuevo">Crear usuario</a>
        </p>
      }

      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-bottom: 1rem; background: #fafafa; padding: 0.75rem; border-radius: 4px;">
        <input
          type="text"
          placeholder="Buscar por nombre o email..."
          [(ngModel)]="filtroQ"
          (keyup.enter)="aplicarFiltros()"
          style="padding: 0.4rem; flex: 1; min-width: 200px;"
        />
        <select [(ngModel)]="filtroActivo" style="padding: 0.4rem;">
          <option value="">Todos</option>
          <option value="true">Activos</option>
          <option value="false">Inactivos</option>
        </select>
        <button (click)="aplicarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Buscar</button>
        <button (click)="limpiarFiltros()" style="padding: 0.4rem 1rem; cursor: pointer;">Limpiar</button>
      </div>

      @if (loading()) {
        <p>Cargando profesores...</p>
      }

      @if (errorMsg()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
          {{ errorMsg() }}
        </div>
      }

      @if (!loading() && profesores().length === 0 && !errorMsg()) {
        <p>No hay profesores que coincidan con los filtros.</p>
      }

      @if (profesores().length > 0) {
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background: #f5f5f5;">
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
              <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (profesor of profesores(); track profesor.id) {
              <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 0.5rem;">{{ profesor.nombreCompleto }}</td>
                <td style="padding: 0.5rem;">{{ profesor.activo ? 'Activo' : 'Inactivo' }}</td>
                <td style="padding: 0.5rem;">
                  <a [routerLink]="['/academico/profesores', profesor.id]" style="font-size: 0.85rem;">
                    Ver asignaciones
                  </a>
                </td>
              </tr>
            }
          </tbody>
        </table>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem;">
          <span style="color: #666; font-size: 0.9rem;">
            {{ totalElements() }} profesor(es) — página {{ page() + 1 }} de {{ totalPaginas() || 1 }}
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
export class ProfesoresListPage implements OnInit {
  profesores = signal<ProfesorResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  filtroQ = '';
  filtroActivo = '';
  page = signal(0);
  totalElements = signal(0);
  totalPaginas = signal(0);
  private readonly tamanoPagina = 20;

  constructor(
    private http: HttpClient,
    protected auth: AuthService
  ) {}

  ngOnInit(): void {
    this.cargarProfesores();
  }

  aplicarFiltros(): void {
    this.page.set(0);
    this.cargarProfesores();
  }

  limpiarFiltros(): void {
    this.filtroQ = '';
    this.filtroActivo = '';
    this.page.set(0);
    this.cargarProfesores();
  }

  irAPagina(nuevaPagina: number): void {
    if (nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.page.set(nuevaPagina);
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
        this.totalPaginas.set(respuesta.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cargar los profesores.');
        this.loading.set(false);
      },
    });
  }
}
