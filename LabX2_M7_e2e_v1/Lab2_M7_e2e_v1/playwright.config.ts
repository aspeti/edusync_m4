import { defineConfig, devices } from '@playwright/test';

// Config del Lab2 (M7, Dia 4). Lo importante:
//  - webServer: Playwright LEVANTA la app antes de correr y la apaga al final.
//    Nadie tiene que abrir otra terminal. Si el puerto ya esta en uso, la reutiliza.
//  - baseURL: los tests usan page.goto('/'), nunca la URL completa.
//  - trace 'on-first-retry': la traza (grabacion paso a paso) se guarda solo cuando
//    un test falla y se reintenta. Se abre con: npx playwright show-trace <archivo.zip>
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://127.0.0.1:8017',
    trace: 'on-first-retry',
    headless: false
  },
  projects: [{
    name: 'chromium',
    use: {
      ...devices['Desktop Chrome'],
      // Si el navegador de Playwright no esta instalado (npx playwright install chromium),
      // se puede apuntar a uno ya existente con la variable CHROMIUM_PATH.
      ...(process.env.CHROMIUM_PATH ? { launchOptions: { executablePath: process.env.CHROMIUM_PATH } } : {}),
    },
  }],
  webServer: {
    command: 'python -m uvicorn app.servidor:app --port 8017',
    url: 'http://127.0.0.1:8017/salud',
    reuseExistingServer: !process.env.CI,
    timeout: 30_000,
  },
});
