import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { EvaluacionResponse } from './evaluacion.model';
import { MateriaResponse } from './materia.model';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { PeriodoEvaluacionResponse } from './periodo-evaluacion.model';
import { SeccionEvaluacionResponse } from './seccion-evaluacion.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/** Evaluaciones de Materia (DD-UC-017/021) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-materia-evaluaciones-page',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    SelectModule,
    InputTextModule,
    TagModule,
    MessageModule,
  ],
  template: `
    <div class="page">
      <p-button
        [label]="esAdmin() ? '← Volver a la materia' : '← Volver a Mis materias'"
        [link]="true"
        (onClick)="volver()"
      />

      @if (notFound()) {
        <p-message
          severity="error"
          text="La Materia no existe o no está asignada a su usuario."
          styleClass="block-msg"
        />
      } @else {
        <h2>Evaluaciones de "{{ materia()?.nombre || '…' }}"</h2>

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        @if (!esAdmin() && gestionActivaNoEncontrada()) {
          <p-message
            severity="warn"
            text="No hay una gestión escolar activa en este momento. Contacta al administrador."
            styleClass="block-msg"
          />
        } @else {
          <div class="filters">
            @if (esAdmin()) {
              <label class="field">
                Gestión escolar
                <p-select
                  [options]="gestiones()"
                  [(ngModel)]="gestionId"
                  (ngModelChange)="onGestionChange()"
                  optionLabel="nombre"
                  optionValue="id"
                  placeholder="— Seleccione —"
                  [showClear]="true"
                />
              </label>
            }
            @if (esAdmin() || !periodoAutoSeleccionado()) {
              <label class="field">
                Periodo
                <p-select
                  [options]="periodos()"
                  [(ngModel)]="periodoId"
                  (ngModelChange)="cargarEvaluaciones()"
                  optionLabel="nombre"
                  optionValue="id"
                  placeholder="— Seleccione —"
                  [disabled]="esAdmin() && !gestionId"
                  [showClear]="true"
                />
              </label>
            } @else {
              <div class="field">
                Periodo actual
                <strong>{{ periodoSeleccionado()?.nombre }} ({{ periodoSeleccionado()?.estado }})</strong>
              </div>
            }
          </div>

          @if (periodoSeleccionado()?.estado === 'ABIERTO') {
            <section class="box">
              <h3>Nueva evaluación</h3>
              <form class="inline-form" (ngSubmit)="crear()">
                <label class="field">
                  Nombre
                  <input pInputText [(ngModel)]="nuevoNombre" name="nombre" required maxlength="100" />
                </label>
                <label class="field">
                  Sección
                  <p-select
                    [options]="seccionOptions"
                    [(ngModel)]="nuevaSeccionId"
                    name="seccion"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="—"
                    [showClear]="true"
                  />
                </label>
                <label class="field">
                  Fecha
                  <input pInputText type="date" [(ngModel)]="nuevaFecha" name="fecha" required />
                </label>
                <label class="field">
                  Descripción (opcional)
                  <input pInputText [(ngModel)]="nuevaDescripcion" name="descripcion" />
                </label>
                <p-button type="submit" [label]="saving() ? 'Guardando…' : 'Crear'" [disabled]="saving()" />
              </form>
            </section>
          } @else if (periodoId) {
            <p class="hint">Solo se pueden crear o anular evaluaciones cuando el periodo está ABIERTO.</p>
          }

          <p-table
            [value]="evaluaciones()"
            dataKey="id"
            [tableStyle]="{ 'min-width': '40rem' }"
            styleClass="p-datatable-sm"
          >
            <ng-template #header>
              <tr>
                <th>Nombre</th>
                <th>Sección</th>
                <th>Fecha</th>
                <th>Puntaje máx.</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template #body let-evaluacion>
              <tr [style.opacity]="evaluacion.estado === 'ANULADA' ? '0.6' : '1'">
                <td>{{ evaluacion.nombre }}</td>
                <td>{{ nombreSeccion(evaluacion.seccionEvaluacionId) }}</td>
                <td>{{ evaluacion.fecha }}</td>
                <td>{{ evaluacion.puntajeMaximo }}</td>
                <td>
                  <p-tag
                    [value]="evaluacion.estado"
                    [severity]="evaluacion.estado === 'ACTIVA' ? 'success' : 'secondary'"
                  />
                </td>
                <td class="actions">
                  @if (evaluacion.estado === 'ACTIVA') {
                    <p-button
                      label="Calificaciones"
                      [link]="true"
                      size="small"
                      (onClick)="irCalificaciones(evaluacion.id)"
                    />
                  }
                  @if (evaluacion.estado === 'ACTIVA' && periodoSeleccionado()?.estado === 'ABIERTO') {
                    <p-button
                      label="Anular"
                      [link]="true"
                      severity="danger"
                      size="small"
                      (onClick)="anular(evaluacion)"
                    />
                  }
                </td>
              </tr>
            </ng-template>
            <ng-template #emptymessage>
              <tr>
                <td colspan="6">
                  {{ periodoId ? 'No hay evaluaciones en este periodo.' : 'Seleccione un periodo.' }}
                </td>
              </tr>
            </ng-template>
          </p-table>
        }
      }
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 960px;
        margin: 0 auto;
      }
      .block-msg {
        display: block;
        margin: var(--space-md) 0;
      }
      .filters {
        display: flex;
        gap: var(--space-md);
        flex-wrap: wrap;
        margin-bottom: var(--space-lg);
        align-items: end;
      }
      .box {
        margin-bottom: var(--space-lg);
        padding: var(--space-md);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }
      .box h3 {
        margin-top: 0;
      }
      .inline-form {
        display: flex;
        gap: var(--space-sm);
        flex-wrap: wrap;
        align-items: end;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
        font-size: var(--font-size-small);
        min-width: 10rem;
      }
      .hint {
        color: var(--color-text-secondary);
        font-size: var(--font-size-small);
      }
      .actions {
        display: flex;
        gap: var(--space-sm);
        align-items: center;
        flex-wrap: wrap;
      }
    `,
  ],
})
export class MateriaEvaluacionesPage implements OnInit {
  materiaId = '';
  materia = signal<MateriaResponse | null>(null);
  gestiones = signal<GestionEscolarResponse[]>([]);
  periodos = signal<PeriodoEvaluacionResponse[]>([]);
  secciones = signal<SeccionEvaluacionResponse[]>([]);
  evaluaciones = signal<EvaluacionResponse[]>([]);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  saving = signal(false);
  gestionActivaNoEncontrada = signal(false);
  periodoAutoSeleccionado = signal(false);

