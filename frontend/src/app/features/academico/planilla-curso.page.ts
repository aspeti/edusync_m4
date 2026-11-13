import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { ApiBase } from '../../core/api/api-base';
import { PlanillaColumna, PlanillaFila, PlanillaMateriaResponse } from './planilla.model';
import { PeriodoEvaluacionResponse } from './periodo-evaluacion.model';
import { GestionEscolarResponse } from './gestion-escolar.model';

/**
 * Planilla estudiantes × evaluaciones (DD-UC-033 / FSD-UC-022).
 * Autosave por celda (blur/Enter); sin Agregar/Editar/Guardar. Promedio solo del backend.
 */
@Component({
  selector: 'app-planilla-curso-page',
  standalone: true,
  imports: [FormsModule, DecimalPipe, InputNumberModule, MessageModule, SelectModule],
  template: `
    <div class="fg-page">
      <nav class="fg-breadcrumb" aria-label="Breadcrumb">
        <button type="button" (click)="volver()">Cursos</button>
        <svg class="sep" viewBox="0 0 4 7" aria-hidden="true">
          <path d="M.5.5l3 3-3 3" fill="none" stroke="currentColor" stroke-width="1" />
        </svg>
        <span>{{ planilla()?.cursoEtiqueta || '…' }}</span>
        <svg class="sep" viewBox="0 0 4 7" aria-hidden="true">
          <path d="M.5.5l3 3-3 3" fill="none" stroke="currentColor" stroke-width="1" />
        </svg>
        <span class="actual">Registro de calificaciones</span>
      </nav>

      <div class="fg-main">
        @if (notFound()) {
          <p-message
            severity="error"
            text="La planilla no existe o no está asignada a su usuario."
          />
        } @else {
          @if (planilla(); as p) {
            <section class="fg-stats fg-stats--planilla" aria-label="Resumen de la planilla">
              <div class="card progreso">
                <div class="row">
                  <span class="t">Progreso de carga</span>
                  <span class="v">{{ progreso() }}%</span>
                </div>
                <div class="track" role="progressbar" [attr.aria-valuenow]="progreso()" aria-valuemin="0" aria-valuemax="100">
                  <div class="fill" [style.width.%]="progreso()"></div>
                </div>
              </div>
              <div class="card resumen">
                <div class="item">
                  <span class="dot dot--ok" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor"><circle cx="6" cy="6" r="3" /></svg>
                  </span>
                  <div>
                    <div class="n">{{ p.filas.length }}</div>
                    <div class="l">Estudiantes</div>
                  </div>
                </div>
                <span class="div"></span>
                <div class="item">
                  <span class="dot dot--warn" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor"><rect x="2" y="2" width="8" height="8" rx="1" /></svg>
                  </span>
                  <div>
                    <div class="n">{{ p.columnas.length }}</div>
                    <div class="l">Evaluaciones</div>
                  </div>
                </div>
              </div>
              <div class="card estado">
                <div>
                  <div class="l">Estado</div>
                  <div class="n">{{ estadoTexto() }}</div>
                </div>
              </div>
            </section>
          }

          <div class="fg-header">
            <div>
              <h1 class="fg-h1" data-testid="planilla-heading">Registro de Calificaciones</h1>
              <p class="fg-sub">
                {{ planilla()?.materiaNombre || '…' }} — {{ planilla()?.cursoEtiqueta || '…' }}
                @if (planilla()) {
                  ({{ planilla()!.periodoNombre }})
                }
              </p>
            </div>
            @if (planilla()) {
              <button type="button" class="fg-btn" (click)="irEvaluaciones()">
                Gestionar evaluaciones
              </button>
            }
          </div>

          @if (planilla() && !planilla()!.editable) {
            <p-message
              severity="warn"
              text="El periodo no está abierto: la planilla es de solo lectura."
            />
          }
          @if (errorMsg()) {
            <p-message severity="error" [text]="errorMsg()!" />
          }

          <div class="fg-filters">
            <div class="search">
              <svg width="14" height="14" viewBox="0 0 18 18" aria-hidden="true">
                <circle cx="7.5" cy="7.5" r="5.5" fill="none" stroke="currentColor" stroke-width="1.8" />
                <path d="M12 12l5 5" stroke="currentColor" stroke-width="1.8" />
              </svg>
              <input
                type="text"
                placeholder="Buscar estudiante..."
                aria-label="Buscar estudiante"
                [ngModel]="q()"
                (ngModelChange)="q.set($event)"
              />
            </div>
            @if (!periodoAutoSeleccionado() && periodos().length > 0) {
              <p-select
                inputId="periodo"
                [options]="periodos()"
                optionLabel="nombre"
                optionValue="id"
                [ngModel]="periodoId"
                (ngModelChange)="onPeriodoChange($event)"
                placeholder="Seleccione periodo"
                styleClass="periodo-select"
              />
            } @else if (planilla()) {
              <span class="chip">{{ planilla()!.periodoNombre }}</span>
            }
          </div>

          @if (planilla(); as p) {
            @if (p.filas.length === 0) {
              <p>No hay estudiantes inscritos en este curso/paralelo.</p>
            } @else {
              <div class="fg-table-card">
                <div class="fg-table-scroll">
                  <table class="fg-grid">
                    <thead>
                      <tr>
                        <th class="nombre" rowspan="2" scope="col">Nº &amp; Nombre del estudiante</th>
                        @for (g of grupos(); track g.id) {
                          <th [attr.colspan]="g.cols.length" scope="colgroup">
                            {{ g.nombre }}
                            <span class="sub">Máx {{ g.maximo }}</span>
                          </th>
                        }
                        <th class="promedio" rowspan="2" scope="col">
                          Promedio
                          <span class="sub">{{ p.periodoNombre }}</span>
                        </th>
                      </tr>
                      <tr class="eval">
                        @for (col of p.columnas; track col.evaluacionId) {
                          <th scope="col" [attr.title]="col.nombre + ' · max ' + col.puntajeMaximo">
                            {{ col.nombre }}
                          </th>
                        }
                      </tr>
                    </thead>
                    <tbody>
                      @for (fila of filasVisibles(); track fila.estudianteId; let rowIndex = $index) {
                        <tr>
                          <td class="nombre">
                            <span class="n">{{ rowIndex + 1 | number: '2.0-0' }}</span>
                            <span class="nom">{{ fila.nombreCompleto }}</span>
                            <small>RUDE {{ fila.rude }}</small>
                          </td>
                          @for (col of p.columnas; track col.evaluacionId; let colIndex = $index) {
                            <td>
                              <p-inputnumber
                                [ngModel]="edits[celdaKey(fila.estudianteId, col.evaluacionId)]"
                                (ngModelChange)="
                                  onNotaChange(fila.estudianteId, col.evaluacionId, $event)
                                "
                                [name]="celdaKey(fila.estudianteId, col.evaluacionId)"
                                [min]="0"
                                [max]="col.puntajeMaximo"
                                [maxFractionDigits]="2"
                                [minFractionDigits]="0"
                                [useGrouping]="false"
                                locale="es-BO"
                                inputStyleClass="nota-input"
                                [disabled]="!p.editable || savingCell() === celdaKey(fila.estudianteId, col.evaluacionId)"
                                [invalid]="!!erroresCelda[celdaKey(fila.estudianteId, col.evaluacionId)]"
                                (onBlur)="persistir(fila.estudianteId, col.evaluacionId, col.puntajeMaximo)"
                                (onKeyDown)="
                                  onNotaKeydown(
                                    $event,
                                    filasVisibles().indexOf(fila),
                                    colIndex,
                                    fila.estudianteId,
                                    col.evaluacionId,
                                    col.puntajeMaximo
                                  )
                                "
                              />
                              @if (erroresCelda[celdaKey(fila.estudianteId, col.evaluacionId)]; as msg) {
                                <small class="celda-error">{{ msg }}</small>
                              }
                            </td>
                          }
                          <td class="prom">{{ fila.notaPeriodo == null ? '—' : fila.notaPeriodo }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
                <div class="fg-footer">
                  <span>Mostrando {{ filasVisibles().length }} de {{ p.filas.length }} estudiantes</span>
                </div>
              </div>
            }
          }
        }
      </div>
    </div>
  `,
  styles: [
    `
      :host ::ng-deep .periodo-select {
        min-width: 14rem;
      }
    `,
  ],
})
export class PlanillaCursoPage implements OnInit {
  planilla = signal<PlanillaMateriaResponse | null>(null);
  periodos = signal<PeriodoEvaluacionResponse[]>([]);
  periodoAutoSeleccionado = signal(false);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  savingCell = signal<string | null>(null);
  q = signal('');

