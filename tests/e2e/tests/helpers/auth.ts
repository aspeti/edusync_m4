import { Page, expect } from '@playwright/test';

/** Credenciales del seed SYSADMIN (plataforma). */
export function sysadminCreds(): { email: string; password: string } {
  return {
    email: process.env.E2E_SYSADMIN_EMAIL || 'sysadmin@edusync.local',
    password: process.env.E2E_SYSADMIN_PASSWORD || 'changeme_local_dev',
  };
}

/** Credenciales del ADMIN de tenant (usuario demo). */
export function adminCreds(): { email: string; password: string } {
  return {
    email: process.env.E2E_ADMIN_EMAIL || 'demo@mail.com',
    password: process.env.E2E_ADMIN_PASSWORD || 'Demo123*',
  };
}

/** Login por etiquetas accesibles — patrón a imitar por el Generator. */
export async function loginAs(page: Page, email: string, password: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Correo electrónico').fill(email);
  await page.getByLabel('Contraseña').fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
}

export async function loginAsSysAdmin(page: Page): Promise<void> {
  const { email, password } = sysadminCreds();
  await loginAs(page, email, password);
  await expect(page.getByTestId('tenants-heading')).toBeVisible({ timeout: 15_000 });
}

export async function loginAsAdmin(page: Page): Promise<void> {
  const { email, password } = adminCreds();
  await loginAs(page, email, password);
  await expect(page.getByTestId('usuarios-heading')).toBeVisible({ timeout: 15_000 });
}
