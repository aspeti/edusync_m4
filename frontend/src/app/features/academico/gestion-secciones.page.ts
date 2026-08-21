import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { PeriodoEvaluacionResponse } from './periodo-evaluacion.model';
import { SeccionEvaluacionDraft, SeccionEvaluacionResponse } from './seccion-evaluacion.model';
import { ApiBase } from '../../core/api/api-base';

/**
 * Detalle de Secciones de una Gestion Escolar (DD-UC-016 §2): GET gestion,
 * tabla editable, Guardar = PUT de la plantilla. Freeze si algun periodo
 * no esta PENDIENTE.
 */
@Component({
  selector: 'app-gestion-secciones-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 800px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        <a routerLink="/academico/gestiones-escolares" style="font-size: 0.85rem;">← Volver a Gestión Escolar</a>
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          La gestión escolar no existe o no pertenece a su institución.
        </div>
      } @else {
        <h2>Secciones de "{{ gestion()?.nombre || '…' }}"</h2>
        @if (gestion()) {
          <p style="color: #666; font-size: 0.9rem;">
            {{ gestion()!.fechaInicio }} — {{ gestion()!.fechaFin }}
            · estado {{ gestion()!.estado }}
          </p>
        }

        @if (loading()) {
          <p>Cargando secciones...</p>
        }

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        @if (okMsg()) {
          <div style="background: #e8f5e9; color: #2e7d32; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ okMsg() }}
          </div>
        }

        @if (!loading()) {
          @if (congelada()) {
            <p style="color: #666; font-size: 0.85rem;">
              Hay un periodo abierto o cerrado: la plantilla de secciones ya no se puede modificar.
            </p>
          }

          <table style="width: 100%; border-collapse: collapse; margin-bottom: 1rem;">
            <thead>
              <tr style="background: #f5f5f5;">
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">#</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nota</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;"></th>
              </tr>
            </thead>
            <tbody>
              @for (fila of filas(); track $index; let i = $index) {
                <tr style="border-bottom: 1px solid #eee;">
                  <td style="padding: 0.5rem;">{{ i + 1 }}</td>
                  <td style="padding: 0.5rem;">
                    <input
                      type="text"
                      [ngModel]="fila.nombre"
                      (ngModelChange)="actualizarFila(i, 'nombre', $event)"
                      [disabled]="congelada() || saving()"
                      maxlength="50"
                      style="padding: 0.35rem; width: 100%;"
                    />
                  </td>
                  <td style="padding: 0.5rem;">
                    <input
                      type="number"
                      [ngModel]="fila.nota"
                      (ngModelChange)="actualizarFila(i, 'nota', $event)"
                      [disabled]="congelada() || saving()"
                      min="0.01"
                      max="100"
                      step="0.01"
                      style="padding: 0.35rem; width: 8rem;"
                    />
                  </td>
                  <td style="padding: 0.5rem;">
                    @if (!congelada()) {
                      <button (click)="quitarFila(i)" [disabled]="saving() || filas().length <= 1" style="cursor: pointer; font-size: 0.85rem; color: #c62828;">
                        Quitar
                      </button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>

          <p [style.color]="sumaValida() ? '#2e7d32' : '#c62828'" style="font-weight: 600;">
            Suma: {{ suma().toFixed(2) }} / 100
          </p>

          @if (!congelada()) {
            <div style="display: flex; gap: 0.5rem; margin-top: 0.75rem;">
              <button (click)="anadirFila()" [disabled]="saving()" style="padding: 0.5rem 1rem; cursor: pointer;">
                Añadir fila
              </button>
              <button
                (click)="guardar()"
                [disabled]="saving() || !sumaValida()"
                style="padding: 0.5rem 1rem; background: #1e3a5f; color: white; border: none; cursor: pointer;"
              >
                {{ saving() ? 'Guardando...' : 'Guardar plantilla' }}
              </button>
            </div>
          }
        }
      }
    </div>
  `,
})
export class GestionSeccionesPage implements OnInit {
  gestion = signal<GestionEscolarResponse | null>(null);
  filas = signal<SeccionEvaluacionDraft[]>([]);
  loading = signal(true);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  okMsg = signal<string | null>(null);
  saving = signal(false);
  congelada = signal(false);

  private gestionId = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.gestionId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargar();
  }

  suma(): number {
    return this.filas().reduce((acc, f) => acc + (Number(f.nota) || 0), 0);
  }

  sumaValida(): boolean {
    return Math.abs(this.suma() - 100) < 0.005 && this.filas().every((f) => (f.nombre ?? '').trim().length > 0);
  }

  actualizarFila(index: number, campo: 'nombre' | 'nota', valor: string | number): void {
    const actual = [...this.filas()];
    if (campo === 'nombre') {
      actual[index] = { ...actual[index], nombre: String(valor) };
    } else {
      const n = valor === '' || valor === null ? null : Number(valor);
      actual[index] = { ...actual[index], nota: n };
    }
    this.filas.set(actual);
  }

  anadirFila(): void {
    this.filas.set([...this.filas(), { nombre: '', nota: null }]);
  }

  quitarFila(index: number): void {
    this.filas.set(this.filas().filter((_, i) => i !== index));
  }

  guardar(): void {
    this.saving.set(true);
    this.errorMsg.set(null);
    this.okMsg.set(null);
    const secciones = this.filas().map((f) => ({ nombre: f.nombre.trim(), nota: Number(f.nota) }));
    this.http
      .put<SeccionEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/secciones`, {
        secciones,
      })
      .subscribe({
        next: (lista) => {
          this.filas.set(lista.map((s) => ({ nombre: s.nombre, nota: Number(s.nota) })));
          this.okMsg.set('Plantilla guardada.');
          this.saving.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(this.mensajeError(err));
          this.saving.set(false);
        },
      });
  }

  cargar(): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.errorMsg.set(null);
    this.http.get<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}`).subscribe({
      next: (gestion) => {
        this.gestion.set(gestion);
        this.http
          .get<PeriodoEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/periodos`)
          .subscribe({
            next: (periodos) => {
              this.congelada.set(periodos.some((p) => p.estado !== 'PENDIENTE'));
              this.http
                .get<SeccionEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/secciones`)
                .subscribe({
                  next: (secciones) => {
                    this.filas.set(secciones.map((s) => ({ nombre: s.nombre, nota: Number(s.nota) })));
                    this.loading.set(false);
                  },
                  error: (err: HttpErrorResponse) => this.alErrorCarga(err),
                });
            },
            error: (err: HttpErrorResponse) => this.alErrorCarga(err),
          });
      },
      error: (err: HttpErrorResponse) => this.alErrorCarga(err),
    });
  }

  private alErrorCarga(err: HttpErrorResponse): void {
    if (err.status === 404) {
      this.notFound.set(true);
    } else {
      this.errorMsg.set('Error al cargar las secciones.');
    }
    this.loading.set(false);
  }

  private mensajeError(err: HttpErrorResponse): string {
    const codigo = err.error?.codigo as string | undefined;
    if (codigo === 'E_SECCIONES_INMUTABLES') {
      return 'Hay un periodo abierto o cerrado: no se pueden cambiar las secciones.';
    }
    if (codigo === 'E_SUMA_SECCIONES_INVALIDA') {
      return 'La suma de nota debe ser exactamente 100.';
    }
    if (codigo === 'E_PESO_INVALIDO') {
      return 'Cada nota debe estar entre 0 (excluido) y 100.';
    }
    return 'No se pudo guardar la plantilla.';
  }
}