  /** Grupos de columnas por sección (encabezado agrupado SER/SABER/HACER…). */
  grupos = computed(() => {
    const p = this.planilla();
    if (!p) return [];
    const mapa = new Map<
      string,
      { id: string; nombre: string; cols: PlanillaColumna[]; maximo: number }
    >();
    for (const col of p.columnas) {
      const g = mapa.get(col.seccionEvaluacionId) ?? {
        id: col.seccionEvaluacionId,
        nombre: col.seccionNombre ?? 'Evaluaciones',
        cols: [],
        maximo: 0,
      };
      g.cols.push(col);
      g.maximo += Number(col.puntajeMaximo);
      mapa.set(col.seccionEvaluacionId, g);
    }
    return [...mapa.values()];
  });

  filasVisibles = computed(() => {
    const p = this.planilla();
    if (!p) return [];
    const q = this.q().trim().toLowerCase();
    if (!q) return p.filas;
    return p.filas.filter(
      (f) => f.nombreCompleto.toLowerCase().includes(q) || f.rude.toLowerCase().includes(q),
    );
  });

  /** % de celdas con nota sobre el total estudiantes × evaluaciones. */
  progreso = computed(() => {
    const p = this.planilla();
    if (!p || p.filas.length === 0 || p.columnas.length === 0) return 0;
    let llenas = 0;
    for (const f of p.filas) {
      for (const c of p.columnas) if (f.notas[c.evaluacionId] != null) llenas++;
    }
    return Math.round((llenas / (p.filas.length * p.columnas.length)) * 100);
  });

