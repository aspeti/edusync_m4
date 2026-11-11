import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { AsistenteService } from './asistente.service';
import {
  AgenteResponse,
  ContextoConsultaAgente,
  FRASES_KEYWORD,
  MAX_TURNOS_HISTORIAL,
  MensajeAsistente,
  STORAGE_CONTEXTO,
  STORAGE_HISTORIAL,
  TurnoHistorialAgente,
} from './asistente.model';

/**
 * Consola del asistente (DD-UC-024/025/026/027): KEYWORD, CONSULTA, ReAct, SALUDO o BLOQUEADO.
 * UI PrimeNG (ADR-0026).
 */
@Component({
  selector: 'app-asistente-chat-page',
  standalone: true,
  imports: [FormsModule, ButtonModule, TextareaModule, TagModule, MessageModule],
  template: `
    <div class="page">
      <h2 data-testid="asistente-heading">Asistente</h2>
      <p class="lead">
        Pregunta en lenguaje natural sobre cursos, materias, estudiantes, notas o promedios.
        No hace falta conocer IDs. Si hay varios alumnos o periodos, el asistente pedirá aclaración.
      </p>

      <div class="frases">
        @for (frase of frases; track frase) {
          <p-button
            [attr.data-testid]="'asistente-chip'"
            [label]="frase"
            severity="secondary"
            [outlined]="true"
            size="small"
            [disabled]="loading()"
            (onClick)="usarFrase(frase)"
          />
        }
      </div>

      @if (mensajes().length > 0) {
        <div data-testid="asistente-hilo" class="hilo">
          @for (m of mensajes(); track $index) {
            <article
              class="msg"
              [class.msg-user]="m.role === 'user'"
              [attr.data-testid]="m.role === 'user' ? 'asistente-msg-user' : 'asistente-respuesta'"
            >
              @if (m.camino) {
                <p class="camino-row">
                  <p-tag
                    data-testid="asistente-camino"
                    [value]="m.camino"
                    [severity]="caminoSeverity(m.camino)"
                  />
                </p>
              }
              <p class="content">{{ m.content }}</p>
            </article>
          }
        </div>
      }

      <form class="composer" (ngSubmit)="enviar()">
        <textarea
          pTextarea
          data-testid="asistente-pregunta"
          [(ngModel)]="pregunta"
          name="pregunta"
          rows="3"
          placeholder="Ej. ¿Cuáles son las notas de Juan del primer trimestre?"
          [disabled]="loading()"
          class="composer-input"
        ></textarea>
        <p-button
          type="submit"
          data-testid="asistente-enviar"
          [label]="loading() ? 'Consultando…' : 'Enviar'"
          [disabled]="loading() || !pregunta.trim()"
        />
      </form>

      @if (errorMsg()) {
        <p-message
          data-testid="asistente-error"
          severity="error"
          [text]="errorMsg()!"
          styleClass="block-msg"
        />
      }

      @if (resultado(); as r) {
        <p class="meta">
          {{ r.fuente }} · {{ r.turnos }} turno(s)
          @if (r.herramientasUsadas.length > 0) {
            · tools:
            @for (t of r.herramientasUsadas; track t; let last = $last) {
              <code>{{ t }}</code>@if (!last) {, }
            }
          }
        </p>
        @if (r.steps && r.steps.length > 0) {
          <ul data-testid="asistente-steps" class="steps">
            @for (s of r.steps; track s.paso) {
              <li>
                {{ s.toolId }}
                @if (s.tablasFuente.length > 0) {
                  · tablas: {{ s.tablasFuente.join(', ') }}
                }
                · {{ s.exito ? 'ok' : 'error' }}
              </li>
            }
          </ul>
        }
        @if (r.confirmacionRequerida) {
          <div class="confirm">
            <p-button
              data-testid="asistente-confirmar"
              label="Confirmar"
              [disabled]="loading()"
              (onClick)="confirmar()"
            />
            <p-button
              data-testid="asistente-cancelar"
              label="Cancelar"
              severity="secondary"
              [outlined]="true"
              [disabled]="loading()"
              (onClick)="cancelar()"
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
      .lead {
        color: var(--color-text-secondary);
        margin-bottom: var(--space-md);
      }
      .frases {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
        margin-bottom: var(--space-md);
      }
      .hilo {
        display: flex;
        flex-direction: column;
        gap: var(--space-md);
        margin-bottom: var(--space-md);
      }
      .msg {
        padding: var(--space-md);
        border-radius: var(--radius-sm);
        border: 1px solid var(--color-border);
        background: var(--color-surface);
        align-self: stretch;
        max-width: 100%;
      }
      .msg-user {
        background: color-mix(in srgb, var(--color-primary) 8%, white);
        align-self: flex-end;
        max-width: 85%;
      }
      .camino-row {
        margin: 0 0 var(--space-sm);
      }
      .content {
        white-space: pre-wrap;
        margin: 0;
      }
      .composer {
        display: flex;
        gap: var(--space-sm);
        margin-bottom: var(--space-md);
        align-items: flex-end;
      }
      .composer-input {
        flex: 1;
        width: 100%;
      }
      .block-msg {
        display: block;
        margin-bottom: var(--space-md);
      }
      .meta {
        font-size: var(--font-size-small);
        color: var(--color-text-secondary);
        margin-bottom: var(--space-sm);
      }
      .steps {
        font-size: var(--font-size-small);
        color: var(--color-text-secondary);
        margin: 0 0 var(--space-md);
        padding-left: 1.2rem;
      }
      .confirm {
        margin-top: var(--space-md);
        display: flex;
        gap: var(--space-sm);
      }
    `,
  ],
})
export class AsistenteChatPage {
  pregunta = '';
  private pendiente = '';
  readonly frases = FRASES_KEYWORD;
  readonly loading = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly resultado = signal<AgenteResponse | null>(null);
  readonly mensajes = signal<MensajeAsistente[]>(this.leerHistorial());
  private contexto: ContextoConsultaAgente | null = this.leerContexto();

