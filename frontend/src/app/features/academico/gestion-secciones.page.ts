import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { SeccionEvaluacionDraft, SeccionEvaluacionResponse } from './seccion-evaluacion.model';
import { ApiBase } from '../../core/api/api-base';
import { AuthService } from '../../core/auth/auth.service';

/** Secciones de Gestión (DD-UC-016/019) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-gestion-secciones-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, InputNumberModule, MessageModule],
  template: `
    <div class="page">
      <p-button label="← Volver a Gestión Escolar" [link]="true" (onClick)="volver()" />

      @if (notFound()) {
        <p-message
          severity="error"
          text="La gestión escolar no existe o no pertenece a su institución."
          styleClass="block-msg"
        />
      } @else {
        <h2>Secciones de "{{ gestion()?.nombre || '…' }}"</h2>
        @if (gestion()) {
          <p class="meta">
            {{ gestion()!.fechaInicio }} — {{ gestion()!.fechaFin }} · estado {{ gestion()!.estado }}
          </p>
        }

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }
        @if (okMsg()) {
          <p-message severity="success" [text]="okMsg()!" styleClass="block-msg" />
        }

        <p-table
          [value]="filas()"
          [loading]="loading()"
          [tableStyle]="{ 'min-width': '30rem' }"
          styleClass="p-datatable-sm"
        >
          <ng-template #header>
            <tr>
              <th>#</th>
              <th>Nombre</th>
              <th>Nota</th>
              @if (esAdmin()) {
                <th></th>
              }
            </tr>
          </ng-template>
          <ng-template #body let-fila let-i="rowIndex">
            <tr>
              <td>{{ i + 1 }}</td>
              <td>
                <input
                  pInputText
                  type="text"
                  [ngModel]="fila.nombre"
                  (ngModelChange)="actualizarFila(i, 'nombre', $event)"
                  [disabled]="!esAdmin() || saving()"
                  maxlength="50"
                />
              </td>
              <td>
                <p-inputnumber
                  [ngModel]="fila.nota"
                  (ngModelChange)="actualizarFila(i, 'nota', $event)"
                  [disabled]="!esAdmin() || saving()"
                  [min]="0.01"
                  [max]="100"
                  [maxFractionDigits]="2"
                  [useGrouping]="false"
                />
              </td>
              @if (esAdmin()) {
                <td>
                  <p-button
                    label="Quitar"
                    [link]="true"
                    severity="danger"
                    size="small"
                    [disabled]="saving() || filas().length <= 1"
                    (onClick)="quitarFila(i)"
                  />
                </td>
              }
            </tr>
          </ng-template>
        </p-table>

        <p [class.ok]="sumaValida()" [class.bad]="!sumaValida()" class="suma">
          Suma: {{ suma().toFixed(2) }} / 100
        </p>

        @if (esAdmin()) {
          <div class="actions">
            <p-button label="Añadir fila" severity="secondary" [outlined]="true" [disabled]="saving()" (onClick)="anadirFila()" />
            <p-button
              [label]="saving() ? 'Guardando...' : 'Guardar plantilla'"
              [disabled]="saving() || !sumaValida()"
              (onClick)="guardar()"
            />
          </div>
        }
      }
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 800px;
        margin: 0 auto;
      }
      .meta {
        color: var(--color-text-secondary);
        font-size: var(--font-size-small);
      }
      .block-msg {
        display: block;
        margin: var(--space-md) 0;
      }
      .suma {
        font-weight: 600;
      }
      .suma.ok {
        color: var(--color-success-text);
      }
      .suma.bad {
        color: var(--color-danger-text);
      }
      .actions {
        display: flex;
        gap: var(--space-sm);
        margin-top: var(--space-sm);
      }
    `,
  ],
})
export class GestionSeccionesPage implements OnInit {
  gestion = signal<GestionEscolarResponse | null>(null);
  filas = signal<SeccionEvaluacionDraft[]>([]);
  loading = signal(true);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  okMsg = signal<string | null>(null);
  saving = signal(false);

  readonly esAdmin = computed(() => this.auth.hasRole('ADMIN'));

  private gestionId = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.gestionId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargar();
  }

  volver(): void {
    this.router.navigate(['/academico/gestiones-escolares']);
  }

  suma(): number {
    return this.filas().reduce((acc, f) => acc + (Number(f.nota) || 0), 0);
  }

  sumaValida(): boolean {
    return Math.abs(this.suma() - 100) < 0.005 && this.filas().every((f) => (f.nombre ?? '').trim().length > 0);
  }

  actualizarFila(index: number, campo: 'nombre' | 'nota', valor: string | number | null): void {
    const actual = [...this.filas()];
    if (campo === 'nombre') {
      actual[index] = { ...actual[index], nombre: String(valor ?? '') };
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
  }

  private alErrorCarga(err: HttpErrorResponse): void {
    if (err.status === 404) this.notFound.set(true);
    else this.errorMsg.set('Error al cargar las secciones.');
    this.loading.set(false);
  }

  private mensajeError(err: HttpErrorResponse): string {
    const codigo = err.error?.codigo as string | undefined;
    if (codigo === 'E_SUMA_SECCIONES_INVALIDA') return 'La suma de nota debe ser exactamente 100.';
    if (codigo === 'E_PESO_INVALIDO') return 'Cada nota debe estar entre 0 (excluido) y 100.';
    return 'No se pudo guardar la plantilla.';
  }
}
