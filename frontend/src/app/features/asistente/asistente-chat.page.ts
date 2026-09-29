import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
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
 */
@Component({
  selector: 'app-asistente-chat-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div style="max-width: 800px; margin: 0 auto;">
      <h2 data-testid="asistente-heading">Asistente</h2>
      <p style="color: #555; margin-bottom: 1rem;">
        Pregunta en lenguaje natural sobre cursos, materias, estudiantes, notas o promedios.
        No hace falta conocer IDs. Si hay varios alumnos o periodos, el asistente pedirá aclaración.
      </p>

      <p style="display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 1rem;">
        @for (frase of frases; track frase) {
          <button
            type="button"
            [attr.data-testid]="'asistente-chip'"
            [disabled]="loading()"
            (click)="usarFrase(frase)"
            style="padding: 0.25rem 0.6rem; cursor: pointer; border-radius: 999px; border: 1px solid #ccc; background: #fff;"
          >
            {{ frase }}
          </button>
        }
      </p>

      @if (mensajes().length > 0) {
        <div data-testid="asistente-hilo" style="display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1rem;">
          @for (m of mensajes(); track $index) {
            <article
              [attr.data-testid]="m.role === 'user' ? 'asistente-msg-user' : 'asistente-respuesta'"
              style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid #eee;"
              [style.background]="m.role === 'user' ? '#eef3fb' : '#fafafa'"
              [style.align-self]="m.role === 'user' ? 'flex-end' : 'stretch'"
              [style.max-width]="m.role === 'user' ? '85%' : '100%'"
            >
              @if (m.camino) {
                <p style="margin: 0 0 0.4rem;">
                  <span
                    data-testid="asistente-camino"
                    [style.background]="m.camino === 'BLOQUEADO' ? '#fdecea' : (m.camino === 'SALUDO' ? '#e8f5e9' : (m.camino === 'PROCESO' ? '#e3f2fd' : '#e8eaf6'))"
                    style="display: inline-block; font-size: 0.75rem; padding: 0.15rem 0.5rem; border-radius: 4px;"
                  >{{ m.camino }}</span>
                </p>
              }
              <p style="white-space: pre-wrap; margin: 0;">{{ m.content }}</p>
            </article>
          }
        </div>
      }

      <form (ngSubmit)="enviar()" style="display: flex; gap: 0.5rem; margin-bottom: 1rem;">
        <textarea
          data-testid="asistente-pregunta"
          [(ngModel)]="pregunta"
          name="pregunta"
          rows="3"
          placeholder="Ej. ¿Cuáles son las notas de Juan del primer trimestre?"
          [disabled]="loading()"
          style="flex: 1; padding: 0.5rem; resize: vertical;"
        ></textarea>
        <button
          type="submit"
          data-testid="asistente-enviar"
          [disabled]="loading() || !pregunta.trim()"
          style="padding: 0.5rem 1rem; cursor: pointer; align-self: flex-end;"
        >
          {{ loading() ? 'Consultando…' : 'Enviar' }}
        </button>
      </form>

      @if (errorMsg()) {
        <div
          data-testid="asistente-error"
          style="background: #fdecea; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;"
        >
          {{ errorMsg() }}
        </div>
      }

      @if (resultado(); as r) {
        <p style="font-size: 0.85rem; color: #666; margin-bottom: 0.5rem;">
          {{ r.fuente }} · {{ r.turnos }} turno(s)
          @if (r.herramientasUsadas.length > 0) {
            · tools:
            @for (t of r.herramientasUsadas; track t; let last = $last) {
              <code>{{ t }}</code>@if (!last) {, }
            }
          }
        </p>
        @if (r.steps && r.steps.length > 0) {
          <ul data-testid="asistente-steps" style="font-size: 0.8rem; color: #555; margin: 0 0 0.75rem; padding-left: 1.2rem;">
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
          <p style="margin: 0.75rem 0 0; display: flex; gap: 0.5rem;">
            <button
              type="button"
              data-testid="asistente-confirmar"
              [disabled]="loading()"
              (click)="confirmar()"
              style="padding: 0.4rem 0.8rem; cursor: pointer;"
            >
              Confirmar
            </button>
            <button
              type="button"
              data-testid="asistente-cancelar"
              [disabled]="loading()"
              (click)="cancelar()"
              style="padding: 0.4rem 0.8rem; cursor: pointer;"
            >
              Cancelar
            </button>
          </p>
        }
      }
    </div>
  `,
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
