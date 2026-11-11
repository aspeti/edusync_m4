import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { MateriaResponse } from './materia.model';
import { ApiBase } from '../../core/api/api-base';

/** Mis materias (PROFESOR, DD-UC-017) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-mis-materias-page',
  standalone: true,
  imports: [TableModule, ButtonModule, MessageModule],
  template: `
    <div class="page">
      <h2>Mis materias</h2>

      @if (errorMsg()) {
        <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
      }

      <p-table
        [value]="materias()"
        dataKey="id"
        [loading]="loading()"
        [tableStyle]="{ 'min-width': '24rem' }"
        styleClass="p-datatable-sm"
      >
        <ng-template #header>
          <tr>
            <th>Materia</th>
            <th>Acciones</th>
          </tr>
        </ng-template>
        <ng-template #body let-materia>
          <tr>
            <td>{{ materia.nombre }}</td>
            <td>
              <p-button
                label="Evaluaciones"
                [link]="true"
                size="small"
                (onClick)="irEvaluaciones(materia.id)"
              />
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage>
          <tr>
            <td colspan="2">No tiene materias asignadas.</td>
          </tr>
        </ng-template>
      </p-table>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 800px;
        margin: 0 auto;
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
    `,
  ],
})
export class MisMateriasPage implements OnInit {
  materias = signal<MateriaResponse[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.http.get<MateriaResponse[]>(`${ApiBase.BASE}/materias/mias`).subscribe({
      next: (lista) => {
        this.materias.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMsg.set('Error al cargar sus materias.');
      },
    });
  }

  irEvaluaciones(id: string): void {
    this.router.navigate(['/academico/materias', id, 'evaluaciones']);
  }
}
