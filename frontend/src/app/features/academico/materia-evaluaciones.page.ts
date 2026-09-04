import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EvaluacionResponse } from './evaluacion.model';
import { MateriaResponse } from './materia.model';
import { GestionEscolarResponse } from './gestion-escolar.model';
import { PeriodoEvaluacionResponse } from './periodo-evaluacion.model';
import { SeccionEvaluacionResponse } from './seccion-evaluacion.model';
import { ApiBase } from '../../core/api/api-base';
import { PageResponse } from '../../core/api/page-response.model';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Evaluaciones de una Materia (DD-UC-017 / FSD-UC-015): selector de gestión y
 * periodo, lista (incluye ANULADA) y alta inline si el periodo está ABIERTO.
 * ADMIN y PROFESOR. puntajeMaximo es de solo lectura.
 */
@Component({
  selector: 'app-materia-evaluaciones-page',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div style="max-width: 960px; margin: 0 auto;">
      <div style="margin-bottom: 1rem;">
        @if (auth.hasRole('ADMIN')) {
          <a [routerLink]="['/academico/materias', materiaId]" style="font-size: 0.85rem;">← Volver a la materia</a>
        } @else {
          <a routerLink="/academico/mis-materias" style="font-size: 0.85rem;">← Volver a Mis materias</a>
        }
      </div>

      @if (notFound()) {
        <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px;">
          La Materia no existe o no está asignada a su usuario.
        </div>
      } @else {
        <h2>Evaluaciones de "{{ materia()?.nombre || '…' }}"</h2>

        @if (errorMsg()) {
          <div style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
            {{ errorMsg() }}
          </div>
        }

        <div style="display: flex; gap: 1rem; flex-wrap: wrap; margin-bottom: 1.5rem;">
          <label>
            Gestión escolar<br />
            <select [(ngModel)]="gestionId" (ngModelChange)="onGestionChange()" style="padding: 0.4rem; min-width: 220px;">
              <option value="">— Seleccione —</option>
              @for (gestion of gestiones(); track gestion.id) {
                <option [value]="gestion.id">{{ gestion.nombre }} ({{ gestion.estado }})</option>
              }
            </select>
          </label>
          <label>
            Periodo<br />
            <select [(ngModel)]="periodoId" (ngModelChange)="cargarEvaluaciones()" style="padding: 0.4rem; min-width: 220px;" [disabled]="!gestionId">
              <option value="">— Seleccione —</option>
              @for (periodo of periodos(); track periodo.id) {
                <option [value]="periodo.id">{{ periodo.nombre }} ({{ periodo.estado }})</option>
              }
            </select>
          </label>
        </div>

        @if (periodoSeleccionado()?.estado === 'ABIERTO') {
          <section style="margin-bottom: 1.5rem; background: #fafafa; padding: 1rem; border-radius: 4px;">
            <h3>Nueva evaluación</h3>
            <form (ngSubmit)="crear()" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: flex-end;">
              <label>
                Nombre<br />
                <input [(ngModel)]="nuevoNombre" name="nombre" required maxlength="100" style="padding: 0.4rem;" />
              </label>
              <label>
                Sección<br />
                <select [(ngModel)]="nuevaSeccionId" name="seccion" required style="padding: 0.4rem;">
                  <option value="">—</option>
                  @for (seccion of secciones(); track seccion.id) {
                    <option [value]="seccion.id">{{ seccion.nombre }} (máx. {{ seccion.nota }})</option>
                  }
                </select>
              </label>
              <label>
                Fecha<br />
                <input type="date" [(ngModel)]="nuevaFecha" name="fecha" required style="padding: 0.4rem;" />
              </label>
              <label>
                Descripción (opcional)<br />
                <input [(ngModel)]="nuevaDescripcion" name="descripcion" style="padding: 0.4rem;" />
              </label>
              <button type="submit" [disabled]="saving()" style="padding: 0.45rem 1rem; cursor: pointer;">
                {{ saving() ? 'Guardando…' : 'Crear' }}
              </button>
            </form>
          </section>
        } @else if (periodoId) {
          <p style="color: #666; font-size: 0.9rem;">
            Solo se pueden crear o anular evaluaciones cuando el periodo está ABIERTO.
          </p>
        }

        @if (evaluaciones().length === 0 && periodoId) {
          <p>No hay evaluaciones en este periodo.</p>
        }

        @if (evaluaciones().length > 0) {
          <table style="width: 100%; border-collapse: collapse;">
            <thead>
              <tr style="background: #f5f5f5;">
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Nombre</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Sección</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Fecha</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Puntaje máx.</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;">Estado</th>
                <th style="padding: 0.5rem; text-align: left; border-bottom: 2px solid #ddd;"></th>
              </tr>
            </thead>
            <tbody>
              @for (evaluacion of evaluaciones(); track evaluacion.id) {
                <tr style="border-bottom: 1px solid #eee;" [style.opacity]="evaluacion.estado === 'ANULADA' ? '0.6' : '1'">
                  <td style="padding: 0.5rem;">{{ evaluacion.nombre }}</td>
                  <td style="padding: 0.5rem;">{{ nombreSeccion(evaluacion.seccionEvaluacionId) }}</td>
                  <td style="padding: 0.5rem;">{{ evaluacion.fecha }}</td>
                  <td style="padding: 0.5rem;">{{ evaluacion.puntajeMaximo }}</td>
                  <td style="padding: 0.5rem;">{{ evaluacion.estado }}</td>
                  <td style="padding: 0.5rem; display: flex; gap: 0.75rem; align-items: center;">
                    @if (evaluacion.estado === 'ACTIVA') {
                      <a
                        [routerLink]="['/academico/materias', materiaId, 'evaluaciones', evaluacion.id, 'calificaciones']"
                        style="font-size: 0.85rem;"
                      >Calificaciones</a>
                    }
                    @if (evaluacion.estado === 'ACTIVA' && periodoSeleccionado()?.estado === 'ABIERTO') {
                      <button (click)="anular(evaluacion)" style="cursor: pointer; font-size: 0.85rem;">Anular</button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      }
    </div>
  `,
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

  gestionId = '';
  periodoId = '';
  nuevoNombre = '';
  nuevaSeccionId = '';
  nuevaFecha = '';
  nuevaDescripcion = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    protected auth: AuthService
  ) {}

  ngOnInit(): void {
    this.materiaId = this.route.snapshot.paramMap.get('id') ?? '';
    this.http.get<MateriaResponse>(`${ApiBase.BASE}/materias/${this.materiaId}`).subscribe({
      next: (materia) => this.materia.set(materia),
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) this.notFound.set(true);
        else this.errorMsg.set('Error al cargar la materia.');
      },
    });
    const params = new HttpParams().set('page', 0).set('size', 100);
    this.http.get<PageResponse<GestionEscolarResponse>>(`${ApiBase.BASE}/gestiones-escolares`, { params }).subscribe({
      next: (respuesta) => this.gestiones.set(respuesta.content),
    });
  }

  periodoSeleccionado(): PeriodoEvaluacionResponse | undefined {
    return this.periodos().find((p) => p.id === this.periodoId);
  }

  nombreSeccion(seccionId: string): string {
    return this.secciones().find((s) => s.id === seccionId)?.nombre ?? seccionId;
  }

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
