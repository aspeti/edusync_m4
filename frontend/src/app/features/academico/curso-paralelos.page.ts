import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ParaleloResponse } from './curso.model';
import { ApiBase } from '../../core/api/api-base';

/** Paralelos de un Curso (DD-UC-011) — UI PrimeNG (ADR-0026). */
@Component({
  selector: 'app-curso-paralelos-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, MessageModule],
  template: `
    <div class="page">
      <p-button label="← Volver a Cursos" [link]="true" (onClick)="volver()" styleClass="back" />

      <h2>Paralelos de "{{ nombreCurso || 'este Curso' }}"</h2>

      @if (notFound()) {
        <p-message
          severity="error"
          text="El Curso no existe o no pertenece a su institución."
          styleClass="block-msg"
        />
      } @else {
        @if (errorMsg()) {
          <p-message severity="error" [text]="errorMsg()!" styleClass="block-msg" />
        }

        <p-table
          [value]="paralelos()"
          dataKey="id"
          [loading]="loading()"
          [tableStyle]="{ 'min-width': '20rem' }"
          styleClass="p-datatable-sm"
        >
          <ng-template #header>
            <tr>
              <th>Nombre</th>
            </tr>
          </ng-template>
          <ng-template #body let-paralelo>
            <tr>
              <td>{{ paralelo.nombre }}</td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td>Este Curso todavía no tiene paralelos.</td>
            </tr>
          </ng-template>
        </p-table>

        <section class="create-box">
          <h3>Nuevo Paralelo</h3>
          <form class="create-form" (ngSubmit)="onSubmit()">
            <input
              pInputText
              type="text"
              [(ngModel)]="nombreNuevoParalelo"
              name="nombreNuevoParalelo"
              required
              placeholder="Ej: A"
              class="grow"
            />
            <p-button
              type="submit"
              [label]="saving() ? 'Creando...' : 'Crear'"
              [disabled]="saving()"
            />
          </form>
        </section>
      }
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 700px;
        margin: 0 auto;
      }
      :host ::ng-deep .back {
        margin-bottom: var(--space-md);
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .create-box {
        margin-top: var(--space-lg);
        padding: var(--space-md);
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
      }
      .create-box h3 {
        margin-top: 0;
      }
      .create-form {
        display: flex;
        gap: var(--space-sm);
        flex-wrap: wrap;
      }
      .grow {
        flex: 1;
        min-width: 8rem;
      }
    `,
  ],
})
export class CursoParalelosPage implements OnInit {
  cursoId = '';
  nombreCurso = '';
  paralelos = signal<ParaleloResponse[]>([]);
  loading = signal(true);
  notFound = signal(false);
  nombreNuevoParalelo = '';
  saving = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cursoId = this.route.snapshot.paramMap.get('id') ?? '';
    this.nombreCurso = this.route.snapshot.queryParamMap.get('nombre') ?? '';
    this.cargarParalelos();
  }

  volver(): void {
    this.router.navigate(['/academico/cursos']);
  }

  cargarParalelos(): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.http.get<ParaleloResponse[]>(`${ApiBase.BASE}/cursos/${this.cursoId}/paralelos`).subscribe({
      next: (paralelos) => {
        this.paralelos.set(paralelos);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 404) {
          this.notFound.set(true);
        } else {
          this.errorMsg.set('Error al cargar los paralelos.');
        }
      },
    });
  }

  onSubmit(): void {
    if (!this.nombreNuevoParalelo.trim()) return;
    this.saving.set(true);
    this.errorMsg.set(null);

    this.http
      .post<ParaleloResponse>(`${ApiBase.BASE}/cursos/${this.cursoId}/paralelos`, {
        nombre: this.nombreNuevoParalelo,
      })
      .subscribe({
        next: (nuevo) => {
          this.paralelos.update((lista) => [...lista, nuevo]);
          this.nombreNuevoParalelo = '';
          this.saving.set(false);
        },
        error: (err) => {
          this.saving.set(false);
          if (err.status === 404) {
            this.notFound.set(true);
          } else if (err.status === 400) {
            this.errorMsg.set('Verifique el nombre ingresado.');
          } else {
            this.errorMsg.set('Error al crear el Paralelo. Intente nuevamente.');
          }
        },
      });
  }
}
