import { Component, Input } from '@angular/core';

/**
 * Tarjeta base del Design System EduSync (ADR-0015). Contenedor blanco con
 * sombra suave y esquinas redondeadas; expone un acento decorativo opcional
 * en la esquina superior derecha (puramente estético, `aria-hidden`), fiel
 * al prototipo Figma (nodo 399:20447).
 *
 * Primer consumidor: DD-UC-020 (login), vía
 * frontend/src/app/features/auth/login/login.page.ts.
 */
@Component({
  selector: 'app-card',
  standalone: true,
  template: `
    <div class="app-card">
      @if (accent) {
        <span class="app-card__accent" aria-hidden="true"></span>
      }
      <div class="app-card__content">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [
    `
      .app-card {
        position: relative;
        background: var(--color-surface);
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-card);
        overflow: hidden;
        padding: var(--space-xl);
        box-sizing: border-box;
      }

      .app-card__accent {
        position: absolute;
        top: 0;
        right: 0;
        width: 96px;
        height: 96px;
        background: var(--color-accent);
        border-radius: 0 0 0 100%;
        pointer-events: none;
      }

      .app-card__content {
        position: relative;
        z-index: 1;
      }
    `,
  ],
})
export class CardComponent {
  /** Muestra el acento decorativo de esquina superior derecha (default: true). */
  @Input() accent = true;
}
