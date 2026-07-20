import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Componente raíz: solo provee el router-outlet principal.
 * El layout post-login (ShellComponent) y las páginas se cargan lazy.
 * DD-UC-004 §2.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
