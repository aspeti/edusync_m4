import { test, expect } from '@playwright/test';
import { loginAsSysAdmin } from '../helpers/auth';

test.describe('Plataforma SYSADMIN', () => {
  test('plat 1.1 Abrir formulario Nuevo Tenant (auditado)', async ({ page }) => {
    await loginAsSysAdmin(page);
    await page.getByRole('link', { name: '+ Nuevo Tenant' }).click();

    await expect(page.getByRole('heading', { name: /Nuevo Tenant — Paso 1/ })).toBeVisible();
    await expect(page.getByLabel('Nombre del tenant')).toBeVisible();
    await expect(page.getByLabel('Fecha inicio suscripción')).toBeVisible();
    await expect(page.getByLabel('Fecha vencimiento suscripción')).toBeVisible();
  });

  test('plat 1.2 Crear Tenant y llegar al paso admin (auditado)', async ({ page }) => {
    const nombre = `E2E Tenant ${Date.now()}`;
    const y = new Date().getFullYear();
    await loginAsSysAdmin(page);
    await page.getByRole('link', { name: '+ Nuevo Tenant' }).click();

    await page.getByLabel('Nombre del tenant').fill(nombre);
    await page.getByLabel('Fecha inicio suscripción').fill(`${y}-01-01`);
    await page.getByLabel('Fecha vencimiento suscripción').fill(`${y}-12-31`);
    await page.getByRole('button', { name: /Crear Tenant/ }).click();

    await expect(page).toHaveURL(/\/plataforma\/tenants\/.+\/admin/, { timeout: 15_000 });
    await expect(page.getByRole('heading', { name: /Paso 2 de 2/ })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Crear Admin' })).toBeVisible();
  });
});
