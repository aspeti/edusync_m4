import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { EduSyncPreset } from './core/ui/edusync-theme';

/**
 * Configuración de la aplicación Angular 21 (standalone).
 * - provideHttpClient con el interceptor Bearer (DD-UC-004 §2).
 * - provideRouter con las rutas protegidas por authGuard/roleGuard.
 * - providePrimeNG con el tema EduSync (ADR-0026 / DD-UC-032).
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    providePrimeNG({
      theme: {
        preset: EduSyncPreset,
        options: {
          // Figma no define modo oscuro (DD-UC-020 / DD-UC-032).
          darkModeSelector: false,
        },
      },
    }),
  ],
};
