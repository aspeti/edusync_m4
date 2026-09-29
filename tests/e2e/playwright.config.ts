import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Suite E2E EduSync (M7 Día 4 — patrón Lab2).
 *
 * Prerrequisito: stack local levantado (Postgres + Spring :8080 + `ng serve` :4200
 * con proxy `/api` → backend). Playwright NO levanta el monolito completo:
 * `reuseExistingServer` asume que ya está en marcha.
 *
 * Credenciales: E2E_SYSADMIN_EMAIL / E2E_SYSADMIN_PASSWORD (ver .env.e2e.example).
 */
function loadEnvE2e(): void {
  const candidates = [
    path.resolve(__dirname, '.env.e2e'),
    path.resolve(__dirname, '../../.env'),
  ];
  for (const file of candidates) {
    if (!fs.existsSync(file)) continue;
    const text = fs.readFileSync(file, 'utf8');
    for (const line of text.split(/\r?\n/)) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('#')) continue;
      const eq = trimmed.indexOf('=');
      if (eq < 1) continue;
      const key = trimmed.slice(0, eq).trim();
      let val = trimmed.slice(eq + 1).trim();
      if (
        (val.startsWith('"') && val.endsWith('"')) ||
        (val.startsWith("'") && val.endsWith("'"))
      ) {
        val = val.slice(1, -1);
      }
      if (!(key in process.env)) {
        process.env[key] = val;
      }
    }
  }
}

loadEnvE2e();

const baseURL = process.env.E2E_BASE_URL || 'http://127.0.0.1:4200';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
  use: {
    baseURL,
    trace: 'on',
    screenshot: 'on',
    headless: process.env.E2E_HEADED === '1' ? false : true,
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        ...(process.env.CHROMIUM_PATH
          ? { launchOptions: { executablePath: process.env.CHROMIUM_PATH } }
          : {}),
      },
    },
  ],
  // No webServer: el stack EduSync (DB + API + SPA) se levanta aparte.
  // Documentado en README.md.
});