  estadoTexto = computed(() => {
    const p = this.planilla();
    if (!p) return 'Cargando…';
    if (!p.editable) return 'Solo lectura';
    return this.savingCell() ? 'Guardando…' : 'Sincronizado';
  });

  edits: Record<string, number | null> = {};
  erroresCelda: Record<string, string> = {};
  private baseline: Record<string, number | null> = {};

  materiaId = '';
  cursoId = '';
  paraleloId = '';
  periodoId = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    this.materiaId = q.get('materiaId') ?? '';
    this.cursoId = q.get('cursoId') ?? '';
    this.paraleloId = q.get('paraleloId') ?? '';
    this.periodoId = q.get('periodoId') ?? '';
    if (!this.materiaId || !this.cursoId || !this.paraleloId) {
      this.notFound.set(true);
      return;
    }
    if (this.periodoId) {
      this.periodoAutoSeleccionado.set(true);
      this.cargarPlanilla();
    } else {
      this.cargarPeriodosYPlanilla();
    }
  }

  celdaKey(estudianteId: string, evaluacionId: string): string {
    return `${estudianteId}:${evaluacionId}`;
  }

  onPeriodoChange(periodoId: string): void {
    this.periodoId = periodoId;
    this.cargarPlanilla();
  }

  onNotaChange(estudianteId: string, evaluacionId: string, valor: number | null): void {
    const key = this.celdaKey(estudianteId, evaluacionId);
    this.edits[key] = valor;
    delete this.erroresCelda[key];
  }

  onNotaKeydown(
    event: Event,
    rowIndex: number,
    _colIndex: number,
    estudianteId: string,
    evaluacionId: string,
    puntajeMaximo: number,
  ): void {
    const ke = event as KeyboardEvent;
    if (ke.key !== 'Enter') return;
    ke.preventDefault();
    this.persistir(estudianteId, evaluacionId, puntajeMaximo, () => {
      const filas = this.planilla()?.filas ?? [];
      const next = filas[rowIndex + 1];
      if (!next) return;
      const nextKey = this.celdaKey(next.estudianteId, evaluacionId);
      Promise.resolve().then(() => {
        const el = document.querySelector(
          `input[name="${CSS.escape(nextKey)}"]`,
        ) as HTMLInputElement | null;
        el?.focus();
        el?.select();
      });
    });
  }

  persistir(
    estudianteId: string,
    evaluacionId: string,
    puntajeMaximo: number,
    afterOk?: () => void,
  ): void {
    const key = this.celdaKey(estudianteId, evaluacionId);
    if (!this.planilla()?.editable) return;
    const actual = this.edits[key] ?? null;
    const previo = this.baseline[key] ?? null;
    if (valoresIguales(actual, previo)) return;

    if (actual != null && (actual < 0 || actual > puntajeMaximo)) {
      this.erroresCelda[key] = `Fuera de rango [0, ${puntajeMaximo}]`;
      return;
    }

    this.savingCell.set(key);
    this.errorMsg.set(null);
    this.http
      .put(`${ApiBase.BASE}/evaluaciones/${evaluacionId}/calificaciones`, {
        items: [{ estudianteId, valor: actual }],
      })
      .subscribe({
        next: () => {
          this.baseline[key] = actual;
          this.savingCell.set(null);
          delete this.erroresCelda[key];
          this.refrescarFilaYPromedio(estudianteId);
          afterOk?.();
        },
        error: (err: HttpErrorResponse) => {
          this.savingCell.set(null);
          const codigo = err.error?.codigo as string | undefined;
          if (codigo === 'E_RANGO_INVALIDO') {
            this.erroresCelda[key] = err.error?.mensaje ?? 'Rango inválido';
            this.edits[key] = previo;
          } else {
            this.errorMsg.set(err.error?.mensaje ?? 'No se pudo guardar la nota.');
            this.edits[key] = previo;
          }
        },
      });
  }

  volver(): void {
    void this.router.navigate(['/academico/mis-cursos']);
  }

  irEvaluaciones(): void {
    void this.router.navigate(['/academico/materias', this.materiaId, 'evaluaciones']);
  }

  private cargarPeriodosYPlanilla(): void {
    this.http.get<GestionEscolarResponse>(`${ApiBase.BASE}/gestiones-escolares/activa`).subscribe({
      next: () => {
        this.http
          .get<PeriodoEvaluacionResponse[]>(`${ApiBase.BASE}/gestiones-escolares/activa/periodos`)
          .subscribe({
            next: (lista) => this.resolverPeriodo(lista),
            error: () => this.errorMsg.set('Error al cargar periodos.'),
          });
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) {
          this.errorMsg.set('No hay gestión escolar activa.');
        } else {
          this.errorMsg.set('Error al cargar la gestión activa.');
        }
      },
    });
  }

  private resolverPeriodo(lista: PeriodoEvaluacionResponse[]): void {
    this.periodos.set(lista);
    const abiertos = lista.filter((p) => p.estado === 'ABIERTO');
    if (abiertos.length === 1) {
      this.periodoAutoSeleccionado.set(true);
      this.periodoId = abiertos[0].id;
      this.cargarPlanilla();
    } else if (lista.length === 1) {
      this.periodoAutoSeleccionado.set(true);
      this.periodoId = lista[0].id;
      this.cargarPlanilla();
    } else {
      this.periodoAutoSeleccionado.set(false);
      this.periodoId = abiertos[0]?.id ?? '';
      if (this.periodoId) this.cargarPlanilla();
    }
  }

  private cargarPlanilla(): void {
    if (!this.periodoId) return;
    const params = new HttpParams()
      .set('cursoId', this.cursoId)
      .set('paraleloId', this.paraleloId)
      .set('periodoId', this.periodoId);
    this.http
      .get<PlanillaMateriaResponse>(`${ApiBase.BASE}/materias/${this.materiaId}/planilla`, {
        params,
      })
      .subscribe({
        next: (p) => {
          this.planilla.set(p);
          this.sincronizarEdits(p);
          this.notFound.set(false);
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 404) this.notFound.set(true);
          else this.errorMsg.set('Error al cargar la planilla.');
        },
      });
  }

  private sincronizarEdits(p: PlanillaMateriaResponse): void {
    const next: Record<string, number | null> = {};
    for (const fila of p.filas) {
      for (const col of p.columnas) {
        const key = this.celdaKey(fila.estudianteId, col.evaluacionId);
        const v = fila.notas[col.evaluacionId];
        next[key] = v == null ? null : Number(v);
      }
    }
    this.edits = next;
    this.baseline = { ...next };
    this.erroresCelda = {};
  }

  private refrescarFilaYPromedio(estudianteId: string): void {
    // Re-GET planilla: promedio solo del servidor (CalculoNotas).
    const params = new HttpParams()
      .set('cursoId', this.cursoId)
      .set('paraleloId', this.paraleloId)
      .set('periodoId', this.periodoId);
    this.http
      .get<PlanillaMateriaResponse>(`${ApiBase.BASE}/materias/${this.materiaId}/planilla`, {
        params,
      })
      .subscribe({
        next: (p) => {
          this.planilla.set(p);
          const fila = p.filas.find((f: PlanillaFila) => f.estudianteId === estudianteId);
          if (!fila) return;
          for (const col of p.columnas) {
            const key = this.celdaKey(estudianteId, col.evaluacionId);
            const v = fila.notas[col.evaluacionId];
            this.edits[key] = v == null ? null : Number(v);
            this.baseline[key] = this.edits[key];
          }
        },
      });
  }
}

function valoresIguales(a: number | null, b: number | null): boolean {
  if (a == null && b == null) return true;
  if (a == null || b == null) return false;
  return Number(a) === Number(b);
}
