import { Component, Input } from '@angular/core';

/**
 * Botón del Design System EduSync (ADR-0015): variantes `primary`/`secondary`
 * y ancho completo opcional. Proyecta su contenido vía `<ng-content>` para no
 * fijar copy en el componente (permite el swap "Iniciar Sesión" ↔
 * "Ingresando..." de `login.page.ts` sin tocar este componente).
 *
 * Primer consumidor: DD-UC-020 (login), vía
 * frontend/src/app/features/auth/login/login.page.ts.
 */
@Component({
  selector: 'app-button',
  standalone: true,
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      class="app-button"
      [class.app-button--secondary]="variant === 'secondary'"
      [class.app-button--full]="fullWidth"
    >
      <ng-content></ng-content>
    </button>
  `,
  styles: [
    `
      .app-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: var(--space-xs);
        padding: var(--space-sm) var(--space-lg);
        font-size: var(--font-size-body);
        font-family: var(--font-family-base);
        font-weight: 600;
        color: #ffffff;
        background: var(--color-primary);
        border: 1px solid var(--color-primary);
        border-radius: var(--radius-sm);
        cursor: pointer;
        transition: background-color 0.15s ease;
      }

      .app-button:hover:not(:disabled) {
        background: var(--color-primary-hover);
      }

      .app-button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .app-button--secondary {
        color: var(--color-primary);
        background: transparent;
      }

      .app-button--secondary:hover:not(:disabled) {
        background: var(--color-accent);
      }

      .app-button--full {
        width: 100%;
      }
    `,
  ],
})
export class ButtonComponent {
  @Input() variant: 'primary' | 'secondary' = 'primary';
  @Input() fullWidth = false;
  @Input() type: 'button' | 'submit' = 'button';
  @Input() disabled = false;
}