  constructor(private readonly asistente: AsistenteService) {}

  caminoSeverity(camino: string): 'danger' | 'success' | 'info' | 'secondary' {
    if (camino === 'BLOQUEADO') return 'danger';
    if (camino === 'SALUDO') return 'success';
    if (camino === 'PROCESO') return 'info';
    return 'secondary';
  }

  usarFrase(frase: string): void {
    this.pregunta = frase;
    this.enviar();
  }

  enviar(): void {
    const texto = this.pregunta.trim();
    if (!texto || this.loading()) return;
    const confirmar =
      texto.toLowerCase() === 'confirmo' && this.pendiente !== '';
    this.consultar(confirmar ? this.pendiente : texto, confirmar);
  }

  confirmar(): void {
    if (!this.pendiente || this.loading()) return;
    this.consultar(this.pendiente, true);
  }

  cancelar(): void {
    this.pendiente = '';
    this.resultado.set(null);
  }

  private consultar(texto: string, confirmed: boolean): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.pregunta = '';
    const history = this.aHistory();
    this.mensajes.update((prev) => [...prev, { role: 'user', content: texto }]);
    this.persistir();

    this.asistente.consultar(texto, confirmed, history, this.contexto).subscribe({
      next: (respuesta) => {
        this.resultado.set(respuesta);
        this.contexto = respuesta.contexto ?? this.contexto;
        this.pendiente = respuesta.confirmacionRequerida ? texto : '';
        this.mensajes.update((prev) => [
          ...prev,
          { role: 'assistant', content: respuesta.respuesta, camino: respuesta.camino },
        ]);
        this.persistir();
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(this.mensajeError(err));
        this.loading.set(false);
      },
    });
  }

  private aHistory(): TurnoHistorialAgente[] {
    return this.mensajes()
      .slice(-MAX_TURNOS_HISTORIAL)
      .map((m) => ({ role: m.role, content: m.content }));
  }

  private persistir(): void {
    try {
      sessionStorage.setItem(STORAGE_HISTORIAL, JSON.stringify(this.mensajes()));
      sessionStorage.setItem(STORAGE_CONTEXTO, JSON.stringify(this.contexto));
    } catch {
      // sessionStorage puede estar deshabilitado
    }
  }

  private leerHistorial(): MensajeAsistente[] {
    try {
      const raw = sessionStorage.getItem(STORAGE_HISTORIAL);
      return raw ? (JSON.parse(raw) as MensajeAsistente[]) : [];
    } catch {
      return [];
    }
  }

  private leerContexto(): ContextoConsultaAgente | null {
    try {
      const raw = sessionStorage.getItem(STORAGE_CONTEXTO);
      return raw ? (JSON.parse(raw) as ContextoConsultaAgente) : null;
    } catch {
      return null;
    }
  }

  private mensajeError(err: HttpErrorResponse): string {
    const codigo = err.error?.codigo as string | undefined;
    if (err.status === 401) return 'Sesión expirada. Vuelve a iniciar sesión.';
    if (err.status === 429 || codigo === 'E_LIMITE_TURNOS') {
      return 'La consulta requirió demasiados pasos. Reformula la pregunta.';
    }
    if (err.status === 503 || codigo === 'E_AI_DESHABILITADO') {
      return 'El asistente está deshabilitado en este entorno.';
    }
    if (err.status === 502 || codigo === 'E_LLM_NO_DISPONIBLE' || codigo === 'E_HERRAMIENTA_NO_DISPONIBLE') {
      return 'No se pudo completar la consulta (Ollama o una herramienta no disponible).';
    }
    return 'No se pudo consultar el asistente.';
  }
}
