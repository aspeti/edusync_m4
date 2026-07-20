import { Component } from '@angular/core';

/**
 * Placeholder de home para roles distintos de SYSADMIN (DD-UC-004 §1 — fuera de alcance).
 * El home funcional de ADMIN se implementará en DDs posteriores.
 */
@Component({
  selector: 'app-home-page',
  standalone: true,
  template: `
    <div style="max-width: 600px; margin: 4rem auto; text-align: center;">
      <h2>Bienvenido a EduSync</h2>
      <p>El portal de su institución estará disponible próximamente.</p>
    </div>
  `,
})
export class HomePage {}