  gestionId = '';
  periodoId = '';
  nuevoNombre = '';
  nuevaSeccionId = '';
  nuevaFecha = '';
  nuevaDescripcion = '';

  get seccionOptions(): { label: string; value: string }[] {
    return this.secciones().map((s) => ({ label: s.nombre, value: s.id }));
  }

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    protected auth: AuthService
  ) {}

  volver(): void {
    if (this.esAdmin()) {
      void this.router.navigate(['/academico/materias', this.materiaId]);
    } else {
      void this.router.navigate(['/academico/mis-materias']);
    }
  }

  irCalificaciones(evaluacionId: string): void {
    void this.router.navigate([
      '/academico/materias',
      this.materiaId,
      'evaluaciones',
      evaluacionId,
      'calificaciones',
    ]);
  }

  ngOnInit(): void {
    this.materiaId = this.route.snapshot.paramMap.get('id') ?? '';
    this.http.get<MateriaResponse>(`${ApiBase.BASE}/materias/${this.materiaId}`).subscribe({
      next: (materia) => this.materia.set(materia),
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) this.notFound.set(true);
        else this.errorMsg.set('Error al cargar la materia.');
      },
    });

    if (this.esAdmin()) {
      const params = new HttpParams().set('page', 0).set('size', 100);
      this.http.get<PageResponse<GestionEscolarResponse>>(`${ApiBase.BASE}/gestiones-escolares`, { params }).subscribe({
        next: (respuesta) => this.gestiones.set(respuesta.content),
      });
    } else {
      this.cargarGestionActiva();
    }
  }

  esAdmin(): boolean {
    return this.auth.hasRole('ADMIN');
  }

  periodoSeleccionado(): PeriodoEvaluacionResponse | undefined {
    return this.periodos().find((p) => p.id === this.periodoId);
  }

  nombreSeccion(seccionId: string): string {
    return this.secciones().find((s) => s.id === seccionId)?.nombre ?? seccionId;
  }

  /** Selector manual de gestión (exclusivo ADMIN, DD-UC-021). */
  onGestionChange(): void {
    this.periodoId = '';
    this.periodos.set([]);
    this.secciones.set([]);
    this.evaluaciones.set([]);
    if (!this.gestionId) return;
    this.http
      .get<PeriodoEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/periodos`)
      .subscribe({
        next: (lista) => {
          this.periodos.set(lista);
          const abierto = lista.find((p) => p.estado === 'ABIERTO');
          this.periodoId = abierto?.id ?? lista[0]?.id ?? '';
          this.cargarEvaluaciones();
        },
      });
    this.http
      .get<SeccionEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/${this.gestionId}/secciones`)
      .subscribe({ next: (lista) => this.secciones.set(lista) });
  }

  /**
   * PROFESOR (DD-UC-021): resuelve la gestión ACTIVA del tenant de forma implícita, sin
   * selector. 404 se traduce en {@code gestionActivaNoEncontrada}.
   */
  private cargarGestionActiva(): void {
    this.http.get<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/activa`).subscribe({
      next: (gestion) => {
        this.gestionId = gestion.id;
        this.http
          .get<PeriodoEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/activa/periodos`)
          .subscribe({ next: (lista) => this.resolverPeriodoAutomatico(lista) });
        this.http
          .get<SeccionEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/activa/secciones`)
          .subscribe({ next: (lista) => this.secciones.set(lista) });
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) this.gestionActivaNoEncontrada.set(true);
        else this.errorMsg.set('Error al cargar la gestión escolar activa.');
      },
    });
  }

  /**
   * DD-UC-021 (decisión "abierto_o_selector_periodo"): si hay exactamente un periodo
   * ABIERTO se autoselecciona sin mostrar selector; en cualquier otro caso se deja que el
   * PROFESOR elija manualmente entre los periodos de la gestión activa.
   */
  private resolverPeriodoAutomatico(lista: PeriodoEvaluacionResponse[]): void {
    this.periodos.set(lista);
    const abiertos = lista.filter((p) => p.estado === 'ABIERTO');
    if (abiertos.length === 1) {
      this.periodoAutoSeleccionado.set(true);
      this.periodoId = abiertos[0].id;
    } else {
      this.periodoAutoSeleccionado.set(false);
      this.periodoId = '';
    }
    this.cargarEvaluaciones();
  }

  cargarEvaluaciones(): void {
    if (!this.periodoId) {
      this.evaluaciones.set([]);
      return;
    }
    const params = new HttpParams().set('periodoId', this.periodoId);
    this.http
      .get<EvaluacionResponse[]>(`${ApiBase.BASE}/materias/${this.materiaId}/evaluaciones`, { params })
      .subscribe({
        next: (lista) => this.evaluaciones.set(lista),
        error: (err: HttpErrorResponse) => {
          if (err.status === 404) this.notFound.set(true);
          else this.errorMsg.set('Error al cargar evaluaciones.');
        },
      });
  }

  crear(): void {
    if (!this.nuevoNombre || !this.nuevaSeccionId || !this.nuevaFecha || !this.periodoId) return;
    this.saving.set(true);
    this.errorMsg.set(null);
    this.http
      .post<EvaluacionResponse>(`${ApiBase.BASE}/evaluaciones`, {
        nombre: this.nuevoNombre,
        materiaId: this.materiaId,
        periodoEvaluacionId: this.periodoId,
        seccionEvaluacionId: this.nuevaSeccionId,
        fecha: this.nuevaFecha,
        descripcion: this.nuevaDescripcion || null,
      })
      .subscribe({
        next: (creada) => {
          this.evaluaciones.update((lista) => [...lista, creada]);
          this.nuevoNombre = '';
          this.nuevaDescripcion = '';
          this.saving.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          this.errorMsg.set(this.mensajeError(err));
        },
      });
  }

  anular(evaluacion: EvaluacionResponse): void {
    if (!confirm(`¿Anular la evaluación "${evaluacion.nombre}"?`)) return;
    this.errorMsg.set(null);
    this.http.patch<EvaluacionResponse>(`${ApiBase.BASE}/evaluaciones/${evaluacion.id}/estado`, { estado: 'ANULADA' }).subscribe({
      next: (actualizada) => {
        this.evaluaciones.update((lista) =>
          lista.map((item) => (item.id === actualizada.id ? actualizada : item))
        );
      },
      error: (err: HttpErrorResponse) => this.errorMsg.set(this.mensajeError(err)),
    });
  }

  private mensajeError(err: HttpErrorResponse): string {
    const codigo = err.error?.codigo;
    if (codigo === 'E_MATERIA_SIN_PROFESOR') {
      return 'La materia no tiene profesor asignado.';
    }
    if (codigo === 'E_PERIODO_NO_ABIERTO') {
      return 'Solo se pueden modificar evaluaciones de un periodo abierto.';
    }
    if (codigo === 'E_SECCION_NO_PERTENECE_A_GESTION') {
      return 'La sección no pertenece a la misma gestión que el periodo.';
    }
    if (err.status === 404) {
      return err.error?.mensaje ?? 'No encontrado.';
    }
    return err.error?.mensaje ?? 'Error al guardar la evaluación.';
  }
}
