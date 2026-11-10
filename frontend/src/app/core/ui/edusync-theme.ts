import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

/**
 * Tema PrimeNG de EduSync (ADR-0026 / DD-UC-032).
 * Todos los valores de color/radio/tipografía salen de `_tokens.scss`
 * vía `var(--…)`. Ningún hexadecimal aquí.
 */
export const EduSyncPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: 'var(--color-primary-50)',
      100: 'var(--color-primary-100)',
      200: 'var(--color-primary-200)',
      300: 'var(--color-primary-300)',
      400: 'var(--color-primary-400)',
      500: 'var(--color-primary-500)',
      600: 'var(--color-primary-600)',
      700: 'var(--color-primary-700)',
      800: 'var(--color-primary-800)',
      900: 'var(--color-primary-900)',
      950: 'var(--color-primary-950)',
    },
    colorScheme: {
      light: {
        surface: {
          0: 'var(--color-surface)',
          50: 'var(--color-bg)',
        },
        text: {
          color: 'var(--color-text)',
          mutedColor: 'var(--color-text-secondary)',
        },
      },
    },
  },
  components: {
    // Sin overrides de componentes: el preset semántico basta para la marca.
  },
});
