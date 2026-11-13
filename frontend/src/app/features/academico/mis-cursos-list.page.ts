import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { MessageModule } from 'primeng/message';
import { ApiBase } from '../../core/api/api-base';
import { MisAsignacionResponse } from './planilla.model';

/**
 * Lista de Cursos del PROFESOR (DD-UC-033 / FSD-UC-022).
 * Skin Figma nodo 774:35346; estilos compartidos en `styles/_figma-profesor.scss`.
 * Solo se muestran datos que entrega `GET /profesores/me/asignaciones`.
 */
@Component({
  selector: 'app-mis-cursos-list-page',
  standalone: true,
  imports: [FormsModule, TableModule, MessageModule],
  template: `
    <div class="fg-page">
      <nav class="fg-breadcrumb" aria-label="Breadcrumb">
        <span>Cursos</span>
        <svg class="sep" viewBox="0 0 4 7" aria-hidden="true">
          <path d="M.5.5l3 3-3 3" fill="none" stroke="currentColor" stroke-width="1" />
        </svg>
        <span class="actual">Lista</span>
      </nav>

      <div class="fg-main">
        <h1 class="fg-h1" data-testid="mis-cursos-heading">Lista de Cursos</h1>

        <section class="fg-stats" aria-label="Resumen">
          <div class="fg-stat">
            <div class="ico" aria-hidden="true">
              <svg width="20" height="16" viewBox="0 0 20 16" fill="currentColor">
                <path d="M10 0L0 5l10 5 8-4v6h2V5L10 0zM4 9.2v3.3c0 1.7 2.7 3.5 6 3.5s6-1.8 6-3.5V9.2l-6 3-6-3z" />
              </svg>
            </div>
            <div class="body">
              <div class="top">
                <span class="num">{{ asignaciones().length }}</span>
                <span class="label">Total cursos</span>
              </div>
              <div class="hint">Asignados actualmente</div>
            </div>
          </div>
          <div class="fg-stat">
            <div class="ico" aria-hidden="true">
              <svg width="18" height="16" viewBox="0 0 18 16" fill="currentColor">
                <path d="M2 0h14a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V2a2 2 0 012-2zm0 2v10h14V2H2zm3 12h8v2H5v-2z" />
              </svg>
            </div>
            <div class="body">
              <div class="top">
                <span class="num">{{ materiasDistintas() }}</span>
                <span class="label">Materias</span>
              </div>
              <div class="hint">A su cargo</div>
            </div>
          </div>
          <div class="fg-stat">
            <div class="ico" aria-hidden="true">
              <svg width="18" height="16" viewBox="0 0 18 16" fill="currentColor">
                <path d="M0 0h8v7H0V0zm10 0h8v7h-8V0zM0 9h8v7H0V9zm10 0h8v7h-8V9z" />
              </svg>
            </div>
            <div class="body">
              <div class="top">
                <span class="num">{{ gradosDistintos().length }}</span>
                <span class="label">Grados</span>
              </div>
              <div class="hint">Con asignación</div>
            </div>
          </div>
        </section>

        <div class="fg-filters">
          <div class="search">
            <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true">
              <circle cx="7.5" cy="7.5" r="5.5" fill="none" stroke="currentColor" stroke-width="1.6" />
              <path d="M12 12l5 5" stroke="currentColor" stroke-width="1.6" />
            </svg>
            <input
              type="text"
              placeholder="Buscar por curso o materia..."
              aria-label="Buscar por curso o materia"
              [ngModel]="q()"
              (ngModelChange)="q.set($event)"
            />
          </div>
          <select aria-label="Filtrar por grado" [ngModel]="grado()" (ngModelChange)="grado.set($event)">
            <option value="">Grado: Todos</option>
            @for (g of gradosDistintos(); track g) {
              <option [value]="g">{{ g }}</option>
            }
          </select>
          <select
            aria-label="Filtrar por paralelo"
            [ngModel]="paralelo()"
            (ngModelChange)="paralelo.set($event)"
          >
            <option value="">Paralelo: Todos</option>
            @for (p of paralelosDistintos(); track p) {
              <option [value]="p">{{ p }}</option>
            }
          </select>
        </div>

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" />
        }

        <div class="fg-table-card">
          <p-table
            [value]="filtradas()"
            dataKey="asignacionId"
            [loading]="loading()"
            [tableStyle]="{ 'min-width': '40rem' }"
            styleClass="fg-table"
          >
            <ng-template #header>
              <tr>
                <th>Grado</th>
                <th class="ta-center">Paralelo</th>
                <th>Materia</th>
                <th class="ta-right">Acciones</th>
              </tr>
            </ng-template>
            <ng-template #body let-fila>
              <tr>
                <td>
                  <div class="grado">{{ fila.cursoNombre }}</div>
                </td>
                <td class="ta-center">
                  <span class="badge">{{ fila.paraleloNombre }}</span>
                </td>
                <td class="materia">{{ fila.materiaNombre }}</td>
                <td class="ta-right">
                  <button type="button" class="accion" (click)="ingresar(fila)">
                    <svg width="15" height="10" viewBox="0 0 15 10" aria-hidden="true">
                      <path
                        d="M7.3 0C4 0 1.2 2 0 5c1.2 3 4 5 7.3 5s6.1-2 7.3-5C13.4 2 10.6 0 7.3 0zm0 8.3A3.3 3.3 0 117.3 1.7a3.3 3.3 0 010 6.6zm0-5.3a2 2 0 100 4 2 2 0 000-4z"
                        fill="currentColor"
                      />
                    </svg>
                    Ver
                  </button>
                </td>
              </tr>
            </ng-template>
            <ng-template #emptymessage>
              <tr>
                <td colspan="4">No tiene cursos asignados.</td>
              </tr>
            </ng-template>
          </p-table>
          <div class="fg-footer">
            <span>Mostrando {{ filtradas().length }} de {{ asignaciones().length }} cursos activos</span>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class MisCursosListPage implements OnInit {
  asignaciones = signal<MisAsignacionResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  q = signal('');
  grado = signal('');
  paralelo = signal('');

  materiasDistintas = computed(() => new Set(this.asignaciones().map((a) => a.materiaId)).size);
  gradosDistintos = computed(() => [...new Set(this.asignaciones().map((a) => a.cursoNombre))]);
  paralelosDistintos = computed(() => [
    ...new Set(this.asignaciones().map((a) => a.paraleloNombre)),
  ]);

  filtradas = computed(() => {
    const q = this.q().trim().toLowerCase();
    const g = this.grado();
    const p = this.paralelo();
    return this.asignaciones().filter(
      (a) =>
        (!g || a.cursoNombre === g) &&
        (!p || a.paraleloNombre === p) &&
        (!q ||
          a.cursoEtiqueta.toLowerCase().includes(q) ||
          a.materiaNombre.toLowerCase().includes(q)),
    );
  });

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.http
      .get<MisAsignacionResponse[]>(`${ApiBase.BASE}/profesores/me/asignaciones`)
      .subscribe({
        next: (lista) => {
          this.asignaciones.set(lista);
          this.loading.set(false);
        },
        error: (_err: HttpErrorResponse) => {
          this.errorMsg.set('No se pudieron cargar sus asignaciones.');
          this.loading.set(false);
        },
      });
  }

  ingresar(fila: MisAsignacionResponse): void {
    void this.router.navigate(['/academico/mis-cursos/planilla'], {
      queryParams: {
        materiaId: fila.materiaId,
        cursoId: fila.cursoId,
        paraleloId: fila.paraleloId,
      },
    });
  }
}
