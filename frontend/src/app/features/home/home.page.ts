import { Component } from '@angular/core';

/**
 * Placeholder de home para roles distintos de SYSADMIN (DD-UC-004 §1).
 */
@Component({
  selector: 'app-home-page',
  standalone: true,
  template: `
    <div class="page">
      <h2>Bienvenido a EduSync</h2>
      <p>El portal de su institución estará disponible próximamente.</p>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 600px;
        margin: var(--space-xl) auto;
        text-align: center;
        color: var(--color-text);
      }
      p {
        color: var(--color-text-secondary);
      }
    `,
  ],
})
export class HomePage {}
