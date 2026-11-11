import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { AsignacionProfesorVistaResponse, ProfesorResponse } from './profesor.model';
import { ApiBase } from '../../core/api/api-base';

/** Detalle Profesor (DD-UC-014) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-profesor-detalle-page',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, MessageModule],
  template: `
    <div class="page">
      <p-button label="← Volver a Profesores" [link]="true" (onClick)="volver()" />

      @if (notFound()) {
        <p-message severity="error" text="El Profesor no existe o no pertenece a su institución." styleClass="block-msg" />
      } @else {
        <h2>{{ profesor()?.nombreCompleto || 'Profesor' }}</h2>
        @if (profesor()) {
          <p class="meta">
            <p-tag
              [value]="profesor()!.activo ? 'Activo' : 'Inactivo'"
              [severity]="profesor()!.activo ? 'success' : 'secondary'"
            />
          </p>
        }

        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        <h3>Asignaciones</h3>
        <p-table
          [value]="asignaciones()"
          dataKey="id"
          [tableStyle]="{ 'min-width': '30rem' }"
          styleClass="p-datatable-sm"
        >
          <ng-template #header>
            <tr>
              <th>Materia</th>
              <th>Curso</th>
              <th>Paralelo</th>
            </tr>
          </ng-template>
          <ng-template #body let-asignacion>
            <tr>
              <td>{{ asignacion.materiaNombre || asignacion.materiaId }}</td>
              <td>{{ asignacion.cursoNombre || asignacion.cursoId }}</td>
              <td>{{ asignacion.paraleloNombre || asignacion.paraleloId }}</td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="3">Este profesor todavía no tiene materias asignadas.</td>
            </tr>
          </ng-template>
        </p-table>
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
        margin-top: 0;
      }
      .block-msg {
        display: block;
        margin: var(--space-md) 0;
      }
    `,
  ],
})
export class ProfesorDetallePage implements OnInit {
  profesorId = '';
  profesor = signal<ProfesorResponse | null>(null);
  notFound = signal(false);
  errorMsg = signal<string | null>(null);
  asignaciones = signal<AsignacionProfesorVistaResponse[]>([]);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.profesorId = this.route.snapshot.paramMap.get('id') ?? '';
    this.cargarDetalle();
  }

  volver(): void {
    this.router.navigate(['/academico/profesores']);
  }

  private cargarDetalle(): void {
    this.http.get<ProfesorResponse>(`${ApiBase.BASE}/profesores/${this.profesorId}`).subscribe({
      next: (profesor) => this.profesor.set(profesor),
      error: (err) => {
        if (err.status === 404) this.notFound.set(true);
        else this.errorMsg.set('Error al cargar el profesor.');
      },
    });
    this.http
      .get<AsignacionProfesorVistaResponse[]>(`${ApiBase.BASE}/profesores/${this.profesorId}/asignaciones`)
      .subscribe({
        next: (lista) => this.asignaciones.set(lista),
        error: (err) => {
          if (err.status === 404) this.notFound.set(true);
          else this.errorMsg.set('Error al cargar las asignaciones.');
        },
      });
  }
}
